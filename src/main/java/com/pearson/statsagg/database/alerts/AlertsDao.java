package com.pearson.statsagg.database.alerts;

import com.pearson.statsagg.database.DatabaseInterface;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.database.DatabaseObjectDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.sql.Clob;
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
        databaseCreationSqlStatements.add(AlertsSql.CreateTable_Alerts);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_PrimaryKey);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_Unique_Name);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_Unique_UppercaseName);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_ForeignKey_MetricGroupId);
        databaseCreationSqlStatements.add(AlertsSql.CreateIndex_Alerts_ForeignKey_NotificationGroupId);
        
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
                alert.getName(), alert.getUppercaseName(), alert.getDescription(), alert.getMetricGroupId(), alert.getNotificationGroupId(),
                alert.isEnabled(), alert.isAlertOnPositive(),
                alert.isAllowResendAlert(), alert.getSendAlertEveryNumMilliseconds(),
                alert.getCautionAlertType(), alert.getCautionOperator(), alert.getCautionCombination(), alert.getCautionCombinationCount(),  
                alert.getCautionThreshold(), alert.getCautionWindowDuration(), alert.getCautionMinimumSampleCount(), 
                alert.isCautionAlertActive(), alert.getCautionAlertLastSentTimestamp(), alert.getCautionActiveAlertsSet(),
                alert.getDangerAlertType(), alert.getDangerOperator(), alert.getDangerCombination(), alert.getDangerCombinationCount(),  
                alert.getDangerThreshold(), alert.getDangerWindowDuration(), alert.getDangerMinimumSampleCount(), 
                alert.isDangerAlertActive(), alert.getDangerAlertLastSentTimestamp(), alert.getDangerActiveAlertsSet()
        );
    }
    
    @Override
    public boolean update(Alert alert) {
        if (alert == null) return false;
        
        return update(AlertsSql.Update_Alert_ByPrimaryKey, 
                alert.getName(), alert.getUppercaseName(), alert.getDescription(), alert.getMetricGroupId(), alert.getNotificationGroupId(), 
                alert.isEnabled(), alert.isAlertOnPositive(),
                alert.isAllowResendAlert(), alert.getSendAlertEveryNumMilliseconds(),
                alert.getCautionAlertType(), alert.getCautionOperator(), alert.getCautionCombination(), alert.getCautionCombinationCount(), 
                alert.getCautionThreshold(), alert.getCautionWindowDuration(), alert.getCautionMinimumSampleCount(), 
                alert.isCautionAlertActive(), alert.getCautionAlertLastSentTimestamp(), alert.getCautionActiveAlertsSet(),
                alert.getDangerAlertType(), alert.getDangerOperator(), alert.getDangerCombination(), alert.getDangerCombinationCount(),
                alert.getDangerThreshold(), alert.getDangerWindowDuration(), alert.getDangerMinimumSampleCount(), 
                alert.isDangerAlertActive(), alert.getDangerAlertLastSentTimestamp(), alert.getDangerActiveAlertsSet(),
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
            
            Integer notificationGroupId = resultSet.getInt("NOTIFICATION_GROUP_ID");
            if (resultSet.wasNull()) notificationGroupId = null;
            
            Boolean isEnabled = resultSet.getBoolean("IS_ENABLED");
            if (resultSet.wasNull()) isEnabled = null;
            
            Boolean alertOnPositive = resultSet.getBoolean("ALERT_ON_POSITIIVE");
            if (resultSet.wasNull()) alertOnPositive = null;
            
            Boolean allowResendAlert = resultSet.getBoolean("ALLOW_RESEND_ALERT");
            if (resultSet.wasNull()) allowResendAlert = null;
            
            Integer sendAlertEveryNumMilliseconds = resultSet.getInt("SEND_ALERT_EVERY_NUM_MILLISECONDS");
            if (resultSet.wasNull()) sendAlertEveryNumMilliseconds = null;
            
            Integer cautionAlertType = resultSet.getInt("CAUTION_ALERT_TYPE");
            if (resultSet.wasNull()) cautionAlertType = null;
            
            Integer cautionOperator = resultSet.getInt("CAUTION_OPERATOR");
            if (resultSet.wasNull()) cautionOperator = null;
            
            Integer cautionCombination = resultSet.getInt("CAUTION_COMBINATION");
            if (resultSet.wasNull()) cautionCombination = null;
            
            Integer cautionCombinationCount = resultSet.getInt("CAUTION_COMBINATION_COUNT");
            if (resultSet.wasNull()) cautionCombinationCount = null;
            
            BigDecimal cautionThreshold = resultSet.getBigDecimal("CAUTION_THRESHOLD");
            if (resultSet.wasNull()) cautionThreshold = null;

            Integer cautionWindowDuration = resultSet.getInt("CAUTION_WINDOW_DURATION");
            if (resultSet.wasNull()) cautionWindowDuration = null;
            
            Integer cautionMinimumSampleCount = resultSet.getInt("CAUTION_MINIMUM_SAMPLE_COUNT");
            if (resultSet.wasNull()) cautionMinimumSampleCount = null;
            
            Boolean isCautionAlertActive = resultSet.getBoolean("IS_CAUTION_ALERT_ACTIVE");
            if (resultSet.wasNull()) isCautionAlertActive = null;
            
            Timestamp cautionAlertLastSentTimestamp = resultSet.getTimestamp("CAUTION_ALERT_LAST_SENT_TIMESTAMP");
            if (resultSet.wasNull()) cautionAlertLastSentTimestamp = null;
            
            Clob cautionActiveAlertsSet = resultSet.getClob("CAUTION_ACTIVE_ALERTS_SET");
            if (resultSet.wasNull()) cautionActiveAlertsSet = null;
            String cautionActiveAlertsSet_String = DatabaseInterface.getStringFromClob(cautionActiveAlertsSet);

            Integer dangerAlertType = resultSet.getInt("DANGER_ALERT_TYPE");
            if (resultSet.wasNull()) dangerAlertType = null;
            
            Integer dangerOperator = resultSet.getInt("DANGER_OPERATOR");
            if (resultSet.wasNull()) dangerOperator = null;
            
            Integer dangerCombination = resultSet.getInt("DANGER_COMBINATION");
            if (resultSet.wasNull()) dangerCombination = null;
            
            Integer dangerCombinationCount = resultSet.getInt("DANGER_COMBINATION_COUNT");
            if (resultSet.wasNull()) dangerCombinationCount = null;
            
            BigDecimal dangerThreshold = resultSet.getBigDecimal("DANGER_THRESHOLD");
            if (resultSet.wasNull()) dangerThreshold = null;

            Integer dangerWindowDuration = resultSet.getInt("DANGER_WINDOW_DURATION");
            if (resultSet.wasNull()) dangerWindowDuration = null;
            
            Integer dangerMinimumSampleCount = resultSet.getInt("DANGER_MINIMUM_SAMPLE_COUNT");
            if (resultSet.wasNull()) dangerMinimumSampleCount = null;
            
            Boolean isDangerAlertActive = resultSet.getBoolean("IS_DANGER_ALERT_ACTIVE");
            if (resultSet.wasNull()) isDangerAlertActive = null;
            
            Timestamp dangerAlertLastSentTimestamp = resultSet.getTimestamp("DANGER_ALERT_LAST_SENT_TIMESTAMP");
            if (resultSet.wasNull()) dangerAlertLastSentTimestamp = null;
            
            Clob dangerActiveAlertsSet = resultSet.getClob("DANGER_ACTIVE_ALERTS_SET");
            if (resultSet.wasNull()) dangerActiveAlertsSet = null;
            String dangerActiveAlertsSet_String = DatabaseInterface.getStringFromClob(dangerActiveAlertsSet);

            Alert alert = new Alert(id, name, uppercaseName, description, metricGroupId, notificationGroupId, isEnabled, alertOnPositive, 
                    allowResendAlert, sendAlertEveryNumMilliseconds, 
                    cautionAlertType, cautionOperator, cautionCombination, cautionCombinationCount, cautionThreshold, cautionWindowDuration, 
                    cautionMinimumSampleCount, isCautionAlertActive, cautionAlertLastSentTimestamp, cautionActiveAlertsSet_String,
                    dangerAlertType, dangerOperator, dangerCombination, dangerCombinationCount,  dangerThreshold, dangerWindowDuration, 
                    dangerMinimumSampleCount, isDangerAlertActive, dangerAlertLastSentTimestamp, dangerActiveAlertsSet_String);
            
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
    
    public List<String> getAlertNames(String prefixFilter) {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            List<String> alertNames = new ArrayList<>();
            
            databaseInterface_.createPreparedStatement(AlertsSql.Select_Alert_Names, 1000);
            databaseInterface_.addPreparedStatementParameters(prefixFilter + "%");
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            while (resultSet.next()) {
                String name = resultSet.getString("NAME");
                if (resultSet.wasNull()) name = null;
                
                if (name != null) alertNames.add(name);
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
    
    public Set<Integer> getDistinctNotificationGroupIds() {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(AlertsSql.Select_DistinctNotificationGroupIds, 100);
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
    
}
