package de.sasd.mustelalog.client.model;

/**
 * Flat event DTO consumed by the Swing client.
 *
 * <p>The field list mirrors the response contract the client needs. It is intentionally independent
 * from any database schema. That keeps the desktop client stable even if the underlying storage is
 * refactored later.</p>
 */
public final class LogEventRecord
{
    private String logEventId;
    private String occurredAt;
    private String observedAt;
    private String receivedAt;
    private String ingestedAt;
    private Integer severityNumber;
    private String severityText;
    private String eventName;
    private String eventCategory;
    private String eventAction;
    private String eventOutcome;
    private String messageText;
    private Object attributesJson;
    private Object rawPayloadJson;
    private String sourceId;
    private String sourceKey;
    private String sourceName;
    private String sourceType;
    private String environmentCode;
    private String hostName;
    private String serviceName;
    private String componentName;
    private String moduleName;
    private String processName;
    private Integer processPid;
    private String threadId;
    private String actorUserId;
    private String actorPrincipal;
    private String sessionHashSha256;
    private String clientIp;
    private String serverIp;
    private String traceId;
    private String spanId;
    private String correlationId;
    private String requestCorrelationId;
    private String classificationCode;
    private String retentionPolicyCode;
    private Boolean legalHoldFlag;
    private String canonicalHashSha256;
    private String previousHashSha256;
    private String sourceSignature;
    private String signatureAlgorithm;

    public String getLogEventId() { return logEventId; }
    public void setLogEventId(String logEventId) { this.logEventId = logEventId; }
    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }
    public String getObservedAt() { return observedAt; }
    public void setObservedAt(String observedAt) { this.observedAt = observedAt; }
    public String getReceivedAt() { return receivedAt; }
    public void setReceivedAt(String receivedAt) { this.receivedAt = receivedAt; }
    public String getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(String ingestedAt) { this.ingestedAt = ingestedAt; }
    public Integer getSeverityNumber() { return severityNumber; }
    public void setSeverityNumber(Integer severityNumber) { this.severityNumber = severityNumber; }
    public String getSeverityText() { return severityText; }
    public void setSeverityText(String severityText) { this.severityText = severityText; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }
    public String getEventAction() { return eventAction; }
    public void setEventAction(String eventAction) { this.eventAction = eventAction; }
    public String getEventOutcome() { return eventOutcome; }
    public void setEventOutcome(String eventOutcome) { this.eventOutcome = eventOutcome; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public Object getAttributesJson() { return attributesJson; }
    public void setAttributesJson(Object attributesJson) { this.attributesJson = attributesJson; }
    public Object getRawPayloadJson() { return rawPayloadJson; }
    public void setRawPayloadJson(Object rawPayloadJson) { this.rawPayloadJson = rawPayloadJson; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getEnvironmentCode() { return environmentCode; }
    public void setEnvironmentCode(String environmentCode) { this.environmentCode = environmentCode; }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Integer getProcessPid() { return processPid; }
    public void setProcessPid(Integer processPid) { this.processPid = processPid; }
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }
    public String getActorPrincipal() { return actorPrincipal; }
    public void setActorPrincipal(String actorPrincipal) { this.actorPrincipal = actorPrincipal; }
    public String getSessionHashSha256() { return sessionHashSha256; }
    public void setSessionHashSha256(String sessionHashSha256) { this.sessionHashSha256 = sessionHashSha256; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = serverIp; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getRequestCorrelationId() { return requestCorrelationId; }
    public void setRequestCorrelationId(String requestCorrelationId) { this.requestCorrelationId = requestCorrelationId; }
    public String getClassificationCode() { return classificationCode; }
    public void setClassificationCode(String classificationCode) { this.classificationCode = classificationCode; }
    public String getRetentionPolicyCode() { return retentionPolicyCode; }
    public void setRetentionPolicyCode(String retentionPolicyCode) { this.retentionPolicyCode = retentionPolicyCode; }
    public Boolean getLegalHoldFlag() { return legalHoldFlag; }
    public void setLegalHoldFlag(Boolean legalHoldFlag) { this.legalHoldFlag = legalHoldFlag; }
    public String getCanonicalHashSha256() { return canonicalHashSha256; }
    public void setCanonicalHashSha256(String canonicalHashSha256) { this.canonicalHashSha256 = canonicalHashSha256; }
    public String getPreviousHashSha256() { return previousHashSha256; }
    public void setPreviousHashSha256(String previousHashSha256) { this.previousHashSha256 = previousHashSha256; }
    public String getSourceSignature() { return sourceSignature; }
    public void setSourceSignature(String sourceSignature) { this.sourceSignature = sourceSignature; }
    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
}
