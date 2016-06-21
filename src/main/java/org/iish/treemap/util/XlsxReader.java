package org.iish.treemap.util;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A basic streaming Excel (XLSX) reader.
 */
public class XlsxReader {
    private File xlsxFile;

    /**
     * Parses the given Excel file.
     *
     * @param xlsxFile An Excel file.
     */
    public XlsxReader(File xlsxFile) {
        this.xlsxFile = xlsxFile;
    }

    /**
     * Parses the given Excel input stream.
     *
     * @param xlsxInputStream An Excel input stream.
     * @throws IOException Thrown on failure to create a temporary file for the input stream.
     */
    public XlsxReader(InputStream xlsxInputStream) throws IOException {
        Path xlsxFilePath = Files.createTempFile(null, ".xlsx");
        this.xlsxFile = xlsxFilePath.toFile();
        Files.copy(xlsxInputStream, xlsxFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Start reading the Excel file.
     *
     * @param rowConsumer The consumer to send read events to.
     */
    public void read(Consumer<Row> rowConsumer) throws XlsxException {
        try {
            OPCPackage opcPackage = OPCPackage.open(this.xlsxFile);
            XSSFReader reader = new XSSFReader(opcPackage);

            ContentHandler handler = new XSSFSheetXMLHandler(
                    null, new ReadOnlySharedStringsTable(opcPackage), new ContentsHandler(rowConsumer), false);

            InputStream sheetStream = reader.getSheetsData().next();
            InputSource sheetSource = new InputSource(sheetStream);

            XMLReader sheetParser = SAXHelper.newXMLReader();
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        }
        catch (Exception e) {
            throw new XlsxException("Failure to read an Excel file", e);
        }
    }

    /**
     * Represents a row in an Excel file.
     */
    public class Row {
        public int index;
        public int lastColNumber;
        public Map<Integer, Cell> cells;
    }

    /**
     * Represents a cell in an Excel file.
     */
    public class Cell {
        public CellReference cellReference;
        public String value;
    }

    /**
     * A handler class that listens to the read events and builds Row and Cell objects to pass on.
     */
    private class ContentsHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private Consumer<Row> consumer;
        private Map<Integer, Cell> cells;
        private int lastColNumber;

        /**
         * Creates a handler class with the given consumer of rows.
         *
         * @param consumer A consumer that accepts Row objects.
         */
        private ContentsHandler(Consumer<Row> consumer) {
            this.consumer = consumer;
        }

        /**
         * Listens to events when the XLSX reader hits a new row.
         *
         * @param rowNum The number of the row.
         */
        @Override
        public void startRow(int rowNum) {
            cells = new HashMap<>();
        }

        /**
         * Listens to events when the XLSX reader hits the end of a row.
         *
         * @param rowNum he number of the row.
         */
        @Override
        public void endRow(int rowNum) {
            Row row = new Row();
            row.index = rowNum;
            row.lastColNumber = lastColNumber;
            row.cells = cells;

            consumer.accept(row);
        }

        /**
         * Listens to events when the XLSX reader hits a cell in an Excel file.
         *
         * @param cellReference  The cell reference.
         * @param formattedValue The value of the cell.
         * @param comment        A comment.
         */
        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            Cell cell = new Cell();
            cell.cellReference = new CellReference(cellReference);
            cell.value = formattedValue;

            lastColNumber = (int) cell.cellReference.getCol();
            cells.put(lastColNumber, cell);
        }

        /**
         * Listens to events when the header or footer is hit by the XLSX reader. (Not used)
         *
         * @param text     The text.
         * @param isHeader Whether this is the header.
         * @param tagName  The name of the tag.
         */
        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
        }
    }
}
