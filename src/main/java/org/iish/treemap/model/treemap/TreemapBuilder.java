package org.iish.treemap.model.treemap;

import org.iish.treemap.model.tabular.TabularData;
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

    private boolean roundSize;
    private String colorColumn;
    private String codeColumn;
    private Map<String, String> emptyMap;
    private Map<String, String> suffixMap;
    private Map<String, String> multiples;

    /**
     * Creates a treemap builder with the given data.
     *
     * @param table            The dataset.
     * @param hierarchyColumns The columns that represent the hierarchy.
     * @param sizeColumn       The column that represents the size.
     */
    public TreemapBuilder(TabularData table, List<String> hierarchyColumns, String sizeColumn) {
        this.table = table;
        this.hierarchyColumns = hierarchyColumns;
        this.sizeColumn = sizeColumn;

        this.roundSize = false;
        this.emptyMap = new HashMap<>();
        this.suffixMap = new HashMap<>();
        this.multiples = new HashMap<>();
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
     * Sets the map containing labels for empty hierarchies.
     *
     * @param emptyMap The empty map.
     */
    public void setEmptyMap(Map<String, String> emptyMap) {
        this.emptyMap = emptyMap;
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
     * Sets the column that represents the code to use.
     *
     * @param codeColumn The column for codes.
     */
    public void setCodeColumn(String codeColumn) {
        this.codeColumn = codeColumn;
    }

    /**
     * Returns the created treemap with the given name.
     *
     * @param name The name of the treemap.
     * @return The treemap.
     */
    public Treemap getTreeMap(String name) {
        Composite treeMap = new Composite(name, name, name);
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
        String originalHierarchy = hierarchies.poll();
        String hierarchy = multiples.getOrDefault(originalHierarchy, originalHierarchy);

        rows.stream()
                .collect(Collectors.groupingBy(rowIndex -> {
                    String value = table.getValue(hierarchy, rowIndex);
                    return (value != null) ? value : "";
                }))
                .forEach((key, rowIndexes) -> {
                    if (hierarchies.isEmpty())
                        addLeaf(originalHierarchy, hierarchy, key, rowIndexes, curBranch);
                    else {
                        String name = key;
                        if (key.isEmpty())
                            name = getEmptyValue(originalHierarchy);

                        Composite nextBranch = new Composite(originalHierarchy, hierarchy, name);
                        addSuffix(nextBranch, originalHierarchy);
                        addColor(nextBranch, rowIndexes);
                        addCode(nextBranch, rowIndexes);
                        addEmpty(nextBranch, key.isEmpty());

                        addBranch(new LinkedList<>(hierarchies), rowIndexes, nextBranch);

                        List<Treemap> children = nextBranch.getChildren();
                        boolean isSingleChild = (children.size() == 1);
                        Treemap singleChild = isSingleChild ? children.get(0) : null;

                        boolean singleSameChild = (isSingleChild && singleChild.getName().equals(name));
                        boolean singleEmptyChild = (isSingleChild && singleChild.isEmpty());

                        if (children.isEmpty() || singleEmptyChild)
                            addLeaf(originalHierarchy, hierarchy, key, rowIndexes, curBranch);
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
     * @param orgHierarchy The name of the current original hierarchy.
     * @param hierarchy    The name of the current hierarchy.
     * @param name         The name of the leaf.
     * @param rows         The rows of the table.
     * @param current      The current branch in the treemap.
     */
    private void addLeaf(String orgHierarchy, String hierarchy, String name, List<Integer> rows, Composite current) {
        BigDecimal count = rows.stream()
                .map(rowIndex -> {
                    String value = table.getValue(sizeColumn, rowIndex);
                    BigDecimal bigDecimalValue = Utils.getBigDecimal(value);
                    return (bigDecimalValue != null) ? bigDecimalValue : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (roundSize)
            count = count.setScale(0, BigDecimal.ROUND_HALF_UP);

        String newName = name;
        if (name.isEmpty())
            newName = getEmptyValue(orgHierarchy);

        Leaf leaf = new Leaf(orgHierarchy, hierarchy, newName, count);
        addSuffix(leaf, orgHierarchy);
        addColor(leaf, rows);
        addCode(leaf, rows);
        addEmpty(leaf, name.isEmpty());

        current.addChild(leaf);
    }

    /**
     * If there is a suffix defined for this hierarchy, add this information to the treemap.
     *
     * @param node         The treemap.
     * @param orgHierarchy The name of the current original hierarchy.
     */
    private void addSuffix(Treemap node, String orgHierarchy) {
        if (suffixMap.containsKey(orgHierarchy))
            node.setSuffix(suffixMap.get(orgHierarchy));
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
     * If there is a code defined for this hierarchy, add this information to the treemap.
     *
     * @param node The treemap.
     * @param rows The table rows.
     */
    private void addCode(Treemap node, List<Integer> rows) {
        if (codeColumn != null) {
            String codes = rows.stream()
                    .flatMap(rowIndex -> Arrays.stream(
                            table.getValue(multiples.getOrDefault(codeColumn, codeColumn), rowIndex).split(",")))
                    .distinct()
                    .collect(Collectors.joining(" or "));
            node.setCode(codes);
        }
    }

    /**
     * Make sure the treemap also contains information whether this is an empty node.
     *
     * @param node    The treemap.
     * @param isEmpty Whether this is an empty value node.
     */
    private void addEmpty(Treemap node, boolean isEmpty) {
        node.setEmpty(isEmpty);
    }

    /**
     * Returns the value for an empty node for the current hierarchy.
     *
     * @param hierarchy The current hierarchy.
     * @return The value.
     */
    private String getEmptyValue(String hierarchy) {
        return emptyMap.getOrDefault(hierarchy, "-");
    }
}
