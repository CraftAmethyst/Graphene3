package org.craftamethyst.tritium.mixin.client.renderer.fbo;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public abstract class FastBlit {

    @Inject(
            method = "_blitToScreen",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tritium$fastBlit(int width, int height, boolean disableBlend, CallbackInfo ci) {
        if (!TritiumConfigBase.Rendering.FastBlit.fastBlit || !disableBlend) {
            return;
        }

        RenderTarget target = (RenderTarget) (Object) this;
        int srcFbo = target.frameBufferId;

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, srcFbo);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);

        GL30.glBlitFramebuffer(
                0, 0, width, height,
                0, 0, width, height,
                GL30.GL_COLOR_BUFFER_BIT,
                GL30.GL_NEAREST
        );

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        ci.cancel();
    }
}