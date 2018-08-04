package com.pearson.statsagg.metric_formats.opentsdb;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbHttpOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbHttpOutputModule.class.getName());
    
    private final boolean isOutputEnabled_;
    private final String urlString_;
    private final int numSendRetryAttempts_;
    private final int maxMetricsPerMessage_;
    private final boolean sanitizeMetrics_;
    private final String uniqueId_;
    
    private URL url_;

    public OpenTsdbHttpOutputModule(boolean isOutputEnabled, String url, int numSendRetryAttempts, int maxMetricsPerMessage, boolean sanitizeMetrics, String uniqueId) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.urlString_ = url;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.sanitizeMetrics_ = sanitizeMetrics;
        this.uniqueId_ = uniqueId;
        
        try {
            url_ = new URL(url);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            url_ = null;
        }
    }

    public boolean isOutputEnabled() {
        return isOutputEnabled_;
    }
    
    public URL getUrl() {
        return url_;
    }
 
    public String getUrlString() {
        return urlString_;
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
