package de.sasd.mustelalog.client.logging;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Masks secrets before they are written to diagnostics logs.
 *
 * <p>This class intentionally uses simple and readable rules instead of heavy reflection or
 * serialization tricks. The goal is that sensitive values are not written accidentally, while the
 * logged context still remains useful for diagnostics.</p>
 */
public final class LogSanitizer
{
    private LogSanitizer() {}

    /**
     * Returns a sanitized copy of a context map.
     *
     * @param input original context map
     * @return sanitized copy
     */
    public static Map<String, Object> sanitizeContext(Map<String, Object> input)
    {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        if (input == null)
        {
            return sanitized;
        }

        for (Map.Entry<String, Object> entry : input.entrySet())
        {
            sanitized.put(entry.getKey(), sanitizeValue(entry.getKey(), entry.getValue()));
        }
        return sanitized;
    }

    /**
     * Sanitizes a single value.
     *
     * @param key context key
     * @param value original value
     * @return sanitized value
     */
    public static Object sanitizeValue(String key, Object value)
    {
        if (value == null)
        {
            return null;
        }

        String normalizedKey = key == null ? "" : key.toLowerCase();
        if (normalizedKey.contains("token")
            || normalizedKey.contains("authorization")
            || normalizedKey.contains("api-key")
            || normalizedKey.contains("apikey")
            || normalizedKey.contains("secret")
            || normalizedKey.contains("password"))
        {
            return "<masked>";
        }

        if (value instanceof String text)
        {
            return sanitizeText(text);
        }

        return value;
    }

    /**
     * Masks obvious token-bearing text fragments.
     *
     * @param text original text
     * @return sanitized text
     */
    public static String sanitizeText(String text)
    {
        String sanitized = text;
        sanitized = sanitized.replaceAll("(?i)(authorization\s*[:=]\s*bearer\s+)[^\s,;]+", "$1<masked>");
        sanitized = sanitized.replaceAll("(?i)(token\s*[:=]\s*)[^\s,;]+", "$1<masked>");
        sanitized = sanitized.replaceAll("(?i)(api[-_ ]?key\s*[:=]\s*)[^\s,;]+", "$1<masked>");
        sanitized = sanitized.replaceAll("(?i)(password\s*[:=]\s*)[^\s,;]+", "$1<masked>");
        return sanitized;
    }
}
