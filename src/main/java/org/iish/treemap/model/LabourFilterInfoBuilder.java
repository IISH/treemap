package org.iish.treemap.model;

import org.iish.treemap.labour.TimePeriods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filter info builder for labour relations that adds information
 * about the time periods for each possible filter value.
 */
public class LabourFilterInfoBuilder extends FilterInfoBuilder {
    private TimePeriods timePeriods;

    /**
     * Creates a builder that creates filter information.
     *
     * @param table The dataset.
     * @param empty The value for empty values.
     */
    public LabourFilterInfoBuilder(TabularData table, String empty) {
        super(table, empty);
    }

    /**
     * Set the time periods.
     *
     * @param timePeriods The time periods.
     */
    public void setTimePeriods(TimePeriods timePeriods) {
        this.timePeriods = timePeriods;
    }

    /**
     * Creates a values filter for the given set of values
     * and adds information about the time periods for each possible filter value.
     *
     * @param column The column on which the filter applies.
     * @param label  The label for the column.
     * @param values The values.
     * @return The values filter.
     */
    @Override
    protected ValuesFilterInfo createValuesFilter(String column, String label, Set<String> values) {
        Map<String, Map<String, String>> timePeriodsForValues = new HashMap<>();
        values.parallelStream()
                .forEach(value -> {
                    List<Integer> rows = getTable().getRows()
                            .stream()
                            .filter(row -> value.equals(getTable().getValue(column, row)))
                            .collect(Collectors.toList());
                    FilteredTabularData data = new FilteredTabularData(getTable(), rows);
                    timePeriodsForValues.put(value, timePeriods.getTimePeriodsFor(data, false));
                });
        return new LabourValuesFilterInfo(column, label, values, timePeriodsForValues);
    }
}
