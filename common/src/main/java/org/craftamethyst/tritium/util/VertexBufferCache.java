package org.craftamethyst.tritium.util;

import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

/**
 * Thread-local cache for vector objects to reduce allocations during rendering.
 * Uses Mojang's math library compatible with Minecraft 1.19.2.
 */
public final class VertexBufferCache {
    private static final ThreadLocal<Vector3f> TL_VEC3 = ThreadLocal.withInitial(() -> new Vector3f(0, 0, 0));
    private static final ThreadLocal<Vector4f> TL_VEC4 = ThreadLocal.withInitial(() -> new Vector4f(0, 0, 0, 1));

    private VertexBufferCache() {
    }

    public static Vector3f getVec3() {
        return TL_VEC3.get();
    }

    public static Vector4f getVec4() {
        return TL_VEC4.get();
    }
}
