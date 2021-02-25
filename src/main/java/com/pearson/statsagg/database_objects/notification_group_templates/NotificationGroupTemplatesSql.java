package com.pearson.statsagg.database_objects.notification_group_templates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupTemplatesSql {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplatesSql.class.getName());
    
    protected final static String Insert_NotificationGroupTemplate =
                    "INSERT INTO NOTIFICATION_GROUP_TEMPLATES " +
                    "(NAME, UPPERCASE_NAME, VARIABLE_SET_LIST_ID, NOTIFICATION_GROUP_NAME_VARIABLE, " +
                    "EMAIL_ADDRESSES_VARIABLE, PAGERDUTY_SERVICE_NAME_VARIABLE, IS_MARKED_FOR_DELETE) " +
                    "VALUES(?,?,?,?,?,?,?)";
    
    protected final static String Update_NotificationGroupTemplate_ByPrimaryKey =
                    "UPDATE NOTIFICATION_GROUP_TEMPLATES " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, VARIABLE_SET_LIST_ID = ?, " +
                    "NOTIFICATION_GROUP_NAME_VARIABLE = ?, EMAIL_ADDRESSES_VARIABLE = ?, PAGERDUTY_SERVICE_NAME_VARIABLE = ?, IS_MARKED_FOR_DELETE = ? " +
                    "WHERE ID = ?";
    
    protected final static String Update_NotificationGroupTemplate_Name =
                    "UPDATE NOTIFICATION_GROUP_TEMPLATES " +
                    "SET NAME = ?, UPPERCASE_NAME = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_NotificationGroupTemplate_ByPrimaryKey = 
                    "DELETE FROM NOTIFICATION_GROUP_TEMPLATES " +
                    "WHERE ID = ?";
    
    protected final static String Select_NotificationGroupTemplate_ByPrimaryKey = 
                    "SELECT * FROM NOTIFICATION_GROUP_TEMPLATES " +
                    "WHERE ID = ?";
    
    protected final static String Select_NotificationGroupTemplate_ByName = 
                    "SELECT * FROM NOTIFICATION_GROUP_TEMPLATES " +
                    "WHERE NAME = ?";
    
    protected final static String Select_NotificationGroupTemplate_ByUppercaseName = 
                    "SELECT * FROM NOTIFICATION_GROUP_TEMPLATES " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_NotificationGroupTemplate_Names_OrderByName = 
                    "SELECT NAME FROM NOTIFICATION_GROUP_TEMPLATES " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";
    
    protected final static String Select_AllNotificationGroupTemplates = 
                    "SELECT * FROM NOTIFICATION_GROUP_TEMPLATES";
    
    protected final static String Select_AllNotificationGroupTemplates_IdsAndNames = 
                    "SELECT ID, NAME FROM NOTIFICATION_GROUP_TEMPLATES";

}
