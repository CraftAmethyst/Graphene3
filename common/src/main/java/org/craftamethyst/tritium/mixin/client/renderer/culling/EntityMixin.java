package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.world.entity.Entity;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityVisibility {

    @Unique
    private boolean tritium$culled = false;

    @Unique
    private boolean tritium$outOfCamera = false;

    @Override
    public boolean tritium$isCulled() {
        return tritium$culled;
    }

    @Override
    public void tritium$setCulled(boolean value) {
        tritium$culled = value;
    }

    @Override
    public boolean tritium$isForcedVisible() {
        return false;
    }

    @Override
    public void tritium$setOutOfCamera(boolean value) {
        tritium$outOfCamera = value;
    }

    @Override
    public boolean tritium$isOutOfCamera() {
        return tritium$outOfCamera;
    }
}
