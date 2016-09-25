package org.iish.treemap.model.treemap;

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
     * @param label  The label of the column.
     * @param values The values to filter on.
     */
    public ValuesFilterInfo(String column, String label, Set<String> values) {
        super(column, label);
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
