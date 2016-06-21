package org.iish.treemap.util;

/**
 * Thrown on failure to read an XLSX file.
 */
public class XlsxException extends Exception {
    public XlsxException(String message, Throwable cause) {
        super(message, cause);
    }
}
