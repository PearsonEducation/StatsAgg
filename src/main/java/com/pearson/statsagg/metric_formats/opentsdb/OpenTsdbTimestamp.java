package com.pearson.statsagg.metric_formats.opentsdb;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbTimestamp {

    private final long timestampLong_;
    private final boolean isMilliseconds_;
    
    public OpenTsdbTimestamp(long timestampLong, boolean isMilliseconds) {
        this.timestampLong_ = timestampLong;
        this.isMilliseconds_ = isMilliseconds;
    }
    
    public long getTimestampLong() {
        return timestampLong_;
    }

    public boolean isMilliseconds() {
        return isMilliseconds_;
    }
    
}
