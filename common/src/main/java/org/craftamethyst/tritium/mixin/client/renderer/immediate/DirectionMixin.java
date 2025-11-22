package org.craftamethyst.tritium.mixin.client.renderer.immediate;

import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Direction.class)
public class DirectionMixin {

    @Overwrite
    public static Direction getNearest(float x, float y, float z) {
        if (x == 0.0f && y == 0.0f && z == 0.0f) {
            return Direction.NORTH;
        }

        float absXSq = Math.fma(x, x, 0.0f);
        float absYSq = Math.fma(y, y, 0.0f);
        float absZSq = Math.fma(z, z, 0.0f);

        if (absYSq >= absZSq) {
            if (absYSq >= absXSq) {
                return (y <= 0.0f) ? Direction.DOWN : Direction.UP;
            }
        } else {
            if (absZSq >= absXSq) {
                return (z <= 0.0f) ? Direction.NORTH : Direction.SOUTH;
            }
        }

        return (x <= 0.0f) ? Direction.WEST : Direction.EAST;
    }
}