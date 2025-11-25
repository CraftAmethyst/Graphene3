package org.craftamethyst.tritium.gpu;

import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class GpuPlus {

    private static final Queue<FramebufferFixer> FIXERS = new ConcurrentLinkedQueue<>();

    private GpuPlus() {
    }

    public static void enqueue(FramebufferFixer fixer) {
        if (fixer != null && TritiumConfigBase.Rendering.GpuPlus.gpuPlus) {
            FIXERS.add(fixer);
        }
    }

    public static void processQueue() {
        if (!TritiumConfigBase.Rendering.GpuPlus.gpuPlus) {
            return;
        }

        boolean destroyed = false;
        int counter = 0;

        while (!FIXERS.isEmpty() && counter++ < 20) {
            FramebufferFixer fixer = FIXERS.poll();
            if (fixer == null) {
                continue;
            }

            try {
                if (!destroyed) {
                    fixer.tritium$destroy();
                    destroyed = true;
                }
                fixer.tritium$release();
            } catch (Exception e) {
                TritiumCommon.LOG.error("[Tritium-GPU PLUS]Failed to process framebuffer cleanup for {}", fixer, e);
            }
        }
    }

}
