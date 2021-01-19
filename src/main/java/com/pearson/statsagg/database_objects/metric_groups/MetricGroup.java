package com.pearson.statsagg.database_objects.metric_groups;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.database_objects.JsonOutputFieldNamingStrategy;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.collection_utils.CollectionUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroup implements DatabaseObject<MetricGroup>  {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroup.class.getName());
    
    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_;
    private transient String uppercaseName_ = null;
    @SerializedName("description") private String description_;
    @SerializedName("metric_group_template_id") private Integer metricGroupTemplateId_ = null;
    @SerializedName("variable_set_id") private Integer variableSetId_ = null;
    @SerializedName("match_regexes") private TreeSet<String> matchRegexes_ = null;
    @SerializedName("blacklist_regexes") private TreeSet<String> blacklistRegexes_ = null;
    @SerializedName("tags") private TreeSet<String> tags_ = null;

    public MetricGroup() {
        this.id_ = -1;
    }

    public MetricGroup(Integer id, String name, String description, Integer metricGroupTemplateId, Integer variableSetId, 
            TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, metricGroupTemplateId, variableSetId, matchRegexes, blacklistRegexes, tags);
    } 
    
    public MetricGroup(Integer id, String name, String uppercaseName, String description, Integer metricGroupTemplateId, Integer variableSetId, 
            TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
        this.metricGroupTemplateId_ = metricGroupTemplateId;
        this.variableSetId_ = variableSetId;
        
        if (matchRegexes != null) matchRegexes_ = new TreeSet<>(matchRegexes);
        if (blacklistRegexes != null) blacklistRegexes_ = new TreeSet<>(blacklistRegexes);
        if (tags != null) tags_ = new TreeSet<>(tags);
    }    
    
    @Override
    public boolean isEqual(MetricGroup metricGroup) {
        
        if (metricGroup == null) return false;
        if (metricGroup == this) return true;
        if (metricGroup.getClass() != getClass()) return false;
        
        boolean areMatchRegexesEqual = CollectionUtilities.areSetContentsEqual(matchRegexes_, metricGroup.getMatchRegexes());
        boolean areBlacklistRegexesEqual = CollectionUtilities.areSetContentsEqual(blacklistRegexes_, metricGroup.getBlacklistRegexes());
        boolean areTagsEqual = CollectionUtilities.areSetContentsEqual(tags_, metricGroup.getTags());
            
        return new EqualsBuilder()
                .append(id_, metricGroup.getId())
                .append(name_, metricGroup.getName())
                .append(uppercaseName_, metricGroup.getUppercaseName())
                .append(description_, metricGroup.getDescription())
                .append(metricGroupTemplateId_, metricGroup.getMetricGroupTemplateId())
                .append(variableSetId_, metricGroup.getVariableSetId())
                .append(areMatchRegexesEqual, true)
                .append(areBlacklistRegexesEqual, true)
                .append(areTagsEqual, true)
                .isEquals();
    }
    
    public static MetricGroup copy(MetricGroup metricGroup) {
        
        if (metricGroup == null) {
            return null;
        }
        
        MetricGroup metricGroupCopy = new MetricGroup();
        
        metricGroupCopy.setId(metricGroup.getId());
        metricGroupCopy.setName(metricGroup.getName());
        metricGroupCopy.setDescription(metricGroup.getDescription());
        metricGroupCopy.setMetricGroupTemplateId(metricGroup.getMetricGroupTemplateId());
        metricGroupCopy.setVariableSetId(metricGroup.getVariableSetId());

        TreeSet matchRegexes = (metricGroup.getMatchRegexes() == null) ? null : new TreeSet<>(metricGroup.getMatchRegexes());
        metricGroupCopy.setMatchRegexes(matchRegexes);
        
        TreeSet blacklistRegexes = (metricGroup.getBlacklistRegexes() == null) ? null : new TreeSet<>(metricGroup.getBlacklistRegexes());
        metricGroupCopy.setBlacklistRegexes(blacklistRegexes);
        
        TreeSet tags = (metricGroup.getTags() == null) ? null : new TreeSet<>(metricGroup.getTags());
        metricGroupCopy.setTags(tags);
        
        return metricGroupCopy;
    }
    
    public static DatabaseObjectValidation isValid(MetricGroup metricGroup) {
        if (metricGroup == null) return new DatabaseObjectValidation(false, "Invalid metric group");
        if ((metricGroup.getName() == null) || metricGroup.getName().isEmpty()) return new DatabaseObjectValidation(false, "Invalid name");
        if (metricGroup.getDescription() == null) return new DatabaseObjectValidation(false, "Invalid description");
        if ((metricGroup.getMatchRegexes() == null) || metricGroup.getMatchRegexes().isEmpty()) return new DatabaseObjectValidation(false, "Invalid match regexes (requires at least 1 regex)");
        
        return new DatabaseObjectValidation(true);
    }
    
    public static JsonObject getJsonObject_ApiFriendly(MetricGroup metricGroup, boolean includeAssociatedMetrics, long includeAssociatedMetrics_Limit) {
        
        if (metricGroup == null) {
            return null;
        }
        
        try {
            Gson metricGroup_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
            JsonElement metricGroup_JsonElement = metricGroup_Gson.toJsonTree(metricGroup);
            JsonObject jsonObject = new Gson().toJsonTree(metricGroup_JsonElement).getAsJsonObject();

//            JsonArray matchRegexes = new JsonArray();
//            JsonArray blacklistRegexes = new JsonArray();
//            JsonArray tags = new JsonArray();
            JsonArray associatedMetrics = null;
            
//            if (metricGroupRegexes != null) {
//                for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
//                    if ((metricGroupRegex.isBlacklistRegex() != null) && !metricGroupRegex.isBlacklistRegex() && (metricGroupRegex.getPattern() != null)) {
//                        matchRegexes.add(metricGroupRegex.getPattern());
//                    }
//                    else if ((metricGroupRegex.isBlacklistRegex() != null) && metricGroupRegex.isBlacklistRegex() && (metricGroupRegex.getPattern() != null)) {
//                        blacklistRegexes.add(metricGroupRegex.getPattern());
//                    }
//                }
//            }
            
//            if (metricGroupTags != null) {
//                for (MetricGroupTag metricGroupTag : metricGroupTags) {
//                    if (metricGroupTag.getTag() != null) tags.add(metricGroupTag.getTag());
//                }
//            }
            
            if (includeAssociatedMetrics && (metricGroup.getId() != null) && (includeAssociatedMetrics_Limit > 0)) {
                associatedMetrics = new JsonArray();
                Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroup.getId());
                
                List<String> matchingMetricKeysAssociatedWithMetricGroupSorted = new ArrayList<>();
                synchronized(matchingMetricKeysAssociatedWithMetricGroup) {
                    int i = 1;
                    for (String matchingMetricKeyAssociatedWithMetricGroup : matchingMetricKeysAssociatedWithMetricGroup) {
                        if ((matchingMetricKeyAssociatedWithMetricGroup == null) || matchingMetricKeyAssociatedWithMetricGroup.isEmpty()) continue;
                        matchingMetricKeysAssociatedWithMetricGroupSorted.add(matchingMetricKeyAssociatedWithMetricGroup);
                        i++;
                        if (i > includeAssociatedMetrics_Limit) break;
                    }
                }
                
                Collections.sort(matchingMetricKeysAssociatedWithMetricGroupSorted);
                
                for (String matchingMetricKeys : matchingMetricKeysAssociatedWithMetricGroupSorted) {
                    associatedMetrics.add(matchingMetricKeys);
                }
            }
            
//            jsonObject.add("match_regexes", matchRegexes);
//            jsonObject.add("blacklist_regexes", blacklistRegexes);
//            jsonObject.add("tags", tags);
            if (includeAssociatedMetrics && (associatedMetrics != null)) jsonObject.add("associated_metrics", associatedMetrics);
                
            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(MetricGroup metricGroup, boolean includeAssociatedMetrics, long includeAssociatedMetrics_Limit) {
        
        if (metricGroup == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(metricGroup, includeAssociatedMetrics, includeAssociatedMetrics_Limit);
            if (jsonObject == null) return null;

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();   
            return gson.toJson(jsonObject);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }

   public static MetricGroup createMetricGroupFromMetricGroupTemplate(MetricGroupTemplate metricGroupTemplate, 
            Integer variableSetId, Integer metricGroupId, String metricGroupName, 
            String description, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {

        if (metricGroupTemplate == null) {
            return null;
        }
        
        MetricGroup metricGroup = new MetricGroup();

        if (metricGroupId == null) metricGroup.setId(-1);
        else metricGroup.setId(metricGroupId);
        
        metricGroup.setMetricGroupTemplateId(metricGroupTemplate.getId());
        metricGroup.setVariableSetId(variableSetId);

        metricGroup.setName(metricGroupName);
        metricGroup.setDescription(description);
        metricGroup.setMatchRegexes(matchRegexes);
        metricGroup.setBlacklistRegexes(blacklistRegexes);
        metricGroup.setTags(tags);

        return metricGroup;
    }
   
    public static boolean areMetricGroupTemplateIdsInConflict(MetricGroup metricGroup1, MetricGroup metricGroup2) {
        if (metricGroup2 == null) return false;
        if (metricGroup1 == null) return false;
        
        if ((metricGroup1.getMetricGroupTemplateId() == null) && (metricGroup2.getMetricGroupTemplateId() == null)) return false;
        if ((metricGroup1.getMetricGroupTemplateId() == null) && (metricGroup2.getMetricGroupTemplateId() != null)) return true;
        if ((metricGroup1.getMetricGroupTemplateId() != null) && (metricGroup2.getMetricGroupTemplateId() == null)) return true;

        return !metricGroup1.getMetricGroupTemplateId().equals(metricGroup2.getMetricGroupTemplateId());
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
        if (name != null) this.uppercaseName_ = name.toUpperCase();
    }
    
    public String getUppercaseName() {
        return uppercaseName_;
    }

    public String getDescription() {
        return description_;
    }

    public void setDescription(String description) {
        this.description_ = description;
    }

    public Integer getMetricGroupTemplateId() {
        return metricGroupTemplateId_;
    }

    public void setMetricGroupTemplateId(Integer metricGroupTemplateId) {
        this.metricGroupTemplateId_ = metricGroupTemplateId;
    }

    public Integer getVariableSetId() {
        return variableSetId_;
    }

    public void setVariableSetId(Integer variableSetId) {
        this.variableSetId_ = variableSetId;
    }
    
    public TreeSet<String> getMatchRegexes() {
        return matchRegexes_;
    }

    public void setMatchRegexes(TreeSet<String> matchRegexes) {
        this.matchRegexes_ = matchRegexes;
    }

    public TreeSet<String> getBlacklistRegexes() {
        return blacklistRegexes_;
    }

    public void setBlacklistRegexes(TreeSet<String> blacklistRegexes) {
        this.blacklistRegexes_ = blacklistRegexes;
    }

    public TreeSet<String> getTags() {
        return tags_;
    }

    public void setTags(TreeSet<String> tags) {
        this.tags_ = tags;
    }
    
}
