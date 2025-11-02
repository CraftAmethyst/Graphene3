package org.craftamethyst.tritium.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * Lambda-based event listener for optimal performance.
 */
@FunctionalInterface
public interface IEventListener {
    void invoke(Event event);
}
