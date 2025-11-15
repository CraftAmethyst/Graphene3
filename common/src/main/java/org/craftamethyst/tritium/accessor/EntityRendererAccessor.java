package org.craftamethyst.tritium.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public interface EntityRendererAccessor<T extends Entity> {
    boolean tritium_shouldShowName(T entity);

    void tritium_renderNameTag(T entity, Component component, PoseStack poseStack,
                               MultiBufferSource multiBufferSource, int light);
}