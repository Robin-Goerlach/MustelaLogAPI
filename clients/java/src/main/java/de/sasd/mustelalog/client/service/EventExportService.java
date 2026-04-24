package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.model.LogEventRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EventExportService {
    private final TimeService timeService;

    public EventExportService(TimeService timeService) {
        this.timeService = timeService;
    }

    public void exportToCsv(Path path, List<LogEventRecord> records) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        StringBuilder builder = new StringBuilder();
        builder.append("logEventId,occurredAt,severityNumber,severityText,sourceKey,sourceName,eventName,messageText,correlationId,traceId").append(System.lineSeparator());
        for (LogEventRecord record : records) {
            appendCsv(builder, record.getLogEventId()); builder.append(',');
            appendCsv(builder, record.getOccurredAt()); builder.append(',');
            appendCsv(builder, record.getSeverityNumber() == null ? "" : String.valueOf(record.getSeverityNumber())); builder.append(',');
            appendCsv(builder, record.getSeverityText()); builder.append(',');
            appendCsv(builder, record.getSourceKey()); builder.append(',');
            appendCsv(builder, record.getSourceName()); builder.append(',');
            appendCsv(builder, record.getEventName()); builder.append(',');
            appendCsv(builder, record.getMessageText()); builder.append(',');
            appendCsv(builder, record.getCorrelationId()); builder.append(',');
            appendCsv(builder, record.getTraceId()); builder.append(System.lineSeparator());
        }
        Files.writeString(path, builder.toString(), StandardCharsets.UTF_8);
    }

    public void exportToJson(Path path, List<LogEventRecord> records) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        ArrayList<Object> payload = new ArrayList<>();
        for (LogEventRecord record : records) payload.add(record.toMap());
        Files.writeString(path, SimpleJson.pretty(payload), StandardCharsets.UTF_8);
    }

    public String defaultFileName(String prefix, String extension) {
        String timestamp = timeService.currentUtcTimestamp().replace(':', '-').replace("T", "_").replace("Z", "");
        return prefix + "_" + timestamp + "." + extension;
    }

    private void appendCsv(StringBuilder builder, String value) {
        String text = value == null ? "" : value;
        boolean needsQuoting = text.contains(",") || text.contains("\n") || text.contains("\r") || text.contains("\"");
        if (!needsQuoting) { builder.append(text); return; }
        builder.append('"').append(text.replace("\"", "\"\"")).append('"');
    }
}
