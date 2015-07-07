package com.pearson.statsagg.metric_formats.statsd;

import java.math.BigDecimal;
import com.pearson.statsagg.metric_formats.GenericMetricFormat;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetricFormat;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetricFormat_v1;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StatsdMetricAggregated implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat, InfluxdbMetricFormat_v1 {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsdMetricAggregated.class.getName());
   
    public static final byte COUNTER_TYPE = 1;
    public static final byte TIMER_TYPE = 2;
    public static final byte GAUGE_TYPE = 3;
    public static final byte SET_TYPE = 4;
    public static final byte UNDEFINED_TYPE = 5;
    
    private long hashKey_ = -1;

    private final String bucket_;
    private final BigDecimal metricValue_;
    private final long metricTimestampInMilliseconds_;
    private final byte metricTypeKey_;
        
    public StatsdMetricAggregated(String bucket, BigDecimal metricValue, long metricTimestampInMilliseconds, Byte metricTypeKey) {
        this.bucket_ = bucket;
        this.metricValue_ = metricValue;
        this.metricTimestampInMilliseconds_ = metricTimestampInMilliseconds;
        this.metricTypeKey_ = metricTypeKey;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(bucket_).append(" ").append(getMetricValueString()).append(" ").append(metricTimestampInMilliseconds_)
                .append(" ").append(getMetricTimestampInSeconds()).append(" ").append(metricTypeKey_);

        return stringBuilder.toString();
    }

    @Override
    public String getGraphiteFormatString(boolean sanitizeMetric, boolean substituteCharacters) {
        StringBuilder stringBuilder = new StringBuilder();
        
        String metricPath = GraphiteMetric.getGraphiteSanitizedString(bucket_, sanitizeMetric, substituteCharacters);

        stringBuilder.append(metricPath).append(" ").append(getMetricValueString()).append(" ").append(getMetricTimestampInSeconds());

        return stringBuilder.toString();
    }

    @Override
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetric) {
        StringBuilder stringBuilder = new StringBuilder();
        
        String metric = sanitizeMetric ? OpenTsdbMetric.getOpenTsdbSanitizedString(bucket_) : bucket_;
        
        stringBuilder.append(metric).append(" ").append(metricTimestampInMilliseconds_).append(" ").append(getMetricValueString()).append(" Format=StatsD");

        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric) {
        
        if ((bucket_ == null) || bucket_.isEmpty()) return null;
        if (getTimestampInMilliseconds() < 0) return null;
        if ((getMetricValue() == null)) return null;
        
        StringBuilder openTsdbJson = new StringBuilder();
   
        openTsdbJson.append("{");

        if (sanitizeMetric) openTsdbJson.append("\"metric\":\"").append(StringEscapeUtils.escapeJson(OpenTsdbMetric.getOpenTsdbSanitizedString(bucket_))).append("\",");
        else openTsdbJson.append("\"metric\":\"").append(StringEscapeUtils.escapeJson(bucket_)).append("\",");
        
        openTsdbJson.append("\"timestamp\":").append(getTimestampInMilliseconds()).append(",");
        openTsdbJson.append("\"value\":").append(getMetricValueString()).append(",");

        openTsdbJson.append("\"tags\":{");
        openTsdbJson.append("\"Format\":\"StatsD\"");
        openTsdbJson.append("}");

        openTsdbJson.append("}");
        
        return openTsdbJson.toString();
    }
    
    @Override
    public String getInfluxdbV1JsonFormatString() {

        if ((bucket_ == null) || bucket_.isEmpty()) return null;
        if (getTimestampInMilliseconds() < 0) return null;
        if ((getMetricValue() == null)) return null;

        StringBuilder influxdbJson = new StringBuilder();

        influxdbJson.append("{");

        // the metric name, with the prefix already built-in
        influxdbJson.append("\"name\":\"").append(StringEscapeUtils.escapeJson(bucket_)).append("\",");

        // column order: value, time, tag(s)
        influxdbJson.append("\"columns\":[\"value\",\"time\"],");

        // only include one point in the points array. note-- timestamp will always be sent to influxdb in milliseconds
        influxdbJson.append("\"points\":[[");
        influxdbJson.append(getMetricValueString()).append(",");
        influxdbJson.append(getTimestampInMilliseconds());
                    
        influxdbJson.append("]]}");

        return influxdbJson.toString();
    }
    
    public long getHashKey() {
        return this.hashKey_;
    }
    
    @Override
    public long getMetricHashKey() {
        return getHashKey();
    }
    
    public void setHashKey(long hashKey) {
        this.hashKey_ = hashKey;
    }

    public String getBucket() {
        return bucket_;
    }
    
    @Override
    public String getMetricKey() {
        return getBucket();
    }
    
    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return metricValue_;
    }
    
    @Override
    public String getMetricValueString() {
        if (metricValue_ == null) return null;
        return metricValue_.stripTrailingZeros().toPlainString();
    }
    
    public long getTimestampInMilliseconds() {
        return metricTimestampInMilliseconds_;
    }
    
    @Override
    public int getMetricTimestampInSeconds() {
        return (int) (metricTimestampInMilliseconds_ / 1000);
    }
    
    @Override
    public long getMetricTimestampInMilliseconds() {
        return getTimestampInMilliseconds();
    }
    
    @Override
    public long getMetricReceivedTimestampInMilliseconds() {
        return getTimestampInMilliseconds();
    }
    
    public byte getMetricTypeKey() {
        return metricTypeKey_;
    }

}
