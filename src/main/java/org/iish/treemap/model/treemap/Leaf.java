package org.iish.treemap.model.treemap;

import java.math.BigDecimal;

/**
 * A leaf of a treemap representation.
 */
public class Leaf extends Treemap {
    private final BigDecimal size;

    /**
     * Creates a composite of a treemap representation.
     *
     * @param originalColumn The original column which represents the current hierarchy.
     * @param column         The column which represents the current hierarchy.
     * @param name           The name of the current hierarchy.
     * @param size           The size of this leaf of the treemap.
     */
    public Leaf(String originalColumn, String column, String name, BigDecimal size) {
        super(originalColumn, column, name);
        this.size = size;
    }

    /**
     * The size of this leaf.
     *
     * @return A number representing the total size.
     */
    public BigDecimal getSize() {
        return size;
    }
}
