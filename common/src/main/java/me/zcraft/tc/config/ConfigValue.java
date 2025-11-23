package me.zcraft.tc.config;

import java.util.function.Supplier;

public class ConfigValue<T> implements Supplier<T> {
    private final Supplier<T> valueSupplier;
    private final long cacheDuration;
    private final Object lock = new Object();
    private T cachedValue;
    private long lastUpdateTime;

    public ConfigValue(Supplier<T> valueSupplier) {
        this(valueSupplier, 3000);
    }

    public ConfigValue(Supplier<T> valueSupplier, long cacheDurationMs) {
        this.valueSupplier = valueSupplier;
        this.cacheDuration = cacheDurationMs;
        this.cachedValue = valueSupplier.get();
        this.lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public T get() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > cacheDuration) {
            synchronized (lock) {
                if (currentTime - lastUpdateTime > cacheDuration) {
                    cachedValue = valueSupplier.get();
                    lastUpdateTime = currentTime;
                }
            }
        }
        return cachedValue;
    }

    public void refresh() {
        synchronized (lock) {
            cachedValue = valueSupplier.get();
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    public T getRaw() {
        return valueSupplier.get();
    }

    public long getCacheAge() {
        return System.currentTimeMillis() - lastUpdateTime;
    }
}