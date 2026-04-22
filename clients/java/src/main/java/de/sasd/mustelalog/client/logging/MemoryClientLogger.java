package de.sasd.mustelalog.client.logging;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory diagnostics logger used for the optional live diagnostics dialog.
 */
public final class MemoryClientLogger implements ClientLogger
{
    private final int maxEntries;
    private final ClientLogLevel minimumLevel;
    private final Deque<LogEntry> entries;
    private final List<LogListener> listeners;

    /**
     * Creates a new memory logger.
     *
     * @param maxEntries maximum number of retained entries
     * @param minimumLevel minimum level that should be stored
     */
    public MemoryClientLogger(int maxEntries, ClientLogLevel minimumLevel)
    {
        this.maxEntries = Math.max(10, maxEntries);
        this.minimumLevel = minimumLevel == null ? ClientLogLevel.INFORMATION : minimumLevel;
        this.entries = new ArrayDeque<>(this.maxEntries);
        this.listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public synchronized void log(LogEntry entry)
    {
        if (entry == null || !entry.getLevel().isEnabledFor(minimumLevel))
        {
            return;
        }

        LogEntry sanitized = new LogEntry(
            entry.getTimestamp(),
            entry.getLevel(),
            LogSanitizer.sanitizeText(entry.getMessage()),
            LogSanitizer.sanitizeContext(entry.getContext()),
            entry.getThrowable());

        if (entries.size() >= maxEntries)
        {
            entries.removeFirst();
        }
        entries.addLast(sanitized);

        for (LogListener listener : listeners)
        {
            try
            {
                listener.onEntryWritten(sanitized);
            }
            catch (Exception ignored)
            {
                // Diagnostics logging must never bring down the client.
            }
        }
    }

    public synchronized List<LogEntry> snapshot()
    {
        return new ArrayList<>(entries);
    }

    public synchronized void clear()
    {
        entries.clear();
    }

    public void addListener(LogListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(LogListener listener)
    {
        listeners.remove(listener);
    }
}
