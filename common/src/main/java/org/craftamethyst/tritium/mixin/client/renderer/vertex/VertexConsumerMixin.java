package org.craftamethyst.tritium.mixin.client.renderer.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.craftamethyst.tritium.util.VertexBufferCache;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {

    @Inject(method = "vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
            at = @At("HEAD"), cancellable = true)
    private void tritium$zeroAllocVertex(Matrix4f mat, float x, float y, float z,
                                          CallbackInfoReturnable<VertexConsumer> cir) {
        Vector3f v = VertexBufferCache.get().set(x, y, z);
        mat.transformPosition(v);
        ((VertexConsumer) this).vertex(v.x, v.y, v.z);
        cir.setReturnValue((VertexConsumer) this);
    }

    /*@Inject(method = "normal(Lorg/joml/Matrix3f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
            at = @At("HEAD"), cancellable = true)
    private void tritium$zeroAllocNormal(Matrix3f matrix, float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
        Vector3f n = VertexBufferCache.get().set(x, y, z);
        //matrix.normalizedPositiveX(n);
        ((VertexConsumer) this).vertex(n.x, n.y, n.z);
        cir.setReturnValue((VertexConsumer) this);
    }*/
}