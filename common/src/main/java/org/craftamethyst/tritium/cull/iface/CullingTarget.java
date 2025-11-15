package org.craftamethyst.tritium.cull.iface;

public interface CullingTarget {
    boolean tritium$isForcedVisible();

    void tritium$setCulled(boolean value);

    boolean tritium$isCulled();

    void tritium$setOutOfCamera(boolean value);

    boolean tritium$isOutOfCamera();
}