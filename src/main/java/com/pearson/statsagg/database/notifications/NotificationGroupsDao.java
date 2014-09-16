package com.pearson.statsagg.database.notifications;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.database.DatabaseObjectDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupsDao extends DatabaseObjectDao<NotificationGroup> {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsDao.class.getName());
   
    private final String tableName_ = "NOTIFICATION_GROUPS";
    
    public NotificationGroupsDao(){}
            
    public NotificationGroupsDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public boolean dropTable() {
        return dropTable(NotificationGroupsSql.DropTable_NotificationGroups);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        databaseCreationSqlStatements.add(NotificationGroupsSql.CreateTable_NotificationGroups);
        databaseCreationSqlStatements.add(NotificationGroupsSql.CreateIndex_NotificationGroups_PrimaryKey);
        databaseCreationSqlStatements.add(NotificationGroupsSql.CreateIndex_NotificationGroups_Unique_Name);
        
        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public NotificationGroup getDatabaseObject(NotificationGroup notificationGroup) {
        if (notificationGroup == null) return null;
        
        return getDatabaseObject(NotificationGroupsSql.Select_NotificationGroup_ByPrimaryKey, 
                notificationGroup.getId()); 
    }
    
    @Override
    public boolean insert(NotificationGroup notificationGroup) {
        if (notificationGroup == null) return false;
        
        return insert(NotificationGroupsSql.Insert_NotificationGroup, 
                notificationGroup.getName(), notificationGroup.getUppercaseName(), notificationGroup.getEmailAddresses());
    }
    
    @Override
    public boolean update(NotificationGroup notificationGroup) {
        if (notificationGroup == null) return false;
        
        return update(NotificationGroupsSql.Update_NotificationGroup_ByPrimaryKey, 
                notificationGroup.getName(), notificationGroup.getUppercaseName(), notificationGroup.getEmailAddresses(), notificationGroup.getId());
    }

    @Override
    public boolean delete(NotificationGroup notificationGroup) {
        if (notificationGroup == null) return false;

        return delete(NotificationGroupsSql.Delete_NotificationGroup_ByPrimaryKey, 
                notificationGroup.getId()); 
    }
    
    @Override
    public NotificationGroup processSingleResultAllColumns(ResultSet resultSet) {
        
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
            
            String emailAddresses = resultSet.getString("EMAIL_ADDRESSES");
            if (resultSet.wasNull()) emailAddresses = null;

            NotificationGroup notificationGroup = new NotificationGroup(id, name, uppercaseName, emailAddresses);
            
            return notificationGroup;
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
    
    public NotificationGroup getNotificationGroup(int id) {
        return getDatabaseObject(NotificationGroupsSql.Select_NotificationGroup_ByPrimaryKey, id); 
    }  
    
    public NotificationGroup getNotificationGroupByName(String name) {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(NotificationGroupsSql.Select_NotificationGroup_ByName, 1);
            databaseInterface_.addPreparedStatementParameters(name);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            if (resultSet.next()) {
                NotificationGroup notificationGroup = processSingleResultAllColumns(resultSet);
                return notificationGroup;
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
    
    public List<Integer> getAllNotificationGroupIds() {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            databaseInterface_.createPreparedStatement(NotificationGroupsSql.Select_DistinctNotificationGroupIds, 1000);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            List<Integer> metricGroupIds = new ArrayList<>();
            
            ResultSet resultSet = databaseInterface_.getResults();
            
            while (resultSet.next()) {
                Integer id = resultSet.getInt("ID");
                metricGroupIds.add(id);
            }
            
            return metricGroupIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public Map<Integer,String> getNotificationGroupNames_ById() {
        
        try {

            if (!isConnectionValid()) {
                return new HashMap<>();
            }

            Map<Integer,String> notificationGroupNames_ById = new HashMap<>();
            
            databaseInterface_.createPreparedStatement(NotificationGroupsSql.Select_AllNotificationGroup_IdsAndNames, 1000);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new HashMap<>();
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            while (resultSet.next()) {
                Integer id = resultSet.getInt("ID");
                if (resultSet.wasNull()) id = null;
                
                String name = resultSet.getString("NAME");
                if (resultSet.wasNull()) name = null;

                if ((id != null) && (name != null)) notificationGroupNames_ById.put(id, name);
            }
            
            return notificationGroupNames_ById;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new HashMap<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public List<String> getNotificationGroupNames(String prefixFilter) {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            List<String> notificationGroupNames = new ArrayList<>();
            
            databaseInterface_.createPreparedStatement(NotificationGroupsSql.Select_NotificationGroup_Names, 1000);
            databaseInterface_.addPreparedStatementParameters(prefixFilter + "%");
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            while (resultSet.next()) {
                String name = resultSet.getString("NAME");
                if (resultSet.wasNull()) name = null;
                
                if (name != null) notificationGroupNames.add(name);
            }
            
            return notificationGroupNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
}
