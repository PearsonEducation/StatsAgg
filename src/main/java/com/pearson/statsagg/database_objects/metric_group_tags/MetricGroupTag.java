package com.pearson.statsagg.database_objects.metric_group_tags;

import com.pearson.statsagg.database_engine.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTag extends DatabaseObject<MetricGroupTag> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTag.class.getName());
    
    private Integer id_;
    private Integer mgId_;
    private String tag_;
    
    public MetricGroupTag() {
        this.id_ = -1;
    }
    
    public MetricGroupTag(Integer id, Integer mgId, String tag) {
        this.id_ = id;
        this.mgId_ = mgId;
        this.tag_ = tag;
    } 
    
    @Override
    public boolean isEqual(MetricGroupTag metricGroupTag) {
        
        if (metricGroupTag == null) return false;
        if (metricGroupTag == this) return true;
        if (metricGroupTag.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, metricGroupTag.getId())
                .append(mgId_, metricGroupTag.getMgId())
                .append(tag_, metricGroupTag.getTag())
                .isEquals();
    }
    
    public static MetricGroupTag copy(MetricGroupTag metricGroupTag) {
        
        if (metricGroupTag == null) {
            return null;
        }
        
        MetricGroupTag metricGroupTagCopy = new MetricGroupTag();
        
        metricGroupTagCopy.setId(metricGroupTag.getId());
        metricGroupTagCopy.setMgId(metricGroupTag.getMgId());
        metricGroupTagCopy.setTag(metricGroupTag.getTag());
        
        return metricGroupTagCopy;
    }
    
    public Integer getId() {
        return id_;
    }

    public void setId(Integer id) {
        this.id_ = id;
    }

    public Integer getMgId() {
        return mgId_;
    }

    public void setMgId(Integer mgId) {
        this.mgId_ = mgId;
    }

    public String getTag() {
        return tag_;
    }

    public void setTag(String tag) {
        this.tag_ = tag;
    }
    
}
