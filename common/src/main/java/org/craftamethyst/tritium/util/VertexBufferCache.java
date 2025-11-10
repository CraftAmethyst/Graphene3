package org.craftamethyst.tritium.util;

import org.joml.Vector3f;

public final class VertexBufferCache {
    private static final ThreadLocal<Vector3f> TL_VEC = ThreadLocal.withInitial(Vector3f::new);
    private VertexBufferCache() {}

    public static Vector3f get() {
        return TL_VEC.get();
    }
}