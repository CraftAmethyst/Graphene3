package org.craftamethyst.tritium;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.fabricmc.api.ModInitializer;

public class tritium implements ModInitializer {
    
    @Override
    public void onInitialize() {

        TritiumCommon.LOG.info("Fabric Ready");
        TritiumConfigScreenReg.registerConfigScreen(TritiumCommon.MOD_ID);
        TritiumCommon.init();
    }
}
