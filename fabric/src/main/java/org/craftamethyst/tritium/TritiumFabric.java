package org.craftamethyst.tritium;

import me.zcraft.tc.fabric.client.TritiumConfigScreenReg;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.craftamethyst.tritium.command.KillMobsCommand;

public class TritiumFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        TritiumCommon.LOG.info("Fabric Ready");
        TritiumCommon.init();
        TritiumConfigScreenReg.registerConfigScreen();
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> KillMobsCommand.register(commandDispatcher));
    }
}
