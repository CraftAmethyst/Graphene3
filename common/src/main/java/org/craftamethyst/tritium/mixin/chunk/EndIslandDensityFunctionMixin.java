package org.craftamethyst.tritium.mixin.chunk;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$EndIslandDensityFunction")
public class EndIslandDensityFunctionMixin {

    @Unique
    private static final int OPTIMIZED_RADIUS = 8;
    @Unique
    private static final long MAX_DISTANCE_SQ = 4096L;
    @Unique
    private static final float[] PRECOMPUTED_RARITY = new float[25];

    static {
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int index = (i + 2) * 5 + (j + 2);
                PRECOMPUTED_RARITY[index] = (Math.abs(i) * 3439.0F + Math.abs(j) * 147.0F) % 13.0F + 9.0F;
            }
        }
    }

    /**
     * @author ZCRAFT
     * @reason optimize
     */
    @Overwrite
    private static float getHeightValue(SimplexNoise noise, int x, int z) {
        int centerX = x / 2;
        int centerZ = z / 2;
        int offsetX = x % 2;
        int offsetZ = z % 2;
        int distanceSq = x * x + z * z;
        float distance = (float) Math.sqrt(distanceSq);
        float baseHeight = 100.0F - distance * 8.0F;
        baseHeight = Mth.clamp(baseHeight, -100.0F, 80.0F);
        for (int dx = -OPTIMIZED_RADIUS; dx <= OPTIMIZED_RADIUS; dx++) {
            long worldX = centerX + dx;
            long worldXSq = worldX * worldX;

            for (int dz = -OPTIMIZED_RADIUS; dz <= OPTIMIZED_RADIUS; dz++) {
                long worldZ = centerZ + dz;
                long chunkDistanceSq = worldXSq + worldZ * worldZ;
                if (chunkDistanceSq <= MAX_DISTANCE_SQ) {
                    continue;
                }
                    float rarity;
                    if (dx >= -2 && dx <= 2 && dz >= -2 && dz <= 2) {
                        int index = (dx + 2) * 5 + (dz + 2);
                        rarity = PRECOMPUTED_RARITY[index];
                    } else {
                        rarity = (Math.abs((float)worldX) * 3439.0F +
                                Math.abs((float)worldZ) * 147.0F) % 13.0F + 9.0F;
                    }
                    float localX = offsetX - dx * 2;
                    float localZ = offsetZ - dz * 2;
                    float localDistanceSq = localX * localX + localZ * localZ;
                    float localDistance = (float) Math.sqrt(localDistanceSq);
                    float islandHeight = 100.0F - localDistance * rarity;
                    islandHeight = Mth.clamp(islandHeight, -100.0F, 80.0F);

                    baseHeight = Math.max(baseHeight, islandHeight);
            }
        }

        return baseHeight;
    }
}