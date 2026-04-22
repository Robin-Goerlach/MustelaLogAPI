package de.sasd.mustelalog.client.ui.table;

import de.sasd.mustelalog.client.model.AggregationBucket;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable two-column table model for simple aggregations.
 */
public final class AggregationTableModel extends AbstractTableModel
{
    private final String keyHeader;
    private final List<AggregationBucket> rows = new ArrayList<>();

    public AggregationTableModel(String keyHeader)
    {
        this.keyHeader = keyHeader;
    }

    public void setRows(List<AggregationBucket> items)
    {
        rows.clear();
        if (items != null) rows.addAll(items);
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return 2; }
    @Override public String getColumnName(int column) { return column == 0 ? keyHeader : "Count"; }
    @Override public Object getValueAt(int rowIndex, int columnIndex)
    {
        AggregationBucket row = rows.get(rowIndex);
        return columnIndex == 0 ? row.getKey() : row.getCount();
    }
}
