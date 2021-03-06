package org.iish.treemap.labour;

import org.iish.treemap.config.Config;
import org.iish.treemap.util.Utils;
import org.iish.treemap.model.tabular.TabularData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for all time periods.
 */
@Singleton
public class TimePeriods {
    private String yearColumn;
    private List<TimePeriod> timePeriods;

    /**
     * Builds all time periods from the configuration.
     *
     * @param config The config with the time periods.
     */
    @Inject
    private TimePeriods(Config config) {
        this.yearColumn = config.labour.xlsx.columns.year;
        this.timePeriods = config.timePeriods.stream()
                .map(timePeriod -> new TimePeriod(timePeriod.timePeriod, timePeriod.minYear, timePeriod.maxYear))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list with all the time periods.
     *
     * @return All the time periods.
     */
    public List<TimePeriod> getTimePeriods() {
        return timePeriods;
    }

    /**
     * Returns a list with all the time periods for the years in the given data set.
     *
     * @param tabularData  The data set.
     * @param includeEmpty Whether to include empty values.
     * @return The years and their matching time periods.
     */
    public Map<String, String> getTimePeriodsFor(TabularData tabularData, boolean includeEmpty) {
        Map<String, String> matchingTimePeriods = new LinkedHashMap<>();
        for (TimePeriod tp : timePeriods) {
            Optional<Integer> minYear = tabularData.getRows().stream()
                    .filter(row -> Utils.getInteger(tabularData.getValue(yearColumn, row)) != null)
                    .map(row -> {
                        int year = Utils.getInteger(tabularData.getValue(yearColumn, row));
                        int difference = Math.abs(tp.getTimePeriod() - year);
                        return new AbstractMap.SimpleEntry<>(year, difference);
                    })
                    .min(Comparator.comparingInt(AbstractMap.SimpleEntry::getValue))
                    .map(AbstractMap.SimpleEntry::getKey);

            if (minYear.isPresent() && tp.isWithinTimePeriod(minYear.get()))
                matchingTimePeriods.put(tp.getTimePeriodString(), String.valueOf(minYear.get()));
            else if (includeEmpty)
                matchingTimePeriods.put(tp.getTimePeriodString(), "-");
        }
        return matchingTimePeriods;
    }
}
