package org.craftamethyst.tritium.mixin.math;

import net.minecraft.util.Mth;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mth.class)
public class MthMixin {
    @Inject(
            method = "lerp(FFF)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void optimizeLerpFloat(float pDelta, float pStart, float pEnd, CallbackInfoReturnable<Float> cir) {
        if (TritiumConfigBase.Performance.MathOptimizations.enableMathOptimizations &&
                TritiumConfigBase.Performance.MathOptimizations.optimizeLerpFunctions) {
            cir.setReturnValue(Math.fma(pDelta, pEnd - pStart, pStart));
        }
    }

    @Inject(
            method = "lerp(DDD)D",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void optimizeLerpDouble(double pDelta, double pStart, double pEnd, CallbackInfoReturnable<Double> cir) {
        if (TritiumConfigBase.Performance.MathOptimizations.enableMathOptimizations &&
                TritiumConfigBase.Performance.MathOptimizations.optimizeLerpFunctions) {
            cir.setReturnValue(Math.fma(pDelta, pEnd - pStart, pStart));
        }
    }

    @Inject(
            method = "lengthSquared(DD)D",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void optimizeLengthSquared2D(double pXDistance, double pYDistance, CallbackInfoReturnable<Double> cir) {
        if (TritiumConfigBase.Performance.MathOptimizations.enableMathOptimizations &&
                TritiumConfigBase.Performance.MathOptimizations.optimizeLengthSquared) {
            cir.setReturnValue(Math.fma(pXDistance, pXDistance, pYDistance * pYDistance));
        }
    }

    @Inject(
            method = "lengthSquared(DDD)D",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void optimizeLengthSquared3D(double pXDistance, double pYDistance, double pZDistance, CallbackInfoReturnable<Double> cir) {
        if (TritiumConfigBase.Performance.MathOptimizations.enableMathOptimizations &&
                TritiumConfigBase.Performance.MathOptimizations.optimizeLengthSquared) {
            cir.setReturnValue(Math.fma(pXDistance, pXDistance,
                    Math.fma(pYDistance, pYDistance, pZDistance * pZDistance)));
        }
    }

    @Inject(
            method = "nextFloat(Lnet/minecraft/util/RandomSource;FF)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void optimizeNextFloat(net.minecraft.util.RandomSource pRandom, float pMinimum, float pMaximum, CallbackInfoReturnable<Float> cir) {
        if (TritiumConfigBase.Performance.MathOptimizations.enableMathOptimizations &&
                TritiumConfigBase.Performance.MathOptimizations.optimizeRandomFunctions) {
            if (pMinimum >= pMaximum) {
                cir.setReturnValue(pMinimum);
            } else {
                cir.setReturnValue(Math.fma(pRandom.nextFloat(), pMaximum - pMinimum, pMinimum));
            }
        }
    }

    @Inject(
            method = "nextDouble(Lnet/minecraft/util/RandomSource;DD)D",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void optimizeNextDouble(net.minecraft.util.RandomSource pRandom, double pMinimum, double pMaximum, CallbackInfoReturnable<Double> cir) {
        if (TritiumConfigBase.Performance.MathOptimizations.enableMathOptimizations &&
                TritiumConfigBase.Performance.MathOptimizations.optimizeRandomFunctions) {
            if (pMinimum >= pMaximum) {
                cir.setReturnValue(pMinimum);
            } else {
                cir.setReturnValue(Math.fma(pRandom.nextDouble(), pMaximum - pMinimum, pMinimum));
            }
        }
    }

    @Inject(
            method = "normal(Lnet/minecraft/util/RandomSource;FF)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void optimizeNormal(net.minecraft.util.RandomSource pRandom, float pMean, float pDeviation, CallbackInfoReturnable<Float> cir) {
        if (TritiumConfigBase.Performance.MathOptimizations.enableMathOptimizations &&
                TritiumConfigBase.Performance.MathOptimizations.optimizeRandomFunctions) {
            cir.setReturnValue(Math.fma((float) pRandom.nextGaussian(), pDeviation, pMean));
        }
    }
}