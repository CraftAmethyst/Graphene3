package org.craftamethyst.tritium.mixin.client.memleakfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(value = Minecraft.class)
public class ScreenshotByteBufferLeakFixMixin {

    @Unique
    private ByteBuffer tritium$buffer = null;

    @WrapOperation(
            method = "grabHugeScreenshot",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlUtil;allocateMemory(I)Ljava/nio/ByteBuffer;"
            )
    )
    private ByteBuffer tritium$wrapAllocateMemory(int size, Operation<ByteBuffer> original) {
        if (!TritiumConfigBase.Fixes.MemoryLeakFix.ScreenshotByteBufferLeakFix) {
            return original.call(size);
        }

        ByteBuffer buffer = original.call(size);
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