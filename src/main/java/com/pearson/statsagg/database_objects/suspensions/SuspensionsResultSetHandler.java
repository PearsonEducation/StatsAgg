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
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    String description = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "description", String.class);
                    Boolean isEnabled = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_enabled", Boolean.class);
                    Integer suspendBy = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "suspend_by", Integer.class);
                    Integer alertId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "alert_id", Integer.class);
                    String metricGroupTagsInclusive = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_tags_inclusive", String.class);
                    String metricGroupTagsExclusive = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_tags_exclusive", String.class);
                    String metricSuspensionRegexes = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_suspension_regexes", String.class);
                    Boolean isOneTime = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_one_time", Boolean.class);
                    Boolean isSuspendNotificationOnly = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_suspend_notification_only", Boolean.class);
                    Boolean isRecurSunday = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_recur_sunday", Boolean.class);
                    Boolean isRecurMonday = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_recur_monday", Boolean.class);
                    Boolean isRecurTuesday = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_recur_tuesday", Boolean.class);
                    Boolean isRecurWednesday = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_recur_wednesday", Boolean.class);
                    Boolean isRecurThursday = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_recur_thursday", Boolean.class);
                    Boolean isRecurFriday = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_recur_friday", Boolean.class);
                    Boolean isRecurSaturday = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_recur_saturday", Boolean.class);
                    Timestamp startDate = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "start_date", Timestamp.class);
                    Timestamp startTime = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "start_time", Timestamp.class);
                    Long duration = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "duration", Long.class);
                    Integer durationTimeUnit = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "duration_time_unit", Integer.class);
                    Timestamp deleteAtTimestamp = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "delete_at_timestamp", Timestamp.class);

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

