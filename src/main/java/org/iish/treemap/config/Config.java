package org.iish.treemap.config;

import java.util.List;
import java.util.Map;

/**
 * Holds the configuration.
 */
public class Config {
    public String key;
    public Dataverse dataverse;
    public Cache cache;
    public Data labour;
    public LabourRelations labourRelations;
    public List<TimePeriod> timePeriods;
    public Map<String, String> countriesToContinent;

    public static class Dataverse {
        public String url;
        public int connectTimeoutMs;
        public int readTimeoutMs;
    }

    public static class Cache {
        public int maximumSize;
        public long maxHoursAccessTime;
    }

    public static class Data {
        public Xlsx xlsx;
        public Treemap treemap;
    }

    public static class Xlsx {
        public String empty;
        public XlsColumns columns;
    }

    public static class XlsColumns {
        public String year;
        public String country;
        public String labourRelationLevel1;
        public String labourRelationLevel2;
        public String labourRelationLevel3;
    }

    public static class Treemap {
        public String empty;
        public Map<String, String> suffix;
        public Map<String, String> multiples;
    }

    public static class LabourRelations {
        public Map<String, String> codes;

        public List<LabourRelationsLevel> level1;
        public List<LabourRelationsLevel> level2;

        public LabourRelationsLevel unknown;
        public LabourRelationsLevel multiple;
    }

    public static class LabourRelationsLevel {
        public String label;
        public String color;
        public int[] range;
    }

    public static class TimePeriod {
        public int timePeriod;
        public int minYear;
        public int maxYear;
    }
}
