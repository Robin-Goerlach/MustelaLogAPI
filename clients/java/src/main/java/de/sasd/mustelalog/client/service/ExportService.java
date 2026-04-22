package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.json.SimpleJson;
import de.sasd.mustelalog.client.model.LogEventRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles export of the currently visible result set.
 */
public final class ExportService
{
    public void exportCsv(List<LogEventRecord> items, Path path) throws IOException
    {
        Files.createDirectories(path.getParent());
        StringBuilder builder = new StringBuilder();
        builder.append("eventId,occurredAt,ingestedAt,severity,sourceKey,host,service,category,action,outcome,message,correlationId,traceId")
            .append(System.lineSeparator());

        for (LogEventRecord item : items)
        {
            builder.append(csv(item.getLogEventId())).append(',')
                .append(csv(item.getOccurredAt())).append(',')
                .append(csv(item.getIngestedAt())).append(',')
                .append(csv(item.getSeverityText())).append(',')
                .append(csv(item.getSourceKey())).append(',')
                .append(csv(item.getHostName())).append(',')
                .append(csv(item.getServiceName())).append(',')
                .append(csv(item.getEventCategory())).append(',')
                .append(csv(item.getEventAction())).append(',')
                .append(csv(item.getEventOutcome())).append(',')
                .append(csv(item.getMessageText())).append(',')
                .append(csv(item.getCorrelationId())).append(',')
                .append(csv(item.getTraceId())).append(System.lineSeparator());
        }
        Files.writeString(path, builder.toString(), StandardCharsets.UTF_8);
    }

    public void exportJson(List<LogEventRecord> items, Path path) throws IOException
    {
        Files.createDirectories(path.getParent());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (LogEventRecord item : items)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("logEventId", item.getLogEventId());
            row.put("occurredAt", item.getOccurredAt());
            row.put("observedAt", item.getObservedAt());
            row.put("receivedAt", item.getReceivedAt());
            row.put("ingestedAt", item.getIngestedAt());
            row.put("severityNumber", item.getSeverityNumber());
            row.put("severityText", item.getSeverityText());
            row.put("eventName", item.getEventName());
            row.put("eventCategory", item.getEventCategory());
            row.put("eventAction", item.getEventAction());
            row.put("eventOutcome", item.getEventOutcome());
            row.put("messageText", item.getMessageText());
            row.put("attributesJson", item.getAttributesJson());
            row.put("rawPayloadJson", item.getRawPayloadJson());
            row.put("sourceKey", item.getSourceKey());
            row.put("sourceName", item.getSourceName());
            row.put("hostName", item.getHostName());
            row.put("serviceName", item.getServiceName());
            row.put("componentName", item.getComponentName());
            row.put("actorUserId", item.getActorUserId());
            row.put("actorPrincipal", item.getActorPrincipal());
            row.put("sessionHashSha256", item.getSessionHashSha256());
            row.put("clientIp", item.getClientIp());
            row.put("serverIp", item.getServerIp());
            row.put("traceId", item.getTraceId());
            row.put("spanId", item.getSpanId());
            row.put("correlationId", item.getCorrelationId());
            row.put("requestCorrelationId", item.getRequestCorrelationId());
            row.put("classificationCode", item.getClassificationCode());
            row.put("retentionPolicyCode", item.getRetentionPolicyCode());
            row.put("legalHoldFlag", item.getLegalHoldFlag());
            row.put("canonicalHashSha256", item.getCanonicalHashSha256());
            row.put("previousHashSha256", item.getPreviousHashSha256());
            row.put("sourceSignature", item.getSourceSignature());
            row.put("signatureAlgorithm", item.getSignatureAlgorithm());
            rows.add(row);
        }
        Files.writeString(path, SimpleJson.stringifyPretty(rows), StandardCharsets.UTF_8);
    }

    private static String csv(String value)
    {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
