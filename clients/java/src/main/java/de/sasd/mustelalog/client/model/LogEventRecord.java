package de.sasd.mustelalog.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LogEventRecord {
    private String logEventId = "";
    private String occurredAt = "";
    private String observedAt = "";
    private String receivedAt = "";
    private String ingestedAt = "";
    private Integer severityNumber;
    private String severityText = "";
    private String eventName = "";
    private String eventCategory = "";
    private String eventAction = "";
    private String eventOutcome = "";
    private String messageText = "";
    private Object attributesJson;
    private Object rawPayloadJson;
    private String sourceId = "";
    private String sourceKey = "";
    private String sourceName = "";
    private String sourceType = "";
    private String environmentCode = "";
    private String hostName = "";
    private String serviceName = "";
    private String componentName = "";
    private String moduleName = "";
    private String processName = "";
    private Integer processPid;
    private String threadId = "";
    private String actorUserId = "";
    private String actorPrincipal = "";
    private String sessionHashSha256 = "";
    private String clientIp = "";
    private String serverIp = "";
    private String traceId = "";
    private String correlationId = "";
    private String requestCorrelationId = "";
    private String classificationCode = "";
    private String retentionPolicyCode = "";
    private String canonicalHashSha256 = "";
    private String previousHashSha256 = "";
    private String sourceSignature = "";
    private String signatureAlgorithm = "";
    private Map<String, Object> rawFields = Map.of();

    public String getLogEventId() { return logEventId; }
    public void setLogEventId(String logEventId) { this.logEventId = safe(logEventId); }
    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = safe(occurredAt); }
    public String getObservedAt() { return observedAt; }
    public void setObservedAt(String observedAt) { this.observedAt = safe(observedAt); }
    public String getReceivedAt() { return receivedAt; }
    public void setReceivedAt(String receivedAt) { this.receivedAt = safe(receivedAt); }
    public String getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(String ingestedAt) { this.ingestedAt = safe(ingestedAt); }
    public Integer getSeverityNumber() { return severityNumber; }
    public void setSeverityNumber(Integer severityNumber) { this.severityNumber = severityNumber; }
    public String getSeverityText() { return severityText; }
    public void setSeverityText(String severityText) { this.severityText = safe(severityText); }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = safe(eventName); }
    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = safe(eventCategory); }
    public String getEventAction() { return eventAction; }
    public void setEventAction(String eventAction) { this.eventAction = safe(eventAction); }
    public String getEventOutcome() { return eventOutcome; }
    public void setEventOutcome(String eventOutcome) { this.eventOutcome = safe(eventOutcome); }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = safe(messageText); }
    public Object getAttributesJson() { return attributesJson; }
    public void setAttributesJson(Object attributesJson) { this.attributesJson = attributesJson; }
    public Object getRawPayloadJson() { return rawPayloadJson; }
    public void setRawPayloadJson(Object rawPayloadJson) { this.rawPayloadJson = rawPayloadJson; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = safe(sourceId); }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = safe(sourceKey); }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = safe(sourceName); }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = safe(sourceType); }
    public String getEnvironmentCode() { return environmentCode; }
    public void setEnvironmentCode(String environmentCode) { this.environmentCode = safe(environmentCode); }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = safe(hostName); }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = safe(serviceName); }
    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = safe(componentName); }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = safe(moduleName); }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = safe(processName); }
    public Integer getProcessPid() { return processPid; }
    public void setProcessPid(Integer processPid) { this.processPid = processPid; }
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = safe(threadId); }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = safe(actorUserId); }
    public String getActorPrincipal() { return actorPrincipal; }
    public void setActorPrincipal(String actorPrincipal) { this.actorPrincipal = safe(actorPrincipal); }
    public String getSessionHashSha256() { return sessionHashSha256; }
    public void setSessionHashSha256(String sessionHashSha256) { this.sessionHashSha256 = safe(sessionHashSha256); }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = safe(clientIp); }
    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = safe(serverIp); }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = safe(traceId); }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = safe(correlationId); }
    public String getRequestCorrelationId() { return requestCorrelationId; }
    public void setRequestCorrelationId(String requestCorrelationId) { this.requestCorrelationId = safe(requestCorrelationId); }
    public String getClassificationCode() { return classificationCode; }
    public void setClassificationCode(String classificationCode) { this.classificationCode = safe(classificationCode); }
    public String getRetentionPolicyCode() { return retentionPolicyCode; }
    public void setRetentionPolicyCode(String retentionPolicyCode) { this.retentionPolicyCode = safe(retentionPolicyCode); }
    public String getCanonicalHashSha256() { return canonicalHashSha256; }
    public void setCanonicalHashSha256(String canonicalHashSha256) { this.canonicalHashSha256 = safe(canonicalHashSha256); }
    public String getPreviousHashSha256() { return previousHashSha256; }
    public void setPreviousHashSha256(String previousHashSha256) { this.previousHashSha256 = safe(previousHashSha256); }
    public String getSourceSignature() { return sourceSignature; }
    public void setSourceSignature(String sourceSignature) { this.sourceSignature = safe(sourceSignature); }
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = safe(signatureAlgorithm); }
    public Map<String, Object> getRawFields() { return rawFields; }
    public void setRawFields(Map<String, Object> rawFields) { this.rawFields = rawFields == null ? Map.of() : rawFields; }

    public Map<String, Object> toMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("logEventId", logEventId);
        map.put("occurredAt", occurredAt);
        map.put("observedAt", observedAt);
        map.put("receivedAt", receivedAt);
        map.put("ingestedAt", ingestedAt);
        map.put("severityNumber", severityNumber);
        map.put("severityText", severityText);
        map.put("eventName", eventName);
        map.put("eventCategory", eventCategory);
        map.put("eventAction", eventAction);
        map.put("eventOutcome", eventOutcome);
        map.put("messageText", messageText);
        map.put("sourceId", sourceId);
        map.put("sourceKey", sourceKey);
        map.put("sourceName", sourceName);
        map.put("sourceType", sourceType);
        map.put("environmentCode", environmentCode);
        map.put("hostName", hostName);
        map.put("serviceName", serviceName);
        map.put("componentName", componentName);
        map.put("moduleName", moduleName);
        map.put("processName", processName);
        map.put("processPid", processPid);
        map.put("threadId", threadId);
        map.put("actorUserId", actorUserId);
        map.put("actorPrincipal", actorPrincipal);
        map.put("sessionHashSha256", sessionHashSha256);
        map.put("clientIp", clientIp);
        map.put("serverIp", serverIp);
        map.put("traceId", traceId);
        map.put("correlationId", correlationId);
        map.put("requestCorrelationId", requestCorrelationId);
        map.put("classificationCode", classificationCode);
        map.put("retentionPolicyCode", retentionPolicyCode);
        map.put("canonicalHashSha256", canonicalHashSha256);
        map.put("previousHashSha256", previousHashSha256);
        map.put("sourceSignature", sourceSignature);
        map.put("signatureAlgorithm", signatureAlgorithm);
        map.put("attributes", attributesJson);
        map.put("rawPayloadJson", rawPayloadJson);
        if (!rawFields.isEmpty()) map.put("rawFields", rawFields);
        return map;
    }

    private String safe(String value) { return value == null ? "" : value; }
}
