package org.craftamethyst.tritium.mixin.entity.stack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/11/15 下午2:23
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity {
    public ExperienceOrbMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyArg(method = "scanForEntities",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(D)Lnet/minecraft/world/phys/AABB;"))
    public double range(double value){
        if(TritiumConfigBase.Entities.EntityStacking.enable) return Math.max(value,TritiumConfigBase.Entities.EntityStacking.mergeDistance);
        return value;
    }
}
