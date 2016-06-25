package org.iish.treemap.model;

import java.util.Map;
import java.util.Set;

public class LabourValuesFilterInfo extends ValuesFilterInfo {
    private final Map<String, Map<String, String>> timePeriods;

    public LabourValuesFilterInfo(String column, Set<String> values, Map<String, Map<String, String>> timePeriods) {
        super(column, values);
        this.timePeriods = timePeriods;
    }

    public Map<String, Map<String, String>> getTimePeriods() {
        return timePeriods;
    }
}
