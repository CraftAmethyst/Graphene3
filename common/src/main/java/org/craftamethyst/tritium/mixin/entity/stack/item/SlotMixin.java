package org.craftamethyst.tritium.mixin.entity.stack.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Inject(
            method = "tryRemove",
            at = @At("HEAD")
    )
    private void onTryRemove(int amount, int decrement, Player player, CallbackInfoReturnable<ItemStack> cir) {
        if (!TritiumConfigBase.Entities.EntityStacking.enable) {
            return;
        }
        if (player.getAbilities().instabuild) {
            int configMax = TritiumConfigBase.Entities.EntityStacking.maxStackSize;
            if (configMax > 0) {
                Slot slot = (Slot) (Object) this;
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && stack.getCount() > configMax) {
                    stack.setCount(configMax);
                }
            }
        }
    }
}