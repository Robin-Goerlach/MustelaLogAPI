package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.model.AggregationBucket;
import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.model.TimeBucket;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds simple client-side aggregations for the currently visible result set.
 */
public final class AggregationService
{
    private final TimeDisplayService timeDisplayService;

    public AggregationService(TimeDisplayService timeDisplayService)
    {
        this.timeDisplayService = timeDisplayService;
    }

    public List<AggregationBucket> aggregateBySeverity(List<LogEventRecord> items) { return aggregate(items, item -> defaultText(item.getSeverityText(), "-")); }
    public List<AggregationBucket> aggregateBySource(List<LogEventRecord> items) { return aggregate(items, item -> defaultText(firstNonBlank(item.getSourceName(), item.getSourceKey(), item.getHostName()), "-")); }
    public List<AggregationBucket> aggregateByService(List<LogEventRecord> items) { return aggregate(items, item -> defaultText(item.getServiceName(), "-")); }
    public List<AggregationBucket> aggregateByCategory(List<LogEventRecord> items) { return aggregate(items, item -> defaultText(item.getEventCategory(), "-")); }
    public List<AggregationBucket> aggregateByOutcome(List<LogEventRecord> items) { return aggregate(items, item -> defaultText(item.getEventOutcome(), "-")); }

    public List<TimeBucket> aggregateOverTime(List<LogEventRecord> items)
    {
        Map<String, Long> grouped = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00").withZone(ZoneOffset.UTC);
        for (LogEventRecord item : items)
        {
            String label = timeDisplayService.parseApiInstant(item.getOccurredAt()).map(formatter::format).orElse("unknown");
            grouped.merge(label, 1L, Long::sum);
        }
        return grouped.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> new TimeBucket(entry.getKey(), entry.getValue())).toList();
    }

    private List<AggregationBucket> aggregate(List<LogEventRecord> items, KeyExtractor extractor)
    {
        Map<String, Long> grouped = new LinkedHashMap<>();
        for (LogEventRecord item : items)
        {
            grouped.merge(extractor.extract(item), 1L, Long::sum);
        }
        List<AggregationBucket> buckets = new ArrayList<>();
        for (Map.Entry<String, Long> entry : grouped.entrySet())
        {
            buckets.add(new AggregationBucket(entry.getKey(), entry.getValue()));
        }
        buckets.sort(Comparator.comparingLong(AggregationBucket::getCount).reversed().thenComparing(AggregationBucket::getKey));
        return buckets;
    }

    private static String defaultText(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private static String firstNonBlank(String... candidates)
    {
        for (String candidate : candidates) { if (candidate != null && !candidate.isBlank()) return candidate; }
        return null;
    }

    @FunctionalInterface
    private interface KeyExtractor { String extract(LogEventRecord item); }
}
