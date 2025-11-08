package org.craftamethyst.tritium.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return org.craftamethyst.tritium.config.ConfigScreenFactory::createConfigScreen;
    }
}
