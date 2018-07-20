package org.iish.treemap.labour;

import org.iish.treemap.config.Config;
import org.iish.treemap.model.tabular.MultiTabularData;
import org.iish.treemap.model.tabular.TabularData;
import org.iish.treemap.util.Utils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class holding total population information.
 */
@Singleton
public class TotalPopulation {
    private String yearColumn;
    private String totalColumn;
    private String continentColumn;
    private Config.WorldPopulation worldPopulation;
    private Map<String, Integer> headers;

    /**
     * Builds the total population dataset based on the given configuration.
     *
     * @param config The configuration with total population information.
     */
    @Inject
    public TotalPopulation(Config config) {
        this.yearColumn = config.labour.xlsx.virtualColumns.bmyear;
        this.totalColumn = config.labour.xlsx.columns.total;
        this.continentColumn = config.labour.xlsx.virtualColumns.continent;
        this.worldPopulation = config.worldPopulation;
        this.headers = new HashMap<>();

        headers.put(yearColumn, 0);
        headers.put(continentColumn, 1);
        headers.put(config.labour.xlsx.columns.total, 2);
        headers.put(config.labour.xlsx.virtualColumns.color, 3);
        headers.put(config.labour.xlsx.virtualColumns.labRel11, 4);
        headers.put(config.labour.xlsx.virtualColumns.labRelMultiple11, 5);
        headers.put(config.labour.xlsx.virtualColumns.code, 6);
    }

    /**
     * Enrich the dataset with missing numbers based on the total population.
     *
     * @param original The original dataset.
     * @return The enriched dataset.
     */
    public TabularData enrichDataset(TabularData original) {
        ArrayList<String[]> data = new ArrayList<>();

        Map<String, Map<String, List<Integer>>> yearContinentRows = new HashMap<>();
        original.getRows().stream()
                .collect(Collectors.groupingBy(rowIdx -> original.getValue(yearColumn, rowIdx)))
                .forEach((year, rows) -> {
                    Map<String, List<Integer>> continentRows = rows.stream()
                            .collect(Collectors.groupingBy(rowIdx -> original.getValue(continentColumn, rowIdx)));
                    yearContinentRows.put(year, continentRows);
                });

        worldPopulation.totals.forEach((year, continentTotals) -> {
            continentTotals.forEach((continent, totalPopulation) -> {
                BigDecimal totalDataset = BigDecimal.ZERO;
                if (yearContinentRows.containsKey(year.toString())) {
                    Map<String, List<Integer>> continentRows = yearContinentRows.get(year.toString());
                    if (continentRows.containsKey(continent))
                        totalDataset = totalPopulationDataset(original, continentRows.get(continent));
                }
                addToDataset(data, year, continent, totalDataset);
            });
        });

        data.trimToSize();
        TabularData extension = new TabularData(headers, data);

        return new MultiTabularData(Arrays.asList(original, extension));
    }

    /**
     * For the given slice from the dataset, compute the total size.
     *
     * @param dataset The dataset.
     * @param rows    The rows in question.
     * @return The total population size from the dataset.
     */
    private BigDecimal totalPopulationDataset(TabularData dataset, List<Integer> rows) {
        BigDecimal count = rows.stream()
                .map(rowIdx -> {
                    String value = dataset.getValue(totalColumn, rowIdx);
                    BigDecimal bigDecimalValue = Utils.getBigDecimal(value);
                    return (bigDecimalValue != null) ? bigDecimalValue : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return count.setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Adds missing numbers to the dataset for the given year and continent.
     *
     * @param data         The missing numbers dataset.
     * @param year         The year.
     * @param continent    The continent.
     * @param totalDataset The total from the original dataset.
     */
    private void addToDataset(ArrayList<String[]> data, Integer year, String continent, BigDecimal totalDataset) {
        Number totalPopulation = worldPopulation.totals.get(year).get(continent);
        BigDecimal missingSize = new BigDecimal(totalPopulation.longValue()).subtract(totalDataset);
        if (missingSize.compareTo(BigDecimal.ZERO) < 0)
            missingSize = BigDecimal.ZERO;

        String[] row = new String[headers.size()];
        row[0] = String.valueOf(year);
        row[1] = continent;
        row[2] = missingSize.toString();
        row[3] = worldPopulation.color;
        row[4] = row[5] = worldPopulation.label;
        row[6] = worldPopulation.code;

        data.add(row);
    }
}
