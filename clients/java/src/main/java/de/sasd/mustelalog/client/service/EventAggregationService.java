package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.model.SeveritySummary;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EventAggregationService {
    public List<SeveritySummary> summarizeBySeverity(List<LogEventRecord> records) {
        LinkedHashMap<String, Long> counts = new LinkedHashMap<>();
        for (LogEventRecord record : records) {
            String key = record.getSeverityText() == null || record.getSeverityText().isBlank() ? "(unknown)" : record.getSeverityText();
            counts.put(key, counts.getOrDefault(key, 0L) + 1L);
        }
        ArrayList<SeveritySummary> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) result.add(new SeveritySummary(entry.getKey(), entry.getValue()));
        result.sort(Comparator.comparing(SeveritySummary::severityText));
        return result;
    }
}
