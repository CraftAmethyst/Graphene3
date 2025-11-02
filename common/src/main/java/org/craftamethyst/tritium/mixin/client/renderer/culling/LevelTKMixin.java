package org.craftamethyst.tritium.mixin.client.renderer.culling;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.craftamethyst.tritium.client.TritiumClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class LevelTKMixin {

    @Inject(
            method = "guardEntityTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ConEntityTick(Consumer<Entity> consumer, Entity entity, CallbackInfo ci) {
        if (!TritiumConfig.get().rendering.enableTickStopping) return;
        if (TritiumClient.instance != null && TritiumClient.instance.shouldSkipEntity(entity)) {
            ci.cancel();
        }
    }
}