package de.sasd.mustelalog.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TestLogEventRequest {
    private String occurredAt = "";
    private String observedAt = "";
    private int severityNumber = 9;
    private String severityText = "INFO";
    private String message = "";
    private String eventName = "";
    private String eventCategory = "";
    private String eventAction = "";
    private String eventOutcome = "";
    private String hostName = "";
    private String serviceName = "";
    private String componentName = "";
    private String moduleName = "";
    private String processName = "";
    private String threadId = "";
    private String actorUserId = "";
    private String actorPrincipal = "";
    private String clientIp = "";
    private String serverIp = "";
    private String traceId = "";
    private String correlationId = "";
    private String requestId = "";
    private String classification = "internal";
    private String retentionPolicy = "standard";
    private String sourceSignature = "";
    private String signatureAlgorithm = "";
    private String tenantScopeKey = "";
    private String attributesJsonText = "{}";

    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = normalize(occurredAt); }
    public String getObservedAt() { return observedAt; }
    public void setObservedAt(String observedAt) { this.observedAt = normalize(observedAt); }
    public int getSeverityNumber() { return severityNumber; }
    public void setSeverityNumber(int severityNumber) { this.severityNumber = severityNumber; }
    public String getSeverityText() { return severityText; }
    public void setSeverityText(String severityText) { this.severityText = normalize(severityText); }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = normalize(message); }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = normalize(eventName); }
    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = normalize(eventCategory); }
    public String getEventAction() { return eventAction; }
    public void setEventAction(String eventAction) { this.eventAction = normalize(eventAction); }
    public String getEventOutcome() { return eventOutcome; }
    public void setEventOutcome(String eventOutcome) { this.eventOutcome = normalize(eventOutcome); }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = normalize(hostName); }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = normalize(serviceName); }
    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = normalize(componentName); }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = normalize(moduleName); }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = normalize(processName); }
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = normalize(threadId); }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = normalize(actorUserId); }
    public String getActorPrincipal() { return actorPrincipal; }
    public void setActorPrincipal(String actorPrincipal) { this.actorPrincipal = normalize(actorPrincipal); }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = normalize(clientIp); }
    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = normalize(serverIp); }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = normalize(traceId); }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = normalize(correlationId); }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = normalize(requestId); }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = normalize(classification); }
    public String getRetentionPolicy() { return retentionPolicy; }
    public void setRetentionPolicy(String retentionPolicy) { this.retentionPolicy = normalize(retentionPolicy); }
    public String getSourceSignature() { return sourceSignature; }
    public void setSourceSignature(String sourceSignature) { this.sourceSignature = normalize(sourceSignature); }
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = normalize(signatureAlgorithm); }
    public String getTenantScopeKey() { return tenantScopeKey; }
    public void setTenantScopeKey(String tenantScopeKey) { this.tenantScopeKey = normalize(tenantScopeKey); }
    public String getAttributesJsonText() { return attributesJsonText; }
    public void setAttributesJsonText(String attributesJsonText) { this.attributesJsonText = attributesJsonText == null || attributesJsonText.isBlank() ? "{}" : attributesJsonText.trim(); }

    public Map<String, Object> toMap(Object attributesObject) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("occurredAt", occurredAt);
        map.put("observedAt", observedAt);
        map.put("severityNumber", severityNumber);
        map.put("severityText", severityText);
        map.put("message", message);
        putIfNotBlank(map, "eventName", eventName);
        putIfNotBlank(map, "eventCategory", eventCategory);
        putIfNotBlank(map, "eventAction", eventAction);
        putIfNotBlank(map, "eventOutcome", eventOutcome);
        putIfNotBlank(map, "hostName", hostName);
        putIfNotBlank(map, "serviceName", serviceName);
        putIfNotBlank(map, "componentName", componentName);
        putIfNotBlank(map, "moduleName", moduleName);
        putIfNotBlank(map, "processName", processName);
        putIfNotBlank(map, "threadId", threadId);
        putIfNotBlank(map, "actorUserId", actorUserId);
        putIfNotBlank(map, "actorPrincipal", actorPrincipal);
        putIfNotBlank(map, "clientIp", clientIp);
        putIfNotBlank(map, "serverIp", serverIp);
        putIfNotBlank(map, "traceId", traceId);
        putIfNotBlank(map, "correlationId", correlationId);
        putIfNotBlank(map, "requestId", requestId);
        putIfNotBlank(map, "classification", classification);
        putIfNotBlank(map, "retentionPolicy", retentionPolicy);
        putIfNotBlank(map, "sourceSignature", sourceSignature);
        putIfNotBlank(map, "signatureAlgorithm", signatureAlgorithm);
        putIfNotBlank(map, "tenantScopeKey", tenantScopeKey);
        map.put("attributes", attributesObject == null ? Map.of() : attributesObject);
        return map;
    }

    private void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }

    private String normalize(String value) { return value == null ? "" : value.trim(); }
}
