package org.craftamethyst.tritium.mixin.tickstop;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.craftamethyst.tritium.helper.EntityTickHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class LevelMixin {

    @Inject(
            method = "tickEntity(Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onEntityTick(Entity ent, CallbackInfo ci) {
        if (EntityTickHelper.shouldSkipTick(ent)) {
            ci.cancel();
        }
    }
}