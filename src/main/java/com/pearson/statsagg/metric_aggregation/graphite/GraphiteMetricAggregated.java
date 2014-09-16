package com.pearson.statsagg.metric_aggregation.graphite;

import java.math.BigDecimal;
import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteMetricAggregated implements GraphiteMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteMetricAggregated.class.getName());
    
    private Long hashKey_ = null;
    
    private final String metricPath_;
    private final BigDecimal metricValue_;
    private final long metricTimestampInSeconds_;
    private final long metricTimestampInMilliseconds_;
    private final long metricReceivedTimestampInMilliseconds_;
    
    public GraphiteMetricAggregated(String metricPath, BigDecimal metricValue, long metricTimestampInMilliseconds, long metricReceivedTimestampInMilliseconds) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestampInSeconds_ = metricTimestampInMilliseconds / 1000;
        this.metricTimestampInMilliseconds_ = metricTimestampInMilliseconds;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metricPath_).append(" ")
                .append(metricValue_).append(" ")
                .append(metricTimestampInSeconds_).append(" ")
                .append(metricTimestampInMilliseconds_).append(" ")
                .append(metricReceivedTimestampInMilliseconds_);
        
        return stringBuilder.toString();
    }
    
    @Override
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metricPath_)
                .append(" ").append(metricValue_)
                .append(" ").append(metricTimestampInSeconds_);

        return stringBuilder.toString();
    }
    
    public Long getHashKey() {
        return this.hashKey_;
    }
    
    @Override
    public Long getMetricHashKey() {
        return getHashKey();
    }
    
    public void setHashKey(Long hashKey) {
        this.hashKey_ = hashKey;
    }
    
    public String getMetricPath() {
        return metricPath_;
    }
    
    @Override
    public String getMetricKey() {
        return getMetricPath();
    }
    
    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return getMetricValue();
    }
    
    public long getMetricTimestampInSeconds() {
        return metricTimestampInSeconds_;
    }
    
    @Override
    public Long getMetricTimestampInMilliseconds() {
        return metricTimestampInMilliseconds_;
    }

    @Override
    public Long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }
    
}
