package org.craftamethyst.tritium.mixin.MCBUG.bee;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.BeeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeeRenderer.class)
public class BeeRendererMixin {
    @ModifyReturnValue(
            method = "getFlipDegrees(Lnet/minecraft/world/entity/LivingEntity;)F",
            at = @At("RETURN")
    )
    private float makeBeeUpsideDown(float original) {
        //MC-2343564
        return 180.0F;
    }
}