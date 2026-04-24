package de.sasd.mustelalog.client.ui;

import de.sasd.mustelalog.client.model.SourceSummary;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Simple table model for tenant-visible sources.
 */
public final class SourceTableModel extends AbstractTableModel {
    private final String[] columns = {
            "Source Key",
            "Source Name",
            "Type",
            "Environment",
            "Host",
            "Service",
            "Active",
            "Source ID"
    };

    private List<SourceSummary> rows = new ArrayList<>();

    public void setRows(List<SourceSummary> rows) {
        this.rows = rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        fireTableDataChanged();
    }

    public SourceSummary getRow(int modelRowIndex) {
        if (modelRowIndex < 0 || modelRowIndex >= rows.size()) {
            return null;
        }
        return rows.get(modelRowIndex);
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
        SourceSummary row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.sourceKey();
            case 1 -> row.sourceName();
            case 2 -> row.sourceType();
            case 3 -> row.environment();
            case 4 -> row.hostName();
            case 5 -> row.serviceName();
            case 6 -> row.active();
            case 7 -> row.sourceId();
            default -> "";
        };
    }
}
