package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.model.LogEventRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Builds local related-event views based on the currently loaded result set.
 */
public final class RelatedEventsService
{
    public List<LogEventRecord> byCorrelation(List<LogEventRecord> rows, LogEventRecord selected) { return filter(rows, selected, item -> equals(item.getCorrelationId(), selected.getCorrelationId())); }
    public List<LogEventRecord> byTrace(List<LogEventRecord> rows, LogEventRecord selected) { return filter(rows, selected, item -> equals(item.getTraceId(), selected.getTraceId())); }
    public List<LogEventRecord> byRequest(List<LogEventRecord> rows, LogEventRecord selected) { return filter(rows, selected, item -> equals(item.getRequestCorrelationId(), selected.getRequestCorrelationId())); }
    public List<LogEventRecord> bySource(List<LogEventRecord> rows, LogEventRecord selected) { return filter(rows, selected, item -> equals(item.getSourceKey(), selected.getSourceKey())); }
    public List<LogEventRecord> bySession(List<LogEventRecord> rows, LogEventRecord selected) { return filter(rows, selected, item -> equals(item.getSessionHashSha256(), selected.getSessionHashSha256())); }
    public List<LogEventRecord> byActor(List<LogEventRecord> rows, LogEventRecord selected) { return filter(rows, selected, item -> equals(item.getActorUserId(), selected.getActorUserId()) || equals(item.getActorPrincipal(), selected.getActorPrincipal())); }

    private List<LogEventRecord> filter(List<LogEventRecord> rows, LogEventRecord selected, Predicate<LogEventRecord> predicate)
    {
        List<LogEventRecord> result = new ArrayList<>();
        if (rows == null || selected == null) return result;
        for (LogEventRecord row : rows) if (row != null && predicate.test(row)) result.add(row);
        return result;
    }

    private boolean equals(String left, String right)
    {
        return left != null && !left.isBlank() && Objects.equals(left, right);
    }
}
