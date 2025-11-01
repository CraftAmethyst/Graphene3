package org.craftamethyst.tritium.platform.services;

import org.craftamethyst.tritium.config.TritiumConfigBase;

public interface IConfigService {
    void init();
    TritiumConfigBase get();
    void save();
}