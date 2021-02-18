package com.pearson.statsagg.database_objects.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertsSql {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsSql.class.getName());
    
    protected final static String Insert_Alert =
                    "INSERT INTO ALERTS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, ALERT_TEMPLATE_ID, VARIABLE_SET_ID, METRIC_GROUP_ID, IS_ENABLED, " +
                    "IS_CAUTION_ENABLED, IS_DANGER_ENABLED, ALERT_TYPE, ALERT_ON_POSITIIVE, ALLOW_RESEND_ALERT, RESEND_ALERT_EVERY, RESEND_ALERT_EVERY_TIME_UNIT, " + 
                    "CAUTION_NOTIFICATION_GROUP_ID, CAUTION_POSITIVE_NOTIFICATION_GROUP_ID, CAUTION_OPERATOR, CAUTION_COMBINATION, CAUTION_COMBINATION_COUNT, " +
                    "CAUTION_THRESHOLD, CAUTION_WINDOW_DURATION, CAUTION_WINDOW_DURATION_TIME_UNIT, CAUTION_STOP_TRACKING_AFTER, CAUTION_STOP_TRACKING_AFTER_TIME_UNIT, " + 
                    "CAUTION_MINIMUM_SAMPLE_COUNT, IS_CAUTION_ALERT_ACTIVE, CAUTION_ALERT_LAST_SENT_TIMESTAMP, IS_CAUTION_ACKNOWLEDGED, CAUTION_ACTIVE_ALERTS_SET, CAUTION_FIRST_ACTIVE_AT, " +
                    "DANGER_NOTIFICATION_GROUP_ID, DANGER_POSITIVE_NOTIFICATION_GROUP_ID, DANGER_OPERATOR, DANGER_COMBINATION, DANGER_COMBINATION_COUNT, " +
                    "DANGER_THRESHOLD, DANGER_WINDOW_DURATION, DANGER_WINDOW_DURATION_TIME_UNIT, DANGER_STOP_TRACKING_AFTER, DANGER_STOP_TRACKING_AFTER_TIME_UNIT, " +
                    "DANGER_MINIMUM_SAMPLE_COUNT, IS_DANGER_ALERT_ACTIVE, DANGER_ALERT_LAST_SENT_TIMESTAMP, IS_DANGER_ACKNOWLEDGED, DANGER_ACTIVE_ALERTS_SET, DANGER_FIRST_ACTIVE_AT) " + 
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    protected final static String Update_Alert_ByPrimaryKey =
                    "UPDATE ALERTS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, ALERT_TEMPLATE_ID = ?, VARIABLE_SET_ID = ?, METRIC_GROUP_ID = ?, IS_ENABLED = ?, " +
                    "IS_CAUTION_ENABLED = ?, IS_DANGER_ENABLED = ?, ALERT_TYPE = ?, ALERT_ON_POSITIIVE = ?, ALLOW_RESEND_ALERT = ?, RESEND_ALERT_EVERY = ?, RESEND_ALERT_EVERY_TIME_UNIT = ?, " +
                    "CAUTION_NOTIFICATION_GROUP_ID = ?, CAUTION_POSITIVE_NOTIFICATION_GROUP_ID = ?, CAUTION_OPERATOR = ?, CAUTION_COMBINATION = ?, CAUTION_COMBINATION_COUNT = ?, " +
                    "CAUTION_THRESHOLD = ?, CAUTION_WINDOW_DURATION = ?, CAUTION_WINDOW_DURATION_TIME_UNIT = ?, CAUTION_STOP_TRACKING_AFTER = ?, CAUTION_STOP_TRACKING_AFTER_TIME_UNIT = ?, " + 
                    "CAUTION_MINIMUM_SAMPLE_COUNT = ?, IS_CAUTION_ALERT_ACTIVE = ?, CAUTION_ALERT_LAST_SENT_TIMESTAMP = ?, " + 
                    "IS_CAUTION_ACKNOWLEDGED = ?, CAUTION_ACTIVE_ALERTS_SET = ?, CAUTION_FIRST_ACTIVE_AT = ?, " +
                    "DANGER_NOTIFICATION_GROUP_ID = ?, DANGER_POSITIVE_NOTIFICATION_GROUP_ID = ?, DANGER_OPERATOR = ?, DANGER_COMBINATION = ?, DANGER_COMBINATION_COUNT = ?, " +
                    "DANGER_THRESHOLD = ?, DANGER_WINDOW_DURATION = ?, DANGER_WINDOW_DURATION_TIME_UNIT = ?, DANGER_STOP_TRACKING_AFTER = ?, DANGER_STOP_TRACKING_AFTER_TIME_UNIT = ?, " + 
                    "DANGER_MINIMUM_SAMPLE_COUNT = ?, IS_DANGER_ALERT_ACTIVE = ?, DANGER_ALERT_LAST_SENT_TIMESTAMP = ?, " + 
                    "IS_DANGER_ACKNOWLEDGED = ?, DANGER_ACTIVE_ALERTS_SET = ?, DANGER_FIRST_ACTIVE_AT = ? " +
                    "WHERE ID = ?";
    
    
    protected final static String Update_Alert_Name =
                    "UPDATE ALERTS " +
                    "SET NAME = ?, UPPERCASE_NAME = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_Alert_ByPrimaryKey =
                    "DELETE FROM ALERTS " +
                    "WHERE ID = ?";
    
    protected final static String Select_AllAlerts = 
                    "SELECT * FROM ALERTS";
    
    protected final static String Select_Alert_ByPrimaryKey = 
                    "SELECT * FROM ALERTS " +
                    "WHERE ID = ?";
    
    protected final static String Select_Alert_ByName = 
                    "SELECT * FROM ALERTS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_Alert_ByUppercaseName = 
                    "SELECT * FROM ALERTS " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_Alert_ByAlertTemplateId = 
                    "SELECT * FROM ALERTS " +
                    "WHERE ALERT_TEMPLATE_ID = ?";
    
    protected final static String Select_Alert_Names_OrderByName = 
                    "SELECT NAME FROM ALERTS " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";

    protected final static String Select_AlertNames = 
                    "SELECT NAME FROM ALERTS";
    
    protected final static String Select_AlertNamesAssociatedWithMetricGroupId = 
                    "SELECT NAME FROM ALERTS WHERE METRIC_GROUP_ID = ?";
    
    protected final static String Select_DistinctMetricGroupIds = 
                    "SELECT DISTINCT(METRIC_GROUP_ID) FROM ALERTS";

    protected final static String Select_AllDistinctNotificationGroupIds = 
                    "(SELECT CAUTION_NOTIFICATION_GROUP_ID AS CAUTION_NOTIFICATION_GROUP_ID FROM ALERTS WHERE CAUTION_NOTIFICATION_GROUP_ID IS NOT NULL) " + 
                    "UNION " +
                    "(SELECT CAUTION_POSITIVE_NOTIFICATION_GROUP_ID AS CAUTION_NOTIFICATION_GROUP_ID FROM ALERTS WHERE CAUTION_POSITIVE_NOTIFICATION_GROUP_ID IS NOT NULL) " + 
                    "UNION " +
                    "(SELECT DANGER_NOTIFICATION_GROUP_ID AS CAUTION_NOTIFICATION_GROUP_ID FROM ALERTS WHERE DANGER_NOTIFICATION_GROUP_ID IS NOT NULL) " + 
                    "UNION " +
                    "(SELECT DANGER_POSITIVE_NOTIFICATION_GROUP_ID AS CAUTION_NOTIFICATION_GROUP_ID FROM ALERTS WHERE DANGER_POSITIVE_NOTIFICATION_GROUP_ID IS NOT NULL)";
    
    protected final static String Select_Alerts_IdAndName = 
                    "SELECT ID, NAME FROM ALERTS ORDER BY ID";
    
}
