package org.craftamethyst.tritium;

import me.zcraft.tconfig.config.TritiumConfig;
//import org.craftamethyst.tritium.client.TritiumClient;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TritiumCommon {
    public static final String MOD_ID = "tritium";
    public static final String MOD_NAME = "Tritium";
    //public static final TritiumConfig CONFIG = new TritiumConfig("tritium", TritiumConfigBase.class);
    public static final Log LOG = LogFactory.getLog("Tritium");

    public static void init() {
        LOG.info("\n" +
                "                        Loading...\n" +
                "\n" +
                "  ______       _  __   _                 \n" +
                " /_  __/_____ (_)/ /_ (_)__  __ ____ ___ \n" +
                "  / /  / ___// // __// // / / // __ `__ \\\n" +
                " / /  / /   / // /_ / // /_/ // / / / / /\n" +
                "/_/  /_/   /_/ \\__//_/ \\__,_//_/ /_/ /_/ \n" +
                "                                         \n");
        //Register configuration file
        try {
            TritiumConfig.register(MOD_ID, TritiumConfigBase.class);
            // Runtime.getRuntime().addShutdownHook(new Thread(TritiumClient::shutdown));
            TritiumCommon.LOG.info("Config initialized");
        } catch (Throwable t) {
            TritiumCommon.LOG.warn("Failed to initialize config service: {}");
        }
    }

}