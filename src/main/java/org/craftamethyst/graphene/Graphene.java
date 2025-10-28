package org.craftamethyst.graphene;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import top.mrxiaom.pluginbase.BukkitPlugin;import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;

import org.jetbrains.annotations.NotNull;

public class Graphene extends BukkitPlugin {
    public static Graphene getInstance() {
        return (Graphene) BukkitPlugin.getInstance();
    }

    public Graphene() {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("org.craftamethyst.graphene.libs")
        );
        // this.scheduler = new FoliaLibScheduler(this);
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }


    @Override
    protected void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    @Override
    protected void afterEnable() {
        getLogger().info("Graphene 加载完毕");
    }
}
