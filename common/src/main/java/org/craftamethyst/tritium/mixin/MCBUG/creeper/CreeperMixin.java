package org.craftamethyst.tritium.mixin.MCBUG.creeper;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(SwellGoal.class)
public class CreeperMixin {

    @Shadow
    private @Nullable LivingEntity target;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/sensing/Sensing;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean improveTargetVisibilityCheck(Sensing sensor, LivingEntity potentialTarget) {
        //MC-179072
        boolean hasVisualContact = sensor.hasLineOfSight(potentialTarget);
        if (!hasVisualContact || this.target == null) {
            return false;
        }
        return this.target.canBeSeenAsEnemy();
    }
}