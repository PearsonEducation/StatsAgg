package com.pearson.statsagg.database_objects.notification_group_templates;

import java.util.ArrayList;
import java.util.List;
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
public class NotificationGroupTemplatesDao {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplatesDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroupTemplate notificationGroupTemplate) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    NotificationGroupTemplatesSql.Insert_NotificationGroupTemplate, 
                    notificationGroupTemplate.getName(), notificationGroupTemplate.getUppercaseName(), notificationGroupTemplate.getVariableSetListId(),
                    notificationGroupTemplate.getNotificationGroupNameVariable(), notificationGroupTemplate.getEmailAddressesVariable(), 
                    notificationGroupTemplate.getPagerdutyServiceNameVariable(), notificationGroupTemplate.isMarkedForDelete());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroupTemplate notificationGroupTemplate) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    NotificationGroupTemplatesSql.Update_NotificationGroupTemplate_ByPrimaryKey, 
                    notificationGroupTemplate.getName(), notificationGroupTemplate.getUppercaseName(), notificationGroupTemplate.getVariableSetListId(), 
                    notificationGroupTemplate.getNotificationGroupNameVariable(), notificationGroupTemplate.getEmailAddressesVariable(),
                    notificationGroupTemplate.getPagerdutyServiceNameVariable(), notificationGroupTemplate.isMarkedForDelete(), 
                    notificationGroupTemplate.getId());
            
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
    
    public static boolean update_Name(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, 
            int notificationGroupTemplateId, String newNotificationGroupTemplateName) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    NotificationGroupTemplatesSql.Update_NotificationGroupTemplate_Name, 
                    newNotificationGroupTemplateName, newNotificationGroupTemplateName.toUpperCase(), 
                    notificationGroupTemplateId);
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroupTemplate notificationGroupTemplate) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            NotificationGroupTemplate notificationGroupTemplateFromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroupTemplate.getId());

            boolean upsertSuccess = true;
            if (notificationGroupTemplateFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, notificationGroupTemplate);
            else if (!notificationGroupTemplateFromDb.isEqual(notificationGroupTemplate)) upsertSuccess = update(connection, false, commitOnCompletion, notificationGroupTemplate);

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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, 
            NotificationGroupTemplate notificationGroupTemplate, String oldNotificationGroupTemplateName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            NotificationGroupTemplate notificationGroupTemplateFromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, oldNotificationGroupTemplateName);

            boolean upsertSuccess = true;
            if (notificationGroupTemplateFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, notificationGroupTemplate);
            }
            else {
                notificationGroupTemplate.setId(notificationGroupTemplateFromDb.getId());
                if (!notificationGroupTemplateFromDb.isEqual(notificationGroupTemplate)) upsertSuccess = update(connection, false, commitOnCompletion, notificationGroupTemplate);
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
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroupTemplate notificationGroupTemplate) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    NotificationGroupTemplatesSql.Delete_NotificationGroupTemplate_ByPrimaryKey, 
                    notificationGroupTemplate.getId());
            
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

    public static NotificationGroupTemplate getNotificationGroupTemplate(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<NotificationGroupTemplate> notificationGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupTemplatesResultSetHandler(), 
                    NotificationGroupTemplatesSql.Select_NotificationGroupTemplate_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(notificationGroupTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static NotificationGroupTemplate getNotificationGroupTemplate(Connection connection, boolean closeConnectionOnCompletion, String notificationGroupTemplateName) {
        
        try {
            List<NotificationGroupTemplate> notificationGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupTemplatesResultSetHandler(), 
                    NotificationGroupTemplatesSql.Select_NotificationGroupTemplate_ByName, notificationGroupTemplateName);
            
            return DatabaseUtils.getSingleResultFromList(notificationGroupTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static NotificationGroupTemplate getNotificationGroupTemplate_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, 
            String notificationGroupTemplateName) {
        
        try {
            List<NotificationGroupTemplate> notificationGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupTemplatesResultSetHandler(), 
                    NotificationGroupTemplatesSql.Select_NotificationGroupTemplate_ByUppercaseName, notificationGroupTemplateName.toUpperCase());
            
            return DatabaseUtils.getSingleResultFromList(notificationGroupTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<NotificationGroupTemplate> getNotificationGroupTemplateIdsAndNames(Connection connection, boolean closeConnectionOnCompletion) {

        try {
            List<NotificationGroupTemplate> notificationGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupTemplatesResultSetHandler(), 
                    NotificationGroupTemplatesSql.Select_AllNotificationGroupTemplates_IdsAndNames);
            
            return notificationGroupTemplates;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<NotificationGroupTemplate> getNotificationGroupTemplates(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<NotificationGroupTemplate> notificationGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupTemplatesResultSetHandler(), 
                    NotificationGroupTemplatesSql.Select_AllNotificationGroupTemplates);
            
            return notificationGroupTemplates;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getNotificationGroupTemplateNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> notificationGroupTemplateNames = new ArrayList<>();
            
            List<NotificationGroupTemplate> notificationGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupTemplatesResultSetHandler(), 
                    (resultSetLimit + 5), 
                    NotificationGroupTemplatesSql.Select_NotificationGroupTemplate_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((notificationGroupTemplates == null) || notificationGroupTemplates.isEmpty()) return notificationGroupTemplateNames;
            
            int rowCounter = 0;
            for (NotificationGroupTemplate notificationGroupTemplate : notificationGroupTemplates) {
                if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getName() != null)) {
                    notificationGroupTemplateNames.add(notificationGroupTemplate.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return notificationGroupTemplateNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<Integer, NotificationGroupTemplate> getNotificationGroupTemplates_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<Integer, NotificationGroupTemplate> notificationGroupTemplatesById = new HashMap<>();

            List<NotificationGroupTemplate> notificationGroupTemplates = getNotificationGroupTemplates(connection, closeConnectionOnCompletion);
            if (notificationGroupTemplates == null) return null;

            for (NotificationGroupTemplate notificationGroupTemplate : notificationGroupTemplates) {
                if (notificationGroupTemplate.getId() != null) {
                    notificationGroupTemplatesById.put(notificationGroupTemplate.getId(), notificationGroupTemplate);
                }
            }
            
            return notificationGroupTemplatesById;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }

    }
        
    public static Map<Integer, String> getNotificationGroupTemplateNames_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<Integer, String> notificationGroupTemplateNamesById = new HashMap<>();

            List<NotificationGroupTemplate> notificationGroupTemplates = getNotificationGroupTemplateIdsAndNames(connection, closeConnectionOnCompletion);
            if (notificationGroupTemplates == null) return null;

            for (NotificationGroupTemplate notificationGroupTemplate : notificationGroupTemplates) {
                if ((notificationGroupTemplate.getName() != null) && (notificationGroupTemplate.getId() != null)) {
                    notificationGroupTemplateNamesById.put(notificationGroupTemplate.getId(), notificationGroupTemplate.getName());
                }
            }
            
            return notificationGroupTemplateNamesById;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }

    }

    public static Map<String,NotificationGroupTemplate> getNotificationGroupTemplates_ByName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<String,NotificationGroupTemplate> notificationGroupTemplates_ByName = new HashMap<>();
            
            List<NotificationGroupTemplate> notificationGroupTemplates = getNotificationGroupTemplates(connection, false);
            if (notificationGroupTemplates == null) return null;

            for (NotificationGroupTemplate notificationGroupTemplate : notificationGroupTemplates) {
                if ((notificationGroupTemplate == null) || (notificationGroupTemplate.getName() == null)) continue;
                notificationGroupTemplates_ByName.put(notificationGroupTemplate.getName(), notificationGroupTemplate);
            }

            return notificationGroupTemplates_ByName;
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
