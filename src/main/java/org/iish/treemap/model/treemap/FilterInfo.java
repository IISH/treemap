package org.iish.treemap.model.treemap;

/**
 * Represents filter information for a column in a dataset.
 */
public abstract class FilterInfo {
    private final String column;
    private final String label;

    /**
     * Creates filter information for a column in a dataset.
     *
     * @param column The column.
     * @param label  The label of the column.
     */
    protected FilterInfo(String column, String label) {
        this.column = column;
        this.label = label;
    }

    /**
     * Returns the name of the column for which the filter information holds.
     *
     * @return The name of the column.
     */
    public String getColumn() {
        return column;
    }

    /**
     * Returns the label of the column for which the filter information holds.
     *
     * @return The label of the column.
     */
    public String getLabel() {
        return label;
    }
}
