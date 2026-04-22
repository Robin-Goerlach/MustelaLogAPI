package de.sasd.mustelalog.client.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the current filter state of the UI.
 */
public final class EventQueryFilter
{
    private LocalDateTime fromLocal;
    private LocalDateTime toLocal;
    private String sourceKey;
    private String hostname;
    private String service;
    private String severity;
    private String eventCategory;
    private String eventAction;
    private String eventOutcome;
    private String textSearch;
    private String correlationId;
    private String traceId;
    private String requestId;
    private String component;
    private String actorUserId;
    private String actorPrincipal;
    private String sessionHash;
    private String clientIp;
    private String serverIp;
    private boolean onlyWithPayload;
    private boolean onlyWithCorrelation;
    private boolean onlyWithActor;

    public LocalDateTime getFromLocal() { return fromLocal; }
    public void setFromLocal(LocalDateTime fromLocal) { this.fromLocal = fromLocal; }
    public LocalDateTime getToLocal() { return toLocal; }
    public void setToLocal(LocalDateTime toLocal) { this.toLocal = toLocal; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }
    public String getEventAction() { return eventAction; }
    public void setEventAction(String eventAction) { this.eventAction = eventAction; }
    public String getEventOutcome() { return eventOutcome; }
    public void setEventOutcome(String eventOutcome) { this.eventOutcome = eventOutcome; }
    public String getTextSearch() { return textSearch; }
    public void setTextSearch(String textSearch) { this.textSearch = textSearch; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }
    public String getActorPrincipal() { return actorPrincipal; }
    public void setActorPrincipal(String actorPrincipal) { this.actorPrincipal = actorPrincipal; }
    public String getSessionHash() { return sessionHash; }
    public void setSessionHash(String sessionHash) { this.sessionHash = sessionHash; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = serverIp; }
    public boolean isOnlyWithPayload() { return onlyWithPayload; }
    public void setOnlyWithPayload(boolean onlyWithPayload) { this.onlyWithPayload = onlyWithPayload; }
    public boolean isOnlyWithCorrelation() { return onlyWithCorrelation; }
    public void setOnlyWithCorrelation(boolean onlyWithCorrelation) { this.onlyWithCorrelation = onlyWithCorrelation; }
    public boolean isOnlyWithActor() { return onlyWithActor; }
    public void setOnlyWithActor(boolean onlyWithActor) { this.onlyWithActor = onlyWithActor; }

    /**
     * Returns a user-friendly summary of the active filters.
     *
     * @return key-value pairs that can be shown as filter chips
     */
    public Map<String, String> toChipMap()
    {
        Map<String, String> chips = new LinkedHashMap<>();
        add(chips, "Source", sourceKey);
        add(chips, "Hostname", hostname);
        add(chips, "Service", service);
        add(chips, "Severity", severity);
        add(chips, "Category", eventCategory);
        add(chips, "Action", eventAction);
        add(chips, "Outcome", eventOutcome);
        add(chips, "Text", textSearch);
        add(chips, "Correlation", correlationId);
        add(chips, "Trace", traceId);
        add(chips, "Request", requestId);
        add(chips, "Component", component);
        add(chips, "Actor User", actorUserId);
        add(chips, "Actor Principal", actorPrincipal);
        add(chips, "Session", sessionHash);
        add(chips, "Client IP", clientIp);
        add(chips, "Server IP", serverIp);
        if (onlyWithPayload) chips.put("Payload", "yes");
        if (onlyWithCorrelation) chips.put("Correlation fields", "yes");
        if (onlyWithActor) chips.put("Actor fields", "yes");
        if (fromLocal != null) chips.put("From", fromLocal.toString());
        if (toLocal != null) chips.put("To", toLocal.toString());
        return chips;
    }

    private void add(Map<String, String> chips, String label, String value)
    {
        if (value != null && !value.isBlank()) chips.put(label, value);
    }
}
