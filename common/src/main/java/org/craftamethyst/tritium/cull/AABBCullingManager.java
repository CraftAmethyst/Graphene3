package org.craftamethyst.tritium.cull;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.cull.iface.BlockEntityVisibility;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;

public class AABBCullingManager {
    private static final double HITBOX_LIMIT = 10.0D;
    private static final double MANHATTAN_THRESHOLD = 1.732;
    private static final long CAMERA_UPDATE_INTERVAL = 50;
    private static final long DISTANCE_UPDATE_INTERVAL = 1000;
    private static final double RESET_DISTANCE_THRESHOLD = 2.0;
    private static final long MIN_RESET_INTERVAL = 1000;
    private final OcclusionCullingInstance occlusionCulling;
    private final CullCache cullCache = new CullCache();
    private final Minecraft mc;
    private final AABBOBJ reusableAABB = new AABBOBJ(0, 0, 0, 0, 0, 0);
    private final Vec3d reusableAabbMin = new Vec3d(0, 0, 0);
    private final Vec3d reusableAabbMax = new Vec3d(0, 0, 0);
    private final Vec3d reusableCamera = new Vec3d(0, 0, 0);
    private Vec3 cachedCameraPos = Vec3.ZERO;
    private double cachedCullingDistance = 128.0;
    private long lastCameraUpdate = 0;
    private long lastDistanceUpdate = 0;
    private Vec3 lastResetCameraPos = Vec3.ZERO;
    private long lastResetTime = 0;

    public AABBCullingManager() {
        this.mc = Minecraft.getInstance();
        this.occlusionCulling = new OcclusionCullingInstance(64, new OcclusionProvider());
    }

    public boolean shouldCullEntity(Entity entity) {
        if (isPlayerSprinting()) return false;
        if (entity == null) return false;
        if (mc.level == null) return false;

        CullCache.CullResult cached = cullCache.checkEntity(entity);
        if (cached.isCached()) {
            return cached.isCulled();
        }

        if (entity instanceof EntityVisibility cullable) {
            if (cullable.tritium$isForcedVisible()) {
                cullCache.cacheEntity(entity, false);
                return false;
            }
        }

        if (!needsDetailedEntityCheck(entity)) {
            cullCache.cacheEntity(entity, false);
            return false;
        }

        Vec3 cameraPos = getCachedCameraPos();
        double cullingDistance = getCachedCullingDistance();

        if (isWithinDistanceOptimized(entity.getEyePosition(), cameraPos, cullingDistance)) {
            cullCache.cacheEntity(entity, true);
            return true;
        }

        AABB boundingBox = entity.getBoundingBox();

        if (isLargeEntity(boundingBox)) {
            cullCache.cacheEntity(entity, false);
            return false;
        }

        boolean visible = performOcclusionCheck(boundingBox, cameraPos);
        boolean shouldCull = !visible;
        cullCache.cacheEntity(entity, shouldCull);
        return shouldCull;
    }

    public boolean shouldCullBlockEntity(BlockEntity blockEntity) {
        if (isPlayerSprinting()) return false;
        if (blockEntity == null) return false;

        if (mc.level == null) return false;

        CullCache.CullResult cached = cullCache.checkBlockEntity(blockEntity);
        if (cached.isCached()) {
            return cached.isCulled();
        }

        if (blockEntity instanceof BlockEntityVisibility cullable) {
            if (cullable.tritium$isForcedVisible()) {
                cullCache.cacheBlockEntity(blockEntity, false);
                return false;
            }
        }

        Vec3 cameraPos = getCachedCameraPos();
        double cullingDistance = getCachedCullingDistance();

        if (isWithinDistanceOptimized(blockEntity.getBlockPos().getCenter(), cameraPos, cullingDistance)) {
            cullCache.cacheBlockEntity(blockEntity, true);
            return true;
        }
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        reusableAABB.set(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

        boolean visible = performOcclusionCheck(reusableAABB, cameraPos);
        boolean shouldCull = !visible;
        cullCache.cacheBlockEntity(blockEntity, shouldCull);
        return shouldCull;
    }

    private boolean needsDetailedEntityCheck(Entity entity) {
        if (entity.isInvisible()) return false;
        if (entity == mc.getCameraEntity()) return false;
        if (entity == mc.player) return false;
        if (entity.isSpectator()) return false;
        if (entity.isVehicle()) return false;
        if (entity.isPassenger()) return false;

        return !(entity instanceof ArmorStand armorStand) || !armorStand.isMarker();
    }

    private boolean isWithinDistanceOptimized(Vec3 entityPos, Vec3 cameraPos, double distance) {
        double manhattan = Math.abs(entityPos.x - cameraPos.x) +
                Math.abs(entityPos.y - cameraPos.y) +
                Math.abs(entityPos.z - cameraPos.z);
        if (manhattan > distance * MANHATTAN_THRESHOLD) return true;
        double dx = entityPos.x - cameraPos.x;
        double dy = entityPos.y - cameraPos.y;
        double dz = entityPos.z - cameraPos.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return !(distanceSq <= (distance * distance));
    }

    private boolean performOcclusionCheck(AABB boundingBox, Vec3 cameraPos) {
        reusableAabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        reusableAabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        reusableCamera.set(cameraPos.x, cameraPos.y, cameraPos.z);

        return occlusionCulling.isAABBVisible(reusableAabbMin, reusableAabbMax, reusableCamera);
    }

    private boolean performOcclusionCheck(AABBOBJ boundingBox, Vec3 cameraPos) {
        reusableAabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        reusableAabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        reusableCamera.set(cameraPos.x, cameraPos.y, cameraPos.z);

        return occlusionCulling.isAABBVisible(reusableAabbMin, reusableAabbMax, reusableCamera);
    }

    private boolean isLargeEntity(AABB boundingBox) {
        return boundingBox.getXsize() > HITBOX_LIMIT ||
                boundingBox.getYsize() > HITBOX_LIMIT ||
                boundingBox.getZsize() > HITBOX_LIMIT;
    }

    private Vec3 getCachedCameraPos() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCameraUpdate > CAMERA_UPDATE_INTERVAL) {
            Camera mainCamera = mc.gameRenderer.getMainCamera();
            cachedCameraPos = mainCamera.getPosition();
            lastCameraUpdate = currentTime;
        }
        return cachedCameraPos;
    }

    private double getCachedCullingDistance() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDistanceUpdate > DISTANCE_UPDATE_INTERVAL) {
            cachedCullingDistance = mc.level == null ? 128.0D :
                    (mc.level.getServerSimulationDistance() * 16) * 1.1;
            lastDistanceUpdate = currentTime;
        }
        return cachedCullingDistance;
    }

    public void updateCameraPosition() {
        Vec3 currentCameraPos = getCachedCameraPos();
        long currentTime = System.currentTimeMillis();

        boolean shouldReset = false;

        if (lastResetCameraPos == Vec3.ZERO) {
            shouldReset = true;
        } else {
            double distanceMoved = currentCameraPos.distanceTo(lastResetCameraPos);
            if (distanceMoved > RESET_DISTANCE_THRESHOLD) {
                shouldReset = true;
            }
        }

        if (currentTime - lastResetTime > MIN_RESET_INTERVAL) {
            shouldReset = true;
        }

        if (shouldReset) {
            occlusionCulling.resetCache();
            lastResetCameraPos = currentCameraPos;
            lastResetTime = currentTime;
        }

        lastCameraUpdate = 0;
    }

    public void dispose() {
        cullCache.clear();
        lastResetCameraPos = Vec3.ZERO;
        lastResetTime = 0;
    }

    private boolean isPlayerSprinting() {
        return mc.player != null && mc.player.isSprinting();
    }

    public double getCurrentCullingDistance() {
        return cachedCullingDistance;
    }

    public OcclusionCullingInstance getOcclusionCulling() {
        return occlusionCulling;
    }

    public void forceResetCache() {
        occlusionCulling.resetCache();
        lastResetCameraPos = getCachedCameraPos();
        lastResetTime = System.currentTimeMillis();
    }

    public static class AABBOBJ {
        public double minX, minY, minZ;
        public double maxX, maxY, maxZ;

        public AABBOBJ(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            set(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public void set(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public double getXsize() {
            return maxX - minX;
        }

        public double getYsize() {
            return maxY - minY;
        }

        public double getZsize() {
            return maxZ - minZ;
        }
    }
}