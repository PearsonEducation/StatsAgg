package com.pearson.statsagg.database_objects.metric_group_templates;

import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTemplate implements DatabaseObject<MetricGroupTemplate>  {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplate.class.getName());
    
    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_;
    private transient String uppercaseName_ = null;
    
    @SerializedName("variable_set_list_id") private Integer variableSetListId_;
    
    @SerializedName("metric_group_name_variable") private String metricGroupNameVariable_ = null;
    @SerializedName("description_variable") private String descriptionVariable_;
    @SerializedName("match_regexes_variable") private String matchRegexesVariable_ = null;
    @SerializedName("blacklist_regexes_variable") private String blacklistRegexesVariable_ = null;
    @SerializedName("tags_variable") private String tagsVariable_ = null;
    private transient Boolean isMarkedForDelete_ = null;

    public MetricGroupTemplate() {
        this.id_ = -1;
    }
    
    public MetricGroupTemplate(Integer id, String name, Integer variableSetListId, 
            String metricGroupNameVariable, String descriptionVariable, String matchRegexesVariable, String blacklistRegexesVariable, String tagsVariable, 
            Boolean isMarkedForDelete) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), variableSetListId,  
                metricGroupNameVariable, descriptionVariable, matchRegexesVariable, blacklistRegexesVariable, tagsVariable, isMarkedForDelete);
    } 
    
    public MetricGroupTemplate(Integer id, String name, String uppercaseName, Integer variableSetListId,
            String metricGroupNameVariable, String descriptionVariable, String matchRegexesVariable, String blacklistRegexesVariable, String tagsVariable, 
            Boolean isMarkedForDelete) {
        
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.variableSetListId_ = variableSetListId;
        this.metricGroupNameVariable_ = metricGroupNameVariable;
        this.descriptionVariable_ = descriptionVariable;
        this.matchRegexesVariable_ = matchRegexesVariable;
        this.blacklistRegexesVariable_ = blacklistRegexesVariable;
        this.tagsVariable_ = tagsVariable;
        this.isMarkedForDelete_ = isMarkedForDelete;
    }    
    
    @Override
    public boolean isEqual(MetricGroupTemplate metricGroupTemplate) {
        
        if (metricGroupTemplate == null) return false;
        if (metricGroupTemplate == this) return true;
        if (metricGroupTemplate.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, metricGroupTemplate.getId())
                .append(name_, metricGroupTemplate.getName())
                .append(uppercaseName_, metricGroupTemplate.getUppercaseName())
                .append(variableSetListId_, metricGroupTemplate.getVariableSetListId())
                .append(metricGroupNameVariable_, metricGroupTemplate.getMetricGroupNameVariable())
                .append(descriptionVariable_, metricGroupTemplate.getDescriptionVariable())
                .append(matchRegexesVariable_, metricGroupTemplate.getMatchRegexesVariable())
                .append(blacklistRegexesVariable_, metricGroupTemplate.getBlacklistRegexesVariable())
                .append(tagsVariable_, metricGroupTemplate.getTagsVariable())
                .append(isMarkedForDelete_, metricGroupTemplate.isMarkedForDelete())
                .isEquals();
    }
    
    public static MetricGroupTemplate copy(MetricGroupTemplate metricGroupTemplate) {
        
        if (metricGroupTemplate == null) {
            return null;
        }
        
        MetricGroupTemplate metricGroupTemplateCopy = new MetricGroupTemplate();
        
        metricGroupTemplateCopy.setId(metricGroupTemplate.getId());
        metricGroupTemplateCopy.setName(metricGroupTemplate.getName());
        metricGroupTemplateCopy.setVariableSetListId(metricGroupTemplate.getVariableSetListId());
        metricGroupTemplateCopy.setMetricGroupNameVariable(metricGroupTemplate.getMetricGroupNameVariable());
        metricGroupTemplateCopy.setDescriptionVariable(metricGroupTemplate.getDescriptionVariable());
        metricGroupTemplateCopy.setMatchRegexesVariable(metricGroupTemplate.getMatchRegexesVariable());
        metricGroupTemplateCopy.setBlacklistRegexsVariable(metricGroupTemplate.getBlacklistRegexesVariable());
        metricGroupTemplateCopy.setTagsVariable(metricGroupTemplate.getTagsVariable());
        metricGroupTemplateCopy.setIsMarkedForDelete(metricGroupTemplate.isMarkedForDelete());

        return metricGroupTemplateCopy;
    }
    
    public static DatabaseObjectValidation isValid(MetricGroupTemplate metricGroupTemplate) {
        if (metricGroupTemplate == null) return new DatabaseObjectValidation(false, "Invalid metric group template");
        
        DatabaseObjectValidation databaseObjectValidation_CoreCriteria = metricGroupTemplate.isValidCoreCriteria();
        if (!databaseObjectValidation_CoreCriteria.isValid()) return databaseObjectValidation_CoreCriteria;

        return new DatabaseObjectValidation(true);
    }
    
    public DatabaseObjectValidation isValidCoreCriteria() {
        if ((name_ == null) || name_.isEmpty()) return new DatabaseObjectValidation(false, "Invalid name");
        if (variableSetListId_ == null) return new DatabaseObjectValidation(false, "Invalid variable set list");
        if ((metricGroupNameVariable_ == null) || metricGroupNameVariable_.isEmpty()) return new DatabaseObjectValidation(false, "Invalid metric group name variable");
        if (isMarkedForDelete_ == null) return new DatabaseObjectValidation(false, "Invalid 'marked for delete' setting");

        return new DatabaseObjectValidation(true);
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
    
    public Integer getVariableSetListId() {
        return variableSetListId_;
    }

    public void setVariableSetListId(Integer variableSetListId) {
        this.variableSetListId_ = variableSetListId;
    }

    public String getMetricGroupNameVariable() {
        return metricGroupNameVariable_;
    }

    public void setMetricGroupNameVariable(String metricGroupNameVariable) {
        this.metricGroupNameVariable_ = metricGroupNameVariable;
    }
    
    public String getDescriptionVariable() {
        return descriptionVariable_;
    }

    public void setDescriptionVariable(String descriptionVariable) {
        this.descriptionVariable_ = descriptionVariable;
    }

    public String getMatchRegexesVariable() {
        return matchRegexesVariable_;
    }

    public void setMatchRegexesVariable(String matchRegexesVariable) {
        this.matchRegexesVariable_ = matchRegexesVariable;
    }

    public String getBlacklistRegexesVariable() {
        return blacklistRegexesVariable_;
    }

    public void setBlacklistRegexsVariable(String blacklistRegexesVariable) {
        this.blacklistRegexesVariable_ = blacklistRegexesVariable;
    }

    public String getTagsVariable() {
        return tagsVariable_;
    }

    public void setTagsVariable(String tagsVariable) {
        this.tagsVariable_ = tagsVariable;
    }

    public Boolean isMarkedForDelete() {
        return isMarkedForDelete_;
    }

    public void setIsMarkedForDelete(Boolean isMarkedForDelete) {
        this.isMarkedForDelete_ = isMarkedForDelete;
    }
    
}
