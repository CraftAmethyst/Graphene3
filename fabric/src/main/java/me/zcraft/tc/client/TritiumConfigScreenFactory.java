package me.zcraft.tc.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.zcraft.tc.config.TritiumAutoConfig;
import me.zcraft.tc.config.TritiumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class TritiumConfigScreenFactory implements ModMenuApi {
    private final TritiumConfig config;

    public TritiumConfigScreenFactory(TritiumConfig config) {
        this.config = config;
    }

    public static Screen createConfigScreen(Screen parent, TritiumConfig config) {
        return new TritiumAutoConfig(config).createConfigScreen(parent);
    }

    public static Screen createConfigScreen(TritiumConfig config) {
        return new TritiumAutoConfig(config).createConfigScreen(null);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent, config);
    }
}