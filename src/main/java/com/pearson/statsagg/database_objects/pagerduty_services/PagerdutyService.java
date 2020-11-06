package com.pearson.statsagg.database_objects.pagerduty_services;

import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class PagerdutyService implements DatabaseObject<PagerdutyService> {
    
    private static final Logger logger = LoggerFactory.getLogger(PagerdutyService.class.getName());
   
    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_ = null;
    private transient String uppercaseName_ = null;
    @SerializedName("description") private String description_;
    @SerializedName("routing_key") private String routingKey_ = null;
    
    public PagerdutyService() {
        this.id_ = -1;
    }
    
    public PagerdutyService(Integer id, String name, String description, String routingKey) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, routingKey);
    } 
    
    public PagerdutyService(Integer id, String name, String uppercaseName, String description, String routingKey) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
        this.routingKey_ = routingKey;
    }
    
    @Override
    public boolean isEqual(PagerdutyService pagerdutyService) {
        
        if (pagerdutyService == null) return false;
        if (pagerdutyService == this) return true;
        if (pagerdutyService.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, pagerdutyService.getId())
                .append(name_, pagerdutyService.getName())
                .append(uppercaseName_, pagerdutyService.getUppercaseName())
                .append(description_, pagerdutyService.getDescription())
                .append(routingKey_, pagerdutyService.getRoutingKey())
                .isEquals();
    }
    
    public static PagerdutyService copy(PagerdutyService pagerdutyService) {
        
        if (pagerdutyService == null) {
            return null;
        }
        
        PagerdutyService pagerdutyServiceCopy = new PagerdutyService();
        
        pagerdutyServiceCopy.setId(pagerdutyService.getId());
        pagerdutyServiceCopy.setName(pagerdutyService.getName());
        pagerdutyServiceCopy.setUppercaseName(pagerdutyService.getUppercaseName());
        pagerdutyServiceCopy.setDescription(pagerdutyService.getDescription());
        pagerdutyServiceCopy.setRoutingKey(pagerdutyService.getRoutingKey());
        
        return pagerdutyServiceCopy;
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
    
    public String getRoutingKey() {
        return routingKey_;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey_ = routingKey;
    }
    
}
