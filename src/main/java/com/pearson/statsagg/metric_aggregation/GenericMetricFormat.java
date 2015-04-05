package com.pearson.statsagg.metric_aggregation;

import java.math.BigDecimal;

/**
 * @author Jeffrey Schmidt
 */
public interface GenericMetricFormat {
    
    public Long getMetricHashKey();

    public String getMetricKey();
    
    public BigDecimal getMetricValueBigDecimal();

    public String getMetricValueString();
    
    public long getMetricTimestampInMilliseconds();
    
    public long getMetricReceivedTimestampInMilliseconds();
    
}
