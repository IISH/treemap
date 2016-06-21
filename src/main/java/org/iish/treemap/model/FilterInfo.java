package org.iish.treemap.model;

/**
 * Represents filter information for a column in a dataset.
 */
public abstract class FilterInfo {
    private final String column;

    /**
     * Creates filter information for a column in a dataset.
     *
     * @param column The column.
     */
    protected FilterInfo(String column) {
        this.column = column;
    }

    /**
     * Returns the name of the column for which the filter information holds.
     *
     * @return The name of the column.
     */
    public String getColumn() {
        return column;
    }
}
