package com.pearson.statsagg.database_objects.metric_groups;

import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import java.util.Collection;
import java.util.TreeSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTag implements DatabaseObject<MetricGroupTag> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTag.class.getName());
    
    @SerializedName("id") private transient Integer id_;
    @SerializedName("metric_group_id") private transient Integer metricGroupId_;
    @SerializedName("tag") private String tag_;
    
    public MetricGroupTag() {
        this.id_ = -1;
    }
    
    public MetricGroupTag(Integer id, Integer metricGroupId, String tag) {
        this.id_ = id;
        this.metricGroupId_ = metricGroupId;
        this.tag_ = tag;
    } 
    
    @Override
    public boolean isEqual(MetricGroupTag metricGroupTag) {
        
        if (metricGroupTag == null) return false;
        if (metricGroupTag == this) return true;
        if (metricGroupTag.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, metricGroupTag.getId())
                .append(metricGroupId_, metricGroupTag.getMetricGroupId())
                .append(tag_, metricGroupTag.getTag())
                .isEquals();
    }
    
    public static MetricGroupTag copy(MetricGroupTag metricGroupTag) {
        
        if (metricGroupTag == null) {
            return null;
        }
        
        MetricGroupTag metricGroupTagCopy = new MetricGroupTag();
        
        metricGroupTagCopy.setId(metricGroupTag.getId());
        metricGroupTagCopy.setMetricGroupId(metricGroupTag.getMetricGroupId());
        metricGroupTagCopy.setTag(metricGroupTag.getTag());
        
        return metricGroupTagCopy;
    }
    
    public static TreeSet<String> getMetricGroupTagStringsSortedSet(Collection<MetricGroupTag> metricGroupTags) {
        if (metricGroupTags == null) return null;
        
        TreeSet<String> metricGroupTagsSet = new TreeSet<>();
            
        for (MetricGroupTag metricGroupTag : metricGroupTags) {
            if ((metricGroupTag.getTag() != null) && !metricGroupTag.getTag().isEmpty()) {
                metricGroupTagsSet.add(metricGroupTag.getTag());
            }
        }

        return metricGroupTagsSet;
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

    public void setMetricGroupId(Integer mgId) {
        this.metricGroupId_ = mgId;
    }

    public String getTag() {
        return tag_;
    }

    public void setTag(String tag) {
        this.tag_ = tag;
    }
    
}
