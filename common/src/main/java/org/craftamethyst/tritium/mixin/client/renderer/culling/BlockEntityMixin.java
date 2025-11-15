package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.craftamethyst.tritium.cull.iface.BlockEntityVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements BlockEntityVisibility {

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