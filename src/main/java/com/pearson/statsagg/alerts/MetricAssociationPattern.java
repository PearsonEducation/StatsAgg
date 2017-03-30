package com.pearson.statsagg.alerts;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAssociationPattern {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricAssociationPattern.class.getName());

    private final Pattern pattern_;
    private final long timestamp_;
    
    public MetricAssociationPattern(Pattern pattern, long timestamp) {
        this.pattern_ = pattern;
        this.timestamp_ = timestamp;
    }
    
    public Pattern getPattern() {
        return pattern_;
    }

    public long getTimestamp() {
        return timestamp_;
    }
    
}
