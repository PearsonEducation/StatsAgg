package com.pearson.statsagg.globals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteOutputModule.class.getName());
    
    private final boolean isOutputEnabled_;
    private final String host_;
    private final int port_;
    private final int numSendRetryAttempts_;
    private final int maxMetricsPerMessage_;

    public GraphiteOutputModule(boolean isOutputEnabled, String host, int port, int numSendRetryAttempts, int maxMetricsPerMessage) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.host_ = host;
        this.port_ = port;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
    }
    
    public boolean isOutputEnabled() {
        return isOutputEnabled_;
    }

    public String getHost() {
        return host_;
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