package org.craftamethyst.tritium.mixin.tickstop;

import net.minecraft.world.entity.EntityType;
import org.craftamethyst.tritium.api.IOptimizableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements IOptimizableEntity {
    @Unique
    private boolean graphene$alwaysTick;
    @Unique
    private boolean graphene$tickInRaid;

    @Override
    public boolean graphene$shouldAlwaysTick() {
        return this.graphene$alwaysTick;
    }

    @Override
    public void graphene$setAlwaysTick(boolean value) {
        this.graphene$alwaysTick = value;
    }

    @Override
    public boolean graphene$shouldTickInRaid() {
        return this.graphene$tickInRaid;
    }

    @Override
    public void graphene$setTickInRaid(boolean value) {
        this.graphene$tickInRaid = value;
    }
}