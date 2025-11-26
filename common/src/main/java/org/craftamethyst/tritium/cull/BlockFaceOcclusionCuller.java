package org.craftamethyst.tritium.cull;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class BlockFaceOcclusionCuller {
    private static final AtomicBoolean FALLBACK_MODE = new AtomicBoolean(false);

    private static final int TRACE_DISTANCE = 16;

    private static final double SAMPLE_OFFSET = 0.2;

    private static final Object2BooleanOpenHashMap<Key> BLOCK_CACHE = new Object2BooleanOpenHashMap<>(16_000);

    private static final ConcurrentMap<Key, CompletableFuture<Boolean>> INFLIGHT = new ConcurrentHashMap<>();

    private static ExecutorService tracerPool;
    private static ScheduledExecutorService timeoutChecker;
    private static final AtomicInteger PENDING = new AtomicInteger();
    private static long lastCacheCleanup = System.currentTimeMillis();

    static {
        initExecutors();
    }

    public static boolean shouldCullBlockFace(BlockGetter level, BlockPos pos, Direction face) {
        if (FALLBACK_MODE.get()) {
            return LeafCulling.checkSimpleConnection(level, pos.relative(face), face);
        }

        long now = System.currentTimeMillis();
        if (now - lastCacheCleanup > 1000) {
            synchronized (BLOCK_CACHE) {
                BLOCK_CACHE.clear();
            }
            lastCacheCleanup = now;
        }

        final Key key = new Key(System.identityHashCode(level), pos.asLong(), (byte) face.ordinal());
        synchronized (BLOCK_CACHE) {
            if (BLOCK_CACHE.containsKey(key)) {
                return BLOCK_CACHE.getBoolean(key);
            }
        }

        final BlockPos adjacentPos = pos.relative(face);
        final BlockState neighbor = level.getBlockState(adjacentPos);

        if (neighbor.isAir()) {
            synchronized (BLOCK_CACHE) {
                BLOCK_CACHE.put(key, false);
            }
            return false;
        }

        if (neighbor.isFaceSturdy(level, adjacentPos, face.getOpposite()) ||
                LeafCulling.checkSimpleConnection(level, adjacentPos)) {
            synchronized (BLOCK_CACHE) {
                BLOCK_CACHE.put(key, true);
            }
            return true;
        }

        scheduleTrace(level, pos, face, key);
        return false;
    }

    private static void scheduleTrace(BlockGetter level, BlockPos pos, Direction face, Key key) {
        INFLIGHT.computeIfAbsent(key, k -> {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            Runnable work = () -> {
                try {
                    if (Thread.currentThread().isInterrupted()) return;
                    Vec3 startCenter = getFaceCenter(pos, face);
                    Vec3 dir = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
                    Vec3 endCenter = startCenter.add(dir.scale(TRACE_DISTANCE));
                    boolean anyVisible = traceVisibilityMultiSample(startCenter, endCenter, level, face);
                    boolean shouldCull = !anyVisible;
                    future.complete(shouldCull);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                } finally {
                    PENDING.decrementAndGet();
                }
            };

            if (PENDING.incrementAndGet() > 4096) {
                PENDING.decrementAndGet();
                future.complete(false);
            } else {
                Future<?> task = tracerPool.submit(work);
                timeoutChecker.schedule(() -> {
                    if (!future.isDone()) {
                        task.cancel(true);
                        future.complete(false);
                    }
                }, 25, TimeUnit.MILLISECONDS);
            }

            future.whenComplete((res, err) -> {
                try {
                    if (err != null) {
                        FALLBACK_MODE.set(true);
                        boolean fb = LeafCulling.checkSimpleConnection(level, pos.relative(face));
                        synchronized (BLOCK_CACHE) {
                            BLOCK_CACHE.put(key, fb);
                        }
                    } else {
                        synchronized (BLOCK_CACHE) {
                            BLOCK_CACHE.put(key, res);
                        }
                    }
                } finally {
                    INFLIGHT.remove(key);
                }
            });

            return future;
        });
    }

    private static boolean traceVisibilityMultiSample(Vec3 centerStart, Vec3 centerEnd, BlockGetter level, Direction face) {
        Vec3[] offsets = sampleOffsets(face);
        for (Vec3 off : offsets) {
            if (Thread.interrupted()) return true;
            Vec3 s = centerStart.add(off);
            Vec3 e = centerEnd.add(off);
            if (traceVisibility(s, e, level)) {
                return true;
            }
        }
        return false;
    }

    private static boolean traceVisibility(Vec3 start, Vec3 end, BlockGetter level) {
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        if (distance < 1.0e-3) return true;

        direction = direction.normalize();
        double stepSize = Math.min(0.25, Math.max(0.0625, distance / 32.0));
        int maxSteps = (int) Math.min(256, Math.ceil(distance / stepSize) + 2);

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        Vec3 current = start;
        int steps = 0;

        while (steps++ < maxSteps && current.distanceTo(start) < distance) {
            if (Thread.interrupted()) return true;

            mpos.set(current.x, current.y, current.z);
            BlockState state = level.getBlockState(mpos);

            if (!state.isAir()) {
                if (!state.getOcclusionShape(level, mpos).isEmpty() &&
                        state.getCollisionShape(level, mpos).bounds().move(mpos).contains(current)) {
                    return false;
                }
            }

            current = current.add(direction.scale(stepSize));
        }
        return true;
    }

    private static Vec3[] sampleOffsets(Direction face) {
        switch (face) {
            case UP:
            case DOWN:
                return new Vec3[]{
                        Vec3.ZERO,
                        new Vec3(+SAMPLE_OFFSET, 0, 0),
                        new Vec3(-SAMPLE_OFFSET, 0, 0),
                        new Vec3(0, 0, +SAMPLE_OFFSET),
                        new Vec3(0, 0, -SAMPLE_OFFSET)
                };
            case NORTH:
            case SOUTH:
                return new Vec3[]{
                        Vec3.ZERO,
                        new Vec3(+SAMPLE_OFFSET, 0, 0),
                        new Vec3(-SAMPLE_OFFSET, 0, 0),
                        new Vec3(0, +SAMPLE_OFFSET, 0),
                        new Vec3(0, -SAMPLE_OFFSET, 0)
                };
            case EAST:
            case WEST:
            default:
                return new Vec3[]{
                        Vec3.ZERO,
                        new Vec3(0, +SAMPLE_OFFSET, 0),
                        new Vec3(0, -SAMPLE_OFFSET, 0),
                        new Vec3(0, 0, +SAMPLE_OFFSET),
                        new Vec3(0, 0, -SAMPLE_OFFSET)
                };
        }
    }

    private static Vec3 getFaceCenter(BlockPos pos, Direction face) {
        return new Vec3(
                pos.getX() + 0.5 + face.getStepX() * 0.501,
                pos.getY() + 0.5 + face.getStepY() * 0.501,
                pos.getZ() + 0.5 + face.getStepZ() * 0.501
        );
    }

    public static boolean isInFallbackMode() {
        return FALLBACK_MODE.get();
    }

    private static synchronized void initExecutors() {
        if (tracerPool == null) {
            int cpus = Math.max(2, Runtime.getRuntime().availableProcessors());
            tracerPool = new ThreadPoolExecutor(
                    Math.max(1, cpus / 4),
                    Math.max(2, cpus / 2),
                    30L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(4096),
                    new NamedThreadFactory("Tritium-occl-trace", true),
                    new ThreadPoolExecutor.DiscardPolicy()
            );
        }
        if (timeoutChecker == null) {
            timeoutChecker = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Tritium-occl-timeout", true));
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final String baseName;
        private final boolean daemon;
        private final AtomicInteger idx = new AtomicInteger(1);

        private NamedThreadFactory(String baseName, boolean daemon) {
            this.baseName = Objects.requireNonNull(baseName);
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, baseName + "-" + idx.getAndIncrement());
            t.setDaemon(daemon);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    }

    private record Key(int levelId, long pos, byte face) { }
}