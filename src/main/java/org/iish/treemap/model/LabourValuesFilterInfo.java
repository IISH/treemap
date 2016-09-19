package org.iish.treemap.model;

import java.util.Map;
import java.util.Set;

/**
 * Represents value filtering information for a column in a labour relations dataset.
 */
public class LabourValuesFilterInfo extends ValuesFilterInfo {
    private final Map<String, Map<String, String>> timePeriods;

    /**
     * Creates filter information for a column in a dataset.
     *
     * @param column      The column.
     * @param label       The label of the column.
     * @param values      The values to filter on.
     * @param timePeriods The available time periods for this column in the (filtered) dataset.
     */
    public LabourValuesFilterInfo(String column, String label, Set<String> values,
                                  Map<String, Map<String, String>> timePeriods) {
        super(column, label, values);
        this.timePeriods = timePeriods;
    }

    /**
     * Returns the available time periods for this column in the (filtered) dataset.
     *
     * @return The time periods.
     */
    public Map<String, Map<String, String>> getTimePeriods() {
        return timePeriods;
    }
}
