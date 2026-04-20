namespace MustelaLog.Client.Core.Models;

/// <summary>Ergebnis einer Related-Events-Abfrage.</summary>
public sealed class RelatedEventsResult
{
    public string Title { get; set; } = string.Empty;
    public bool UsedServerQuery { get; set; }
    public IReadOnlyList<LogEventRecord> Items { get; set; } = Array.Empty<LogEventRecord>();
}
