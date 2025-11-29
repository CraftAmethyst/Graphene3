package org.craftamethyst.tritium.mixin.MCBUG.bee;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Bee;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bee.class)
public class BeeMixin {

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onReadAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes &&
                TritiumConfigBase.Fixes.BeeFixes.fixBeeGravity) {
            Bee bee = (Bee) (Object) this;
            if (compound.contains("NoGravity")) {
                bee.setNoGravity(true);
            }
        }
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void onGetBreedOffspring(ServerLevel level, AgeableMob otherParent,
                                     CallbackInfoReturnable<Bee> cir) {
        if (TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes &&
                TritiumConfigBase.Fixes.BeeFixes.fixBeeGravity) {
            Bee offspring = cir.getReturnValue();
            if (offspring != null) {
                offspring.setNoGravity(true);
            }
        }
    }
}