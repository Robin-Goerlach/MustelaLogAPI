package de.sasd.mustelalog.client.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TimeService {
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String currentUtcTimestamp() {
        return Instant.now().toString();
    }

    public String normalizeQueryFrom(String input) {
        return normalizeQueryBoundary(input, false);
    }

    public String normalizeQueryTo(String input) {
        return normalizeQueryBoundary(input, true);
    }

    private String normalizeQueryBoundary(String input, boolean endOfDay) {
        if (input == null || input.isBlank()) return "";
        String value = input.trim();
        try { return Instant.parse(value).toString(); } catch (DateTimeParseException ignored) { }
        try { return OffsetDateTime.parse(value).toInstant().toString(); } catch (DateTimeParseException ignored) { }
        try { return LocalDateTime.parse(value.replace(' ', 'T')).atZone(ZoneId.systemDefault()).toInstant().toString(); } catch (DateTimeParseException ignored) { }
        try {
            LocalDate date = LocalDate.parse(value);
            LocalDateTime localDateTime = endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
        } catch (DateTimeParseException ignored) { }
        return value;
    }

    public String formatForDisplay(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) return "";
        try { return DISPLAY.format(OffsetDateTime.parse(isoTimestamp).atZoneSameInstant(ZoneId.systemDefault())); } catch (DateTimeParseException ignored) { }
        try { return DISPLAY.format(Instant.parse(isoTimestamp).atZone(ZoneId.systemDefault())); } catch (DateTimeParseException ignored) { }
        return isoTimestamp;
    }

    public String utcNowPlusSeconds(int seconds) {
        return Instant.now().plusSeconds(seconds).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
