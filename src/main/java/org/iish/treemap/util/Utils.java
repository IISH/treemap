package org.iish.treemap.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class.
 */
public class Utils {

    /**
     * Attempt to parse a value to an integer.
     *
     * @param value The value to parse.
     * @return The integer, or null if unable to parse.
     */
    public static Integer getInteger(String value) {
        try {
            return (value != null) ? Integer.parseInt(value) : null;
        }
        catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Attempt to parse a value to a BigDecimal.
     *
     * @param value The value to parse.
     * @return The BigDecimal, or null if unable to parse.
     */
    public static BigDecimal getBigDecimal(String value) {
        try {
            return (value != null) ? new BigDecimal(value) : null;
        }
        catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * From a list of values, filter out the empty values.
     *
     * @param list The list of values.
     * @return The filtered list of values.
     */
    public static List<String> filterOutEmpty(List<String> list) {
        return list.stream()
                .filter(column -> !column.trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * From a list of values, filter out the null values.
     *
     * @param list The list of values.
     * @return The filtered list of values.
     */
    public static List<String[]> filterOutNull(List<String[]> list) {
        return list.stream()
                .filter(values -> values != null)
                .collect(Collectors.toList());
    }
}
