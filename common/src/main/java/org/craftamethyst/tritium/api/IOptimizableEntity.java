package org.craftamethyst.tritium.api;

public interface IOptimizableEntity {
    boolean tritium$shouldAlwaysTick();

    void tritium$setAlwaysTick(boolean value);

    boolean tritium$shouldTickInRaid();

    void tritium$setTickInRaid(boolean value);
}