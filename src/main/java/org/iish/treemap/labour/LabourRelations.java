package org.iish.treemap.labour;

import org.iish.treemap.config.Config;
import org.iish.treemap.model.LegendValue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class holding labour relations.
 */
@Singleton
public class LabourRelations {
    private Map<String, String> level1;
    private Map<String, String> level2;
    private Map<String, String> level3;
    private Map<String, String> colors;

    private String unknownLabel;
    private String unknownColor;

    private String multipleLabel;

    private List<LegendValue> legend;

    /**
     * Builds the labour relations based on the given configuration.
     *
     * @param config The configuration with labour relations information.
     */
    @Inject
    public LabourRelations(Config config) {
        level3 = new HashMap<>(config.labourRelations.codes);

        level1 = setUpFor(config.labourRelations.level1, true);
        level2 = setUpFor(config.labourRelations.level2, true);

        colors = setUpFor(config.labourRelations.level1, false);

        unknownLabel = config.labourRelations.unknown.label;
        unknownColor = config.labourRelations.unknown.color;

        multipleLabel = config.labourRelations.multiple.label;

        setUpLegend(config.labourRelations.level1);
    }

    /**
     * For the given labour relation values, return the level 1.
     *
     * @param values           The labour relation values separated by a 0.
     * @param combineMultiples Whether to combine all multiples.
     * @return The level 1 labour relation.
     */
    public String getLevel1(String values, boolean combineMultiples) {
        return getMapping(values, level1, unknownLabel, combineMultiples ? null : multipleLabel, " or ");
    }

    /**
     * For the given labour relation values, return the level 2.
     *
     * @param values           The labour relation values separated by a 0.
     * @param combineMultiples Whether to combine all multiples.
     * @return The level 2 labour relation.
     */
    public String getLevel2(String values, boolean combineMultiples) {
        return getMapping(values, level2, unknownLabel, combineMultiples ? null : multipleLabel, " or ");
    }

    /**
     * For the given labour relation values, return the level 3.
     *
     * @param values           The labour relation values separated by a 0.
     * @param combineMultiples Whether to combine all multiples.
     * @return The level 3 labour relation.
     */
    public String getLevel3(String values, boolean combineMultiples) {
        return getMapping(values, level3, unknownLabel, combineMultiples ? null : multipleLabel, " or ");
    }

    /**
     * For the given labour relation values, return the defined color.
     *
     * @param values The labour relation values separated by a 0.
     * @return The color defined for the labour relation.
     */
    public String getColor(String values) {
        return getMapping(values, colors, unknownColor, null, ";");
    }

    /**
     * Returns the labour relations legend.
     *
     * @return The legend.
     */
    public List<LegendValue> getLegend() {
        return legend;
    }

    /**
     * Returns the mapped labour relation level value for a given labour relation values.
     *
     * @param values    The labour relation values separated by a 0.
     * @param mapping   The labour relation level mapping to use.
     * @param unknown   The value to use if the given labour relation level is unknown.
     * @param multiple  The value to use if there are multiple different labour relation level given.
     * @param delimiter The value to use to combine multiple values.
     * @return The mapped labour relation level value.
     */
    private String getMapping(String values, Map<String, String> mapping, String unknown,
                              String multiple, String delimiter) {
        if (values == null)
            return null;

        List<String> relations = Arrays.stream(values.split("0(?!0)"))
                .sorted()
                .map((String value) -> mapping.getOrDefault(value, unknown))
                .distinct()
                .collect(Collectors.toList());

        if (relations.size() == 1)
            return relations.get(0);

        if (multiple != null)
            return multiple;

        return String.join(delimiter, relations);
    }

    /**
     * Sets up a labour relation level mapping for the given configuration.
     *
     * @param source The configuration source.
     * @param label  Use the label value, rather than the color value.
     * @return The labour relation level mapping.
     */
    private Map<String, String> setUpFor(List<Config.LabourRelationsLevel> source, boolean label) {
        Map<String, String> target = new HashMap<>();
        source.forEach(labourRelationsLevel -> {
            int min = labourRelationsLevel.range[0];
            int max = labourRelationsLevel.range[1];

            level3.keySet().forEach(key -> {
                String cleanKey = key.replaceAll("[^0-9]", "");
                int code = Integer.parseInt(cleanKey.substring(0, Math.min(2, cleanKey.length())));
                if ((code >= min) && (code <= max)) {
                    target.put(key, label ? labourRelationsLevel.label : labourRelationsLevel.color);
                }
            });
        });
        return target;
    }

    /**
     * Sets up the legend.
     *
     * @param source The configuration source.
     */
    private void setUpLegend(List<Config.LabourRelationsLevel> source) {
        legend = new ArrayList<>();
        source.forEach(labourRelationsLevel ->
                legend.add(new LegendValue(labourRelationsLevel.label, labourRelationsLevel.color)));
    }
}
