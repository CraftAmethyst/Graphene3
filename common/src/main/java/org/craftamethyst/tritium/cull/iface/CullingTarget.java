package org.craftamethyst.tritium.cull.iface;

import net.minecraft.world.phys.AABB;

public interface CullingTarget {
    boolean graphene$isForcedVisible();

    AABB graphene$getCullingBox();
}
