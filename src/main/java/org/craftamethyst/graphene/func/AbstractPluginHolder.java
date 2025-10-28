package org.craftamethyst.graphene.func;

import org.craftamethyst.graphene.Graphene;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<Graphene> {
    public AbstractPluginHolder(Graphene plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(Graphene plugin, boolean register) {
        super(plugin, register);
    }
}
