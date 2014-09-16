package com.pearson.statsagg.database.gauges;

import java.math.BigDecimal;
import java.sql.Timestamp;
import com.pearson.statsagg.database.DatabaseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Gauge extends DatabaseObject<Gauge> {
    
    private static final Logger logger = LoggerFactory.getLogger(Gauge.class.getName());
   
    private final String bucket_;
    private final BigDecimal metricValue_;
    private final Timestamp lastModified_;
    
    public Gauge(String bucket, BigDecimal metricValue, Timestamp lastModified) {
        this.bucket_ = bucket;
        this.metricValue_ = metricValue;
        
        if (lastModified == null) this.lastModified_ = null;
        else this.lastModified_ = (Timestamp) lastModified.clone();
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append("bucket=").append(bucket_).append(", metricValue=").append(metricValue_).append(", lastModified=").append(lastModified_.getTime());

        return stringBuilder.toString();
    }
    
    @Override
    public boolean isEqual(Gauge gauge) {
        
        boolean isEquals = true;
        
        if (isEquals && (bucket_ != null)) {
            isEquals = bucket_.equals(gauge.getBucket());
        }
        else if (isEquals && (bucket_ == null)) {
            isEquals = gauge.getBucket() == null;
        }

        if (isEquals && (metricValue_ != null)) {
            isEquals = (metricValue_.compareTo(gauge.getMetricValue()) == 0);
        }
        else if (isEquals && (metricValue_ == null)) {
            isEquals = gauge.getMetricValue() == null;
        }
        
        if (isEquals && (lastModified_ != null)) {
            isEquals = (lastModified_.getTime() == gauge.getLastModified().getTime());
        }
        else {
            isEquals = gauge.getLastModified() == null;
        }
        
        return isEquals;
    }
    
    public String getBucket() {
        return bucket_;
    }

    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    public Timestamp getLastModified() {
        if (lastModified_ == null) return null;
        else return (Timestamp) lastModified_.clone();
    }
    
}
