package org.craftamethyst.tritium.mixin.client.lang;

import net.minecraft.client.Minecraft;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.util.LanguageLoadOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", 
            at = @At("HEAD"), 
            cancellable = true,
            require = 0)
    private void onReloadResourcePacks(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (!me.zcraft.tritiumconfig.config.TritiumConfig.get().clientOptimizations.fastLanguageSwitch) {
            return;
        }
        
        if (LanguageLoadOptimizer.isLanguageChanging()) {
            TritiumCommon.LOG.info("Skipping full resource pack reload during language change");
            cir.setReturnValue(CompletableFuture.completedFuture(null));
            LanguageLoadOptimizer.reset();
        }
    }
}
