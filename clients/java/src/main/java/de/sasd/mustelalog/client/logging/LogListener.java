package de.sasd.mustelalog.client.logging;

/**
 * Listener for live diagnostics entries.
 */
@FunctionalInterface
public interface LogListener
{
    /**
     * Handles a new in-memory log entry.
     *
     * @param entry new entry
     */
    void onEntryWritten(LogEntry entry);
}
