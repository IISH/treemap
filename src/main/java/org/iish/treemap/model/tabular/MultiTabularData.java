package org.iish.treemap.model.tabular;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a combination of various tabular datasets.
 */
public class MultiTabularData extends TabularData implements Serializable {
    private List<TabularData> datasets;

    /**
     * Creates a combined tabular dataset.
     *
     * @param datasets The datasets to combine.
     */
    public MultiTabularData(List<TabularData> datasets) {
        this.datasets = datasets.stream()
                .flatMap(dataset -> {
                    if (dataset instanceof MultiTabularData)
                        return ((MultiTabularData) dataset).getDatasets().stream();
                    return Collections.singleton(dataset).stream();
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns all of the combined datasets.
     *
     * @return The datasets.
     */
    public List<TabularData> getDatasets() {
        return datasets;
    }

    /**
     * Returns the headers of the datasets.
     *
     * @return The headers of the datasets.
     */
    public Set<String> getHeaders() {
        return datasets.stream()
                .flatMap(dataset -> dataset.getHeaders().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Returns the value for a column in a given row.
     *
     * @param header The name of the header.
     * @param row    The index of the row.
     * @return The value.
     */
    public String getValue(String header, int row) {
        int rowIndex = row;
        for (TabularData dataset : datasets) {
            if (rowIndex < dataset.getSize())
                return dataset.getValue(header, rowIndex);
            rowIndex -= dataset.getSize();
        }
        throw new IndexOutOfBoundsException("Row " + rowIndex + " from total size " + getSize());
    }

    /**
     * Returns the size of the datasets.
     *
     * @return The size.
     */
    public int getSize() {
        return datasets.stream()
                .mapToInt(TabularData::getSize)
                .sum();
    }
}
