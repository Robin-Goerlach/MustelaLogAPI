package de.sasd.mustelalog.client.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonSupport {
    private JsonSupport() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value, String message) {
        if (value instanceof Map<?, ?> map) {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        throw new IllegalArgumentException(message);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asList(Object value) {
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return Collections.emptyList();
    }

    public static Map<String, Object> getObject(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Map<?, ?> ? asObject(value, "Field '" + key + "' must be an object.") : null;
    }

    public static String getString(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    public static int getInt(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public static boolean getBoolean(Map<String, Object> map, String key, boolean fallback) {
        Object value = map.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return fallback;
    }
}
