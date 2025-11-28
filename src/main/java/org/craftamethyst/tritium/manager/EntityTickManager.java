package org.craftamethyst.tritium.manager;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.craftamethyst.tritium.Tritium;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.helper.EntityTickHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EntityTickManager implements Listener {
    private final Tritium plugin;
    private final Map<UUID, Boolean> skipTickState = new HashMap<>();
    private final Map<UUID, Long> lastProcessedTick = new HashMap<>();
    private final Set<UUID> frozenEntities = new HashSet<>();
    private int taskId = -1;
    private boolean isShuttingDown = false;

    public EntityTickManager(Tritium plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void start() {
        if (taskId != -1) {
            stop();
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::processEntityTicksOptimized, 1L, 1L);
        Tritium.LOG.info("Entity tick optimization started");
    }

    public void stop() {
        isShuttingDown = true;

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        restoreAllFrozenEntities();
        skipTickState.clear();
        lastProcessedTick.clear();
        frozenEntities.clear();

        isShuttingDown = false;
        Tritium.LOG.info("Entity tick optimization stopped, all entities restored");
    }

    private void restoreAllFrozenEntities() {
        Tritium.LOG.info("Restoring all frozen entities...");
        int restoredCount = 0;

        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity instanceof Player) continue;

                try {
                    applyUnfreezeOptimizations(entity);
                    restoredCount++;
                } catch (Exception e) {
                    Tritium.LOG.warn("{}{}", "Failed to restore entity " + entity.getUniqueId() + ": ", e.getMessage());
                }
            }
        }

        Tritium.LOG.info("{} entities", "Successfully restored " + restoredCount);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!TritiumConfigBase.Entities.EntityOpt.optimizeEntities || isShuttingDown) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Entity entity : event.getChunk().getEntities()) {
                if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
                    if (EntityTickHelper.shouldSkipTick(living)) {
                        applyFreezeOptimizations(living);
                    } else {
                        applyUnfreezeOptimizations(living);
                    }
                }
            }
        }, 2L);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!TritiumConfigBase.Entities.EntityOpt.optimizeEntities || isShuttingDown) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = event.getWorld();
            for (LivingEntity entity : world.getLivingEntities()) {
                if (entity instanceof Player) continue;

                if (EntityTickHelper.shouldSkipTick(entity)) {
                    applyFreezeOptimizations(entity);
                } else {
                    applyUnfreezeOptimizations(entity);
                }
            }
        }, 5L);
    }

    private void processEntityTicksOptimized() {
        if (!TritiumConfigBase.Entities.EntityOpt.optimizeEntities || isShuttingDown) {
            return;
        }

        long currentTick = Bukkit.getCurrentTick();
        int checkInterval = Math.max(1, TritiumConfigBase.Entities.EntityOpt.checkInterval);

        for (World world : Bukkit.getWorlds()) {
            if (world.getPlayers().isEmpty()) {
                continue;
            }

            for (Chunk chunk : world.getLoadedChunks()) {
                if (!chunk.isLoaded()) {
                    continue;
                }

                for (Entity entity : chunk.getEntities()) {
                    if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
                        continue;
                    }

                    UUID entityId = entity.getUniqueId();

                    Long lastTick = lastProcessedTick.get(entityId);
                    if (lastTick != null && lastTick == currentTick) {
                        continue;
                    }

                    Boolean shouldSkip = skipTickState.get(entityId);
                    if (currentTick % checkInterval == 0 || shouldSkip == null) {
                        shouldSkip = EntityTickHelper.shouldSkipTick(living);
                        skipTickState.put(entityId, shouldSkip);
                    }

                    if (shouldSkip) {
                        applyFreezeOptimizations(living);
                        frozenEntities.add(entityId);
                    } else {
                        applyUnfreezeOptimizations(living);
                        frozenEntities.remove(entityId);
                    }

                    lastProcessedTick.put(entityId, currentTick);
                }
            }
        }

        if (currentTick % 100 == 0) {
            cleanupCache(currentTick);
        }
    }

    private void applyFreezeOptimizations(LivingEntity entity) {
        if (entity instanceof Player || isShuttingDown) return;

        try {
            if (frozenEntities.contains(entity.getUniqueId())) {
                return;
            }
            if (entity instanceof Mob mob) {
                mob.setAware(false);
            }

            if (entity.hasAI()) {
                entity.setAI(false);
            }
            if (entity.hasGravity()) {
                entity.setGravity(false);
            }
            entity.setInvulnerable(true);
            entity.setVelocity(entity.getVelocity().zero());
            entity.setFallDistance(0);

        } catch (Exception e) {
            Tritium.LOG.debug("Failed to freeze entity: {}", e.getMessage());
        }
    }

    private void applyUnfreezeOptimizations(LivingEntity entity) {
        if (entity instanceof Player) return;

        try {
            if (entity instanceof Mob mob) {
                mob.setAware(true);
            }

            if (!entity.hasAI()) {
                entity.setAI(true);
            }
            if (!entity.hasGravity()) {
                entity.setGravity(true);
            }
            entity.setInvulnerable(false);

        } catch (Exception e) {
            Tritium.LOG.debug("Failed to unfreeze entity: {}", e.getMessage());
        }
    }

    private void cleanupCache(long currentTick) {
        skipTickState.entrySet().removeIf(entry -> {
            UUID entityId = entry.getKey();
            Entity entity = Bukkit.getEntity(entityId);
            if (entity == null || !entity.isValid()) {
                frozenEntities.remove(entityId);
                return true;
            }
            return false;
        });

        lastProcessedTick.entrySet().removeIf(entry ->
                currentTick - entry.getValue() > 200
        );
    }

    public int getFrozenEntityCount() {
        return frozenEntities.size();
    }


    public void reload() {
        restoreAllFrozenEntities();
        frozenEntities.clear();
        skipTickState.clear();
        lastProcessedTick.clear();

        start();
    }
}