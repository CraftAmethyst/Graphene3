package org.craftamethyst.tritium.helper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.craftamethyst.tritium.Tritium;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class EntityTickHelper {
    private static final Set<EntityType> WHITE_LIST = ConcurrentHashMap.newKeySet();
    private static final Set<EntityType> BLACK_LIST = ConcurrentHashMap.newKeySet();
    private static final List<WildcardPattern> WHITE_PATTERNS = new ArrayList<>();
    private static final List<WildcardPattern> BLACK_PATTERNS = new ArrayList<>();

    private static final Map<UUID, Boolean> proximityCache = new ConcurrentHashMap<>();
    private static long lastCacheCleanup = 0;

    static {
        reloadConfig();
    }
    public static boolean shouldSkipTick(LivingEntity entity) {
        if (!TritiumConfigBase.Entities.EntityOpt.optimizeEntities) return false;
        if (entity instanceof Player) return false;

        if (!entity.isValid()) return true;

        EntityType type = entity.getType();

        if (matchesWildcard(type, BLACK_PATTERNS) || BLACK_LIST.contains(type)) {
            return true;
        }
        if (matchesWildcard(type, WHITE_PATTERNS) || WHITE_LIST.contains(type)) {
            return false;
        }

        if (TritiumConfigBase.Entities.EntityOpt.tickRaidersInRaid && isRaiderInRaid(entity)) {
            return false;
        }

        return !isNearPlayerCached(entity);
    }
    private static boolean isNearPlayerCached(LivingEntity entity) {
        UUID entityId = entity.getUniqueId();
        World world = entity.getWorld();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheCleanup > 30000) {
            proximityCache.entrySet().removeIf(entry -> {
                Entity cachedEntity = Bukkit.getEntity(entry.getKey());
                return cachedEntity == null || !cachedEntity.isValid();
            });
            lastCacheCleanup = currentTime;
        }

        Boolean cached = proximityCache.get(entityId);
        if (cached != null) {
            return cached;
        }

        boolean result = isNearPlayer(entity);
        proximityCache.put(entityId, result);
        return result;
    }
    public static void reloadConfig() {
        WHITE_LIST.clear();
        BLACK_LIST.clear();
        WHITE_PATTERNS.clear();
        BLACK_PATTERNS.clear();
        proximityCache.clear();

        List<String> whiteRaw = TritiumConfigBase.Entities.EntityOpt.entityWhitelist;
        whiteRaw.forEach(s -> parseEntry(s, WHITE_LIST));

        Tritium.LOG.info("Entity tick optimization reloaded. Whitelist: " + WHITE_LIST.size() + " entities");
    }
    private static void parseEntry(String raw, Set<EntityType> target) {
        if (raw.contains("*") || raw.contains("?")) {
            WHITE_PATTERNS.add(new WildcardPattern(raw));
        } else {
            try {
                EntityType type = EntityType.valueOf(raw.toUpperCase().replace("minecraft:", ""));
                target.add(type);
            } catch (IllegalArgumentException e) {
                try {
                    String typeName = raw.replace("minecraft:", "").toUpperCase();
                    EntityType type = EntityType.valueOf(typeName);
                    target.add(type);
                } catch (IllegalArgumentException ex) {
                    Tritium.LOG.warn("Unknown entity type in config: " + raw);
                }
            }
        }
    }

    private static boolean matchesWildcard(EntityType type, List<WildcardPattern> list) {
        String typeName = type.getKey().toString();
        for (WildcardPattern p : list) {
            if (p.matches(typeName)) return true;
        }
        return false;
    }

    private static boolean isRaiderInRaid(LivingEntity entity) {
        return entity instanceof Raider raider && raider.isValid() && raider.getRaid() != null;
    }

    private static boolean isNearPlayer(LivingEntity entity) {
        World world = entity.getWorld();
        Location loc = entity.getLocation();

        int horizontalRange = TritiumConfigBase.Entities.EntityOpt.horizontalRange;
        int verticalRange = TritiumConfigBase.Entities.EntityOpt.verticalRange;

        double horizontalRangeSquared = horizontalRange * horizontalRange;
        double verticalRangeSquared = verticalRange * verticalRange;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isValid() || !player.getWorld().equals(world)) continue;

            Location playerLoc = player.getLocation();

            double dx = playerLoc.getX() - loc.getX();
            double dz = playerLoc.getZ() - loc.getZ();
            if (dx * dx + dz * dz > horizontalRangeSquared) continue;

            double dy = playerLoc.getY() - loc.getY();
            if (dy * dy > verticalRangeSquared) continue;

            return true;
        }

        return false;
    }


    private static final class WildcardPattern {
        private final Pattern regex;

        WildcardPattern(String raw) {
            String s = raw.replace("?", ".{1}").replace("*", ".*");
            this.regex = Pattern.compile("^" + s + "$");
        }

        boolean matches(String str) {
            return regex.matcher(str).matches();
        }
    }
}