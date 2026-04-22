package de.sasd.mustelalog.client.model;

/**
 * Simple time series bucket for the current result set.
 */
public final class TimeBucket
{
    private final String label;
    private final long count;

    public TimeBucket(String label, long count)
    {
        this.label = label;
        this.count = count;
    }

    public String getLabel() { return label; }
    public long getCount() { return count; }
}
