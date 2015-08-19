package com.pearson.statsagg.database_objects.metric_group;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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
public class MetricGroupsDao extends DatabaseObjectDao<MetricGroup> {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsDao.class.getName());
   
    private final String tableName_ = "METRIC_GROUPS";
    
    public MetricGroupsDao(){}
            
    public MetricGroupsDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public boolean dropTable() {
        return dropTable(MetricGroupsSql.DropTable_MetricGroups);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(MetricGroupsSql.CreateTable_MetricGroups_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(MetricGroupsSql.CreateTable_MetricGroups_Derby);
            databaseCreationSqlStatements.add(MetricGroupsSql.CreateIndex_MetricGroups_PrimaryKey);
        }
        
        databaseCreationSqlStatements.add(MetricGroupsSql.CreateIndex_MetricGroups_Unique_Name);
        databaseCreationSqlStatements.add(MetricGroupsSql.CreateIndex_MetricGroups_Unique_UppercaseName);

        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public MetricGroup getDatabaseObject(MetricGroup metricGroup) {
        if (metricGroup == null) return null;
        
        return getDatabaseObject(MetricGroupsSql.Select_MetricGroup_ByPrimaryKey, 
                metricGroup.getId()); 
    }
    
    @Override
    public boolean insert(MetricGroup metricGroup) {
        if (metricGroup == null) return false;
        
        return insert(MetricGroupsSql.Insert_MetricGroup, 
                metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription());
    }
    
    @Override
    public boolean update(MetricGroup metricGroup) {
        if (metricGroup == null) return false;
        
        return update(MetricGroupsSql.Update_MetricGroup_ByPrimaryKey, 
                metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription(), metricGroup.getId());
    }

    @Override
    public boolean delete(MetricGroup metricGroup) {
        if (metricGroup == null) return false;

        return delete(MetricGroupsSql.Delete_MetricGroup_ByPrimaryKey, 
                metricGroup.getId()); 
    }
    
    @Override
    public MetricGroup processSingleResultAllColumns(ResultSet resultSet) {
        
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

            MetricGroup metricGroup = new MetricGroup(id, name, uppercaseName, description);
            
            return metricGroup;
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
    
    public MetricGroup getMetricGroup(int id) {
        return getDatabaseObject(MetricGroupsSql.Select_MetricGroup_ByPrimaryKey, 
                id); 
    }  
    
    public MetricGroup getMetricGroupByName(String name) {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(MetricGroupsSql.Select_MetricGroup_ByName, 1);
            databaseInterface_.addPreparedStatementParameters(name);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            if (resultSet.next()) {
                MetricGroup metricGroup = processSingleResultAllColumns(resultSet);
                return metricGroup;
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
    
    public List<Integer> getAllMetricGroupIds() {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(MetricGroupsSql.Select_DistinctMetricGroupIds, 1000);
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
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public List<String> getMetricGroupNames(String filter, int resultSetLimit) {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            List<String> metricGroupNames = new ArrayList<>();
            
            databaseInterface_.createPreparedStatement(MetricGroupsSql.Select_MetricGroup_Names_OrderByName, 1000);
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
                    metricGroupNames.add(name);
                    rowCounter++;
                }
            }
            
            return metricGroupNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }

    public JSONObject getMetricGroups(int offset, int pageSize) {
        logger.debug("getMetricGroups");
        List<Object> parametersList = new ArrayList<>(2);
        
        JSONArray metricGroupsList = new JSONArray();
        JSONObject metricGroupsJson = new JSONObject();
        int alertsCount = 0;
        
        try {
            if (!isConnectionValid()) {
                return null;
            }
            
            if ((offset == 0) && (pageSize == 0)) {
                metricGroupsJson.put("metricgroups", metricGroupsList);
                metricGroupsJson.put("count", alertsCount);
                return metricGroupsJson;
            }
            
            parametersList.add(offset);
            parametersList.add(pageSize);
            
            if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
                databaseInterface_.createPreparedStatement(MetricGroupsSql.Select_MetricGroups_ByPageNumberAndPageSize_Derby, pageSize);
            } else {
                databaseInterface_.createPreparedStatement(MetricGroupsSql.Select_MetricGroups_ByPageNumberAndPageSize_MySQL, pageSize);
            }
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
                metricGroupsList.add(alert);
                alertsCount++;
            }
            
            metricGroupsJson.put("metricgroups", metricGroupsList);
            metricGroupsJson.put("count", alertsCount);
            
            return metricGroupsJson;
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
