package com.pearson.statsagg.database_objects.metric_groups;

import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTagsDao {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTagsDao.class.getName());
  
    protected static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTag metricGroupTag) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupTagsSql.Insert_MetricGroupTag, 
                    metricGroupTag.getMetricGroupId(), metricGroupTag.getTag());
            
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
    
    protected static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTag metricGroupTag) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupTagsSql.Update_MetricGroupTag_ByPrimaryKey, 
                    metricGroupTag.getMetricGroupId(), metricGroupTag.getMetricGroupId(), metricGroupTag.getTag(), metricGroupTag.getId());
            
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
    
    protected static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTag metricGroupTag) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricGroupTag metricGroupTagFromDb = getMetricGroupTag(connection, false, metricGroupTag.getId());

            boolean upsertSuccess = true;
            if (metricGroupTagFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, metricGroupTag);
            else if (!metricGroupTagFromDb.isEqual(metricGroupTag)) upsertSuccess = update(connection, false, commitOnCompletion, metricGroupTag);

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

    protected static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTag metricGroupTag) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupTagsSql.Delete_MetricGroupTag_ByPrimaryKey, metricGroupTag.getId());
            
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
                    MetricGroupTagsSql.Delete_MetricGroupTag_ByMetricGroupId, 
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
    
    protected static MetricGroupTag getMetricGroupTag(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupTagId) {
        
        try {
            List<MetricGroupTag> metricGroupTags = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTagsResultSetHandler(), 
                    MetricGroupTagsSql.Select_MetricGroupTag_ByPrimaryKey, metricGroupTagId);
            
            return DatabaseUtils.getSingleResultFromList(metricGroupTags);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    protected static List<MetricGroupTag> getMetricGroupTags(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroupTag> metricGroupTags = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTagsResultSetHandler(), 
                    MetricGroupTagsSql.Select_AllMetricGroupTags);
            
            return metricGroupTags;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<MetricGroupTag> getMetricGroupTagsByMetricGroupId(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupId) {
        
        try {
            List<MetricGroupTag> metricGroupTags = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTagsResultSetHandler(), 
                    MetricGroupTagsSql.Select_MetricGroupTags_ByMetricGroupId, metricGroupId);
            
            return metricGroupTags;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    protected static TreeSet<String> getMetricGroupTagsSortedSetByMetricGroupId(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupId) {
        
        try {
            List<MetricGroupTag> metricGroupTags = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTagsResultSetHandler(), 
                    MetricGroupTagsSql.Select_MetricGroupTags_ByMetricGroupId, metricGroupId);
            
            if (metricGroupTags == null) return null;
            
            TreeSet<String> metricGroupTagsSet = new TreeSet<>();
            
            for (MetricGroupTag metricGroupTag : metricGroupTags) {
                if (metricGroupTag.getTag() != null) {
                    metricGroupTagsSet.add(metricGroupTag.getTag());
                }
            }
            
            return metricGroupTagsSet;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    protected static Map<Integer,List<MetricGroupTag>> getAllMetricGroupTagsByMetricGroupId(Connection connection, boolean closeConnectionOnCompletion) {
        
        List<MetricGroupTag> metricGroupTags = getMetricGroupTags(connection, closeConnectionOnCompletion); 
        
        if ((metricGroupTags == null) || metricGroupTags.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Integer,List<MetricGroupTag>> metricGroupTagByMetricGroupId = new HashMap<>();

        try {
            for (MetricGroupTag metricGroupTag : metricGroupTags) {
                Integer metricGroupId = metricGroupTag.getMetricGroupId();

                if (metricGroupTagByMetricGroupId.containsKey(metricGroupId)) {
                    List<MetricGroupTag> metricGroupTagsForMetricGroup = metricGroupTagByMetricGroupId.get(metricGroupId);
                    metricGroupTagsForMetricGroup.add(metricGroupTag);
                }
                else {
                    List<MetricGroupTag> metricGroupTagsForMetricGroup = new ArrayList<>();
                    metricGroupTagsForMetricGroup.add(metricGroupTag);
                    metricGroupTagByMetricGroupId.put(metricGroupId, metricGroupTagsForMetricGroup);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return metricGroupTagByMetricGroupId;
    }
    
}
