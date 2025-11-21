package org.craftamethyst.tritium.mixin.entity.stack.item.fix;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BucketItem.class)
public interface BucketItemAccessor {
    @Accessor("content")
    Fluid getContent();
}