package org.craftamethyst.tritium.api;

public interface IOptimizableEntity {
    boolean graphene$shouldAlwaysTick();

    void graphene$setAlwaysTick(boolean value);

    boolean graphene$shouldTickInRaid();

    void graphene$setTickInRaid(boolean value);
}