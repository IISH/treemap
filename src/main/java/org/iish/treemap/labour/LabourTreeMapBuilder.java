package org.iish.treemap.labour;

import com.google.common.cache.Cache;
import org.iish.treemap.config.Config;
import org.iish.treemap.config.StandardDataset;
import org.iish.treemap.dataset.DataverseApiClient;
import org.iish.treemap.dataset.DataverseException;
import org.iish.treemap.dataset.DataverseFile;
import org.iish.treemap.model.*;
import org.iish.treemap.model.Treemap;
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
    private Cache<String, TabularData> cache;
    private FilterInfoBuilder filterInfoBuilder;

    /**
     * Labour relations treemap builder.
     *
     * @param config             The configuration to use.
     * @param standardDataset    The standard dataset to use.
     * @param dataverseApiClient The Dataverse API client to use.
     * @param labourRelations    The labour relations to use.
     * @param timePeriods        The time periods to use.
     * @param cache              The cache holding datasets.
     */
    @Inject
    public LabourTreeMapBuilder(Config config, StandardDataset standardDataset, DataverseApiClient dataverseApiClient,
                                LabourRelations labourRelations, TimePeriods timePeriods,
                                Cache<String, TabularData> cache) {
        this.config = config;
        this.standardDataset = standardDataset;
        this.dataverseApiClient = dataverseApiClient;
        this.labourRelations = labourRelations;
        this.timePeriods = timePeriods;
        this.cache = cache;
        this.filterInfoBuilder = new FilterInfoBuilder(config.labour.treemap.empty, Collections.singleton("bmyear"));
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
    public LabourTreemapInfo getTreemap(Request request) throws LabourTreemapException {
        TabularData data = getTabularData(request);
        TabularData filterData = filterData(request, data);

        Treemap treemap = buildTreemap(request, filterData);
        List<FilterInfo> filterInfo = buildFilterInfo(request, filterData);
        Map<String, String> timePeriods = this.timePeriods.getTimePeriodsFor(filterData);

        return new LabourTreemapInfo(treemap, filterInfo, labourRelations.getLegend(), timePeriods);
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
                else {
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
     * If the request specifies filters, then apply those filters on the dataset.
     *
     * @param request The filter request.
     * @param data    The data set.
     * @return The filtered dataset.
     */
    private TabularData filterData(Request request, TabularData data) {
        Set<TabularDataFilter> filters = new HashSet<>();

        for (Map.Entry<String, String[]> entry : request.queryMap().toMap().entrySet()) {
            if (entry.getKey().startsWith("filter:")) {
                Set<String> values = Arrays.asList(entry.getValue())
                        .stream()
                        .filter(v -> !v.trim().isEmpty())
                        .collect(Collectors.toSet());

                if (!values.isEmpty()) {
                    filters.add(new ValuesTabularDataFilter(
                            entry.getKey().substring(7), values, values.contains(config.labour.treemap.empty)
                    ));
                }
            }

            if (entry.getKey().startsWith("min:")) {
                filters.add(new MinimalTabularDataFilter(
                        entry.getKey().substring(4), Utils.getBigDecimal(entry.getValue()[0])
                ));
            }

            if (entry.getKey().startsWith("max:")) {
                filters.add(new MaximumTabularDataFilter(
                        entry.getKey().substring(4), Utils.getBigDecimal(entry.getValue()[0])
                ));
            }
        }

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

        TreemapBuilder treemapBuilder =
                new TreemapBuilder(data, hierarchy, request.queryParams("size"), config.labour.treemap.empty);
        treemapBuilder.setRoundSize(true);
        treemapBuilder.setColorColumn("color");
        treemapBuilder.setSuffixMap(config.labour.treemap.suffix);

        if (request.queryParams("multiples") != null)
            treemapBuilder.setMultiples(config.labour.treemap.multiples);

        return treemapBuilder.getTreeMap("Labour relations");
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
        List<String> filter = (filterInfoArr != null) ? Arrays.asList(filterInfoArr) : Collections.emptyList();

        filter = new ArrayList<>(filter);
        if (filter.contains("bmyear"))
            filter.add("year");

        return filterInfoBuilder.getFilterInfo(data, Utils.filterOutEmpty(filter));
    }
}
