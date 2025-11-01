package org.craftamethyst.tritium.platform;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.craftamethyst.tritium.config.TritiumConfig;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.platform.services.IConfigService;

public class NeoForgeConfigService implements IConfigService {
    private boolean initialized = false;

    @Override
    public void init() {
        if (!initialized) {
            AutoConfig.register(TritiumConfig.class, GsonConfigSerializer::new);
            initialized = true;
        }
    }

    @Override
    public TritiumConfigBase get() {
        if (!initialized) init();
        return AutoConfig.getConfigHolder(TritiumConfig.class).getConfig();
    }

    @Override
    public void save() {
        if (initialized) {
            AutoConfig.getConfigHolder(TritiumConfig.class).save();
        }
    }
}