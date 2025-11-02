package org.craftamethyst.tritium;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.craftamethyst.tritium.client.TritiumClient;

@Mod(TritiumCommon.MOD_ID)
public class Tritium {

    public Tritium(IEventBus eventBus) {
        TritiumCommon.LOG.info("NeoForge Ready");
        TritiumCommon.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TritiumClient.registerConfigScreen();
        }
    }
}
