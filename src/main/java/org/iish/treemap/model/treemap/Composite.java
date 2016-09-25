package org.iish.treemap.model.treemap;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite of a treemap representation.
 */
public class Composite extends Treemap {
    private final List<Treemap> children;

    /**
     * Creates a composite of a treemap representation.
     *
     * @param originalColumn The original column which represents the current hierarchy.
     * @param column         The column which represents the current hierarchy.
     * @param name           The name of the current hierarchy.
     */
    public Composite(String originalColumn, String column, String name) {
        super(originalColumn, column, name);
        this.children = new ArrayList<>();
    }

    /**
     * Adds a child node to the treemap.
     *
     * @param child The child node.
     */
    public void addChild(Treemap child) {
        this.children.add(child);
    }

    /**
     * Returns all the children of this node.
     *
     * @return The children of this node.
     */
    public List<Treemap> getChildren() {
        return children;
    }
}
