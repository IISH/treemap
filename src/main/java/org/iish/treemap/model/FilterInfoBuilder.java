package org.iish.treemap.model;

import org.iish.treemap.util.Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A builder that creates filter information for a given dataset.
 */
public class FilterInfoBuilder {
    private TabularData table;
    private String empty;
    private Set<String> columnsAllValues;

    /**
     * Creates a builder that creates filter information.
     *
     * @param table The dataset.
     * @param empty The value for empty values.
     */
    public FilterInfoBuilder(TabularData table, String empty) {
        this.table = table;
        this.empty = empty;
    }

    /**
     * Creates a builder that creates filter information.
     *
     * @param table            The dataset.
     * @param empty            The value for empty values.
     * @param columnsAllValues Which columns always should return all values.
     */
    public FilterInfoBuilder(TabularData table, String empty, Set<String> columnsAllValues) {
        this.table = table;
        this.empty = empty;
        this.columnsAllValues = columnsAllValues;
    }

    public TabularData getTable() {
        return table;
    }

    /**
     * Returns filter information for the given dataset.
     *
     * @param filterColumns The column names to obtain filter information about.
     * @return A list with filter information.
     */
    public List<FilterInfo> getFilterInfo(Collection<String> filterColumns) {
        List<FilterInfo> filterInfoList = new ArrayList<>();
        filterColumns.forEach(column -> {
            boolean useValuesFilters = ((columnsAllValues != null) && columnsAllValues.contains(column));

            Set<String> values = getValues(column);
            Set<BigDecimal> numbers = !useValuesFilters ? getNumbers(values) : null;

            if ((numbers != null) && numbers.stream().allMatch(value -> value != null)) {
                BigDecimal min = numbers.stream().min(BigDecimal::compareTo).orElse(null);
                BigDecimal max = numbers.stream().max(BigDecimal::compareTo).orElse(null);

                if ((min != null) && (max != null) && (min.compareTo(max) != 0)) {
                    filterInfoList.add(createRangeFilter(column, min, max));
                }
            }
            else {
                filterInfoList.add(createValuesFilter(column, values));
            }
        });
        return filterInfoList;
    }

    /**
     * Get all distinct values for a column.
     *
     * @param column The column.
     * @return A set of distinct values in the dataset for the given column.
     */
    private Set<String> getValues(String column) {
        return table.getRows().stream()
                .map(row -> {
                    String value = table.getValue(column, row);
                    return (value != null) ? value : empty;
                })
                .distinct()
                .collect(Collectors.toSet());
    }

    /**
     * Get all numeric values for a column.
     *
     * @param values The values.
     * @return A set of numeric values in the dataset for the given set of values.
     */
    private Set<BigDecimal> getNumbers(Set<String> values) {
        return values.stream()
                .map(Utils::getBigDecimal)
                .collect(Collectors.toSet());
    }

    /**
     * Creates a range filter for the given min and max value.
     *
     * @param column The column on which the filter applies.
     * @param min    The minimum value.
     * @param max    The maximum value.
     * @return The range filter.
     */
    protected RangeFilterInfo createRangeFilter(String column, BigDecimal min, BigDecimal max) {
        return new RangeFilterInfo(column, min, max);
    }

    /**
     * Creates a values filter for the given set of values.
     *
     * @param column The column on which the filter applies.
     * @param values The values.
     * @return The values filter.
     */
    protected ValuesFilterInfo createValuesFilter(String column, Set<String> values) {
        return new ValuesFilterInfo(column, values);
    }
}
