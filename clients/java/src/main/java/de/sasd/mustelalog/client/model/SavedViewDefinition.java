package de.sasd.mustelalog.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Locally stored saved view definition.
 */
public final class SavedViewDefinition
{
    private String name;
    private EventQueryFilter filter = new EventQueryFilter();
    private String sortColumn = "occurredAt";
    private boolean sortAscending = false;
    private Map<String, Boolean> visibleColumns = new LinkedHashMap<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EventQueryFilter getFilter() { return filter; }
    public void setFilter(EventQueryFilter filter) { this.filter = filter; }
    public String getSortColumn() { return sortColumn; }
    public void setSortColumn(String sortColumn) { this.sortColumn = sortColumn; }
    public boolean isSortAscending() { return sortAscending; }
    public void setSortAscending(boolean sortAscending) { this.sortAscending = sortAscending; }
    public Map<String, Boolean> getVisibleColumns() { return visibleColumns; }
    public void setVisibleColumns(Map<String, Boolean> visibleColumns) { this.visibleColumns = visibleColumns; }
    @Override public String toString() { return name; }
}
