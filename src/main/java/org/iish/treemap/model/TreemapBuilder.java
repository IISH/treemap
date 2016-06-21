package org.iish.treemap.model;

import org.iish.treemap.util.Utils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A builder that creates treemaps.
 */
public class TreemapBuilder {
    private TabularData table;
    private List<String> hierarchyColumns;
    private String sizeColumn;
    private String emptyVal;

    private boolean roundSize;
    private String colorColumn;
    private Map<String, String> suffixMap;
    private Map<String, String> multiples;

    /**
     * Creates a treemap builder with the given data.
     *
     * @param table            The dataset.
     * @param hierarchyColumns The columns that represent the hierarchy.
     * @param sizeColumn       The column that represents the size.
     * @param emptyVal         The value to use for empty values in the treemap.
     */
    public TreemapBuilder(TabularData table, List<String> hierarchyColumns, String sizeColumn, String emptyVal) {
        this.table = table;
        this.hierarchyColumns = hierarchyColumns;
        this.sizeColumn = sizeColumn;
        this.emptyVal = emptyVal;
        this.roundSize = false;
    }

    /**
     * Sets the column that represents the color to use.
     *
     * @param colorColumn The column for colors.
     */
    public void setColorColumn(String colorColumn) {
        this.colorColumn = colorColumn;
    }

    /**
     * Sets the map containing suffixes and on which hierarchies to use them.
     *
     * @param suffixMap The suffix map.
     */
    public void setSuffixMap(Map<String, String> suffixMap) {
        this.suffixMap = suffixMap;
    }

    /**
     * Sets the map containing the columns and their multiple column variant.
     *
     * @param multiples The multiples map.
     */
    public void setMultiples(Map<String, String> multiples) {
        this.multiples = multiples;
        updateHierarchyColumns();
    }

    /**
     * Whether to round sizes.
     *
     * @param roundSize Whether to round sizes.
     */
    public void setRoundSize(boolean roundSize) {
        this.roundSize = roundSize;
    }

    /**
     * Returns the created treemap with the given name.
     *
     * @param name The name of the treemap.
     * @return The treemap.
     */
    public Treemap getTreeMap(String name) {
        Composite treeMap = new Composite(name, name);
        addBranch(new LinkedList<>(hierarchyColumns), table.getRows(), treeMap);
        return treeMap;
    }

    /**
     * Adds a branch to the treemap.
     *
     * @param hierarchies The list of hierarchies to visit still.
     * @param rows        The rows of the table.
     * @param curBranch   The current branch in the treemap.
     */
    private void addBranch(Queue<String> hierarchies, List<Integer> rows, Composite curBranch) {
        String hierarchy = hierarchies.poll();
        rows.stream()
                .collect(Collectors.groupingBy(rowIndex -> {
                    String value = table.getValue(hierarchy, rowIndex);
                    return (value != null) ? value : emptyVal;
                }))
                .forEach((key, rowIndexes) -> {
                    if (hierarchies.isEmpty())
                        addLeaf(hierarchy, key, rowIndexes, curBranch);
                    else {
                        Composite nextBranch = new Composite(hierarchy, key);
                        addSuffix(nextBranch, hierarchy);
                        addColor(nextBranch, rowIndexes);

                        addBranch(new LinkedList<>(hierarchies), rowIndexes, nextBranch);

                        List<Treemap> children = nextBranch.getChildren();
                        boolean isSingleChild = (children.size() == 1);
                        Treemap singleChild = isSingleChild ? children.get(0) : null;

                        boolean singleSameChild = (isSingleChild && singleChild.getName().equals(key));
                        boolean singleEmptyChild = (isSingleChild && singleChild.getName().equals(emptyVal));

                        if (children.isEmpty() || singleEmptyChild)
                            addLeaf(hierarchy, key, rowIndexes, curBranch);
                        else if (singleSameChild)
                            curBranch.addChild(singleChild);
                        else
                            curBranch.addChild(nextBranch);
                    }
                });
    }

    /**
     * Adds a leaf to the treemap.
     *
     * @param hierarchy The name of the current hiereachy.
     * @param name      The name of the leaf.
     * @param rows      The rows of the table.
     * @param current   The current branch in the treemap.
     */
    private void addLeaf(String hierarchy, String name, List<Integer> rows, Composite current) {
        BigDecimal count = rows.stream()
                .map(rowIndex -> {
                    String value = table.getValue(sizeColumn, rowIndex);
                    BigDecimal bigDecimalValue = Utils.getBigDecimal(value);
                    return (bigDecimalValue != null) ? bigDecimalValue : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (roundSize)
            count = count.setScale(0, BigDecimal.ROUND_HALF_UP);

        Leaf leaf = new Leaf(hierarchy, name, count);
        addSuffix(leaf, hierarchy);
        addColor(leaf, rows);

        current.addChild(leaf);
    }

    /**
     * If there is a suffix defined for this hierarchy, add this information to the treemap.
     *
     * @param node      The treemap.
     * @param hierarchy The hierarchy.
     */
    private void addSuffix(Treemap node, String hierarchy) {
        if ((suffixMap != null) && suffixMap.containsKey(hierarchy))
            node.setSuffix(suffixMap.get(hierarchy));
    }

    /**
     * If there is a column defined for colors, add this information to the treemap.
     *
     * @param node The treemap.
     * @param rows The table rows.
     */
    private void addColor(Treemap node, List<Integer> rows) {
        if (colorColumn != null) {
            String colors = rows.stream()
                    .flatMap(rowIndex -> Arrays.stream(table.getValue(colorColumn, rowIndex).split(";")))
                    .distinct()
                    .collect(Collectors.joining(";"));
            node.setColor(colors);
        }
    }

    /**
     * Updates the hierarchy columns with their multiple column variant if it exists.
     */
    private void updateHierarchyColumns() {
        if (multiples != null) {
            hierarchyColumns = hierarchyColumns.stream()
                    .map(hierarchy -> multiples.getOrDefault(hierarchy, hierarchy))
                    .collect(Collectors.toList());
        }
    }
}
