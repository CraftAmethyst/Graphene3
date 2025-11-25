package org.craftamethyst.tritium.mixin.client.renderer.culling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.tritium.config.TritiumConfigBase;
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
        if (!TritiumConfigBase.Rendering.LeafCulling.enableLeafCulling) {
            return;
        }
        if (state.getBlock() instanceof LeavesBlock) {
            // hide inner leaves: if fully enclosed, render no faces.
            if (TritiumConfigBase.Rendering.LeafCulling.hideInnerLeaves && LeafCulling.shouldHideInnerLeaves(level, pos)) {
                cir.setReturnValue(false);
                return;
            }
            // Use occlusion culler if enabled; otherwise, fall back to adjacency heuristic.
            if (TritiumConfigBase.Rendering.LeafCulling.enableFaceOcclusionCulling) {
                boolean cull = BlockFaceOcclusionCuller.shouldCullBlockFace(level, pos, face);
                cir.setReturnValue(!cull);
            } else {
                cir.setReturnValue(!LeafCulling.shouldCullFace(level, pos, face));
            }
        }
    }
}
