package org.craftamethyst.tritium.mixin.create;

import net.minecraft.server.MinecraftServer;
import org.craftamethyst.tritium.util.RailOffloaderHub;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
abstract class ServerLifecycleHook {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(CallbackInfo ci) {
        RailOffloaderHub.initialize();
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void tickHead(BooleanSupplier sup, CallbackInfo ci) {
        RailOffloaderHub.onTickStart((MinecraftServer) (Object) this);
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void tickTail(BooleanSupplier sup, CallbackInfo ci) {
        RailOffloaderHub.onTickEnd();
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        RailOffloaderHub.shutdown();
    }
}