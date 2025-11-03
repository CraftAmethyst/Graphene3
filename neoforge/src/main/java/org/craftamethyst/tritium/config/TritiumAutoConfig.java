package org.craftamethyst.tritium.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.zcraft.tritiumconfig.annotation.Range;
import me.zcraft.tritiumconfig.annotation.SubCategory;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import me.zcraft.tritiumconfig.config.TritiumConfigBase;
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
            TritiumConfigBase config = me.zcraft.tritiumconfig.config.TritiumConfig.get();
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
                String translationKey = "config.tritium." + sectionName + "." + fieldName.replace('.', '_');

                if (field.isAnnotationPresent(SubCategory.class)) {
                    SubCategory subCat = field.getAnnotation(SubCategory.class);
                    SubCategoryBuilder subCategoryBuilder = entryBuilder.startSubCategory(Component.translatable(translationKey));
                    generateSubCategoryEntries(entryBuilder, subCategoryBuilder, currentValue, sectionName, fieldName);
                    category.addEntry(subCategoryBuilder.build());
                } else {
                    generateFieldEntry(entryBuilder, category, field, currentValue, translationKey, sectionName, fieldName);
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate entries for section: {}", sectionName, e);
        }
    }

    private static void generateSubCategoryEntries(ConfigEntryBuilder entryBuilder,
                                                   SubCategoryBuilder subCategoryBuilder,
                                                   Object section,
                                                   String sectionName,
                                                   String path) {
        if (section == null) return;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                Object currentValue = field.get(section);
                String fullPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                String translationKey = "config.tritium." + sectionName + "." + fullPath.replace('.', '_');

                if (field.isAnnotationPresent(SubCategory.class)) {
                    // Handle nested subcategory
                    SubCategory subCat = field.getAnnotation(SubCategory.class);
                    SubCategoryBuilder nestedSubCategoryBuilder = entryBuilder.startSubCategory(Component.translatable(translationKey));

                    generateSubCategoryEntries(entryBuilder, nestedSubCategoryBuilder, currentValue, sectionName, fullPath);
                    subCategoryBuilder.add(nestedSubCategoryBuilder.build());
                } else {
                    generateSubCategoryFieldEntry(entryBuilder, subCategoryBuilder, field, currentValue, translationKey, sectionName, fullPath);
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate subcategory entries for section: {}", sectionName, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void generateFieldEntry(ConfigEntryBuilder entryBuilder,
                                           ConfigCategory category,
                                           Field field,
                                           Object currentValue,
                                           String translationKey,
                                           String sectionName,
                                           String fieldPath) {
        Class<?> fieldType = field.getType();

        try {
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                category.addEntry(entryBuilder.startBooleanToggle(
                                Component.translatable(translationKey),
                                (Boolean) currentValue
                        )
                        .setDefaultValue(getDefaultBooleanValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createBooleanSaveConsumer(sectionName, fieldPath))
                        .build());

            } else if (fieldType == int.class || fieldType == Integer.class) {
                var intField = entryBuilder.startIntField(
                                Component.translatable(translationKey),
                                (Integer) currentValue
                        )
                        .setDefaultValue(getDefaultIntValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createIntSaveConsumer(sectionName, fieldPath));

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
                        .setSaveConsumer(createDoubleSaveConsumer(sectionName, fieldPath));

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
                        .setSaveConsumer(createStringSaveConsumer(sectionName, fieldPath))
                        .build());

            } else if (List.class.isAssignableFrom(fieldType)) {
                List<String> currentList = (List<String>) currentValue;

                category.addEntry(entryBuilder.startStrList(
                                Component.translatable(translationKey),
                                currentList
                        )
                        .setDefaultValue(getDefaultStringListValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createStringListSaveConsumer(sectionName, fieldPath))
                        .build());

            } else if (fieldType.isEnum()) {
                category.addEntry(entryBuilder.startEnumSelector(
                                Component.translatable(translationKey),
                                (Class<Enum>) fieldType,
                                (Enum) currentValue
                        )
                        .setDefaultValue((Enum) getDefaultEnumValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createEnumSaveConsumer(sectionName, fieldPath, fieldType))
                        .build());
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate field entry: {}", fieldPath, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void generateSubCategoryFieldEntry(ConfigEntryBuilder entryBuilder,
                                                      SubCategoryBuilder subCategoryBuilder,
                                                      Field field,
                                                      Object currentValue,
                                                      String translationKey,
                                                      String sectionName,
                                                      String fieldPath) {
        Class<?> fieldType = field.getType();

        try {
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                subCategoryBuilder.add(entryBuilder.startBooleanToggle(
                                Component.translatable(translationKey),
                                (Boolean) currentValue
                        )
                        .setDefaultValue(getDefaultBooleanValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createBooleanSaveConsumer(sectionName, fieldPath))
                        .build());

            } else if (fieldType == int.class || fieldType == Integer.class) {
                var intField = entryBuilder.startIntField(
                                Component.translatable(translationKey),
                                (Integer) currentValue
                        )
                        .setDefaultValue(getDefaultIntValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createIntSaveConsumer(sectionName, fieldPath));

                Range range = field.getAnnotation(Range.class);
                if (range != null) {
                    intField.setMin((int) range.min()).setMax((int) range.max());
                }

                subCategoryBuilder.add(intField.build());

            } else if (fieldType == double.class || fieldType == Double.class) {
                var doubleField = entryBuilder.startDoubleField(
                                Component.translatable(translationKey),
                                (Double) currentValue
                        )
                        .setDefaultValue(getDefaultDoubleValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createDoubleSaveConsumer(sectionName, fieldPath));

                Range range = field.getAnnotation(Range.class);
                if (range != null) {
                    doubleField.setMin(range.min()).setMax(range.max());
                }

                subCategoryBuilder.add(doubleField.build());

            } else if (fieldType == String.class) {
                subCategoryBuilder.add(entryBuilder.startStrField(
                                Component.translatable(translationKey),
                                (String) currentValue
                        )
                        .setDefaultValue(getDefaultStringValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createStringSaveConsumer(sectionName, fieldPath))
                        .build());

            } else if (List.class.isAssignableFrom(fieldType)) {
                List<String> currentList = (List<String>) currentValue;

                subCategoryBuilder.add(entryBuilder.startStrList(
                                Component.translatable(translationKey),
                                currentList
                        )
                        .setDefaultValue(getDefaultStringListValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createStringListSaveConsumer(sectionName, fieldPath))
                        .build());

            } else if (fieldType.isEnum()) {
                subCategoryBuilder.add(entryBuilder.startEnumSelector(
                                Component.translatable(translationKey),
                                (Class<Enum>) fieldType,
                                (Enum) currentValue
                        )
                        .setDefaultValue((Enum) getDefaultEnumValue(field))
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createEnumSaveConsumer(sectionName, fieldPath, fieldType))
                        .build());
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate subcategory field entry: {}", fieldPath, e);
        }
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

    @SuppressWarnings("rawtypes")
    private static Consumer<Enum> createEnumSaveConsumer(String section, String key, Class<?> enumClass) {
        return value -> updateConfigValue(section, key, value);
    }

    private static void updateConfigValue(String section, String key, Object value) {
        try {
            TritiumConfigBase config = me.zcraft.tritiumconfig.config.TritiumConfig.get();
            String fullPath = section + "." + key;

            String[] pathParts = fullPath.split("\\.");
            Object currentObj = config;
            Field currentField;

            for (int i = 0; i < pathParts.length; i++) {
                String part = pathParts[i];
                currentField = findField(currentObj.getClass(), part);
                if (currentField == null) {
                    TritiumCommon.LOG.error("Field not found: {} in class {}", part, currentObj.getClass().getSimpleName());
                    return;
                }

                currentField.setAccessible(true);

                if (i < pathParts.length - 1) {
                    Object nextObj = currentField.get(currentObj);
                    if (nextObj == null) {
                        nextObj = currentField.getType().newInstance();
                        currentField.set(currentObj, nextObj);
                    }
                    currentObj = nextObj;
                } else {
                    currentField.set(currentObj, value);
                }
            }

            TritiumConfig.save();

        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to update config value: {}.{}", section, key, e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findField(superClass, fieldName);
            }
            return null;
        }
    }

    private static void saveConfig() {
        TritiumConfig.save();
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

    private static Object getDefaultEnumValue(Field field) {
        try {
            Object defaultSection = field.getDeclaringClass().newInstance();
            return field.get(defaultSection);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean hasConfigurableFields(Object section) {
        if (section == null) return false;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
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