package org.craftamethyst.tritium;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.craftamethyst.tritium.command.KillMobsCommand;

@Mod(TritiumCommon.MOD_ID)
public class TritiumNeoForge {

    public TritiumNeoForge(IEventBus modEventBus) {
        TritiumCommon.init();
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        modEventBus.addListener(this::onClientSideSetup);
    }
public void onClientSideSetup(FMLClientSetupEvent event){
    TritiumConfigScreenReg.registerConfigScreen(TritiumCommon.MOD_ID);
}
    private void onRegisterCommands(RegisterCommandsEvent event) {
        KillMobsCommand.register(event.getDispatcher());
    }
}