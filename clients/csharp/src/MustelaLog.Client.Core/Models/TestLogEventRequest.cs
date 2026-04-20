namespace MustelaLog.Client.Core.Models;

/// <summary>Eingabemodell für einen per UI erzeugten Test-Logeintrag.</summary>
public sealed class TestLogEventRequest
{
    public string SeverityText { get; set; } = "Information";
    public int SeverityNumber { get; set; } = 9;
    public string Message { get; set; } = "Test event from desktop client";
    public string EventName { get; set; } = "desktop.test";
    public string EventCategory { get; set; } = "diagnostic";
    public string EventAction { get; set; } = "send_test_log";
    public string? EventOutcome { get; set; } = "success";
    public string? SourceEventId { get; set; }
    public string? HostName { get; set; }
    public string? ServiceName { get; set; }
    public string? ComponentName { get; set; }
    public string? CorrelationId { get; set; }
    public string? TraceId { get; set; }
    public string? RequestId { get; set; }
    public string AttributesJson { get; set; } = "{\n  \"origin\": \"desktop-client\"\n}";
}
