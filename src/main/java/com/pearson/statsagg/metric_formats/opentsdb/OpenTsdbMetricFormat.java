package com.pearson.statsagg.metric_formats.opentsdb;

/**
 * @author Jeffrey Schmidt
 */
public interface OpenTsdbMetricFormat {

    public String getOpenTsdbTelnetFormatString();
    
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetrics);
    
    public String getOpenTsdbJsonFormatString();
    
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetrics);
}
