package me.zcraft.tritiumconfig.tritiumconfig.config;

import me.zcraft.tritiumconfig.annotation.ClientOnly;
import me.zcraft.tritiumconfig.annotation.Range;
import me.zcraft.tritiumconfig.annotation.SubCategory;
import me.zcraft.tritiumconfig.config.ConfigParser;
import me.zcraft.tritiumconfig.config.ConfigValue;
import me.zcraft.tritiumconfig.config.watcher.ConfigFileWatcher;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TritiumConfig {
    private static final Map<String, ConfigValue<?>> configCache = new ConcurrentHashMap<>();
    private static final Map<String, FieldAccessor> fieldAccessors = new ConcurrentHashMap<>();
    private static final Object CONFIG_LOCK = new Object();
    private static volatile TritiumConfigBase config = new TritiumConfigBase();
    private static String configFileName = "tritium";
    private static boolean isClient = true;
    private static boolean registered = false;
    private static me.zcraft.tritiumconfig.config.ConfigParser configParser;
    private static ConfigFileWatcher fileWatcher;

    public static synchronized TritiumConfig register() {
        if (registered) {
            TritiumCommon.LOG.warn("Tritium config is already registered!");
            return new TritiumConfig();
        }

        registered = true;

        try {
            ConfigValidator.validateConfig(new TritiumConfigBase());
            TritiumCommon.LOG.info("Default configuration validation passed");
        } catch (Exception e) {
            TritiumCommon.LOG.error("Default configuration validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid default configuration", e);
        }

        cacheFieldAccessors();
        initializeConfigSystem();
        TritiumCommon.LOG.info("Tritium config registered successfully");
        return new TritiumConfig();
    }

    public static synchronized void reload() {
        configCache.values().forEach(ConfigValue::refresh);
        configCache.clear();

        if (configParser != null) {
            configParser.load();
            ConfigMigration.migrateConfig(getConfigPath(), configParser);
        }
        rebuildConfigObject();
    }

    public static void setEnvironment(boolean client) {
        if (isClient != client) {
            isClient = client;
            reload();
        }
    }

    public static TritiumConfigBase get() {
        return config;
    }

    public static void stop() {
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        configCache.clear();
        fieldAccessors.clear();
    }

    public static synchronized void save() {
        try {
            Path configPath = getConfigPath();
            String configContent = generateConfigFile();
            Files.write(configPath, configContent.getBytes());
        } catch (IOException e) {
            TritiumCommon.LOG.error("Failed to save configuration", e);
        }
    }

    private static void cacheFieldAccessors() {
        try {
            cacheFieldsRecursive(TritiumConfigBase.class, "");
            TritiumCommon.LOG.debug("Cached {} field accessors", fieldAccessors.size());
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to cache field accessors", e);
        }
    }

    private static void initializeConfigSystem() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }
        configParser = new ConfigParser(configPath);

        ConfigMigration.migrateConfig(configPath, configParser);
        rebuildConfigObject();

        fileWatcher = new ConfigFileWatcher(configPath, TritiumConfig::reload);
        fileWatcher.start();
    }

    private static void cacheFieldsRecursive(Class<?> clazz, String prefix) throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fullPath = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();

            if (!field.isAnnotationPresent(SubCategory.class)) {
                MethodHandle getter = lookup.unreflectGetter(field);
                MethodHandle setter = lookup.unreflectSetter(field);

                fieldAccessors.put(fullPath, new MethodHandleFieldAccessor(
                        getter, setter, field.getType(), () -> {
                    try {
                        Object defaultInstance = field.getDeclaringClass().newInstance();
                        return field.get(defaultInstance);
                    } catch (Exception e) {
                        return getTypeDefaultValue(field.getType());
                    }
                }
                ));
            }

            if (field.isAnnotationPresent(SubCategory.class)) {
                cacheFieldsRecursive(field.getType(), fullPath);
            }
        }
    }

    private static void configureObjectRecursive(Object obj, String prefix) throws Exception {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(ClientOnly.class) && !isClient) {
                continue;
            }

            String fieldPath = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();

            if (field.isAnnotationPresent(SubCategory.class)) {
                Object subObj = field.getType().newInstance();
                configureObjectRecursive(subObj, fieldPath);
                field.set(obj, subObj);
            } else {
                if (isSimpleType(field.getType())) {
                    FieldAccessor accessor = fieldAccessors.get(fieldPath);
                    if (accessor != null) {
                        Object defaultValue = accessor.getDefaultValue();
                        ConfigValue<?> configValue = getCachedConfigValue(fieldPath, field.getType(), defaultValue);
                        Object value = configValue.get();

                        if (value instanceof Number) {
                            value = validateRange(field, (Number) value, (Number) defaultValue);
                        }

                        accessor.setValue(obj, value);
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ConfigValue<?> getCachedConfigValue(String key, Class<?> type, Object defaultValue) {
        return configCache.computeIfAbsent(key, k -> createConfigValueSupplier(key, type, defaultValue));
    }

    private static ConfigValue<?> createConfigValueSupplier(String key, Class<?> type, Object defaultValue) {
        if (type == boolean.class || type == Boolean.class) {
            return new ConfigValue<>(configParser.getBoolean(key, (Boolean) defaultValue));
        } else if (type == int.class || type == Integer.class) {
            return new ConfigValue<>(configParser.getInt(key, (Integer) defaultValue));
        } else if (type == long.class || type == Long.class) {
            return new ConfigValue<>(configParser.getLong(key, (Long) defaultValue));
        } else if (type == double.class || type == Double.class) {
            return new ConfigValue<>(configParser.getDouble(key, (Double) defaultValue));
        } else if (type == String.class) {
            return new ConfigValue<>(configParser.getString(key, (String) defaultValue));
        } else if (type.isEnum()) {
            return new ConfigValue<>(configParser.getEnum(key, (Enum) defaultValue));
        } else if (List.class.isAssignableFrom(type)) {
            return new ConfigValue<>(configParser.getStringList(key, (List<String>) defaultValue));
        } else {
            TritiumCommon.LOG.warn("Unsupported configuration type: {} for key: {}", type, key);
            return new ConfigValue<>(() -> defaultValue);
        }
    }

    private static void rebuildConfigObject() {
        synchronized (CONFIG_LOCK) {
            try {
                TritiumConfigBase newConfig = new TritiumConfigBase();
                configureObjectRecursive(newConfig, "");
                config = newConfig;
                ConfigValidator.validateConfig(config);
                TritiumCommon.LOG.debug("Configuration object rebuilt and validated");
            } catch (Exception e) {
                TritiumCommon.LOG.error("Failed to rebuild configuration object", e);
            }
        }
    }

    private static Object getTypeDefaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == String.class) return "";
        return null;
    }

    private interface FieldAccessor {
        Object getValue(Object obj) throws Exception;
        void setValue(Object obj, Object value) throws Exception;
        Object getDefaultValue() throws Exception;
        Class<?> getType();
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

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

    private static void generateFlattenedSectionContent(StringBuilder sb, Object section, String indent) throws Exception {
        if (section == null) return;

        for (Field field : section.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            String fieldName = field.getName();
            Object value = field.get(section);

            if (field.isAnnotationPresent(SubCategory.class)) {
                SubCategory subCat = field.getAnnotation(SubCategory.class);
                sb.append(indent).append("#").append("-".repeat(25)).append("\n");
                sb.append(indent).append("# ").append(subCat.value()).append("\n");
                sb.append(indent).append("#").append("-".repeat(25)).append("\n\n");
                generateFlattenedSectionContent(sb, value, indent);
            } else {
                if (isSimpleType(field.getType())) {
                    sb.append(indent).append("## ").append(formatFieldNameAsComment(fieldName)).append("\n");
                    sb.append(indent).append(fieldName).append(" = ");

                    if (value instanceof Boolean) {
                        sb.append(value.toString().toLowerCase());
                    } else if (value instanceof String) {
                        sb.append("\"").append(value).append("\"");
                    } else if (value instanceof Enum) {
                        sb.append("\"").append(((Enum<?>) value).name()).append("\"");
                    } else if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> list = (List<String>) value;
                        sb.append("[");
                        for (int i = 0; i < list.size(); i++) {
                            if (i > 0) sb.append(", ");
                            sb.append("\"").append(list.get(i)).append("\"");
                        }
                        sb.append("]");
                    } else if (value instanceof Long) {
                        sb.append(value);
                    } else {
                        sb.append(value);
                    }
                    sb.append("\n\n");
                }
            }
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

        sb.append("# Tritium Configuration\n");
        sb.append("# Generated by TritiumConfig\n");
        sb.append("# \n");
        sb.append("# Client-only sections will not be generated on server side\n");
        sb.append("# Edit this file and it will be automatically reloaded\n");
        sb.append("\n");

        try {
            TritiumConfigBase configObj = config;
            for (Field sectionField : TritiumConfigBase.class.getDeclaredFields()) {
                sectionField.setAccessible(true);

                if (sectionField.isAnnotationPresent(ClientOnly.class) && !isClient) {
                    continue;
                }

                Object section = sectionField.get(configObj);
                String sectionName = sectionField.getName();
                sb.append("[").append(sectionName).append("]\n");
                generateFlattenedSectionContent(sb, section, "");
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate configuration content", e);
        }
        return sb.toString();
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type == Boolean.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Double.class ||
                type == String.class ||
                type.isEnum() ||
                type == List.class;
    }

    private static class MethodHandleFieldAccessor implements FieldAccessor {
        private final MethodHandle getter;
        private final MethodHandle setter;
        private final Class<?> type;
        private final SupplierWithException<Object> defaultValueSupplier;

        public MethodHandleFieldAccessor(MethodHandle getter, MethodHandle setter,
                                         Class<?> type, SupplierWithException<Object> defaultValueSupplier) {
            this.getter = getter;
            this.setter = setter;
            this.type = type;
            this.defaultValueSupplier = defaultValueSupplier;
        }

        @Override
        public Object getValue(Object obj) throws Exception {
            try {
                return getter.invoke(obj);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setValue(Object obj, Object value) throws Exception {
            try {
                setter.invoke(obj, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object getDefaultValue() throws Exception {
            return defaultValueSupplier.get();
        }

        @Override
        public Class<?> getType() {
            return type;
        }
    }

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

    public TritiumConfig filename(String name) {
        configFileName = name;
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        initializeConfigSystem();
        return this;
    }
}