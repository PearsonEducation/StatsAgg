package com.pearson.statsagg.database_objects.notification_groups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupsSql {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsSql.class.getName());
    
    protected final static String Insert_NotificationGroup =
                    "INSERT INTO NOTIFICATION_GROUPS " +
                    "(NAME, UPPERCASE_NAME, NOTIFICATION_GROUP_TEMPLATE_ID, VARIABLE_SET_ID, EMAIL_ADDRESSES, PAGERDUTY_SERVICE_ID) " +
                    "VALUES(?,?,?,?,?,?)";
    
    protected final static String Update_NotificationGroup_ByPrimaryKey =
                    "UPDATE NOTIFICATION_GROUPS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, NOTIFICATION_GROUP_TEMPLATE_ID = ?, VARIABLE_SET_ID = ?, EMAIL_ADDRESSES = ?, PAGERDUTY_SERVICE_ID = ? " +
                    "WHERE ID = ?";
    
    protected final static String Update_NotificationGroup_Name =
                    "UPDATE NOTIFICATION_GROUPS " +
                    "SET NAME = ?, UPPERCASE_NAME = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_NotificationGroup_ByPrimaryKey = 
                    "DELETE FROM NOTIFICATION_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_NotificationGroup_ByPrimaryKey = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_NotificationGroup_ByName = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_NotificationGroup_ByUppercaseName = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_NotificationGroup_Names_OrderByName = 
                    "SELECT NAME FROM NOTIFICATION_GROUPS " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";
    
    protected final static String Select_AllNotificationGroups = 
                    "SELECT * FROM NOTIFICATION_GROUPS";
    
    protected final static String Select_AllNotificationGroups_IdsAndNames = 
                    "SELECT ID, NAME FROM NOTIFICATION_GROUPS";

    protected final static String Select_DistinctPagerdutyServiceIds = 
                    "SELECT DISTINCT(PAGERDUTY_SERVICE_ID) FROM NOTIFICATION_GROUPS";
    
    protected final static String Select_NotificationGroup_ByNotificationGroupTemplateId = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE NOTIFICATION_GROUP_TEMPLATE_ID = ?";
    
}
