package me.zcraft.tritiumconfig.tritiumconfig.config;

import me.zcraft.tritiumconfig.annotation.Range;
import org.craftamethyst.tritium.TritiumCommon;

import java.lang.reflect.Field;
import java.util.List;

public class ConfigValidator {

    public static void validateConfig(Object config) {
        try {
            for (Field field : config.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(config);

                if (value != null) {
                    validateField(field, value, "");
                }
            }
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to validate configuration", e);
        }
    }

    private static void validateField(Field field, Object value, String path) throws Exception {
        String fieldPath = path.isEmpty() ? field.getName() : path + "." + field.getName();

        Range range = field.getAnnotation(Range.class);
        if (range != null && value instanceof Number) {
            double numValue = ((Number) value).doubleValue();
            if (numValue < range.min() || numValue > range.max()) {
                throw new IllegalArgumentException(String.format(
                        "Config validation failed: %s = %s is out of range [%s, %s]",
                        fieldPath, numValue, range.min(), range.max()
                ));
            }
        }

        if (!field.getType().isPrimitive() && !field.getType().isEnum() &&
                !field.getType().getName().startsWith("java.") &&
                !List.class.isAssignableFrom(field.getType())) {
            for (Field nestedField : value.getClass().getDeclaredFields()) {
                nestedField.setAccessible(true);
                Object nestedValue = nestedField.get(value);

                if (nestedValue != null) {
                    validateField(nestedField, nestedValue, fieldPath);
                }
            }
        }
    }
}