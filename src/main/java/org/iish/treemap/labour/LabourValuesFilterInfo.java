package org.iish.treemap.labour;

import org.iish.treemap.model.treemap.ValuesFilterInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents value filtering information for a column in a labour relations dataset.
 */
public class LabourValuesFilterInfo extends ValuesFilterInfo {
    private Map<String, List<String>> years;
    private Map<String, Map<String, String>> timePeriods;

    /**
     * Creates filter information for a column in a dataset.
     *
     * @param column The column.
     * @param label  The label of the column.
     * @param values The values to filter on.
     */
    public LabourValuesFilterInfo(String column, String label, Set<String> values) {
        super(column, label, values);
    }

    /**
     * Returns the available years for each cross section year.
     *
     * @return The available years for each cross section year.
     */
    public Map<String, List<String>> getYears() {
        return years;
    }

    /**
     * Sets the available years for each cross section year.
     *
     * @param years The available years for each cross section year.
     */
    public void setYears(Map<String, List<String>> years) {
        this.years = years;
    }

    /**
     * Returns the available time periods for this column in the (filtered) dataset.
     *
     * @return The time periods.
     */
    public Map<String, Map<String, String>> getTimePeriods() {
        return timePeriods;
    }

    /**
     * Sets the available time periods for this column in the (filtered) dataset.
     *
     * @param timePeriods The time periods.
     */
    public void setTimePeriods(Map<String, Map<String, String>> timePeriods) {
        this.timePeriods = timePeriods;
    }

}
