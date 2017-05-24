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
    public WorldPopulation worldPopulation;
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
        public VirtualColumns virtualColumns;
    }

    public static class XlsColumns {
        public String year;
        public String total;
        public String country;
        public String labourRelationLevel1;
        public String labourRelationLevel2;
        public String labourRelationLevel3;
    }

    public static class VirtualColumns {
        public String labRel11;
        public String labRel12;
        public String labRel13;
        public String labRel21;
        public String labRel22;
        public String labRel23;
        public String labRel31;
        public String labRel32;
        public String labRel33;
        public String code;
        public String labRelMultiple11;
        public String labRelMultiple12;
        public String labRelMultiple13;
        public String labRelMultiple21;
        public String labRelMultiple22;
        public String labRelMultiple23;
        public String labRelMultiple31;
        public String labRelMultiple32;
        public String labRelMultiple33;
        public String codeMultiple;
        public String color;
        public String bmyear;
        public String continent;
    }

    public static class Treemap {
        public String rootLabel;
        public Map<String, String> labels;
        public Map<String, String> empty;
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
        public String code;
        public int[] range;
    }

    public static class TimePeriod {
        public int timePeriod;
        public int minYear;
        public int maxYear;
    }

    public static class WorldPopulation {
        public String label;
        public String color;
        public String code;
        public Map<Integer, Map<String, Number>> totals;
    }
}
