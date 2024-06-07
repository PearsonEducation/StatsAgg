package com.pearson.statsagg.database_objects.metric_last_seen;

import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricLastSeen implements DatabaseObject<MetricLastSeen> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricLastSeen.class.getName());
   
    @SerializedName("metric_key_sha1") private String metricKeySha1_;
    @SerializedName("metric_key") private String metricKey_;
    @SerializedName("last_modified") private Timestamp lastModified_;
    
    public MetricLastSeen() {}
     
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
