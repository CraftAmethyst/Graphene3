package me.zcraft.tconfig.config;


import org.craftamethyst.tritium.Tritium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class ConfigParser {
    public final Map<String, String> configValues = new HashMap<>();
    private final Path configPath;
    private long lastLoadTime = 0;
    private String currentSection = "";

    public ConfigParser(Path configPath) {
        this.configPath = configPath;
        load();
    }

    public void load() {
        if (!Files.exists(configPath)) {
            Tritium.LOG.warn("Config file not found: {}", configPath);
            return;
        }

        try {
            configValues.clear();
            currentSection = "";
            Files.lines(configPath).forEach(line -> {
                String trimmed = line.trim();
                if (trimmed.startsWith("#") || trimmed.isEmpty()) {
                    return;
                }

                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    currentSection = trimmed.substring(1, trimmed.length() - 1).trim();
                    return;
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    String fullKey = currentSection.isEmpty() ? key : currentSection + "." + key;
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    configValues.put(fullKey, value);
                }
            });
            lastLoadTime = System.currentTimeMillis();
            Tritium.LOG.debug("Loaded {} config values from: {}", configValues.size(), configPath);
        } catch (IOException e) {
            Tritium.LOG.error("Failed to load config file: {}", configPath, e);
        }
    }

    public Supplier<Boolean> getBoolean(String key, boolean defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            value = value.toLowerCase().trim();
            return value.equals("true") || value.equals("t") || value.equals("1") || value.equals("yes") || value.equals("y");
        };
    }

    public Supplier<Integer> getInt(String key, int defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                Tritium.LOG.warn("Invalid integer value '{}' for key '{}', using default: {}", value, key, defaultValue);
                return defaultValue;
            }
        };
    }

    public Supplier<Long> getLong(String key, long defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                Tritium.LOG.warn("Invalid long value '{}' for key '{}', using default: {}", value, key, defaultValue);
                return defaultValue;
            }
        };
    }

    public Supplier<String> getString(String key, String defaultValue) {
        return () -> {
            String value = configValues.get(key);
            return value != null ? value.trim() : defaultValue;
        };
    }

    public Supplier<Double> getDouble(String key, double defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                Tritium.LOG.warn("Invalid double value '{}' for key '{}', using default: {}", value, key, defaultValue);
                return defaultValue;
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Supplier<Enum> getEnum(String key, Enum defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            try {
                return Enum.valueOf(defaultValue.getClass(), value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                Tritium.LOG.warn("Invalid enum value '{}' for key '{}', using default: {}", value, key, defaultValue);
                return defaultValue;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public Supplier<List<String>> getStringList(String key, List<String> defaultValue) {
        return () -> {
            String value = configValues.get(key);
            if (value == null) return defaultValue;

            try {
                if (value.startsWith("[") && value.endsWith("]")) {
                    String arrayContent = value.substring(1, value.length() - 1).trim();
                    if (arrayContent.isEmpty()) {
                        return new ArrayList<>();
                    }

                    List<String> result = new ArrayList<>();
                    String[] items = arrayContent.split(",");
                    for (String item : items) {
                        String trimmedItem = item.trim();
                        if (trimmedItem.startsWith("\"") && trimmedItem.endsWith("\"")) {
                            trimmedItem = trimmedItem.substring(1, trimmedItem.length() - 1);
                        }
                        result.add(trimmedItem);
                    }
                    return result;
                }
                return Collections.singletonList(value.trim());
            } catch (Exception e) {
                Tritium.LOG.warn("Invalid list value '{}' for key '{}', using default: {}", value, key, defaultValue);
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