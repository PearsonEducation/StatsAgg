package com.pearson.statsagg.database_objects.alerts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertsDao {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsDao.class.getName());
  
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Alert alert) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    AlertsSql.Insert_Alert, 
                    alert.getName(), alert.getUppercaseName(), alert.getDescription(), alert.getAlertTemplateId(), alert.getVariableSetId(), alert.getMetricGroupId(), 
                    alert.isEnabled(), alert.isCautionEnabled(), alert.isDangerEnabled(), alert.getAlertType(), alert.isAlertOnPositive(),
                    alert.isAllowResendAlert(), alert.getResendAlertEvery(), alert.getResendAlertEveryTimeUnit(), 
                    alert.getCautionNotificationGroupId(), alert.getCautionPositiveNotificationGroupId(), alert.getCautionOperator(), alert.getCautionCombination(),   
                    alert.getCautionCombinationCount(), alert.getCautionThreshold(), alert.getCautionWindowDuration(), alert.getCautionWindowDurationTimeUnit(),   
                    alert.getCautionStopTrackingAfter(), alert.getCautionStopTrackingAfterTimeUnit(), alert.getCautionMinimumSampleCount(), alert.isCautionAlertActive(),  
                    alert.getCautionAlertLastSentTimestamp(), alert.isCautionAlertAcknowledged(),alert.getCautionActiveAlertsSet(), alert.getCautionFirstActiveAt(),
                    alert.getDangerNotificationGroupId(), alert.getDangerPositiveNotificationGroupId(), alert.getDangerOperator(), alert.getDangerCombination(),   
                    alert.getDangerCombinationCount(), alert.getDangerThreshold(), alert.getDangerWindowDuration(), alert.getDangerWindowDurationTimeUnit(), 
                    alert.getDangerStopTrackingAfter(), alert.getDangerStopTrackingAfterTimeUnit(), alert.getDangerMinimumSampleCount(), alert.isDangerAlertActive(), 
                    alert.getDangerAlertLastSentTimestamp(), alert.isDangerAlertAcknowledged(), alert.getDangerActiveAlertsSet(), alert.getDangerFirstActiveAt());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Alert alert) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    AlertsSql.Update_Alert_ByPrimaryKey, 
                    alert.getName(), alert.getUppercaseName(), alert.getDescription(), alert.getAlertTemplateId(), alert.getVariableSetId(), alert.getMetricGroupId(), 
                    alert.isEnabled(), alert.isCautionEnabled(), alert.isDangerEnabled(), alert.getAlertType(), alert.isAlertOnPositive(),
                    alert.isAllowResendAlert(), alert.getResendAlertEvery(), alert.getResendAlertEveryTimeUnit(), 
                    alert.getCautionNotificationGroupId(), alert.getCautionPositiveNotificationGroupId(), alert.getCautionOperator(), alert.getCautionCombination(),   
                    alert.getCautionCombinationCount(), alert.getCautionThreshold(), alert.getCautionWindowDuration(), alert.getCautionWindowDurationTimeUnit(),   
                    alert.getCautionStopTrackingAfter(), alert.getCautionStopTrackingAfterTimeUnit(), alert.getCautionMinimumSampleCount(), alert.isCautionAlertActive(),  
                    alert.getCautionAlertLastSentTimestamp(), alert.isCautionAlertAcknowledged(),alert.getCautionActiveAlertsSet(), alert.getCautionFirstActiveAt(),
                    alert.getDangerNotificationGroupId(), alert.getDangerPositiveNotificationGroupId(), alert.getDangerOperator(), alert.getDangerCombination(),   
                    alert.getDangerCombinationCount(), alert.getDangerThreshold(), alert.getDangerWindowDuration(), alert.getDangerWindowDurationTimeUnit(), 
                    alert.getDangerStopTrackingAfter(), alert.getDangerStopTrackingAfterTimeUnit(), alert.getDangerMinimumSampleCount(), alert.isDangerAlertActive(), 
                    alert.getDangerAlertLastSentTimestamp(), alert.isDangerAlertAcknowledged(), alert.getDangerActiveAlertsSet(), alert.getDangerFirstActiveAt(), 
                    alert.getId());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean update_Name(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, int alertId, String newAlertName) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    AlertsSql.Update_Alert_Name, 
                    newAlertName, newAlertName.toUpperCase(), 
                    alertId);
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Alert alert) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            Alert alertFromDb = AlertsDao.getAlert(connection, false, alert.getId());

            boolean upsertSuccess = true;
            if (alertFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, alert);
            else if (!alertFromDb.isEqual(alert)) upsertSuccess = update(connection, false, commitOnCompletion, alert);

            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
            
            return upsertSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }

    }
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Alert alert, String oldAlertName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            Alert alertFromDb = AlertsDao.getAlert(connection, false, oldAlertName);

            boolean upsertSuccess = true;
            if (alertFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, alert);
            }
            else {
                alert.setId(alertFromDb.getId());
                if (!alertFromDb.isEqual(alert)) upsertSuccess = update(connection, false, commitOnCompletion, alert);
            }

            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
            
            return upsertSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }

    }
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Alert alert) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    AlertsSql.Delete_Alert_ByPrimaryKey, 
                    alert.getId());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static Alert getAlert(Connection connection, boolean closeConnectionOnCompletion, Integer alertId) {
        
        try {
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    AlertsSql.Select_Alert_ByPrimaryKey, alertId);
            
            return DatabaseUtils.getSingleResultFromList(alerts);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static List<Alert> getAlerts(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                        new AlertsResultSetHandler(), 
                        AlertsSql.Select_AllAlerts);
            
            return alerts;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
        
    public static Alert getAlert(Connection connection, boolean closeConnectionOnCompletion, String alertName) {
        
        try {
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    AlertsSql.Select_Alert_ByName, alertName);
            
            return DatabaseUtils.getSingleResultFromList(alerts);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Alert getAlert_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, String alertName) {
        
        try {
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    AlertsSql.Select_Alert_ByUppercaseName, alertName.toUpperCase());
            
            return DatabaseUtils.getSingleResultFromList(alerts);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Alert> getAlerts_FilterByAlertTemplateId(Connection connection, boolean closeConnectionOnCompletion, Integer alertTemplateId) {
        
        try {
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    AlertsSql.Select_Alert_ByAlertTemplateId, 
                    alertTemplateId);
            
            return alerts;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,Alert> getAlerts_FilterByAlertTemplateId_ByName(Connection connection, boolean closeConnectionOnCompletion, Integer alertTemplateId) {
        
        try {
            List<Alert> alerts = getAlerts_FilterByAlertTemplateId(connection, false, alertTemplateId);
            if (alerts == null) return null;
            
            Map<String,Alert> alerts_ByName = new HashMap<>();

            for (Alert alert : alerts) {
                if ((alert == null) || (alert.getName() == null)) continue;
                alerts_ByName.put(alert.getName(), alert);
            }

            return alerts_ByName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Set<String> getAlertNames(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                        new AlertsResultSetHandler(), 
                        AlertsSql.Select_AlertNames);
            
            Set<String> alertNames = new HashSet<>();
            if (alerts == null) return alertNames;
            
            for (Alert alert : alerts) {
                if ((alert != null) && (alert.getName() != null)) alertNames.add(alert.getName());
            }
            
            return alertNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,Alert> getAlerts_ByName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<String,Alert> alerts_ByName = new HashMap<>();
            
            List<Alert> alerts = getAlerts(connection, false);
            
            if (alerts != null) {
                for (Alert alert : alerts) {
                    if ((alert == null) || (alert.getName() == null)) continue;
                    alerts_ByName.put(alert.getName(), alert);
                }
            }

            return alerts_ByName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,Alert> getAlerts_ByUppercaseName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<Alert> alerts = getAlerts(connection, false);
            if (alerts == null) return null;
            
            Map<String,Alert> alerts_ByUppercaseName = new HashMap<>();

            for (Alert alert : alerts) {
                if ((alert == null) || (alert.getName() == null)) continue;
                alerts_ByUppercaseName.put(alert.getName().toUpperCase(), alert);
            }

            return alerts_ByUppercaseName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getAlertNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> alertNames = new ArrayList<>();
            
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    (resultSetLimit + 5), 
                    AlertsSql.Select_Alert_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((alerts == null) || alerts.isEmpty()) return alertNames;
            
            int rowCounter = 0;
            for (Alert alert : alerts) {
                if ((alert != null) && (alert.getName() != null)) {
                    alertNames.add(alert.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return alertNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
      
    public static List<String> getAlertNamesAssociatedWithMetricGroupId(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupId) {
        
        try {            
            List<String> alertNames = new ArrayList<>();
            
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    AlertsSql.Select_AlertNamesAssociatedWithMetricGroupId,
                    metricGroupId);
            
            if (alerts == null) return null;
            
            for (Alert alert : alerts) {
                if ((alert != null) && (alert.getName() != null)) {
                    alertNames.add(alert.getName());
                }
            }
            
            return alertNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Set<Integer> getDistinctMetricGroupIdsAssociatedWithAlerts(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Set<Integer> metricGroupIds = new HashSet<>();
            
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    AlertsSql.Select_DistinctMetricGroupIds);
            
            if (alerts == null) return null;
            
            for (Alert alert : alerts) {
                if ((alert != null) && (alert.getMetricGroupId() != null)) {
                    metricGroupIds.add(alert.getMetricGroupId());
                }
            }
            
            return metricGroupIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Set<Integer> getDistinctNotificationGroupIdsAssociatedWithAlerts(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Set<Integer> notificationGroupIds = new HashSet<>();
            
            List<Alert> alerts = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertsResultSetHandler(), 
                    AlertsSql.Select_AllDistinctNotificationGroupIds);
            
            if (alerts == null) return null;
            
            for (Alert alert : alerts) {
                if ((alert != null) && (alert.getCautionNotificationGroupId() != null)) {
                    notificationGroupIds.add(alert.getCautionNotificationGroupId());
                }
            }
            
            return notificationGroupIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean isAlertCreatedByAlertTemplate(Connection connection, boolean closeConnectionOnCompletion, Alert alert, String oldAlertName) {
        
        try {
            Alert alertFromDb;
            
            if (oldAlertName != null) {
                alertFromDb = AlertsDao.getAlert(connection, closeConnectionOnCompletion, oldAlertName);
            }
            else {
                if ((alert == null) || (alert.getName() == null)) return false;
                alertFromDb = AlertsDao.getAlert(connection, closeConnectionOnCompletion, alert.getName());
            }

            if ((alertFromDb != null) && (alertFromDb.getAlertTemplateId() != null)) return true;
        }
        catch (Exception e){
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }    
        
        return false;
    }

}
