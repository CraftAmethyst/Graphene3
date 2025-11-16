package org.craftamethyst.tritium.mixin.create;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Train.class)
public class TrainEntitiesMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private void redirectCarriageEntityManagement(List<Carriage> carriages, Consumer<? super Carriage> action, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                for (Carriage carriage : carriages) {
                    carriage.manageEntities(level);
                }
            });
        } else {
            carriages.forEach(action);
        }
    }
}