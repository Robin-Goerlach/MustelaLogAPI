package de.sasd.mustelalog.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EventQueryFilter {
    private String sourceKey = "";
    private String severityText = "";
    private String traceId = "";
    private String correlationId = "";
    private String from = "";
    private String to = "";

    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = normalize(sourceKey); }
    public String getSeverityText() { return severityText; }
    public void setSeverityText(String severityText) { this.severityText = normalize(severityText); }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = normalize(traceId); }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = normalize(correlationId); }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = normalize(from); }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = normalize(to); }

    public Map<String, String> describeActiveFilters() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        putIfNotBlank(map, "sourceKey", sourceKey);
        putIfNotBlank(map, "severityText", severityText);
        putIfNotBlank(map, "traceId", traceId);
        putIfNotBlank(map, "correlationId", correlationId);
        putIfNotBlank(map, "from", from);
        putIfNotBlank(map, "to", to);
        return map;
    }

    private String normalize(String value) { return value == null ? "" : value.trim(); }
    private void putIfNotBlank(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }
}
