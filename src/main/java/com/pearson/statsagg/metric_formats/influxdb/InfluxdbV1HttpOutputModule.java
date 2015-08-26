package com.pearson.statsagg.metric_formats.influxdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbV1HttpOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbV1HttpOutputModule.class.getName());
    
    private final boolean isOutputEnabled_;
    private final String url_;
    private final int numSendRetryAttempts_;
    private final int maxMetricsPerMessage_;
    private final String uniqueId_;
    
    public InfluxdbV1HttpOutputModule(boolean isOutputEnabled, String url, int numSendRetryAttempts, int maxMetricsPerMessage, String uniqueId) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.url_ = url;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.uniqueId_ = uniqueId;
    }

    public boolean isOutputEnabled() {
        return isOutputEnabled_;
    }

    public String getUrl() {
        return url_;
    }

    public int getNumSendRetryAttempts() {
        return numSendRetryAttempts_;
    }

    public int getMaxMetricsPerMessage() {
        return maxMetricsPerMessage_;
    }
    
    public String getUniqueId() {
        return uniqueId_;
    }
    
}
