package com.pearson.statsagg.metric_aggregation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricKeyLastSeen {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricKeyLastSeen.class.getName());

    // The most timestamp that this metric was received by this program
    private final Long metricKeyLastSeenTimestamp_Current_; 
    
    // The most timestamp that this metric was received by this program. 
    // This is intended to be refreshed every time the aggregation routine runs (even if no new metric is received)
    private final Long metricKeyLastSeenTimestamp_UpdateOnResend_;

    public MetricKeyLastSeen(Long metricKeyLastSeenTimestamp_Current, Long metricKeyLastSeenTimestamp_UpdateOnResend) {
        this.metricKeyLastSeenTimestamp_Current_ = metricKeyLastSeenTimestamp_Current;
        this.metricKeyLastSeenTimestamp_UpdateOnResend_ = metricKeyLastSeenTimestamp_UpdateOnResend;
    }
    
    public Long getMetricKeyLastSeenTimestamp_Current() {
        return metricKeyLastSeenTimestamp_Current_;
    }

    public Long getMetricKeyLastSeenTimestamp_UpdateOnResend() {
        return metricKeyLastSeenTimestamp_UpdateOnResend_;
    }
    
}
