package org.craftamethyst.tritium.mixin.client.renderer.reflex;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.lwjgl.opengl.GL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL33.*;

@Mixin(GameRenderer.class)
public abstract class ReflexSchedulerMixin {

    @Unique
    private static final int MODE_DISABLED = 0;
    @Unique
    private static final int MODE_TIMESTAMP = 1;
    @Unique
    private static final int MODE_ELAPSED = 2;
    @Unique
    private static final Logger tritium$LOGGER = LogManager.getLogger("Tritium-Reflex");
    @Unique
    private static final long MAX_WAIT_NS = 2_000_000L;
    @Unique
    private static final long MIN_FRAME_NS = 1_000_000L;
    @Unique
    private static final double SMOOTH_ALPHA = 0.15;
    @Unique
    private final int[] tritium$queryIds = new int[2];
    @Unique
    private int tritium$timingMode = MODE_DISABLED;
    @Unique
    private int tritium$queryIndex = 0;

    @Unique
    private long tritium$lastGpuDoneNs = -1L;
    @Unique
    private long tritium$lastFrameEndNs = -1L;
    @Unique
    private double tritium$smoothedDeltaNs = 0.0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reflex$init(Minecraft p_234219_, ItemInHandRenderer p_234220_, ResourceManager p_234221_, RenderBuffers p_234222_, CallbackInfo ci) {

        if (GL.getCapabilities().GL_ARB_timer_query) {
            tritium$timingMode = MODE_TIMESTAMP;
            glGenQueries(tritium$queryIds);
            tritium$LOGGER.info("Using high-precision timestamp queries");
        } else if (GL.getCapabilities().GL_EXT_timer_query ||
                GL.getCapabilities().GL_ARB_occlusion_query) {
            tritium$timingMode = MODE_ELAPSED;
            glGenQueries(tritium$queryIds);
            tritium$LOGGER.info("Using elapsed time queries (compatibility mode)");
        } else {
            tritium$LOGGER.warn("No supported GPU timing method available, Reflex disabled");
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void reflex$onCpuStart(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        if (!TritiumConfigBase.Rendering.Reflex.enableReflex || tritium$timingMode == MODE_DISABLED) return;

        final long cpuNow = System.nanoTime();


        long gpuDone = -1;
        gpuDone = switch (tritium$timingMode) {
            case MODE_TIMESTAMP -> tritium$getGpuTimestamp(cpuNow);
            case MODE_ELAPSED -> tritium$getGpuElapsedTime();
            default -> gpuDone;
        };

        if (gpuDone > 0 && gpuDone < cpuNow) {
            tritium$lastGpuDoneNs = gpuDone;
            long cpuElapsed = cpuNow - tritium$lastGpuDoneNs;
            tritium$smoothedDeltaNs = SMOOTH_ALPHA * cpuElapsed + (1.0 - SMOOTH_ALPHA) * tritium$smoothedDeltaNs;

            long waitNs = (long) (tritium$smoothedDeltaNs + TritiumConfigBase.Rendering.Reflex.reflexOffsetNs);
            waitNs = Math.max(-MAX_WAIT_NS, Math.min(MAX_WAIT_NS, waitNs));

            if (waitNs > 0) {
                tritium$smartWait(cpuNow, waitNs);
            }
        }

        int maxFps = TritiumConfigBase.Rendering.Reflex.MAX_FPS;
        if (maxFps > 0 && tritium$lastFrameEndNs > 0) {
            long targetFrameTime = 1_000_000_000L / maxFps;
            long elapsed = cpuNow - tritium$lastFrameEndNs;
            long remaining = targetFrameTime - elapsed;

            if (remaining > MIN_FRAME_NS) {
                tritium$smartWait(cpuNow, remaining);
            }
        }

        if (TritiumConfigBase.Rendering.Reflex.reflexDebug) {
            tritium$LOGGER.debug("Reflex stats - Mode: {}, GPU: {}ns, CPU: {}ns, Delta: {}ns",
                    tritium$timingModeToString(), tritium$lastGpuDoneNs, tritium$lastFrameEndNs, tritium$smoothedDeltaNs);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void reflex$onCpuEnd(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        if (tritium$timingMode == MODE_DISABLED || !TritiumConfigBase.Rendering.Reflex.enableReflex) return;

        switch (tritium$timingMode) {
            case MODE_TIMESTAMP:
                glQueryCounter(tritium$queryIds[tritium$queryIndex], GL_TIMESTAMP);
                break;
            case MODE_ELAPSED:
                glBeginQuery(GL_TIME_ELAPSED, tritium$queryIds[tritium$queryIndex]);
                glEndQuery(GL_TIME_ELAPSED);
                break;
        }
        tritium$queryIndex ^= 1;
        tritium$lastFrameEndNs = System.nanoTime();
    }

    @Unique
    private long tritium$getGpuTimestamp(long cpuNow) {
        int prev = tritium$queryIndex ^ 1;
        if (!glIsQuery(tritium$queryIds[prev])) return -1;

        int[] ready = {0};
        glGetQueryObjectiv(tritium$queryIds[prev], GL_QUERY_RESULT_AVAILABLE, ready);
        if (ready[0] == 0) return -1;

        long gpuTime = glGetQueryObjecti64(tritium$queryIds[prev], GL_QUERY_RESULT);
        return (gpuTime > 0 && gpuTime < cpuNow) ? gpuTime : -1;
    }

    @Unique
    private long tritium$getGpuElapsedTime() {
        int prev = tritium$queryIndex ^ 1;
        if (!glIsQuery(tritium$queryIds[prev])) return -1;

        int[] ready = {0};
        glGetQueryObjectiv(tritium$queryIds[prev], GL_QUERY_RESULT_AVAILABLE, ready);
        if (ready[0] == 0) return -1;

        int[] timeNs = {0};
        glGetQueryObjectiv(tritium$queryIds[prev], GL_QUERY_RESULT, timeNs);
        return (tritium$lastFrameEndNs > 0) ? tritium$lastFrameEndNs + timeNs[0] * 1_000_000L : -1;
    }

    @Unique
    private void tritium$smartWait(long startTime, long waitNs) {
        long endTime = startTime + waitNs;

        while (System.nanoTime() < endTime - 100_000L) {
            Thread.onSpinWait();
        }

        while (System.nanoTime() < endTime) {
            try {
                Thread.sleep(0, 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Unique
    private String tritium$timingModeToString() {
        return switch (tritium$timingMode) {
            case MODE_TIMESTAMP -> "TIMESTAMP";
            case MODE_ELAPSED -> "ELAPSED";
            default -> "DISABLED";
        };
    }
}