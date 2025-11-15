package org.craftamethyst.tritium.cull;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CullCache {
    private static final long ENTITY_CACHE_TIMEOUT = 200;
    private static final long BLOCK_ENTITY_CACHE_TIMEOUT = 500;
    private final Int2BooleanOpenHashMap entityCullCache = new Int2BooleanOpenHashMap(8192);
    private final Long2BooleanOpenHashMap blockEntityCullCache = new Long2BooleanOpenHashMap(8192);
    private final Long2LongOpenHashMap entityCacheTimestamps = new Long2LongOpenHashMap(8192);
    private final Long2LongOpenHashMap blockEntityCacheTimestamps = new Long2LongOpenHashMap(8192);
    private final CullResult cachedTrueResult = new CullResult(true, true);
    private final CullResult cachedFalseResult = new CullResult(true, false);
    private final CullResult uncachedResult = new CullResult(false, false);
    private int entityCacheHits = 0;
    private int entityCacheMisses = 0;
    private int blockEntityCacheHits = 0;
    private int blockEntityCacheMisses = 0;

    public CullResult checkEntity(Entity entity) {
        if (entity == null) return uncachedResult;

        int entityId = entity.getId();
        long cacheKey = entityHash(entityId);

        if (isEntityCached(cacheKey)) {
            entityCacheHits++;
            boolean culled = entityCullCache.get(entityId);
            return culled ? cachedTrueResult : cachedFalseResult;
        }

        entityCacheMisses++;
        return uncachedResult;
    }

    public void cacheEntity(Entity entity, boolean culled) {
        if (entity == null) return;

        int entityId = entity.getId();
        long cacheKey = entityHash(entityId);

        cacheEntityInternal(cacheKey, entityId, culled);
    }

    public CullResult checkBlockEntity(BlockEntity blockEntity) {
        if (blockEntity == null) return uncachedResult;

        long blockPos = blockEntity.getBlockPos().asLong();
        long cacheKey = blockEntityHash(blockPos);

        if (isBlockEntityCached(cacheKey)) {
            blockEntityCacheHits++;
            boolean culled = blockEntityCullCache.get(blockPos);
            return culled ? cachedTrueResult : cachedFalseResult;
        }

        blockEntityCacheMisses++;
        return uncachedResult;
    }

    public void cacheBlockEntity(BlockEntity blockEntity, boolean culled) {
        if (blockEntity == null) return;

        long blockPos = blockEntity.getBlockPos().asLong();
        long cacheKey = blockEntityHash(blockPos);

        cacheBlockEntityInternal(cacheKey, blockPos, culled);
    }

    public void clear() {
        entityCullCache.clear();
        blockEntityCullCache.clear();
        entityCacheTimestamps.clear();
        blockEntityCacheTimestamps.clear();

        entityCacheHits = 0;
        entityCacheMisses = 0;
        blockEntityCacheHits = 0;
        blockEntityCacheMisses = 0;
    }

    public double getEntityCacheHitRate() {
        int total = entityCacheHits + entityCacheMisses;
        return total == 0 ? 0 : (double) entityCacheHits / total;
    }

    public double getBlockEntityCacheHitRate() {
        int total = blockEntityCacheHits + blockEntityCacheMisses;
        return total == 0 ? 0 : (double) blockEntityCacheHits / total;
    }

    private boolean isEntityCached(long cacheKey) {
        if (!entityCacheTimestamps.containsKey(cacheKey)) return false;

        long currentTime = System.currentTimeMillis();
        return currentTime - entityCacheTimestamps.get(cacheKey) < ENTITY_CACHE_TIMEOUT;
    }

    private void cacheEntityInternal(long cacheKey, int entityId, boolean culled) {
        long currentTime = System.currentTimeMillis();
        entityCullCache.put(entityId, culled);
        entityCacheTimestamps.put(cacheKey, currentTime);
    }

    private boolean isBlockEntityCached(long cacheKey) {
        if (!blockEntityCacheTimestamps.containsKey(cacheKey)) return false;

        long currentTime = System.currentTimeMillis();
        return currentTime - blockEntityCacheTimestamps.get(cacheKey) < BLOCK_ENTITY_CACHE_TIMEOUT;
    }

    private void cacheBlockEntityInternal(long cacheKey, long blockPos, boolean culled) {
        long currentTime = System.currentTimeMillis();
        blockEntityCullCache.put(blockPos, culled);
        blockEntityCacheTimestamps.put(cacheKey, currentTime);
    }

    private long entityHash(int entityId) {
        return (long) entityId * 0x9e3775b9L;
    }

    private long blockEntityHash(long blockPos) {
        return blockPos * 0x9e3775b9L;
    }

    public static class CullResult {
        private final boolean cached;
        private final boolean culled;

        public CullResult(boolean cached, boolean culled) {
            this.cached = cached;
            this.culled = culled;
        }

        public boolean isCached() {
            return cached;
        }

        public boolean isCulled() {
            return culled;
        }
    }

}