package org.craftamethyst.tritium.mixin.client.renderer.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.craftamethyst.tritium.util.VertexBufferCache;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {

    @Shadow VertexConsumer vertex(double v, double v1, double v2);

    /*@Inject(method = "vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
            at = @At("HEAD"), cancellable = true)
    private void tritium$zeroAllocVertex(Matrix4f mat, float x, float y, float z,
                                          CallbackInfoReturnable<VertexConsumer> cir) {
        Vector3f v = VertexBufferCache.get().set(x, y, z);
        mat.transformPosition(v);
        ((VertexConsumer) this).vertex(v.x, v.y, v.z);
        cir.setReturnValue((VertexConsumer) this);
    }*/

    /**
     * @author KSmc_brigade
     * @reason sync 1.21
     */
    @Overwrite
    default VertexConsumer vertex(Matrix4f mat, float x, float y, float z) {
        Vector3f v = VertexBufferCache.get().set(x, y, z);
        mat.transformPosition(v);
        return this.vertex(v.x, v.y, v.z);
    }
}