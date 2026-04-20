namespace MustelaLog.Client.Core.Models;

/// <summary>Zählt Treffer pro Dimension.</summary>
public sealed class CountBucket
{
    public string Key { get; set; } = string.Empty;
    public int Count { get; set; }
}
