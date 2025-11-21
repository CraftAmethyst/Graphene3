package org.craftamethyst.tritium;

import org.craftamethyst.tritium.util.Vec3d;

public class OcclusionCullingInstance {

    public OcclusionCullingInstance(int renderDistanceChunks, DataProvider provider) {
        // No-op
    }

    public boolean isAABBVisible(Vec3d min, Vec3d max, Vec3d camera) {
        return true;
    }

    public void resetCache() {
        // No-op
    }
}
