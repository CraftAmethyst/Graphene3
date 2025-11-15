package org.craftamethyst.tritium.client;

import net.fabricmc.api.ClientModInitializer;
import org.craftamethyst.tritium.TritiumCommon;

public class FabricClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new TritiumClient();
        TritiumCommon.LOG.info("TritiumClient initialized on Fabric");
    }
}
