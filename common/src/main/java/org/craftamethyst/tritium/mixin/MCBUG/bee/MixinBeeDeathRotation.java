package org.craftamethyst.tritium.mixin.MCBUG.bee;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinBeeDeathRotation {

    @Inject(
            method = "die",
            at = @At("HEAD")
    )
    private void fixBeeDeathRotation(DamageSource pCause, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Bee) {
            //MC-234364
            self.setXRot(180.0F);
        }
    }
}