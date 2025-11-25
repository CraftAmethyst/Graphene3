package org.craftamethyst.tritium.mixin.cache;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.BlockStatePairKey.class)
public class BlockStatePairKeyLazyCache {
    @Final
    @Shadow
    private BlockState first;

    @Final
    @Shadow
    private BlockState second;

    @Final
    @Shadow
    private Direction direction;

    @Unique
    private int tritium$cachedHash = 0;

    @Unique
    private boolean tritium$isHashComputed = false;

    @Inject(method = "hashCode", at = @At("HEAD"), cancellable = true)
    private void onHashCode(CallbackInfoReturnable<Integer> cir) {
        if (!tritium$isHashComputed) {
            computeAndCacheHashCode();
        }
        cir.setReturnValue(tritium$cachedHash);
    }

    @Unique
    private void computeAndCacheHashCode() {
        int firstHash = this.first.hashCode();
        int secondHash = this.second.hashCode();
        int directionHash = this.direction.hashCode();
        int result = firstHash;
        result = (result << 5) - result + secondHash;
        result = (result << 5) - result + directionHash;

        this.tritium$cachedHash = result;
        this.tritium$isHashComputed = true;
    }
}