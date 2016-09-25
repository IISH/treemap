package org.iish.treemap.labour;

import org.iish.treemap.model.filter.TabularDataFilter;
import org.iish.treemap.model.tabular.FilteredTabularData;
import org.iish.treemap.model.tabular.TabularData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default filter for labour relations datasets.
 * Determines the closest year for each time period of each country.
 * Then filters out all data of all other years.
 */
public class DefaultLabourFilter implements TabularDataFilter {
    private String yearColumn;
    private String countryColumn;
    private TimePeriods timePeriods;

    /**
     * Creates the default labour relation filter.
     *
     * @param yearColumn The name of the year column.
     * @param countryColumn The name of the country column.
     * @param timePeriods The time periods to filter on.
     */
    public DefaultLabourFilter(String yearColumn, String countryColumn, TimePeriods timePeriods) {
        this.yearColumn = yearColumn;
        this.countryColumn = countryColumn;
        this.timePeriods = timePeriods;
    }

    /**
     * Filters the given dataset.
     *
     * @param data The dataset.
     * @return The filtered dataset.
     */
    @Override
    public TabularData filter(TabularData data) {
        Map<String, Map<String, String>> timePeriodsForCountries = new HashMap<>();
        data.getRows().parallelStream()
                .collect(Collectors.groupingBy(rowIndex -> data.getValue(countryColumn, rowIndex)))
                .forEach((country, rows) -> {
                    FilteredTabularData countryData = new FilteredTabularData(data, rows);
                    timePeriodsForCountries.put(country, timePeriods.getTimePeriodsFor(countryData, false));
                });

        List<Integer> rows = data.getRows().stream()
                .filter(rowIndex -> {
                    String country = data.getValue(countryColumn, rowIndex);
                    String year = data.getValue(yearColumn, rowIndex);

                    Collection<String> years = timePeriodsForCountries.getOrDefault(country, new HashMap<>()).values();
                    return years.contains(year);
                })
                .collect(Collectors.toList());

        return new FilteredTabularData(data, rows);
    }
}
