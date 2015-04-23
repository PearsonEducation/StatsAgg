package com.pearson.statsagg.database.metric_last_seen;

import java.sql.Timestamp;
import com.pearson.statsagg.database.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricLastSeen extends DatabaseObject<MetricLastSeen> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricLastSeen.class.getName());
   
    private final String metricKeySha1_;
    private final String metricKey_;
    private final Timestamp lastModified_;
    
    public MetricLastSeen(String metricKeySha1, String metricKey, Timestamp lastModified) {
        this.metricKeySha1_ = metricKeySha1;
        this.metricKey_ = metricKey;
        
        if (lastModified == null) this.lastModified_ = null;
        else this.lastModified_ = (Timestamp) lastModified.clone();
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append("metricKey_sha1=").append(metricKeySha1_).append(", metricKey=").append(metricKey_).append(", lastModified=").append(lastModified_.getTime());

        return stringBuilder.toString();
    }
    
    @Override
    public boolean isEqual(MetricLastSeen metricLastSeen) {
        
        if (metricLastSeen == null) return false;
        if (metricLastSeen == this) return true;
        if (metricLastSeen.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(metricKeySha1_, metricLastSeen.getMetricKeySha1())
                .append(metricKey_, metricLastSeen.getMetricKey())
                .append(lastModified_, metricLastSeen.getLastModified())
                .isEquals();
    }
    
    public String getMetricKeySha1() {
        return metricKeySha1_;
    }
    
    public String getMetricKey() {
        return metricKey_;
    }

    public Timestamp getLastModified() {
        if (lastModified_ == null) return null;
        else return (Timestamp) lastModified_.clone();
    }
    
}
