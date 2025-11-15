package org.craftamethyst.tritium.mixin.client.renderer.fbo;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import org.craftamethyst.tritium.gpu.FramebufferFixer;
import org.craftamethyst.tritium.gpu.GpuPlus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.ref.Cleaner;

@Mixin(RenderTarget.class)
public abstract class FramebufferCleanerMixin implements FramebufferFixer, Cleaner.Cleanable {

    @Shadow
    public int colorTextureId;
    @Shadow
    public int depthBufferId;
    @Shadow
    public int frameBufferId;

    @Override
    public void clean() {
        try {
            if (TritiumConfig.get().rendering.gpuPlus
                    && (this.colorTextureId > -1 || this.depthBufferId > -1 || this.frameBufferId > -1)) {
                GpuPlus.enqueue(this);
            }
        } catch (Exception ignored) {
            // we avoid crashing the game due to clean up issues; failures will be logged when processing the queue.
        }
    }

    @Override
    public void destroy() {
        GlStateManager._bindTexture(0);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void release() {
        if (this.colorTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorTextureId);
        }

        if (this.depthBufferId > -1) {
            TextureUtil.releaseTextureId(this.depthBufferId);
        }

        if (this.frameBufferId > -1) {
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
            GlStateManager._glDeleteFramebuffers(this.frameBufferId);
        }
    }

    @Override
    public String toString() {
        return "RenderTarget(" + this.colorTextureId + ", " + this.depthBufferId + ", " + this.frameBufferId + ")";
    }
}
