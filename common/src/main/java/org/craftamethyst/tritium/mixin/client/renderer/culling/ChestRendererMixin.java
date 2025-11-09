package org.craftamethyst.tritium.mixin.client.renderer.culling;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/11/9 上午8:30
 */
@Mixin(ChestRenderer.class)
public class ChestRendererMixin {
    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;FII)V",
            at = @At(value = "HEAD"),
            index = 6, argsOnly = true)
    public float renderLid(float value){
        if(!TritiumConfig.get().clientOptimizations.chest_rendering_optimization) return value;
        return 0F;
    }
}
