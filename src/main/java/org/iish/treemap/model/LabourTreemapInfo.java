package org.iish.treemap.model;

import java.util.List;
import java.util.Map;

/**
 * Class to hold the treemap and all related information for labour relation treemaps.
 */
public class LabourTreemapInfo extends TreemapInfo {
    private Map<String, String> timePeriods;

    /**
     * Creates a new treemap and related information holder for labour relation treemaps.
     *
     * @param treemap     The treemap.
     * @param filterInfo  The filter information.
     * @param legend      The legend.
     * @param timePeriods The defined time periods.
     */
    public LabourTreemapInfo(Treemap treemap, List<FilterInfo> filterInfo, List<LegendValue> legend,
                             Map<String, String> timePeriods) {
        super(treemap, filterInfo, legend);
        this.timePeriods = timePeriods;
    }

    /**
     * Returns the time periods.
     *
     * @return The time periods.
     */
    public Map<String, String> getTimePeriods() {
        return timePeriods;
    }

    /**
     * Sets the time periods.
     *
     * @param timePeriods The time periods.
     */
    public void setTimePeriods(Map<String, String> timePeriods) {
        this.timePeriods = timePeriods;
    }
}
