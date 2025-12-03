package org.craftamethyst.tritium.mixin.chunk;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImprovedNoise.class)
public abstract class ImprovedNoiseMixin {
    @Shadow @Final
    public double xo;

    @Shadow @Final
    public double yo;

    @Shadow @Final
    public double zo;

    @Shadow @Final
    private byte[] p;

    @Unique
    private static final double[] OPTIMIZED_GRADIENTS = tritium$createOptimizedGradients();

    @Unique
    private int[] tritium$optimizedPermutation;

    @Unique
    private static double[] tritium$createOptimizedGradients() {
        int[][] simplexGradients = {
                {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
                {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
                {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1},
                {1, 1, 0}, {-1, 1, 0}, {0, -1, 1}, {0, -1, -1}
        };

        double[] gradients = new double[48];
        for (int i = 0; i < 16; i++) {
            gradients[i * 3] = simplexGradients[i][0];
            gradients[i * 3 + 1] = simplexGradients[i][1];
            gradients[i * 3 + 2] = simplexGradients[i][2];
        }

        return gradients;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void tritium$onInit(RandomSource random, CallbackInfo ci) {
        this.tritium$optimizedPermutation = new int[512];
        for (int i = 0; i < 256; i++) {
            this.tritium$optimizedPermutation[i] = this.p[i] & 0xFF;
            this.tritium$optimizedPermutation[i + 256] = this.p[i] & 0xFF;
        }
    }

    /**
     * @author ZCRAFT
     * @reason Optimize PerlinNoise
     */
    @Deprecated
    @Overwrite
    public double noise(double x, double y, double z, double yScale, double yMax) {
        double offsetX = x + this.xo;
        double offsetY = y + this.yo;
        double offsetZ = z + this.zo;

        int gridX = Mth.floor(offsetX);
        int gridY = Mth.floor(offsetY);
        int gridZ = Mth.floor(offsetZ);

        double deltaX = offsetX - gridX;
        double deltaY = offsetY - gridY;
        double deltaZ = offsetZ - gridZ;

        double yShift;
        if (yScale != 0.0) {
            double clampedY = yMax >= 0.0 && yMax < deltaY ? yMax : deltaY;
            yShift = Mth.floor(clampedY / yScale + 1.0E-7F) * yScale;
        } else {
            yShift = 0.0;
        }

        return tritium$optimizedSampleAndLerp(
                gridX, gridY, gridZ,
                deltaX, deltaY - yShift, deltaZ, deltaY
        );
    }

    @Unique
    private double tritium$optimizedSampleAndLerp(int gridX, int gridY, int gridZ,
                                                  double deltaX, double weirdDeltaY,
                                                  double deltaZ, double deltaY) {
        int idxX0 = this.tritium$optimizedPermutation[gridX & 0xFF];
        int idxX1 = this.tritium$optimizedPermutation[(gridX + 1) & 0xFF];

        int idxXY00 = this.tritium$optimizedPermutation[idxX0 + gridY];
        int idxXY10 = this.tritium$optimizedPermutation[idxX1 + gridY];
        int idxXY01 = this.tritium$optimizedPermutation[idxX0 + gridY + 1];
        int idxXY11 = this.tritium$optimizedPermutation[idxX1 + gridY + 1];

        int gradIdx000 = this.tritium$optimizedPermutation[idxXY00 + gridZ] & 15;
        int gradIdx100 = this.tritium$optimizedPermutation[idxXY10 + gridZ] & 15;
        int gradIdx010 = this.tritium$optimizedPermutation[idxXY01 + gridZ] & 15;
        int gradIdx110 = this.tritium$optimizedPermutation[idxXY11 + gridZ] & 15;
        int gradIdx001 = this.tritium$optimizedPermutation[idxXY00 + gridZ + 1] & 15;
        int gradIdx101 = this.tritium$optimizedPermutation[idxXY10 + gridZ + 1] & 15;
        int gradIdx011 = this.tritium$optimizedPermutation[idxXY01 + gridZ + 1] & 15;
        int gradIdx111 = this.tritium$optimizedPermutation[idxXY11 + gridZ + 1] & 15;

        double d0 = tritium$optimizedGradDot(gradIdx000, deltaX, weirdDeltaY, deltaZ);
        double d1 = tritium$optimizedGradDot(gradIdx100, deltaX - 1.0, weirdDeltaY, deltaZ);
        double d2 = tritium$optimizedGradDot(gradIdx010, deltaX, weirdDeltaY - 1.0, deltaZ);
        double d3 = tritium$optimizedGradDot(gradIdx110, deltaX - 1.0, weirdDeltaY - 1.0, deltaZ);
        double d4 = tritium$optimizedGradDot(gradIdx001, deltaX, weirdDeltaY, deltaZ - 1.0);
        double d5 = tritium$optimizedGradDot(gradIdx101, deltaX - 1.0, weirdDeltaY, deltaZ - 1.0);
        double d6 = tritium$optimizedGradDot(gradIdx011, deltaX, weirdDeltaY - 1.0, deltaZ - 1.0);
        double d7 = tritium$optimizedGradDot(gradIdx111, deltaX - 1.0, weirdDeltaY - 1.0, deltaZ - 1.0);

        double smoothX = tritium$optimizedSmoothstep(deltaX);
        double smoothY = tritium$optimizedSmoothstep(deltaY);
        double smoothZ = tritium$optimizedSmoothstep(deltaZ);

        return Mth.lerp3(smoothX, smoothY, smoothZ, d0, d1, d2, d3, d4, d5, d6, d7);
    }

    @Unique
    private double tritium$optimizedGradDot(int gradIdx, double x, double y, double z) {
        int baseIdx = gradIdx * 3;
        double gradX = OPTIMIZED_GRADIENTS[baseIdx];
        double gradY = OPTIMIZED_GRADIENTS[baseIdx + 1];
        double gradZ = OPTIMIZED_GRADIENTS[baseIdx + 2];

        return gradX * x + gradY * y + gradZ * z;
    }

    @Unique
    private double tritium$optimizedSmoothstep(double t) {
        double t3 = t * t * t;
        return t3 * (t * (t * 6.0 - 15.0) + 10.0);
    }

    /**
     * @author ZCRAFT
     * @reason Optimize PerlinNoise-noiseWithDerivative
     */
    @Overwrite
    public double noiseWithDerivative(double x, double y, double z, double[] values) {
        double offsetX = x + this.xo;
        double offsetY = y + this.yo;
        double offsetZ = z + this.zo;

        int gridX = Mth.floor(offsetX);
        int gridY = Mth.floor(offsetY);
        int gridZ = Mth.floor(offsetZ);

        double deltaX = offsetX - gridX;
        double deltaY = offsetY - gridY;
        double deltaZ = offsetZ - gridZ;

        return tritium$optimizedSampleWithDerivative(gridX, gridY, gridZ, deltaX, deltaY, deltaZ, values);
    }

    @Unique
    private double tritium$optimizedSampleWithDerivative(int gridX, int gridY, int gridZ,
                                                         double deltaX, double deltaY, double deltaZ,
                                                         double[] values) {
        int idxX0 = this.tritium$optimizedPermutation[gridX & 0xFF];
        int idxX1 = this.tritium$optimizedPermutation[(gridX + 1) & 0xFF];

        int idxXY00 = this.tritium$optimizedPermutation[idxX0 + gridY];
        int idxXY10 = this.tritium$optimizedPermutation[idxX1 + gridY];
        int idxXY01 = this.tritium$optimizedPermutation[idxX0 + gridY + 1];
        int idxXY11 = this.tritium$optimizedPermutation[idxX1 + gridY + 1];

        int gradIdx000 = this.tritium$optimizedPermutation[idxXY00 + gridZ] & 15;
        int gradIdx100 = this.tritium$optimizedPermutation[idxXY10 + gridZ] & 15;
        int gradIdx010 = this.tritium$optimizedPermutation[idxXY01 + gridZ] & 15;
        int gradIdx110 = this.tritium$optimizedPermutation[idxXY11 + gridZ] & 15;
        int gradIdx001 = this.tritium$optimizedPermutation[idxXY00 + gridZ + 1] & 15;
        int gradIdx101 = this.tritium$optimizedPermutation[idxXY10 + gridZ + 1] & 15;
        int gradIdx011 = this.tritium$optimizedPermutation[idxXY01 + gridZ + 1] & 15;
        int gradIdx111 = this.tritium$optimizedPermutation[idxXY11 + gridZ + 1] & 15;

        int base000 = gradIdx000 * 3;
        int base100 = gradIdx100 * 3;
        int base010 = gradIdx010 * 3;
        int base110 = gradIdx110 * 3;
        int base001 = gradIdx001 * 3;
        int base101 = gradIdx101 * 3;
        int base011 = gradIdx011 * 3;
        int base111 = gradIdx111 * 3;

        double d0 = tritium$optimizedGradDot(gradIdx000, deltaX, deltaY, deltaZ);
        double d1 = tritium$optimizedGradDot(gradIdx100, deltaX - 1.0, deltaY, deltaZ);
        double d2 = tritium$optimizedGradDot(gradIdx010, deltaX, deltaY - 1.0, deltaZ);
        double d3 = tritium$optimizedGradDot(gradIdx110, deltaX - 1.0, deltaY - 1.0, deltaZ);
        double d4 = tritium$optimizedGradDot(gradIdx001, deltaX, deltaY, deltaZ - 1.0);
        double d5 = tritium$optimizedGradDot(gradIdx101, deltaX - 1.0, deltaY, deltaZ - 1.0);
        double d6 = tritium$optimizedGradDot(gradIdx011, deltaX, deltaY - 1.0, deltaZ - 1.0);
        double d7 = tritium$optimizedGradDot(gradIdx111, deltaX - 1.0, deltaY - 1.0, deltaZ - 1.0);

        double smoothX = tritium$optimizedSmoothstep(deltaX);
        double smoothY = tritium$optimizedSmoothstep(deltaY);
        double smoothZ = tritium$optimizedSmoothstep(deltaZ);

        double gradX0 = OPTIMIZED_GRADIENTS[base000];
        double gradX1 = OPTIMIZED_GRADIENTS[base100];
        double gradX2 = OPTIMIZED_GRADIENTS[base010];
        double gradX3 = OPTIMIZED_GRADIENTS[base110];
        double gradX4 = OPTIMIZED_GRADIENTS[base001];
        double gradX5 = OPTIMIZED_GRADIENTS[base101];
        double gradX6 = OPTIMIZED_GRADIENTS[base011];
        double gradX7 = OPTIMIZED_GRADIENTS[base111];

        double gradY0 = OPTIMIZED_GRADIENTS[base000 + 1];
        double gradY1 = OPTIMIZED_GRADIENTS[base100 + 1];
        double gradY2 = OPTIMIZED_GRADIENTS[base010 + 1];
        double gradY3 = OPTIMIZED_GRADIENTS[base110 + 1];
        double gradY4 = OPTIMIZED_GRADIENTS[base001 + 1];
        double gradY5 = OPTIMIZED_GRADIENTS[base101 + 1];
        double gradY6 = OPTIMIZED_GRADIENTS[base011 + 1];
        double gradY7 = OPTIMIZED_GRADIENTS[base111 + 1];

        double gradZ0 = OPTIMIZED_GRADIENTS[base000 + 2];
        double gradZ1 = OPTIMIZED_GRADIENTS[base100 + 2];
        double gradZ2 = OPTIMIZED_GRADIENTS[base010 + 2];
        double gradZ3 = OPTIMIZED_GRADIENTS[base110 + 2];
        double gradZ4 = OPTIMIZED_GRADIENTS[base001 + 2];
        double gradZ5 = OPTIMIZED_GRADIENTS[base101 + 2];
        double gradZ6 = OPTIMIZED_GRADIENTS[base011 + 2];
        double gradZ7 = OPTIMIZED_GRADIENTS[base111 + 2];

        double interpolatedX = Mth.lerp3(smoothX, smoothY, smoothZ,
                gradX0, gradX1, gradX2, gradX3, gradX4, gradX5, gradX6, gradX7);
        double interpolatedY = Mth.lerp3(smoothX, smoothY, smoothZ,
                gradY0, gradY1, gradY2, gradY3, gradY4, gradY5, gradY6, gradY7);
        double interpolatedZ = Mth.lerp3(smoothX, smoothY, smoothZ,
                gradZ0, gradZ1, gradZ2, gradZ3, gradZ4, gradZ5, gradZ6, gradZ7);

        double diff01 = d1 - d0;
        double diff23 = d3 - d2;
        double diff45 = d5 - d4;
        double diff67 = d7 - d6;
        double diff02 = d2 - d0;
        double diff46 = d6 - d4;
        double diff13 = d3 - d1;
        double diff57 = d7 - d5;
        double diff04 = d4 - d0;
        double diff15 = d5 - d1;
        double diff26 = d6 - d2;
        double diff37 = d7 - d3;

        double derivativeX = Mth.lerp2(smoothY, smoothZ, diff01, diff23, diff45, diff67);
        double derivativeY = Mth.lerp2(smoothZ, smoothX, diff02, diff46, diff13, diff57);
        double derivativeZ = Mth.lerp2(smoothX, smoothY, diff04, diff15, diff26, diff37);

        double smoothDerivativeX = tritium$optimizedSmoothstepDerivative(deltaX);
        double smoothDerivativeY = tritium$optimizedSmoothstepDerivative(deltaY);
        double smoothDerivativeZ = tritium$optimizedSmoothstepDerivative(deltaZ);

        values[0] += interpolatedX + smoothDerivativeX * derivativeX;
        values[1] += interpolatedY + smoothDerivativeY * derivativeY;
        values[2] += interpolatedZ + smoothDerivativeZ * derivativeZ;

        return Mth.lerp3(smoothX, smoothY, smoothZ, d0, d1, d2, d3, d4, d5, d6, d7);
    }

    @Unique
    private double tritium$optimizedSmoothstepDerivative(double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        return 30.0 * t4 - 60.0 * t3 + 30.0 * t2;
    }
}