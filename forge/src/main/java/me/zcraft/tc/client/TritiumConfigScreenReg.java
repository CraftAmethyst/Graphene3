package me.zcraft.tc.client;

import org.craftamethyst.tritium.TritiumCommon;
import me.zcraft.tc.config.TritiumConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class TritiumConfigScreenReg {

    public static void registerConfigScreen() {
        try {
            ModContainer currentMod = ModLoadingContext.get().getActiveContainer();
            String modId = currentMod.getModId();
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

        registerSingleConfigScreen(modId, config);
    }

    private static void registerSingleConfigScreen(String modId, TritiumConfig config) {
        ModList.get().getModContainerById(modId).ifPresent(container -> {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) ->
                            TritiumConfigScreenFactory.createConfigScreen(parent, config))
            );
            TritiumCommon.LOG.info("Config screen registered for mod: {}", modId);
        });
    }
}