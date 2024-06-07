package com.pearson.statsagg.database_objects.suspensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SuspensionsSql {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionsSql.class.getName());

    protected final static String Select_Suspension_ByPrimaryKey = 
                    "SELECT * FROM SUSPENSIONS " +
                    "WHERE ID = ?";
    
    protected final static String Select_Suspension_ByName = 
                    "SELECT * FROM SUSPENSIONS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_Suspension_ByUppercaseName = 
                    "SELECT * FROM SUSPENSIONS " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_AllSuspensions = 
                    "SELECT * FROM SUSPENSIONS";
    
    protected final static String Select_SuspensionId_BySuspendBy = 
                    "SELECT ID FROM SUSPENSIONS " +
                    "WHERE SUSPEND_BY = ?";
    
    protected final static String Select_Suspension_BySuspendBy = 
                    "SELECT * FROM SUSPENSIONS " +
                    "WHERE SUSPEND_BY = ?";
    
    protected final static String Select_Suspension_ByAlertId = 
                    "SELECT * FROM SUSPENSIONS " +
                    "WHERE ALERT_ID = ?";
    
    protected final static String Insert_Suspension =
                    "INSERT INTO SUSPENSIONS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, IS_ENABLED, SUSPEND_BY, ALERT_ID, METRIC_GROUP_TAGS_INCLUSIVE, METRIC_GROUP_TAGS_EXCLUSIVE, METRIC_SUSPENSION_REGEXES, " + 
                    "IS_ONE_TIME, IS_SUSPEND_NOTIFICATION_ONLY, " +
                    "IS_RECUR_SUNDAY, IS_RECUR_MONDAY, IS_RECUR_TUESDAY, IS_RECUR_WEDNESDAY, IS_RECUR_THURSDAY, IS_RECUR_FRIDAY, IS_RECUR_SATURDAY, " +
                    "START_DATE, START_TIME, DURATION, DURATION_TIME_UNIT, DELETE_AT_TIMESTAMP) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
    protected final static String Update_Suspension_ByPrimaryKey =
                    "UPDATE SUSPENSIONS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, IS_ENABLED = ?, SUSPEND_BY = ?, ALERT_ID = ?, " +
                    "METRIC_GROUP_TAGS_INCLUSIVE = ?, METRIC_GROUP_TAGS_EXCLUSIVE = ?, METRIC_SUSPENSION_REGEXES = ?, IS_ONE_TIME = ?, IS_SUSPEND_NOTIFICATION_ONLY = ?, " + 
                    "IS_RECUR_SUNDAY = ?, IS_RECUR_MONDAY = ?, IS_RECUR_TUESDAY = ?, IS_RECUR_WEDNESDAY = ?, " +
                    "IS_RECUR_THURSDAY = ?, IS_RECUR_FRIDAY = ?, IS_RECUR_SATURDAY = ?, " +
                    "START_DATE = ?, START_TIME = ?, DURATION = ?, DURATION_TIME_UNIT = ?, DELETE_AT_TIMESTAMP = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_Suspension_ByPrimaryKey =
                    "DELETE FROM SUSPENSIONS " +
                    "WHERE ID = ?";
    
    protected final static String Delete_Suspension_DeleteAtTimestamp =
                    "DELETE FROM SUSPENSIONS " +
                    "WHERE DELETE_AT_TIMESTAMP <= ?";
    
    protected final static String Select_Suspension_ByPageNumberAndPageSize_Derby = 
                    "SELECT ID, NAME FROM SUSPENSIONS ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

    protected final static String Select_Suspension_ByPageNumberAndPageSize_MySQL = 
                    "SELECT ID, NAME FROM SUSPENSIONS ORDER BY ID ASC LIMIT ?,?";
}
