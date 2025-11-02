package me.zcraft.tritiumconfig.config;

import java.util.function.Supplier;

public class ConfigValue<T> implements Supplier<T> {
    private final Supplier<T> valueSupplier;
    private final long cacheDuration;
    private T cachedValue;
    private long lastUpdateTime;

    public ConfigValue(Supplier<T> valueSupplier) {
        this(valueSupplier, 5000);
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
            cachedValue = valueSupplier.get();
            lastUpdateTime = currentTime;
        }
        return cachedValue;
    }

    /**
     * refresh cache
     */
    public void refresh() {
        cachedValue = valueSupplier.get();
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Retrieve raw values (without caching)
     */
    public T getRaw() {
        return valueSupplier.get();
    }
}