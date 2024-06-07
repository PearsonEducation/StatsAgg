package com.pearson.statsagg.database_objects.output_blacklist;

import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OutputBlacklistDao {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputBlacklistDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, OutputBlacklist outputBlacklist) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    OutputBlacklistSql.Insert_OutputBlacklist, 
                    outputBlacklist.getMetricGroupId());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, OutputBlacklist outputBlacklist) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    OutputBlacklistSql.Update_OutputBlacklist_ByPrimaryKey, 
                    outputBlacklist.getMetricGroupId(), outputBlacklist.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, OutputBlacklist outputBlacklist) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            OutputBlacklist outputBlacklistFromDb = OutputBlacklistDao.getOutputBlacklist(connection, false, outputBlacklist.getId());

            boolean upsertSuccess = true;
            if (outputBlacklistFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, outputBlacklist);
            else if (!outputBlacklistFromDb.isEqual(outputBlacklist)) upsertSuccess = update(connection, false, commitOnCompletion, outputBlacklist);

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

    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, OutputBlacklist outputBlacklist) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    OutputBlacklistSql.Delete_OutputBlacklist_ByPrimaryKey, 
                    outputBlacklist.getId());
            
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

    public static OutputBlacklist getOutputBlacklist(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<OutputBlacklist> outputBlacklists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new OutputBlacklistResultSetHandler(), 
                    OutputBlacklistSql.Select_OutputBlacklist_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(outputBlacklists);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static List<OutputBlacklist> getOutputBlacklists(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<OutputBlacklist> outputBlacklists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new OutputBlacklistResultSetHandler(), 
                    OutputBlacklistSql.Select_AllOutputBlacklist);
            
            return outputBlacklists;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
   public static OutputBlacklist getOutputBlacklist_SingleRow(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<OutputBlacklist> outputBlacklists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new OutputBlacklistResultSetHandler(), 
                    OutputBlacklistSql.Select_AllOutputBlacklist);
            
            if (outputBlacklists == null) return null;
            if (outputBlacklists.size() > 1) logger.warn("There should not be more than one output blacklist row in the database.");
        
            if (!outputBlacklists.isEmpty()) return outputBlacklists.get(0);
            else return null;
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
