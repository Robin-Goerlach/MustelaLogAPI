using MustelaLog.Client.Core.Models;

namespace MustelaLog.Client.Core.Services;

/// <summary>Erzeugt einfache clientseitige Aggregationen.</summary>
public sealed class AggregationService
{
    private readonly TimeDisplayService _timeDisplayService;

    public AggregationService(TimeDisplayService timeDisplayService)
    {
        _timeDisplayService = timeDisplayService;
    }

    public IReadOnlyList<CountBucket> CountBySeverity(IEnumerable<LogEventRecord> events) => CountBy(events, e => e.SeverityText);
    public IReadOnlyList<CountBucket> CountBySourceOrHost(IEnumerable<LogEventRecord> events) => CountBy(events, e => string.IsNullOrWhiteSpace(e.HostName) ? e.SourceName : $"{e.SourceName} / {e.HostName}");
    public IReadOnlyList<CountBucket> CountByService(IEnumerable<LogEventRecord> events) => CountBy(events, e => e.ServiceName);
    public IReadOnlyList<CountBucket> CountByCategory(IEnumerable<LogEventRecord> events) => CountBy(events, e => e.EventCategory);
    public IReadOnlyList<CountBucket> CountByOutcome(IEnumerable<LogEventRecord> events) => CountBy(events, e => e.EventOutcome);

    public IReadOnlyList<TimeBucket> CountOverTime(IEnumerable<LogEventRecord> events)
    {
        var parsedTimes = events
            .Select(e => _timeDisplayService.ParseApiTime(e.OccurredAt))
            .Where(v => v.HasValue)
            .Select(v => v!.Value)
            .OrderBy(v => v)
            .ToList();

        if (parsedTimes.Count == 0)
        {
            return Array.Empty<TimeBucket>();
        }

        var span = parsedTimes.Last() - parsedTimes.First();
        var bucketSize = ResolveBucketSize(span);

        return parsedTimes
            .GroupBy(value => TruncateToBucket(value, bucketSize))
            .OrderBy(group => group.Key)
            .Select(group => new TimeBucket
            {
                Label = group.Key.ToString("MM-dd HH:mm"),
                Count = group.Count()
            })
            .ToList();
    }

    private static IReadOnlyList<CountBucket> CountBy(IEnumerable<LogEventRecord> events, Func<LogEventRecord, string?> keySelector)
    {
        return events
            .GroupBy(e => NormalizeKey(keySelector(e)))
            .Select(group => new CountBucket { Key = group.Key, Count = group.Count() })
            .OrderByDescending(b => b.Count)
            .ThenBy(b => b.Key)
            .ToList();
    }

    private static string NormalizeKey(string? value) => string.IsNullOrWhiteSpace(value) ? "(empty)" : value.Trim();

    private static TimeSpan ResolveBucketSize(TimeSpan span)
    {
        if (span <= TimeSpan.FromMinutes(15)) return TimeSpan.FromMinutes(1);
        if (span <= TimeSpan.FromHours(6)) return TimeSpan.FromMinutes(15);
        if (span <= TimeSpan.FromDays(2)) return TimeSpan.FromHours(1);
        return TimeSpan.FromDays(1);
    }

    private static DateTimeOffset TruncateToBucket(DateTimeOffset value, TimeSpan bucketSize)
    {
        var ticks = value.UtcTicks / bucketSize.Ticks * bucketSize.Ticks;
        return new DateTimeOffset(ticks, TimeSpan.Zero);
    }
}
