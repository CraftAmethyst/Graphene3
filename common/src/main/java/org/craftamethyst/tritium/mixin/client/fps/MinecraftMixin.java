package org.craftamethyst.tritium.mixin.client.fps;

import com.mojang.blaze3d.platform.Window;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/11/9 上午7:14
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public abstract boolean isWindowActive();

    @Shadow @Final private Window window;

    @Inject(method = "<init>",at = @At(value = "TAIL"))
    private void init(GameConfig pGameConfig, CallbackInfo ci){
        GLFW.glfwSetWindowIconifyCallback(this.window.getWindow(), (window, iconified) -> {
            if(TritiumConfig.get().clientOptimizations.dynamicFPS.enable)Minecraft.getInstance().noRender = iconified;
        });
    }

    @Inject(method = "getFramerateLimit",at = @At("RETURN"),cancellable = true)
    public void framerateLimit(CallbackInfoReturnable<Integer> cir){
        if(!this.isWindowActive() && TritiumConfig.get().clientOptimizations.dynamicFPS.enable){
            cir.setReturnValue(TritiumConfig.get().clientOptimizations.dynamicFPS.minimizedFPS);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tritium$gpuPlusTick(CallbackInfo ci) {
        org.craftamethyst.tritium.gpu.GpuPlus.processQueue();
    }
}
