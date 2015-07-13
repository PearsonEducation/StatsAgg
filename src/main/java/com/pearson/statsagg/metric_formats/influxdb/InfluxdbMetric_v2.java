package com.pearson.statsagg.metric_formats.influxdb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * 
 * WORK IN PROGRESS -- This object is intended to be compatible with the InfluxDB format used in InfluxDB v0.9x
 */
public class InfluxdbMetric_v2 {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbMetric_v2.class.getName());

    private long hashKey_ = -1;
    
    private final String database_;
    private final String username_;
    private final String password_;
    private final String basicAuth_;
    private final String retentionPolicy_;
    private final String consistency_;
    private final byte timePrecisionCode_;
    private final String namePrefix_;
    private final String name_;

    private long metricsReceivedTimestampInMilliseconds_ = -1;
    
    private ArrayList<InfluxdbStandardizedMetric> influxdbStandardizedMetrics_ = null;

    public InfluxdbMetric_v2(String database, String username, String password, String basicAuth, String retentionPolicy, String consistency, 
            byte timePrecisionCode, String namePrefix, String name, ArrayList<String> columns, ArrayList<ArrayList<Object>> points, 
            long metricsReceivedTimestampInMilliseconds) {
        this.database_ = database;
        this.username_ = username;
        this.password_ = password;
        this.basicAuth_ = basicAuth;
        this.retentionPolicy_ = retentionPolicy;
        this.consistency_ = consistency;
        this.timePrecisionCode_ = timePrecisionCode;
        this.namePrefix_ = namePrefix;
        this.name_ = name;
        
        this.metricsReceivedTimestampInMilliseconds_ = metricsReceivedTimestampInMilliseconds;
        //createInfluxdbStandardizedMetrics(namePrefix);

        if (this.influxdbStandardizedMetrics_ != null) this.influxdbStandardizedMetrics_.trimToSize();
    }
    
    public static byte getTimePrecisionCodeFromTimePrecisionString(String timePrecisionString) {
        
        if (timePrecisionString == null) {
            return Common.TIMESTAMP_PRECISION_UNKNOWN;
        }
        
        byte timePrecisionCode;
        
        if (timePrecisionString.equals("n")) timePrecisionCode = Common.TIMESTAMP_PRECISION_NANOSECONDS;
        else if (timePrecisionString.equals("u")) timePrecisionCode = Common.TIMESTAMP_PRECISION_MICROSECONDS;
        else if (timePrecisionString.equals("ms")) timePrecisionCode = Common.TIMESTAMP_PRECISION_MILLISECONDS;
        else if (timePrecisionString.equals("s")) timePrecisionCode = Common.TIMESTAMP_PRECISION_SECONDS;
        else if (timePrecisionString.equals("m")) timePrecisionCode = Common.TIMESTAMP_PRECISION_MINUTES;
        else if (timePrecisionString.equals("h")) timePrecisionCode = Common.TIMESTAMP_PRECISION_HOURS;

        else timePrecisionCode = Common.TIMESTAMP_PRECISION_UNKNOWN;

        return timePrecisionCode;
    }
    
    public static String getTimePrecisionStringFromTimePrecisionCode(byte timePrecisionCode) {
        
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_UNKNOWN) {
            return null;
        }
        
        String timePrecisionString;
        
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_NANOSECONDS) timePrecisionString = "n";
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) timePrecisionString = "u";
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) timePrecisionString = "ms";
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) timePrecisionString = "s";
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MINUTES) timePrecisionString = "m";
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_HOURS) timePrecisionString = "h";

        else timePrecisionString = null;

        return timePrecisionString;
    }
    
    public static long getMetricTimestampInSeconds(byte timePrecisionCode, long time) {
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_NANOSECONDS) return (time / 1000000000l);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) return (time / 1000000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) return (time / 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) return  time;
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MINUTES) return (time * 60);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_HOURS) return (time * 3600);
        else return (int) time;
    }
    
    public static long getMetricTimestampInMilliseconds(byte timePrecisionCode, long time) {
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_NANOSECONDS) return (time / 1000000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) return (time / 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) return time;
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) return (time * 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MINUTES) return (time * 60000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_HOURS) return (time * 3600000);
        else return time;
    }
    
    public static long getMetricTimestampInMicroseconds(byte timePrecisionCode, long time) {
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_NANOSECONDS) return (time / 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) return time;
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) return (time * 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) return (time * 1000000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MINUTES) return (time * 60000000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_HOURS) return (time * 3600000000l);
        else return time;
    }
    
    public static long getMetricTimestampInNanoseconds(byte timePrecisionCode, long time) {
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_NANOSECONDS) return time;
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) return (time * 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) return (time * 1000000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) return (time * 1000000000l);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MINUTES) return (time * 60000000000l);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_HOURS) return (time * 3600000000000l);
        else return time;
    }
    
    public static String getInfluxdbJson(InfluxdbMetricFormat_v1 influxdbMetric) {
        
        if (influxdbMetric == null) return null;
        
        StringBuilder influxdbJson = new StringBuilder();
        
        influxdbJson.append("[");
        influxdbJson.append(influxdbMetric.getInfluxdbV1JsonFormatString());
        influxdbJson.append("]");
        
        return influxdbJson.toString();
    }
    
    public static String getInfluxdbJson(List<? extends InfluxdbMetricFormat_v1> influxdbMetrics) {
        
        if (influxdbMetrics == null) return null;
        
        StringBuilder influxdbJson = new StringBuilder();
        
        influxdbJson.append("[");
        
        for (int i = 0; i < influxdbMetrics.size(); i++) {
            influxdbJson.append(influxdbMetrics.get(i).getInfluxdbV1JsonFormatString());
            if ((i + 1) != influxdbMetrics.size()) influxdbJson.append(",");
        }
        
        influxdbJson.append("]");
        
        return influxdbJson.toString();
    }

    public static List<InfluxdbMetric_v2> parseInfluxdbMetricLines(String database, String unparsedMetrics, String username, String password, String basicAuth, 
            String retentionPolicy, String consistency, String timePrecision, String namePrefix, long metricsReceivedTimestampInMilliseconds) {

        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty() || (database == null) || (database.isEmpty())) {
            return new ArrayList<>();
        }

        AtomicInteger index = new AtomicInteger(0);
        List<InfluxdbMetric_v2> influxdbMetrics = new ArrayList<>();

        // parse name
        parseInfluxdbMetricLines_Name(unparsedMetrics, index);
        
        // parse tags
        parseInfluxdbMetricLines_TagsAndFields(unparsedMetrics, index);
        
        // parse fields
        parseInfluxdbMetricLines_TagsAndFields(unparsedMetrics, index);
        
        return influxdbMetrics;
    }
    
    private static String parseInfluxdbMetricLines_Name(String unparsedMetrics, AtomicInteger index) {
        
        if ((unparsedMetrics == null) || (index == null)) {
            return null;
        }

        StringBuilder nameBuilder = new StringBuilder();
        
        int i = index.get();
        
        while (i < unparsedMetrics.length()) {
            char currentCharacter = unparsedMetrics.charAt(i);
            
            if (((currentCharacter == ',') || (currentCharacter == ' ') || (currentCharacter == '=')) && (i > 0) && (unparsedMetrics.charAt(i-1) == '\\')) {
                nameBuilder.append(currentCharacter);
                i++;
                continue;
            }
            
            if (((currentCharacter == ',') || (currentCharacter == ' ')) && (i > 0) && (unparsedMetrics.charAt(i-1) != '\\')) {
                i++;
                break;
            }
            
            i++;
            nameBuilder.append(currentCharacter);
        }
        
        return nameBuilder.toString();
    }
    
    private static List<InfluxdbKV> parseInfluxdbMetricLines_TagsAndFields(String unparsedMetrics, AtomicInteger index) {
        
        if ((unparsedMetrics == null) || (index == null)) {
            return new ArrayList<>();
        }
        
        List<InfluxdbKV> InfluxdbKVs = new ArrayList<>();

        boolean foundEndOfTags = false;
        int i = index.get();
        
        while (!foundEndOfTags && (i < unparsedMetrics.length())) {
            // parse key        
            StringBuilder tagKeyBuilder = new StringBuilder();
            while (i < unparsedMetrics.length()) {
                char currentCharacter = unparsedMetrics.charAt(i);

                if (((currentCharacter == ',') || (currentCharacter == ' ') || (currentCharacter == '=')) && (i > 0)) {
                    if (unparsedMetrics.charAt(i-1) == '\\') {
                        tagKeyBuilder.append(currentCharacter);
                        i++;
                        continue;
                    }
                    else {
                        i++;
                        break;
                    }
                }
                
                i++;
                tagKeyBuilder.append(currentCharacter);
            }

            // parse value   
            StringBuilder tagValueBuilder = new StringBuilder();
            while (i < unparsedMetrics.length()) {
                char currentCharacter = unparsedMetrics.charAt(i);

                if (((currentCharacter == ',') || (currentCharacter == ' ') || (currentCharacter == '=')) && (i > 0) && (unparsedMetrics.charAt(i-1) == '\\')) {
                    tagValueBuilder.append(currentCharacter);
                    i++;
                    continue;
                }

                if (((currentCharacter == ',') || (currentCharacter == ' ')) && (i > 0) && (unparsedMetrics.charAt(i-1) != '\\')) {
                    if ((currentCharacter == ' ')) foundEndOfTags = true;
                    i++;
                    break;
                }

                i++;
                tagValueBuilder.append(currentCharacter);
            }
            
            InfluxdbKV influxdbKV = new InfluxdbKV(tagKeyBuilder.toString(), tagValueBuilder.toString());
            InfluxdbKVs.add(influxdbKV);
        }
        
        index.set(i);
        
        return InfluxdbKVs;
    }
    
    public long getHashKey() {
        return hashKey_;
    }

    public void setHashKey(long hashKey) {
        this.hashKey_ = hashKey;
    }
    
    public String getDatabase() {
        return database_;
    }

    public String getUsername() {
        return username_;
    }

    public String getPassword() {
        return password_;
    }
    
    public String getBasicAuth() {
        return basicAuth_;
    }

    public String getRetentionPolicy() {
        return retentionPolicy_;
    }

    public String getConsistency() {
        return consistency_;
    }
    
    public byte getTimePrecisionCode() {
        return timePrecisionCode_;
    }

    public String getNamePrefix() {
        return namePrefix_;
    }

    public String getName() {
        return name_;
    }

    public long getMetricsReceivedTimestampInMilliseconds() {
        return metricsReceivedTimestampInMilliseconds_;
    }

    public ArrayList<InfluxdbStandardizedMetric> getInfluxdbStandardizedMetrics() {
        return influxdbStandardizedMetrics_;
    }

}
