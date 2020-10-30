package com.pearson.statsagg.database_objects.metric_groups;

import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupsDao {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupsSql.Insert_MetricGroup, 
                    metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupsSql.Update_MetricGroup_ByPrimaryKey, 
                    metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription(), metricGroup.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricGroup metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, metricGroup.getId());

            boolean upsertSuccess = true;
            if (metricGroupFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, metricGroup);
            else if (!metricGroupFromDb.isEqual(metricGroup)) upsertSuccess = update(connection, false, commitOnCompletion, metricGroup);

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

    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupsSql.Delete_MetricGroup_ByPrimaryKey, 
                    metricGroup.getId());
            
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

    public static MetricGroup getMetricGroup(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_MetricGroup_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(metricGroups);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static MetricGroup getMetricGroup(Connection connection, boolean closeConnectionOnCompletion, String metricGroupName) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_MetricGroup_ByName, metricGroupName);
            
            return DatabaseUtils.getSingleResultFromList(metricGroups);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<MetricGroup> getMetricGroups(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_AllMetricGroups);
            
            return metricGroups;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getMetricGroupNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> metricGroupNames = new ArrayList<>();
            
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupsResultSetHandler(), 
                    (resultSetLimit + 5), 
                    MetricGroupsSql.Select_MetricGroup_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((metricGroups == null) || metricGroups.isEmpty()) return metricGroupNames;
            
            int rowCounter = 0;
            for (MetricGroup metricGroup : metricGroups) {
                if ((metricGroup != null) && (metricGroup.getName() != null)) {
                    metricGroupNames.add(metricGroup.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return metricGroupNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Integer> getAllMetricGroupIds(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<Integer> metricGroupIds = new ArrayList<>();
            
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_DistinctMetricGroupIds);
            
            if ((metricGroups == null) || metricGroups.isEmpty()) return metricGroupIds;
            
            for (MetricGroup metricGroup : metricGroups) {
                if ((metricGroup != null) && (metricGroup.getId() != null)) {
                    metricGroupIds.add(metricGroup.getId());
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
    
}
