package org.craftamethyst.tritium.mixin.create;

import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.foundation.events.CommonEvents;
import net.minecraft.world.level.Level;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommonEvents.class)
public class CommonEventsMixin {

    @SuppressWarnings("StatementWithEmptyBody")
    @Redirect(
            method = "onServerWorldTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/trains/GlobalRailwayManager;tick(Lnet/minecraft/world/level/Level;)V"
            ),remap = false
    )
    private static void disableOriginalRailwayTicking(GlobalRailwayManager instance, Level level) {
        if (TritiumConfigBase.TechOptimizations.CreateOptimizations.enableRailOffloading) {

        } else {
            instance.tick(level);
        }
    }
}