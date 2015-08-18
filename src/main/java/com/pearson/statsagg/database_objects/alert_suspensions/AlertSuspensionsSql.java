package com.pearson.statsagg.database_objects.alert_suspensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertSuspensionsSql {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionsSql.class.getName());
    
    protected final static String DropTable_AlertSuspensions = 
                    "DROP TABLE ALERT_SUSPENSIONS";
    
    protected final static String CreateTable_AlertSuspensions_Derby =  
                    "CREATE TABLE ALERT_SUSPENSIONS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "DESCRIPTION CLOB(1048576) NOT NULL, " + 
                    "IS_ENABLED BOOLEAN NOT NULL, " + 
                    "SUSPEND_BY INTEGER NOT NULL, " + 
                    "ALERT_ID INTEGER, " +
                    "METRIC_GROUP_TAGS_INCLUSIVE CLOB(1048576), " + 
                    "METRIC_GROUP_TAGS_EXCLUSIVE CLOB(1048576), " + 
                    "IS_ONE_TIME BOOLEAN NOT NULL, " + 
                    "IS_SUSPEND_NOTIFICATION_ONLY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_SUNDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_MONDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_TUESDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_WEDNESDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_THURSDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_FRIDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_SATURDAY BOOLEAN NOT NULL, " +
                    "START_DATE TIMESTAMP NOT NULL, " + 
                    "START_TIME TIMESTAMP NOT NULL, " +
                    "DURATION BIGINT NOT NULL, " + 
                    "DURATION_TIME_UNIT INTEGER NOT NULL, " + 
                    "DELETE_AT_TIMESTAMP TIMESTAMP " + 
                    ")";
    
    protected final static String CreateTable_AlertSuspensions_MySQL =  
                    "CREATE TABLE ALERT_SUSPENSIONS (" + 
                    "ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL, " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "DESCRIPTION MEDIUMTEXT NOT NULL, " + 
                    "IS_ENABLED BOOLEAN NOT NULL, " + 
                    "SUSPEND_BY INTEGER NOT NULL, " + 
                    "ALERT_ID INTEGER, " +
                    "METRIC_GROUP_TAGS_INCLUSIVE MEDIUMTEXT, " + 
                    "METRIC_GROUP_TAGS_EXCLUSIVE MEDIUMTEXT, " + 
                    "IS_ONE_TIME BOOLEAN NOT NULL, " + 
                    "IS_SUSPEND_NOTIFICATION_ONLY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_SUNDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_MONDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_TUESDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_WEDNESDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_THURSDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_FRIDAY BOOLEAN NOT NULL, " + 
                    "IS_RECUR_SATURDAY BOOLEAN NOT NULL, " +
                    "START_DATE TIMESTAMP NULL DEFAULT NULL, " + 
                    "START_TIME TIMESTAMP NULL DEFAULT NULL, " +
                    "DURATION BIGINT NOT NULL, " + 
                    "DURATION_TIME_UNIT INTEGER NOT NULL, " + 
                    "DELETE_AT_TIMESTAMP TIMESTAMP NULL DEFAULT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_AlertSuspensions_PrimaryKey =
                    "ALTER TABLE ALERT_SUSPENSIONS " +
                    "ADD CONSTRAINT AS_PK PRIMARY KEY (ID)";
    
    protected final static String CreateIndex_AlertSuspensions_Unique_Name =
                    "ALTER TABLE ALERT_SUSPENSIONS ADD CONSTRAINT AS_U_NAME UNIQUE (" + 
                    "NAME" + 
                    ")";

    protected final static String CreateIndex_AlertSuspensions_Unique_UppercaseName =
                    "ALTER TABLE ALERT_SUSPENSIONS ADD CONSTRAINT AS_U_UPPERCASE_NAME UNIQUE (" + 
                    "UPPERCASE_NAME" + 
                    ")";
    
    protected final static String CreateIndex_AlertSuspensions_SuspendBy =
                    "CREATE INDEX AS_SUSPEND_BY ON ALERT_SUSPENSIONS(SUSPEND_BY)";

    protected final static String CreateIndex_AlertSuspensions_DeleteAtTimestamp =
                    "CREATE INDEX AS_DELETE_AT_TIMESTAMP ON ALERT_SUSPENSIONS(DELETE_AT_TIMESTAMP)";
    
    protected final static String CreateIndex_AlertSuspensions_ForeignKey_AlertId =
                    "ALTER TABLE ALERT_SUSPENSIONS " +
                    "ADD CONSTRAINT AS_AID_FK FOREIGN KEY (ALERT_ID) " + 
                    "REFERENCES ALERTS(ID)";
    
    protected final static String Select_AlertSuspension_ByPrimaryKey = 
                    "SELECT * FROM ALERT_SUSPENSIONS " +
                    "WHERE ID = ?";
    
    protected final static String Select_AlertSuspension_ByName = 
                    "SELECT * FROM ALERT_SUSPENSIONS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_AllAlertSuspensions = 
                    "SELECT * FROM ALERT_SUSPENSIONS";
    
    protected final static String Select_AlertSuspension_BySuspendBy = 
                    "SELECT * FROM ALERT_SUSPENSIONS " +
                    "WHERE SUSPEND_BY = ?";
    
    protected final static String Insert_AlertSuspension =
                    "INSERT INTO ALERT_SUSPENSIONS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, IS_ENABLED, SUSPEND_BY, ALERT_ID, METRIC_GROUP_TAGS_INCLUSIVE, METRIC_GROUP_TAGS_EXCLUSIVE, " + 
                    "IS_ONE_TIME, IS_SUSPEND_NOTIFICATION_ONLY, " +
                    "IS_RECUR_SUNDAY, IS_RECUR_MONDAY, IS_RECUR_TUESDAY, IS_RECUR_WEDNESDAY, IS_RECUR_THURSDAY, IS_RECUR_FRIDAY, IS_RECUR_SATURDAY, " +
                    "START_DATE, START_TIME, DURATION, DURATION_TIME_UNIT, DELETE_AT_TIMESTAMP) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    protected final static String Update_AlertSuspension_ByPrimaryKey =
                    "UPDATE ALERT_SUSPENSIONS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, IS_ENABLED = ?, SUSPEND_BY = ?, ALERT_ID = ?, " +
                    " METRIC_GROUP_TAGS_INCLUSIVE = ?, METRIC_GROUP_TAGS_EXCLUSIVE = ?, IS_ONE_TIME = ?, IS_SUSPEND_NOTIFICATION_ONLY = ?, " + 
                    "IS_RECUR_SUNDAY = ?, IS_RECUR_MONDAY = ?, IS_RECUR_TUESDAY = ?, IS_RECUR_WEDNESDAY = ?, " +
                    "IS_RECUR_THURSDAY = ?, IS_RECUR_FRIDAY = ?, IS_RECUR_SATURDAY = ?, " +
                    "START_DATE = ?, START_TIME = ?, DURATION = ?, DURATION_TIME_UNIT = ?, DELETE_AT_TIMESTAMP = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_AlertSuspension_ByPrimaryKey =
                    "DELETE FROM ALERT_SUSPENSIONS " +
                    "WHERE ID = ?";
    
    protected final static String Delete_AlertSuspension_DeleteAtTimestamp =
                    "DELETE FROM ALERT_SUSPENSIONS " +
                    "WHERE DELETE_AT_TIMESTAMP <= ?";
    
    protected final static String Select_AlertSuspension_ByPageNumberAndPageSize_Derby = 
                    "SELECT ID, NAME FROM ALERT_SUSPENSIONS ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    protected final static String Select_AlertSuspension_ByPageNumberAndPageSize_MySQL = 
                    "SELECT ID, NAME FROM ALERT_SUSPENSIONS LIMIT ?,?";
}
