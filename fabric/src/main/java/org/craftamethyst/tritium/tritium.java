package org.craftamethyst.tritium;

import net.fabricmc.api.ModInitializer;

public class tritium implements ModInitializer {
    
    @Override
    public void onInitialize() {

        TritiumCommon.LOG.info("Fabric Ready");
        TritiumCommon.init();
    }
}
