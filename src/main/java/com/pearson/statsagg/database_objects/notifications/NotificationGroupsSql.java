package com.pearson.statsagg.database_objects.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupsSql {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsSql.class.getName());
    
    protected final static String Select_NotificationGroup_ByPrimaryKey = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_NotificationGroup_ByName = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_NotificationGroup_Names_OrderByName = 
                    "SELECT NAME FROM NOTIFICATION_GROUPS " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";
    
    protected final static String Select_AllNotificationGroups = 
                    "SELECT * FROM NOTIFICATION_GROUPS";
    
    protected final static String Select_AllNotificationGroups_IdsAndNames = 
                    "SELECT ID, NAME FROM NOTIFICATION_GROUPS";
    
    protected final static String Select_DistinctNotificationGroupIds = 
                    "SELECT DISTINCT ID FROM NOTIFICATION_GROUPS";
    
    protected final static String Insert_NotificationGroup =
                    "INSERT INTO NOTIFICATION_GROUPS " +
                    "(NAME, UPPERCASE_NAME, EMAIL_ADDRESSES) " +
                    "VALUES(?,?,?)";
    
    protected final static String Update_NotificationGroup_ByPrimaryKey =
                    "UPDATE NOTIFICATION_GROUPS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, EMAIL_ADDRESSES = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_NotificationGroup_ByPrimaryKey = 
                    "DELETE FROM NOTIFICATION_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_NotificationGroups_ByPageNumberAndPageSize_Derby = 
                    "SELECT ID, NAME FROM NOTIFICATION_GROUPS ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    
    protected final static String Select_NotificationGroups_ByPageNumberAndPageSize_MySQL = 
                    "SELECT ID, NAME FROM NOTIFICATION_GROUPS ORDER BY ID ASC LIMIT ?,?";
    
}
