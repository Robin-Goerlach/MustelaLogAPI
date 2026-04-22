package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.model.TimeMode;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Handles API timestamps and user-facing time formatting.
 */
public final class TimeDisplayService
{
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter API_QUERY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Parses an API timestamp.
     *
     * @param rawValue timestamp text
     * @return parsed instant if possible
     */
    public Optional<Instant> parseApiInstant(String rawValue)
    {
        if (rawValue == null || rawValue.isBlank())
        {
            return Optional.empty();
        }

        try
        {
            return Optional.of(Instant.parse(rawValue));
        }
        catch (Exception ignored)
        {
            try
            {
                return Optional.of(LocalDateTime.parse(rawValue.replace(' ', 'T')).toInstant(ZoneOffset.UTC));
            }
            catch (Exception ignoredAgain)
            {
                return Optional.empty();
            }
        }
    }

    public String formatApiTime(String rawValue, TimeMode timeMode)
    {
        return parseApiInstant(rawValue)
            .map(instant -> DISPLAY_FORMAT.format(instant.atZone(resolveZone(timeMode))) + " " + suffix(timeMode))
            .orElse("-");
    }

    public String toApiQueryDateTime(LocalDateTime localDateTime)
    {
        if (localDateTime == null)
        {
            return null;
        }
        return API_QUERY_FORMAT.format(localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC));
    }

    public String toApiQueryEndOfDay(LocalDateTime localDateTime)
    {
        if (localDateTime == null)
        {
            return null;
        }
        return toApiQueryDateTime(localDateTime.withHour(23).withMinute(59).withSecond(59));
    }

    private ZoneId resolveZone(TimeMode timeMode)
    {
        return timeMode == TimeMode.UTC ? ZoneOffset.UTC : ZoneId.systemDefault();
    }

    private String suffix(TimeMode timeMode)
    {
        return timeMode == TimeMode.UTC ? "UTC" : "Local";
    }
}
