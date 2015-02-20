package com.pearson.statsagg.metric_aggregation.opentsdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbTag {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbTag.class.getName());

    private final String key_;
    private final String value_;
    
    public OpenTsdbTag(String key, String value) {
        this.key_ = key;
        this.value_ = value;
    }

    public String getKey() {
        return key_;
    }

    public String getValue() {
        return value_;
    }
    
}
