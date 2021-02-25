package com.pearson.statsagg.database_objects.notification_groups;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.database_objects.JsonOutputFieldNamingStrategy;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroup implements DatabaseObject<NotificationGroup>  {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroup.class.getName());

    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_ = null;
    private transient String uppercaseName_ = null;
    @SerializedName("notification_group_template_id") private Integer notificationGroupTemplateId_ = null;
    @SerializedName("variable_set_id") private Integer variableSetId_ = null;
    @SerializedName("email_addresses") private String emailAddresses_ = null;
    @SerializedName("pagerduty_service_id") private Integer pagerdutyServiceId_ = null;

    public NotificationGroup() {
        this.id_ = -1;
    }
    
    public NotificationGroup(Integer id, String name, Integer notificationGroupTemplateId, Integer variableSetId, 
            String emailAddresses, Integer pagerdutyServiceId) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), notificationGroupTemplateId, variableSetId, emailAddresses, pagerdutyServiceId);
    } 
    
    public NotificationGroup(Integer id, String name, String uppercaseName, Integer notificationGroupTemplateId, Integer variableSetId, 
            String emailAddresses, Integer pagerdutyServiceId) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.notificationGroupTemplateId_ = notificationGroupTemplateId;
        this.variableSetId_ = variableSetId;
        this.emailAddresses_ = emailAddresses;
        this.pagerdutyServiceId_ = pagerdutyServiceId;
    }

    @Override
    public boolean isEqual(NotificationGroup notificationGroup) {
        
        if (notificationGroup == null) return false;
        if (notificationGroup == this) return true;
        if (notificationGroup.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, notificationGroup.getId())
                .append(name_, notificationGroup.getName())
                .append(uppercaseName_, notificationGroup.getUppercaseName())
                .append(notificationGroupTemplateId_, notificationGroup.getNotificationGroupTemplateId())
                .append(variableSetId_, notificationGroup.getVariableSetId())
                .append(emailAddresses_, notificationGroup.getEmailAddresses())
                .append(pagerdutyServiceId_, notificationGroup.getPagerdutyServiceId())
                .isEquals();

    }
    
    public static NotificationGroup copy(NotificationGroup notificationGroup) {
        
        if (notificationGroup == null) {
            return null;
        }
        
        NotificationGroup notificationGroupCopy = new NotificationGroup();
        
        notificationGroupCopy.setId(notificationGroup.getId());
        notificationGroupCopy.setName(notificationGroup.getName());
        notificationGroupCopy.setVariableSetId(notificationGroup.getVariableSetId());
        notificationGroupCopy.setNotificationGroupTemplateId(notificationGroup.getNotificationGroupTemplateId());
        notificationGroupCopy.setEmailAddresses(notificationGroup.getEmailAddresses());
        notificationGroupCopy.setPagerdutyServiceId(notificationGroup.getPagerdutyServiceId());
        
        return notificationGroupCopy;
    }
    
    public static DatabaseObjectValidation isValid(NotificationGroup notificationGroup) {
        if (notificationGroup == null) return new DatabaseObjectValidation(false, "Invalid notification group");
        if ((notificationGroup.getName() == null) || notificationGroup.getName().isEmpty()) return new DatabaseObjectValidation(false, "Invalid name");

        return new DatabaseObjectValidation(true);
    }
    
    public String getEmailAddressesCsv() {
        
        if (emailAddresses_ == null) {
            return null;
        }
        
        StringBuilder emailAddressesOutput = new StringBuilder();
        
        try {
            String[] emailAddresses = StringUtils.split(emailAddresses_, ",");

            if ((emailAddresses != null) && (emailAddresses.length != 0)) {
                for (int i = 0; i < emailAddresses.length; i++) {
                    String trimmedEmailAddress = emailAddresses[i].trim();
                    emailAddressesOutput.append(trimmedEmailAddress);
                    if ((i + 1) != emailAddresses.length) emailAddressesOutput.append(", ");
                }
            }
        }
        catch(Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return emailAddressesOutput.toString();
    }
    
    public List<String> getEmailAddressesList() {

        if (emailAddresses_ == null) {
            return new ArrayList<>();
        }
        
        List<String> emailAddresses = new ArrayList<>();
        
        try {
            String[] emailAddresses_Array = StringUtils.split(emailAddresses_, ",");
            emailAddresses.addAll(Arrays.asList(emailAddresses_Array));
        }
        catch(Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return emailAddresses;
    }

    public static JsonObject getJsonObject_ApiFriendly(NotificationGroup notificationGroup) {
        
        if (notificationGroup == null) {
            return null;
        }
        
        try {
            Gson notificationGroup_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
            JsonElement notificationGroup_JsonElement = notificationGroup_Gson.toJsonTree(notificationGroup);
            JsonObject jsonObject = new Gson().toJsonTree(notificationGroup_JsonElement).getAsJsonObject();
            
            JsonArray emailAddresses_JsonArray = new JsonArray();
            List<String> emailAddresses_List = notificationGroup.getEmailAddressesList();
            
            if (emailAddresses_List != null) {
                for (String emailAddress : emailAddresses_List) {
                    if (emailAddress == null) continue;
                    emailAddresses_JsonArray.add(emailAddress.trim());
                }
            }
                    
            jsonObject.remove("email_addresses");
            jsonObject.add("email_addresses", emailAddresses_JsonArray);
            
            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(NotificationGroup notificationGroup){
        
        if (notificationGroup == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(notificationGroup);
            if (jsonObject == null) return null;

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();   
            return gson.toJson(jsonObject);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static NotificationGroup createNotificationGroupFromNotificationGroupTemplate(NotificationGroupTemplate notificationGroupTemplate, 
            Integer variableSetId, Integer notificationGroupId, String notificationGroupName, 
            String emailAddresses, Integer pagerdutyServiceId) {

        if (notificationGroupTemplate == null) {
            return null;
        }
        
        NotificationGroup notificationGroup = new NotificationGroup();

        if (notificationGroupId == null) notificationGroup.setId(-1);
        else notificationGroup.setId(notificationGroupId);
        
        notificationGroup.setNotificationGroupTemplateId(notificationGroupTemplate.getId());
        notificationGroup.setVariableSetId(variableSetId);

        notificationGroup.setName(notificationGroupName);
        notificationGroup.setEmailAddresses(emailAddresses);
        notificationGroup.setPagerdutyServiceId(pagerdutyServiceId);

        return notificationGroup;
    }
    
    public static boolean areNotificationGroupTemplateIdsInConflict(NotificationGroup notificationGroup1, NotificationGroup notificationGroup2) {
        if (notificationGroup2 == null) return false;
        if (notificationGroup1 == null) return false;
        
        if ((notificationGroup1.getNotificationGroupTemplateId() == null) && (notificationGroup2.getNotificationGroupTemplateId() == null)) return false;
        if ((notificationGroup1.getNotificationGroupTemplateId() == null) && (notificationGroup2.getNotificationGroupTemplateId() != null)) return true;
        if ((notificationGroup1.getNotificationGroupTemplateId() != null) && (notificationGroup2.getNotificationGroupTemplateId() == null)) return true;

        return !notificationGroup1.getNotificationGroupTemplateId().equals(notificationGroup2.getNotificationGroupTemplateId());
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
    
    public Integer getNotificationGroupTemplateId() {
        return notificationGroupTemplateId_;
    }

    public void setNotificationGroupTemplateId(Integer notificationGroupTemplateId) {
        this.notificationGroupTemplateId_ = notificationGroupTemplateId;
    }

    public Integer getVariableSetId() {
        return variableSetId_;
    }

    public void setVariableSetId(Integer variableSetId) {
        this.variableSetId_ = variableSetId;
    }
    
    public String getEmailAddresses() {
        return emailAddresses_;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses_ = emailAddresses;
    }

    public Integer getPagerdutyServiceId() {
        return pagerdutyServiceId_;
    }

    public void setPagerdutyServiceId(Integer pagerdutyServiceId) {
        this.pagerdutyServiceId_ = pagerdutyServiceId;
    }
    
}
