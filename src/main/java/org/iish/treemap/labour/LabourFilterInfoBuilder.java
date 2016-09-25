package org.iish.treemap.labour;

import org.iish.treemap.model.tabular.TabularData;
import org.iish.treemap.model.tabular.FilteredTabularData;
import org.iish.treemap.model.treemap.FilterInfoBuilder;
import org.iish.treemap.model.treemap.ValuesFilterInfo;

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
     */
    public LabourFilterInfoBuilder(TabularData table) {
        super(table);
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
        LabourValuesFilterInfo filterInfo = new LabourValuesFilterInfo(column, label, values);

        if (!column.equals("bmyear")) {
            Map<String, Map<String, String>> timePeriodsForValues = new HashMap<>();
            values.parallelStream().forEach(value -> {
                List<Integer> rows = getTable().getRows().stream()
                        .filter(row -> value.equals(getTable().getValue(column, row)))
                        .collect(Collectors.toList());
                FilteredTabularData data = new FilteredTabularData(getTable(), rows);
                timePeriodsForValues.put(value, timePeriods.getTimePeriodsFor(data, false));
            });
            filterInfo.setTimePeriods(timePeriodsForValues);
        }

        return filterInfo;
    }
}
