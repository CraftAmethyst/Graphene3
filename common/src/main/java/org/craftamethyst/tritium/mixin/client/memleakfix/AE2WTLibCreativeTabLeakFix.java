package org.craftamethyst.tritium.mixin.client.memleakfix;

import de.mari_023.ae2wtlib.AE2wtlibCreativeTab;
import net.minecraft.world.item.ItemStack;
import org.craftamethyst.tritium.config.TritiumConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = AE2wtlibCreativeTab.class, remap = false)
public class AE2WTLibCreativeTabLeakFix {

    @Shadow
    @Final
    private static List<ItemStack> items;

    @Inject(method = "init", at = @At("HEAD"),require = 0)
    private static void tritium$clearOnInit(CallbackInfo ci) {
        if (TritiumConfig.Fixes.MemoryLeakFix_AE2WTLibCreativeTabLeakFix) {
            synchronized (items) {
                items.clear();
            }
        }
    }
}