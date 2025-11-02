package me.zcraft.tritiumconfig.config;

import me.zcraft.tritiumconfig.annotation.ClientOnly;
import org.craftamethyst.tritium.TritiumCommon;

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
    private static TritiumConfigBase config = new TritiumConfigBase();
    private static String configFileName = "tritium";
    private static boolean isClient = true;
    private static boolean registered = false;
    private static ConfigParser configParser;
    private static ConfigFileWatcher fileWatcher;

    /**
     * Register the configuration system
     */
    public static TritiumConfig register() {
        if (registered) {
            TritiumCommon.LOG.warn("Tritium config is already registered!");
            return new TritiumConfig();
        }

        registered = true;
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
    public static void reload() {
        TritiumCommon.LOG.info("Reloading Tritium configuration...");

        // Clear any cached values
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
    public static void save() {
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

    private static void rebuildConfigObject() {
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
                ConfigValue<?> configValue = getCachedConfigValue(sectionName, configKey, fieldType, getDefaultValue(section, field));
                Object value = configValue.get();
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
                // Section header
                sb.append("\n# ").append(capitalize(sectionName)).append("\n");
                generateSectionContent(sb, section, sectionName);
                sb.append("\n");
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