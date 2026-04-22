package de.sasd.mustelalog.client.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Very small JSON parser and writer for the client.
 *
 * <p>The project intentionally avoids runtime dependencies to keep the V1 client easy to build and
 * easy to understand. This helper is therefore small by design and only implements the JSON features
 * needed by the client.</p>
 */
public final class SimpleJson
{
    private SimpleJson()
    {
    }

    public static Object parse(String json)
    {
        return new Parser(json).parse();
    }

    public static String stringify(Object value)
    {
        StringBuilder builder = new StringBuilder();
        writeValue(builder, value, false, 0);
        return builder.toString();
    }

    public static String stringifyPretty(Object value)
    {
        StringBuilder builder = new StringBuilder();
        writeValue(builder, value, true, 0);
        return builder.toString();
    }

    private static void writeValue(StringBuilder builder, Object value, boolean pretty, int indent)
    {
        if (value == null)
        {
            builder.append("null");
            return;
        }

        if (value instanceof String text)
        {
            builder.append('"').append(escape(text)).append('"');
            return;
        }

        if (value instanceof Number || value instanceof Boolean)
        {
            builder.append(value);
            return;
        }

        if (value instanceof Map<?, ?> map)
        {
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet())
            {
                if (!first)
                {
                    builder.append(',');
                }
                if (pretty)
                {
                    builder.append('\n').append("  ".repeat(indent + 1));
                }
                builder.append('"').append(escape(String.valueOf(entry.getKey()))).append('"').append(':');
                if (pretty)
                {
                    builder.append(' ');
                }
                writeValue(builder, entry.getValue(), pretty, indent + 1);
                first = false;
            }
            if (pretty && !map.isEmpty())
            {
                builder.append('\n').append("  ".repeat(indent));
            }
            builder.append('}');
            return;
        }

        if (value instanceof List<?> list)
        {
            builder.append('[');
            boolean first = true;
            for (Object item : list)
            {
                if (!first)
                {
                    builder.append(',');
                }
                if (pretty)
                {
                    builder.append('\n').append("  ".repeat(indent + 1));
                }
                writeValue(builder, item, pretty, indent + 1);
                first = false;
            }
            if (pretty && !list.isEmpty())
            {
                builder.append('\n').append("  ".repeat(indent));
            }
            builder.append(']');
            return;
        }

        builder.append('"').append(escape(String.valueOf(value))).append('"');
    }

    private static String escape(String input)
    {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static final class Parser
    {
        private final String json;
        private int index;

        private Parser(String json)
        {
            this.json = json == null ? "" : json;
        }

        private Object parse()
        {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != json.length())
            {
                throw new JsonException("Unexpected trailing JSON content at index " + index);
            }
            return value;
        }

        private Object parseValue()
        {
            skipWhitespace();
            if (index >= json.length())
            {
                throw new JsonException("Unexpected end of JSON");
            }
            char current = json.charAt(index);
            return switch (current)
            {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject()
        {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}'))
            {
                expect('}');
                return result;
            }
            while (true)
            {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                result.put(key, parseValue());
                skipWhitespace();
                if (peek('}'))
                {
                    expect('}');
                    return result;
                }
                expect(',');
            }
        }

        private List<Object> parseArray()
        {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (peek(']'))
            {
                expect(']');
                return result;
            }
            while (true)
            {
                result.add(parseValue());
                skipWhitespace();
                if (peek(']'))
                {
                    expect(']');
                    return result;
                }
                expect(',');
                skipWhitespace();
            }
        }

        private String parseString()
        {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < json.length())
            {
                char current = json.charAt(index++);
                if (current == '"')
                {
                    return builder.toString();
                }
                if (current == '\\')
                {
                    if (index >= json.length())
                    {
                        throw new JsonException("Invalid escape sequence");
                    }
                    char escaped = json.charAt(index++);
                    switch (escaped)
                    {
                        case '"', '\\', '/' -> builder.append(escaped);
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' ->
                        {
                            if (index + 4 > json.length())
                            {
                                throw new JsonException("Invalid unicode escape");
                            }
                            String hex = json.substring(index, index + 4);
                            builder.append((char) Integer.parseInt(hex, 16));
                            index += 4;
                        }
                        default -> throw new JsonException("Unsupported escape sequence: \\" + escaped);
                    }
                }
                else
                {
                    builder.append(current);
                }
            }
            throw new JsonException("Unterminated string literal");
        }

        private Object parseLiteral(String literal, Object value)
        {
            if (json.startsWith(literal, index))
            {
                index += literal.length();
                return value;
            }
            throw new JsonException("Invalid JSON literal at index " + index);
        }

        private Number parseNumber()
        {
            int start = index;
            while (index < json.length())
            {
                char current = json.charAt(index);
                if ("-+0123456789.eE".indexOf(current) >= 0)
                {
                    index++;
                }
                else
                {
                    break;
                }
            }
            String token = json.substring(start, index);
            if (token.isBlank())
            {
                throw new JsonException("Expected JSON value at index " + start);
            }
            return new BigDecimal(token);
        }

        private void skipWhitespace()
        {
            while (index < json.length() && Character.isWhitespace(json.charAt(index)))
            {
                index++;
            }
        }

        private boolean peek(char expected)
        {
            return index < json.length() && json.charAt(index) == expected;
        }

        private void expect(char expected)
        {
            if (index >= json.length() || json.charAt(index) != expected)
            {
                throw new JsonException("Expected '" + expected + "' at index " + index);
            }
            index++;
        }
    }
}
