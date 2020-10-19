package com.pearson.statsagg.configuration;

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
    private final boolean sanitizeMetrics_;
    private final boolean substituteCharacters_;
    private final String uniqueId_;
    
    public GraphiteOutputModule(boolean isOutputEnabled, String host, int port, int numSendRetryAttempts, 
            int maxMetricsPerMessage, boolean sanitizeMetrics, boolean substituteCharacters, String uniqueId) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.host_ = host;
        this.port_ = port;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.sanitizeMetrics_ = sanitizeMetrics;
        this.substituteCharacters_ = substituteCharacters;
        this.uniqueId_ = uniqueId;
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
    
    public boolean isSanitizeMetrics() {
        return sanitizeMetrics_;
    }

    public boolean isSubstituteCharacters() {
        return substituteCharacters_;
    }
    
    public String getUniqueId() {
        return uniqueId_;
    }
    
}