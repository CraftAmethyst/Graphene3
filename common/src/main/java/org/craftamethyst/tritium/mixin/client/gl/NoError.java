package org.craftamethyst.tritium.mixin.client.gl;

import com.mojang.blaze3d.platform.Window;
import org.craftamethyst.tritium.platform.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = Window.class)
public class NoError {

    @Inject(
            method = "defaultErrorCallback",
            at = @At("HEAD"),
            cancellable = true
    )
    private void graphene$noerror(int error, long description, CallbackInfo ci) {
        if (Services.CONFIG.get().fixes.noglog) {
            ci.cancel();
        }
    }
}