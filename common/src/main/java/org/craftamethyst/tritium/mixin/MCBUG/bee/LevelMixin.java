package org.craftamethyst.tritium.mixin.MCBUG.bee;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "prepareWeather", at = @At("HEAD"), cancellable = true)
    private void onPrepareWeather(CallbackInfo ci) {
        Level level = (Level) (Object) this;
        if (!level.dimensionType().hasSkyLight()) {
            ci.cancel();
        }
    }
}