package de.sasd.mustelalog.client.logging;

@FunctionalInterface
public interface DiagnosticsListener {
    void onLogEntry(LogEntry entry);
}
