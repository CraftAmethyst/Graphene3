package org.craftamethyst.tritium.mixin.stack;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.UnaryOperator;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/11/30
 */
@Mixin(DataComponents.class)
public class DataComponentsMixin {

    @ModifyArgs(method = "register",at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static void resetMaxStackCount(Args args){
        if(args.get(1) instanceof String name && name.equals("max_stack_size")){
            UnaryOperator<DataComponentType.Builder<Integer>> NEW_MAX_STACK_SIZE = (p_333287_) -> p_333287_.persistent(ExtraCodecs.intRange(1,Integer.MAX_VALUE)).networkSynchronized(ByteBufCodecs.VAR_INT);
            args.set(2,NEW_MAX_STACK_SIZE.apply(DataComponentType.builder()).build());
        }
    }
}
