package de.sasd.mustelalog.client.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleJson {
    private SimpleJson() {
    }

    public static Object parse(String text) {
        return new Parser(text == null ? "" : text).parse();
    }

    public static String stringify(Object value) {
        return JsonWriter.write(value, false);
    }

    public static String pretty(Object value) {
        return JsonWriter.write(value, true);
    }

    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) {
            this.text = text;
        }

        private Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != text.length()) {
                throw new IllegalArgumentException("Unexpected trailing characters at position " + index + ".");
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON input.");
            }
            char current = text.charAt(index);
            return switch (current) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> readKeyword("true", Boolean.TRUE);
                case 'f' -> readKeyword("false", Boolean.FALSE);
                case 'n' -> readKeyword("null", null);
                default -> {
                    if (current == '-' || Character.isDigit(current)) {
                        yield parseNumber();
                    }
                    throw new IllegalArgumentException("Unexpected character '" + current + "' at position " + index + ".");
                }
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                expect('}');
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                map.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) {
                    expect('}');
                    return map;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            expect('[');
            ArrayList<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                expect(']');
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    expect(']');
                    return list;
                }
                expect(',');
                skipWhitespace();
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < text.length()) {
                char current = text.charAt(index++);
                if (current == '"') {
                    return builder.toString();
                }
                if (current == '\\') {
                    if (index >= text.length()) {
                        throw new IllegalArgumentException("Unterminated escape sequence.");
                    }
                    char escape = text.charAt(index++);
                    switch (escape) {
                        case '"' -> builder.append('"');
                        case '\\' -> builder.append('\\');
                        case '/' -> builder.append('/');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' -> builder.append(readUnicodeEscape());
                        default -> throw new IllegalArgumentException("Illegal escape sequence '\\" + escape + "'.");
                    }
                } else {
                    builder.append(current);
                }
            }
            throw new IllegalArgumentException("Unterminated string literal.");
        }

        private char readUnicodeEscape() {
            if (index + 4 > text.length()) {
                throw new IllegalArgumentException("Incomplete unicode escape sequence.");
            }
            String hex = text.substring(index, index + 4);
            index += 4;
            try {
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Illegal unicode escape: \\u" + hex + ".", exception);
            }
        }

        private Object parseNumber() {
            int start = index;
            if (peek('-')) {
                index++;
            }
            readDigits();
            boolean floatingPoint = false;
            if (peek('.')) {
                floatingPoint = true;
                index++;
                readDigits();
            }
            if (peek('e') || peek('E')) {
                floatingPoint = true;
                index++;
                if (peek('+') || peek('-')) {
                    index++;
                }
                readDigits();
            }
            String numberText = text.substring(start, index);
            try {
                if (floatingPoint) {
                    return Double.parseDouble(numberText);
                }
                long asLong = Long.parseLong(numberText);
                if (asLong >= Integer.MIN_VALUE && asLong <= Integer.MAX_VALUE) {
                    return (int) asLong;
                }
                return asLong;
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid number at position " + start + ": " + numberText, exception);
            }
        }

        private void readDigits() {
            if (index >= text.length() || !Character.isDigit(text.charAt(index))) {
                throw new IllegalArgumentException("Expected a digit at position " + index + ".");
            }
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
        }

        private Object readKeyword(String keyword, Object value) {
            if (!text.regionMatches(index, keyword, 0, keyword.length())) {
                throw new IllegalArgumentException("Expected keyword '" + keyword + "' at position " + index + ".");
            }
            index += keyword.length();
            return value;
        }

        private void skipWhitespace() {
            while (index < text.length()) {
                char current = text.charAt(index);
                if (current == ' ' || current == '\n' || current == '\r' || current == '\t') {
                    index++;
                } else {
                    return;
                }
            }
        }

        private void expect(char expected) {
            if (index >= text.length() || text.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' at position " + index + ".");
            }
            index++;
        }

        private boolean peek(char expected) {
            return index < text.length() && text.charAt(index) == expected;
        }
    }
}
