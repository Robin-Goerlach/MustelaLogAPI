package de.sasd.mustelalog.client.logging;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable diagnostics log entry.
 */
public final class LogEntry
{
    private final Instant timestamp;
    private final ClientLogLevel level;
    private final String message;
    private final Map<String, Object> context;
    private final Throwable throwable;

    /**
     * Creates a new log entry.
     *
     * @param timestamp timestamp of the entry
     * @param level severity level
     * @param message human-readable message
     * @param context optional structured context
     * @param throwable optional exception
     */
    public LogEntry(Instant timestamp, ClientLogLevel level, String message, Map<String, Object> context, Throwable throwable)
    {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.context = context;
        this.throwable = throwable;
    }

    public Instant getTimestamp() { return timestamp; }
    public ClientLogLevel getLevel() { return level; }
    public String getMessage() { return message; }
    public Map<String, Object> getContext() { return context; }
    public Throwable getThrowable() { return throwable; }
}
