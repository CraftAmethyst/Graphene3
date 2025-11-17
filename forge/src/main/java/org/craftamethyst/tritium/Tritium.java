package org.craftamethyst.tritium;

import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Tritium {

    public Tritium() {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        TritiumCommon.init();

    }
}
