package com.pearson.statsagg.database_objects.suspensions;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseResultSetHandler;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SuspensionsResultSetHandler extends Suspension implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionsResultSetHandler.class.getName());
    
    @Override
    public List<Suspension> handleResultSet(ResultSet resultSet) {
        
        List<Suspension> suspensions = new ArrayList<>();
        
        try {
            Set<String> columnNames = DatabaseUtils.getResultSetColumns(resultSet);
            
            while ((columnNames != null) && resultSet.next()) {
                try {
                    String columnName = "ID";
                    Integer id = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) id = null;

                    columnName = "NAME";
                    String name = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) name = null;

                    columnName = "UPPERCASE_NAME";
                    String uppercaseName = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) uppercaseName = null;

                    columnName = "DESCRIPTION";
                    String description = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) description = null;

                    columnName = "IS_ENABLED";
                    Boolean isEnabled = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isEnabled = null;

                    columnName = "SUSPEND_BY";
                    Integer suspendBy = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) suspendBy = null;

                    columnName = "ALERT_ID";
                    Integer alertId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) alertId = null;

                    columnName = "METRIC_GROUP_TAGS_INCLUSIVE";
                    String metricGroupTagsInclusive = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) metricGroupTagsInclusive = null;

                    columnName = "METRIC_GROUP_TAGS_EXCLUSIVE";
                    String metricGroupTagsExclusive = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) metricGroupTagsExclusive = null;

                    columnName = "METRIC_SUSPENSION_REGEXES";
                    String metricSuspensionRegexes = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) metricSuspensionRegexes = null;

                    columnName = "IS_ONE_TIME";
                    Boolean isOneTime = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isOneTime = null;

                    columnName = "IS_SUSPEND_NOTIFICATION_ONLY";
                    Boolean isSuspendNotificationOnly = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isSuspendNotificationOnly = null;

                    columnName = "IS_RECUR_SUNDAY";
                    Boolean isRecurSunday = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isRecurSunday = null;

                    columnName = "IS_RECUR_MONDAY";
                    Boolean isRecurMonday = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isRecurMonday = null;

                    columnName = "IS_RECUR_TUESDAY";
                    Boolean isRecurTuesday = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isRecurTuesday = null;

                    columnName = "IS_RECUR_WEDNESDAY";
                    Boolean isRecurWednesday = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isRecurWednesday = null;

                    columnName = "IS_RECUR_THURSDAY";
                    Boolean isRecurThursday = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isRecurThursday = null;

                    columnName = "IS_RECUR_FRIDAY";
                    Boolean isRecurFriday = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isRecurFriday = null;

                    columnName = "IS_RECUR_SATURDAY";
                    Boolean isRecurSaturday = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isRecurSaturday = null;

                    columnName = "START_DATE";
                    Timestamp startDate = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) startDate = null;

                    columnName = "START_TIME";
                    Timestamp startTime = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) startTime = null;

                    columnName = "DURATION";
                    Long duration = (columnNames.contains(columnName)) ? resultSet.getLong(columnName) : null;
                    if (resultSet.wasNull()) duration = null;

                    columnName = "DURATION_TIME_UNIT";
                    Integer durationTimeUnit = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) durationTimeUnit = null;

                    columnName = "DELETE_AT_TIMESTAMP";
                    Timestamp deleteAtTimestamp = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) deleteAtTimestamp = null; 

                    Suspension suspension = new Suspension(
                            id, name, uppercaseName, description, isEnabled, suspendBy, alertId, metricGroupTagsInclusive, metricGroupTagsExclusive, metricSuspensionRegexes,
                            isOneTime, isSuspendNotificationOnly, isRecurSunday, isRecurMonday, isRecurTuesday, isRecurWednesday, isRecurThursday, isRecurFriday, isRecurSaturday, 
                            startDate, startTime, duration, durationTimeUnit, deleteAtTimestamp);
            
                    suspensions.add(suspension);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return suspensions;
    }

}

