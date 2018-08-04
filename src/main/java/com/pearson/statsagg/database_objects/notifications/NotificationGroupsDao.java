package com.pearson.statsagg.database_objects.notifications;

import com.pearson.statsagg.database_engine.DatabaseInterface;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.database_engine.DatabaseObjectDao;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
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
    
    public NotificationGroupsDao(DatabaseInterface databaseInterface) {
        super(databaseInterface);
    }
    
    public boolean dropTable() {
        return dropTable(NotificationGroupsSql.DropTable_NotificationGroups);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(NotificationGroupsSql.CreateTable_NotificationGroups_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(NotificationGroupsSql.CreateTable_NotificationGroups_Derby);
            databaseCreationSqlStatements.add(NotificationGroupsSql.CreateIndex_NotificationGroups_PrimaryKey);
        }

        databaseCreationSqlStatements.add(NotificationGroupsSql.CreateIndex_NotificationGroups_Unique_Name);
        databaseCreationSqlStatements.add(NotificationGroupsSql.CreateIndex_NotificationGroups_Unique_UppercaseName);

        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public NotificationGroup getDatabaseObject(NotificationGroup notificationGroup) {
        if (notificationGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return null;
        }
        
        return getDatabaseObject(NotificationGroupsSql.Select_NotificationGroup_ByPrimaryKey, 
                notificationGroup.getId()); 
    }
    
    @Override
    public boolean insert(NotificationGroup notificationGroup) {
        if (notificationGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        return insert(NotificationGroupsSql.Insert_NotificationGroup, 
                notificationGroup.getName(), notificationGroup.getUppercaseName(), notificationGroup.getEmailAddresses());
    }
    
    @Override
    public boolean update(NotificationGroup notificationGroup) {
        if (notificationGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        return update(NotificationGroupsSql.Update_NotificationGroup_ByPrimaryKey, 
                notificationGroup.getName(), notificationGroup.getUppercaseName(), notificationGroup.getEmailAddresses(), notificationGroup.getId());
    }

    @Override
    public boolean delete(NotificationGroup notificationGroup) {
        if (notificationGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }

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
    
    public NotificationGroup getNotificationGroup(Integer id) {
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
    
    public List<String> getNotificationGroupNames(String filter, int resultSetLimit) {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            List<String> notificationGroupNames = new ArrayList<>();
            
            databaseInterface_.createPreparedStatement(NotificationGroupsSql.Select_NotificationGroup_Names_OrderByName, 1000);
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
                    notificationGroupNames.add(name);
                    rowCounter++;
                }
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

    public Map<Integer, NotificationGroup> getNotificationGroups_ById() {
        
        Map<Integer, NotificationGroup> notificationGroupsById = new HashMap<>();
        
        try {
            List<NotificationGroup> notificationGroups = super.getAllDatabaseObjectsInTable();
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
    
}
