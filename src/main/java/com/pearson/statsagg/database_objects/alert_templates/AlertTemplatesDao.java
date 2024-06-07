package com.pearson.statsagg.database_objects.alert_templates;

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
public class AlertTemplatesDao {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertTemplatesDao.class.getName());
  
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, AlertTemplate alertTemplate) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    AlertTemplatesSql.Insert_AlertTemplate, 
                    alertTemplate.getName(), alertTemplate.getUppercaseName(), alertTemplate.getVariableSetListId(), alertTemplate.getDescriptionVariable(), alertTemplate.getAlertNameVariable(), alertTemplate.getMetricGroupNameVariable(), 
                    alertTemplate.isEnabled(), alertTemplate.isCautionEnabled(), alertTemplate.isDangerEnabled(), alertTemplate.getAlertType(), alertTemplate.isAlertOnPositive(),
                    alertTemplate.isAllowResendAlert(), alertTemplate.getResendAlertEvery(), alertTemplate.getResendAlertEveryTimeUnit(), 
                    alertTemplate.getCautionNotificationGroupNameVariable(), alertTemplate.getCautionPositiveNotificationGroupNameVariable(), alertTemplate.getCautionOperator(), alertTemplate.getCautionCombination(),   
                    alertTemplate.getCautionCombinationCount(), alertTemplate.getCautionThreshold(), alertTemplate.getCautionWindowDuration(), alertTemplate.getCautionWindowDurationTimeUnit(),   
                    alertTemplate.getCautionStopTrackingAfter(), alertTemplate.getCautionStopTrackingAfterTimeUnit(), alertTemplate.getCautionMinimumSampleCount(), 
                    alertTemplate.getDangerNotificationGroupNameVariable(), alertTemplate.getDangerPositiveNotificationGroupNameVariable(), alertTemplate.getDangerOperator(), alertTemplate.getDangerCombination(),   
                    alertTemplate.getDangerCombinationCount(), alertTemplate.getDangerThreshold(), alertTemplate.getDangerWindowDuration(), alertTemplate.getDangerWindowDurationTimeUnit(), 
                    alertTemplate.getDangerStopTrackingAfter(), alertTemplate.getDangerStopTrackingAfterTimeUnit(), alertTemplate.getDangerMinimumSampleCount());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, AlertTemplate alertTemplate) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    AlertTemplatesSql.Update_AlertTemplate_ByPrimaryKey, 
                    alertTemplate.getName(), alertTemplate.getUppercaseName(), alertTemplate.getVariableSetListId(), alertTemplate.getDescriptionVariable(), alertTemplate.getAlertNameVariable(), alertTemplate.getMetricGroupNameVariable(), 
                    alertTemplate.isEnabled(), alertTemplate.isCautionEnabled(), alertTemplate.isDangerEnabled(), alertTemplate.getAlertType(), alertTemplate.isAlertOnPositive(),
                    alertTemplate.isAllowResendAlert(), alertTemplate.getResendAlertEvery(), alertTemplate.getResendAlertEveryTimeUnit(), 
                    alertTemplate.getCautionNotificationGroupNameVariable(), alertTemplate.getCautionPositiveNotificationGroupNameVariable(), alertTemplate.getCautionOperator(), alertTemplate.getCautionCombination(),   
                    alertTemplate.getCautionCombinationCount(), alertTemplate.getCautionThreshold(), alertTemplate.getCautionWindowDuration(), alertTemplate.getCautionWindowDurationTimeUnit(),   
                    alertTemplate.getCautionStopTrackingAfter(), alertTemplate.getCautionStopTrackingAfterTimeUnit(), alertTemplate.getCautionMinimumSampleCount(), 
                    alertTemplate.getDangerNotificationGroupNameVariable(), alertTemplate.getDangerPositiveNotificationGroupNameVariable(), alertTemplate.getDangerOperator(), alertTemplate.getDangerCombination(),   
                    alertTemplate.getDangerCombinationCount(), alertTemplate.getDangerThreshold(), alertTemplate.getDangerWindowDuration(), alertTemplate.getDangerWindowDurationTimeUnit(), 
                    alertTemplate.getDangerStopTrackingAfter(), alertTemplate.getDangerStopTrackingAfterTimeUnit(), alertTemplate.getDangerMinimumSampleCount(), 
                    alertTemplate.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, AlertTemplate alertTemplate) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            AlertTemplate alertTemplateFromDb = AlertTemplatesDao.getAlertTemplate(connection, false, alertTemplate.getId());

            boolean upsertSuccess = true;
            if (alertTemplateFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, alertTemplate);
            else if (!alertTemplateFromDb.isEqual(alertTemplate)) upsertSuccess = update(connection, false, commitOnCompletion, alertTemplate);

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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, AlertTemplate alertTemplate, String oldAlertTemplateName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            AlertTemplate alertTemplateFromDb = AlertTemplatesDao.getAlertTemplate(connection, false, oldAlertTemplateName);

            boolean upsertSuccess = true;
            if (alertTemplateFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, alertTemplate);
            }
            else {
                alertTemplate.setId(alertTemplateFromDb.getId());
                if (!alertTemplateFromDb.isEqual(alertTemplate)) upsertSuccess = update(connection, false, commitOnCompletion, alertTemplate);
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
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, AlertTemplate alertTemplate) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    AlertTemplatesSql.Delete_AlertTemplate_ByPrimaryKey, 
                    alertTemplate.getId());
            
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

    public static AlertTemplate getAlertTemplate(Connection connection, boolean closeConnectionOnCompletion, Integer alertTemplateId) {
        
        try {
            List<AlertTemplate> alertTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertTemplatesResultSetHandler(), 
                    AlertTemplatesSql.Select_AlertTemplate_ByPrimaryKey, alertTemplateId);
            
            return DatabaseUtils.getSingleResultFromList(alertTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static List<AlertTemplate> getAlertTemplates(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<AlertTemplate> alertTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                        new AlertTemplatesResultSetHandler(), 
                        AlertTemplatesSql.Select_AllAlertTemplates);
            
            return alertTemplates;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
        
    public static AlertTemplate getAlertTemplate(Connection connection, boolean closeConnectionOnCompletion, String alertTemplateName) {
        
        try {
            List<AlertTemplate> alertTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertTemplatesResultSetHandler(), 
                    AlertTemplatesSql.Select_AlertTemplate_ByName, alertTemplateName);
            
            return DatabaseUtils.getSingleResultFromList(alertTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static AlertTemplate getAlertTemplate_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, String alertTemplateName) {
        
        try {
            List<AlertTemplate> alertTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertTemplatesResultSetHandler(), 
                    AlertTemplatesSql.Select_AlertTemplate_ByUppercaseName, alertTemplateName.toUpperCase());
            
            return DatabaseUtils.getSingleResultFromList(alertTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Set<String> getAlertTemplateNames(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<AlertTemplate> alertTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                        new AlertTemplatesResultSetHandler(), 
                        AlertTemplatesSql.Select_AlertTemplateNames);
            
            Set<String> alertTemplateNames = new HashSet<>();
            if (alertTemplates == null) return alertTemplateNames;
            
            for (AlertTemplate alertTemplate : alertTemplates) {
                if ((alertTemplate != null) && (alertTemplate.getName() != null)) alertTemplateNames.add(alertTemplate.getName());
            }
            
            return alertTemplateNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,AlertTemplate> getAlertTemplates_ByName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<String,AlertTemplate> alertTemplates_ByName = new HashMap<>();
            
            List<AlertTemplate> alertTemplates = getAlertTemplates(connection, false);
            
            if (alertTemplates != null) {
                for (AlertTemplate alertTemplate : alertTemplates) {
                    if ((alertTemplate == null) || (alertTemplate.getName() == null)) continue;
                    alertTemplates_ByName.put(alertTemplate.getName(), alertTemplate);
                }
            }

            return alertTemplates_ByName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<Integer,AlertTemplate> getAlertTemplates_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<Integer,AlertTemplate> alertTemplates_ById = new HashMap<>();
            
            List<AlertTemplate> alertTemplates = getAlertTemplates(connection, false);
            if (alertTemplates == null) return null;
            
            for (AlertTemplate alertTemplate : alertTemplates) {
                if ((alertTemplate == null) || (alertTemplate.getId() == null)) continue;
                alertTemplates_ById.put(alertTemplate.getId(), alertTemplate);
            }

            return alertTemplates_ById;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getAlertTemplateNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> alertTemplateNames = new ArrayList<>();
            
            List<AlertTemplate> alertTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new AlertTemplatesResultSetHandler(), 
                    (resultSetLimit + 5), 
                    AlertTemplatesSql.Select_AlertTemplate_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((alertTemplates == null) || alertTemplates.isEmpty()) return alertTemplateNames;
            
            int rowCounter = 0;
            for (AlertTemplate alertTemplate : alertTemplates) {
                if ((alertTemplate != null) && (alertTemplate.getName() != null)) {
                    alertTemplateNames.add(alertTemplate.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return alertTemplateNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

}
