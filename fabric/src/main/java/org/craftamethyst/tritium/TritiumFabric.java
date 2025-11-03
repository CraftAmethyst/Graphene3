package org.craftamethyst.tritium;

import net.fabricmc.api.ModInitializer;

public class TritiumFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        TritiumCommon.LOG.info("Fabric Ready");
        TritiumCommon.init();
    }
}
