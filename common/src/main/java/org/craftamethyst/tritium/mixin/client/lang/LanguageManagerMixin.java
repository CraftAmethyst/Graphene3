package org.craftamethyst.tritium.mixin.client.lang;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.util.LanguageLoadOptimizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageManager.class)
public abstract class LanguageManagerMixin {

    @Shadow
    private String currentCode;

    @Shadow
    protected abstract void onResourceManagerReload(ResourceManager resourceManager);

    @Unique
    private String tritium$previousLanguage;

    @Inject(method = "setSelected", at = @At("HEAD"))
    private void onSetLanguageHead(String languageCode, CallbackInfo ci) {
        if (!TritiumConfig.get().clientOptimizations.fastLanguage.fastLanguageSwitch) {
            return;
        }
        
        tritium$previousLanguage = this.currentCode;
        
        if (tritium$previousLanguage != null && !tritium$previousLanguage.equals(languageCode)) {
            LanguageLoadOptimizer.setLanguageChanging(true);
        }
    }

    @Inject(method = "setSelected", at = @At("TAIL"))
    private void onSetLanguageTail(String languageCode, CallbackInfo ci) {
        if (!TritiumConfig.get().clientOptimizations.fastLanguage.fastLanguageSwitch) {
            return;
        }
        
        if (tritium$previousLanguage != null && !tritium$previousLanguage.equals(languageCode)) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.getResourceManager() != null) {
                try {
                    this.onResourceManagerReload(minecraft.getResourceManager());
                } catch (Exception e) {
                    TritiumCommon.LOG.error("Failed to reload language resources", e);
                    LanguageLoadOptimizer.reset();
                }
            }
        }
    }
}
