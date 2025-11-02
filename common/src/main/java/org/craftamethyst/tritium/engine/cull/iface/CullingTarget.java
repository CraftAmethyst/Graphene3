package org.craftamethyst.tritium.engine.cull.iface;

public interface CullingTarget {
    boolean graphene$isForcedVisible();

    void graphene$setCulled(boolean value);

    boolean graphene$isCulled();

    void graphene$setOutOfCamera(boolean value);

    boolean graphene$isOutOfCamera();
}