package com.pearson.statsagg.metric_aggregation.graphite;

import java.math.BigDecimal;
import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteMetricAggregated implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteMetricAggregated.class.getName());
    
    private Long hashKey_ = null;
    
    private final String metricPath_;
    private final BigDecimal metricValue_;
    private final int metricTimestampInSeconds_;
    private final long metricTimestampInMilliseconds_;
    private final long metricReceivedTimestampInMilliseconds_;
    
    public GraphiteMetricAggregated(String metricPath, BigDecimal metricValue, long metricTimestampInMilliseconds, long metricReceivedTimestampInMilliseconds) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestampInSeconds_ = (int) (metricTimestampInMilliseconds / 1000);
        this.metricTimestampInMilliseconds_ = metricTimestampInMilliseconds;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
    public String createAndGetMetricValueString() {
        if (metricValue_ == null) return null;
        return metricValue_.stripTrailingZeros().toPlainString();
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(metricPath_).append(" ")
                .append(getMetricValueString()).append(" ")
                .append(metricTimestampInSeconds_).append(" ")
                .append(" @ ").append(metricReceivedTimestampInMilliseconds_);
        
        return stringBuilder.toString();
    }
    
    @Override
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(metricPath_).append(" ").append(getMetricValueString()).append(" ").append(metricTimestampInSeconds_);

        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(metricPath_).append(" ").append(metricTimestampInMilliseconds_).append(" ").append(getMetricValueString()).append(" Format=Graphite");

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
        return metricValue_;
    }
    
    @Override
    public String getMetricValueString() {
        return createAndGetMetricValueString();
    }
    
    public int getMetricTimestampInSeconds() {
        return metricTimestampInSeconds_;
    }
    
    @Override
    public long getMetricTimestampInMilliseconds() {
        return metricTimestampInMilliseconds_;
    }

    @Override
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }
    
}
