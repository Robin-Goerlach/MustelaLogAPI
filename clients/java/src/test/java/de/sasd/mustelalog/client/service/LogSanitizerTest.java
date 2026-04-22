package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.logging.LogSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for log sanitizing helpers.
 */
class LogSanitizerTest
{
    /**
     * Ensures that obvious bearer tokens are masked.
     */
    @Test
    void sanitizeTextMasksBearerToken()
    {
        String sanitized = LogSanitizer.sanitizeText("Authorization=Bearer abc123");
        assertFalse(sanitized.contains("abc123"));
    }
}
