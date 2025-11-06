package org.craftamethyst.tritium.mixin.client.renderer.culling;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.craftamethyst.tritium.engine.cull.LeafOptiEngine;
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
            if (TritiumConfig.get().rendering.leafCulling.hideInnerLeaves && LeafOptiEngine.shouldHideInnerLeaves(level, pos)) {
                cir.setReturnValue(false);
                return;
            }
            // otherwise, cull faces that touch another leaf block.
            cir.setReturnValue(!LeafOptiEngine.shouldCullFace(level, pos, face));
        }
    }
}
