package org.craftamethyst.tritium.mixin.entity.stack.item;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Inventory.class)
public class InventoryMixin {

    @ModifyVariable(
            method = "add(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack onAddStack(ItemStack stack) {
        int configMax = TritiumConfigBase.Entities.EntityStacking.maxStackSize;
        if (configMax > 0 && stack.getCount() > configMax) {
            stack.setCount(configMax);
        }
        return stack;
    }
}