package com.pearson.statsagg.database_objects.gauges;

import com.google.common.collect.Lists;
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
public class GaugesDao {
    
    private static final Logger logger = LoggerFactory.getLogger(GaugesDao.class.getName());
    
    public static boolean truncateTable(Connection connection, boolean closeConnectionOnCompletion) {
        try {
            List<String> databaseCreationSqlStatements = new ArrayList<>();
            databaseCreationSqlStatements.add(GaugesSql.TruncateTable_Gauges);
            return DatabaseUtils.genericDDL(connection, closeConnectionOnCompletion, databaseCreationSqlStatements);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
    }
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Gauge gauge) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    GaugesSql.Insert_Gauge, 
                    gauge.getBucketSha1(), gauge.getBucket(), gauge.getMetricValue(), gauge.getLastModified());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Gauge gauge) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    GaugesSql.Update_Gauge_ByPrimaryKey, 
                    gauge.getBucket(), gauge.getMetricValue(), gauge.getLastModified(), gauge.getBucketSha1());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Gauge gauge) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            Gauge gaugeFromDb = getGauge(connection, false, gauge.getBucketSha1());

            boolean upsertSuccess = true;
            if (gaugeFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, gauge);
            else if (!gaugeFromDb.isEqual(gauge)) upsertSuccess = update(connection, false, commitOnCompletion, gauge);

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

    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Gauge gauge) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    GaugesSql.Delete_Gauge_ByPrimaryKey, gauge.getBucketSha1());
            
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
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, String bucketSha1) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    GaugesSql.Delete_Gauge_ByPrimaryKey, bucketSha1);
            
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
    
    public static Gauge getGauge(Connection connection, boolean closeConnectionOnCompletion, String bucketSha1) {
        
        try {
            List<Gauge> gauges = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new GaugesResultSetHandler(), GaugesSql.Select_Gauge_ByPrimaryKey, bucketSha1);
            
            return DatabaseUtils.getSingleResultFromList(gauges);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Gauge> getGauges(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<Gauge> gauges = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new GaugesResultSetHandler(), GaugesSql.Select_AllGauges);
            
            return gauges;
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
    public static boolean batchUpsert(Connection connection, boolean closeConnectionOnCompletion, List<Gauge> gauges) {
        
        try {
            if ((gauges == null) || gauges.isEmpty()) {
                return false;
            }
            
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            boolean wasAllUpsertSuccess = true;
            
            List<List<Gauge>> gaugesPartitions = Lists.partition(gauges, 1000);

            for (List<Gauge> gaugesPartition : gaugesPartitions) {
                for (Gauge gauge : gaugesPartition) {
                    boolean wasUpsertSuccess = upsert(connection, false, false, gauge);
                    if (!wasUpsertSuccess) wasAllUpsertSuccess = false;
                }

                boolean wasCommitSuccess = DatabaseUtils.commit(connection, false);
                if (!wasCommitSuccess) wasAllUpsertSuccess = false;
            }
            
//            // code is untested after refactoring.  not confident this will work.    
//            if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
//                List<List<Object>> listOfParameters = new ArrayList<>();
//
//                for (Gauge gauge : gauges) {
//                    if (gauge == null) continue;
//                    List<Object> parameters = new ArrayList<>();
//                    parameters.add(gauge.getBucketSha1());
//                    parameters.add(gauge.getBucket());
//                    parameters.add(gauge.getMetricValue());
//                    parameters.add(gauge.getLastModified());
//                    listOfParameters.add(parameters);
//                }
//                   
//                List<Integer> results = DatabaseUtils.dml_PreparedStatement_Batch(connection, closeConnectionOnCompletion, 
//                        true, 1000, true, GaugesSql.generateBatchUpsert(1000), listOfParameters);
//                if (results == null) wasAllUpsertSuccess = false;
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
