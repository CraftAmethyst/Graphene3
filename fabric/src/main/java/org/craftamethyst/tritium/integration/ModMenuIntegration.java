package org.craftamethyst.tritium.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.zcraft.tc.client.TritiumConfigScreenFactory;
import me.zcraft.tc.config.TritiumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import org.craftamethyst.tritium.TritiumCommon;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    private final TritiumConfig config = TritiumConfig.getConfig(TritiumCommon.MOD_ID);

    public static Screen createConfigScreen(TritiumConfig config) {
        return TritiumConfigScreenFactory.createConfigScreen(config);
    }
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(config);
    }
}