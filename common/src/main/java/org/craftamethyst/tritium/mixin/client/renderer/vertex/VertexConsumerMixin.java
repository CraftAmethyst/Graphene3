package org.craftamethyst.tritium.mixin.client.renderer.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.craftamethyst.tritium.util.VertexBufferCache;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {

    /**
     * @author ZCRAFT
     *Zero-allocation vertex transformation using cached Vector3f
     */
    @Overwrite
    default VertexConsumer addVertex(Matrix4f mat, float x, float y, float z) {
        Vector3f v = VertexBufferCache.get().set(x, y, z);
        mat.transformPosition(v);
        ((VertexConsumer) this).addVertex(v.x, v.y, v.z);
        return (VertexConsumer) this;
    }

    /**
     * @author ZCRAFT
     *Zero-allocation normal transformation using cached Vector3f
     */
    @Overwrite
    default VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {
        Vector3f n = VertexBufferCache.get().set(x, y, z);
        pose.transformNormal(n.x, n.y, n.z, n);
        ((VertexConsumer) this).setNormal(n.x, n.y, n.z);
        return (VertexConsumer) this;
    }
}