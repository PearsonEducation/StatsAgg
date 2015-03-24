package com.pearson.statsagg.globals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbHttpOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbHttpOutputModule.class.getName());
    
    private final boolean isOutputEnabled_;
    private final String url_;
    private final int port_;
    private final int numSendRetryAttempts_;
    private final int maxMetricsPerMessage_;
    
    public OpenTsdbHttpOutputModule(boolean isOutputEnabled, String url, int port, int numSendRetryAttempts, int maxMetricsPerMessage) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.url_ = url;
        this.port_ = port;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
    }

    public boolean isOutputEnabled() {
        return isOutputEnabled_;
    }

    public String getUrl() {
        return url_;
    }

    public int getPort() {
        return port_;
    }

    public int getNumSendRetryAttempts() {
        return numSendRetryAttempts_;
    }

    public int getMaxMetricsPerMessage() {
        return maxMetricsPerMessage_;
    }
    
}
