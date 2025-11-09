package org.craftamethyst.tritium.mixin.chunk;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunk.class)
public class NoiseSamplingCacheMixin {

    @Unique
    private final ThreadLocal<DensityFunction.SinglePointContext> tritium$reusableContext =
            ThreadLocal.withInitial(() -> new DensityFunction.SinglePointContext(0, 0, 0));
    @Final
    @Shadow
    int firstNoiseX;
    @Final
    @Shadow
    int firstNoiseZ;

    @Inject(method = "initializeForFirstCellX", at = @At("HEAD"))
    private void onFirstCell(CallbackInfo ci) {
        if (!TritiumConfig.get().serverPerformance.noiseSamplingCache) {
            return;
        }
        int startBlockX = net.minecraft.core.QuartPos.toBlock(firstNoiseX);
        int startBlockZ = net.minecraft.core.QuartPos.toBlock(firstNoiseZ);
        tritium$reusableContext.set(new DensityFunction.SinglePointContext(startBlockX, 0, startBlockZ));
    }
}