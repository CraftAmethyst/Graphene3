package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.world.entity.Entity;
import org.craftamethyst.tritium.engine.cull.iface.EntityVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityVisibility {

    @Unique
    private boolean graphene$culled = false;

    @Unique
    private boolean graphene$outOfCamera = false;

    @Override
    public boolean graphene$isCulled() {
        return graphene$culled;
    }

    @Override
    public void graphene$setCulled(boolean value) {
        graphene$culled = value;
    }

    @Override
    public boolean graphene$isForcedVisible() {
        return false;
    }

    @Override
    public void graphene$setOutOfCamera(boolean value) {
        graphene$outOfCamera = value;
    }

    @Override
    public boolean graphene$isOutOfCamera() {
        return graphene$outOfCamera;
    }
}