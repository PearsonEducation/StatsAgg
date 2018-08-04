package com.pearson.statsagg.database_objects.metric_group;

import com.pearson.statsagg.database_engine.DatabaseInterface;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.database_engine.DatabaseObjectDao;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
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
    
    public MetricGroupsDao(DatabaseInterface databaseInterface) {
        super(databaseInterface);
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
        if (metricGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return null;
        }
        
        return getDatabaseObject(MetricGroupsSql.Select_MetricGroup_ByPrimaryKey, metricGroup.getId()); 
    }
    
    @Override
    public boolean insert(MetricGroup metricGroup) {
        if (metricGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        return insert(MetricGroupsSql.Insert_MetricGroup, metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription());
    }
    
    @Override
    public boolean update(MetricGroup metricGroup) {
        if (metricGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        return update(MetricGroupsSql.Update_MetricGroup_ByPrimaryKey, metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription(), metricGroup.getId());
    }

    @Override
    public boolean delete(MetricGroup metricGroup) {
        if (metricGroup == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }

        return delete(MetricGroupsSql.Delete_MetricGroup_ByPrimaryKey, metricGroup.getId()); 
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
    
    public MetricGroup getMetricGroup(Integer id) {
        return getDatabaseObject(MetricGroupsSql.Select_MetricGroup_ByPrimaryKey, id); 
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
    
    public List<String> getMetricGroupNames(String filter, Integer resultSetLimit) {
        
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

}
