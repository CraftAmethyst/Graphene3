package org.craftamethyst.tritium.mixin.tickstop;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.craftamethyst.tritium.helper.EntityTickHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(
            method = "guardEntityTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onEntityTick(Consumer<Entity> consumer, Entity entity, CallbackInfo ci) {
        if (EntityTickHelper.shouldSkipTick(entity)) {
            ci.cancel();
        }
    }
}