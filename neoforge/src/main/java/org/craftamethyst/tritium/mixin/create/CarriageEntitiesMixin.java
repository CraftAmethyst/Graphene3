package org.craftamethyst.tritium.mixin.create;

import com.simibubi.create.content.trains.entity.Carriage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Carriage.class)
public class CarriageEntitiesMixin {

    @Redirect(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/trains/entity/Carriage;manageEntities(Lnet/minecraft/world/level/Level;)V"
            )
    )
    private void redirectManageEntities(Carriage carriage, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                carriage.manageEntities(level);
            });
        } else {
            carriage.manageEntities(level);
        }
    }
}