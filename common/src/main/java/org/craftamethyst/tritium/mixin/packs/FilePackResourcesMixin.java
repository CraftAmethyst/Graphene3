package org.craftamethyst.tritium.mixin.packs;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(FilePackResources.class)
public abstract class FilePackResourcesMixin {

    @Unique
    private ConcurrentHashMap<PackType, Set<String>> tritium$namespaceCache = null;

    @Inject(method = "getNamespaces", at = @At("RETURN"), cancellable = true)
    private void cacheNamespaces(PackType type, CallbackInfoReturnable<Set<String>> cir) {
        if (!TritiumConfig.get().clientOptimizations.fastResourcePack.resourcePackCache) return;

        if (tritium$namespaceCache == null) {
            tritium$namespaceCache = new ConcurrentHashMap<>();
        }

        Set<String> result = cir.getReturnValue();
        if (result != null && !result.isEmpty()) {
            tritium$namespaceCache.put(type, result);
        }
    }

    @Inject(method = "getNamespaces", at = @At("HEAD"), cancellable = true)
    private void useCachedNamespaces(PackType type, CallbackInfoReturnable<Set<String>> cir) {
        if (!TritiumConfig.get().clientOptimizations.fastResourcePack.resourcePackCache) return;

        if (tritium$namespaceCache != null) {
            Set<String> cached = tritium$namespaceCache.get(type);
            if (cached != null) {
                cir.setReturnValue(cached);
            }
        }
    }
}