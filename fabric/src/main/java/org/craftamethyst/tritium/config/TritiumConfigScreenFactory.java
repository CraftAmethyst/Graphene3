package org.craftamethyst.tritium.config;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;


@Environment(EnvType.CLIENT)
public class TritiumConfigScreenFactory implements ModMenuApi {
    public static Screen createConfigScreen(Screen parent) {
        return TritiumAutoConfig.createConfigScreen(parent);
    }
    public static Screen createConfigScreen() {
        return TritiumAutoConfig.createConfigScreen(null);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TritiumConfigScreenFactory::createConfigScreen;
    }
}