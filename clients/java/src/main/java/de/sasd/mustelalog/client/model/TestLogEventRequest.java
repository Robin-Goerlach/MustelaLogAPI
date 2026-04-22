package de.sasd.mustelalog.client.model;

/**
 * User-entered test log event request.
 */
public final class TestLogEventRequest
{
    private String severityText = "INFORMATION";
    private int severityNumber = 9;
    private String message = "Desktop client test event";
    private String eventName = "desktop.client.test";
    private String eventCategory = "diagnostics";
    private String eventAction = "manual-test";
    private String sourceKey = "";
    private String correlationId = "";
    private String attributesJsonText = "{\"client\":\"MustelaLog Swing Client\"}";

    public String getSeverityText() { return severityText; }
    public void setSeverityText(String severityText) { this.severityText = severityText; }
    public int getSeverityNumber() { return severityNumber; }
    public void setSeverityNumber(int severityNumber) { this.severityNumber = severityNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }
    public String getEventAction() { return eventAction; }
    public void setEventAction(String eventAction) { this.eventAction = eventAction; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getAttributesJsonText() { return attributesJsonText; }
    public void setAttributesJsonText(String attributesJsonText) { this.attributesJsonText = attributesJsonText; }
}
