package org.iish.treemap.labour;

import org.iish.treemap.config.Config;
import org.iish.treemap.util.Utils;
import org.iish.treemap.model.TabularData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for all time periods.
 */
@Singleton
public class TimePeriods {
    private List<TimePeriod> timePeriods;
    private String empty;

    /**
     * Builds all time periods from the configuration.
     *
     * @param config The config with the time periods.
     */
    @Inject
    private TimePeriods(Config config) {
        this.timePeriods = config.timePeriods.stream()
                .map(timePeriod -> new TimePeriod(timePeriod.timePeriod, timePeriod.minYear, timePeriod.maxYear))
                .collect(Collectors.toList());
        this.empty = config.labour.treemap.empty;
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
     * @param tabularData The data set.
     * @return The years and their matching time periods.
     */
    public Map<String, String> getTimePeriodsFor(TabularData tabularData) {
        Map<String, String> matchingTimePeriods = new LinkedHashMap<>();
        for (TimePeriod tp : timePeriods) {
            Optional<Integer> result = tabularData.getRows().stream()
                    .filter(row -> Utils.getInteger(tabularData.getValue("year", row)) != null)
                    .map(row -> {
                        int year = Utils.getInteger(tabularData.getValue("year", row));
                        int difference = Math.abs(tp.getTimePeriod() - year);
                        return new AbstractMap.SimpleEntry<>(year, difference);
                    })
                    .min(Comparator.comparingInt(AbstractMap.SimpleEntry::getValue))
                    .map(AbstractMap.SimpleEntry::getKey);
            Integer minYear = result.isPresent() ? result.get() : null;

            if ((minYear != null) && tp.isWithinTimePeriod(minYear)) // (Math.abs(tp.getTimePeriod() - minYear) < 75)
                matchingTimePeriods.put(tp.getTimePeriodString(), String.valueOf(minYear));
            else
                matchingTimePeriods.put(tp.getTimePeriodString(), empty);
        }
        return matchingTimePeriods;
    }
}
