package org.craftamethyst.tritium.mixin.tickstop;

import net.minecraft.world.entity.EntityType;
import org.craftamethyst.tritium.api.IOptimizableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements IOptimizableEntity {
    @Unique
    private boolean tritium$alwaysTick;
    @Unique
    private boolean tritium$tickInRaid;

    @Override
    public boolean tritium$shouldAlwaysTick() {
        return this.tritium$alwaysTick;
    }

    @Override
    public void tritium$setAlwaysTick(boolean value) {
        this.tritium$alwaysTick = value;
    }

    @Override
    public boolean tritium$shouldTickInRaid() {
        return this.tritium$tickInRaid;
    }

    @Override
    public void tritium$setTickInRaid(boolean value) {
        this.tritium$tickInRaid = value;
    }
}