package org.craftamethyst.tritium.mixin.client.renderer.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.cull.AABBCullingManager;
import org.craftamethyst.tritium.cull.iface.BlockEntityVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <T extends BlockEntity> void tritium$cullBlockEntity(T be, float partialTick,
                                                                 PoseStack poseStack,
                                                                 MultiBufferSource buffers,
                                                                 CallbackInfo ci) {
        if (!(be instanceof BlockEntityVisibility vis) || vis.graphene$isForcedVisible()) return;
        Level level = be.getLevel();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        if (!AABBCullingManager.isBlockEntityVisible(vis, camera, level)) {
            ci.cancel();
        }
    }
}
