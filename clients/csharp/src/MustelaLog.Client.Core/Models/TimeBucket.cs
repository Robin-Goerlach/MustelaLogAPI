namespace MustelaLog.Client.Core.Models;

/// <summary>Einfacher Zeit-Bucket für den clientseitigen Zeitverlauf.</summary>
public sealed class TimeBucket
{
    public string Label { get; set; } = string.Empty;
    public int Count { get; set; }
}
