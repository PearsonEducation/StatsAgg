package com.pearson.statsagg.globals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbTelnetOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbTelnetOutputModule.class.getName());
    
    private final boolean isOutputEnabled_;
    private final String host_;
    private final int port_;
    private final int numSendRetryAttempts_;
    private final boolean sanitizeMetrics_;
    private final String uniqueId_;
    
    public OpenTsdbTelnetOutputModule(boolean isOutputEnabled, String host, int port, int numSendRetryAttempts, boolean sanitizeMetrics, String uniqueId) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.host_ = host;
        this.port_ = port;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.sanitizeMetrics_ = sanitizeMetrics;
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
    
    public boolean isSanitizeMetrics() {
        return sanitizeMetrics_;
    }
    
    public String getUniqueId() {
        return uniqueId_;
    }
    
}