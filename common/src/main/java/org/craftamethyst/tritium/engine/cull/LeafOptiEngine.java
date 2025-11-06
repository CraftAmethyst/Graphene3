package org.craftamethyst.tritium.engine.cull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class LeafOptiEngine {

    public static boolean shouldCullFace(BlockGetter level, BlockPos pos, Direction face) {
        return isLeaf(level.getBlockState(pos.relative(face)));
    }

    /**
     * back-compat: formerly a neighborhood heuristic; now identical to a simple leaf adjacency check.
     */
    public static boolean checkSimpleConnection(BlockGetter level, BlockPos pos, Direction face) {
        // In the new model, a "connection" means the target position is a leaf.
        return isLeaf(level.getBlockState(pos));
    }

    /**
     * back-compat overload.
     */
    public static boolean checkSimpleConnection(BlockGetter level, BlockPos pos) {
        return isLeaf(level.getBlockState(pos));
    }

    /**
     * back-compat: preserved for callers; simplified to the same adjacency test.
     */
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
