package de.sasd.mustelalog.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record SourceSummary(
        String sourceId,
        String sourceKey,
        String sourceName,
        String sourceType,
        String environment,
        String hostName,
        String serviceName,
        boolean active,
        Map<String, Object> rawFields) {

    public Map<String, Object> toMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("sourceId", sourceId);
        map.put("sourceKey", sourceKey);
        map.put("sourceName", sourceName);
        map.put("sourceType", sourceType);
        map.put("environment", environment);
        map.put("hostName", hostName);
        map.put("serviceName", serviceName);
        map.put("active", active);
        if (rawFields != null && !rawFields.isEmpty()) map.put("rawFields", rawFields);
        return map;
    }
}
