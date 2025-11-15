package org.craftamethyst.tritium.mixin.entity;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
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
        if(TritiumConfig.get().entities.entityStacking.enable) return Math.max(value,TritiumConfig.get().entities.entityStacking.range);
        return value;
    }
}
