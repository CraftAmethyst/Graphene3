package org.craftamethyst.tritium;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.craftamethyst.tritium.config.TritiumConfigScreenFactory;

@Mod(TritiumCommon.MOD_ID)
public class tritium {

    public tritium(IEventBus eventBus) {
        TritiumCommon.LOG.info("NeoForge Ready");
        TritiumCommon.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerConfigScreen();
        }
    }
    private static void registerConfigScreen() {
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
