package org.iish.treemap.model.treemap;

import java.math.BigDecimal;

/**
 * Represents range filtering information for a column in a dataset.
 */
public class RangeFilterInfo extends FilterInfo {
    private final BigDecimal min;
    private final BigDecimal max;

    /**
     * Creates filter information for a column in a dataset.
     *
     * @param column The column.
     * @param label  The label of the column.
     * @param min    The minimal value to filter on.
     * @param max    The maximal value to filter on.
     */
    public RangeFilterInfo(String column, String label, BigDecimal min, BigDecimal max) {
        super(column, label);
        this.min = min;
        this.max = max;
    }

    /**
     * Returns the minimal value to filter on.
     *
     * @return The minimal value to filter on.
     */
    public BigDecimal getMin() {
        return min;
    }

    /**
     * Returns the maximal value to filter on.
     *
     * @return The maximal value to filter on.
     */
    public BigDecimal getMax() {
        return max;
    }
}
