package me.zcraft.tconfig.config;

import org.craftamethyst.tritium.TritiumCommon;
import me.zcraft.tconfig.annotation.Range;
import me.zcraft.tconfig.annotation.Validation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

            validateCustomRules(config);
        } catch (Exception e) {
            TritiumCommon.LOG.error("Failed to validate configuration", e);
            throw new RuntimeException("Configuration validation failed", e);
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

        Validation validation = field.getAnnotation(Validation.class);
        if (validation != null) {
            validateCustomRule(validation, field, value, fieldPath);
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

    private static void validateCustomRule(Validation validation, Field field, Object value, String fieldPath) {
        String rule = validation.value();

        if (rule.startsWith("minLength:")) {
            int minLength = Integer.parseInt(rule.substring(10));
            if (value instanceof String && ((String) value).length() < minLength) {
                throw new IllegalArgumentException(String.format(
                        "Config validation failed: %s length must be at least %d",
                        fieldPath, minLength
                ));
            }
        } else if (rule.startsWith("maxLength:")) {
            int maxLength = Integer.parseInt(rule.substring(10));
            if (value instanceof String && ((String) value).length() > maxLength) {
                throw new IllegalArgumentException(String.format(
                        "Config validation failed: %s length must be at most %d",
                        fieldPath, maxLength
                ));
            }
        } else if (rule.startsWith("regex:")) {
            String regex = rule.substring(6);
            if (value instanceof String && !((String) value).matches(regex)) {
                throw new IllegalArgumentException(String.format(
                        "Config validation failed: %s must match pattern %s",
                        fieldPath, regex
                ));
            }
        }
    }

    private static void validateCustomRules(Object config) {
        try {
            Method validationMethod = config.getClass().getMethod("validateConfig");
            validationMethod.invoke(config);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
            throw new RuntimeException("Custom validation failed: " + e.getMessage(), e);
        }
    }
}