package org.craftamethyst.tritium.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class SingleTaskLane {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    SingleTaskLane(String name) {
        thread = new Thread(this::loop, name);
        thread.setDaemon(true);
        thread.start();
    }

    Future<?> submit(Runnable r) {
        if (!running.get()) return CompletableFuture.completedFuture(null);
        CompletableFuture<Void> f = new CompletableFuture<>();
        queue.add(() -> {
            try {
                r.run();
                f.complete(null);
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
        return f;
    }

    void shutdown() {
        running.set(false);
        thread.interrupt();
    }

    private void loop() {
        while (running.get()) {
            try {
                Runnable task = queue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) task.run();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}