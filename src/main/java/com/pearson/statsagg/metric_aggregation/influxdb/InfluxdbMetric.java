package com.pearson.statsagg.metric_aggregation.influxdb;

import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.Json;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.boon.Boon;
import org.boon.core.value.LazyValueMap;
import org.boon.core.value.ValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbMetric {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbMetric.class.getName());
    
    public static final byte TIMESTAMP_PRECISION_UNKNOWN = -1;
    public static final byte TIMESTAMP_PRECISION_SECONDS = 1;
    public static final byte TIMESTAMP_PRECISION_MILLISECONDS = 2;
    public static final byte TIMESTAMP_PRECISION_MICROSECONDS = 3;

    private long hashKey_ = -1;
    
    private final String database_;
    private final String username_;
    private final String password_;
    
    private final String name_;
    private final ArrayList<String> columns_;
    private final ArrayList<ArrayList<Object>> points_;
    private long metricsReceivedTimestampInMilliseconds_ = -1;
    
    private ArrayList<InfluxdbStatsAggMetric> influxdbStatsAggMetrics_ = null;
    
    public InfluxdbMetric(String database, String username, String password, String name, ArrayList<String> columns, ArrayList<ArrayList<Object>> points, long metricsReceivedTimestampInMilliseconds) {
        this.database_ = database;
        this.username_ = username;
        this.password_ = password;
        this.name_ = name;
        this.columns_ = columns;
        this.points_ = points;
        
        this.metricsReceivedTimestampInMilliseconds_ = metricsReceivedTimestampInMilliseconds;
        createInfluxdbStatsAggMetrics();

        if (this.influxdbStatsAggMetrics_ != null) this.influxdbStatsAggMetrics_.trimToSize();
        if (this.columns_ != null) this.columns_.trimToSize();
        if (this.points_ != null) {
            for (ArrayList point : points_) if (point != null) point.trimToSize();
            this.points_.trimToSize();
        }
    }
    
    public InfluxdbMetric(String database, String username, String password, String name, ArrayList<String> columns, ArrayList<ArrayList<Object>> points, 
            long metricsReceivedTimestampInMilliseconds, ArrayList<InfluxdbStatsAggMetric> influxdbStatsAggMetrics) {
        this.database_ = database;
        this.username_ = username;
        this.password_ = password;
        this.name_ = name;
        this.columns_ = columns;
        this.points_ = points;
        this.metricsReceivedTimestampInMilliseconds_ = metricsReceivedTimestampInMilliseconds;
        this.influxdbStatsAggMetrics_ = influxdbStatsAggMetrics;
    }
    
    private void createInfluxdbStatsAggMetrics() {
        
        if ((name_ == null) || (columns_ == null) || (points_ == null) || columns_.isEmpty() || points_.isEmpty()) return;

        influxdbStatsAggMetrics_ = new ArrayList<>();

        for (ArrayList<Object> point : points_) {
            if ((point == null) || (point.size() != columns_.size())) continue;
            
            ArrayList<InfluxdbStatsAggMetric> influxdbStatsAggMetrics = new ArrayList<>();
            long time = getTimeFromPoint(point);
            byte timePrecision = getTimePrecisionFromPoint(point);
            String metricKeyStringFields = getSortedStringFieldsForMetricKey(columns_, point);

            ArrayList<InfluxdbStatsAggMetric> influxdbStatsAggMetricsFromNumericPointValues = getInfluxdbStatsAggMetricsFromNumericPointValues(columns_, point, metricKeyStringFields, time, timePrecision);
            if (influxdbStatsAggMetricsFromNumericPointValues != null) influxdbStatsAggMetrics.addAll(influxdbStatsAggMetricsFromNumericPointValues);

            if (influxdbStatsAggMetrics.isEmpty()) {
                InfluxdbStatsAggMetric influxdbStatsAggMetric = getInfluxdbStatsAggMetricFromStringFieldsOnly(metricKeyStringFields, time, timePrecision);
                if (influxdbStatsAggMetricsFromNumericPointValues != null) influxdbStatsAggMetrics.add(influxdbStatsAggMetric);
            }
            
            influxdbStatsAggMetrics_.addAll(influxdbStatsAggMetrics);
        }
            
    }

    // create list of sorted string key/value pairs of influxdb fields (non-numeric fields).
    // statsagg treats this as the influxdb version of opentsdb tags
    private String getSortedStringFieldsForMetricKey(ArrayList<String> columns, ArrayList<Object> point) {
        
        if ((columns == null) || (point == null) || (columns.size() != point.size()) || point.isEmpty()) {
            return null;
        }
        
        ArrayList<String> stringFields = new ArrayList<>();
        
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            Object pointColumnValue = point.get(i);
            
            if ((pointColumnValue != null) && (pointColumnValue instanceof String)) {
                String pointString = (String) pointColumnValue;
                
                if (!pointString.equals("time_precision")) {
                    String stringField = "\"" + column + "\"=\"" + pointString + "\"";
                    stringFields.add(stringField);
                }
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
    
    private ArrayList<InfluxdbStatsAggMetric> getInfluxdbStatsAggMetricsFromNumericPointValues(ArrayList<String> columns, ArrayList<Object> point, String metricKeyStringFields, long time, byte timePrecision) {
        
        if ((columns == null) || (point == null) || (columns.size() != point.size()) || point.isEmpty()) {
            return new ArrayList<>();
        }
        
        ArrayList<InfluxdbStatsAggMetric> influxdbStatsAggMetrics = new ArrayList<>();
        
        for (int i = 0; i < point.size(); i++) {
            Object pointColumnValue = point.get(i);
            String column = columns_.get(i);
            if ((pointColumnValue == null) || !Json.isObjectNumberic(pointColumnValue)) continue;
            if (column.equals("time") || column.equals("time_precision") || column.equals("sequence_number")) continue;
            
            StringBuilder metricKey = new StringBuilder();
            metricKey.append(database_).append(" : ").append(name_).append(" : ").append(column);
            if ((metricKeyStringFields != null) && !metricKeyStringFields.isEmpty()) metricKey.append(" : ").append(metricKeyStringFields);
            BigDecimal metricValue = Json.convertBoxedPrimativeNumberToBigDecimal(pointColumnValue);

            long metricTimestamp;
            byte metricTimestampPrecision;

            if ((time >= 0) && (timePrecision != TIMESTAMP_PRECISION_UNKNOWN)) {
                metricTimestamp = time;
                metricTimestampPrecision = timePrecision;
            }
            else {
                metricTimestamp = metricsReceivedTimestampInMilliseconds_;
                metricTimestampPrecision = TIMESTAMP_PRECISION_MILLISECONDS;
            }

            InfluxdbStatsAggMetric influxdbStatsAggMetric = new InfluxdbStatsAggMetric(metricKey.toString(), metricValue, metricTimestamp, metricTimestampPrecision, metricsReceivedTimestampInMilliseconds_);
            influxdbStatsAggMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            influxdbStatsAggMetrics.add(influxdbStatsAggMetric);
        }
        
        return influxdbStatsAggMetrics;
    }
    
    private InfluxdbStatsAggMetric getInfluxdbStatsAggMetricFromStringFieldsOnly(String metricKeyStringFields, long time, byte timePrecision) {
        
        if ((metricKeyStringFields == null) || metricKeyStringFields.isEmpty()) {
            return null;
        }
                
        String metricKey = database_ + " : " + name_ + " : " + metricKeyStringFields;

        long metricTimestamp;
        byte metricTimestampPrecision;

        if ((time >= 0) && (timePrecision != TIMESTAMP_PRECISION_UNKNOWN)) {
            metricTimestamp = time;
            metricTimestampPrecision = timePrecision;
        }
        else {
            metricTimestamp = metricsReceivedTimestampInMilliseconds_;
            metricTimestampPrecision = TIMESTAMP_PRECISION_MILLISECONDS;
        }

        InfluxdbStatsAggMetric influxdbStatsAggMetric = new InfluxdbStatsAggMetric(metricKey, BigDecimal.ONE, metricTimestamp, 
                metricTimestampPrecision, metricsReceivedTimestampInMilliseconds_);
        influxdbStatsAggMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());

        return influxdbStatsAggMetric;
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
    
    private byte getTimePrecisionFromPoint(ArrayList<Object> point) {
        
        if ((columns_ == null) || (point == null) || (columns_.size() != point.size()) || columns_.isEmpty() || point.isEmpty()) {
            return TIMESTAMP_PRECISION_UNKNOWN;
        }
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            
            if ((column != null) && column.equals("time_precision")) {
                Object timePrecision = point.get(i);
                
                if ((timePrecision != null) && (timePrecision instanceof String)) {
                    if (timePrecision.equals("s")) return TIMESTAMP_PRECISION_SECONDS;
                    else if (timePrecision.equals("ms")) return TIMESTAMP_PRECISION_MILLISECONDS;
                    else if (timePrecision.equals("u")) return TIMESTAMP_PRECISION_MICROSECONDS;
                    else logger.warn("Invalid time_precision for InfluxDB. Name=\"" + StringUtilities.removeNewlinesFromString(name_) + "\"");
                }
                
                break;
            }
        }
        
        return TIMESTAMP_PRECISION_UNKNOWN;
    }
    
    public static int getMetricTimestampInSeconds(byte timePrecision, long time) {
        if (timePrecision == TIMESTAMP_PRECISION_SECONDS) return (int) time;
        else if (timePrecision == TIMESTAMP_PRECISION_MILLISECONDS) return (int) (time / 1000);
        else if (timePrecision == TIMESTAMP_PRECISION_MICROSECONDS) return (int) (time / 1000000);
        else return (int) time;
    }
    
    public static long getMetricTimestampInMilliseconds(byte timePrecision, long time) {
        if (timePrecision == TIMESTAMP_PRECISION_MILLISECONDS) return time;
        else if (timePrecision == TIMESTAMP_PRECISION_SECONDS) return (time * 1000);
        else if (timePrecision == TIMESTAMP_PRECISION_MICROSECONDS) return (time / 1000);
        else return time;
    }
    
    public static long getMetricTimestampInMicroseconds(byte timePrecision, long time) {
        if (timePrecision == TIMESTAMP_PRECISION_MICROSECONDS) return time;
        else if (timePrecision == TIMESTAMP_PRECISION_SECONDS) return (time * 1000000);
        else if (timePrecision == TIMESTAMP_PRECISION_MILLISECONDS) return (time * 1000);
        else return time;
    }
    
    public static String getInfluxdbJson(List<InfluxdbMetric> influxdbMetrics) {
        
        if (influxdbMetrics == null || influxdbMetrics.isEmpty()) {
            return null;
        }
        
        
        return "";
    }

    public static InfluxdbMetric parseInfluxdbMetricJson(String database, String inputJson, String username, String password, String metricPrefix, long metricsReceivedTimestampInMilliseconds) {

        if ((inputJson == null) || inputJson.isEmpty() || (database == null) || (database.isEmpty())) {
            return null;
        }
        
        ValueList parsedJsonObject = null;
        
        try {
            parsedJsonObject = (ValueList) Boon.fromJson(inputJson);
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (parsedJsonObject == null) return null;
                            
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

                InfluxdbMetric influxdbMetric = new InfluxdbMetric(database, username, password, name, columns, points, metricsReceivedTimestampInMilliseconds);
                return influxdbMetric;
            }
            catch (Exception e) {
                logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return null;
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

    public ArrayList<InfluxdbStatsAggMetric> getInfluxdbStatsAggMetrics() {
        return influxdbStatsAggMetrics_;
    }

}
