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
    private String empty;
    private Set<String> columnsAllValues;

    /**
     * Creates a builder that creates filter information.
     *
     * @param empty The value for empty values.
     */
    public FilterInfoBuilder(String empty) {
        this.empty = empty;
    }

    /**
     * Creates a builder that creates filter information.
     *
     * @param empty       The value for empty values.
     * @param columnsAllValues Which columns always should return all values.
     */
    public FilterInfoBuilder(String empty, Set<String> columnsAllValues) {
        this.empty = empty;
        this.columnsAllValues = columnsAllValues;
    }

    /**
     * Returns filter information for the given dataset.
     *
     * @param table         The dataset.
     * @param filterColumns The column names to obtain filter information about.
     * @return A list with filter information.
     */
    public List<FilterInfo> getFilterInfo(TabularData table, Collection<String> filterColumns) {
        List<FilterInfo> filterInfoList = new ArrayList<>();
        filterColumns.forEach(column -> {
            Set<String> values = table.getRows().stream()
                    .map(row -> {
                        String value = table.getValue(column, row);
                        return (value != null) ? value : empty;
                    })
                    .distinct()
                    .collect(Collectors.toSet());

            boolean useValuesFilters = ((columnsAllValues != null) && columnsAllValues.contains(column));
            List<BigDecimal> numbers = values.stream().map(Utils::getBigDecimal).collect(Collectors.toList());

            if (!useValuesFilters && numbers.stream().allMatch(value -> value != null)) {
                BigDecimal min = numbers.stream().min(BigDecimal::compareTo).orElse(null);
                BigDecimal max = numbers.stream().max(BigDecimal::compareTo).orElse(null);

                if ((min != null) && (max != null) && (min.compareTo(max) != 0))
                    filterInfoList.add(new RangeFilterInfo(column, min, max));
            }
            else {
                filterInfoList.add(new ValuesFilterInfo(column, values));
            }
        });
        return filterInfoList;
    }
}
