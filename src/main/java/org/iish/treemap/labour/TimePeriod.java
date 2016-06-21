package org.iish.treemap.labour;

/**
 * Represents a time period.
 */
public class TimePeriod {
    private int timePeriod;
    private int minYear;
    private int maxYear;

    /**
     * Creates a time period.
     *
     * @param timePeriod The year representing the time period.
     * @param minYear    The min year to belong to this time period.
     * @param maxYear    The max year to belong to this time period.
     */
    public TimePeriod(int timePeriod, int minYear, int maxYear) {
        this.timePeriod = timePeriod;
        this.minYear = minYear;
        this.maxYear = maxYear;
    }

    /**
     * The year representing the time period.
     *
     * @return A year.
     */
    public int getTimePeriod() {
        return timePeriod;
    }

    /**
     * The year representing the time period as a string.
     *
     * @return A year.
     */
    public String getTimePeriodString() {
        return String.valueOf(timePeriod);
    }

    /**
     * The min year to belong to this time period.
     *
     * @return The minimal year.
     */
    public int getMinYear() {
        return minYear;
    }

    /**
     * The max year to belong to this time period.
     *
     * @return The maximal year.
     */
    public int getMaxYear() {
        return maxYear;
    }

    /**
     * Whether the given year is within this time period.
     *
     * @param year The year to check.
     * @return True if the year falls within the time period.
     */
    public boolean isWithinTimePeriod(int year) {
        return (year >= minYear) && (year < maxYear);
    }
}
