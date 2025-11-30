package org.craftamethyst.tritium.mixin.stack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.world.item.ItemStack.ITEM_NON_AIR_CODEC;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/11/30
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Mutable
    @Shadow @Final public static Codec<ItemStack> CODEC;

    @Inject(method = "<clinit>",at = @At("TAIL"))
    private static void resetMaxStackCount(CallbackInfo ci){
        CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create((p_341560_) -> p_341560_.group(ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder), ExtraCodecs.intRange(1, Integer.MAX_VALUE).fieldOf("count").orElse(1).forGetter(ItemStack::getCount), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)).apply(p_341560_, ItemStack::new)));
    }
}
