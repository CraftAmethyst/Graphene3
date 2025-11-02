package org.craftamethyst.tritium.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.craftamethyst.tritium.Constants;
import org.craftamethyst.tritium.platform.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(MinecraftServer.class)
public abstract class AsyncWorldSaveMixin {
    
    private static final ExecutorService SAVE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Tritium-Async-World-Save");
        thread.setDaemon(true);
        return thread;
    });

    @Shadow
    public abstract Iterable<ServerLevel> getAllLevels();

    @Shadow
    private volatile boolean running;

    @Inject(
        method = "saveAllChunks(ZZZ)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSaveAllChunks(boolean suppressLogs, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        if (!Services.CONFIG.get().serverPerformance.asyncWorldSave) {
            return;
        }
        
        if (!this.running && forced) {
            Constants.LOG.info("Using async world save on server shutdown");
            
            CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
                try {
                    for (ServerLevel level : this.getAllLevels()) {
                        Constants.LOG.info("Async saving world: {}", level.dimension().location());
                        level.save(null, flush, level.noSave);
                    }
                    Constants.LOG.info("Async world save completed");
                } catch (Exception e) {
                    Constants.LOG.error("Error during async world save", e);
                }
            }, SAVE_EXECUTOR);

            int timeout = Services.CONFIG.get().serverPerformance.asyncWorldSaveTimeoutSeconds;
            try {
                saveFuture.get(timeout, java.util.concurrent.TimeUnit.SECONDS);
                cir.setReturnValue(true);
            } catch (Exception e) {
                Constants.LOG.error("Async world save timeout or error", e);
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "stopServer()V", at = @At("HEAD"))
    private void onStopServer(CallbackInfo ci) {
        Constants.LOG.info("Server stopping, preparing async world save");
    }

    @Inject(method = "stopServer()V", at = @At("TAIL"))
    private void afterStopServer(CallbackInfo ci) {
        SAVE_EXECUTOR.shutdown();
        try {
            if (!SAVE_EXECUTOR.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                SAVE_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            SAVE_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
