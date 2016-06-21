package org.iish.treemap.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a tabular dataset.
 */
public class TabularData implements Serializable {
    private Map<String, Integer> headers;
    private List<String[]> data;

    /**
     * Creates an empty tabular dataset.
     */
    TabularData() {
    }

    /**
     * Creates a tabular dataset.
     *
     * @param headers The headers and their index of the dataset.
     * @param data    A list with rows of data.
     */
    public TabularData(Map<String, Integer> headers, List<String[]> data) {
        this.headers = headers;
        this.data = data;
    }

    /**
     * Returns the headers of the dataset.
     *
     * @return The headers of the dataset.
     */
    public Set<String> getHeaders() {
        return headers.keySet();
    }

    /**
     * Returns a list of the available rows.
     *
     * @return The list of rows.
     */
    public List<Integer> getRows() {
        return IntStream.range(0, getSize()).boxed().collect(Collectors.toList());
    }

    /**
     * Returns the value for a column in a given row.
     *
     * @param header The name of the header.
     * @param row    The index of the row.
     * @return The value.
     */
    public String getValue(String header, int row) {
        return data.get(row)[headers.get(header)];
    }

    /**
     * Returns the size of the dataset.
     *
     * @return The size.
     */
    public int getSize() {
        return data.size();
    }
}
