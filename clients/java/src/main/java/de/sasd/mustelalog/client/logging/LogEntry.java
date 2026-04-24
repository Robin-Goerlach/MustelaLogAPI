package de.sasd.mustelalog.client.logging;

import java.time.Instant;
import java.util.Map;

public record LogEntry(Instant timestamp, LogLevel level, String message, Map<String, Object> context) {
}
