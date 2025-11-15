package org.craftamethyst.tritium.gpu;

public interface FramebufferFixer {

    /**
     * Called once before processing a batch of fixers.
     * Implementations should reset global GL state that might interfere with cleanup.
     */
    void destroy();

    /**
     * Release all GPU resources associated with this framebuffer instance.
     */
    void release();
}
