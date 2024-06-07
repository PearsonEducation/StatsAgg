package com.pearson.statsagg.database_objects.variable_set_list_entry;

import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * @author Jeffrey Schmidt
 */
public class VariableSetListEntriesDao {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetListEntriesDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetListEntry variableSetListEntry) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetListEntriesSql.Insert_VariableSetListEntry, 
                    variableSetListEntry.getVariableSetListId(), variableSetListEntry.getVariableSetId());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetListEntry variableSetListEntry) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetListEntriesSql.Update_VariableSetListEntry_ByPrimaryKey, 
                    variableSetListEntry.getVariableSetListId(), variableSetListEntry.getVariableSetId(), variableSetListEntry.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetListEntry variableSetListEntry) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            VariableSetListEntry variableSetListEntryFromDb = VariableSetListEntriesDao.getVariableSetListEntry(connection, false, variableSetListEntry.getId());

            boolean upsertSuccess = true;
            if (variableSetListEntryFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, variableSetListEntry);
            else if (!variableSetListEntryFromDb.isEqual(variableSetListEntry)) upsertSuccess = update(connection, false, commitOnCompletion, variableSetListEntry);

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

    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetListEntry variableSetListEntry) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetListEntriesSql.Delete_VariableSetListEntry_ByPrimaryKey, 
                    variableSetListEntry.getId());
            
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
    
    public static boolean delete_ByVariableSetListId(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Integer variableSetListId) {
        
        try {     
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetListEntriesSql.Delete_VariableSetListEntry_ByVariableSetListId, 
                    variableSetListId);
            
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
    
    public static List<VariableSetListEntry> getVariableSetListEntries(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<VariableSetListEntry> variableSetListEntries = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListEntriesResultSetHandler(), 
                    VariableSetListEntriesSql.Select_AllVariableSetListEntries);
            
            return variableSetListEntries;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static VariableSetListEntry getVariableSetListEntry(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<VariableSetListEntry> variableSetListEntrys = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListEntriesResultSetHandler(), 
                    VariableSetListEntriesSql.Select_VariableSetListEntry_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(variableSetListEntrys);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<VariableSetListEntry> getVariableSetListEntries_ByVariableSetId(Connection connection, boolean closeConnectionOnCompletion, Integer variableSetId) {
        
        try {
            List<VariableSetListEntry> variableSetListEntries = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListEntriesResultSetHandler(), 
                    VariableSetListEntriesSql.Select_VariableSetListEntries_ByVariableSetId, variableSetId);
            
            return variableSetListEntries;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<VariableSetListEntry> getVariableSetListEntries_ByVariableSetListId(Connection connection, boolean closeConnectionOnCompletion, Integer variableSetListId) {
        
        try {
            List<VariableSetListEntry> variableSetListEntries = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListEntriesResultSetHandler(), 
                    VariableSetListEntriesSql.Select_VariableSetListEntries_ByVariableSetListId, variableSetListId);
            
            return variableSetListEntries;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Integer> getVariableSetIds_ForVariableSetListId(Connection connection, boolean closeConnectionOnCompletion, Integer variableSetListId) {
        
        try {
            List<VariableSetListEntry> variableSetListEntries = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListEntriesResultSetHandler(), 
                    VariableSetListEntriesSql.Select_VariableSetIds_ByVariableSetListId, 
                    variableSetListId);
            
            List<Integer> variableSetIds = new ArrayList<>();
            
            for (VariableSetListEntry variableSetListEntry : variableSetListEntries) {
                if ((variableSetListEntry == null) || (variableSetListEntry.getVariableSetId() == null)) continue;
                variableSetIds.add(variableSetListEntry.getVariableSetId());
            }
            
            return variableSetIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Set<Integer> getAllDistinctVariableSetIds(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<VariableSetListEntry> variableSetListEntries = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListEntriesResultSetHandler(), 
                    VariableSetListEntriesSql.Select_DistinctVariableSetIds);
            
            Set<Integer> variableSetIds = new HashSet<>();
            
            for (VariableSetListEntry variableSetListEntry : variableSetListEntries) {
                if ((variableSetListEntry == null) || (variableSetListEntry.getVariableSetId() == null)) continue;
                variableSetIds.add(variableSetListEntry.getVariableSetId());
            }
            
            return variableSetIds;
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
