package org.iish.treemap.model.filter;

import org.iish.treemap.model.tabular.TabularData;
import org.iish.treemap.model.tabular.FilteredTabularData;
import org.iish.treemap.util.Utils;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A filter for datasets that filters out rows where a column does not contain values within a certain maximum value.
 */
public class MaximumTabularDataFilter implements TabularDataFilter {
    private String column;
    private BigDecimal maximum;

    /**
     * Creates a new maximum value filter.
     *
     * @param column  The name of the column to filter on.
     * @param maximum The maximum value.
     */
    public MaximumTabularDataFilter(String column, BigDecimal maximum) {
        this.column = column;
        this.maximum = maximum;
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
                    return ((value == null) || (value.compareTo(maximum) < 0));
                })
                .collect(Collectors.toSet());
        return new FilteredTabularData(data, filteredRows);
    }
}
