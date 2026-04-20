using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Core.Services;

/// <summary>Unterstützt die Suche nach zusammengehörigen Events.</summary>
public sealed class EventCorrelationService
{
    public IReadOnlyList<LogEventRecord> FindLocally(IEnumerable<LogEventRecord> items, LogEventRecord selected, string relationMode)
    {
        return relationMode.ToLowerInvariant() switch
        {
            "correlation" => items.Where(e => Same(e.CorrelationId, selected.CorrelationId)).ToList(),
            "trace" => items.Where(e => Same(e.TraceId, selected.TraceId)).ToList(),
            "request" => items.Where(e => Same(e.RequestCorrelationId, selected.RequestCorrelationId)).ToList(),
            "source" => items.Where(e => Same(e.SourceKey, selected.SourceKey)).ToList(),
            "session" => items.Where(e => Same(e.SessionHashSha256, selected.SessionHashSha256)).ToList(),
            "actor" => items.Where(e => Same(e.ActorPrincipal, selected.ActorPrincipal) || Same(e.ActorUserId, selected.ActorUserId)).ToList(),
            _ => Array.Empty<LogEventRecord>()
        };
    }

    private static bool Same(string? left, string? right)
        => !string.IsNullOrWhiteSpace(left)
           && !string.IsNullOrWhiteSpace(right)
           && string.Equals(left, right, StringComparison.OrdinalIgnoreCase);
}
