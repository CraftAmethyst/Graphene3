package org.craftamethyst.tritium.mixin.jigsaw;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JigsawBlockEntity.class)
public class JigsawBlockEntityMixin {

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void optimizeJigsawGeneration(ServerLevel level, int maxDepth, boolean keepJigsaws, CallbackInfo ci) {
        JigsawBlockEntity self = (JigsawBlockEntity) (Object) this;
        BlockPos pos = self.getBlockPos();
        BlockState state = self.getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            BlockPos checkPos = pos.relative(facing);
            if (!level.isEmptyBlock(checkPos)) {
                boolean hasSpace = false;
                for (int i = 1; i <= 3; i++) {
                    if (level.isEmptyBlock(checkPos.relative(facing, i))) {
                        hasSpace = true;
                        break;
                    }
                }

                if (!hasSpace) {
                    ci.cancel();
                    return;
                }
            }
        }

        JigsawBlockEntity.JointType joint = self.getJoint();
        if (joint != null && joint != JigsawBlockEntity.JointType.ALIGNED && joint != JigsawBlockEntity.JointType.ROLLABLE) {
            ci.cancel();
        }
    }
}