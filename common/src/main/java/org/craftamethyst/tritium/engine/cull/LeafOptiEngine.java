package org.craftamethyst.tritium.engine.cull;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class LeafOptiEngine {
    public static final Cache<Long, Boolean> CULL_CACHE = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();
    private static final Map<Direction, Direction[]> CONNECTED_DIRECTIONS = new EnumMap<>(Direction.class);
    private static final int minLeafConnections = 2;

    static {
        for (Direction face : Direction.values()) {
            Direction[] dirs = new Direction[4];
            int i = 0;
            for (Direction dir : Direction.values()) {
                if (dir != face && dir != face.getOpposite()) {
                    dirs[i++] = dir;
                }
            }
            CONNECTED_DIRECTIONS.put(face, dirs);
        }
    }

    public static boolean shouldCullFace(BlockGetter level, BlockPos pos, Direction face) {
        long cacheKey = (pos.asLong() << 8) | face.ordinal();
        Boolean cached = CULL_CACHE.getIfPresent(cacheKey);
        if (cached != null) return cached;

        BlockPos adjacentPos = pos.relative(face);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        if (!isLeaf(adjacentState)) {
            CULL_CACHE.put(cacheKey, false);
            return false;
        }

        boolean result = TritiumConfig.get().rendering.leafCulling.enableLeafCulling && !RenderOptimizer.isInFallbackMode()
                ? RenderOptimizer.shouldCullBlockFace(level, pos, face)
                : checkSimpleConnection(level, adjacentPos, face);

        CULL_CACHE.put(cacheKey, result);
        return result;
    }

    public static boolean checkSimpleConnection(BlockGetter level, BlockPos pos, Direction face) {
        int count = 0;

        for (Direction dir : CONNECTED_DIRECTIONS.get(face)) {
            if (dir == null) continue;

            if (isLeaf(level.getBlockState(pos.relative(dir)))) {
                if (++count >= minLeafConnections) return true;
            }
        }
        return false;
    }

    public static boolean checkSimpleConnection(BlockGetter level, BlockPos pos) {
        return checkConnectedLeaves(level, pos, null);
    }

    public static boolean checkConnectedLeaves(BlockGetter level, BlockPos pos, @Nullable Direction face) {
        int count = 0;

        Direction[] checkDirs = (face != null) ? CONNECTED_DIRECTIONS.get(face) : Direction.values();

        for (Direction dir : checkDirs) {
            if (dir == null) continue;

            if (isLeaf(level.getBlockState(pos.relative(dir)))) {
                if (++count >= minLeafConnections) return true;
            }
        }
        return false;
    }

    private static boolean isLeaf(BlockState state) {
        return state.getBlock() instanceof LeavesBlock;
    }
}