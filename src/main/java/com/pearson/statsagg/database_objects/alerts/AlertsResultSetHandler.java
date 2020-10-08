package com.pearson.statsagg.database_objects.alerts;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseResultSetHandler;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.math.BigDecimal;
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
public class AlertsResultSetHandler extends Alert implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsResultSetHandler.class.getName());
    
    @Override
    public List<Alert> handleResultSet(ResultSet resultSet) {
        
        List<Alert> alerts = new ArrayList<>();
        
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

                    columnName = "METRIC_GROUP_ID";
                    Integer metricGroupId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) metricGroupId = null;

                    columnName = "IS_ENABLED";
                    Boolean isEnabled = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isEnabled = null;

                    columnName = "IS_CAUTION_ENABLED";
                    Boolean isCautionEnabled = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isCautionEnabled = null;

                    columnName = "IS_DANGER_ENABLED";
                    Boolean isDangerEnabled = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isDangerEnabled = null;

                    columnName = "ALERT_TYPE";
                    Integer alertType = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) alertType = null;

                    columnName = "ALERT_ON_POSITIIVE";
                    Boolean alertOnPositive = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) alertOnPositive = null;

                    columnName = "ALLOW_RESEND_ALERT";
                    Boolean allowResendAlert = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) allowResendAlert = null;

                    columnName = "RESEND_ALERT_EVERY";
                    Long resendAlertEvery = (columnNames.contains(columnName)) ? resultSet.getLong(columnName) : null;
                    if (resultSet.wasNull()) resendAlertEvery = null;

                    columnName = "RESEND_ALERT_EVERY_TIME_UNIT";
                    Integer resendAlertEveryTimeUnit = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) resendAlertEveryTimeUnit = null;

                    columnName = "CAUTION_NOTIFICATION_GROUP_ID";
                    Integer cautionNotificationGroupId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionNotificationGroupId = null;

                    columnName = "CAUTION_POSITIVE_NOTIFICATION_GROUP_ID";
                    Integer cautionPositiveNotificationGroupId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionPositiveNotificationGroupId = null;

                    columnName = "CAUTION_OPERATOR";
                    Integer cautionOperator = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionOperator = null;

                    columnName = "CAUTION_COMBINATION";
                    Integer cautionCombination = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionCombination = null;

                    columnName = "CAUTION_COMBINATION_COUNT";
                    Integer cautionCombinationCount = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionCombinationCount = null;

                    columnName = "CAUTION_THRESHOLD";
                    BigDecimal cautionThreshold = (columnNames.contains(columnName)) ? resultSet.getBigDecimal(columnName) : null;
                    if (resultSet.wasNull()) cautionThreshold = null;

                    columnName = "CAUTION_WINDOW_DURATION";
                    Long cautionWindowDuration = (columnNames.contains(columnName)) ? resultSet.getLong(columnName) : null;
                    if (resultSet.wasNull()) cautionWindowDuration = null;

                    columnName = "CAUTION_WINDOW_DURATION_TIME_UNIT";
                    Integer cautionWindowDurationTimeUnit = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionWindowDurationTimeUnit = null;

                    columnName = "CAUTION_STOP_TRACKING_AFTER";
                    Long cautionStopTrackingAfter = (columnNames.contains(columnName)) ? resultSet.getLong(columnName) : null;
                    if (resultSet.wasNull()) cautionStopTrackingAfter = null;

                    columnName = "CAUTION_STOP_TRACKING_AFTER_TIME_UNIT";
                    Integer cautionStopTrackingAfterTimeUnit = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionStopTrackingAfterTimeUnit = null;

                    columnName = "CAUTION_MINIMUM_SAMPLE_COUNT";
                    Integer cautionMinimumSampleCount = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) cautionMinimumSampleCount = null;

                    columnName = "IS_CAUTION_ALERT_ACTIVE";
                    Boolean isCautionAlertActive = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isCautionAlertActive = null;

                    columnName = "CAUTION_ALERT_LAST_SENT_TIMESTAMP";
                    Timestamp cautionAlertLastSentTimestamp = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) cautionAlertLastSentTimestamp = null;

                    columnName = "IS_CAUTION_ACKNOWLEDGED";
                    Boolean isCautionAcknowledged = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isCautionAcknowledged = null;

                    columnName = "CAUTION_ACTIVE_ALERTS_SET";
                    String cautionActiveAlertsSet = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) cautionActiveAlertsSet = null;

                    columnName = "CAUTION_FIRST_ACTIVE_AT";
                    Timestamp cautionFirstActiveAt = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) cautionFirstActiveAt = null;

                    columnName = "DANGER_NOTIFICATION_GROUP_ID";
                    Integer dangerNotificationGroupId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerNotificationGroupId = null;

                    columnName = "DANGER_POSITIVE_NOTIFICATION_GROUP_ID";
                    Integer dangerPositiveNotificationGroupId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerPositiveNotificationGroupId = null;

                    columnName = "DANGER_OPERATOR";
                    Integer dangerOperator = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerOperator = null;

                    columnName = "DANGER_COMBINATION";
                    Integer dangerCombination = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerCombination = null;

                    columnName = "DANGER_COMBINATION_COUNT";
                    Integer dangerCombinationCount = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerCombinationCount = null;

                    columnName = "DANGER_THRESHOLD";
                    BigDecimal dangerThreshold = (columnNames.contains(columnName)) ? resultSet.getBigDecimal(columnName) : null;
                    if (resultSet.wasNull()) dangerThreshold = null;

                    columnName = "DANGER_WINDOW_DURATION";
                    Long dangerWindowDuration = (columnNames.contains(columnName)) ? resultSet.getLong(columnName) : null;
                    if (resultSet.wasNull()) dangerWindowDuration = null;

                    columnName = "DANGER_WINDOW_DURATION_TIME_UNIT";
                    Integer dangerWindowDurationTimeUnit = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerWindowDurationTimeUnit = null;

                    columnName = "DANGER_STOP_TRACKING_AFTER";
                    Long dangerStopTrackingAfter = (columnNames.contains(columnName)) ? resultSet.getLong(columnName) : null;
                    if (resultSet.wasNull()) dangerStopTrackingAfter = null;

                    columnName = "DANGER_STOP_TRACKING_AFTER_TIME_UNIT";
                    Integer dangerStopTrackingAfterTimeUnit = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerStopTrackingAfterTimeUnit = null;

                    columnName = "DANGER_MINIMUM_SAMPLE_COUNT";
                    Integer dangerMinimumSampleCount = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) dangerMinimumSampleCount = null;

                    columnName = "IS_DANGER_ALERT_ACTIVE";
                    Boolean isDangerAlertActive = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isDangerAlertActive = null;

                    columnName = "DANGER_ALERT_LAST_SENT_TIMESTAMP";
                    Timestamp dangerAlertLastSentTimestamp = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) dangerAlertLastSentTimestamp = null;

                    columnName = "IS_DANGER_ACKNOWLEDGED";
                    Boolean isDangerAcknowledged = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isDangerAcknowledged = null;

                    columnName = "DANGER_ACTIVE_ALERTS_SET";
                    String dangerActiveAlertsSet = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) dangerActiveAlertsSet = null;

                    columnName = "DANGER_FIRST_ACTIVE_AT";
                    Timestamp dangerFirstActiveAt = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) dangerFirstActiveAt = null;

                    Alert alert = new Alert(id, name, uppercaseName, description, metricGroupId, isEnabled, isCautionEnabled, isDangerEnabled, 
                            alertType, alertOnPositive, allowResendAlert, resendAlertEvery, resendAlertEveryTimeUnit, cautionNotificationGroupId, 
                            cautionPositiveNotificationGroupId, cautionOperator, cautionCombination, cautionCombinationCount, cautionThreshold, 
                            cautionWindowDuration, cautionWindowDurationTimeUnit, cautionStopTrackingAfter, cautionStopTrackingAfterTimeUnit, 
                            cautionMinimumSampleCount, isCautionAlertActive, cautionAlertLastSentTimestamp, isCautionAcknowledged, cautionActiveAlertsSet, 
                            cautionFirstActiveAt, dangerNotificationGroupId, dangerPositiveNotificationGroupId, dangerOperator, dangerCombination, 
                            dangerCombinationCount, dangerThreshold, dangerWindowDuration, dangerWindowDurationTimeUnit, dangerStopTrackingAfter, 
                            dangerStopTrackingAfterTimeUnit, dangerMinimumSampleCount, isDangerAlertActive, dangerAlertLastSentTimestamp, isDangerAcknowledged, 
                            dangerActiveAlertsSet, dangerFirstActiveAt);
            
                    alerts.add(alert);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return alerts;
    }

}

