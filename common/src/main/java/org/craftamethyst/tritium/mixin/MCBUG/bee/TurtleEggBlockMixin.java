package org.craftamethyst.tritium.mixin.MCBUG.bee;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.TurtleEggBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBlockMixin {

    @ModifyExpressionValue(
            method = "canDestroyEgg(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/entity/ambient/Bat")
    )
    private static boolean preventEntitiesFromDestroyingEgg(boolean isBat, Entity entity) {
        //MC-248332
        if (entity instanceof Bee) {
            return false;
        }
        return isBat;
    }
}