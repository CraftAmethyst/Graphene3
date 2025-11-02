package me.zcraft.tritiumconfig.config;

import org.craftamethyst.tritium.TritiumCommon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigParser {
    private final Path configPath;
    private final Map<String, String> configValues = new HashMap<>();
    private long lastLoadTime = 0;

    public ConfigParser(Path configPath) {
        this.configPath = configPath;
        load();
    }

    public void load() {
        if (!Files.exists(configPath)) {
            TritiumCommon.LOG.warn("Config file not found: {}", configPath);
            return;
        }

        try {
            configValues.clear();
            Files.lines(configPath)
                    .filter(line -> !line.trim().startsWith("#") && !line.trim().isEmpty())
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            configValues.put(key, value);
                        }
                    });
            lastLoadTime = System.currentTimeMillis();
            TritiumCommon.LOG.debug("Loaded {} config values from: {}", configValues.size(), configPath);
        } catch (IOException e) {
            TritiumCommon.LOG.error("Failed to load config file: {}", configPath, e);
        }
    }

    public Supplier<Boolean> getBoolean(String key, boolean defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            value = value.toLowerCase().trim();
            if (value.equals("true") || value.equals("t") || value.equals("1") || value.equals("yes") || value.equals("y")) {
                return true;
            } else if (value.equals("false") || value.equals("f") || value.equals("0") || value.equals("no") || value.equals("n")) {
                return false;
            } else {
                TritiumCommon.LOG.warn("Invalid boolean value '{}' for key '{}', using default: {}", value, key, defaultValue);
                return defaultValue;
            }
        };
    }

    public Supplier<Integer> getInt(String key, int defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                TritiumCommon.LOG.warn("Invalid integer value '{}' for key '{}', using default: {}", value, key, defaultValue);
                return defaultValue;
            }
        };
    }

    public Supplier<String> getString(String key, String defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            value = value.trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value;
        };
    }

    public Supplier<Double> getDouble(String key, double defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                TritiumCommon.LOG.warn("Invalid double value '{}' for key '{}', using default: {}", value, key, defaultValue);
                return defaultValue;
            }
        };
    }

    public long getLastLoadTime() {
        return lastLoadTime;
    }

    public boolean hasKey(String key) {
        return configValues.containsKey(key);
    }
}