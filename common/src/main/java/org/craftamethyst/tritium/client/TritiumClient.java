package org.craftamethyst.tritium.client;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.engine.cull.AABBCullingManager;
import org.craftamethyst.tritium.engine.cull.CullCache;

import java.util.List;

public class TritiumClient {
    public static TritiumClient instance;
    private final CullCache cache = new CullCache();
    private final AABBCullingManager aabbCulling = new AABBCullingManager();
    private Vec3 lastCameraPos = Vec3.ZERO;
    private int framesSinceLastUpdate = 0;


    public TritiumClient() {
        instance = this;
    }

    public static void shutdown() {
        if (instance != null) {
            instance.aabbCulling.dispose();
            instance.cache.clear();
        }
    }

    public boolean shouldSkipEntity(Entity e) {
        if (e == null) return false;
        if (!TritiumConfig.get().rendering.enableCulling) return false;
        if (isEntityBlacklisted(e)) return false;
        updateCameraPosition();
        return aabbCulling.shouldCullEntity(e);
    }

    public boolean shouldSkipBlockEntity(BlockEntity be) {
        if (be == null) return false;
        if (!TritiumConfig.get().rendering.enableBlockEntityCulling) return false;
        updateCameraPosition();
        return aabbCulling.shouldCullBlockEntity(be);
    }

    public CullCache getCullCache() {
        return cache;
    }

    public AABBCullingManager getAABBCullingManager() {
        return aabbCulling;
    }

    private boolean isEntityBlacklisted(Entity entity) {
        ResourceLocation entityId = EntityType.getKey(entity.getType());

        String entityName = entityId.toString();
        List<? extends String> blacklist = TritiumConfig.get().rendering.entityBlacklist;

        for (String pattern : blacklist) {
            if (matchesPattern(entityName, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesPattern(String entityName, String pattern) {
        if (pattern.equals("*")) return true;
        if (pattern.endsWith(":*")) {
            String namespace = pattern.substring(0, pattern.length() - 2);
            return entityName.startsWith(namespace + ":");
        }
        return entityName.equals(pattern);
    }

    private void updateCameraPosition() {
        Minecraft mc = Minecraft.getInstance();
        Vec3 currentCameraPos = mc.gameRenderer.getMainCamera().getPosition();
        framesSinceLastUpdate++;
        if (framesSinceLastUpdate >= 5 ||
                currentCameraPos.distanceToSqr(lastCameraPos) > 4.0) {
            lastCameraPos = currentCameraPos;
            aabbCulling.updateCameraPosition();
            framesSinceLastUpdate = 0;
        }
    }
}