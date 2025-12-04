package org.craftamethyst.tritium.mixin.entity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(EntityCollisionContext.class)
public interface EntityCollisionContextAccessor {
    @Accessor("heldItem")
    void setHeldItem(ItemStack stack);

    @Accessor("canStandOnFluid")
    void setCanStandOnFluid(Predicate<FluidState> predicate);
}