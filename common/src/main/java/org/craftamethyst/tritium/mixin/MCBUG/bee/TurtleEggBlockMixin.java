package org.craftamethyst.tritium.mixin.MCBUG.bee;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TurtleEggBlock;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBlockMixin {

    @Inject(method = "canDestroyEgg", at = @At("HEAD"), cancellable = true)
    private void tritum$insertBeeCheck(Level level, Entity entity,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (TritiumConfigBase.Fixes.BeeFixes.enableBeeFixes &&
                TritiumConfigBase.Fixes.BeeFixes.fixBeeTurtleEgg) {
            if (entity instanceof Bee) {
                cir.setReturnValue(false);
            }
        }
    }
}