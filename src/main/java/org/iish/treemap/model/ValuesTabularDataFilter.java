package org.iish.treemap.model;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A filter for datasets that filters out rows where a column does not contain certain values.
 */
public class ValuesTabularDataFilter implements TabularDataFilter {
    private String column;
    private Set<String> values;
    private boolean includeEmpty;

    /**
     * Creates a new values filter.
     *
     * @param column       The name of the column to filter on.
     * @param values       The values to filter on.
     * @param includeEmpty Whether to include or filter out empty values.
     */
    public ValuesTabularDataFilter(String column, Set<String> values, boolean includeEmpty) {
        this.column = column;
        this.values = values;
        this.includeEmpty = includeEmpty;
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
                    String value = data.getValue(column, row);
                    return values.contains(value) || (includeEmpty && (value == null));
                })
                .collect(Collectors.toSet());
        return new FilteredTabularData(data, filteredRows);
    }
}
