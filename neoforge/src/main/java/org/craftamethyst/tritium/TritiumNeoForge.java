package org.craftamethyst.tritium;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.craftamethyst.tritium.client.TritiumConfigScreenReg;
import org.craftamethyst.tritium.command.KillMobsCommand;

@Mod(TritiumCommon.MOD_ID)
public class TritiumNeoForge {

    public TritiumNeoForge(IEventBus eventBus) {
        TritiumCommon.LOG.info("NeoForge Ready");
        TritiumCommon.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TritiumConfigScreenReg.registerConfigScreen();
        }
    }
    @SubscribeEvent
    public void onRegisterCommands(RegisterClientCommandsEvent event) {
        KillMobsCommand.register(event.getDispatcher());
    }
}
