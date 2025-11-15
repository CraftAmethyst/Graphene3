package me.zcraft.tritiumconfig.config;

import me.zcraft.tritiumconfig.annotation.ConfigVersion;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigMigration {
    private static final int CURRENT_VERSION = getCurrentConfigVersion();

    public static void migrateConfig(Path configPath, ConfigParser parser) {
        try {
            int fileVersion = detectConfigVersion(parser);

            if (fileVersion < CURRENT_VERSION) {
                TritiumCommon.LOG.info("Migrating config from version {} to {}", fileVersion, CURRENT_VERSION);
                performMigration(configPath, parser, fileVersion);
            }
        } catch (Exception e) {
            TritiumCommon.LOG.warn("Config migration failed: {}", e.getMessage());
        }
    }

    private static int getCurrentConfigVersion() {
        try {
            ConfigVersion version = TritiumConfigBase.class.getAnnotation(ConfigVersion.class);
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

    private static void performMigration(Path configPath, ConfigParser parser, int fromVersion) throws IOException {
        Map<String, String> migratedValues = new HashMap<>(parser.configValues);
        for (int version = fromVersion; version < CURRENT_VERSION; version++) {
            migrateFromVersion(migratedValues, version);
        }

        migratedValues.put("config_version", String.valueOf(CURRENT_VERSION));
        StringBuilder newConfig = new StringBuilder();
        newConfig.append("# Tritium Configuration\n");
        newConfig.append("# Migrated from version ").append(fromVersion).append(" to ").append(CURRENT_VERSION).append("\n");
        newConfig.append("config_version = ").append(CURRENT_VERSION).append("\n\n");

        for (Map.Entry<String, String> entry : migratedValues.entrySet()) {
            if (!entry.getKey().equals("config_version")) {
                newConfig.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
        }

        Files.write(configPath, newConfig.toString().getBytes());
        TritiumCommon.LOG.info("Config migration completed successfully");
    }

    private static void migrateFromVersion(Map<String, String> values, int fromVersion) {
        switch (fromVersion) {
            case 1:
                migrateV1toV2(values);
                break;
        }
    }

    private static void migrateV1toV2(Map<String, String> values) {
        Map<String, String> migrations = Map.of(
                "rendering.enableCulling", "rendering.entityCulling.enableCulling",
                "rendering.enableEntityCulling", "rendering.entityCulling.enableEntityCulling"
        );

        for (Map.Entry<String, String> migration : migrations.entrySet()) {
            String oldKey = migration.getKey();
            String newKey = migration.getValue();

            if (values.containsKey(oldKey) && !values.containsKey(newKey)) {
                values.put(newKey, values.get(oldKey));
                TritiumCommon.LOG.debug("Migrated config key: {} -> {}", oldKey, newKey);
            }
        }
    }
}