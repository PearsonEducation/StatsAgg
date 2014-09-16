package com.pearson.statsagg.metric_aggregation;

import java.math.BigDecimal;

/**
 * @author Jeffrey Schmidt
 */
public interface GenericMetricFormat {
    
    public String getMetricKey();
    
    public Long getMetricHashKey();
    
    public BigDecimal getMetricValueBigDecimal();
    
    public Long getMetricTimestampInMilliseconds();
    
    public Long getMetricReceivedTimestampInMilliseconds();
    
}
