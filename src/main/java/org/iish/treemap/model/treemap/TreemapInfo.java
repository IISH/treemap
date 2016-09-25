package org.iish.treemap.model.treemap;

import java.util.List;

/**
 * Class to hold the treemap and all related information.
 */
public class TreemapInfo {
    private Treemap treemap;
    private List<FilterInfo> filterInfo;
    private List<LegendValue> legend;

    /**
     * Creates a new treemap and related information holder.
     *
     * @param treemap    The treemap.
     * @param filterInfo The filter information.
     * @param legend     The legend.
     */
    public TreemapInfo(Treemap treemap, List<FilterInfo> filterInfo, List<LegendValue> legend) {
        this.treemap = treemap;
        this.filterInfo = filterInfo;
        this.legend = legend;
    }

    /**
     * Returns the treemap.
     *
     * @return The treemap.
     */
    public Treemap getTreemap() {
        return treemap;
    }

    /**
     * Sets the treemap.
     *
     * @param treemap The treemap.
     */
    public void setTreemap(Treemap treemap) {
        this.treemap = treemap;
    }

    /**
     * Returns the filter information.
     *
     * @return The filter information.
     */
    public List<FilterInfo> getFilterInfo() {
        return filterInfo;
    }

    /**
     * Sets the filter information.
     *
     * @param filterInfo The filter information.
     */
    public void setFilterInfo(List<FilterInfo> filterInfo) {
        this.filterInfo = filterInfo;
    }

    /**
     * Returns the legend.
     *
     * @return The legend.
     */
    public List<LegendValue> getLegend() {
        return legend;
    }

    /**
     * Sets the legend.
     *
     * @param legend The legend.
     */
    public void setLegend(List<LegendValue> legend) {
        this.legend = legend;
    }
}
