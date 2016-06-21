package org.iish.treemap.model;

import java.util.Set;

/**
 * Represents value filtering information for a column in a dataset.
 */
public class ValuesFilterInfo extends FilterInfo {
    private final Set<String> values;

    /**
     * Creates filter information for a column in a dataset.
     *
     * @param column The column.
     * @param values The values to filter on.
     */
    public ValuesFilterInfo(String column, Set<String> values) {
        super(column);
        this.values = values;
    }

    /**
     * Returns the values to filter on.
     *
     * @return The values to filter on.
     */
    public Set<String> getValues() {
        return values;
    }
}
