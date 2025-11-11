package org.craftamethyst.tritium.mixin.MCBUG.bee;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bee.class)
public class BeeMixin {

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onReadAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        Bee bee = (Bee) (Object) this;
        if (compound.contains("NoGravity")) {
            bee.setNoGravity(true);
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                 MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                 CallbackInfo ci) {
        Bee bee = (Bee) (Object) this;
        bee.setNoGravity(true);
    }

    @Inject(method = "getBreedOffspring", at = @At("RETURN"))
    private void onGetBreedOffspring(ServerLevel level, AgeableMob otherParent,
                                     CallbackInfoReturnable<Bee> cir) {
        Bee offspring = cir.getReturnValue();
        if (offspring != null) {
            offspring.setNoGravity(true);
        }
    }
}