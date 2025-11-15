package org.craftamethyst.tritium.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.zcraft.tritiumconfig.annotation.Range;
import me.zcraft.tritiumconfig.annotation.SubCategory;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.TritiumCommon;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TritiumAutoConfig {

    private static final Map<String, FieldAccessor> fieldAccessors = new ConcurrentHashMap<>();

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.tritium.title"))
                .transparentBackground()
                .setSavingRunnable(TritiumAutoConfig::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        try {
            TritiumConfigBase config = TritiumConfig.get();
            initializeFieldAccessors(config);

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

    private static void initializeFieldAccessors(TritiumConfigBase config) throws Exception {
        if (!fieldAccessors.isEmpty()) return;

        for (Field sectionField : TritiumConfigBase.class.getDeclaredFields()) {
            sectionField.setAccessible(true);
            Object section = sectionField.get(config);
            cacheFieldAccessorsRecursive(section, sectionField.getName());
        }
    }

    private static void cacheFieldAccessorsRecursive(Object section, String path) throws Exception {
        if (section == null) return;

        for (Field field : section.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String fullPath = path + "." + field.getName();

            if (field.isAnnotationPresent(SubCategory.class)) {
                Object subSection = field.get(section);
                cacheFieldAccessorsRecursive(subSection, fullPath);
            } else {
                fieldAccessors.put(fullPath, new ReflectionFieldAccessor(field));
            }
        }
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
                Object currentValue = field.get(section);
                String translationKey = "config.tritium." + sectionName + "." + fieldName.replace('.', '_');

                if (field.isAnnotationPresent(SubCategory.class)) {
                    SubCategory subCat = field.getAnnotation(SubCategory.class);
                    SubCategoryBuilder subCategoryBuilder = entryBuilder.startSubCategory(Component.translatable(translationKey));
                    generateSubCategoryEntries(entryBuilder, subCategoryBuilder, currentValue, sectionName, fieldName);
                    category.addEntry(subCategoryBuilder.build());
                } else {
                    String fullPath = sectionName + "." + fieldName;
                    FieldAccessor accessor = fieldAccessors.get(fullPath);
                    if (accessor != null) {
                        generateFieldEntry(entryBuilder, category, accessor, currentValue, translationKey, fullPath);
                    }
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
                Object currentValue = field.get(section);
                String fullPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                String accessorPath = sectionName + "." + path + "." + fieldName;
                String translationKey = "config.tritium" + "." + sectionName + "." + fullPath.replace('.', '_');

                if (field.isAnnotationPresent(SubCategory.class)) {
                    SubCategory subCat = field.getAnnotation(SubCategory.class);
                    SubCategoryBuilder nestedSubCategoryBuilder = entryBuilder.startSubCategory(Component.translatable(translationKey));
                    generateSubCategoryEntries(entryBuilder, nestedSubCategoryBuilder, currentValue, sectionName, fullPath);
                    subCategoryBuilder.add(nestedSubCategoryBuilder.build());
                } else {
                    FieldAccessor accessor = fieldAccessors.get(accessorPath);
                    if (accessor != null) {
                        generateSubCategoryFieldEntry(entryBuilder, subCategoryBuilder, accessor, currentValue, translationKey, accessorPath);
                    }
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate subcategory entries for section: {}", sectionName, e);
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void generateFieldEntry(ConfigEntryBuilder entryBuilder,
                                           ConfigCategory category,
                                           FieldAccessor accessor,
                                           Object currentValue,
                                           String translationKey,
                                           String fullPath) {
        Class<?> fieldType = accessor.getType();

        try {
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                category.addEntry(entryBuilder.startBooleanToggle(
                                Component.translatable(translationKey),
                                (Boolean) currentValue
                        )
                        .setDefaultValue((Boolean) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(fullPath))
                        .build());

            } else if (fieldType == int.class || fieldType == Integer.class) {
                var intField = entryBuilder.startIntField(
                                Component.translatable(translationKey),
                                (Integer) currentValue
                        )
                        .setDefaultValue((Integer) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(fullPath));

                Range range = accessor.getRangeAnnotation();
                if (range != null) {
                    intField.setMin((int) range.min()).setMax((int) range.max());
                }

                category.addEntry(intField.build());

            } else if (fieldType == double.class || fieldType == Double.class) {
                var doubleField = entryBuilder.startDoubleField(
                                Component.translatable(translationKey),
                                (Double) currentValue
                        )
                        .setDefaultValue((Double) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(fullPath));

                Range range = accessor.getRangeAnnotation();
                if (range != null) {
                    doubleField.setMin(range.min()).setMax(range.max());
                }

                category.addEntry(doubleField.build());

            } else if (fieldType == String.class) {
                category.addEntry(entryBuilder.startStrField(
                                Component.translatable(translationKey),
                                (String) currentValue
                        )
                        .setDefaultValue((String) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(fullPath))
                        .build());

            } else if (List.class.isAssignableFrom(fieldType)) {
                List<String> currentList = (List<String>) currentValue;

                category.addEntry(entryBuilder.startStrList(
                                Component.translatable(translationKey),
                                currentList
                        )
                        .setDefaultValue((List<String>) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(fullPath))
                        .build());

            } else if (fieldType.isEnum()) {
                category.addEntry(entryBuilder.startEnumSelector(
                                Component.translatable(translationKey),
                                (Class<Enum>) fieldType,
                                (Enum) currentValue
                        )
                        .setDefaultValue((Enum) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(fullPath))
                        .build());
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate field entry: {}", fullPath, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void generateSubCategoryFieldEntry(ConfigEntryBuilder entryBuilder,
                                                      SubCategoryBuilder subCategoryBuilder,
                                                      FieldAccessor accessor,
                                                      Object currentValue,
                                                      String translationKey,
                                                      String accessorPath) {
        Class<?> fieldType = accessor.getType();

        try {
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                subCategoryBuilder.add(entryBuilder.startBooleanToggle(
                                Component.translatable(translationKey),
                                (Boolean) currentValue
                        )
                        .setDefaultValue((Boolean) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(accessorPath))
                        .build());

            } else if (fieldType == int.class || fieldType == Integer.class) {
                var intField = entryBuilder.startIntField(
                                Component.translatable(translationKey),
                                (Integer) currentValue
                        )
                        .setDefaultValue((Integer) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(accessorPath));

                Range range = accessor.getRangeAnnotation();
                if (range != null) {
                    intField.setMin((int) range.min()).setMax((int) range.max());
                }

                subCategoryBuilder.add(intField.build());

            } else if (fieldType == double.class || fieldType == Double.class) {
                var doubleField = entryBuilder.startDoubleField(
                                Component.translatable(translationKey),
                                (Double) currentValue
                        )
                        .setDefaultValue((Double) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(accessorPath));

                Range range = accessor.getRangeAnnotation();
                if (range != null) {
                    doubleField.setMin(range.min()).setMax(range.max());
                }

                subCategoryBuilder.add(doubleField.build());

            } else if (fieldType == String.class) {
                subCategoryBuilder.add(entryBuilder.startStrField(
                                Component.translatable(translationKey),
                                (String) currentValue
                        )
                        .setDefaultValue((String) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(accessorPath))
                        .build());

            } else if (List.class.isAssignableFrom(fieldType)) {
                List<String> currentList = (List<String>) currentValue;

                subCategoryBuilder.add(entryBuilder.startStrList(
                                Component.translatable(translationKey),
                                currentList
                        )
                        .setDefaultValue((List<String>) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(accessorPath))
                        .build());

            } else if (fieldType.isEnum()) {
                subCategoryBuilder.add(entryBuilder.startEnumSelector(
                                Component.translatable(translationKey),
                                (Class<Enum>) fieldType,
                                (Enum) currentValue
                        )
                        .setDefaultValue((Enum) accessor.getDefaultValue())
                        .setTooltip(Component.translatable(translationKey + ".tooltip"))
                        .setSaveConsumer(createSaveConsumer(accessorPath))
                        .build());
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate subcategory field entry: {}", accessorPath, e);
        }
    }

    private static <T> Consumer<T> createSaveConsumer(String fullPath) {
        return value -> updateConfigValue(fullPath, value);
    }

    private static void updateConfigValue(String fullPath, Object value) {
        try {
            String[] pathParts = fullPath.split("\\.");
            if (pathParts.length < 2) return;

            Object currentObj = TritiumConfig.get();
            for (int i = 0; i < pathParts.length - 1; i++) {
                Field field = findField(currentObj.getClass(), pathParts[i]);
                if (field == null) return;

                field.setAccessible(true);
                Object nextObj = field.get(currentObj);
                if (nextObj == null) {
                    nextObj = field.getType().newInstance();
                    field.set(currentObj, nextObj);
                }
                currentObj = nextObj;
            }

            String finalFieldName = pathParts[pathParts.length - 1];
            Field targetField = findField(currentObj.getClass(), finalFieldName);
            if (targetField != null) {
                targetField.setAccessible(true);
                targetField.set(currentObj, value);
                TritiumConfig.save();
                TritiumConfig.reload();
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to update config value: {}", fullPath, e);
        }
    }
    private interface FieldAccessor {
        Object getValue(Object obj) throws Exception;
        void setValue(Object obj, Object value) throws Exception;
        Object getDefaultValue() throws Exception;
        Class<?> getType();
        Range getRangeAnnotation();
    }

    private static class ReflectionFieldAccessor implements FieldAccessor {
        private final Field field;
        private final Range range;

        public ReflectionFieldAccessor(Field field) {
            this.field = field;
            this.field.setAccessible(true);
            this.range = field.getAnnotation(Range.class);
        }

        @Override
        public Object getValue(Object obj) throws Exception {
            return field.get(obj);
        }

        @Override
        public void setValue(Object obj, Object value) throws Exception {
            field.set(obj, value);
        }

        @Override
        public Object getDefaultValue() throws Exception {
            Object defaultInstance = field.getDeclaringClass().newInstance();
            return field.get(defaultInstance);
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }

        @Override
        public Range getRangeAnnotation() {
            return range;
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

    private static boolean hasConfigurableFields(Object section) {
        if (section == null) return false;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                if (!field.isSynthetic() && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (type == boolean.class || type == Boolean.class ||
                            type == int.class || type == Integer.class ||
                            type == double.class || type == Double.class ||
                            type == String.class ||
                            type.isEnum() ||
                            List.class.isAssignableFrom(type)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}