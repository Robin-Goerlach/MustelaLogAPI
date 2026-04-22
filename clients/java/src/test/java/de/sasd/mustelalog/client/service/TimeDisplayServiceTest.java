package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.model.TimeMode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TimeDisplayService}.
 */
class TimeDisplayServiceTest
{
    /**
     * Ensures that API time formatting appends a mode suffix.
     */
    @Test
    void formatApiTimeAppendsModeSuffix()
    {
        TimeDisplayService service = new TimeDisplayService();
        String formatted = service.formatApiTime("2026-04-21T12:00:00Z", TimeMode.UTC);
        assertTrue(formatted.endsWith("UTC"));
    }

    /**
     * Ensures that end-of-day conversion keeps the selected day inclusive.
     */
    @Test
    void toApiQueryEndOfDayIncludesDayEnd()
    {
        TimeDisplayService service = new TimeDisplayService();
        String formatted = service.toApiQueryEndOfDay(LocalDateTime.of(2026, 4, 21, 0, 0));
        assertTrue(formatted.endsWith("23:59:59"));
    }
}
