package com.pearson.statsagg.database_objects.gauges;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.sql.Timestamp;
import com.pearson.statsagg.database_engine.DatabaseObject;
import com.pearson.statsagg.utilities.MathUtilities;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Gauge extends DatabaseObject<Gauge> {
    
    private static final Logger logger = LoggerFactory.getLogger(Gauge.class.getName());
   
    @SerializedName("bucket_sha1") private final String bucketSha1_;
    @SerializedName("bucket") private final String bucket_;
    @SerializedName("metric_value") private final BigDecimal metricValue_;
    @SerializedName("last_modified") private final Timestamp lastModified_;
    
    public Gauge(String bucketSha1, String bucket, BigDecimal metricValue, Timestamp lastModified) {
        this.bucketSha1_ = bucketSha1;
        this.bucket_ = bucket;
        this.metricValue_ = metricValue;
        
        if (lastModified == null) this.lastModified_ = null;
        else this.lastModified_ = (Timestamp) lastModified.clone();
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append("bucket_sha1=").append(bucketSha1_).append(", bucket=").append(bucket_).append(", metricValue=")
                .append(metricValue_).append(", lastModified=").append(lastModified_.getTime());

        return stringBuilder.toString();
    }
    
    @Override
    public boolean isEqual(Gauge gauge) {
        
        if (gauge == null) return false;
        if (gauge == this) return true;
        if (gauge.getClass() != getClass()) return false;
        
        boolean areMetricValuesNumericallyEqual = MathUtilities.areBigDecimalsNumericallyEqual(metricValue_, gauge.getMetricValue());

        return new EqualsBuilder()
                .append(bucketSha1_, gauge.getBucketSha1())
                .append(bucket_, gauge.getBucket())
                .append(areMetricValuesNumericallyEqual, true)
                .append(lastModified_, gauge.getLastModified())
                .isEquals();
    }
    
    public boolean isValid() {
        if (bucketSha1_ == null) return false;
        if (bucket_ == null) return false;
        if (metricValue_ == null) return false;
        if (lastModified_ == null) return false;

        return true;
    }

    public String getBucketSha1() {
        return bucketSha1_;
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
