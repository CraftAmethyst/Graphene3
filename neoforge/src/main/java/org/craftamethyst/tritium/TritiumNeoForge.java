package org.craftamethyst.tritium;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.craftamethyst.tritium.command.KillMobsCommand;

@Mod(TritiumCommon.MOD_ID)
public class TritiumNeoForge {

    public TritiumNeoForge(IEventBus eventBus) {
        TritiumCommon.LOG.info("NeoForge Ready");
        TritiumCommon.init();
    }
    @SubscribeEvent
    public void onRegisterCommands(RegisterClientCommandsEvent event) {
        KillMobsCommand.register(event.getDispatcher());
    }
}
