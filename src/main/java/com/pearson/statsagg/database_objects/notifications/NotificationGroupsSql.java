package com.pearson.statsagg.database_objects.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupsSql {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsSql.class.getName());
    
    protected final static String DropTable_NotificationGroups = 
                    "DROP TABLE NOTIFICATION_GROUPS";
    
    protected final static String CreateTable_NotificationGroups_Derby =  
                    "CREATE TABLE NOTIFICATION_GROUPS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "EMAIL_ADDRESSES CLOB(65535) NOT NULL" + 
                    ")";
    
    protected final static String CreateTable_NotificationGroups_MySQL =  
                    "CREATE TABLE NOTIFICATION_GROUPS (" + 
                    "ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL, " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "EMAIL_ADDRESSES TEXT NOT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_NotificationGroups_PrimaryKey =
                    "ALTER TABLE NOTIFICATION_GROUPS ADD CONSTRAINT NG_PK PRIMARY KEY (" + 
                    "ID" + 
                    ")";
    
    protected final static String CreateIndex_NotificationGroups_Unique_Name =
                    "ALTER TABLE NOTIFICATION_GROUPS ADD CONSTRAINT NG_U_NAME UNIQUE (" + 
                    "NAME" + 
                    ")";
    
    protected final static String CreateIndex_NotificationGroups_Unique_UppercaseName =
                    "ALTER TABLE NOTIFICATION_GROUPS ADD CONSTRAINT NG_U_UPPERCASE_NAME UNIQUE (" + 
                    "UPPERCASE_NAME" + 
                    ")";
    
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
    
    protected final static String Select_NotificationGroups_ByPageNumberAndPageSize_Derby = 
                    "SELECT ID, NAME FROM NOTIFICATION_GROUPS ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    
    protected final static String Select_NotificationGroups_ByPageNumberAndPageSize_MySQL = 
                    "SELECT ID, NAME FROM NOTIFICATION_GROUPS ORDER BY ID ASC LIMIT ?,?";
    
}
