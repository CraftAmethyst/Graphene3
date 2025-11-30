package org.craftamethyst.tritium;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import org.dimdev.rift.listener.CommandAdder;
import org.dimdev.rift.listener.MinecraftStartListener;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public class TritiumRift implements MinecraftStartListener, InitializationListener, CommandAdder {
    @Override
    public void onMinecraftStart() {
        TritiumCommon.init();
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher) {

    }

    @Override
    public void onInitialization() {
        MixinBootstrap.init();
        Mixins.addConfiguration("tritium.mixins.json");
    }
}
