package me.zcraft.tc.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.craftamethyst.tritium.TritiumCommon;
import me.zcraft.tc.config.TritiumConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class TritiumConfigScreenReg implements ModMenuApi {

    public static void registerConfigScreen() {
        try {
            String modId = FabricLoader.getInstance().getModContainer(TritiumCommon.MOD_ID)
                    .orElseThrow(() -> new IllegalStateException("Mod container not found"))
                    .getMetadata().getId();
            registerConfigScreenInternal(modId, TritiumConfig.getConfig(modId));
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to auto-register config screen for current mod", e);
        }
    }

    public static void registerConfigScreen(String modId) {
        try {
            registerConfigScreenInternal(modId, TritiumConfig.getConfig(modId));
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register config screen for mod: {}", modId, e);
        }
    }

    public static void registerConfigScreen(TritiumConfig config) {
        try {
            registerConfigScreenInternal(config.getModId(), config);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register config screen for config: {}", config.getConfigClass().getName(), e);
        }
    }

    private static void registerConfigScreenInternal(String modId, TritiumConfig config) {
        if (config == null) {
            TritiumCommon.LOG.warn("No configuration found for mod: {}. Call TritiumConfig.register() first.", modId);
            return;
        }

        if (!config.isClientEnvironment()) {
            TritiumCommon.LOG.debug("Skipping config screen registration for mod {} in server environment", modId);
            return;
        }

        // For Fabric, the config screen is automatically available through ModMenu
        TritiumCommon.LOG.info("Config screen registered for mod: {} (available through ModMenu)", modId);
    }

    public static Screen createConfigScreen(Screen parent, TritiumConfig config) {
        return TritiumConfigScreenFactory.createConfigScreen(parent, config);
    }

    public static Screen createConfigScreen(Screen parent, String modId) {
        return TritiumConfigScreenFactory.createConfigScreen(parent, TritiumConfig.getConfig(modId));
    }

    private static Screen createConfigScreen(Screen parent, String modId, TritiumConfig config) {
        return TritiumConfigScreenFactory.createConfigScreen(parent, config);
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent, TritiumCommon.MOD_ID);
    }
}