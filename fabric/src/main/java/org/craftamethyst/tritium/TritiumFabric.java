package org.craftamethyst.tritium;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.craftamethyst.tritium.command.KillMobsCommand;

public class TritiumFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        TritiumCommon.LOG.info("Fabric Ready");
        TritiumCommon.init();
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> KillMobsCommand.register(commandDispatcher));
    }
}
