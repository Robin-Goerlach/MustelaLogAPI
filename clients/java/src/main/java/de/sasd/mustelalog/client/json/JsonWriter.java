package de.sasd.mustelalog.client.json;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

public final class JsonWriter {
    private JsonWriter() {
    }

    public static String write(Object value, boolean pretty) {
        StringBuilder builder = new StringBuilder();
        appendValue(builder, value, pretty, 0);
        return builder.toString();
    }

    private static void appendValue(StringBuilder builder, Object value, boolean pretty, int depth) {
        if (value == null) {
            builder.append("null");
            return;
        }
        if (value instanceof String text) {
            appendString(builder, text);
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            builder.append(String.valueOf(value));
            return;
        }
        if (value instanceof Map<?, ?> map) {
            appendMap(builder, map, pretty, depth);
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            appendIterable(builder, iterable.iterator(), pretty, depth);
            return;
        }
        if (value.getClass().isArray()) {
            appendArray(builder, value, pretty, depth);
            return;
        }
        appendString(builder, String.valueOf(value));
    }

    private static void appendMap(StringBuilder builder, Map<?, ?> map, boolean pretty, int depth) {
        builder.append('{');
        if (!map.isEmpty()) {
            int index = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (index++ > 0) {
                    builder.append(',');
                }
                newline(builder, pretty, depth + 1);
                appendString(builder, String.valueOf(entry.getKey()));
                builder.append(pretty ? ": " : ":");
                appendValue(builder, entry.getValue(), pretty, depth + 1);
            }
            newline(builder, pretty, depth);
        }
        builder.append('}');
    }

    private static void appendIterable(StringBuilder builder, Iterator<?> iterator, boolean pretty, int depth) {
        builder.append('[');
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                builder.append(',');
            }
            newline(builder, pretty, depth + 1);
            appendValue(builder, iterator.next(), pretty, depth + 1);
            first = false;
        }
        if (!first) {
            newline(builder, pretty, depth);
        }
        builder.append(']');
    }

    private static void appendArray(StringBuilder builder, Object value, boolean pretty, int depth) {
        builder.append('[');
        int length = Array.getLength(value);
        for (int index = 0; index < length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            newline(builder, pretty, depth + 1);
            appendValue(builder, Array.get(value, index), pretty, depth + 1);
        }
        if (length > 0) {
            newline(builder, pretty, depth);
        }
        builder.append(']');
    }

    private static void appendString(StringBuilder builder, String text) {
        builder.append('"');
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            switch (current) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (current < 32) {
                        builder.append(String.format("\\u%04x", (int) current));
                    } else {
                        builder.append(current);
                    }
                }
            }
        }
        builder.append('"');
    }

    private static void newline(StringBuilder builder, boolean pretty, int depth) {
        if (!pretty) {
            return;
        }
        builder.append('\n');
        builder.append("  ".repeat(Math.max(0, depth)));
    }
}
