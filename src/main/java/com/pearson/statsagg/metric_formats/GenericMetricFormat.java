package com.pearson.statsagg.metric_formats;

import java.math.BigDecimal;

/**
 * @author Jeffrey Schmidt
 */
public interface GenericMetricFormat {
    
    public long getMetricHashKey();
    
    public void setMetricHashKey(long hashKey);

    public String getMetricKey();
        
    public BigDecimal getMetricValueBigDecimal();

    public String getMetricValueString();
    
    public int getMetricTimestampInSeconds();
    
    public long getMetricTimestampInMilliseconds();
    
    public long getMetricReceivedTimestampInMilliseconds();
    
}
