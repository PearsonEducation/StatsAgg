package com.pearson.statsagg.database_objects.variable_set_list;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.database_objects.JsonOutputFieldNamingStrategy;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetList implements DatabaseObject<VariableSetList>  {
    
    private static final Logger logger = LoggerFactory.getLogger(VariableSetList.class.getName());
    
    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_;
    private transient String uppercaseName_ = null;
    @SerializedName("description") private String description_;
    
    public VariableSetList() {
        this.id_ = -1;
    }
    
    public VariableSetList(Integer id, String name, String description) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), description);
    } 
    
    public VariableSetList(Integer id, String name, String uppercaseName, String description) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
    }    
    
    @Override
    public boolean isEqual(VariableSetList variableSet) {
        
        if (variableSet == null) return false;
        if (variableSet == this) return true;
        if (variableSet.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, variableSet.getId())
                .append(name_, variableSet.getName())
                .append(uppercaseName_, variableSet.getUppercaseName())
                .append(description_, variableSet.getDescription())
                .isEquals();
    }
    
    public static VariableSetList copy(VariableSetList variableSet) {
        
        if (variableSet == null) {
            return null;
        }
        
        VariableSetList variableSetCopy = new VariableSetList();
        
        variableSetCopy.setId(variableSet.getId());
        variableSetCopy.setName(variableSet.getName());
        variableSetCopy.setDescription(variableSet.getDescription());
        
        return variableSetCopy;
    }
    
    public static JsonObject getJsonObject_ApiFriendly(VariableSetList variableSetList, List<VariableSet> variableSets) {
        
        if (variableSetList == null) {
            return null;
        }
        
        try {
            Gson variableSetList_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
            JsonElement variableSetList_JsonElement = variableSetList_Gson.toJsonTree(variableSetList);
            JsonObject jsonObject = new Gson().toJsonTree(variableSetList_JsonElement).getAsJsonObject();
            
            JsonArray variableSet_JsonArray = new JsonArray();
            
            if (variableSets != null) {
                for (VariableSet variableSet : variableSets) {
                    if (variableSet == null) continue;
                    variableSet_JsonArray.add(VariableSet.getJsonObject_ApiFriendly(variableSet));
                }
            }
            
            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(VariableSetList variableSetList, List<VariableSet> variableSet) {
        
        if (variableSet == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(variableSetList, variableSet);
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

}
