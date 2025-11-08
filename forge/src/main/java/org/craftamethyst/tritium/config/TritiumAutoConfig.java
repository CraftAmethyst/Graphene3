package org.craftamethyst.tritium.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.zcraft.tritiumconfig.annotation.Range;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.TritiumCommon;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public class TritiumAutoConfig {

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.tritium.title"))
                .transparentBackground()
                .setSavingRunnable(TritiumAutoConfig::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        try {
            TritiumConfigBase config = TritiumConfig.get();
            for (Field sectionField : TritiumConfigBase.class.getDeclaredFields()) {
                sectionField.setAccessible(true);
                Object section = sectionField.get(config);
                String sectionName = sectionField.getName();
                if (hasConfigurableFields(section)) {
                    ConfigCategory category = builder.getOrCreateCategory(
                            Component.translatable("config.tritium.category." + sectionName)
                    );

                    generateSectionEntries(entryBuilder, category, section, sectionName);
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate config screen", e);
        }

        return builder.build();
    }

    private static void generateSectionEntries(ConfigEntryBuilder entryBuilder,
                                               ConfigCategory category,
                                               Object section,
                                               String sectionName) {
        if (section == null) return;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                Object currentValue = field.get(section);

                String translationKey = "config.tritium." + sectionName + "." + fieldName;

                if (fieldType == boolean.class || fieldType == Boolean.class) {
                    category.addEntry(entryBuilder.startBooleanToggle(
                                    Component.translatable(translationKey),
                                    (Boolean) currentValue
                            )
                            .setDefaultValue(getDefaultBooleanValue(field))
                            .setTooltip(Component.translatable(translationKey + ".tooltip"))
                            .setSaveConsumer(createBooleanSaveConsumer(sectionName, fieldName))
                            .build());

                } else if (fieldType == int.class || fieldType == Integer.class) {
                    var intField = entryBuilder.startIntField(
                                    Component.translatable(translationKey),
                                    (Integer) currentValue
                            )
                            .setDefaultValue(getDefaultIntValue(field))
                            .setTooltip(Component.translatable(translationKey + ".tooltip"))
                            .setSaveConsumer(createIntSaveConsumer(sectionName, fieldName));

                    // Apply range constraints if present
                    Range range = field.getAnnotation(Range.class);
                    if (range != null) {
                        intField.setMin((int) range.min()).setMax((int) range.max());
                    }

                    category.addEntry(intField.build());

                } else if (fieldType == double.class || fieldType == Double.class) {
                    var doubleField = entryBuilder.startDoubleField(
                                    Component.translatable(translationKey),
                                    (Double) currentValue
                            )
                            .setDefaultValue(getDefaultDoubleValue(field))
                            .setTooltip(Component.translatable(translationKey + ".tooltip"))
                            .setSaveConsumer(createDoubleSaveConsumer(sectionName, fieldName));

                    // Apply range constraints if present
                    Range range = field.getAnnotation(Range.class);
                    if (range != null) {
                        doubleField.setMin(range.min()).setMax(range.max());
                    }

                    category.addEntry(doubleField.build());

                } else if (fieldType == String.class) {
                    category.addEntry(entryBuilder.startStrField(
                                    Component.translatable(translationKey),
                                    (String) currentValue
                            )
                            .setDefaultValue(getDefaultStringValue(field))
                            .setTooltip(Component.translatable(translationKey + ".tooltip"))
                            .setSaveConsumer(createStringSaveConsumer(sectionName, fieldName))
                            .build());

                } else if (List.class.isAssignableFrom(fieldType) && isStringList(field)) {
                    // Handle List<String> type for entity whitelist and other string lists
                    @SuppressWarnings("unchecked")
                    List<String> currentList = (List<String>) currentValue;

                    category.addEntry(entryBuilder.startStrList(
                                    Component.translatable(translationKey),
                                    currentList
                            )
                            .setDefaultValue(getDefaultStringListValue(field))
                            .setTooltip(Component.translatable(translationKey + ".tooltip"))
                            .setSaveConsumer(createStringListSaveConsumer(sectionName, fieldName))
                            .build());
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate entries for section: {}", sectionName, e);
        }
    }

    private static void saveConfig() {
        TritiumCommon.LOG.info("Saving configuration via Cloth Config...");
        TritiumConfig.save();
    }

    private static Consumer<Boolean> createBooleanSaveConsumer(String section, String key) {
        return value -> updateConfigValue(section, key, value);
    }

    private static Consumer<Integer> createIntSaveConsumer(String section, String key) {
        return value -> updateConfigValue(section, key, value);
    }

    private static Consumer<Double> createDoubleSaveConsumer(String section, String key) {
        return value -> updateConfigValue(section, key, value);
    }

    private static Consumer<String> createStringSaveConsumer(String section, String key) {
        return value -> updateConfigValue(section, key, value);
    }

    private static Consumer<List<String>> createStringListSaveConsumer(String section, String key) {
        return value -> updateConfigValue(section, key, value);
    }

    private static void updateConfigValue(String section, String key, Object value) {
        try {
            TritiumConfigBase config = TritiumConfig.get();
            Field sectionField = TritiumConfigBase.class.getDeclaredField(section);
            sectionField.setAccessible(true);
            Object sectionObj = sectionField.get(config);

            Field valueField = sectionObj.getClass().getDeclaredField(key);
            valueField.setAccessible(true);
            valueField.set(sectionObj, value);

            TritiumConfig.save();

        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to update config value: {}.{}", section, key, e);
        }
    }

    private static boolean getDefaultBooleanValue(Field field) {
        try {
            Object defaultSection = field.getDeclaringClass().newInstance();
            return field.getBoolean(defaultSection);
        } catch (Exception e) {
            return false;
        }
    }

    private static int getDefaultIntValue(Field field) {
        try {
            Object defaultSection = field.getDeclaringClass().newInstance();
            return field.getInt(defaultSection);
        } catch (Exception e) {
            return 0;
        }
    }

    private static double getDefaultDoubleValue(Field field) {
        try {
            Object defaultSection = field.getDeclaringClass().newInstance();
            return field.getDouble(defaultSection);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static String getDefaultStringValue(Field field) {
        try {
            Object defaultSection = field.getDeclaringClass().newInstance();
            return (String) field.get(defaultSection);
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> getDefaultStringListValue(Field field) {
        try {
            Object defaultSection = field.getDeclaringClass().newInstance();
            return (List<String>) field.get(defaultSection);
        } catch (Exception e) {
            return List.of();
        }
    }

    private static boolean isStringList(Field field) {
        // This is a simplified check - in practice you might want more robust type checking
        return List.class.isAssignableFrom(field.getType());
    }

    private static boolean hasConfigurableFields(Object section) {
        if (section == null) return false;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                // Check if field is a configuration field (not synthetic, not static)
                if (!field.isSynthetic() && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}