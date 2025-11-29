package me.zcraft.tconfig.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import org.craftamethyst.tritium.TritiumCommon;
import me.zcraft.tconfig.annotation.Range;
import me.zcraft.tconfig.annotation.SubCategory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TritiumAutoConfig {
    private final TritiumConfig config;
    private final Map<String, FieldAccessor> fieldAccessors = new ConcurrentHashMap<>();

    public TritiumAutoConfig(TritiumConfig config) {
        this.config = config;
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

    private static boolean hasConfigurableFields(Object section) {
        if (section == null) return false;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isSynthetic()) continue;
                // Non-subcategory f the section has configurable entries
                if (!field.isAnnotationPresent(SubCategory.class)) {
                    return true;
                }
                // Subcategory field: check recursively whether it contains configurable fields
                Object subSection = field.get(section);
                if (hasConfigurableFields(subSection)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config." + config.getModId() + ".title"))
                .transparentBackground()
                .setSavingRunnable(config::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        boolean createdAnyCategory = false;

        try {
            Object configObj = config.get();
            Class<?> configClass = configObj.getClass();
            initializeFieldAccessors(configObj);

            for (Field sectionField : configClass.getDeclaredFields()) {
                sectionField.setAccessible(true);
                Object section = sectionField.get(configObj);
                String sectionName = sectionField.getName();

                if (section != null && hasConfigurableFields(section)) {
                    ConfigCategory category = builder.getOrCreateCategory(
                            Component.translatable("config." + config.getModId() + ".category." + sectionName)
                    );
                    createdAnyCategory = true;
                    generateSectionEntries(entryBuilder, category, section, sectionName);
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate config screen for mod: {}", config.getModId(), e);
        }

        // Ensure at least one category exists to satisfy Cloth Config v15 requirements
        if (!createdAnyCategory) {
            builder.getOrCreateCategory(Component.translatable("config." + config.getModId() + ".category.general"));
        }

        return builder.build();
    }

    private void initializeFieldAccessors(Object config) throws Exception {
        if (!fieldAccessors.isEmpty()) return;

        for (Field sectionField : config.getClass().getDeclaredFields()) {
            sectionField.setAccessible(true);
            Object section = sectionField.get(config);
            cacheFieldAccessorsRecursive(section, sectionField.getName());
        }
    }

    private void cacheFieldAccessorsRecursive(Object section, String path) throws Exception {
        if (section == null) return;

        for (Field field : section.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String fullPath = path.isEmpty() ? field.getName() : path + "." + field.getName();

            if (field.isAnnotationPresent(SubCategory.class)) {
                Object subSection = field.get(section);
                cacheFieldAccessorsRecursive(subSection, fullPath);
            } else {
                fieldAccessors.put(fullPath, new ReflectionFieldAccessor(field));
            }
        }
    }

    private void generateSectionEntries(ConfigEntryBuilder entryBuilder,
                                        ConfigCategory category,
                                        Object section,
                                        String sectionName) {
        if (section == null) return;

        try {
            for (Field field : section.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                String fieldName = field.getName();
                Object currentValue = field.get(section);
                String translationKey = "config." + config.getModId() + "." + sectionName + "." + fieldName.replace('.', '_');

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

    private void generateSubCategoryEntries(ConfigEntryBuilder entryBuilder,
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
                String accessorPath = sectionName + "." + fullPath;
                String translationKey = "config." + config.getModId() + "." + sectionName + "." + fullPath.replace('.', '_');

                if (field.isAnnotationPresent(SubCategory.class)) {
                    SubCategory subCat = field.getAnnotation(SubCategory.class);
                    SubCategoryBuilder nestedSubCategoryBuilder = entryBuilder.startSubCategory(Component.translatable(translationKey));
                    generateSubCategoryEntries(entryBuilder, nestedSubCategoryBuilder, currentValue, sectionName, fullPath);
                    subCategoryBuilder.add(nestedSubCategoryBuilder.build());
                } else {
                    FieldAccessor accessor = fieldAccessors.get(accessorPath);
                    if (accessor != null) {
                        generateSubCategoryFieldEntry(entryBuilder, subCategoryBuilder, accessor, currentValue, translationKey, accessorPath);
                    } else {
                        TritiumCommon.LOG.warn("No accessor found for path: {}", accessorPath);
                    }
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to generate subcategory entries for section: {}", sectionName, e);
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void generateFieldEntry(ConfigEntryBuilder entryBuilder,
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
    private void generateSubCategoryFieldEntry(ConfigEntryBuilder entryBuilder,
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
                        .setSaveConsumer(createSaveConsumer(accessorPath));  // 修复：使用 accessorPath

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
                        .setSaveConsumer(createSaveConsumer(accessorPath));  // 修复：使用 accessorPath

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
                        .setSaveConsumer(createSaveConsumer(accessorPath))  // 修复：使用 accessorPath
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

    private <T> Consumer<T> createSaveConsumer(String fullPath) {
        return value -> updateConfigValue(fullPath, value);
    }

    private void updateConfigValue(String fullPath, Object value) {
        try {
            TritiumCommon.LOG.debug("Updating config value: {} = {}", fullPath, value);

            String[] pathParts = fullPath.split("\\.");
            if (pathParts.length < 2) {
                TritiumCommon.LOG.warn("Invalid path format: {}", fullPath);
                return;
            }

            FieldAccessor accessor = fieldAccessors.get(fullPath);
            if (accessor == null) {
                TritiumCommon.LOG.warn("No field accessor found for path: {}", fullPath);
                TritiumCommon.LOG.debug("Available accessors: {}", fieldAccessors.keySet());
                return;
            }

            Object currentObj = config.get();

            // 遍历路径的所有部分（除了最后一个字段名）
            for (int i = 0; i < pathParts.length - 1; i++) {
                String part = pathParts[i];
                Field field = findField(currentObj.getClass(), part);
                if (field == null) {
                    TritiumCommon.LOG.warn("Field not found: {} in path: {}", part, fullPath);
                    return;
                }

                field.setAccessible(true);
                Object nextObj = field.get(currentObj);

                // 如果子对象为null，创建新的实例
                if (nextObj == null) {
                    try {
                        nextObj = field.getType().newInstance();
                        field.set(currentObj, nextObj);
                        TritiumCommon.LOG.debug("Created new instance for: {}", part);
                    } catch (Exception e) {
                        TritiumCommon.LOG.error("Failed to create instance for: {}", part, e);
                        return;
                    }
                }
                currentObj = nextObj;
            }

            // 设置最终字段的值
            accessor.setValue(currentObj, value);
            config.save();
            TritiumCommon.LOG.debug("Successfully updated config value: {}", fullPath);

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
}