package org.craftamethyst.tritium;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.craftamethyst.tritium.command.KillMobsCommand;

@Mod(TritiumCommon.MOD_ID)
public class TritiumNeoForge {

    public TritiumNeoForge(IEventBus modEventBus) {
        TritiumCommon.LOG.info("NeoForge Ready");
        TritiumCommon.init();

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterClientCommandsEvent event) {
        KillMobsCommand.register(event.getDispatcher());
    }

}