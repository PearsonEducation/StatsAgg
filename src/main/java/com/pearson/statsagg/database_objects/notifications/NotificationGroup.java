package com.pearson.statsagg.database_objects.notifications;

import com.pearson.statsagg.database_engine.DatabaseObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroup extends DatabaseObject<NotificationGroup> {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroup.class.getName());

    private Integer id_;
    private String name_ = null;
    private String uppercaseName_ = null;
    private String emailAddresses_ = null;
    
    public NotificationGroup() {
        this.id_ = -1;
    }
    
    public NotificationGroup(Integer id, String name, String emailAddresses) {
        this(id, name, ((name == null) ? null : name.toUpperCase()), emailAddresses);
    } 
    
    public NotificationGroup(Integer id, String name, String uppercaseName, String emailAddresses) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.emailAddresses_ = emailAddresses;
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
                .append(emailAddresses_, notificationGroup.getEmailAddresses())
                .isEquals();

    }

    public static NotificationGroup copy(NotificationGroup notificationGroup) {
        
        if (notificationGroup == null) {
            return null;
        }
        
        NotificationGroup notificationGroupCopy = new NotificationGroup();
        
        notificationGroupCopy.setId(notificationGroup.getId());
        notificationGroupCopy.setName(notificationGroup.getName());
        notificationGroupCopy.setUppercaseName(notificationGroup.getUppercaseName());
        notificationGroupCopy.setEmailAddresses(notificationGroup.getEmailAddresses());
        
        return notificationGroupCopy;
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
    
    public String getEmailAddresses() {
        return emailAddresses_;
    }

    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses_ = emailAddresses;
    }
    
}
