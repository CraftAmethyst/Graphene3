package org.craftamethyst.tritium.mixin.client.renderer.culling;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.tritium.cull.BlockFaceOcclusionCuller;
import org.craftamethyst.tritium.cull.LeafCulling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class LeafRenderMixin {
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void onShouldRenderFace(BlockState state, BlockGetter level,
                                           BlockPos pos, Direction face,
                                           BlockPos offsetPos,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!TritiumConfig.get().rendering.leafCulling.enableLeafCulling) {
            return;
        }
        if (state.getBlock() instanceof LeavesBlock) {
            // hide inner leaves: if fully enclosed, render no faces.
            if (TritiumConfig.get().rendering.leafCulling.hideInnerLeaves && LeafCulling.shouldHideInnerLeaves(level, pos)) {
                cir.setReturnValue(false);
                return;
            }
            // Use occlusion culler if enabled; otherwise, fall back to adjacency heuristic.
            if (TritiumConfig.get().rendering.leafCulling.enableFaceOcclusionCulling) {
                boolean cull = BlockFaceOcclusionCuller.shouldCullBlockFace(level, pos, face);
                cir.setReturnValue(!cull);
            } else {
                cir.setReturnValue(!LeafCulling.shouldCullFace(level, pos, face));
            }
        }
    }
}
