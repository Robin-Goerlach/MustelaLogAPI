package de.sasd.mustelalog.client.ui.table;

import de.sasd.mustelalog.client.model.LogEventRecord;
import de.sasd.mustelalog.client.model.TimeMode;
import de.sasd.mustelalog.client.service.TimeDisplayService;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for the main and related event grids.
 */
public final class EventTableModel extends AbstractTableModel
{
    private static final String[] COLUMNS =
    {
        "Occurred At",
        "Ingested At",
        "Severity",
        "Source / Host",
        "Service",
        "Category",
        "Action",
        "Outcome",
        "Message",
        "Correlation / Trace",
        "Event ID"
    };

    private final TimeDisplayService timeDisplayService;
    private final List<LogEventRecord> rows = new ArrayList<>();
    private TimeMode timeMode = TimeMode.LOCAL;

    public EventTableModel(TimeDisplayService timeDisplayService)
    {
        this.timeDisplayService = timeDisplayService;
    }

    public void setRows(List<LogEventRecord> items)
    {
        rows.clear();
        if (items != null) rows.addAll(items);
        fireTableDataChanged();
    }

    public List<LogEventRecord> getRows()
    {
        return new ArrayList<>(rows);
    }

    public LogEventRecord getRow(int rowIndex)
    {
        return rows.get(rowIndex);
    }

    public void setTimeMode(TimeMode timeMode)
    {
        this.timeMode = timeMode;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int column) { return COLUMNS[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        LogEventRecord item = rows.get(rowIndex);
        return switch (columnIndex)
        {
            case 0 -> timeDisplayService.formatApiTime(item.getOccurredAt(), timeMode);
            case 1 -> timeDisplayService.formatApiTime(item.getIngestedAt(), timeMode);
            case 2 -> item.getSeverityText();
            case 3 -> firstNonBlank(item.getSourceName(), item.getSourceKey(), item.getHostName());
            case 4 -> item.getServiceName();
            case 5 -> item.getEventCategory();
            case 6 -> item.getEventAction();
            case 7 -> item.getEventOutcome();
            case 8 -> abbreviate(item.getMessageText(), 110);
            case 9 -> abbreviate(firstNonBlank(item.getCorrelationId(), item.getTraceId()), 24);
            case 10 -> item.getLogEventId();
            default -> "";
        };
    }

    private String abbreviate(String text, int maxLength)
    {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 1) + "…";
    }

    private String firstNonBlank(String... candidates)
    {
        for (String candidate : candidates)
        {
            if (candidate != null && !candidate.isBlank()) return candidate;
        }
        return "";
    }
}
