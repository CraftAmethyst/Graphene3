package org.craftamethyst.tritium.mixin.client.renderer.culling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Shadow
    protected abstract boolean shouldShowName(T entity);

    @Shadow
    protected abstract void renderNameTag(T entity, Component displayName, PoseStack poseStack,
                                          MultiBufferSource bufferSource, int packedLight, float partialTick);

    @Unique
    public boolean graphene_shouldShowName(T entity) {
        return shouldShowName(entity);
    }

    @Unique
    public void graphene_renderNameTag(T entity, Component component, PoseStack poseStack,
                                       MultiBufferSource multiBufferSource, int light, float partialTick) {
        renderNameTag(entity, component, poseStack, multiBufferSource, light, partialTick);
    }
}