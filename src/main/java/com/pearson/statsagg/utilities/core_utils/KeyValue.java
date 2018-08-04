package com.pearson.statsagg.utilities.core_utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValue<S, T> {
    
    private static final Logger logger = LoggerFactory.getLogger(KeyValue.class.getName());
    
    private S key_ = null;
    public T value_ = null;
    
    public KeyValue(S key, T value) {
        this.key_ = key;
        this.value_ = value;
    }

    public S getKey() {
        return key_;
    }

    public void setKey(S key) {
        this.key_ = key;
    }

    public T getValue() {
        return value_;
    }

    public void setValue(T value) {
        this.value_ = value;
    }
    
}
