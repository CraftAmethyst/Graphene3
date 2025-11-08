package me.zcraft.tritiumconfig.config;

import me.zcraft.tritiumconfig.annotation.ClientOnly;
import me.zcraft.tritiumconfig.annotation.Range;
import me.zcraft.tritiumconfig.config.watcher.ConfigFileWatcher;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Tritium configuration system - Inspired by Forge design patterns
 */
public class TritiumConfig {
    private static final Map<String, ConfigValue<?>> configCache = new HashMap<>();
    private static final Map<String, Field> fieldCache = new HashMap<>();
    private static final Object CONFIG_LOCK = new Object();
    private static volatile TritiumConfigBase config = new TritiumConfigBase();
    private static String configFileName = "tritium";
    private static boolean isClient = true;
    private static boolean registered = false;
    private static ConfigParser configParser;
    private static ConfigFileWatcher fileWatcher;

    /**
     * Register the configuration system
     */
    public static synchronized TritiumConfig register() {
        if (registered) {
            TritiumCommon.LOG.warn("Tritium config is already registered!");
            return new TritiumConfig();
        }

        registered = true;
        cacheFieldReflection();
        initializeConfigSystem();
        TritiumCommon.LOG.info("Tritium config registered successfully");
        return new TritiumConfig();
    }

    /**
     * Set the runtime environment
     */
    public static void setEnvironment(boolean client) {
        if (isClient != client) {
            isClient = client;
            // Reload config when environment changes
            reload();
        }
    }

    /**
     * Get the configuration instance (nt: returns cached values)
     */
    public static TritiumConfigBase get() {
        return config;
    }

    /**
     * Reload the configuration from disk
     */
    public static synchronized void reload() {
        TritiumCommon.LOG.info("Reloading Tritium configuration...");

        // Force refresh all cached config values before clearing
        configCache.values().forEach(ConfigValue::refresh);
        configCache.clear();

        if (configParser != null) {
            configParser.load();
        }
        rebuildConfigObject();
        TritiumCommon.LOG.info("Configuration reloaded successfully");
    }

    /**
     * Save the current configuration to file
     */
    public static synchronized void save() {
        try {
            Path configPath = getConfigPath();
            String configContent = generateConfigFile();
            Files.write(configPath, configContent.getBytes());
            TritiumCommon.LOG.info("Configuration saved to: {}", configPath);
            reload();
        } catch (IOException e) {
            TritiumCommon.LOG.error("Failed to save configuration", e);
        }
    }

    /**
     * Stop the configuration system and cleanup resources
     */
    public static void stop() {
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        configCache.clear();
        TritiumCommon.LOG.info("Tritium configuration system stopped");
    }

    private static void initializeConfigSystem() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }
        configParser = new ConfigParser(configPath);
        rebuildConfigObject();

        fileWatcher = new ConfigFileWatcher(configPath, TritiumConfig::reload);
        fileWatcher.start();
    }

    /**
     * Cache Field objects to avoid repeated reflection
     */
    private static void cacheFieldReflection() {
        try {
            for (Field sectionField : TritiumConfigBase.class.getDeclaredFields()) {
                sectionField.setAccessible(true);
                String sectionName = sectionField.getName();
                fieldCache.put("section." + sectionName, sectionField);

                Object tempSection = sectionField.getType().newInstance();
                for (Field field : tempSection.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    fieldCache.put(sectionName + "." + field.getName(), field);
                }
            }
            TritiumCommon.LOG.debug("Cached {} field reflections", fieldCache.size());
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to cache field reflections", e);
        }
    }

    private static void rebuildConfigObject() {
        synchronized (CONFIG_LOCK) {
            try {
                TritiumConfigBase newConfig = new TritiumConfigBase();
                for (Field field : TritiumConfigBase.class.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object section = field.get(newConfig);
                    String sectionName = field.getName();

                    if (field.isAnnotationPresent(ClientOnly.class) && !isClient) {
                        continue;
                    }

                    configureSection(section, sectionName);
                }

                config = newConfig;
                TritiumCommon.LOG.debug("Configuration object rebuilt");
            } catch (Exception e) {
                TritiumCommon.LOG.error("Failed to rebuild configuration object", e);
            }
        }
    }

    private static void configureSection(Object section, String sectionName) {
        if (section == null) return;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(ClientOnly.class) && !isClient) {
                    continue;
                }

                String fieldName = field.getName();
                String configKey = fieldName;
                Class<?> fieldType = field.getType();
                Object defaultValue = getDefaultValue(section, field);
                ConfigValue<?> configValue = getCachedConfigValue(sectionName, configKey, fieldType, defaultValue);
                Object value = configValue.get();
                
                // Apply range validation for numeric types
                if (value instanceof Number && (fieldType == int.class || fieldType == Integer.class || 
                    fieldType == double.class || fieldType == Double.class)) {
                    value = validateRange(field, (Number) value, (Number) defaultValue);
                }
                
                field.set(section, value);
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to configure section: " + sectionName, e);
        }
    }

    private static ConfigValue<?> getCachedConfigValue(String section, String key, Class<?> type, Object defaultValue) {
        String cacheKey = section + "." + key;

        return configCache.computeIfAbsent(cacheKey, k -> {
            if (type == boolean.class || type == Boolean.class) {
                return new ConfigValue<>(configParser.getBoolean(key, (Boolean) defaultValue));
            } else if (type == int.class || type == Integer.class) {
                return new ConfigValue<>(configParser.getInt(key, (Integer) defaultValue));
            } else if (type == double.class || type == Double.class) {
                return new ConfigValue<>(configParser.getDouble(key, (Double) defaultValue));
            } else if (type == String.class) {
                return new ConfigValue<>(configParser.getString(key, (String) defaultValue));
            } else {
                TritiumCommon.LOG.warn("Unsupported configuration type: {} for key: {}", type, key);
                return new ConfigValue<>(() -> defaultValue);
            }
        });
    }

    /**
     * Validate numeric value against Range annotation if present
     */
    private static <T extends Number> T validateRange(Field field, T value, T defaultValue) {
        Range range = field.getAnnotation(Range.class);
        if (range == null) {
            return value;
        }

        double numValue = value.doubleValue();
        double min = range.min();
        double max = range.max();

        if (numValue < min || numValue > max) {
            TritiumCommon.LOG.warn(
                "Config value {} = {} is out of range [{}, {}], using default: {}",
                field.getName(), numValue, min, max, defaultValue
            );
            return defaultValue;
        }

        return value;
    }

    private static Object getDefaultValue(Object section, Field field) {
        try {
            Object defaultSection = field.getDeclaringClass().newInstance();
            return field.get(defaultSection);
        } catch (Exception e) {
            Class<?> type = field.getType();
            if (type == boolean.class) return false;
            if (type == int.class) return 0;
            if (type == double.class) return 0.0;
            if (type == String.class) return "";
            return null;
        }
    }

    private static void createDefaultConfig(Path configPath) {
        try {
            String configContent = generateConfigFile();
            Files.createDirectories(configPath.getParent());
            Files.write(configPath, configContent.getBytes());
            TritiumCommon.LOG.info("Default configuration created at: {}", configPath);
        } catch (IOException e) {
            TritiumCommon.LOG.error("Failed to create default configuration", e);
        }
    }

    private static Path getConfigPath() {
        return Paths.get("config", "tritium", configFileName + ".toml");
    }

    private static String generateConfigFile() {
        StringBuilder sb = new StringBuilder();

        sb.append("# ").append(configFileName).append(" Configuration\n");
        sb.append("# Generated by Tritium\n");
        sb.append("# \n");
        sb.append("# Client-only sections will not be generated on server side\n");
        sb.append("# Edit this file and it will be automatically reloaded\n");
        sb.append("\n");

        try {
            for (Field field : TritiumConfigBase.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object section = field.get(config);
                String sectionName = field.getName();

                // Skip client-only sections on server
                if (field.isAnnotationPresent(ClientOnly.class) && !isClient) {
                    continue;
                }
                // TOML section header with description
                sb.append("\n# ═══════════════════════════════════════════════════════\n");
                sb.append("# ").append(capitalize(sectionName)).append(" Settings\n");
                sb.append("# ═══════════════════════════════════════════════════════\n");
                sb.append("[").append(sectionName).append("]\n");
                generateSectionContent(sb, section, sectionName);
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate configuration content", e);
        }
        return sb.toString();
    }

    private static void generateSectionContent(StringBuilder sb, Object section, String sectionName) {
        if (section == null) return;
        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(ClientOnly.class) && !isClient) {
                    continue;
                }

                String fieldName = field.getName();
                Object value = field.get(section);

                // Add comment with field name description
                sb.append("\n# ").append(formatFieldNameAsComment(fieldName)).append("\n");
                sb.append(fieldName).append(" = ");

                if (value instanceof Boolean) {
                    sb.append(value.toString().toLowerCase());
                } else if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else {
                    sb.append(value);
                }

                sb.append("\n");
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate section content for: " + sectionName, e);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Format camelCase field names into readable comments
     */
    private static String formatFieldNameAsComment(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return fieldName;
        
        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(fieldName.charAt(0)));
        
        for (int i = 1; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(' ').append(c);
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    /**
     * Set the configuration filename (without extension)
     */
    public TritiumConfig filename(String name) {
        configFileName = name;
        // Reinitialize the config system with new filename
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        initializeConfigSystem();
        return this;
    }
}