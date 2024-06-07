package com.pearson.statsagg.metric_formats.influxdb;

import com.pearson.statsagg.metric_formats.GenericMetricFormat;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetricFormat;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * 
 * This object is intended to be compatible with the InfluxDB format used in InfluxDB v0.6x, v0.7x, v0.8x
 */
public class InfluxdbStandardizedMetric implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbStandardizedMetric.class.getName());
    
    private long hashKey_ = -1;
    
    private final String metricKey_;
    private final String metricDatabase_;
    private final String metricPrefix_;
    private final String metricName_;
    private final String metricValueName_;
    private final BigDecimal metricValue_;
    private final long metricTimestamp_;
    private final byte metricTimestampPrecision_;
    private final long metricReceivedTimestampInMilliseconds_;
    
    private final ArrayList<String> columns_;
    private final ArrayList<Object> point_;
    
    private boolean includeDatabaseInNonNativeOuput_ = true;
    
    public InfluxdbStandardizedMetric(String metricKey, String metricDatabase, String metricPrefix, String metricName, String metricValueName, 
            BigDecimal metricValue, long metricTimestamp, byte metricTimestampPrecision, long metricReceivedTimestampInMilliseconds,
            ArrayList<String> columns, ArrayList<Object> point, boolean includeDatabaseInNonNativeOuput) {
        this.metricKey_ = metricKey;
        this.metricDatabase_ = metricDatabase;
        this.metricPrefix_ = metricPrefix;
        this.metricName_ = metricName;
        this.metricValueName_ = metricValueName;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricTimestampPrecision_ = metricTimestampPrecision;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.columns_ = columns;
        this.point_ = point;
        this.includeDatabaseInNonNativeOuput_ = includeDatabaseInNonNativeOuput;
    }
    
    @Override
    public String getGraphiteFormatString(boolean sanitizeMetric, boolean substituteCharacters) {
        StringBuilder graphiteMetric = new StringBuilder();
        StringBuilder metricPath = new StringBuilder();
        
        if (includeDatabaseInNonNativeOuput_ && (metricDatabase_ != null)) metricPath.append(metricDatabase_).append(".");
        if (metricPrefix_ != null) metricPath.append(metricPrefix_);
        if (metricName_ != null) metricPath.append(metricName_);
        if (metricValueName_ != null) metricPath.append(".").append(metricValueName_);

        String metricPathString = GraphiteMetric.getGraphiteSanitizedString(metricPath.toString(), sanitizeMetric, substituteCharacters);
        
        graphiteMetric.append(metricPathString).append(" ").append(getMetricValueString()).append(" ").append(getMetricTimestampInSeconds());

        return graphiteMetric.toString();
    }
    
    @Override
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetric) {
        return getOpenTsdbTelnetFormatString(sanitizeMetric, null, null);
    }
    
    @Override
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue) {
        StringBuilder openTsdbTelnetMetric = new StringBuilder();
        StringBuilder metric = new StringBuilder();
        
        if (includeDatabaseInNonNativeOuput_ && (metricDatabase_ != null)) metric.append(metricDatabase_).append(".");
        if (metricPrefix_ != null) metric.append(metricPrefix_);
        if (metricName_ != null) metric.append(metricName_);
        if (metricValueName_ != null) metric.append(".").append(metricValueName_);
        
        String metricString = sanitizeMetric ? OpenTsdbMetric.getOpenTsdbSanitizedString(metric.toString()) : metric.toString();
        
        openTsdbTelnetMetric.append(metricString).append(" ").append(getMetricTimestampInMilliseconds()).append(" ").append(getMetricValueString()).append(" ");
        
        boolean didWriteAnyTag = false;
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            Object pointColumnValue = point_.get(i);
            
            if ((pointColumnValue != null) && (pointColumnValue instanceof String)) {
                String pointString = (String) pointColumnValue;
                
                if (sanitizeMetric) {
                    openTsdbTelnetMetric.append(OpenTsdbMetric.getOpenTsdbSanitizedString(column)).append("=").append(OpenTsdbMetric.getOpenTsdbSanitizedString(pointString));
                } 
                else {
                    openTsdbTelnetMetric.append(column).append("=").append(pointString);
                }
                
                didWriteAnyTag = true;
                if ((i + 1) != columns_.size()) openTsdbTelnetMetric.append(" ");
            }
        }
        
        if (!didWriteAnyTag && (defaultOpenTsdbTagKey != null)) openTsdbTelnetMetric.append(defaultOpenTsdbTagKey).append("=").append(defaultOpenTsdbTagValue);
        else if (!didWriteAnyTag) openTsdbTelnetMetric.append("Format").append("=").append("InfluxDB");
        
        return openTsdbTelnetMetric.toString();
    }
    
    @Override
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric) {
        return getOpenTsdbJsonFormatString(sanitizeMetric, null, null);
    }
    
    @Override
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue) {
                        
        StringBuilder metric = new StringBuilder();
        
        if (includeDatabaseInNonNativeOuput_ && (metricDatabase_ != null)) metric.append(metricDatabase_).append(".");
        if (metricPrefix_ != null) metric.append(metricPrefix_);
        if (metricName_ != null) metric.append(metricName_);
        if (metricValueName_ != null) metric.append(".").append(metricValueName_);
        
        String metricString = sanitizeMetric ? OpenTsdbMetric.getOpenTsdbSanitizedString(metric.toString()) : metric.toString();

        if ((metricString == null) || metricString.isEmpty()) return null;
        if (getMetricTimestampInMilliseconds() < 0) return null;
        if ((getMetricValue() == null)) return null;
        
        StringBuilder openTsdbJson = new StringBuilder();

        openTsdbJson.append("{");

        openTsdbJson.append("\"metric\":\"").append(StringEscapeUtils.escapeJson(metricString)).append("\",");
        openTsdbJson.append("\"timestamp\":").append(getMetricTimestampInMilliseconds()).append(",");
        openTsdbJson.append("\"value\":").append(getMetricValueString()).append(",");

        openTsdbJson.append("\"tags\":{");
        String openTsdbTags = getOpenTsdbTagsJsonFromInfluxColumnsAndPoints(sanitizeMetric, defaultOpenTsdbTagKey, defaultOpenTsdbTagValue);
        if (openTsdbTags != null) openTsdbJson.append(openTsdbTags);
        openTsdbJson.append("}");

        openTsdbJson.append("}");
        
        return openTsdbJson.toString();
    }

    private String getOpenTsdbTagsJsonFromInfluxColumnsAndPoints(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue) {
        
        if ((columns_ == null) || (point_ == null) || (columns_.size() != point_.size()) || point_.isEmpty()) {
            return null;
        }
        
        StringBuilder openTsdbTagsJson = new StringBuilder();
        boolean didWriteAnyTag = false;

        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            Object pointColumnValue = point_.get(i);
                        
            if ((pointColumnValue != null) && (pointColumnValue instanceof String)) {
                String pointString = (String) pointColumnValue;
                
                openTsdbTagsJson.append("\"");
                if (sanitizeMetric) openTsdbTagsJson.append(StringEscapeUtils.escapeJson(OpenTsdbMetric.getOpenTsdbSanitizedString(column)));
                else openTsdbTagsJson.append(StringEscapeUtils.escapeJson(column));
                
                openTsdbTagsJson.append("\":\"");
                
                if (sanitizeMetric) openTsdbTagsJson.append(StringEscapeUtils.escapeJson(OpenTsdbMetric.getOpenTsdbSanitizedString(pointString)));
                else openTsdbTagsJson.append(StringEscapeUtils.escapeJson(pointString));
                openTsdbTagsJson.append("\"");
                
                didWriteAnyTag = true;
                if ((i + 1) != columns_.size()) openTsdbTagsJson.append(",");
            }
        }

        if (!didWriteAnyTag && (defaultOpenTsdbTagKey != null)) openTsdbTagsJson.append("\"").append(defaultOpenTsdbTagKey).append("\":\"").append(defaultOpenTsdbTagValue).append("\"");
        else if (!didWriteAnyTag) openTsdbTagsJson.append("\"Format\":\"InfluxDB\"");
                        
        return openTsdbTagsJson.toString();
    }
    
    public long getHashKey() {
        return hashKey_;
    }
    
    @Override
    public long getMetricHashKey() {
        return getHashKey();
    }
    
    public void setHashKey(long hashKey) {
        this.hashKey_ = hashKey;
    }
    
    @Override
    public void setMetricHashKey(long hashKey) {
        setHashKey(hashKey);
    }
    
    @Override
    public String getMetricKey() {
        return metricKey_;
    }

    public String getMetricDatabase() {
        return metricDatabase_;
    }

    public String getMetricPrefix() {
        return metricPrefix_;
    }

    public String getMetricName() {
        return metricName_;
    }

    public String getMetricValueName() {
        return metricValueName_;
    }

    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public String getMetricValueString() {
        if (metricValue_ == null) return null;
        return MathUtilities.getFastPlainStringWithNoTrailingZeros(metricValue_);
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return metricValue_;
    }
    
    public long getMetricTimestamp() {
        return metricTimestamp_;
    }

    public byte getMetricTimestampPrecision() {
        return metricTimestampPrecision_;
    }
    
    @Override
    public int getMetricTimestampInSeconds() {
        return InfluxdbMetric_v1.getMetricTimestampInSeconds(metricTimestampPrecision_, metricTimestamp_);
    }
    
    @Override
    public long getMetricTimestampInMilliseconds() {
        return InfluxdbMetric_v1.getMetricTimestampInMilliseconds(metricTimestampPrecision_, metricTimestamp_);
    }

    @Override
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

    public ArrayList<String> getColumns() {
        return columns_;
    }

    public ArrayList<Object> getPoint() {
        return point_;
    }

    public boolean isIncludeDatabaseInNonNativeOuput() {
        return includeDatabaseInNonNativeOuput_;
    }

    public void setIncludeDatabaseInNonNativeOuput(boolean includeDatabaseInNonNativeOuput) {
        this.includeDatabaseInNonNativeOuput_ = includeDatabaseInNonNativeOuput;
    }
    
}
