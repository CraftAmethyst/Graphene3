package org.craftamethyst.tritium;

import me.zcraft.tconfig.client.TritiumConfigScreenReg;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import org.craftamethyst.tritium.command.KillMobsCommand;
@Mod(TritiumCommon.MOD_ID)
public class tritium {

    public tritium() {
        TritiumCommon.init();
        TritiumConfigScreenReg.registerConfigScreen(TritiumCommon.MOD_ID);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }
    private void onRegisterCommands(RegisterClientCommandsEvent event) {
        KillMobsCommand.register(event.getDispatcher());
    }
}
