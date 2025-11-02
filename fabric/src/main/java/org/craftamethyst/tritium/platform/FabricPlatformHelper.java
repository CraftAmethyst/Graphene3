package org.craftamethyst.tritium.platform;

import org.craftamethyst.tritium.Constants;
import org.craftamethyst.tritium.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(Constants.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("Unknown");
    }
}
