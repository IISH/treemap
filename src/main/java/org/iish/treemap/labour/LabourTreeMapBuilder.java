package org.iish.treemap.labour;

import com.google.common.cache.Cache;
import org.iish.treemap.config.Config;
import org.iish.treemap.config.StandardDataset;
import org.iish.treemap.dataset.DataverseApiClient;
import org.iish.treemap.dataset.DataverseException;
import org.iish.treemap.dataset.DataverseFile;
import org.iish.treemap.model.treemap.*;
import org.iish.treemap.model.filter.*;
import org.iish.treemap.model.tabular.MultiTabularData;
import org.iish.treemap.model.tabular.TabularData;
import org.iish.treemap.util.Utils;
import org.iish.treemap.util.XlsxException;
import spark.Request;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds labour relation treemaps for given requests.
 */
@Singleton
public class LabourTreeMapBuilder {
    private static final String LABOUR_RELATIONS_DATASET_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private Config config;
    private StandardDataset standardDataset;
    private DataverseApiClient dataverseApiClient;
    private LabourRelations labourRelations;
    private TimePeriods timePeriods;
    private TotalPopulation totalPopulation;
    private Cache<String, TabularData> cache;

    /**
     * Labour relations treemap builder.
     *
     * @param config             The configuration to use.
     * @param standardDataset    The standard dataset to use.
     * @param dataverseApiClient The Dataverse API client to use.
     * @param labourRelations    The labour relations to use.
     * @param timePeriods        The time periods to use.
     * @param totalPopulation    The total population to use.
     * @param cache              The cache holding datasets.
     */
    @Inject
    public LabourTreeMapBuilder(Config config, StandardDataset standardDataset, DataverseApiClient dataverseApiClient,
                                LabourRelations labourRelations, TimePeriods timePeriods,
                                TotalPopulation totalPopulation, Cache<String, TabularData> cache) {
        this.config = config;
        this.standardDataset = standardDataset;
        this.dataverseApiClient = dataverseApiClient;
        this.labourRelations = labourRelations;
        this.timePeriods = timePeriods;
        this.totalPopulation = totalPopulation;
        this.cache = cache;
    }

    /**
     * Returns the available datasets for the given PID.
     *
     * @param request The request with the PID.
     * @return The set of files.
     * @throws DataverseException When unable to obtain the datasets information from Dataverse.
     */
    public Set<DataverseFile> getFiles(Request request) throws DataverseException {
        return dataverseApiClient.getFilesForPid(request.queryParams("pid"), LABOUR_RELATIONS_DATASET_CONTENT_TYPE);
    }

    /**
     * Returns the columns for the dataset of the given request.
     *
     * @param request The request with the dataset URL.
     * @return The set of columns.
     * @throws LabourTreemapException When unable to parse the Excel dataset.
     */
    public Set<String> getColumns(Request request) throws LabourTreemapException {
        TabularData data = getTabularData(request);
        return data.getHeaders();
    }

    /**
     * Builds a treemap for the given request.
     *
     * @param request The treemap request.
     * @return The labour relations treemap information.
     * @throws LabourTreemapException When no treemap could be build.
     */
    public TreemapInfo getTreemap(Request request) throws LabourTreemapException {
        TabularData data = getTabularData(request);
        Set<TabularDataFilter> filters = getRequestFilters(request);

        DefaultLabourFilter defaultLabourFilter = new DefaultLabourFilter(
                config.labour.xlsx.columns.year, config.labour.xlsx.columns.country, timePeriods);
        TabularData defaultFilteredData = defaultLabourFilter.filter(data);

        TabularData extendedData = extendData(request, defaultFilteredData);
        TabularData filteredData = filterData(filters, extendedData);

        Treemap treemap = buildTreemap(request, filteredData);
        List<FilterInfo> filterInfo = buildFilterInfo(request, filteredData);

        return new TreemapInfo(treemap, filterInfo, labourRelations.getLegend());
    }

    /**
     * Parse the given Excel file to a TabularData object.
     *
     * @param request The request with the URL where to find the Excel data.
     * @return The parsed Excel file.
     * @throws LabourTreemapException When parsing failed.
     */
    private TabularData getTabularData(Request request) throws LabourTreemapException {
        try {
            List<TabularData> datasets = new ArrayList<>();

            for (String fileId : request.queryParamsValues("file")) {
                TabularData data = cache.getIfPresent(fileId);
                if (fileId.equalsIgnoreCase("dataset") && (standardDataset.getDataset() != null))
                    data = standardDataset.getDataset();

                if (data != null) {
                    datasets.add(data);
                }
                else if (fileId.matches("\\d+")) {
                    InputStream inputStream = dataverseApiClient.getFileById(new Long(fileId));
                    LabourRelationsXlsxReader xlsxReader = new LabourRelationsXlsxReader(
                            this.config, this.labourRelations, this.timePeriods, inputStream);
                    data = xlsxReader.getData();
                    cache.put(fileId, data);

                    datasets.add(data);
                }
            }

            if (datasets.isEmpty())
                throw new LabourTreemapException("No datasets requested!");

            if (datasets.size() == 1)
                return datasets.get(0);

            return new MultiTabularData(datasets);
        }
        catch (DataverseException e) {
            throw new LabourTreemapException("Unable to obtain file from Dataverse", e);
        }
        catch (XlsxException | IOException e) {
            throw new LabourTreemapException("Unable to parse Excel document", e);
        }
    }

    /**
     * If the request specifies filters, then return those filters.
     *
     * @param request The filter request.
     * @return The filters.
     */
    private Set<TabularDataFilter> getRequestFilters(Request request) {
        Set<TabularDataFilter> filters = new HashSet<>();
        Set<Map.Entry<String, String[]>> entrySet = request.queryMap().toMap().entrySet();

        entrySet.stream()
                .filter(entry -> entry.getKey().startsWith("filter:"))
                .map(entry -> {
                    String column = entry.getKey().substring(7);
                    Set<String> values = Arrays.stream(entry.getValue())
                            .filter(v -> !v.trim().isEmpty())
                            .collect(Collectors.toSet());
                    return new AbstractMap.SimpleEntry<>(column, values);
                })
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> new ValuesTabularDataFilter(entry.getKey(), entry.getValue(), true))
                .forEach(filters::add);

        entrySet.stream()
                .filter(entry -> entry.getKey().startsWith("min:"))
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().substring(4), entry.getValue()[0]))
                .map(entry -> new MinimalTabularDataFilter(entry.getKey(), Utils.getBigDecimal(entry.getValue())))
                .forEach(filters::add);

        entrySet.stream()
                .filter(entry -> entry.getKey().startsWith("max:"))
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().substring(4), entry.getValue()[0]))
                .map(entry -> new MaximumTabularDataFilter(entry.getKey(), Utils.getBigDecimal(entry.getValue())))
                .forEach(filters::add);

        return filters;
    }

    /**
     * If the request requires total population information, add the data to the dataset.
     * Can only extend dataset if no filters (except on bmyear or continent) are applied.
     *
     * @param request     The request.
     * @param tabularData The data set.
     * @return The extended dataset.
     */
    private TabularData extendData(Request request, TabularData tabularData) {
        String showTotalPopulation = request.queryParams("totalPopulation");

        if ((showTotalPopulation != null) && showTotalPopulation.equalsIgnoreCase("show")) {
            long noFiltersApplied = request.queryMap().toMap().entrySet().stream()
                    .filter(entry -> {
                        String key = entry.getKey();
                        return key.startsWith("filter:") || key.startsWith("min:") || key.startsWith("max:");
                    })
                    .filter(entry -> {
                        String key = entry.getKey();
                        return !key.equals("filter:" + config.labour.xlsx.virtualColumns.bmyear) &&
                                !key.equals("filter:" + config.labour.xlsx.virtualColumns.continent);
                    })
                    .count();
            if (noFiltersApplied == 0) {
                return totalPopulation.enrichDataset(tabularData);
            }
        }
        return tabularData;
    }

    /**
     * Apply the given filters on the dataset.
     *
     * @param filters The filters.
     * @param data    The data set.
     * @return The filtered dataset.
     */
    private TabularData filterData(Set<TabularDataFilter> filters, TabularData data) {
        for (TabularDataFilter filter : filters) {
            data = filter.filter(data);
        }
        return data;
    }

    /**
     * Builds the actual treemap.
     *
     * @param request The treemap request.
     * @param data    The data set.
     * @return The treemap.
     */
    private Treemap buildTreemap(Request request, TabularData data) {
        List<String> hierarchy = Utils.filterOutEmpty(Arrays.asList(request.queryParamsValues("hierarchy")));

        TreemapBuilder treemapBuilder = new TreemapBuilder(data, hierarchy, request.queryParams("size"));
        treemapBuilder.setRoundSize(true);
        treemapBuilder.setColorColumn(config.labour.xlsx.virtualColumns.color);
        treemapBuilder.setEmptyMap(config.labour.treemap.empty);
        treemapBuilder.setSuffixMap(config.labour.treemap.suffix);

        String showMultiples = request.queryParams("multiples");
        if ((showMultiples != null) && showMultiples.equalsIgnoreCase("show"))
            treemapBuilder.setMultiples(config.labour.treemap.multiples);

        return treemapBuilder.getTreeMap(config.labour.treemap.rootLabel);
    }

    /**
     * Creates a list with information on which filters can be applied.
     *
     * @param request The treemap request.
     * @param data    The data set.
     * @return A list with filter information.
     */
    private List<FilterInfo> buildFilterInfo(Request request, TabularData data) {
        String[] filterInfoArr = request.queryParamsValues("filterInfo");
        List<String> filter = new ArrayList<>((filterInfoArr != null)
                ? Arrays.asList(filterInfoArr) : Collections.emptyList());

        LabourFilterInfoBuilder filterInfoBuilder = new LabourFilterInfoBuilder(data);
        filterInfoBuilder.setColumnsAllValues(Collections.singleton(config.labour.xlsx.virtualColumns.bmyear));
        filterInfoBuilder.setLabels(config.labour.treemap.labels);
        filterInfoBuilder.setTimePeriods(timePeriods);

        return filterInfoBuilder.getFilterInfo(Utils.filterOutEmpty(filter));
    }
}
