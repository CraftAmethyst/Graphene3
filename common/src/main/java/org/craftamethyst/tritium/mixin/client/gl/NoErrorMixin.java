package org.craftamethyst.tritium.mixin.client.gl;

import com.mojang.blaze3d.platform.Window;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = Window.class)
public class NoErrorMixin {

    @Inject(
            method = "defaultErrorCallback",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tritium$noerror(int error, long description, CallbackInfo ci) {
        if (TritiumConfig.get().fixes.noGLog.noGLog) {
            ci.cancel();
        }
    }
}