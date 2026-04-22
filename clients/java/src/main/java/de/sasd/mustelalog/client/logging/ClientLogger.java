package de.sasd.mustelalog.client.logging;

import java.util.Collections;
import java.util.Map;

/**
 * Common logging interface used by the client.
 *
 * <p>The goal is not to outsmart existing logging frameworks. The goal is to keep the V1 client
 * small, understandable, and safe. This interface allows file logging and a live diagnostics buffer
 * to be combined without coupling UI code to file I/O.</p>
 */
public interface ClientLogger
{
    /**
     * Writes the supplied log entry if the logger accepts the level.
     *
     * @param entry entry to write
     */
    void log(LogEntry entry);

    /**
     * Writes a structured message.
     *
     * @param level log level
     * @param message message
     * @param context optional structured context
     */
    default void log(ClientLogLevel level, String message, Map<String, Object> context)
    {
        log(new LogEntry(java.time.Instant.now(), level, message, context == null ? Collections.emptyMap() : context, null));
    }

    /**
     * Writes a structured message with an attached exception.
     *
     * @param level log level
     * @param message message
     * @param context optional context
     * @param throwable optional throwable
     */
    default void log(ClientLogLevel level, String message, Map<String, Object> context, Throwable throwable)
    {
        log(new LogEntry(java.time.Instant.now(), level, message, context == null ? Collections.emptyMap() : context, throwable));
    }

    default void error(String message, Map<String, Object> context, Throwable throwable)
    {
        log(ClientLogLevel.ERROR, message, context, throwable);
    }

    default void warning(String message, Map<String, Object> context)
    {
        log(ClientLogLevel.WARNING, message, context);
    }

    default void information(String message, Map<String, Object> context)
    {
        log(ClientLogLevel.INFORMATION, message, context);
    }

    default void debug(String message, Map<String, Object> context)
    {
        log(ClientLogLevel.DEBUG, message, context);
    }
}
