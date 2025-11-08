package org.craftamethyst.tritium.cull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class LeafCulling {

    private LeafCulling() {
    }

    public static boolean shouldCullFace(BlockGetter level, BlockPos pos, Direction face) {
        return isLeaf(level.getBlockState(pos.relative(face)));
    }

    public static boolean checkSimpleConnection(BlockGetter level, BlockPos pos, Direction face) {
        return isLeaf(level.getBlockState(pos));
    }

    public static boolean checkSimpleConnection(BlockGetter level, BlockPos pos) {
        return isLeaf(level.getBlockState(pos));
    }

    public static boolean checkConnectedLeaves(BlockGetter level, BlockPos pos, @Nullable Direction face) {
        return isLeaf(level.getBlockState(pos));
    }

    public static boolean shouldHideInnerLeaves(BlockGetter level, BlockPos pos) {
        BlockState self = level.getBlockState(pos);
        if (!isLeaf(self)) return false;
        for (Direction dir : Direction.values()) {
            BlockPos otherPos = pos.relative(dir);
            BlockState other = level.getBlockState(otherPos);
            if (!(isLeaf(other) || other.isSolidRender(level, otherPos))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLeaf(BlockState state) {
        return state.getBlock() instanceof LeavesBlock;
    }
}
