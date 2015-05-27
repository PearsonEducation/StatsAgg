package com.pearson.statsagg.metric_aggregation.influxdb;

import com.pearson.statsagg.utilities.Json;
import com.pearson.statsagg.utilities.StringUtilities;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final ArrayList<Object> points_;
    private final long time_;
    private final byte timePrecision_;
    private long metricReceivedTimestampInMilliseconds_ = -1;
    
    private ArrayList<String> metricKeys_ = null;
    private ArrayList<BigDecimal> metricValues_ = null;
    
    public InfluxdbMetric(String database, String username, String password, String name, ArrayList<String> columns, ArrayList<Object> points, 
            long metricReceivedTimestampInMilliseconds) {
        this.database_ = database;
        this.username_ = username;
        this.password_ = password;
        this.name_ = name;
        this.columns_ = columns;
        this.points_ = points;
        this.time_ = getTime();
        this.timePrecision_ = getTimePrecision();
        
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        createMetricKeysAndMetricValues(name, columns, points);
        
        if (this.columns_ != null) this.columns_.trimToSize();
        if (this.points_ != null) this.points_.trimToSize();
        if (this.metricKeys_ != null) this.metricKeys_.trimToSize();
        if (this.metricValues_ != null) this.metricValues_.trimToSize();
    }
    
    public InfluxdbMetric(String database, String username, String password, String name, ArrayList<String> columns, ArrayList<Object> points, 
            long time, byte timePrecision, long metricReceivedTimestampInMilliseconds, ArrayList<String> metricKeys) {
        this.database_ = database;
        this.username_ = username;
        this.password_ = password;
        this.name_ = name;
        this.columns_ = columns;
        this.points_ = points;
        this.time_ = time;
        this.timePrecision_ = timePrecision;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        this.metricKeys_ = metricKeys;
    }
    
    public final long getTime() {
        
        if ((columns_ == null) || (points_ == null) || (columns_.size() != points_.size()) || columns_.isEmpty()) {
            return -1;
        }
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            
            if ((column != null) && column.equals("time")) {
                Object time = points_.get(i);
                if ((time != null) && (time instanceof Long)) return (long) time;
                break;
            }
        }
        
        return -1;
    }
    
    public final byte getTimePrecision() {
        
        if ((columns_ == null) || (points_ == null) || (columns_.size() != points_.size()) || columns_.isEmpty()) {
            return TIMESTAMP_PRECISION_UNKNOWN;
        }
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            
            if ((column != null) && column.equals("time_precision")) {
                Object timePrecision = points_.get(i);
                
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
    
    public final void createMetricKeysAndMetricValues(String name, ArrayList<String> columns, ArrayList<Object> points) {
        
        if (name == null) return;
        if ((columns_ == null) || (points_ == null) || (columns_.size() != points_.size()) || columns_.isEmpty()) return;
        
        metricKeys_ = new ArrayList<>();
        metricValues_ = new ArrayList<>();
        
        ArrayList<String> stringFields = new ArrayList<>();
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            Object point = points_.get(i);
            
            if ((point != null) && (point instanceof String)) {
                String stringField = name + "\"" + column + "\"=\"" + point + "\"";
                stringFields.add(stringField);
            }
        }
        
        Collections.sort(stringFields);
        
        StringBuilder metricKeyStringFields_StringBuilder = new StringBuilder();
        if (!stringFields.isEmpty()) metricKeyStringFields_StringBuilder.append(" : ");
        for (int i = 0; i < stringFields.size(); i++) {
            metricKeyStringFields_StringBuilder.append(stringFields.get(i));
            if ((i + 1) != stringFields.size()) metricKeyStringFields_StringBuilder.append(" ");
        }
        String metricKeyStringFields = metricKeyStringFields_StringBuilder.toString();
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            Object point = points_.get(i);
            
            if ((point != null) && Json.isObjectNumberic(point)) {
                String metricKey = name + " : \"" + column + "\" : " + metricKeyStringFields;
                metricKeys_.add(metricKey);
                
                BigDecimal metricValue = Json.convertBoxedPrimativeNumberToBigDecimal(point);
                metricValues_.add(metricValue);
            }
        }
        
        if (metricKeys_.isEmpty() && !stringFields.isEmpty()) {
            String metricKey = name + " : " + metricKeyStringFields;
            metricKeys_.add(metricKey);
            metricValues_.add(BigDecimal.ONE);
        }
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
    
}
