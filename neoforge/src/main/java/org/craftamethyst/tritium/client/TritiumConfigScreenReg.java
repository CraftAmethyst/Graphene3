package org.craftamethyst.tritium.client;

import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigScreenFactory;

public class TritiumConfigScreenReg {
    public static void registerConfigScreen() {
        try {
            ModList.get().getModContainerById(TritiumCommon.MOD_ID).ifPresent(container -> {
                if (isClothConfigAvailable()) {
                    ModLoadingContext.get().registerExtensionPoint(
                            IConfigScreenFactory.class,
                            () -> (minecraft, screen) -> TritiumConfigScreenFactory.createConfigScreen(screen)
                    );
                    TritiumCommon.LOG.info("Cloth Config integration registered");
                } else {
                    TritiumCommon.LOG.warn("Cloth Config not available, config screen disabled");
                }
            });
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register config screen", e);
        }
    }

    private static boolean isClothConfigAvailable() {
        try {
            Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
