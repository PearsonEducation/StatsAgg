package com.pearson.statsagg.database_objects.metric_last_seen;

import com.google.common.collect.Lists;
import com.pearson.statsagg.database_objects.DDL_Helper;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricLastSeenDao {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricLastSeenDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricLastSeen metricLastSeen) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricLastSeenSql.Insert_MetricLastSeen, 
                    metricLastSeen.getMetricKeySha1(), metricLastSeen.getMetricKey(), metricLastSeen.getLastModified());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricLastSeen metricLastSeen) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricLastSeenSql.Update_MetricLastSeen_ByPrimaryKey, 
                    metricLastSeen.getMetricKey(), metricLastSeen.getLastModified(), metricLastSeen.getMetricKeySha1());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricLastSeen metricLastSeen) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricLastSeen metricLastSeenFromDb = getMetricLastSeen(connection, false, metricLastSeen.getMetricKeySha1());

            boolean upsertSuccess = true;
            if (metricLastSeenFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, metricLastSeen);
            else if (!metricLastSeenFromDb.isEqual(metricLastSeen)) upsertSuccess = update(connection, false, commitOnCompletion, metricLastSeen);

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

    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricLastSeen metricLastSeen) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricLastSeenSql.Delete_MetricLastSeen_ByPrimaryKey, metricLastSeen.getMetricKeySha1());
            
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
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, String metricKeySha1) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricLastSeenSql.Delete_MetricLastSeen_ByPrimaryKey, metricKeySha1);
            
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
    
    public static MetricLastSeen getMetricLastSeen(Connection connection, boolean closeConnectionOnCompletion, String metricKeySha1) {
        
        try {
            List<MetricLastSeen> metricLastSeens = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricLastSeenResultSetHandler(), MetricLastSeenSql.Select_MetricLastSeen_ByPrimaryKey, metricKeySha1);
            
            return DatabaseUtils.getSingleResultFromList(metricLastSeens);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<MetricLastSeen> getMetricLastSeens(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricLastSeen> metricLastSeens = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricLastSeenResultSetHandler(), MetricLastSeenSql.Select_AllMetricLastSeen);
            
            return metricLastSeens;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    // this method automatically commits gauges in batches of 1000
    public static boolean batchUpsert(Connection connection, boolean closeConnectionOnCompletion, List<MetricLastSeen> metricLastSeens) {
        
        try {
            if ((metricLastSeens == null) || metricLastSeens.isEmpty()) {
                return false;
            }
            
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            boolean wasAllUpsertSuccess = true;
            
            List<List<MetricLastSeen>> metricLastSeensPartitions = Lists.partition(metricLastSeens, 1000);

            for (List<MetricLastSeen> metricLastSeenPartition : metricLastSeensPartitions) {
                for (MetricLastSeen metricLastSeen : metricLastSeenPartition) {
                    boolean wasUpsertSuccess = upsert(connection, false, false, metricLastSeen);
                    if (!wasUpsertSuccess) wasAllUpsertSuccess = false;
                }

                boolean wasCommitSuccess = DatabaseUtils.commit(connection, false);
                if (!wasCommitSuccess) wasAllUpsertSuccess = false;
            }
            
//            // code is untested after refactoring.  not confident this will work.    
//            if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
//                boolean wasAllUpsertSuccess = true;
//                List<List<MetricLastSeen>> metricLastSeenPartitions = Lists.partition(metricLastSeens, 1000);
//
//                for (List<MetricLastSeen> metricLastSeenPartition : metricLastSeenPartitions) {
//                    List<Object> parameters = new ArrayList<>();
//
//                    for (MetricLastSeen metricLastSeen : metricLastSeenPartition) {
//                        parameters.add(metricLastSeen.getMetricKeySha1());
//                        parameters.add(metricLastSeen.getMetricKey());
//                        parameters.add(metricLastSeen.getLastModified());
//                    }
//
//                    boolean wasUpsertSuccess = genericDmlStatement(MetricLastSeenSql.generateBatchUpsert(metricLastSeenPartition.size()), parameters);
//                    if (!wasUpsertSuccess) wasAllUpsertSuccess = false;
//                }
//
//                return wasAllUpsertSuccess;
//            }

            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
            return wasAllUpsertSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
}
