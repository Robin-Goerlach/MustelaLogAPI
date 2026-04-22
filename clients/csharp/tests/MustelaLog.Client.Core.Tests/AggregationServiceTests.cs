using Xunit;
using MustelaLog.Client.Core.Models;
using MustelaLog.Client.Core.Services;

namespace MustelaLog.Client.Core.Tests;

/// <summary>
/// Prüft die clientseitigen Aggregationshilfen.
/// Diese Tests sichern die spätere Drill-down- und Diagrammfunktion ab,
/// ohne dafür WPF oder HTTP-Kommunikation zu benötigen.
/// </summary>
public sealed class AggregationServiceTests
{
    private readonly AggregationService _service = new(new TimeDisplayService());

    [Fact]
    public void CountBySeverity_GroupsAndOrdersDescendingByCount()
    {
        var events = new[]
        {
            CreateEvent("ERROR"),
            CreateEvent("ERROR"),
            CreateEvent("WARN"),
            CreateEvent(null)
        };

        var result = _service.CountBySeverity(events).ToList();

        Assert.Equal("ERROR", result[0].Key);
        Assert.Equal(2, result[0].Count);
        Assert.Equal("(empty)", result[2].Key);
        Assert.Equal(1, result[2].Count);
    }

    [Fact]
    public void CountOverTime_UsesExpectedBucketsForShortRange()
    {
        var events = new[]
        {
            CreateEvent("INFO", "2026-04-20T10:00:02Z"),
            CreateEvent("INFO", "2026-04-20T10:00:40Z"),
            CreateEvent("INFO", "2026-04-20T10:01:10Z")
        };

        var result = _service.CountOverTime(events).ToList();

        Assert.Equal(2, result.Count);
        Assert.Equal(2, result[0].Count);
        Assert.Equal(1, result[1].Count);
    }

    [Fact]
    public void CountBySourceOrHost_PrefersCombinedLabelWhenHostExists()
    {
        var events = new[]
        {
            new LogEventRecord { SourceName = "api", HostName = "app-01" },
            new LogEventRecord { SourceName = "api", HostName = "app-01" },
            new LogEventRecord { SourceName = "worker" }
        };

        var result = _service.CountBySourceOrHost(events).ToList();

        Assert.Equal("api / app-01", result[0].Key);
        Assert.Equal(2, result[0].Count);
        Assert.Equal("worker", result[1].Key);
    }

    private static LogEventRecord CreateEvent(string? severity, string occurredAt = "2026-04-20T10:00:00Z")
    {
        return new LogEventRecord
        {
            SeverityText = severity,
            OccurredAt = occurredAt,
            SourceName = "api"
        };
    }
}
