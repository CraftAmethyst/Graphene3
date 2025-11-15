package org.craftamethyst.tritium;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.craftamethyst.tritium.client.ForgeClientInitializer;

@Mod(TritiumCommon.MOD_ID)
public class tritium {
    
    public tritium() {
        TritiumCommon.LOG.info("Forge Ready");
        TritiumCommon.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClientInitializer.initializeClient();
            ForgeClientInitializer.registerConfigScreen();
        }
    }
}
