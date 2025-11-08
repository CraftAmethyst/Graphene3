package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.cull.AABBCullingManager;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityVisibility {
    @Unique
    private boolean tritium$forcedVisible;

    @Override
    public boolean graphene$isForcedVisible() {
        return tritium$forcedVisible;
    }

    @Override
    public AABB graphene$getCullingBox() {
        Entity self = (Entity) (Object) this;
        return self.getBoundingBox();
    }

    @Unique
    private boolean tritium$isVisibleToCamera(Vec3 camera) {
        Entity self = (Entity) (Object) this;
        Level lvl = self.level;
        return AABBCullingManager.isEntityVisible(this, camera, lvl);
    }
}
