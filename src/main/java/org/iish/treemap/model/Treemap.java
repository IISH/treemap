package org.iish.treemap.model;

/**
 * Representation of a treemap.
 */
public abstract class Treemap {
    private final String column;
    private final String name;

    private String suffix;
    private String color;

    /**
     * Creates a treemap.
     *
     * @param column The column which represents the current hierarchy.
     * @param name   The name of the current hierarchy.
     */
    public Treemap(String column, String name) {
        this.column = column;
        this.name = name;
    }

    /**
     * Returns the column which represents the current hierarchy.
     *
     * @return The column name.
     */
    public String getColumn() {
        return column;
    }

    /**
     * Returns the name of the current hierarchy.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the suffix of the name of this node.
     *
     * @return The name suffix.
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix of the name of this node.
     *
     * @param suffix The name suffix.
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Returns the color of the current hierarchy.
     *
     * @return The color.
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color of the current hierarchy.
     *
     * @param color The color.
     */
    public void setColor(String color) {
        this.color = color;
    }
}
