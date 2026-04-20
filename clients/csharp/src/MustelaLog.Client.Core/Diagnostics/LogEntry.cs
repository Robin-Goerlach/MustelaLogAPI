using MustelaLog.Client.Core.Enums;

namespace MustelaLog.Client.Core.Diagnostics;

/// <summary>Repräsentiert einen einzelnen Diagnoseeintrag.</summary>
public sealed class LogEntry
{
    public DateTimeOffset TimestampUtc { get; init; } = DateTimeOffset.UtcNow;
    public ClientLogLevel Level { get; init; }
    public string Message { get; init; } = string.Empty;
    public string? ContextText { get; init; }
    public string? ExceptionText { get; init; }

    public string ToSingleLineText()
    {
        var baseText = $"[{TimestampUtc:O}] [{Level}] {Message}";
        if (!string.IsNullOrWhiteSpace(ContextText))
        {
            baseText += $" | ctx={ContextText}";
        }
        if (!string.IsNullOrWhiteSpace(ExceptionText))
        {
            baseText += $" | ex={ExceptionText}";
        }
        return baseText;
    }
}
