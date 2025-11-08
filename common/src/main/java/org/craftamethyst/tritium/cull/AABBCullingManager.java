package org.craftamethyst.tritium.cull;

import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.craftamethyst.tritium.cull.iface.BlockEntityVisibility;
import org.craftamethyst.tritium.cull.iface.EntityVisibility;

public class AABBCullingManager {

    private static final ThreadLocal<OcclusionCullingInstance> OCCLUSION = ThreadLocal.withInitial(
            () -> new OcclusionCullingInstance(128, new OcclusionProvider())
    );

    public static boolean isEntityVisible(EntityVisibility entity, Vec3 camera, BlockGetter level) {
        AABB aabb = entity.graphene$getCullingBox();
        if (exceedsLimit(aabb)) return true;
        return OCCLUSION.get().isAABBVisible(toVec3d(camera), toVec3d(aabb.minX, aabb.minY, aabb.minZ), toVec3d(aabb.maxX, aabb.maxY, aabb.maxZ));
    }

    public static boolean isBlockEntityVisible(BlockEntityVisibility be, Vec3 camera, BlockGetter level) {
        AABB aabb = be.graphene$getCullingBox();
        if (exceedsLimit(aabb)) return true;
        return OCCLUSION.get().isAABBVisible(toVec3d(camera), toVec3d(aabb.minX, aabb.minY, aabb.minZ), toVec3d(aabb.maxX, aabb.maxY, aabb.maxZ));
    }

    private static boolean exceedsLimit(AABB aabb) {
        double limit = TritiumConfig.get().rendering.occlusionCulling.hitboxSizeLimit;
        return (aabb.getXsize() > limit) || (aabb.getYsize() > limit) || (aabb.getZsize() > limit);
    }

    private static Vec3d toVec3d(Vec3 v) {
        return new Vec3d(v.x, v.y, v.z);
    }

    private static Vec3d toVec3d(double x, double y, double z) {
        return new Vec3d(x, y, z);
    }
}
