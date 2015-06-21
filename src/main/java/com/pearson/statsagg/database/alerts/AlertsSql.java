package com.pearson.statsagg.database.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertsSql {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsSql.class.getName());
    
    protected final static String DropTable_Alerts = 
                    "DROP TABLE ALERTS";
    
    protected final static String CreateTable_Alerts_Derby =  
                    "CREATE TABLE ALERTS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "DESCRIPTION CLOB(1048576), " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL, " +
                    "IS_ENABLED BOOLEAN NOT NULL, " +
                    "IS_CAUTION_ENABLED BOOLEAN NOT NULL, " +
                    "IS_DANGER_ENABLED BOOLEAN NOT NULL, " +
                    "ALERT_TYPE INTEGER NOT NULL, " +
                    "ALERT_ON_POSITIIVE BOOLEAN NOT NULL, " + 
                    "ALLOW_RESEND_ALERT BOOLEAN NOT NULL, " + 
                    "SEND_ALERT_EVERY_NUM_MILLISECONDS INTEGER, " + 
                    "CAUTION_NOTIFICATION_GROUP_ID INTEGER, " +
                    "CAUTION_OPERATOR INTEGER, " + 
                    "CAUTION_COMBINATION INTEGER, " + 
                    "CAUTION_COMBINATION_COUNT INTEGER, " + 
                    "CAUTION_THRESHOLD DECIMAL(31,7), " + 
                    "CAUTION_WINDOW_DURATION BIGINT, " + 
                    "CAUTION_STOP_TRACKING_AFTER BIGINT, " +
                    "CAUTION_MINIMUM_SAMPLE_COUNT INTEGER, " + 
                    "IS_CAUTION_ALERT_ACTIVE BOOLEAN NOT NULL, " +
                    "CAUTION_ALERT_LAST_SENT_TIMESTAMP TIMESTAMP, " +
                    "IS_CAUTION_ACKNOWLEDGED BOOLEAN, " +
                    "CAUTION_ACTIVE_ALERTS_SET CLOB(1048576), " +
                    "CAUTION_FIRST_ACTIVE_AT TIMESTAMP, " + 
                    "DANGER_NOTIFICATION_GROUP_ID INTEGER, " +
                    "DANGER_OPERATOR INTEGER, " + 
                    "DANGER_COMBINATION INTEGER, " + 
                    "DANGER_COMBINATION_COUNT INTEGER, " + 
                    "DANGER_THRESHOLD DECIMAL(31,7), " + 
                    "DANGER_WINDOW_DURATION BIGINT, " + 
                    "DANGER_STOP_TRACKING_AFTER BIGINT, " +
                    "DANGER_MINIMUM_SAMPLE_COUNT INTEGER, " + 
                    "IS_DANGER_ALERT_ACTIVE BOOLEAN NOT NULL, " +
                    "DANGER_ALERT_LAST_SENT_TIMESTAMP TIMESTAMP, " +
                    "IS_DANGER_ACKNOWLEDGED BOOLEAN, " +
                    "DANGER_ACTIVE_ALERTS_SET CLOB(1048576), " +
                    "DANGER_FIRST_ACTIVE_AT TIMESTAMP " + 
                    ")";
    
    protected final static String CreateTable_Alerts_MySQL =  
                    "CREATE TABLE ALERTS (" + 
                    "ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL, " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "DESCRIPTION MEDIUMTEXT, " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL, " +
                    "IS_ENABLED BOOLEAN NOT NULL, " +
                    "IS_CAUTION_ENABLED BOOLEAN NOT NULL, " +
                    "IS_DANGER_ENABLED BOOLEAN NOT NULL, " +
                    "ALERT_TYPE INTEGER NOT NULL, " +
                    "ALERT_ON_POSITIIVE BOOLEAN NOT NULL, " + 
                    "ALLOW_RESEND_ALERT BOOLEAN NOT NULL, " + 
                    "SEND_ALERT_EVERY_NUM_MILLISECONDS INTEGER, " + 
                    "CAUTION_NOTIFICATION_GROUP_ID INTEGER, " +
                    "CAUTION_OPERATOR INTEGER, " + 
                    "CAUTION_COMBINATION INTEGER, " + 
                    "CAUTION_COMBINATION_COUNT INTEGER, " + 
                    "CAUTION_THRESHOLD DECIMAL(65,7), " + 
                    "CAUTION_WINDOW_DURATION BIGINT, " + 
                    "CAUTION_STOP_TRACKING_AFTER BIGINT, " +
                    "CAUTION_MINIMUM_SAMPLE_COUNT INTEGER, " + 
                    "IS_CAUTION_ALERT_ACTIVE BOOLEAN NOT NULL, " +
                    "CAUTION_ALERT_LAST_SENT_TIMESTAMP TIMESTAMP NULL DEFAULT NULL, " +
                    "IS_CAUTION_ACKNOWLEDGED BOOLEAN, " +
                    "CAUTION_ACTIVE_ALERTS_SET MEDIUMTEXT, " +
                    "CAUTION_FIRST_ACTIVE_AT TIMESTAMP NULL DEFAULT NULL, " + 
                    "DANGER_NOTIFICATION_GROUP_ID INTEGER, " +
                    "DANGER_OPERATOR INTEGER, " + 
                    "DANGER_COMBINATION INTEGER, " + 
                    "DANGER_COMBINATION_COUNT INTEGER, " + 
                    "DANGER_THRESHOLD DECIMAL(65,7), " + 
                    "DANGER_WINDOW_DURATION BIGINT, " + 
                    "DANGER_STOP_TRACKING_AFTER BIGINT, " +
                    "DANGER_MINIMUM_SAMPLE_COUNT INTEGER, " + 
                    "IS_DANGER_ALERT_ACTIVE BOOLEAN NOT NULL, " +
                    "DANGER_ALERT_LAST_SENT_TIMESTAMP TIMESTAMP NULL DEFAULT NULL, " +
                    "IS_DANGER_ACKNOWLEDGED BOOLEAN, " +
                    "DANGER_ACTIVE_ALERTS_SET MEDIUMTEXT, " +
                    "DANGER_FIRST_ACTIVE_AT TIMESTAMP NULL DEFAULT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_Alerts_PrimaryKey =
                    "ALTER TABLE ALERTS " +
                    "ADD CONSTRAINT A_PK PRIMARY KEY (ID)";
    
    protected final static String CreateIndex_Alerts_Unique_Name =
                    "ALTER TABLE ALERTS ADD CONSTRAINT A_U_NAME UNIQUE (" + 
                    "NAME" + 
                    ")";
    
    protected final static String CreateIndex_Alerts_Unique_UppercaseName =
                    "ALTER TABLE ALERTS ADD CONSTRAINT A_U_UPPERCASE_NAME UNIQUE (" + 
                    "UPPERCASE_NAME" + 
                    ")";
    
    protected final static String CreateIndex_Alerts_ForeignKey_MetricGroupId =
                    "ALTER TABLE ALERTS " +
                    "ADD CONSTRAINT A_MGID_FK FOREIGN KEY (METRIC_GROUP_ID) " + 
                    "REFERENCES METRIC_GROUPS(ID)";
    
    protected final static String CreateIndex_Alerts_ForeignKey_CautionNotificationGroupId =
                    "ALTER TABLE ALERTS " +
                    "ADD CONSTRAINT A_CNGID_FK FOREIGN KEY (CAUTION_NOTIFICATION_GROUP_ID) " + 
                    "REFERENCES NOTIFICATION_GROUPS(ID)";
    
    protected final static String CreateIndex_Alerts_ForeignKey_DangerNotificationGroupId =
                    "ALTER TABLE ALERTS " +
                    "ADD CONSTRAINT A_DNGID_FK FOREIGN KEY (DANGER_NOTIFICATION_GROUP_ID) " + 
                    "REFERENCES NOTIFICATION_GROUPS(ID)";
    
    protected final static String Select_Alert_ByPrimaryKey = 
                    "SELECT * FROM ALERTS " +
                    "WHERE ID = ?";
    
    protected final static String Select_Alert_ByName = 
                    "SELECT * FROM ALERTS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_Alert_Names_OrderByName = 
                    "SELECT NAME FROM ALERTS " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";
    
    protected final static String Select_AllAlerts = 
                    "SELECT * FROM ALERTS";
    
    protected final static String Select_AlertNamesAssociatedWithMetricGroupId = 
                    "SELECT NAME FROM ALERTS WHERE METRIC_GROUP_ID = ?";
    
    protected final static String Select_DistinctMetricGroupIds = 
                    "SELECT DISTINCT(METRIC_GROUP_ID) FROM ALERTS";
    
    protected final static String Select_DistinctCautionNotificationGroupIds = 
                    "SELECT DISTINCT(CAUTION_NOTIFICATION_GROUP_ID) FROM ALERTS";
    
    protected final static String Select_DistinctDangerNotificationGroupIds = 
                    "SELECT DISTINCT(DANGER_NOTIFICATION_GROUP_ID) FROM ALERTS";
    
    protected final static String Insert_Alert =
                    "INSERT INTO ALERTS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, METRIC_GROUP_ID, IS_ENABLED, IS_CAUTION_ENABLED, IS_DANGER_ENABLED, ALERT_TYPE, " +
                    "ALERT_ON_POSITIIVE, ALLOW_RESEND_ALERT, SEND_ALERT_EVERY_NUM_MILLISECONDS, " + 
                    "CAUTION_NOTIFICATION_GROUP_ID, CAUTION_OPERATOR, CAUTION_COMBINATION, CAUTION_COMBINATION_COUNT, CAUTION_THRESHOLD, CAUTION_WINDOW_DURATION, " +
                    "CAUTION_STOP_TRACKING_AFTER, CAUTION_MINIMUM_SAMPLE_COUNT, IS_CAUTION_ALERT_ACTIVE, CAUTION_ALERT_LAST_SENT_TIMESTAMP, IS_CAUTION_ACKNOWLEDGED, CAUTION_ACTIVE_ALERTS_SET, CAUTION_FIRST_ACTIVE_AT, " + 
                    "DANGER_NOTIFICATION_GROUP_ID, DANGER_OPERATOR, DANGER_COMBINATION, DANGER_COMBINATION_COUNT, DANGER_THRESHOLD, DANGER_WINDOW_DURATION, " +
                    "DANGER_STOP_TRACKING_AFTER, DANGER_MINIMUM_SAMPLE_COUNT, IS_DANGER_ALERT_ACTIVE, DANGER_ALERT_LAST_SENT_TIMESTAMP, IS_DANGER_ACKNOWLEDGED, DANGER_ACTIVE_ALERTS_SET, DANGER_FIRST_ACTIVE_AT) " + 
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    protected final static String Update_Alert_ByPrimaryKey =
                    "UPDATE ALERTS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, METRIC_GROUP_ID = ?, IS_ENABLED = ?, IS_CAUTION_ENABLED = ?, IS_DANGER_ENABLED = ?, ALERT_TYPE = ?, " +
                    "ALERT_ON_POSITIIVE = ?, ALLOW_RESEND_ALERT = ?, SEND_ALERT_EVERY_NUM_MILLISECONDS = ?, " +
                    "CAUTION_NOTIFICATION_GROUP_ID = ?, CAUTION_OPERATOR = ?, CAUTION_COMBINATION = ?, CAUTION_COMBINATION_COUNT = ?, CAUTION_THRESHOLD = ?, CAUTION_WINDOW_DURATION = ?, " +
                    "CAUTION_STOP_TRACKING_AFTER =?, CAUTION_MINIMUM_SAMPLE_COUNT = ?, IS_CAUTION_ALERT_ACTIVE = ?, CAUTION_ALERT_LAST_SENT_TIMESTAMP = ?, " + 
                    "IS_CAUTION_ACKNOWLEDGED = ?, CAUTION_ACTIVE_ALERTS_SET = ?, CAUTION_FIRST_ACTIVE_AT = ?, " +
                    "DANGER_NOTIFICATION_GROUP_ID = ?, DANGER_OPERATOR = ?, DANGER_COMBINATION = ?, DANGER_COMBINATION_COUNT = ?, DANGER_THRESHOLD = ?, DANGER_WINDOW_DURATION = ?, " +
                    "DANGER_STOP_TRACKING_AFTER = ?, DANGER_MINIMUM_SAMPLE_COUNT = ?, IS_DANGER_ALERT_ACTIVE = ?, DANGER_ALERT_LAST_SENT_TIMESTAMP = ?, " + 
                    "IS_DANGER_ACKNOWLEDGED = ?, DANGER_ACTIVE_ALERTS_SET = ?, DANGER_FIRST_ACTIVE_AT = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_Alert_ByPrimaryKey =
                    "DELETE FROM ALERTS " +
                    "WHERE ID = ?";
    
    protected final static String Select_Alerts_ByPageNumberAndPageSize_Derby = 
                    "SELECT ID, NAME FROM ALERTS ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    
}
