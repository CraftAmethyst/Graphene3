package org.craftamethyst.tritium.platform;

import org.craftamethyst.tritium.Constants;
import org.craftamethyst.tritium.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public String getModVersion() {
        return ModList.get()
                .getModContainerById(Constants.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("Unknown");
    }
}
