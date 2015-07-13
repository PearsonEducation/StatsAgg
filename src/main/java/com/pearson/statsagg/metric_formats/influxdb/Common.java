package com.pearson.statsagg.metric_formats.influxdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Common {
    
    private static final Logger logger = LoggerFactory.getLogger(Common.class.getName());
    
    public static final byte TIMESTAMP_PRECISION_UNKNOWN = -1;
    public static final byte TIMESTAMP_PRECISION_HOURS = 1;
    public static final byte TIMESTAMP_PRECISION_MINUTES = 2;
    public static final byte TIMESTAMP_PRECISION_SECONDS = 3;
    public static final byte TIMESTAMP_PRECISION_MILLISECONDS = 4;
    public static final byte TIMESTAMP_PRECISION_MICROSECONDS = 5;
    public static final byte TIMESTAMP_PRECISION_NANOSECONDS = 6;
    
}
