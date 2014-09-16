package com.pearson.statsagg.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValue {
    
    private static final Logger logger = LoggerFactory.getLogger(KeyValue.class.getName());
    
    private String key_ = null;
    public String value_ = null;
    
    public KeyValue(String key, String value) {
        this.key_ = key;
        this.value_ = value;
    }

    public String getKey() {
        return key_;
    }

    public void setKey(String key) {
        this.key_ = key;
    }

    public String getValue() {
        return value_;
    }

    public void setValue(String value) {
        this.value_ = value;
    }
    
}
