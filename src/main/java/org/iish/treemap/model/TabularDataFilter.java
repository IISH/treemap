package org.iish.treemap.model;

/**
 * A filter for datasets.
 */
public interface TabularDataFilter {

    /**
     * Filters the given dataset.
     *
     * @param data The dataset.
     * @return The filtered dataset.
     */
    TabularData filter(TabularData data);
}
