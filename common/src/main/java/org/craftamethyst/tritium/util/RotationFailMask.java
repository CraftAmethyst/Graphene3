package org.craftamethyst.tritium.util;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

public final class RotationFailMask {
    private static final ThreadLocal<Long2ByteOpenHashMap> MAP =
            ThreadLocal.withInitial(Long2ByteOpenHashMap::new);

    public static boolean isFullyFailed(int templateId, int x, int y, int z) {
        long key = pack(templateId, x, y, z);
        byte mask = MAP.get().get(key);
        return (mask & 0x0F) == 0x0F;
    }

    public static boolean markFailed(int templateId, int x, int y, int z, int rotIndex) {
        long key = pack(templateId, x, y, z);
        Long2ByteOpenHashMap m = MAP.get();
        byte old = m.get(key);
        byte neo = (byte) (old | (1 << rotIndex));
        m.put(key, neo);
        return (neo & 0x0F) == 0x0F;
    }

    public static void clear() {
        MAP.get().clear();
    }

    private static long pack(int templateId, int x, int y, int z) {
        return (((long) templateId & 0xFFFFFFFFL) << 32)
                | (((long) x & 0xFFFFL) << 16)
                | (((long) y & 0xFFL) << 8)
                | ((long) z & 0xFFL);
    }
}