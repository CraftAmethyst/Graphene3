package org.craftamethyst.tritium.mixin.client.memleakfix;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import de.mari_023.ae2wtlib.AE2WTLibCreativeTab;

import java.util.List;

@Mixin(value = AE2WTLibCreativeTab.class, remap = false)
public class AE2WTLibCreativeTabLeakFix {

    @Shadow
    @Final
    private static List<ItemStack> items;

    @Inject(method = "init", at = @At("HEAD"))
    private static void tritium$clearOnInit(CallbackInfoReturnable<CreativeModeTab> ci) {

        if (TritiumConfig.get().fixes.MemoryLeakFix_AE2WTLibCreativeTabLeakFix) {
            synchronized (items) {
                items.clear();
            }
        }
    }
}