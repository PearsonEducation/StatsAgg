package com.pearson.statsagg.metric_formats.influxdb;

import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.JsonUtils;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.boon.Boon;
import org.boon.core.value.LazyValueMap;
import org.boon.core.value.ValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * 
 * This object is intended to be compatible with the InfluxDB format used in InfluxDB v0.6x, v0.7x, v0.8x
 */
public class InfluxdbMetric_v1 implements InfluxdbMetricFormat_v1 {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbMetric_v1.class.getName());

    private long hashKey_ = -1;
    
    private final String database_;
    private final String username_;
    private final String password_;
    private final String basicAuth_;
    private final byte timePrecisionCode_;
    
    private final String namePrefix_;
    private final String name_;
    private final ArrayList<String> columns_;
    private final ArrayList<ArrayList<Object>> points_;
    private long metricsReceivedTimestampInMilliseconds_ = -1;
    
    private ArrayList<InfluxdbStandardizedMetric> influxdbStandardizedMetrics_ = null;
    private Boolean includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput_ = true;
    
    public InfluxdbMetric_v1(String database, String username, String password, String basicAuth, byte timePrecisionCode,
            String namePrefix, String name, ArrayList<String> columns, ArrayList<ArrayList<Object>> points, 
            long metricsReceivedTimestampInMilliseconds) {
        this.database_ = database;
        this.username_ = username;
        this.password_ = password;
        this.basicAuth_ = basicAuth;
        this.timePrecisionCode_ = timePrecisionCode;
        this.namePrefix_ = namePrefix;
        this.name_ = name;
        this.columns_ = columns;
        this.points_ = points;
        
        if (this.columns_ != null) this.columns_.trimToSize();
        if (this.points_ != null) {
            for (ArrayList point : points_) if (point != null) point.trimToSize();
            this.points_.trimToSize();
        }
        
        this.metricsReceivedTimestampInMilliseconds_ = metricsReceivedTimestampInMilliseconds;
        createInfluxdbStandardizedMetrics(namePrefix);

        if (this.influxdbStandardizedMetrics_ != null) this.influxdbStandardizedMetrics_.trimToSize();
    }

    private void createInfluxdbStandardizedMetrics(String namePrefix) {
        
        if ((name_ == null) || (columns_ == null) || (points_ == null) || columns_.isEmpty() || points_.isEmpty()) return;

        influxdbStandardizedMetrics_ = new ArrayList<>();

        for (ArrayList<Object> point : points_) {
            if ((point == null) || (point.size() != columns_.size())) continue;
            
            ArrayList<InfluxdbStandardizedMetric> influxdbStandardizedMetrics = new ArrayList<>();
            long time = getTimeFromPoint(point);
            String metricKeyStringFields = getSortedStringFieldsForMetricKey(columns_, point);

            ArrayList<InfluxdbStandardizedMetric> influxdbStandardizedMetricsFromNumericPointValues = getInfluxdbStandardizedMetricsFromNumericPointValues(columns_,  
                    point, namePrefix, metricKeyStringFields, time);
            if (influxdbStandardizedMetricsFromNumericPointValues != null) influxdbStandardizedMetrics.addAll(influxdbStandardizedMetricsFromNumericPointValues);

            if (influxdbStandardizedMetrics.isEmpty()) {
                InfluxdbStandardizedMetric influxdbStandardizedMetric = getInfluxdbStandardizedMetricFromStringFieldsOnly(point, namePrefix, metricKeyStringFields, time);
                if (influxdbStandardizedMetricsFromNumericPointValues != null) influxdbStandardizedMetrics.add(influxdbStandardizedMetric);
            }
            
            influxdbStandardizedMetrics_.addAll(influxdbStandardizedMetrics);
        }
            
    }

    // create list of sorted string key/value pairs of influxdb fields (non-numeric fields).
    // this is treated as the influxdb version of opentsdb tags
    private String getSortedStringFieldsForMetricKey(ArrayList<String> columns, ArrayList<Object> point) {
        
        if ((columns == null) || (point == null) || (columns.size() != point.size()) || point.isEmpty()) {
            return null;
        }
        
        ArrayList<String> stringFields = new ArrayList<>();
        
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            Object pointColumnValue = point.get(i);
            if ((column == null) || column.equals("sequence_number")) continue;

            if ((pointColumnValue != null) && (pointColumnValue instanceof String)) {
                String pointString = (String) pointColumnValue;
                String stringField = "\"" + column + "\"=\"" + pointString + "\"";
                stringFields.add(stringField);
            }
        }

        Collections.sort(stringFields);

        StringBuilder metricKeyStringFields_StringBuilder = new StringBuilder();
        for (int j = 0; j < stringFields.size(); j++) {
            metricKeyStringFields_StringBuilder.append(stringFields.get(j));
            if ((j + 1) != stringFields.size()) metricKeyStringFields_StringBuilder.append(" ");
        }

        String metricKeyStringFields = metricKeyStringFields_StringBuilder.toString();
        return metricKeyStringFields;
    }
    
    private ArrayList<InfluxdbStandardizedMetric> getInfluxdbStandardizedMetricsFromNumericPointValues(ArrayList<String> columns, ArrayList<Object> point, 
            String namePrefix, String metricKeyStringFields, long time) {
        
        if ((columns == null) || (point == null) || (columns.size() != point.size()) || point.isEmpty()) {
            return new ArrayList<>();
        }
        
        ArrayList<InfluxdbStandardizedMetric> influxdbStandardizedMetrics = new ArrayList<>();
        
        for (int i = 0; i < point.size(); i++) {
            Object pointColumnValue = point.get(i);
            String column = columns_.get(i);
            if ((pointColumnValue == null) || !JsonUtils.isObjectNumberic(pointColumnValue, true)) continue;
            if ((column == null) || column.equals("time") || column.equals("sequence_number")) continue;
            
            StringBuilder metricKey = new StringBuilder();
            metricKey.append(database_).append(" : ");
            if (namePrefix != null) metricKey.append(namePrefix);
            metricKey.append(name_).append(" : ").append(column);
            if ((metricKeyStringFields != null) && !metricKeyStringFields.isEmpty()) metricKey.append(" : ").append(metricKeyStringFields);
            
            BigDecimal metricValue = JsonUtils.convertNumericObjectToBigDecimal(pointColumnValue, true);

            long metricTimestamp;
            byte metricTimestampPrecision;

            if ((time >= 0) && (timePrecisionCode_ != Common.TIMESTAMP_PRECISION_UNKNOWN)) {
                metricTimestamp = time;
                metricTimestampPrecision = timePrecisionCode_;
            }
            else if ((time >= 0) && (timePrecisionCode_ == Common.TIMESTAMP_PRECISION_UNKNOWN)) {
                metricTimestamp = time;
                metricTimestampPrecision = Common.TIMESTAMP_PRECISION_MILLISECONDS;
            }
            else {
                metricTimestamp = metricsReceivedTimestampInMilliseconds_;
                metricTimestampPrecision = Common.TIMESTAMP_PRECISION_MILLISECONDS;
            }

            InfluxdbStandardizedMetric influxdbStandardizedMetric = new InfluxdbStandardizedMetric(metricKey.toString(), database_, namePrefix, 
                    name_, column, metricValue, metricTimestamp, metricTimestampPrecision,
                    metricsReceivedTimestampInMilliseconds_, columns_, point, includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput_);
            influxdbStandardizedMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            
            influxdbStandardizedMetrics.add(influxdbStandardizedMetric);
        }
        
        return influxdbStandardizedMetrics;
    }
    
    private InfluxdbStandardizedMetric getInfluxdbStandardizedMetricFromStringFieldsOnly(ArrayList<Object> point, 
            String namePrefix, String metricKeyStringFields, long time) {
        
        if ((metricKeyStringFields == null) || metricKeyStringFields.isEmpty()) {
            return null;
        }
              
        StringBuilder metricKey = new StringBuilder();
        metricKey.append(database_).append(" : ");
        if (namePrefix != null) metricKey.append(namePrefix);
        metricKey.append(name_).append(" : ").append(metricKeyStringFields);

        long metricTimestamp;
        byte metricTimestampPrecision;

        if ((time >= 0) && (timePrecisionCode_ != Common.TIMESTAMP_PRECISION_UNKNOWN)) {
            metricTimestamp = time;
            metricTimestampPrecision = timePrecisionCode_;
        }
        else if ((time >= 0) && (timePrecisionCode_ == Common.TIMESTAMP_PRECISION_UNKNOWN)) {
            metricTimestamp = time;
            metricTimestampPrecision = Common.TIMESTAMP_PRECISION_MILLISECONDS;
        }
        else {
            metricTimestamp = metricsReceivedTimestampInMilliseconds_;
            metricTimestampPrecision = Common.TIMESTAMP_PRECISION_MILLISECONDS;
        }

        InfluxdbStandardizedMetric influxdbStandardizedMetric = new InfluxdbStandardizedMetric(metricKey.toString(), database_, namePrefix, name_, 
                null, BigDecimal.ONE, metricTimestamp, metricTimestampPrecision, metricsReceivedTimestampInMilliseconds_, columns_, point, 
                includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput_);
        influxdbStandardizedMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());

        return influxdbStandardizedMetric;
    }

    private long getTimeFromPoint(ArrayList<Object> point) {
        
        if ((columns_ == null) || (point == null) || (columns_.size() != point.size()) || columns_.isEmpty() || point.isEmpty()) {
            return -1;
        }
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            
            if ((column != null) && column.equals("time")) {
                Object time = point.get(i);
                
                if (time != null) {
                    if (time instanceof Long) {
                        Long timeLong = (Long) time;
                        return timeLong;
                    }
                    else if (time instanceof Integer) {
                        Integer timeInteger = (Integer) time;
                        return timeInteger.longValue();
                    }
                }
                
                break;
            }
        }
        
        return -1;
    }
    
    public void setIncludeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput(boolean includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput) {
        this.includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput_ = includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput;
        
        if (this.influxdbStandardizedMetrics_ == null) return;

        for (InfluxdbStandardizedMetric influxdbStandardizedMetric : influxdbStandardizedMetrics_) {
            influxdbStandardizedMetric.setIncludeDatabaseInNonNativeOuput(includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput);
        }
    }
    
    @Override
    public String getInfluxdbV1JsonFormatString() {

        if (name_ == null) return null;

        StringBuilder influxdbJson = new StringBuilder();

        influxdbJson.append("{");

        influxdbJson.append("\"name\":\"");
        if (namePrefix_ != null) influxdbJson.append(StringEscapeUtils.escapeJson(namePrefix_));
        influxdbJson.append(StringEscapeUtils.escapeJson(name_)).append("\",");

        influxdbJson.append("\"columns\":[");

        if (columns_ != null) {
            for (int j = 0; j < columns_.size(); j++) {
                String column = columns_.get(j);
                if ((column == null) || column.isEmpty()) continue;

                influxdbJson.append("\"").append(StringEscapeUtils.escapeJson(column)).append("\"");

                if ((j + 1) != columns_.size()) influxdbJson.append(",");
            }
        }

        influxdbJson.append("],");

        if (points_ != null) {
            influxdbJson.append("\"points\":[");

            for (int j = 0; j < points_.size(); j++) {
                ArrayList<Object> point = points_.get(j);
                if ((point == null) || point.isEmpty()) continue;

                influxdbJson.append("[");

                for (int k = 0; k < point.size(); k++) {
                    Object pointObject = point.get(k);
                    if (pointObject == null) continue;

                    if (JsonUtils.isObjectNumberic(pointObject, true)) influxdbJson.append(JsonUtils.convertNumericObjectToString(pointObject, false));
                    else if (pointObject instanceof String) influxdbJson.append("\"").append(StringEscapeUtils.escapeJson((String) pointObject)).append("\"");

                    if ((k + 1) != point.size()) influxdbJson.append(",");
                }

                influxdbJson.append("]");

                if ((j + 1) != points_.size()) influxdbJson.append(",");
            }

            influxdbJson.append("]");
        }

        influxdbJson.append("}");
        
        return influxdbJson.toString();
    }
    
    public static byte getTimePrecisionCodeFromTimePrecisionString(String timePrecisionString) {
        
        if (timePrecisionString == null) {
            return Common.TIMESTAMP_PRECISION_UNKNOWN;
        }
        
        byte timePrecisionCode;

        if (timePrecisionString.equals("s")) timePrecisionCode = Common.TIMESTAMP_PRECISION_SECONDS;
        else if (timePrecisionString.equals("ms")) timePrecisionCode = Common.TIMESTAMP_PRECISION_MILLISECONDS;
        else if (timePrecisionString.equals("u")) timePrecisionCode = Common.TIMESTAMP_PRECISION_MICROSECONDS;
        else timePrecisionCode = Common.TIMESTAMP_PRECISION_UNKNOWN;

        return timePrecisionCode;
    }
    
    public static String getTimePrecisionStringFromTimePrecisionCode(byte timePrecisionCode) {
        
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_UNKNOWN) {
            return null;
        }
        
        String timePrecisionString;

        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) timePrecisionString = "s";
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) timePrecisionString = "ms";
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) timePrecisionString = "u";
        else timePrecisionString = null;

        return timePrecisionString;
    }
    
    public static int getMetricTimestampInSeconds(byte timePrecisionCode, long time) {
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) return (int) time;
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) return (int) (time / 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) return (int) (time / 1000000);
        else return (int) time;
    }
    
    public static long getMetricTimestampInMilliseconds(byte timePrecisionCode, long time) {
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) return time;
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) return (time * 1000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) return (time / 1000);
        else return time;
    }
    
    public static long getMetricTimestampInMicroseconds(byte timePrecisionCode, long time) {
        if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MICROSECONDS) return time;
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_SECONDS) return (time * 1000000);
        else if (timePrecisionCode == Common.TIMESTAMP_PRECISION_MILLISECONDS) return (time * 1000);
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

    public static List<InfluxdbMetric_v1> parseInfluxdbMetricJson(String database, String inputJson, String username, String password, String basicAuth, 
            String timePrecision, String namePrefix, long metricsReceivedTimestampInMilliseconds) {

        if ((inputJson == null) || inputJson.isEmpty() || (database == null) || (database.isEmpty())) {
            return new ArrayList<>();
        }
        
        ValueList parsedJsonObject = null;
        
        try {
            parsedJsonObject = (ValueList) Boon.fromJson(inputJson);
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (parsedJsonObject == null) return new ArrayList<>();
               
        List<InfluxdbMetric_v1> influxdbMetrics = new ArrayList<>();
        
        for (Object influxdbMetricJsonObject : parsedJsonObject) {
            try {
                LazyValueMap influxdbMetricObject = (LazyValueMap) influxdbMetricJsonObject;
                
                String name = (String) influxdbMetricObject.get("name");
                
                ArrayList<String> columns = new ArrayList<>();
                ValueList columnsObjects = (ValueList) influxdbMetricObject.get("columns");
                if (columnsObjects != null) {
                    for (Object columnObject : columnsObjects) {
                        if (columnObject instanceof String) columns.add((String) columnObject);
                    }
                }
                
                ArrayList<ArrayList<Object>> points = new ArrayList<>();
                ValueList pointObjects = (ValueList) influxdbMetricObject.get("points");
                
                for (Object pointObject : pointObjects) {
                    ValueList point = (ValueList) pointObject;
                    if ((point == null) || point.isEmpty()) continue;
                    
                    ArrayList<Object> pointColumnValues = new ArrayList<>();
                    
                    for (Object pointColumnValue : point) {
                        if (pointColumnValue != null) pointColumnValues.add(pointColumnValue);
                    }
                    
                    points.add(pointColumnValues);
                }
                
                byte timePrecisionCode = getTimePrecisionCodeFromTimePrecisionString(timePrecision);

                InfluxdbMetric_v1 influxdbMetric = new InfluxdbMetric_v1(database, username, password, basicAuth, timePrecisionCode, 
                        namePrefix, name, columns, points, metricsReceivedTimestampInMilliseconds);
                
                influxdbMetrics.add(influxdbMetric);
            }
            catch (Exception e) {
                logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return influxdbMetrics;
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

    public byte getTimePrecisionCode() {
        return timePrecisionCode_;
    }

    public String getNamePrefix() {
        return namePrefix_;
    }

    public String getName() {
        return name_;
    }

    public ArrayList<String> getColumns() {
        return columns_;
    }

    public ArrayList<ArrayList<Object>> getPoints() {
        return points_;
    }

    public long getMetricsReceivedTimestampInMilliseconds() {
        return metricsReceivedTimestampInMilliseconds_;
    }

    public ArrayList<InfluxdbStandardizedMetric> getInfluxdbStandardizedMetrics() {
        return influxdbStandardizedMetrics_;
    }

    public boolean isIncludeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput() {
        return includeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput_;
    }

}
