package org.craftamethyst.tritium;

import me.zcraft.tc.client.TritiumConfigScreenReg;
import me.zcraft.tc.config.TritiumConfig;
import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TritiumCommon {
    public static final String MOD_ID = "tritium";
    public static final String MOD_NAME = "Tritium";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    public static void init() {
        LOG.info("Loading...\n" +
                "\n" +
                "  ______       _  __   _                 \n" +
                " /_  __/_____ (_)/ /_ (_)__  __ ____ ___ \n" +
                "  / /  / ___// // __// // / / // __ `__ \\\n" +
                " / /  / /   / // /_ / // /_/ // / / / / /\n" +
                "/_/  /_/   /_/ \\__//_/ \\__,_//_/ /_/ /_/ \n" +
                "                                         \n" +
                "Version: {} | Platform: {} | Environment: {}\n" +
                "\n",
                Services.PLATFORM.getModVersion(), Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
       //Register configuration file
        try {
            TritiumConfig.register(MOD_ID, TritiumConfigBase.class);
            TritiumConfigScreenReg.registerConfigScreen();
            Runtime.getRuntime().addShutdownHook(new Thread(TritiumClient::shutdown));
            TritiumCommon.LOG.info("Config initialized");
        } catch (Throwable t) {
            TritiumCommon.LOG.warn("Failed to initialize config service: {}", t.toString());
        }

        if (Services.PLATFORM.isModLoaded("tritium")) {
            TritiumCommon.LOG.info("Loading completed!");
        }
    }
}
