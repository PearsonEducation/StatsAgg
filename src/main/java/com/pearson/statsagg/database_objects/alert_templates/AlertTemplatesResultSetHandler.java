package com.pearson.statsagg.database_objects.alert_templates;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseResultSetHandler;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplatesResultSetHandler extends AlertTemplate implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertTemplatesResultSetHandler.class.getName());
    
    @Override
    public List<AlertTemplate> handleResultSet(ResultSet resultSet) {
        
        List<AlertTemplate> alertTemplates = new ArrayList<>();
        
        try {
            Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    Integer variableSetListId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variable_set_list_id", Integer.class);
                    String descriptionVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "description_variable", String.class);
                    String alertNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "alert_name_variable", String.class);
                    String metricGroupNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_name_variable", String.class);
                    Boolean isEnabled = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_enabled", Boolean.class);
                    Boolean isCautionEnabled = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_caution_enabled", Boolean.class);
                    Boolean isDangerEnabled = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_danger_enabled", Boolean.class);
                    Integer alertType = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "alert_type", Integer.class);
                    Boolean alertOnPositive = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "alert_on_positiive", Boolean.class);
                    Boolean allowResendAlert = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "allow_resend_alert", Boolean.class);
                    Long resendAlertEvery = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "resend_alert_every", Long.class);
                    Integer resendAlertEveryTimeUnit = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "resend_alert_every_time_unit", Integer.class);
                    String cautionNotificationGroupNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_notification_group_name_variable", String.class);
                    String cautionPositiveNotificationGroupNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_positive_notification_group_name_variable", String.class);
                    Integer cautionOperator = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_operator", Integer.class);
                    Integer cautionCombination = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_combination", Integer.class);
                    Integer cautionCombinationCount = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_combination_count", Integer.class);
                    BigDecimal cautionThreshold = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_threshold", BigDecimal.class);
                    Long cautionWindowDuration = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_window_duration", Long.class);
                    Integer cautionWindowDurationTimeUnit = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_window_duration_time_unit", Integer.class);
                    Long cautionStopTrackingAfter = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_stop_tracking_after", Long.class);
                    Integer cautionStopTrackingAfterTimeUnit = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_stop_tracking_after_time_unit", Integer.class);
                    Integer cautionMinimumSampleCount = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "caution_minimum_sample_count", Integer.class);
                    String dangerNotificationGroupNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_notification_group_name_variable", String.class);
                    String dangerPositiveNotificationGroupNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_positive_notification_group_name_variable", String.class);
                    Integer dangerOperator = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_operator", Integer.class);
                    Integer dangerCombination = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_combination", Integer.class);
                    Integer dangerCombinationCount = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_combination_count", Integer.class);
                    BigDecimal dangerThreshold = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_threshold", BigDecimal.class);
                    Long dangerWindowDuration = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_window_duration", Long.class);
                    Integer dangerWindowDurationTimeUnit = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_window_duration_time_unit", Integer.class);
                    Long dangerStopTrackingAfter = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_stop_tracking_after", Long.class);
                    Integer dangerStopTrackingAfterTimeUnit = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_stop_tracking_after_time_unit", Integer.class);
                    Integer dangerMinimumSampleCount = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "danger_minimum_sample_count", Integer.class);

                    AlertTemplate alertTemplate = new AlertTemplate(id, name, uppercaseName, variableSetListId, descriptionVariable, alertNameVariable, metricGroupNameVariable, 
                            isEnabled, isCautionEnabled, isDangerEnabled, 
                            alertType, alertOnPositive, allowResendAlert, resendAlertEvery, resendAlertEveryTimeUnit, cautionNotificationGroupNameVariable, 
                            cautionPositiveNotificationGroupNameVariable, cautionOperator, cautionCombination, cautionCombinationCount, cautionThreshold, 
                            cautionWindowDuration, cautionWindowDurationTimeUnit, cautionStopTrackingAfter, cautionStopTrackingAfterTimeUnit, cautionMinimumSampleCount, 
                            dangerNotificationGroupNameVariable, dangerPositiveNotificationGroupNameVariable, dangerOperator, dangerCombination, 
                            dangerCombinationCount, dangerThreshold, dangerWindowDuration, dangerWindowDurationTimeUnit, dangerStopTrackingAfter, 
                            dangerStopTrackingAfterTimeUnit, dangerMinimumSampleCount);
            
                    alertTemplates.add(alertTemplate);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return alertTemplates;
    }

}

