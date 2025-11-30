package org.craftamethyst.tritium.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import me.zcraft.tconfig.config.TritiumConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public final class EntityTickHelper {
    private static final AtomicReference<Set<EntityType<?>>> WHITE_LIST = new AtomicReference<>(Collections.emptySet());
    private static final AtomicReference<Set<EntityType<?>>> BLACK_LIST = new AtomicReference<>(Collections.emptySet());
    private static final List<WildcardPattern> WHITE_PATTERNS = new ArrayList<>();
    private static final List<WildcardPattern> BLACK_PATTERNS = new ArrayList<>();
    private static final boolean ignoreDeadEntities = true;
    private static volatile boolean enabled = true;
    private static volatile int horizontalRange = 32;
    private static volatile int verticalRange = 16;

    static {
        try {
            TritiumConfig config = TritiumConfig.getConfig("tritium");
            config.addReloadListener(EntityTickHelper::reloadConfig);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to register config reload listener", e);
        }

        reloadConfig();
    }

    public static boolean shouldSkipTick(Entity entity) {
        if (!enabled) return false;

        if (ignoreDeadEntities && !entity.isAlive()) {
            return false;
        }
        if (!entity.isAlive()) return true;

        EntityType<?> type = entity.getType();
        if (matchesWildcard(type, BLACK_PATTERNS) || BLACK_LIST.get().contains(type)) {
            return true;
        }
        if (matchesWildcard(type, WHITE_PATTERNS) || WHITE_LIST.get().contains(type)) {
            return false;
        }
        return !isNearPlayer(entity);
    }

    private static void reloadConfig() {
        enabled = TritiumConfigBase.Entities.EntityOpt.optimizeEntities;
        horizontalRange = TritiumConfigBase.Entities.EntityOpt.horizontalRange;
        verticalRange = TritiumConfigBase.Entities.EntityOpt.verticalRange;
        List<String> whiteRaw = TritiumConfigBase.Entities.EntityOpt.entityWhitelist;

        Set<EntityType<?>> whiteIds = Sets.newHashSet();
        WHITE_PATTERNS.clear();
        BLACK_PATTERNS.clear();

        for (String s : whiteRaw) {
            parseEntry(s, whiteIds);
        }
        WHITE_LIST.set(ImmutableSet.copyOf(whiteIds));
    }

    private static void parseEntry(String raw, Set<EntityType<?>> idTarget) {
        if (raw.contains("*") || raw.contains("?")) {
            WHITE_PATTERNS.add(new WildcardPattern(raw));
        } else {
            try {
                ResourceLocation key = new ResourceLocation(raw);
                EntityType<?> type = IRegistry.field_212629_r.get(key);
                if (type != null) {
                    idTarget.add(type);
                }
            } catch (Exception e) {

            }
        }
    }

    private static boolean matchesWildcard(EntityType<?> type, List<WildcardPattern> list) {
        ResourceLocation id = IRegistry.field_212629_r.getKey(type);
        if (id == null) return false;
        String str = id.toString();
        for (WildcardPattern p : list) {
            if (p.matches(str)) return true;
        }
        return false;
    }


    private static boolean isNearPlayer(Entity entity) {
        World level = entity.world;
        if (!(level instanceof WorldServer)) return true;
        WorldServer sl = (WorldServer) level;
        BlockPos pos = entity.getPosition();

        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        int radius = (horizontalRange >> 4) + 1;

        AxisAlignedBB box = new AxisAlignedBB(
                pos.getX() - horizontalRange,
                pos.getY() - verticalRange,
                pos.getZ() - horizontalRange,
                pos.getX() + horizontalRange,
                pos.getY() + verticalRange,
                pos.getZ() + horizontalRange
        );

        for (EntityPlayer player : sl.playerEntities) {
            if (!player.isAlive()) continue;
            BlockPos ppos = player.getPosition();
            int pcx = ppos.getX() >> 4;
            int pcz = ppos.getZ() >> 4;
            if (Math.abs(pcx - cx) > radius || Math.abs(pcz - cz) > radius) continue;
            if (player.getBoundingBox().intersects(box)) return true;
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