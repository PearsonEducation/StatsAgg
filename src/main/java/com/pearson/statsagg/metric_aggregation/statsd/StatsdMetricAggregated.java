package com.pearson.statsagg.metric_aggregation.statsd;

import java.math.BigDecimal;
import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StatsdMetricAggregated implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
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
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(bucket_).append(" ").append(getMetricValueString()).append(" ").append(getMetricTimestampInSeconds());

        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(bucket_).append(" ").append(metricTimestampInMilliseconds_).append(" ").append(getMetricValueString()).append(" Format=StatsD");

        return stringBuilder.toString();
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
