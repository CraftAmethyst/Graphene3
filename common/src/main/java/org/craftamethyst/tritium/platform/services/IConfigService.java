package org.craftamethyst.tritium.platform.services;

import org.craftamethyst.tritium.config.TritiumConfigBase;

public interface IConfigService {
    /** Initialize config system (register, load from disk). Safe to call multiple times. */
    void init();

    /** Get the current mutable config instance. */
    TritiumConfigBase get();

    /** Persist current config to disk if supported. */
    void save();
}