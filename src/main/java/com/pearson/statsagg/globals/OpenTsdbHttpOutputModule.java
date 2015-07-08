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
    private final int numSendRetryAttempts_;
    private final int maxMetricsPerMessage_;
    private final boolean sanitizeMetrics_;
    private final String uniqueId_;
    
    public OpenTsdbHttpOutputModule(boolean isOutputEnabled, String url, int numSendRetryAttempts, int maxMetricsPerMessage, boolean sanitizeMetrics, String uniqueId) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.url_ = url;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.sanitizeMetrics_ = sanitizeMetrics;
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

    public boolean isSanitizeMetrics() {
        return sanitizeMetrics_;
    }
    
    public String getUniqueId() {
        return uniqueId_;
    }
    
}
