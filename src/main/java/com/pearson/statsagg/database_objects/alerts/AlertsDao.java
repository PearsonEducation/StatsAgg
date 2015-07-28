package com.pearson.statsagg.database_objects.alerts;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.database_engine.DatabaseObjectDao;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertsDao extends DatabaseObjectDao<Alert> {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsDao.class.getName());
   
    private final String tableName_ = "ALERTS";
    
    public AlertsDao(){}
            
    public AlertsDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public boolean dropTable() {
        return dropTable(AlertsSql.DropTable_Alerts);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(AlertsSql.CreateTable_Alerts_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(AlertsSql.CreateTable_Alerts_Derby);
            databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_PrimaryKey);
        }
        
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_Unique_Name);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_Unique_UppercaseName);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_ForeignKey_MetricGroupId);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_ForeignKey_CautionNotificationGroupId);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_ForeignKey_DangerNotificationGroupId);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_ForeignKey_CautionPositiveNotificationGroupId);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_ForeignKey_DangerPositiveNotificationGroupId);
        
        return createTable(databaseCreationSqlStatements);
    }

    @Override
    public Alert getDatabaseObject(Alert alert) {
        if (alert == null) return null;
        
        return getDatabaseObject(AlertsSql.Select_Alert_ByPrimaryKey, 
                alert.getId()); 
    }
    
    @Override
    public boolean insert(Alert alert) {
        if (alert == null) return false;
        
        return insert(AlertsSql.Insert_Alert, 
                alert.getName(), alert.getUppercaseName(), alert.getDescription(), alert.getMetricGroupId(), 
                alert.isEnabled(), alert.isCautionEnabled(), alert.isDangerEnabled(), alert.getAlertType(), alert.isAlertOnPositive(),
                alert.isAllowResendAlert(), alert.getSendAlertEveryNumMilliseconds(),
                alert.getCautionNotificationGroupId(), alert.getCautionPositiveNotificationGroupId(), alert.getCautionOperator(), alert.getCautionCombination(),   
                alert.getCautionCombinationCount(), alert.getCautionThreshold(), alert.getCautionWindowDuration(), alert.getCautionWindowDurationTimeUnit(),   
                alert.getCautionStopTrackingAfter(), alert.getCautionStopTrackingAfterTimeUnit(), alert.getCautionMinimumSampleCount(), alert.isCautionAlertActive(),  
                alert.getCautionAlertLastSentTimestamp(), alert.isCautionAcknowledged(),alert.getCautionActiveAlertsSet(), alert.getCautionFirstActiveAt(),
                alert.getDangerNotificationGroupId(), alert.getDangerPositiveNotificationGroupId(), alert.getDangerOperator(), alert.getDangerCombination(),   
                alert.getDangerCombinationCount(), alert.getDangerThreshold(), alert.getDangerWindowDuration(), alert.getDangerWindowDurationTimeUnit(), 
                alert.getDangerStopTrackingAfter(), alert.getDangerStopTrackingAfterTimeUnit(), alert.getDangerMinimumSampleCount(), alert.isDangerAlertActive(), 
                alert.getDangerAlertLastSentTimestamp(), alert.isDangerAcknowledged(), alert.getDangerActiveAlertsSet(), alert.getDangerFirstActiveAt()
        );
    }
    
    @Override
    public boolean update(Alert alert) {
        if (alert == null) return false;
        
        return update(AlertsSql.Update_Alert_ByPrimaryKey, 
                alert.getName(), alert.getUppercaseName(), alert.getDescription(), alert.getMetricGroupId(), 
                alert.isEnabled(), alert.isCautionEnabled(), alert.isDangerEnabled(), alert.getAlertType(), alert.isAlertOnPositive(),
                alert.isAllowResendAlert(), alert.getSendAlertEveryNumMilliseconds(),
                alert.getCautionNotificationGroupId(), alert.getCautionPositiveNotificationGroupId(), alert.getCautionOperator(), alert.getCautionCombination(),   
                alert.getCautionCombinationCount(), alert.getCautionThreshold(), alert.getCautionWindowDuration(), alert.getCautionWindowDurationTimeUnit(),   
                alert.getCautionStopTrackingAfter(), alert.getCautionStopTrackingAfterTimeUnit(), alert.getCautionMinimumSampleCount(), alert.isCautionAlertActive(),  
                alert.getCautionAlertLastSentTimestamp(), alert.isCautionAcknowledged(),alert.getCautionActiveAlertsSet(), alert.getCautionFirstActiveAt(),
                alert.getDangerNotificationGroupId(), alert.getDangerPositiveNotificationGroupId(), alert.getDangerOperator(), alert.getDangerCombination(),   
                alert.getDangerCombinationCount(), alert.getDangerThreshold(), alert.getDangerWindowDuration(), alert.getDangerWindowDurationTimeUnit(), 
                alert.getDangerStopTrackingAfter(), alert.getDangerStopTrackingAfterTimeUnit(), alert.getDangerMinimumSampleCount(), alert.isDangerAlertActive(), 
                alert.getDangerAlertLastSentTimestamp(), alert.isDangerAcknowledged(), alert.getDangerActiveAlertsSet(), alert.getDangerFirstActiveAt(), 
                alert.getId());
    }

    @Override
    public boolean delete(Alert alert) {
        if (alert == null) return false;
        
        return delete(AlertsSql.Delete_Alert_ByPrimaryKey, 
                alert.getId()); 
    }
    
    @Override
    public Alert processSingleResultAllColumns(ResultSet resultSet) {
        
        try {     
            if ((resultSet == null) || resultSet.isClosed()) {
                return null;
            }

            Integer id = resultSet.getInt("ID");
            if (resultSet.wasNull()) id = null;
            
            String name = resultSet.getString("NAME");
            if (resultSet.wasNull()) name = null;
            
            String uppercaseName = resultSet.getString("UPPERCASE_NAME");
            if (resultSet.wasNull()) uppercaseName = null;
            
            String description = resultSet.getString("DESCRIPTION");
            if (resultSet.wasNull()) description = null;
            
            Integer metricGroupId = resultSet.getInt("METRIC_GROUP_ID");
            if (resultSet.wasNull()) metricGroupId = null;
            
            Boolean isEnabled = resultSet.getBoolean("IS_ENABLED");
            if (resultSet.wasNull()) isEnabled = null;

            Boolean isCautionEnabled = resultSet.getBoolean("IS_CAUTION_ENABLED");
            if (resultSet.wasNull()) isCautionEnabled = null;
            
            Boolean isDangerEnabled = resultSet.getBoolean("IS_DANGER_ENABLED");
            if (resultSet.wasNull()) isDangerEnabled = null;
            
            Integer alertType = resultSet.getInt("ALERT_TYPE");
            if (resultSet.wasNull()) alertType = null;
            
            Boolean alertOnPositive = resultSet.getBoolean("ALERT_ON_POSITIIVE");
            if (resultSet.wasNull()) alertOnPositive = null;
            
            Boolean allowResendAlert = resultSet.getBoolean("ALLOW_RESEND_ALERT");
            if (resultSet.wasNull()) allowResendAlert = null;
            
            Integer sendAlertEveryNumMilliseconds = resultSet.getInt("SEND_ALERT_EVERY_NUM_MILLISECONDS");
            if (resultSet.wasNull()) sendAlertEveryNumMilliseconds = null;

            Integer cautionNotificationGroupId = resultSet.getInt("CAUTION_NOTIFICATION_GROUP_ID");
            if (resultSet.wasNull()) cautionNotificationGroupId = null;
            
            Integer cautionPositiveNotificationGroupId = resultSet.getInt("CAUTION_POSITIVE_NOTIFICATION_GROUP_ID");
            if (resultSet.wasNull()) cautionPositiveNotificationGroupId = null;
            
            Integer cautionOperator = resultSet.getInt("CAUTION_OPERATOR");
            if (resultSet.wasNull()) cautionOperator = null;
            
            Integer cautionCombination = resultSet.getInt("CAUTION_COMBINATION");
            if (resultSet.wasNull()) cautionCombination = null;
            
            Integer cautionCombinationCount = resultSet.getInt("CAUTION_COMBINATION_COUNT");
            if (resultSet.wasNull()) cautionCombinationCount = null;
            
            BigDecimal cautionThreshold = resultSet.getBigDecimal("CAUTION_THRESHOLD");
            if (resultSet.wasNull()) cautionThreshold = null;

            Long cautionWindowDuration = resultSet.getLong("CAUTION_WINDOW_DURATION");
            if (resultSet.wasNull()) cautionWindowDuration = null;
            
            Integer cautionWindowDurationTimeUnit = resultSet.getInt("CAUTION_WINDOW_DURATION_TIME_UNIT");
            if (resultSet.wasNull()) cautionWindowDurationTimeUnit = null;
            
            Long cautionStopTrackingAfter = resultSet.getLong("CAUTION_STOP_TRACKING_AFTER");
            if (resultSet.wasNull()) cautionStopTrackingAfter = null;
            
            Integer cautionStopTrackingAfterTimeUnit = resultSet.getInt("CAUTION_STOP_TRACKING_AFTER_TIME_UNIT");
            if (resultSet.wasNull()) cautionStopTrackingAfterTimeUnit = null;
            
            Integer cautionMinimumSampleCount = resultSet.getInt("CAUTION_MINIMUM_SAMPLE_COUNT");
            if (resultSet.wasNull()) cautionMinimumSampleCount = null;
            
            Boolean isCautionAlertActive = resultSet.getBoolean("IS_CAUTION_ALERT_ACTIVE");
            if (resultSet.wasNull()) isCautionAlertActive = null;
            
            Timestamp cautionAlertLastSentTimestamp = resultSet.getTimestamp("CAUTION_ALERT_LAST_SENT_TIMESTAMP");
            if (resultSet.wasNull()) cautionAlertLastSentTimestamp = null;
            
            Boolean isCautionAcknowledged = resultSet.getBoolean("IS_CAUTION_ACKNOWLEDGED");
            if (resultSet.wasNull()) isCautionAcknowledged = null;
            
            String cautionActiveAlertsSet = resultSet.getString("CAUTION_ACTIVE_ALERTS_SET");
            if (resultSet.wasNull()) cautionActiveAlertsSet = null;

            Timestamp cautionFirstActiveAt = resultSet.getTimestamp("CAUTION_FIRST_ACTIVE_AT");
            if (resultSet.wasNull()) cautionFirstActiveAt = null;
            
            Integer dangerNotificationGroupId = resultSet.getInt("DANGER_NOTIFICATION_GROUP_ID");
            if (resultSet.wasNull()) dangerNotificationGroupId = null;
            
            Integer dangerPositiveNotificationGroupId = resultSet.getInt("DANGER_POSITIVE_NOTIFICATION_GROUP_ID");
            if (resultSet.wasNull()) dangerPositiveNotificationGroupId = null;
            
            Integer dangerOperator = resultSet.getInt("DANGER_OPERATOR");
            if (resultSet.wasNull()) dangerOperator = null;
            
            Integer dangerCombination = resultSet.getInt("DANGER_COMBINATION");
            if (resultSet.wasNull()) dangerCombination = null;
            
            Integer dangerCombinationCount = resultSet.getInt("DANGER_COMBINATION_COUNT");
            if (resultSet.wasNull()) dangerCombinationCount = null;
            
            BigDecimal dangerThreshold = resultSet.getBigDecimal("DANGER_THRESHOLD");
            if (resultSet.wasNull()) dangerThreshold = null;

            Long dangerWindowDuration = resultSet.getLong("DANGER_WINDOW_DURATION");
            if (resultSet.wasNull()) dangerWindowDuration = null;

            Integer dangerWindowDurationTimeUnit = resultSet.getInt("DANGER_WINDOW_DURATION_TIME_UNIT");
            if (resultSet.wasNull()) dangerWindowDurationTimeUnit = null;
            
            Long dangerStopTrackingAfter = resultSet.getLong("DANGER_STOP_TRACKING_AFTER");
            if (resultSet.wasNull()) dangerStopTrackingAfter = null;
            
            Integer dangerStopTrackingAfterTimeUnit = resultSet.getInt("DANGER_STOP_TRACKING_AFTER_TIME_UNIT");
            if (resultSet.wasNull()) dangerStopTrackingAfterTimeUnit = null;
                    
            Integer dangerMinimumSampleCount = resultSet.getInt("DANGER_MINIMUM_SAMPLE_COUNT");
            if (resultSet.wasNull()) dangerMinimumSampleCount = null;
            
            Boolean isDangerAlertActive = resultSet.getBoolean("IS_DANGER_ALERT_ACTIVE");
            if (resultSet.wasNull()) isDangerAlertActive = null;
            
            Timestamp dangerAlertLastSentTimestamp = resultSet.getTimestamp("DANGER_ALERT_LAST_SENT_TIMESTAMP");
            if (resultSet.wasNull()) dangerAlertLastSentTimestamp = null;
            
            Boolean isDangerAcknowledged = resultSet.getBoolean("IS_DANGER_ACKNOWLEDGED");
            if (resultSet.wasNull()) isDangerAcknowledged = null;
            
            String dangerActiveAlertsSet = resultSet.getString("DANGER_ACTIVE_ALERTS_SET");
            if (resultSet.wasNull()) dangerActiveAlertsSet = null;

            Timestamp dangerFirstActiveAt = resultSet.getTimestamp("DANGER_FIRST_ACTIVE_AT");
            if (resultSet.wasNull()) dangerFirstActiveAt = null;
    
            Alert alert = new Alert(id, name, uppercaseName, description, metricGroupId, isEnabled, isCautionEnabled, isDangerEnabled,
                    alertType, alertOnPositive, allowResendAlert, sendAlertEveryNumMilliseconds, 
                    cautionNotificationGroupId, cautionPositiveNotificationGroupId, cautionOperator, cautionCombination, cautionCombinationCount, cautionThreshold, 
                    cautionWindowDuration, cautionWindowDurationTimeUnit, cautionStopTrackingAfter, cautionStopTrackingAfterTimeUnit, cautionMinimumSampleCount, 
                    isCautionAlertActive, cautionAlertLastSentTimestamp, isCautionAcknowledged, cautionActiveAlertsSet, cautionFirstActiveAt,
                    dangerNotificationGroupId, dangerPositiveNotificationGroupId, dangerOperator, dangerCombination, dangerCombinationCount,  dangerThreshold, 
                    dangerWindowDuration, dangerWindowDurationTimeUnit, dangerStopTrackingAfter, dangerStopTrackingAfterTimeUnit, dangerMinimumSampleCount,  
                    isDangerAlertActive, dangerAlertLastSentTimestamp, isDangerAcknowledged, dangerActiveAlertsSet, dangerFirstActiveAt);
            
            return alert;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }

    @Override
    public String getTableName() {
        return tableName_;
    }

    public Alert getAlert(int id) {
        return getDatabaseObject(AlertsSql.Select_Alert_ByPrimaryKey, 
                id); 
    }  
    
    public List<String> getAlertNames(String filter, int resultSetLimit) {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            List<String> alertNames = new ArrayList<>();
            
            databaseInterface_.createPreparedStatement(AlertsSql.Select_Alert_Names_OrderByName, 1000);
            databaseInterface_.addPreparedStatementParameters("%" + filter + "%");
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            int rowCounter = 0;
            while (resultSet.next() && (rowCounter < resultSetLimit)) {
                String name = resultSet.getString("NAME");
                if (resultSet.wasNull()) name = null;
                
                if (name != null) {
                    alertNames.add(name);
                    rowCounter++;
                }
            }
            
            return alertNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public Alert getAlertByName(String name) {
        
        try {

            if (name == null) {
                return null;
            }
            
            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(AlertsSql.Select_Alert_ByName, 1);
            databaseInterface_.addPreparedStatementParameters(name);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            if (resultSet.next()) {
                Alert alert = processSingleResultAllColumns(resultSet);
                return alert;
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public List<String> getAlertsAssociatedWithMetricGroupId(Integer metricGroupId) {
        
        try {

            if (metricGroupId == null) {
                return null;
            }
            
            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(AlertsSql.Select_AlertNamesAssociatedWithMetricGroupId, 1000);
            databaseInterface_.addPreparedStatementParameters(metricGroupId);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }
            
            List<String> alertNames = new ArrayList<>();

            ResultSet resultSet = databaseInterface_.getResults();
            
            while (resultSet.next()) {
                String alertName = resultSet.getString("NAME");
                if (resultSet.wasNull()) alertName = null;
                if (alertName != null) alertNames.add(alertName);
            }
            
            return alertNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public Set<Integer> getDistinctMetricGroupIds() {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(AlertsSql.Select_DistinctMetricGroupIds, 100);
            databaseInterface_.addPreparedStatementParameters();
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }
            
            Set<Integer> metricGroupIds = new HashSet<>();
            
            ResultSet resultSet = databaseInterface_.getResults();
            
            while (resultSet.next()) {
                Integer metricGroupId = resultSet.getInt(1);
                if (resultSet.wasNull()) metricGroupId = null;
                
                if (metricGroupId != null) metricGroupIds.add(metricGroupId);
            }

            return metricGroupIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }

    public Set<Integer> getAllDistinctNotificationGroupIds() {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(AlertsSql.Select_AllDistinctNotificationGroupIds, 100);
            databaseInterface_.addPreparedStatementParameters();
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }
            
            Set<Integer> notificationGroupIds = new HashSet<>();
            
            ResultSet resultSet = databaseInterface_.getResults();
            
            while (resultSet.next()) {
                Integer notificationGroupId = resultSet.getInt(1);
                if (resultSet.wasNull()) notificationGroupId = null;
                
                if (notificationGroupId != null) notificationGroupIds.add(notificationGroupId);
            }

            return notificationGroupIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }

     public JSONObject getAlerts(int offset, int pageSize) {        
        logger.debug("getAlerts");
        List<Object> parametersList = new ArrayList<>(2);
        
        JSONArray alertsList = new JSONArray();
        JSONObject alertsJson = new JSONObject();
        int alertsCount = 0;
        
        try {
            if (!isConnectionValid()) {
                return null;
            }
            
            if ((offset == 0) && (pageSize == 0)) {
                alertsJson.put("alerts", alertsList);
                alertsJson.put("count", alertsCount);
                return alertsJson;
            }
            
            parametersList.add(offset);
            parametersList.add(pageSize);
            databaseInterface_.createPreparedStatement(AlertsSql.Select_Alerts_ByPageNumberAndPageSize_Derby, pageSize);
            databaseInterface_.addPreparedStatementParameters(parametersList);

            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                logger.debug("Invalid resultset");
                return null;
            }
            
            ResultSet resultSet = databaseInterface_.getResults();
            
            while(resultSet.next()) {
                JSONObject alert = new JSONObject();
                alert.put("name", resultSet.getString("NAME"));
                alert.put("id", resultSet.getString("ID"));
                alertsList.add(alert);
                alertsCount++;
            }
            
            alertsJson.put("alerts", alertsList);
            alertsJson.put("count", alertsCount);
            
            return alertsJson;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
    }
     
}
