package org.craftamethyst.tritium;

import me.zcraft.tconfig.config.TritiumConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.helper.EntityTickHelper;
import org.craftamethyst.tritium.manager.EntityTickManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Tritium extends JavaPlugin {
    public static final Logger LOG = LoggerFactory.getLogger("Tritium");
    private EntityTickManager entityTickManager;

    @Override
    public void onEnable() {
        LOG.info("""
                Loading...
                
                """ +
                "  ______       _  __   _                 \n" +
                " /_  __/_____ (_)/ /_ (_)__  __ ____ ___ \n" +
                "  / /  / ___// // __// // / / // __ `__ \\\n" +
                " / /  / /   / // /_ / // /_/ // / / / / /\n" +
                "/_/  /_/   /_/ \\__//_/ \\__,_//_/ /_/ /_/ \n" +
                "                                         \n");

        TritiumConfig.register("Tritium", TritiumConfigBase.class);
        entityTickManager = new EntityTickManager(this);
        entityTickManager.start();
        setupCommands();

        LOG.info("Tritium enabled with Paper API optimizations");
    }

    @Override
    public void onDisable() {
        if (entityTickManager != null) {
            entityTickManager.stop();
        }
        LOG.info("Tritium disabled");
    }

    private void setupCommands() {
        try {

            PluginCommand command = getCommand("tritium");
            if (command != null) {
                command.setExecutor(this::executeTritiumCommand);
            } else {
                LOG.warn("Command 'tritium' not found in plugin.yml. Commands will not work.");
            }
        } catch (Exception e) {
            LOG.error("Failed to setup commands", e);
        }
    }

    private boolean executeTritiumCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("tritium.reload") || sender.hasPermission("tritium.admin")) {
                    reload();
                    sender.sendMessage("§aTritium configuration reloaded!");
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                }
                break;

            case "disable":
            case "stop":
                if (sender.hasPermission("tritium.admin")) {
                    if (entityTickManager != null) {
                        entityTickManager.stop();
                        sender.sendMessage("§aEntity tick optimization disabled. All entities restored.");
                    }
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                }
                break;

            case "enable":
            case "start":
                if (sender.hasPermission("tritium.admin")) {
                    if (entityTickManager != null) {
                        entityTickManager.start();
                        sender.sendMessage("§aEntity tick optimization enabled.");
                    }
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                }
                break;

            case "status":
                if (sender.hasPermission("tritium.status") || sender.hasPermission("tritium.admin")) {
                    if (entityTickManager != null) {
                        int frozenCount = entityTickManager.getFrozenEntityCount();
                        sender.sendMessage("§6Tritium Status:");
                        sender.sendMessage("§7Frozen entities: §f" + frozenCount);
                        sender.sendMessage("§7Optimization: §f" +
                                (TritiumConfigBase.Entities.EntityOpt.optimizeEntities ? "Enabled" : "Disabled"));
                    }
                } else {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                }
                break;

            default:
                sender.sendMessage("§cUnknown command. Use /tritium for help.");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6Tritium §f- Entity Tick Optimization");
        sender.sendMessage("§7/tritium status §f- 查看状态和冻结实体数量");
        sender.sendMessage("§7/tritium reload §f- 重载配置");
        sender.sendMessage("§7/tritium disable §f- 临时禁用优化");
        sender.sendMessage("§7/tritium enable §f- 重新启用优化");
    }

    public void reload() {
        EntityTickHelper.reloadConfig();
        if (entityTickManager != null) {
            entityTickManager.reload();
        }
    }
}