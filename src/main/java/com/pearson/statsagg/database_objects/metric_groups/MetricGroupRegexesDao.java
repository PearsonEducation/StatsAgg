package com.pearson.statsagg.database_objects.metric_groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupRegexesDao {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupRegexesDao.class.getName());
    
    protected static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupRegex metricGroupRegex) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupRegexesSql.Insert_MetricGroupRegex, 
                    metricGroupRegex.getMetricGroupId(), metricGroupRegex.isBlacklistRegex(), metricGroupRegex.getPattern());
            
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
    
    protected static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupRegex metricGroupRegex) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupRegexesSql.Update_MetricGroupRegex_ByPrimaryKey, 
                    metricGroupRegex.getMetricGroupId(), metricGroupRegex.isBlacklistRegex(), metricGroupRegex.getPattern(), metricGroupRegex.getId());
            
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
    
    protected static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupRegex metricGroupRegex) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricGroupRegex metricGroupRegexFromDb = getMetricGroupRegex(connection, false, metricGroupRegex.getId());

            boolean upsertSuccess = true;
            if (metricGroupRegexFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, metricGroupRegex);
            else if (!metricGroupRegexFromDb.isEqual(metricGroupRegex)) upsertSuccess = update(connection, false, commitOnCompletion, metricGroupRegex);

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

    protected static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupRegex metricGroupRegex) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupRegexesSql.Delete_MetricGroupRegex_ByPrimaryKey, 
                    metricGroupRegex.getId());
            
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
    
    protected static boolean deleteByMetricGroupId(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Integer metricGroupId) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupRegexesSql.Delete_MetricGroupRegex_ByMetricGroupId, 
                    metricGroupId);
            
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
    
    protected static MetricGroupRegex getMetricGroupRegex(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupRegexId) {
        
        try {
            List<MetricGroupRegex> metricGroupRegexes = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupRegexesResultSetHandler(), 
                    MetricGroupRegexesSql.Select_MetricGroupRegex_ByPrimaryKey, metricGroupRegexId);
            
            return DatabaseUtils.getSingleResultFromList(metricGroupRegexes);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    protected static List<MetricGroupRegex> getMetricGroupRegexes(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroupRegex> metricGroupRegexes = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupRegexesResultSetHandler(), 
                    MetricGroupRegexesSql.Select_AllMetricGroupRegexes);
            
            return metricGroupRegexes;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<MetricGroupRegex> getMetricGroupRegexesByMetricGroupId(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupId) {
        
        try {
            List<MetricGroupRegex> metricGroupRegexes = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupRegexesResultSetHandler(), 
                    MetricGroupRegexesSql.Select_MetricGroupRegexes_ByMetricGroupId, metricGroupId);
            
            return metricGroupRegexes;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    protected static Map<Integer,List<MetricGroupRegex>> getAllMetricGroupRegexesByMetricGroupId(Connection connection, boolean closeConnectionOnCompletion) {
        
        Map<Integer,List<MetricGroupRegex>> metricGroupRegexesByMetricGroupId = null;
        
        try {
            List<MetricGroupRegex> metricGroupRegexes = getMetricGroupRegexes(connection, closeConnectionOnCompletion);
            if (metricGroupRegexes == null) return null;

            metricGroupRegexesByMetricGroupId = new HashMap<>();
            
            for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
                Integer metricGroupId = metricGroupRegex.getMetricGroupId();

                if (metricGroupRegexesByMetricGroupId.containsKey(metricGroupId)) {
                    List<MetricGroupRegex> metricGroupRegexesForMetricGroup = metricGroupRegexesByMetricGroupId.get(metricGroupId);
                    metricGroupRegexesForMetricGroup.add(metricGroupRegex);
                }
                else {
                    List<MetricGroupRegex> metricGroupRegexesForMetricGroup = new ArrayList<>();
                    metricGroupRegexesForMetricGroup.add(metricGroupRegex);
                    metricGroupRegexesByMetricGroupId.put(metricGroupId, metricGroupRegexesForMetricGroup);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
        return metricGroupRegexesByMetricGroupId;
    }

}
