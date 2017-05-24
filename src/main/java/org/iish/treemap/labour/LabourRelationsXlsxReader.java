package org.iish.treemap.labour;

import org.iish.treemap.config.Config;
import org.iish.treemap.util.Utils;
import org.iish.treemap.model.tabular.TabularData;
import org.iish.treemap.util.XlsxException;
import org.iish.treemap.util.XlsxReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A reader that parses Excel labour relations data sets.
 */
public class LabourRelationsXlsxReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabourRelationsXlsxReader.class);
    private static final Pattern HEADERS = Pattern.compile("[\\W]");

    private Config config;
    private LabourRelations labourRelations;
    private TimePeriods timePeriods;
    private XlsxReader xlsxReader;

    /**
     * Builds an XLSX reader for the given labour relation information and data set.
     *
     * @param config          The configuration.
     * @param labourRelations The labour relations information.
     * @param timePeriods     The defined time periods.
     * @param xlsxReader      A default XLSX reader for a given Excel document.
     */
    public LabourRelationsXlsxReader(Config config, LabourRelations labourRelations, TimePeriods timePeriods,
                                     XlsxReader xlsxReader) {
        this.config = config;
        this.labourRelations = labourRelations;
        this.timePeriods = timePeriods;
        this.xlsxReader = xlsxReader;
    }

    /**
     * Builds an XLSX reader for the given labour relation information and data set.
     *
     * @param config          The configuration.
     * @param labourRelations The labour relations information.
     * @param timePeriods     The defined time periods.
     * @param xlsxFile        An XLSX file to read.
     */
    public LabourRelationsXlsxReader(Config config, LabourRelations labourRelations, TimePeriods timePeriods,
                                     File xlsxFile) {
        this.config = config;
        this.labourRelations = labourRelations;
        this.timePeriods = timePeriods;
        this.xlsxReader = new XlsxReader(xlsxFile);
    }

    /**
     * Builds an XLSX reader for the given labour relation information and data set.
     *
     * @param config          The configuration.
     * @param labourRelations The labour relations information.
     * @param timePeriods     The defined time periods.
     * @param xlsxInputStream The XLSX input stream to read.
     * @throws IOException On failure to read the input stream.
     */
    public LabourRelationsXlsxReader(Config config, LabourRelations labourRelations, TimePeriods timePeriods,
                                     InputStream xlsxInputStream) throws IOException {
        this.config = config;
        this.labourRelations = labourRelations;
        this.timePeriods = timePeriods;
        this.xlsxReader = new XlsxReader(xlsxInputStream);
    }

    /**
     * Returns the data set as a TabularData object.
     *
     * @return The data set, read from the given Excel file.
     * @throws XlsxException On failure to parse the Excel file.
     */
    public TabularData getData() throws XlsxException {
        Map<String, Integer> headers = new HashMap<>();
        ArrayList<String[]> data = new ArrayList<>();
        List<String> defaults = new ArrayList<>();

        xlsxReader.read((XlsxReader.Row row) -> {
            if (row.index == 0) {
                setHeaders(row, headers);
                defaults.addAll(Collections.nCopies(row.lastColNumber, null));
            }

            List<String> rowData = new ArrayList<>(defaults);
            if (setData(row, headers, rowData)) {
                data.add(rowData.toArray(new String[rowData.size()]));
            }
        });

        data.trimToSize();
        return new TabularData(headers, data);
    }

    /**
     * Reads the headers from the Excel file and creates new headers.
     *
     * @param row     The header row.
     * @param headers The map to hold all headers and their index.
     */
    private void setHeaders(XlsxReader.Row row, Map<String, Integer> headers) {
        row.cells.values().forEach(cell -> {
            String name = cell.value;
            name = HEADERS.matcher(name).replaceAll("").toLowerCase();
            headers.put(name, (int) cell.cellReference.getCol());
        });

        int i = row.lastColNumber;
        for (Field field : config.labour.xlsx.virtualColumns.getClass().getFields()) {
            try {
                headers.put(field.get(config.labour.xlsx.virtualColumns).toString(), i++);
            }
            catch (IllegalAccessException iae) {
                LOGGER.warn("Illegal access to virtual columns config class!", iae);
            }
        }
    }

    /**
     * Reads a row from the Excel file and parses the data from that row.
     *
     * @param row     The row to read.
     * @param headers Information on the headers.
     * @param rowData The parsed row data.
     * @return Whether the row was read and parsed. Returns false when the row was skipped.
     */
    private boolean setData(XlsxReader.Row row, Map<String, Integer> headers, List<String> rowData) {
        int labourRelation1Idx = headers.get(config.labour.xlsx.columns.labourRelationLevel1);

        if ((row.index > 2) && row.cells.containsKey(labourRelation1Idx)) {
            String labourRelation1Val = getValue(row.cells.get(labourRelation1Idx).value);

            if ((labourRelation1Val != null) && !labourRelation1Val.isEmpty()) {
                row.cells.values().forEach(cell -> {
                    if (cell.cellReference.getCol() < rowData.size())
                        rowData.set(cell.cellReference.getCol(), getValue(cell.value));
                });

                List<String> labourRelationsColumns = Arrays.asList(
                        config.labour.xlsx.columns.labourRelationLevel1,
                        config.labour.xlsx.columns.labourRelationLevel2,
                        config.labour.xlsx.columns.labourRelationLevel3
                );

                for (boolean combineMultiples : Arrays.asList(false, true)) {
                    for (String header : labourRelationsColumns) {
                        XlsxReader.Cell cell = row.cells.get(headers.get(header));
                        String value = (cell != null) ? getValue(cell.value) : null;

                        rowData.add(labourRelations.getLevel1(value, combineMultiples));
                        rowData.add(labourRelations.getLevel2(value, combineMultiples));
                        rowData.add(labourRelations.getLevel3(value, combineMultiples));
                    }
                    rowData.add(labourRelations.getCode(labourRelation1Val, combineMultiples));
                }

                rowData.add(labourRelations.getColor(labourRelation1Val));

                XlsxReader.Cell yearCell = row.cells.get(headers.get(config.labour.xlsx.columns.year));
                rowData.add(getTimePeriod(getValue(yearCell.value)));

                XlsxReader.Cell countryCell = row.cells.get(headers.get(config.labour.xlsx.columns.country));
                rowData.add(config.countriesToContinent.getOrDefault(getValue(countryCell.value), null));

                return true;
            }
        }

        return false;
    }

    /**
     * Parses a single value from the Excel file.
     *
     * @param value The value to parse.
     * @return The parsed value.
     */
    private String getValue(String value) {
        if (value == null)
            return null;

        value = value.trim();
        if (value.isEmpty() || value.equalsIgnoreCase(config.labour.xlsx.empty))
            return null;

        return value;
    }

    /**
     * Returns the time period to which the given year belongs.
     *
     * @param year The year.
     * @return The time period.
     */
    private String getTimePeriod(String year) {
        Integer yearInt = Utils.getInteger(year);
        if (yearInt == null)
            return null;

        return this.timePeriods.getTimePeriods().stream()
                .filter(tp -> tp.isWithinTimePeriod(yearInt))
                .findFirst()
                .map(TimePeriod::getTimePeriodString)
                .orElse(null);
    }
}
