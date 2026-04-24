package de.sasd.mustelalog.client.ui;

import de.sasd.mustelalog.client.model.LogEventRecord;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Swing table model for the event overview grid.
 *
 * The model intentionally exposes only the columns that are reliably useful in the
 * overview. The full event payload remains available through {@link LogEventRecord}
 * and is rendered in the detail pane.
 */
public final class EventTableModel extends AbstractTableModel {
    private final String[] columns = {
            "Occurred At",
            "Severity",
            "Source Key",
            "Source Name",
            "Event Name",
            "Message",
            "Correlation ID",
            "Trace ID",
            "Event ID"
    };

    private List<LogEventRecord> rows = new ArrayList<>();

    public void setRows(List<LogEventRecord> rows) {
        this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public LogEventRecord getRow(int modelRowIndex) {
        if (modelRowIndex < 0 || modelRowIndex >= rows.size()) {
            return null;
        }
        return rows.get(modelRowIndex);
    }

    public List<LogEventRecord> snapshot() {
        return new ArrayList<>(rows);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogEventRecord row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.getOccurredAt();
            case 1 -> row.getSeverityText() + (row.getSeverityNumber() == null ? "" : " (" + row.getSeverityNumber() + ")");
            case 2 -> row.getSourceKey();
            case 3 -> row.getSourceName();
            case 4 -> row.getEventName();
            case 5 -> row.getMessageText();
            case 6 -> row.getCorrelationId();
            case 7 -> row.getTraceId();
            case 8 -> row.getLogEventId();
            default -> "";
        };
    }
}
