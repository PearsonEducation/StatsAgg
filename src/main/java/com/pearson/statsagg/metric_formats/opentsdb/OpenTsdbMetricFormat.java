package com.pearson.statsagg.metric_formats.opentsdb;

/**
 * @author Jeffrey Schmidt
 */
public interface OpenTsdbMetricFormat {

    public String getOpenTsdbTelnetFormatString();
    
    public String getOpenTsdbJsonFormatString();
    
}
