package de.sasd.mustelalog.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged event response.
 */
public final class EventListResponse
{
    private final List<LogEventRecord> items;
    private final int total;
    private final int page;
    private final int pageSize;

    public EventListResponse(List<LogEventRecord> items, int total, int page, int pageSize)
    {
        this.items = new ArrayList<>(items);
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<LogEventRecord> getItems() { return new ArrayList<>(items); }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
}
