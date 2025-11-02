package org.craftamethyst.tritium.config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;


@Environment(EnvType.CLIENT)
public class TritiumConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent) {
        return TritiumAutoConfig.createConfigScreen(parent);
    }
    public static Screen createConfigScreen() {
        return TritiumAutoConfig.createConfigScreen(null);
    }
}