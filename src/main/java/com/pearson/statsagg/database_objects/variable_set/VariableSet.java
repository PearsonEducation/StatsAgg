package com.pearson.statsagg.database_objects.variable_set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.database_objects.JsonOutputFieldNamingStrategy;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSet implements DatabaseObject<VariableSet>  {
    
    private static final Logger logger = LoggerFactory.getLogger(VariableSet.class.getName());
    
    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_;
    private transient String uppercaseName_ = null;
    @SerializedName("description") private String description_;
    @SerializedName("variables") private String variables_;
    
    public VariableSet() {
        this.id_ = -1;
    }
    
    public VariableSet(Integer id, String name, String description, String variables) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, variables);
    } 
    
    public VariableSet(Integer id, String name, String uppercaseName, String description, String variables) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
        this.variables_ = variables;
    }    
    
    @Override
    public boolean isEqual(VariableSet variableSet) {
        
        if (variableSet == null) return false;
        if (variableSet == this) return true;
        if (variableSet.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, variableSet.getId())
                .append(name_, variableSet.getName())
                .append(uppercaseName_, variableSet.getUppercaseName())
                .append(description_, variableSet.getDescription())
                .append(variables_, variableSet.getVariables())
                .isEquals();
    }
    
    public static VariableSet copy(VariableSet variableSet) {
        
        if (variableSet == null) {
            return null;
        }
        
        VariableSet variableSetCopy = new VariableSet();
        
        variableSetCopy.setId(variableSet.getId());
        variableSetCopy.setName(variableSet.getName());
        variableSetCopy.setDescription(variableSet.getDescription());
        variableSetCopy.setVariables(variableSet.getVariables());
        
        return variableSetCopy;
    }
    
    public List<String> getVariables_KeysAndValues() {
        String trimmedNewLineDelimitedString = trimNewLineDelimitedVariables(variables_);
        return StringUtilities.getListOfStringsFromDelimitedString(trimmedNewLineDelimitedString, '\n');
    }
    
    public Map<String,String> getVariables_Map() {
        List<String> variableSetKeysAndValues = getVariables_KeysAndValues();
        if (variableSetKeysAndValues == null) return new HashMap<>();
        
        Map<String,String> variablesMap = new HashMap<>();
        
        for (String variableKeyAndValue : variableSetKeysAndValues) {
            try {
                if ((variableKeyAndValue == null) || variableKeyAndValue.isEmpty()) continue;
                
                String[] variableKeyAndValue_Seperated = StringUtils.split(variableKeyAndValue, "=", 2);
                
                if (variableKeyAndValue_Seperated != null) {
                    if (variableKeyAndValue_Seperated.length == 0) continue;
                    if (variableKeyAndValue_Seperated.length == 1) continue;
                    if (variableKeyAndValue_Seperated.length > 2) continue;
                    if (variableKeyAndValue_Seperated.length == 2) variablesMap.put(variableKeyAndValue_Seperated[0], variableKeyAndValue_Seperated[1]);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return variablesMap;
    }
    
    public static String trimNewLineDelimitedVariables(String newLineDelimitedVariables) {
        return DatabaseObjectCommon.trimNewLineDelimitedString(newLineDelimitedVariables);
    }

    public static JsonObject getJsonObject_ApiFriendly(VariableSet variableSet) {
        
        if (variableSet == null) {
            return null;
        }
        
        try {
            Gson notificationGroup_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
            JsonElement notificationGroup_JsonElement = notificationGroup_Gson.toJsonTree(variableSet);
            JsonObject jsonObject = new Gson().toJsonTree(notificationGroup_JsonElement).getAsJsonObject();
            
            String currentFieldToAlter = "variables";
            JsonElement currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String currentField_JsonElement_String = currentField_JsonElement.getAsString();
                if (currentField_JsonElement_String.trim().isEmpty()) jsonObject.remove(currentFieldToAlter);
                else {
                    List<String> fieldArrayValues = StringUtilities.getListOfStringsFromDelimitedString(currentField_JsonElement_String.trim(), '\n');
                    JsonArray jsonArray = new JsonArray();
                    if (fieldArrayValues != null) for (String variable : fieldArrayValues) jsonArray.add(variable);
                    jsonObject.add(currentFieldToAlter, jsonArray);
                }
            }
            
            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(VariableSet variableSet) {
        
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

    public String getVariables() {
        return variables_;
    }

    public void setVariables(String variables) {
        this.variables_ = variables;
    }
    
}
