package de.sasd.mustelalog.client.tests;

import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.service.EventExportService;
import de.sasd.mustelalog.client.service.TimeService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExportSelfTest {
    private ExportSelfTest() {
    }

    public static void run() throws Exception {
        EventExportService service = new EventExportService(new TimeService());
        LogEventRecord record = new LogEventRecord();
        record.setLogEventId("evt-1");
        record.setOccurredAt("2026-04-24T10:15:30Z");
        record.setSeverityNumber(9);
        record.setSeverityText("INFO");
        record.setSourceKey("java-client");
        record.setSourceName("Java Client");
        record.setEventName("client.test");
        record.setMessageText("integration test");
        record.setCorrelationId("corr-1");
        record.setTraceId("trace-1");

        Path tempDir = Files.createTempDirectory("mustela-export-test");
        Path csv = tempDir.resolve("events.csv");
        Path json = tempDir.resolve("events.json");
        service.exportToCsv(csv, List.of(record));
        service.exportToJson(json, List.of(record));

        String csvText = Files.readString(csv, StandardCharsets.UTF_8);
        String jsonText = Files.readString(json, StandardCharsets.UTF_8);
        if (!csvText.contains("evt-1") || !jsonText.contains("client.test")) {
            throw new IllegalStateException("Export output does not contain expected values.");
        }
    }
}
