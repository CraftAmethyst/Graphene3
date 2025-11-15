package org.craftamethyst.tritium.mixin.client.memleakfix;

import com.mojang.blaze3d.platform.GlUtil;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(Minecraft.class)
public class ScreenshotByteBufferLeakFixMixin {

    @Unique
    private ByteBuffer tritium$buffer = null;

    /**
     * Intercept the GlUtil.allocateMemory call in grabHugeScreenshot to track the allocated buffer
     * and free it after the screenshot is taken, preventing a native memory leak.
     */
    @Redirect(
            method = "grabHugeScreenshot",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlUtil;allocateMemory(I)Ljava/nio/ByteBuffer;"
            )
    )
    private ByteBuffer tritium$redirectAllocateMemory(int size) {
        if (!TritiumConfig.get().fixes.MemoryLeakFix_ScreenshotByteBufferLeakFix) {
            return GlUtil.allocateMemory(size);
        }

        ByteBuffer buffer = GlUtil.allocateMemory(size);
        this.tritium$buffer = buffer;
        return buffer;
    }

    @Inject(method = "grabHugeScreenshot", at = @At("RETURN"))
    private void tritium$freeOnReturn(CallbackInfoReturnable<Component> cir) {
        if (TritiumConfig.get().fixes.MemoryLeakFix_ScreenshotByteBufferLeakFix) {
            if (this.tritium$buffer != null) {
                GlUtil.freeMemory(this.tritium$buffer);
                this.tritium$buffer = null;
            }
        }
    }
}
