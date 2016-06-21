package org.iish.treemap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Represents a filtered tabular dataset.
 */
public class FilteredTabularData extends TabularData implements Serializable {
    private TabularData tabularData;
    private List<Integer> rows;

    /**
     * Creates a filtered tabular dataset.
     *
     * @param tabularData The original dataset.
     * @param rows        The available rows.
     */
    public FilteredTabularData(TabularData tabularData, Collection<Integer> rows) {
        if (tabularData instanceof FilteredTabularData) {
            FilteredTabularData filtered = (FilteredTabularData) tabularData;
            this.tabularData = filtered.getTabularData();
            rows = rows.stream().map(row -> filtered.getFilteredRows().get(row)).collect(Collectors.toList());
        }
        else {
            this.tabularData = tabularData;
        }

        this.rows = new ArrayList<>(new TreeSet<>(rows));
    }

    /**
     * The original tabular data source.
     *
     * @return The dataset.
     */
    public TabularData getTabularData() {
        return tabularData;
    }

    /**
     * The rows that are available.
     *
     * @return The set of available rows.
     */
    public List<Integer> getFilteredRows() {
        return rows;
    }

    /**
     * Returns the value for a column in a given row.
     *
     * @param header The name of the header.
     * @param row    The index of the row.
     * @return The value.
     */
    public String getValue(String header, int row) {
        return tabularData.getValue(header, rows.get(row));
    }

    /**
     * Returns the size of the filtered dataset.
     *
     * @return The size.
     */
    public int getSize() {
        return rows.size();
    }
}
