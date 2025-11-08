package org.craftamethyst.tritium.mixin.client.renderer.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.cull.AABBCullingManager;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void tritium$cullEntity(E entity, double d0, double d1, double d2,
                                                       float yaw, float partialTicks,
                                                       PoseStack poseStack, MultiBufferSource buffers,
                                                       int packedLight, CallbackInfo ci) {
        if (!(entity instanceof EntityVisibility vis) || vis.graphene$isForcedVisible()) return;
        Level level = entity.level;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        if (!AABBCullingManager.isEntityVisible(vis, camera, level)) {
            ci.cancel();
        }
    }
}
