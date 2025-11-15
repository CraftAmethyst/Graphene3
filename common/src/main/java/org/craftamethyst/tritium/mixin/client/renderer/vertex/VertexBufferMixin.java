package org.craftamethyst.tritium.mixin.client.renderer.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import org.craftamethyst.tritium.gpu.GpuPlusGL;
import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.ByteBuffer;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin {

    @Shadow
    private int vertexBufferId;

    @Redirect(method = {"uploadVertexBuffer", "uploadIndexBuffer"},
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;glBufferData(ILjava/nio/ByteBuffer;I)V"))
    private void tritium$gpuPlusUpload(int target, ByteBuffer data, int usage) {
        if (!TritiumConfig.get().rendering.gpuPlus) {
            RenderSystem.glBufferData(target, data, usage);
            return;
        }

        if (target == GL15.GL_ARRAY_BUFFER && !TritiumConfig.get().rendering.gpuPlusVbo) {
            RenderSystem.glBufferData(target, data, usage);
            return;
        }
        if (target == GL15.GL_ELEMENT_ARRAY_BUFFER && !TritiumConfig.get().rendering.gpuPlusIndex) {
            RenderSystem.glBufferData(target, data, usage);
            return;
        }

        int storageFlags = ARBBufferStorage.GL_DYNAMIC_STORAGE_BIT;
        GpuPlusGL.uploadBoundBuffer(target, data, usage, storageFlags);
    }
}
