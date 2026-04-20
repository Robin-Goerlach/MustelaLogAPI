namespace MustelaLog.Client.Core.Models;

/// <summary>Beschreibt die Filterkriterien für die Eventsuche.</summary>
public sealed class LogQueryFilter
{
    public DateTimeOffset? FromUtc { get; set; }
    public DateTimeOffset? ToUtc { get; set; }
    public string? SourceKey { get; set; }
    public string? Hostname { get; set; }
    public string? Service { get; set; }
    public string? SeverityText { get; set; }
    public string? EventCategory { get; set; }
    public string? EventAction { get; set; }
    public string? EventOutcome { get; set; }
    public string? TextSearch { get; set; }
    public string? CorrelationId { get; set; }
    public string? TraceId { get; set; }
    public string? RequestId { get; set; }
    public string? Component { get; set; }
    public string? ActorUserId { get; set; }
    public string? ActorPrincipal { get; set; }
    public string? SessionHash { get; set; }
    public string? ClientIp { get; set; }
    public string? ServerIp { get; set; }
    public bool OnlyWithPayload { get; set; }
    public bool OnlyWithCorrelation { get; set; }
    public bool OnlyWithActor { get; set; }

    public LogQueryFilter Clone() => (LogQueryFilter)MemberwiseClone();

    public IReadOnlyList<string> ToChips()
    {
        var chips = new List<string>();
        if (FromUtc is not null) chips.Add($"from: {FromUtc:yyyy-MM-dd HH:mm} UTC");
        if (ToUtc is not null) chips.Add($"to: {ToUtc:yyyy-MM-dd HH:mm} UTC");
        AddIfSet(chips, "source", SourceKey);
        AddIfSet(chips, "host", Hostname);
        AddIfSet(chips, "service", Service);
        AddIfSet(chips, "severity", SeverityText);
        AddIfSet(chips, "category", EventCategory);
        AddIfSet(chips, "action", EventAction);
        AddIfSet(chips, "outcome", EventOutcome);
        AddIfSet(chips, "text", TextSearch);
        AddIfSet(chips, "correlation", CorrelationId);
        AddIfSet(chips, "trace", TraceId);
        AddIfSet(chips, "request", RequestId);
        AddIfSet(chips, "component", Component);
        AddIfSet(chips, "actorUser", ActorUserId);
        AddIfSet(chips, "actor", ActorPrincipal);
        AddIfSet(chips, "session", SessionHash);
        AddIfSet(chips, "clientIp", ClientIp);
        AddIfSet(chips, "serverIp", ServerIp);
        if (OnlyWithPayload) chips.Add("has:payload");
        if (OnlyWithCorrelation) chips.Add("has:correlation");
        if (OnlyWithActor) chips.Add("has:actor");
        return chips;
    }

    private static void AddIfSet(List<string> chips, string label, string? value)
    {
        if (!string.IsNullOrWhiteSpace(value))
        {
            chips.Add($"{label}: {value}");
        }
    }
}
