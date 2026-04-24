package de.sasd.mustelalog.client.tests;

import de.sasd.mustelalog.client.service.TimeService;

public final class TimeServiceSelfTest {
    private TimeServiceSelfTest() {
    }

    public static void run() {
        TimeService service = new TimeService();
        String from = service.normalizeQueryFrom("2026-04-24");
        String to = service.normalizeQueryTo("2026-04-24");
        if (from.isBlank() || to.isBlank()) {
            throw new IllegalStateException("Normalized timestamps must not be blank.");
        }
        if (!service.currentUtcTimestamp().endsWith("Z")) {
            throw new IllegalStateException("UTC timestamps should end with 'Z'.");
        }
        String display = service.formatForDisplay("2026-04-24T10:15:30Z");
        if (!display.startsWith("2026-04-24")) {
            throw new IllegalStateException("Display timestamp should include the date.");
        }
    }
}
