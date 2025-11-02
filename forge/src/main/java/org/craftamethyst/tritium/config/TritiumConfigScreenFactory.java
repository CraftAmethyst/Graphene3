package org.craftamethyst.tritium.config;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TritiumConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent) {
        return TritiumAutoConfig.createConfigScreen(parent);
    }
    public static Screen createConfigScreen() {
        return TritiumAutoConfig.createConfigScreen(null);
    }
}