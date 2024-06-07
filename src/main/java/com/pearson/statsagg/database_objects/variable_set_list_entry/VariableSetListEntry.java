package com.pearson.statsagg.database_objects.variable_set_list_entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.database_objects.JsonOutputFieldNamingStrategy;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetListEntry implements DatabaseObject<VariableSetListEntry>  {
    
    private static final Logger logger = LoggerFactory.getLogger(VariableSetListEntry.class.getName());
    
    @SerializedName("id") private Integer id_;
    @SerializedName("variable_set_list_id") private Integer variableSetListId_;
    @SerializedName("variable_set_id") private Integer variableSetId_;
    
    public VariableSetListEntry() {
        this.id_ = -1;
    }
    
    public VariableSetListEntry(Integer id, Integer variableSetListId, Integer variableSetId) {
        this.id_ = id;
        this.variableSetListId_ = variableSetListId;
        this.variableSetId_ = variableSetId;
    }    
    
    @Override
    public boolean isEqual(VariableSetListEntry variableSet) {
        
        if (variableSet == null) return false;
        if (variableSet == this) return true;
        if (variableSet.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, variableSet.getId())
                .append(variableSetListId_, variableSet.getVariableSetListId())
                .append(variableSetId_, variableSet.getVariableSetId())
                .isEquals();
    }
    
    public static VariableSetListEntry copy(VariableSetListEntry variableSetListEntry) {
        
        if (variableSetListEntry == null) {
            return null;
        }
        
        VariableSetListEntry variableSetListEntryCopy = new VariableSetListEntry();
        
        variableSetListEntryCopy.setId(variableSetListEntry.getId());
        variableSetListEntryCopy.setVariableSetListId(variableSetListEntry.getVariableSetListId());
        variableSetListEntryCopy.setVariableSetId(variableSetListEntry.getVariableSetId());
        
        return variableSetListEntryCopy;
    }
    
    public static JsonObject getJsonObject_ApiFriendly(VariableSetListEntry variableSet) {
        
        if (variableSet == null) {
            return null;
        }
        
        try {
            Gson notificationGroup_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
            JsonElement notificationGroup_JsonElement = notificationGroup_Gson.toJsonTree(variableSet);
            JsonObject jsonObject = new Gson().toJsonTree(notificationGroup_JsonElement).getAsJsonObject();
            
            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(VariableSetListEntry variableSet) {
        
        if (variableSet == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(variableSet);
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

    public Integer getVariableSetListId() {
        return variableSetListId_;
    }

    public void setVariableSetListId(Integer variableSetListId) {
        this.variableSetListId_ = variableSetListId;
    }

    public Integer getVariableSetId() {
        return variableSetId_;
    }

    public void setVariableSetId(Integer variableSetId) {
        this.variableSetId_ = variableSetId;
    }

}
