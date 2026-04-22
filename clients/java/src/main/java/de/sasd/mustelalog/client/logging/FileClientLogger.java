package de.sasd.mustelalog.client.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * File-based diagnostics logger with simple size-based rotation.
 */
public final class FileClientLogger implements ClientLogger
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final Path logFile;
    private final long maxFileSizeBytes;
    private final int maxRetainedFiles;
    private final ClientLogLevel minimumLevel;

    public FileClientLogger(Path logFile, long maxFileSizeBytes, int maxRetainedFiles, ClientLogLevel minimumLevel)
    {
        this.logFile = logFile;
        this.maxFileSizeBytes = Math.max(64 * 1024L, maxFileSizeBytes);
        this.maxRetainedFiles = Math.max(1, maxRetainedFiles);
        this.minimumLevel = minimumLevel == null ? ClientLogLevel.INFORMATION : minimumLevel;
    }

    @Override
    public synchronized void log(LogEntry entry)
    {
        if (entry == null || !entry.getLevel().isEnabledFor(minimumLevel))
        {
            return;
        }

        try
        {
            Files.createDirectories(logFile.getParent());
            rotateIfNeeded();

            StringBuilder builder = new StringBuilder();
            builder.append(FORMATTER.format(entry.getTimestamp().atOffset(ZoneOffset.UTC)))
                .append(" [").append(entry.getLevel()).append("] ")
                .append(LogSanitizer.sanitizeText(entry.getMessage()));

            Map<String, Object> context = LogSanitizer.sanitizeContext(entry.getContext());
            if (!context.isEmpty())
            {
                builder.append(" | context=").append(context);
            }

            if (entry.getThrowable() != null)
            {
                builder.append(" | exception=")
                    .append(entry.getThrowable().getClass().getSimpleName())
                    .append(": ")
                    .append(LogSanitizer.sanitizeText(entry.getThrowable().getMessage()));
            }

            builder.append(System.lineSeparator());

            Files.writeString(
                logFile,
                builder.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        }
        catch (Exception ignored)
        {
            // File logging is intentionally fire-and-forget in V1.
        }
    }

    private void rotateIfNeeded() throws IOException
    {
        if (!Files.exists(logFile))
        {
            return;
        }

        long currentSize = Files.size(logFile);
        if (currentSize < maxFileSizeBytes)
        {
            return;
        }

        for (int index = maxRetainedFiles; index >= 1; index--)
        {
            Path current = rotatedPath(index);
            if (Files.exists(current))
            {
                if (index == maxRetainedFiles)
                {
                    Files.delete(current);
                }
                else
                {
                    Files.move(current, rotatedPath(index + 1));
                }
            }
        }

        Files.move(logFile, rotatedPath(1));
    }

    private Path rotatedPath(int index)
    {
        return logFile.resolveSibling(logFile.getFileName().toString() + "." + index);
    }
}
