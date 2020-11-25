package com.pearson.statsagg.database_objects.variable_set_list;

import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * @author Jeffrey Schmidt
 */
public class VariableSetListsDao {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetListsDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetList variableSetList) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetListsSql.Insert_VariableSetList, 
                    variableSetList.getName(), variableSetList.getUppercaseName(), variableSetList.getDescription());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetList variableSetList) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetListsSql.Update_VariableSetList_ByPrimaryKey, 
                    variableSetList.getName(), variableSetList.getUppercaseName(), variableSetList.getDescription(), variableSetList.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetList variableSetList) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            VariableSetList variableSetListFromDb = VariableSetListsDao.getVariableSetList(connection, false, variableSetList.getId());

            boolean upsertSuccess = true;
            if (variableSetListFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, variableSetList);
            else if (!variableSetListFromDb.isEqual(variableSetList)) upsertSuccess = update(connection, false, commitOnCompletion, variableSetList);

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

    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSetList variableSetList) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetListsSql.Delete_VariableSetList_ByPrimaryKey, 
                    variableSetList.getId());
            
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

    public static VariableSetList getVariableSetList(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<VariableSetList> variableSetLists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListsResultSetHandler(), 
                    VariableSetListsSql.Select_VariableSetList_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(variableSetLists);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static VariableSetList getVariableSetList(Connection connection, boolean closeConnectionOnCompletion, String variableSetListName) {
        
        try {
            List<VariableSetList> variableSetLists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListsResultSetHandler(), 
                    VariableSetListsSql.Select_VariableSetList_ByName, variableSetListName);
            
            return DatabaseUtils.getSingleResultFromList(variableSetLists);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<VariableSetList> getVariableSetListIdsAndNames(Connection connection, boolean closeConnectionOnCompletion) {

        try {
            List<VariableSetList> variableSetLists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListsResultSetHandler(), 
                    VariableSetListsSql.Select_AllVariableSetLists_IdsAndNames);
            
            return variableSetLists;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<VariableSetList> getVariableSetLists(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<VariableSetList> variableSetLists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListsResultSetHandler(), 
                    VariableSetListsSql.Select_AllVariableSetLists);
            
            return variableSetLists;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getVariableSetListNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> variableSetListNames = new ArrayList<>();
            
            List<VariableSetList> variableSetLists = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetListsResultSetHandler(), 
                    (resultSetLimit + 5), 
                    VariableSetListsSql.Select_VariableSetList_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((variableSetLists == null) || variableSetLists.isEmpty()) return variableSetListNames;
            
            int rowCounter = 0;
            for (VariableSetList variableSetList : variableSetLists) {
                if ((variableSetList != null) && (variableSetList.getName() != null)) {
                    variableSetListNames.add(variableSetList.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return variableSetListNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static Map<Integer, VariableSetList> getVariableSetLists_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        Map<Integer, VariableSetList> variableSetListsById = new HashMap<>();
        
        try {
            List<VariableSetList> variableSetLists = getVariableSetLists(connection, closeConnectionOnCompletion);
            if (variableSetLists == null) return variableSetListsById;

            for (VariableSetList variableSetList : variableSetLists) {
                if (variableSetList.getId() != null) variableSetListsById.put(variableSetList.getId(), variableSetList);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return variableSetListsById;
    }
        
    public static Map<Integer, String> getVariableSetListNames_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        Map<Integer, String> variableSetListNamesById = new HashMap<>();
        
        try {
            List<VariableSetList> variableSetLists = getVariableSetListIdsAndNames(connection, closeConnectionOnCompletion);
            if (variableSetLists == null) return variableSetListNamesById;

            for (VariableSetList variableSetList : variableSetLists) {
                if (variableSetList.getName() != null) variableSetListNamesById.put(variableSetList.getId(), variableSetList.getName());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return variableSetListNamesById;
    }

}
