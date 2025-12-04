package org.craftamethyst.tritium.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityCollisionContext.class)
public class EntityCollisionContextMixin {

    @Shadow
    @Final
    private Entity entity;

    @Unique
    private boolean tritium$heldItemInitialized = false;
    @Unique
    private boolean tritium$fluidPredicateInitialized = false;

    @Inject(
            method = "isHoldingItem",
            at = @At("HEAD")
    )
    private void initHeldItemOnDemand(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (!tritium$heldItemInitialized) {
            tritium$initializeHeldItem();
        }
    }

    @Inject(
            method = "canStandOnFluid",
            at = @At("HEAD")
    )
    private void initFluidPredicateOnDemand(FluidState p_205115_, FluidState p_205116_, CallbackInfoReturnable<Boolean> cir) {
        if (!tritium$fluidPredicateInitialized) {
            tritium$initializeFluidPredicate();
        }
    }

    @Unique
    private void tritium$initializeHeldItem() {
        if (this.entity instanceof LivingEntity livingEntity) {
            ((EntityCollisionContextAccessor) this).setHeldItem(livingEntity.getMainHandItem());
        }
        this.tritium$heldItemInitialized = true;
    }

    @Unique
    private void tritium$initializeFluidPredicate() {
        if (this.entity instanceof LivingEntity livingEntity) {
            ((EntityCollisionContextAccessor) this).setCanStandOnFluid(livingEntity::canStandOnFluid);
        }
        this.tritium$fluidPredicateInitialized = true;
    }

    @Inject(
            method = "isHoldingItem",
            at = @At("RETURN")
    )
    private void afterIsHoldingItem(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (!tritium$heldItemInitialized) {
            tritium$heldItemInitialized = true;
        }
    }

    @Inject(
            method = "canStandOnFluid",
            at = @At("RETURN")
    )
    private void afterCanStandOnFluid(FluidState p_205115_, FluidState p_205116_, CallbackInfoReturnable<Boolean> cir) {
        if (!tritium$fluidPredicateInitialized) {
            tritium$fluidPredicateInitialized = true;
        }
    }
}