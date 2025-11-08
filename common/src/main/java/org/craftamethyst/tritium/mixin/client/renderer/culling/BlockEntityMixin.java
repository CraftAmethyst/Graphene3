package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.cull.AABBCullingManager;
import org.craftamethyst.tritium.cull.iface.BlockEntityVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements BlockEntityVisibility {
    @Unique
    private boolean tritium$forcedVisible;

    @Override
    public boolean graphene$isForcedVisible() {
        return tritium$forcedVisible;
    }

    @Override
    public AABB graphene$getCullingBox() {
        BlockEntity self = (BlockEntity) (Object) this;
        BlockPos p = self.getBlockPos();
        return new AABB(p);
    }

    @Unique
    private boolean tritium$isVisibleToCamera(Vec3 camera) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level lvl = self.getLevel();
        return AABBCullingManager.isBlockEntityVisible(this, camera, lvl);
    }
}
