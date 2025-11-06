package org.craftamethyst.tritium.mixin.client.renderer.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.accessor.EntityRendererAccessor;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private final Minecraft graphene$mc = Minecraft.getInstance();
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderEntity",
            at = @At("TAIL"),
            cancellable = true)
    private void graphene$skipCulledEntityButMaybeRenderNameTag(
            Entity entity, double cameraX, double cameraY, double cameraZ,
            float tickDelta, PoseStack matrices, MultiBufferSource consumers,
            CallbackInfo ci) {
        if (!TritiumConfig.get().rendering.entityCulling.enableCulling) return;
        TritiumClient client = TritiumClient.instance;
        if (client == null || !(entity instanceof EntityVisibility cullable)) return;
        if (cullable.graphene$isForcedVisible() || entity.noCulling) {
            cullable.graphene$setOutOfCamera(false);
            return;
        }
        if (client.shouldSkipEntity(entity)) {
            if (!TritiumConfig.get().rendering.entityCulling.enableNameTagCulling
                    && matrices != null
                    && consumers != null
                    && graphene$shouldRenderNameTag(entity)) {
                graphene$renderNameTag(entity, cameraX, cameraY, cameraZ, tickDelta, matrices, consumers);
            }
            ci.cancel();
            return;
        }
        cullable.graphene$setOutOfCamera(false);
    }

    @Unique
    private boolean graphene$shouldRenderNameTag(Entity entity) {
        EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) entityRenderDispatcher.getRenderer(entity);
        return renderer instanceof EntityRendererAccessor accessor
                && accessor.graphene_shouldShowName(entity);
    }

    @Unique
    private void graphene$renderNameTag(Entity entity, double camX, double camY, double camZ,
                                        float tickDelta, PoseStack matrices, MultiBufferSource consumers) {
        EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) entityRenderDispatcher.getRenderer(entity);
        if (!(renderer instanceof EntityRendererAccessor accessor)) return;

        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX()) - camX;
        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY()) - camY;
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ()) - camZ;

        Vec3 offset = renderer.getRenderOffset(entity, tickDelta);
        matrices.pushPose();
        matrices.translate(x + offset.x, y + offset.y, z + offset.z);
        accessor.graphene_renderNameTag(entity, entity.getDisplayName(), matrices, consumers,
                entityRenderDispatcher.getPackedLightCoords(entity, tickDelta));
        matrices.popPose();
    }
}