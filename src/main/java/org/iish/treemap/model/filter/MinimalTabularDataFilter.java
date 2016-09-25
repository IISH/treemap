package org.iish.treemap.model.filter;

import org.iish.treemap.model.tabular.TabularData;
import org.iish.treemap.model.tabular.FilteredTabularData;
import org.iish.treemap.util.Utils;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A filter for datasets that filters out rows where a column does not contain values within a certain minimum value.
 */
public class MinimalTabularDataFilter implements TabularDataFilter {
    private String column;
    private BigDecimal minimum;

    /**
     * Creates a new minimum value filter.
     *
     * @param column  The name of the column to filter on.
     * @param minimum The minimum value.
     */
    public MinimalTabularDataFilter(String column, BigDecimal minimum) {
        this.column = column;
        this.minimum = minimum;
    }

    /**
     * Filters the given dataset.
     *
     * @param data The dataset.
     * @return The filtered dataset.
     */
    @Override
    public TabularData filter(TabularData data) {
        Set<Integer> filteredRows = data.getRows().stream()
                .filter(row -> {
                    BigDecimal value = Utils.getBigDecimal(data.getValue(column, row));
                    return ((value == null) || (value.compareTo(minimum) > 0));
                })
                .collect(Collectors.toSet());
        return new FilteredTabularData(data, filteredRows);
    }
}
