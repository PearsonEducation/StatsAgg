package com.pearson.statsagg.metric_formats.influxdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbKV {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbKV.class.getName());

    private final String key_;
    private final Object value_;
    
    public InfluxdbKV(String key, Object value) {
        this.key_ = key;
        this.value_ = value;
    }

    public String getKey() {
        return key_;
    }

    public Object getValue() {
        return value_;
    }
    
}
