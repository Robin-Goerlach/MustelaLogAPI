package de.sasd.mustelalog.client.logging;

import de.sasd.mustelalog.client.json.SimpleJson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientLogger {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);
    private final Path logFile;
    private final int maxEntries;
    private final ArrayDeque<LogEntry> entries = new ArrayDeque<>();
    private final List<DiagnosticsListener> listeners = new ArrayList<>();

    public ClientLogger(Path logFile, int maxEntries) throws IOException {
        this.logFile = logFile;
        this.maxEntries = Math.max(100, maxEntries);
        Path parent = logFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(logFile)) {
            Files.createFile(logFile);
        }
    }

    public synchronized void debug(String message, Map<String, Object> context) { log(LogLevel.DEBUG, message, context, null); }
    public synchronized void information(String message, Map<String, Object> context) { log(LogLevel.INFORMATION, message, context, null); }
    public synchronized void warning(String message, Map<String, Object> context) { log(LogLevel.WARNING, message, context, null); }
    public synchronized void error(String message, Map<String, Object> context, Throwable throwable) { log(LogLevel.ERROR, message, context, throwable); }
    public synchronized void addListener(DiagnosticsListener listener) { listeners.add(listener); }
    public synchronized List<LogEntry> snapshot() { return Collections.unmodifiableList(new ArrayList<>(entries)); }

    private void log(LogLevel level, String message, Map<String, Object> context, Throwable throwable) {
        Instant now = Instant.now();
        Map<String, Object> normalizedContext = new LinkedHashMap<>();
        if (context != null) normalizedContext.putAll(context);
        if (throwable != null) {
            normalizedContext.put("exceptionType", throwable.getClass().getName());
            normalizedContext.put("exceptionMessage", throwable.getMessage());
        }
        LogEntry entry = new LogEntry(now, level, message, normalizedContext);
        entries.addLast(entry);
        while (entries.size() > maxEntries) entries.removeFirst();
        writeToFile(entry);
        for (DiagnosticsListener listener : listeners) listener.onLogEntry(entry);
    }

    private void writeToFile(LogEntry entry) {
        String line = TIMESTAMP.format(entry.timestamp()) + " [" + entry.level() + "] " + entry.message()
                + (entry.context().isEmpty() ? "" : " | " + SimpleJson.stringify(entry.context())) + System.lineSeparator();
        try {
            Files.writeString(logFile, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException ignored) {
        }
    }
}
