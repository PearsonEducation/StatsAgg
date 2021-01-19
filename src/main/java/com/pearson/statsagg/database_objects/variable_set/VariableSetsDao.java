package com.pearson.statsagg.database_objects.variable_set;

import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * @author Jeffrey Schmidt
 */
public class VariableSetsDao {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetsDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSet variableSet) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetsSql.Insert_VariableSet, 
                    variableSet.getName(), variableSet.getUppercaseName(), variableSet.getDescription(), variableSet.getVariables());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSet variableSet) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetsSql.Update_VariableSet_ByPrimaryKey, 
                    variableSet.getName(), variableSet.getUppercaseName(), variableSet.getDescription(), variableSet.getVariables(), variableSet.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSet variableSet) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            VariableSet variableSetFromDb = VariableSetsDao.getVariableSet(connection, false, variableSet.getId());

            boolean upsertSuccess = true;
            if (variableSetFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, variableSet);
            else if (!variableSetFromDb.isEqual(variableSet)) upsertSuccess = update(connection, false, commitOnCompletion, variableSet);

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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSet variableSet, String oldVariableSetName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            VariableSet variableSetFromDb = VariableSetsDao.getVariableSet(connection, false, oldVariableSetName);

            boolean upsertSuccess = true;
            if (variableSetFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, variableSet);
            }
            else {
                variableSet.setId(variableSetFromDb.getId());
                if (!variableSetFromDb.isEqual(variableSet)) upsertSuccess = update(connection, false, commitOnCompletion, variableSet);
            }

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
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, VariableSet variableSet) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    VariableSetsSql.Delete_VariableSet_ByPrimaryKey, 
                    variableSet.getId());
            
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

    public static VariableSet getVariableSet(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<VariableSet> variableSets = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetsResultSetHandler(), 
                    VariableSetsSql.Select_VariableSet_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(variableSets);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static VariableSet getVariableSet(Connection connection, boolean closeConnectionOnCompletion, String variableSetName) {
        
        try {
            List<VariableSet> variableSets = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetsResultSetHandler(), 
                    VariableSetsSql.Select_VariableSet_ByName, variableSetName);
            
            return DatabaseUtils.getSingleResultFromList(variableSets);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static VariableSet getVariableSet_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, String variableSetName) {
        
        try {
            List<VariableSet> variableSets = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetsResultSetHandler(), 
                    VariableSetsSql.Select_VariableSet_ByUppercaseName, variableSetName.toUpperCase());
            
            return DatabaseUtils.getSingleResultFromList(variableSets);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<VariableSet> getVariableSets(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<VariableSet> variableSets = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetsResultSetHandler(), 
                    VariableSetsSql.Select_AllVariableSets);
            
            return variableSets;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<Integer,VariableSet> getVariableSets_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<Integer,VariableSet> variableSets_ById = new HashMap<>();
            
            List<VariableSet> variableSets = getVariableSets(connection, false);
            if (variableSets == null) return null;
            
            for (VariableSet variableSet : variableSets) {
                if ((variableSet == null) || (variableSet.getId() == null)) continue;
                variableSets_ById.put(variableSet.getId(), variableSet);
            }

            return variableSets_ById;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,VariableSet> getVariableSets_ByName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<String,VariableSet> variableSets_ByName = new HashMap<>();
            
            List<VariableSet> variableSets = getVariableSets(connection, false);
            if (variableSets == null) return null;
            
            for (VariableSet variableSet : variableSets) {
                if ((variableSet == null) || (variableSet.getName() == null)) continue;
                variableSets_ByName.put(variableSet.getName(), variableSet);
            }

            return variableSets_ByName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getVariableSetNames_OrderedByName(Connection connection, boolean closeConnectionOnCompletion, List<Integer> variableSetIds) {
        
        try {
            if (variableSetIds.isEmpty()) return new ArrayList<>();
            
            List<VariableSet> variableSets = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new VariableSetsResultSetHandler(), 
                    VariableSetsSql.selectVariableSetNames_ByListOfVariableSetIds_OrderByNameAsc(variableSetIds), 
                    variableSetIds);
            
            List<String> variableSetNames = new ArrayList<>();
            
            for (VariableSet variableSet : variableSets) {
                if ((variableSet == null) || (variableSet.getName() == null)) continue;
                variableSetNames.add(variableSet.getName());
            }
            
            return variableSetNames;
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
