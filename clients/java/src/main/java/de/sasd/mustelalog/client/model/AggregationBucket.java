package de.sasd.mustelalog.client.model;

/**
 * Simple aggregation bucket used in the UI.
 */
public final class AggregationBucket
{
    private final String key;
    private final long count;

    public AggregationBucket(String key, long count)
    {
        this.key = key;
        this.count = count;
    }

    public String getKey() { return key; }
    public long getCount() { return count; }
}
