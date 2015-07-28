package com.pearson.statsagg.database_objects.metric_group;

import com.pearson.statsagg.database_engine.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroup extends DatabaseObject<MetricGroup> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroup.class.getName());
    
    private Integer id_;
    private String name_;
    private String uppercaseName_ = null;
    private String description_;
    
    public MetricGroup() {
        this.id_ = -1;
    }
    
    public MetricGroup(Integer id, String name, String description) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), description);
    } 
    
    public MetricGroup(Integer id, String name, String uppercaseName, String description) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
    } 
    
    @Override
    public boolean isEqual(MetricGroup metricGroup) {
        
        if (metricGroup == null) return false;
        if (metricGroup == this) return true;
        if (metricGroup.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, metricGroup.getId())
                .append(name_, metricGroup.getName())
                .append(uppercaseName_, metricGroup.getUppercaseName())
                .append(description_, metricGroup.getDescription())
                .isEquals();
    }
    
    public static MetricGroup copy(MetricGroup metricGroup) {
        
        if (metricGroup == null) {
            return null;
        }
        
        MetricGroup metricGroupCopy = new MetricGroup();
        
        metricGroupCopy.setId(metricGroup.getId());
        metricGroupCopy.setName(metricGroup.getName());
        metricGroupCopy.setUppercaseName(metricGroup.getUppercaseName());
        metricGroupCopy.setDescription(metricGroup.getDescription());
        
        return metricGroupCopy;
    }
    
    public Integer getId() {
        return id_;
    }

    public void setId(Integer id) {
        this.id_ = id;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        this.name_ = name;
    }
    
    public String getUppercaseName() {
        return uppercaseName_;
    }

    public void setUppercaseName(String uppercaseName) {
        this.uppercaseName_ = uppercaseName;
    }
    
    public String getDescription() {
        return description_;
    }

    public void setDescription(String description) {
        this.description_ = description;
    }
    
}
