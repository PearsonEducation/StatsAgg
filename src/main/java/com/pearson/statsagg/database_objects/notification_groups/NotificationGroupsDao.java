package com.pearson.statsagg.database_objects.notification_groups;

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
public class NotificationGroupsDao {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroup notificationGroup) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    NotificationGroupsSql.Insert_NotificationGroup, 
                    notificationGroup.getName(), notificationGroup.getUppercaseName(), 
                    notificationGroup.getEmailAddresses(), notificationGroup.getPagerdutyServiceId());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroup notificationGroup) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    NotificationGroupsSql.Update_NotificationGroup_ByPrimaryKey, 
                    notificationGroup.getName(), notificationGroup.getUppercaseName(), 
                    notificationGroup.getEmailAddresses(), notificationGroup.getPagerdutyServiceId(), 
                    notificationGroup.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroup notificationGroup) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            NotificationGroup notificationGroupFromDb = NotificationGroupsDao.getNotificationGroup(connection, false, notificationGroup.getId());

            boolean upsertSuccess = true;
            if (notificationGroupFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, notificationGroup);
            else if (!notificationGroupFromDb.isEqual(notificationGroup)) upsertSuccess = update(connection, false, commitOnCompletion, notificationGroup);

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

    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, NotificationGroup notificationGroup) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    NotificationGroupsSql.Delete_NotificationGroup_ByPrimaryKey, 
                    notificationGroup.getId());
            
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

    public static NotificationGroup getNotificationGroup(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<NotificationGroup> notificationGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupsResultSetHandler(), 
                    NotificationGroupsSql.Select_NotificationGroup_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(notificationGroups);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static NotificationGroup getNotificationGroup(Connection connection, boolean closeConnectionOnCompletion, String notificationGroupName) {
        
        try {
            List<NotificationGroup> notificationGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupsResultSetHandler(), 
                    NotificationGroupsSql.Select_NotificationGroup_ByName, notificationGroupName);
            
            return DatabaseUtils.getSingleResultFromList(notificationGroups);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<NotificationGroup> getNotificationGroupIdsAndNames(Connection connection, boolean closeConnectionOnCompletion) {

        try {
            List<NotificationGroup> notificationGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupsResultSetHandler(), 
                    NotificationGroupsSql.Select_AllNotificationGroups_IdsAndNames);
            
            return notificationGroups;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<NotificationGroup> getNotificationGroups(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<NotificationGroup> notificationGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupsResultSetHandler(), 
                    NotificationGroupsSql.Select_AllNotificationGroups);
            
            return notificationGroups;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getNotificationGroupNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> notificationGroupNames = new ArrayList<>();
            
            List<NotificationGroup> notificationGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new NotificationGroupsResultSetHandler(), 
                    (resultSetLimit + 5), 
                    NotificationGroupsSql.Select_NotificationGroup_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((notificationGroups == null) || notificationGroups.isEmpty()) return notificationGroupNames;
            
            int rowCounter = 0;
            for (NotificationGroup notificationGroup : notificationGroups) {
                if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
                    notificationGroupNames.add(notificationGroup.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return notificationGroupNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static Map<Integer, NotificationGroup> getNotificationGroups_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        Map<Integer, NotificationGroup> notificationGroupsById = new HashMap<>();
        
        try {
            List<NotificationGroup> notificationGroups = getNotificationGroups(connection, closeConnectionOnCompletion);
            if (notificationGroups == null) return notificationGroupsById;

            for (NotificationGroup notificationGroup : notificationGroups) {
                if (notificationGroup.getId() != null) notificationGroupsById.put(notificationGroup.getId(), notificationGroup);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return notificationGroupsById;
    }
        
    public static Map<Integer, String> getNotificationGroupNames_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        Map<Integer, String> notificationGroupNamesById = new HashMap<>();
        
        try {
            List<NotificationGroup> notificationGroups = getNotificationGroupIdsAndNames(connection, closeConnectionOnCompletion);
            if (notificationGroups == null) return notificationGroupNamesById;

            for (NotificationGroup notificationGroup : notificationGroups) {
                if (notificationGroup.getName() != null) notificationGroupNamesById.put(notificationGroup.getId(), notificationGroup.getName());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return notificationGroupNamesById;
    }

}
