package me.zcraft.tconfig.config;

import me.zcraft.tconfig.annotation.ConfigVersion;
import org.craftamethyst.tritium.TritiumCommon;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigMigration {
    public static boolean migrateConfig(Path configPath, ConfigParser parser, Class<?> configClass) {
        try {
            int currentVersion = getCurrentConfigVersion(configClass);
            int fileVersion = detectConfigVersion(parser);

            if (fileVersion < currentVersion) {
                TritiumCommon.LOG.info("Migrating config from version {} to {}");
                return !performMigration(configPath, parser, fileVersion, currentVersion, configClass);
            }
            return false;
        } catch (Exception e) {
            TritiumCommon.LOG.error("Config migration failed: {}");
            return true;
        }
    }

    private static int getCurrentConfigVersion(Class<?> configClass) {
        try {
            ConfigVersion version = configClass.getAnnotation(ConfigVersion.class);
            return version != null ? version.value() : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private static int detectConfigVersion(ConfigParser parser) {
        if (parser.hasKey("config_version")) {
            return parser.getInt("config_version", 1).get();
        }
        return 1;
    }

    private static boolean performMigration(Path configPath, ConfigParser parser, int fromVersion, int toVersion, Class<?> configClass) {
        Map<String, String> migratedValues = new HashMap<>(parser.configValues);

        try {
            for (int version = fromVersion; version < toVersion; version++) {
                if (!migrateFromVersion(migratedValues, version, configClass)) {
                    return false;
                }
            }

            migratedValues.put("config_version", String.valueOf(toVersion));
            StringBuilder newConfig = new StringBuilder();
            newConfig.append("# Configuration\n");
            newConfig.append("# Migrated from version ").append(fromVersion).append(" to ").append(toVersion).append("\n");
            newConfig.append("config_version = ").append(toVersion).append("\n\n");

            for (Map.Entry<String, String> entry : migratedValues.entrySet()) {
                if (!entry.getKey().equals("config_version")) {
                    newConfig.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
                }
            }

            Files.write(configPath, newConfig.toString().getBytes());
            TritiumCommon.LOG.info("Config migration completed successfully");
            return true;
        } catch (Exception e) {
            TritiumCommon.LOG.error("Migration failed: {}");
            return false;
        }
    }

    private static boolean migrateFromVersion(Map<String, String> values, int fromVersion, Class<?> configClass) {
        try {
            String methodName = "migrateFromV" + fromVersion;
            Method migrationMethod = configClass.getMethod(methodName, Map.class);
            migrationMethod.invoke(null, values);
            TritiumCommon.LOG.debug("Applied migration from version {}");
            return true;
        } catch (NoSuchMethodException e) {
            return applyDefaultMigration(values, fromVersion);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to apply custom migration for version {}: {}");
            return false;
        }
    }

    private static boolean applyDefaultMigration(Map<String, String> values, int fromVersion) {
        try {
            switch (fromVersion) {
                case 1:
                    migrateV1toV2(values);
                    break;
                case 2:
                    migrateV2toV3(values);
                    break;
            }
            return true;
        } catch (Exception e) {
            TritiumCommon.LOG.error("Default migration failed for version {}: {}");
            return false;
        }
    }

    private static void migrateV1toV2(Map<String, String> values) {
        Map<String, String> migrations = new HashMap<String, String>() {{
            put("rendering.enableCulling", "rendering.entityCulling.enableCulling");
            put("rendering.enableEntityCulling", "rendering.entityCulling.enableEntityCulling");
        }};

        applyMigrations(values, migrations);
    }

    private static void migrateV2toV3(Map<String, String> values) {
        Map<String, String> migrations = new HashMap<String, String>() {{
            put("old.setting", "new.setting");
        }};

        applyMigrations(values, migrations);
    }

    private static void applyMigrations(Map<String, String> values, Map<String, String> migrations) {
        for (Map.Entry<String, String> migration : migrations.entrySet()) {
            String oldKey = migration.getKey();
            String newKey = migration.getValue();

            if (values.containsKey(oldKey) && !values.containsKey(newKey)) {
                values.put(newKey, values.get(oldKey));
                values.remove(oldKey);
                TritiumCommon.LOG.debug("Migrated config key: {} -> {}");
            }
        }
    }
}