package org.craftamethyst.tritium;

import de.mari_023.ae2wtlib.AE2WTLibCreativeTab;
import net.fabricmc.api.ModInitializer;

import java.lang.reflect.Method;

public class tritium implements ModInitializer {
    
    @Override
    public void onInitialize() {

        TritiumCommon.LOG.info("Fabric Ready");
        TritiumCommon.init();

        try {
            Method m = AE2WTLibCreativeTab.class.getMethod("init");
            System.out.println(m.getReturnType().getSimpleName());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
