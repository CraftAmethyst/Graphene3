package org.craftamethyst.tritium.util;

import com.simibubi.create.Create;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.craftamethyst.tritium.TritiumCommon;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class RailOffloaderHub {
    private static SingleTaskLane worker;
    private static volatile Future<?> currentFuture;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;

        worker = new SingleTaskLane("TritiumRailWorker");
        initialized = true;
        TritiumCommon.LOG.info("Tritium rail offloader initialized");
    }

    public static void shutdown() {
        initialized = false;
        if (worker != null) {
            worker.shutdown();
            worker = null;
        }
    }

    public static void onTickStart(MinecraftServer server) {
        if (!initialized || worker == null) return;

        ServerLevel overworld = server.overworld();

        currentFuture = worker.submit(() -> {
            try {

                Create.RAILWAYS.tick(overworld);
            } catch (Exception e) {
                TritiumCommon.LOG.error("Error in rail offloader tick", e);
            }
        });
    }

    public static void onTickEnd() {
        if (currentFuture == null) return;

        try {
            currentFuture.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            TritiumCommon.LOG.warn("Rail tick took too long, skipping sync");
            currentFuture.cancel(true);
        } finally {
            currentFuture = null;
        }
    }

}