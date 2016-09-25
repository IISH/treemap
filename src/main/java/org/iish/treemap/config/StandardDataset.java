package org.iish.treemap.config;

import com.google.inject.Singleton;
import org.iish.treemap.model.tabular.TabularData;

/**
 * Temporary holder for the default large dataset, not found in Dataverse yet.
 */
@Singleton
public class StandardDataset {
    private TabularData dataset;

    /**
     * Returns the default dataset.
     *
     * @return The dataset.
     */
    public TabularData getDataset() {
        return dataset;
    }

    /**
     * Sets the default dataset.
     *
     * @param dataset The dataset.
     */
    public void setDataset(TabularData dataset) {
        this.dataset = dataset;
    }
}
