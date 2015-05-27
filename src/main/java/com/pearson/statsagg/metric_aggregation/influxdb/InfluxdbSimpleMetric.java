package com.pearson.statsagg.metric_aggregation.influxdb;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbSimpleMetric {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbSimpleMetric.class.getName());

    private final String metricKey_;
    private final BigDecimal metricValue_;
    private final long metricTimestamp_;
    private final byte metricTimestampPrecision_;
    private final long metricReceivedTimestampInMilliseconds_;
    
    public InfluxdbSimpleMetric(String metricKey, BigDecimal metricValue, long metricTimestamp, byte metricTimestampPrecision, long metricReceivedTimestampInMilliseconds) {
        this.metricKey_ = metricKey;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricTimestampPrecision_ = metricTimestampPrecision;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }

    
    
}
