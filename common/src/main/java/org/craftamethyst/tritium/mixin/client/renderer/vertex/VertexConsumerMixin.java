package org.craftamethyst.tritium.mixin.client.renderer.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Math;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {

    /**
     * @author ZCRAFT
     * Zero-allocation vertex transformation with manual matrix expansion
     */
    @Overwrite
    default VertexConsumer addVertex(Matrix4f matrix, float x, float y, float z) {
        final float m00 = matrix.m00(), m10 = matrix.m10(), m20 = matrix.m20(), m30 = matrix.m30();
        final float m01 = matrix.m01(), m11 = matrix.m11(), m21 = matrix.m21(), m31 = matrix.m31();
        final float m02 = matrix.m02(), m12 = matrix.m12(), m22 = matrix.m22(), m32 = matrix.m32();

        final float xt = m00 * x + m10 * y + m20 * z + m30;
        final float yt = m01 * x + m11 * y + m21 * z + m31;
        final float zt = m02 * x + m12 * y + m22 * z + m32;

        return ((VertexConsumer) this).addVertex(xt, yt, zt);
    }

    /**
     * @author ZCRAFT
     * Optimized normal transformation with manual matrix expansion
     */
    @Overwrite
    default VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {
        final Matrix3f matrix = pose.normal();
        final float m00 = matrix.m00(), m10 = matrix.m10(), m20 = matrix.m20();
        final float m01 = matrix.m01(), m11 = matrix.m11(), m21 = matrix.m21();
        final float m02 = matrix.m02(), m12 = matrix.m12(), m22 = matrix.m22();

        float xt = m00 * x + m10 * y + m20 * z;
        float yt = m01 * x + m11 * y + m21 * z;
        float zt = m02 * x + m12 * y + m22 * z;
        PoseStackPoseAccessor poseAccessor = (PoseStackPoseAccessor) (Object) pose;
        if (!poseAccessor.isTrustedNormals()) {
            return this.tritium$setNormalNormalized(xt, yt, zt);
        }

        return ((VertexConsumer) this).setNormal(xt, yt, zt);
    }

    @Unique
    private VertexConsumer tritium$setNormalNormalized(float x, float y, float z) {
        final float lenSq = Math.fma(x, x, Math.fma(y, y, z * z));
        if (lenSq < 1.0E-8f) {
            return ((VertexConsumer) this).setNormal(0.0f, 1.0f, 0.0f);
        }
        if (lenSq > 0.999f && lenSq < 1.001f) {
            return ((VertexConsumer) this).setNormal(x, y, z);
        }
        final float invLen = Math.invsqrt(lenSq);
        return ((VertexConsumer) this).setNormal(x * invLen, y * invLen, z * invLen);
    }
}