package com.pearson.statsagg.database_objects.notification_group_templates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.database_objects.JsonOutputFieldNamingStrategy;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupTemplate implements DatabaseObject<NotificationGroupTemplate>  {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplate.class.getName());

    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_ = null;
    private transient String uppercaseName_ = null;
    
    @SerializedName("variable_set_list_id") private Integer variableSetListId_;
    
    @SerializedName("notification_group_name_variable") private String notificationGroupNameVariable_ = null;
    @SerializedName("email_addresses_variable") private String emailAddressesVariable_ = null;
    @SerializedName("pagerduty_service_name_variable") private String pagerdutyServiceNameVariable_ = null;
    private transient Boolean isMarkedForDelete_ = null;

    public NotificationGroupTemplate() {
        this.id_ = -1;
    }
    
    public NotificationGroupTemplate(Integer id, String name, Integer variableSetListId, String notificationGroupNameVariable, String emailAddressesVariable, 
            String pagerdutyServiceNameVariable, Boolean isMarkedForDelete) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), variableSetListId, notificationGroupNameVariable,
                emailAddressesVariable, pagerdutyServiceNameVariable, isMarkedForDelete);
    } 
    
    public NotificationGroupTemplate(Integer id, String name, String uppercaseName, Integer variableSetListId, String notificationGroupNameVariable,
            String emailAddressesVariable, String pagerdutyServiceNameVariable, Boolean isMarkedForDelete) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.variableSetListId_ = variableSetListId;
        this.notificationGroupNameVariable_ = notificationGroupNameVariable;
        this.emailAddressesVariable_ = emailAddressesVariable;
        this.pagerdutyServiceNameVariable_ = pagerdutyServiceNameVariable;
        this.isMarkedForDelete_ = isMarkedForDelete;
    }

    @Override
    public boolean isEqual(NotificationGroupTemplate notificationGroupTemplate) {
        
        if (notificationGroupTemplate == null) return false;
        if (notificationGroupTemplate == this) return true;
        if (notificationGroupTemplate.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, notificationGroupTemplate.getId())
                .append(name_, notificationGroupTemplate.getName())
                .append(uppercaseName_, notificationGroupTemplate.getUppercaseName())
                .append(variableSetListId_, notificationGroupTemplate.getVariableSetListId())
                .append(notificationGroupNameVariable_, notificationGroupTemplate.getNotificationGroupNameVariable())
                .append(emailAddressesVariable_, notificationGroupTemplate.getEmailAddressesVariable())
                .append(pagerdutyServiceNameVariable_, notificationGroupTemplate.getPagerdutyServiceNameVariable())
                .append(isMarkedForDelete_, notificationGroupTemplate.isMarkedForDelete())
                .isEquals();
    }
    
    public static NotificationGroupTemplate copy(NotificationGroupTemplate notificationGroupTemplate) {
        
        if (notificationGroupTemplate == null) {
            return null;
        }
        
        NotificationGroupTemplate notificationGroupTemplateCopy = new NotificationGroupTemplate();
        
        notificationGroupTemplateCopy.setId(notificationGroupTemplate.getId());
        notificationGroupTemplateCopy.setName(notificationGroupTemplate.getName());
        notificationGroupTemplateCopy.setVariableSetListId(notificationGroupTemplate.getVariableSetListId());
        notificationGroupTemplateCopy.setNotificationGroupNameVariable(notificationGroupTemplate.getNotificationGroupNameVariable());
        notificationGroupTemplateCopy.setEmailAddressesVariable(notificationGroupTemplate.getEmailAddressesVariable());
        notificationGroupTemplateCopy.setPagerdutyServiceNameVariable(notificationGroupTemplate.getPagerdutyServiceNameVariable());
        notificationGroupTemplateCopy.setIsMarkedForDelete(notificationGroupTemplate.isMarkedForDelete());

        return notificationGroupTemplateCopy;
    }
    
    public static DatabaseObjectValidation isValid(NotificationGroupTemplate notificationGroupTemplate) {
        if (notificationGroupTemplate == null) return new DatabaseObjectValidation(false, "Invalid notification group template");
        
        DatabaseObjectValidation databaseObjectValidation_CoreCriteria = notificationGroupTemplate.isValidCoreCriteria();
        if (!databaseObjectValidation_CoreCriteria.isValid()) return databaseObjectValidation_CoreCriteria;

        return new DatabaseObjectValidation(true);
    }
    
    public DatabaseObjectValidation isValidCoreCriteria() {
        if ((name_ == null) || name_.isEmpty()) return new DatabaseObjectValidation(false, "Invalid name");
        if (variableSetListId_ == null) return new DatabaseObjectValidation(false, "Invalid variable set list");
        if ((notificationGroupNameVariable_ == null) || notificationGroupNameVariable_.isEmpty()) return new DatabaseObjectValidation(false, "Invalid notification group name variable");
        if (isMarkedForDelete_ == null) return new DatabaseObjectValidation(false, "Invalid 'marked for delete' setting");

        return new DatabaseObjectValidation(true);
    }
    
    public static JsonObject getJsonObject_ApiFriendly(NotificationGroupTemplate notificationGroupTemplate) {
        
        if (notificationGroupTemplate == null) {
            return null;
        }
        
        try {
            Gson notificationGroupTemplate_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
            JsonElement notificationGroupTemplate_JsonElement = notificationGroupTemplate_Gson.toJsonTree(notificationGroupTemplate);
            JsonObject jsonObject = new Gson().toJsonTree(notificationGroupTemplate_JsonElement).getAsJsonObject();
            
            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(NotificationGroupTemplate notificationGroupTemplate) {
        
        if (notificationGroupTemplate == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(notificationGroupTemplate);
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
    
    public Integer getVariableSetListId() {
        return variableSetListId_;
    }

    public void setVariableSetListId(Integer variableSetListId) {
        this.variableSetListId_ = variableSetListId;
    }
    
    public String getNotificationGroupNameVariable() {
        return notificationGroupNameVariable_;
    }

    public void setNotificationGroupNameVariable(String notificationGroupNameVariable) {
        this.notificationGroupNameVariable_ = notificationGroupNameVariable;
    }
    
    public String getEmailAddressesVariable() {
        return emailAddressesVariable_;
    }

    public void setEmailAddressesVariable(String emailAddressesVariable) {
        this.emailAddressesVariable_ = emailAddressesVariable;
    }

    public String getPagerdutyServiceNameVariable() {
        return pagerdutyServiceNameVariable_;
    }

    public void setPagerdutyServiceNameVariable(String pagerdutyServiceNameVariable) {
        this.pagerdutyServiceNameVariable_ = pagerdutyServiceNameVariable;
    }

    public Boolean isMarkedForDelete() {
        return isMarkedForDelete_;
    }

    public void setIsMarkedForDelete(Boolean isMarkedForDelete) {
        this.isMarkedForDelete_ = isMarkedForDelete;
    }

}
