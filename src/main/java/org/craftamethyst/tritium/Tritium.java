package org.craftamethyst.tritium;

import me.zcraft.tconfig.config.TritiumConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Tritium extends JavaPlugin {
    public static final Logger LOG = LoggerFactory.getLogger("Tritium");

    @Override
    public void onEnable() {
        LOG.info("Loading...\n" +
                        "\n" +
                        "  ______       _  __   _                 \n" +
                        " /_  __/_____ (_)/ /_ (_)__  __ ____ ___ \n" +
                        "  / /  / ___// // __// // / / // __ `__ \\\n" +
                        " / /  / /   / // /_ / // /_/ // / / / / /\n" +
                        "/_/  /_/   /_/ \\__//_/ \\__,_//_/ /_/ /_/ \n" +
                        "                                         \n");
        //Register configuration file
        TritiumConfig.register("Tritium", TritiumConfigBase.class);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
