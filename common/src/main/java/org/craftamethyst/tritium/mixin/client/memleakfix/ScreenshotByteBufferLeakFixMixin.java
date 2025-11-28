package org.craftamethyst.tritium.mixin.client.memleakfix;

import com.mojang.blaze3d.platform.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(value = Minecraft.class)
public class ScreenshotByteBufferLeakFixMixin {

    @Unique
    private ByteBuffer tritium$buffer = null;

    @Redirect(
            method = "grabHugeScreenshot",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlUtil;allocateMemory(I)Ljava/nio/ByteBuffer;"
            )
    )
    private ByteBuffer tritium$redirectAllocateMemory(int size) {
        if (!TritiumConfigBase.Fixes.MemoryLeakFix.ScreenshotByteBufferLeakFix) {
            return GlUtil.allocateMemory(size);
        }

        ByteBuffer buffer = GlUtil.allocateMemory(size);
        tritium$buffer = buffer;
        return buffer;
    }

    @Inject(method = "grabHugeScreenshot", at = @At("RETURN"))
    private void tritium$freeOnReturn(CallbackInfoReturnable<Component> cir) {
        if (TritiumConfigBase.Fixes.MemoryLeakFix.ScreenshotByteBufferLeakFix) {
            if (tritium$buffer != null) {
                GlUtil.freeMemory(tritium$buffer);
                tritium$buffer = null;
            }
        }
    }
}