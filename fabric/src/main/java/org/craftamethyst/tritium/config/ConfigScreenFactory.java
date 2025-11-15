package org.craftamethyst.tritium.config;

import net.minecraft.client.gui.screens.Screen;

public class ConfigScreenFactory {

    public static Screen createConfigScreen(Screen parent) {
        return TritiumAutoConfig.createConfigScreen(parent);
    }
}
