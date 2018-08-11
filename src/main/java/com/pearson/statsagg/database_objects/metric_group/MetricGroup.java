package com.pearson.statsagg.database_objects.metric_group;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_engine.DatabaseObject;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.webui.api.JsonOutputFieldNamingStrategy;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroup extends DatabaseObject<MetricGroup> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroup.class.getName());
    
    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_;
    private transient String uppercaseName_ = null;
    @SerializedName("description") private String description_;
    
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
    
    public static JsonObject getJsonObject_ApiFriendly(MetricGroup metricGroup) {
        return getJsonObject_ApiFriendly(metricGroup, null, null);
    }
    
    public static JsonObject getJsonObject_ApiFriendly(MetricGroup metricGroup, List<MetricGroupRegex> metricGroupRegexes, List<MetricGroupTag> metricGroupTags) {
        
        if (metricGroup == null) {
            return null;
        }
        
        try {
            Gson metricGroup_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
            JsonElement metricGroup_JsonElement = metricGroup_Gson.toJsonTree(metricGroup);
            JsonObject jsonObject = new Gson().toJsonTree(metricGroup_JsonElement).getAsJsonObject();

            JsonArray matchRegexes = new JsonArray();
            JsonArray blacklistRegexes = new JsonArray();
            JsonArray tags = new JsonArray();

            if (metricGroupRegexes != null) {
                for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
                    if ((metricGroupRegex.isBlacklistRegex() != null) && !metricGroupRegex.isBlacklistRegex() && (metricGroupRegex.getPattern() != null)) {
                        matchRegexes.add(metricGroupRegex.getPattern());
                    }
                    else if ((metricGroupRegex.isBlacklistRegex() != null) && metricGroupRegex.isBlacklistRegex() && (metricGroupRegex.getPattern() != null)) {
                        blacklistRegexes.add(metricGroupRegex.getPattern());
                    }
                }
            }
            
            if (metricGroupTags != null) {
                for (MetricGroupTag metricGroupTag : metricGroupTags) {
                    if (metricGroupTag.getTag() != null) tags.add(metricGroupTag.getTag());
                }
            }
            
            jsonObject.add("match_regexes", matchRegexes);
            jsonObject.add("blacklist_regexes", blacklistRegexes);
            jsonObject.add("tags", tags);
            
            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(MetricGroup metricGroup) {
        return getJsonString_ApiFriendly(metricGroup, null, null);
    }
    
    public static String getJsonString_ApiFriendly(MetricGroup metricGroup, List<MetricGroupRegex> metricGroupRegexes, List<MetricGroupTag> metricGroupTags) {
        
        if (metricGroup == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(metricGroup, metricGroupRegexes, metricGroupTags);
            if (jsonObject == null) return null;

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();   
            return gson.toJson(jsonObject);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
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
