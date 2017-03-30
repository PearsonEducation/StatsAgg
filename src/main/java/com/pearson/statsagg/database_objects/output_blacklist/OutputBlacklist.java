package com.pearson.statsagg.database_objects.output_blacklist;

import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_engine.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OutputBlacklist extends DatabaseObject<OutputBlacklist> {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputBlacklist.class.getName());
   
    @SerializedName("id") private Integer id_;
    @SerializedName("metric_group_id") private Integer metricGroupId_;
    
    public OutputBlacklist(Integer id, Integer metricGroupId) {
        this.id_ = id;
        this.metricGroupId_ = metricGroupId;
    } 
    
    @Override
    public boolean isEqual(OutputBlacklist outputBlacklist) {
        
        if (outputBlacklist == null) return false;
        if (outputBlacklist == this) return true;
        if (outputBlacklist.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, outputBlacklist.getId())
                .append(metricGroupId_, outputBlacklist.getMetricGroupId())
                .isEquals();
    }

    public Integer getId() {
        return id_;
    }

    public void setId(Integer id) {
        this.id_ = id;
    }

    public Integer getMetricGroupId() {
        return metricGroupId_;
    }

    public void setMetricGroupId(Integer metricGroupId) {
        this.metricGroupId_ = metricGroupId;
    }
    
}
