package com.pearson.statsagg.metric_formats.opentsdb;

/**
 * @author Jeffrey Schmidt
 */
public interface OpenTsdbMetricFormat {
    
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetric);
    
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue);
    
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric);
    
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue);
    
}
