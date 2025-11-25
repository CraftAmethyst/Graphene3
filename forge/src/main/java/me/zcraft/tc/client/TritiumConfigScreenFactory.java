package me.zcraft.tc.client;

import me.zcraft.tc.config.TritiumAutoConfig;
import me.zcraft.tc.config.TritiumConfig;
import net.minecraft.client.gui.screens.Screen;

public class TritiumConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent, TritiumConfig config) {
        return new TritiumAutoConfig(config).createConfigScreen(parent);
    }

    public static Screen createConfigScreen(TritiumConfig config) {
        return new TritiumAutoConfig(config).createConfigScreen(null);
    }
}