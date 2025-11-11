package org.craftamethyst.tritium.mixin.MCBUG.bee;

import net.minecraft.world.entity.ai.util.RandomPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(RandomPos.class)
public class RandomPosMixin {

    @ModifyArgs(
            method = "generateRandomDirectionWithinRadians",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"
            )
    )
    private static void centerRandomPosition(Args args) {
        args.set(0, (double) args.get(0) + 0.5D);
        args.set(2, (double) args.get(2) + 0.5D);
    }
}