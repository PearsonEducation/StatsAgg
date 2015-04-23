package com.pearson.statsagg.metric_aggregation;

import java.math.BigDecimal;

/**
 * @author Jeffrey Schmidt
 */
public interface GenericMetricFormat {
    
    public long getMetricHashKey();

    public String getMetricKey();
    
    public BigDecimal getMetricValueBigDecimal();

    public String getMetricValueString();
    
    public int getMetricTimestampInSeconds();
    
    public long getMetricTimestampInMilliseconds();
    
    public long getMetricReceivedTimestampInMilliseconds();
    
}
