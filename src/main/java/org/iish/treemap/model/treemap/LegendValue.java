package org.iish.treemap.model.treemap;

/**
 * A value in a legend.
 */
public class LegendValue {
    private String label;
    private String color;

    /**
     * Creates a new legend value.
     *
     * @param label The label.
     * @param color The color.
     */
    public LegendValue(String label, String color) {
        this.label = label;
        this.color = color;
    }

    /**
     * Returns the legend value label.
     *
     * @return The legend value label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the legend value label.
     *
     * @param label The legend value label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the legend value color.
     *
     * @return The legend value color.
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the legend value color.
     *
     * @param color The legend value color.
     */
    public void setColor(String color) {
        this.color = color;
    }
}
