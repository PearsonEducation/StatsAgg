package com.pearson.statsagg.database.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupsSql {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsSql.class.getName());
    
    protected final static String DropTable_NotificationGroups = 
                    "DROP TABLE NOTIFICATION_GROUPS";
    
    protected final static String CreateTable_NotificationGroups =  
                    "CREATE TABLE NOTIFICATION_GROUPS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "NAME VARCHAR(32000) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(32000) NOT NULL, " + 
                    "EMAIL_ADDRESSES VARCHAR(32000) NOT NULL" + 
                    ")";
    
    protected final static String CreateIndex_NotificationGroups_PrimaryKey =
                    "ALTER TABLE NOTIFICATION_GROUPS ADD CONSTRAINT N_PK PRIMARY KEY (" + 
                    "ID" + 
                    ")";
    
    protected final static String CreateIndex_NotificationGroups_Unique_Name =
                    "ALTER TABLE NOTIFICATION_GROUPS ADD CONSTRAINT N_U_NAME UNIQUE (" + 
                    "NAME" + 
                    ")";
    
    protected final static String CreateIndex_NotificationGroups_Unique_UppercaseName =
                    "ALTER TABLE NOTIFICATION_GROUPS ADD CONSTRAINT N_U_UPPERCASE_NAME UNIQUE (" + 
                    "UPPERCASE_NAME" + 
                    ")";
    
    protected final static String Select_NotificationGroup_ByPrimaryKey = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_NotificationGroup_ByName = 
                    "SELECT * FROM NOTIFICATION_GROUPS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_NotificationGroup_Names = 
                    "SELECT NAME FROM NOTIFICATION_GROUPS " +
                    "WHERE NAME LIKE ?";
    
    protected final static String Select_AllNotificationGroups = 
                    "SELECT * FROM NOTIFICATION_GROUPS";
    
    protected final static String Select_AllNotificationGroup_IdsAndNames = 
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
    
}
