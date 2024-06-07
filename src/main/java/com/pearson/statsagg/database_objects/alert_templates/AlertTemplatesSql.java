package com.pearson.statsagg.database_objects.alert_templates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplatesSql {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertTemplatesSql.class.getName());
    
    protected final static String Insert_AlertTemplate =
                    "INSERT INTO ALERT_TEMPLATES " +
                    "(NAME, UPPERCASE_NAME, VARIABLE_SET_LIST_ID, DESCRIPTION_VARIABLE, ALERT_NAME_VARIABLE, METRIC_GROUP_NAME_VARIABLE, IS_ENABLED, " +
                    "IS_CAUTION_ENABLED, IS_DANGER_ENABLED, ALERT_TYPE, ALERT_ON_POSITIIVE, ALLOW_RESEND_ALERT, RESEND_ALERT_EVERY, RESEND_ALERT_EVERY_TIME_UNIT, " + 
                    "CAUTION_NOTIFICATION_GROUP_NAME_VARIABLE, CAUTION_POSITIVE_NOTIFICATION_GROUP_NAME_VARIABLE, CAUTION_OPERATOR, CAUTION_COMBINATION, CAUTION_COMBINATION_COUNT, " +
                    "CAUTION_THRESHOLD, CAUTION_WINDOW_DURATION, CAUTION_WINDOW_DURATION_TIME_UNIT, CAUTION_STOP_TRACKING_AFTER, CAUTION_STOP_TRACKING_AFTER_TIME_UNIT, " + 
                    "CAUTION_MINIMUM_SAMPLE_COUNT, " +
                    "DANGER_NOTIFICATION_GROUP_NAME_VARIABLE, DANGER_POSITIVE_NOTIFICATION_GROUP_NAME_VARIABLE, DANGER_OPERATOR, DANGER_COMBINATION, DANGER_COMBINATION_COUNT, " +
                    "DANGER_THRESHOLD, DANGER_WINDOW_DURATION, DANGER_WINDOW_DURATION_TIME_UNIT, DANGER_STOP_TRACKING_AFTER, DANGER_STOP_TRACKING_AFTER_TIME_UNIT, " +
                    "DANGER_MINIMUM_SAMPLE_COUNT) " + 
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    protected final static String Update_AlertTemplate_ByPrimaryKey =
                    "UPDATE ALERT_TEMPLATES " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, VARIABLE_SET_LIST_ID = ?, DESCRIPTION_VARIABLE = ?, ALERT_NAME_VARIABLE = ?, METRIC_GROUP_NAME_VARIABLE = ?, IS_ENABLED = ?, " +
                    "IS_CAUTION_ENABLED = ?, IS_DANGER_ENABLED = ?, ALERT_TYPE = ?, ALERT_ON_POSITIIVE = ?, ALLOW_RESEND_ALERT = ?, RESEND_ALERT_EVERY = ?, RESEND_ALERT_EVERY_TIME_UNIT = ?, " +
                    "CAUTION_NOTIFICATION_GROUP_NAME_VARIABLE = ?, CAUTION_POSITIVE_NOTIFICATION_GROUP_NAME_VARIABLE = ?, CAUTION_OPERATOR = ?, CAUTION_COMBINATION = ?, CAUTION_COMBINATION_COUNT = ?, " +
                    "CAUTION_THRESHOLD = ?, CAUTION_WINDOW_DURATION = ?, CAUTION_WINDOW_DURATION_TIME_UNIT = ?, CAUTION_STOP_TRACKING_AFTER = ?, CAUTION_STOP_TRACKING_AFTER_TIME_UNIT = ?, " + 
                    "CAUTION_MINIMUM_SAMPLE_COUNT = ?, " +
                    "DANGER_NOTIFICATION_GROUP_NAME_VARIABLE = ?, DANGER_POSITIVE_NOTIFICATION_GROUP_NAME_VARIABLE = ?, DANGER_OPERATOR = ?, DANGER_COMBINATION = ?, DANGER_COMBINATION_COUNT = ?, " +
                    "DANGER_THRESHOLD = ?, DANGER_WINDOW_DURATION = ?, DANGER_WINDOW_DURATION_TIME_UNIT = ?, DANGER_STOP_TRACKING_AFTER = ?, DANGER_STOP_TRACKING_AFTER_TIME_UNIT = ?, " + 
                    "DANGER_MINIMUM_SAMPLE_COUNT = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_AlertTemplate_ByPrimaryKey =
                    "DELETE FROM ALERT_TEMPLATES " +
                    "WHERE ID = ?";
    
    protected final static String Select_AllAlertTemplates = 
                    "SELECT * FROM ALERT_TEMPLATES";
    
    protected final static String Select_AlertTemplateNames = 
                    "SELECT NAME FROM ALERT_TEMPLATES";
    
    protected final static String Select_AlertTemplate_ByPrimaryKey = 
                    "SELECT * FROM ALERT_TEMPLATES " +
                    "WHERE ID = ?";
    
    protected final static String Select_AlertTemplate_ByName = 
                    "SELECT * FROM ALERT_TEMPLATES " +
                    "WHERE NAME = ?";
    
    protected final static String Select_AlertTemplate_ByUppercaseName = 
                    "SELECT * FROM ALERT_TEMPLATES " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_AlertTemplate_Names_OrderByName = 
                    "SELECT NAME FROM ALERT_TEMPLATES " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";

    protected final static String Select_AlertTemplates_IdAndName = 
                    "SELECT ID, NAME FROM ALERT_TEMPLATES ORDER BY ID";
    
}
