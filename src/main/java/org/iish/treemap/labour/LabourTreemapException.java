package org.iish.treemap.labour;

/**
 * Thrown on failure to build a labour relations treemap.
 */
public class LabourTreemapException extends Exception {
    public LabourTreemapException(String message) {
        super(message);
    }

    public LabourTreemapException(String message, Throwable cause) {
        super(message, cause);
    }
}
