package com.pearson.statsagg.database_engine;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * @param <T>
 */
public abstract class DatabaseObjectDao<T extends DatabaseObject> extends DatabaseDao {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseObjectDao.class.getName());

    public DatabaseObjectDao() {
        super();
    }
   
    public DatabaseObjectDao(boolean closeConnectionAfterOperation) {
        super(closeConnectionAfterOperation);
    }
     
    public DatabaseObjectDao(DatabaseInterface databaseInterface) {
        super(databaseInterface);
    }
 
    public DatabaseObjectDao(DatabaseInterface databaseInterface, boolean closeConnectionAfterOperation) {
        super(databaseInterface, closeConnectionAfterOperation);
    }

    public boolean dropTable(String sql) {

        boolean isTransactionSuccess = true;
        
        try {
            
            if (!isConnectionValid() || (isConnectionReadOnly() == null) || isConnectionReadOnly()) {
                return false;
            }
             
            databaseInterface_.createStatement().execute(sql);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isTransactionSuccess = false;
        }
        finally {
            isTransactionSuccess = databaseInterface_.endTransactionConditional(isTransactionSuccess) && isTransactionSuccess;
            databaseInterface_.cleanupAutomatic();
        }

        return isTransactionSuccess;
    }
    
    public boolean truncateTable(String sql) {

        boolean isTransactionSuccess = true;
        
        try {
            
            if (!isConnectionValid() || (isConnectionReadOnly() == null) || isConnectionReadOnly()) {
                return false;
            }
             
            databaseInterface_.createStatement().execute(sql);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isTransactionSuccess = false;
        }
        finally {
            isTransactionSuccess = databaseInterface_.endTransactionConditional(isTransactionSuccess) && isTransactionSuccess;
            databaseInterface_.cleanupAutomatic();
        }

        return isTransactionSuccess;
    }
    
    public boolean createTable(String... parameters) {
        List<String> parametersList;
        
        try {
            parametersList = new ArrayList<>(Arrays.asList(parameters));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            parametersList = new ArrayList<>();
        }

        return createTable(parametersList);
    }
    
    public boolean createTable(List<String> databaseCreationSqlStatements) {

        boolean isTransactionSuccess = true;
                
        try {
            
            if (!isConnectionValid() || (isConnectionReadOnly() == null) || isConnectionReadOnly()) {
                return false;
            }

            for (String databaseCreationSqlStatement : databaseCreationSqlStatements) {
                databaseInterface_.createStatement().execute(databaseCreationSqlStatement);
            }
        }
        catch (Exception e) {
            if (e.toString().contains("already exists") && 
                    (e.toString().contains("java.sql.SQLException: Table/View") || e.toString().contains("MySQLSyntaxErrorException: Table"))) {
                logger.warn("Table " + getTableName() + " already exists.");
            }
            else {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
            isTransactionSuccess = false;
        }  
        finally {
            isTransactionSuccess = databaseInterface_.endTransactionConditional(isTransactionSuccess) && isTransactionSuccess;
            databaseInterface_.cleanupAutomatic();
        }

        return isTransactionSuccess;
    }
    
    public T getDatabaseObject(String sql, Object... parameters) {
        
        List<Object> parametersList;
        
        try {
            parametersList = new ArrayList<>(Arrays.asList(parameters));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            parametersList = new ArrayList<>();
        }
        
        return getDatabaseObject(sql, parametersList);
    }
    
    public T getDatabaseObject(String sql, List<Object> parameters) {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(sql, 1);
            databaseInterface_.addPreparedStatementParameters(parameters);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();
            if (resultSet.next()) {
                T databaseObject = processSingleResultAllColumns(resultSet);
                return databaseObject;
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
    
    public List<T> getAllDatabaseObjectsInTable() {
        return getAllDatabaseObjectsInTable(10000);
    }
        
    public List<T> getAllDatabaseObjectsInTable(Integer fetchSize) {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            String sql = "SELECT * FROM " + getTableName();
            databaseInterface_.createPreparedStatement(sql, fetchSize);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            List<T> databaseObjects = new ArrayList<>();
            ResultSet resultSet = databaseInterface_.getResults();

            while (resultSet.next()) {
                T databaseObject = processSingleResultAllColumns(resultSet);
                databaseObjects.add(databaseObject);
            }

            return databaseObjects;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public Long countDatabaseObjectsInTable() {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            String sql = "SELECT COUNT(*) FROM " + getTableName();
            databaseInterface_.createPreparedStatement(sql, 1);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            if (resultSet.next()) {
                Long rowCount = resultSet.getLong(1);
                return rowCount;
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
    
    public boolean insert(String sql, Object... parameters) {
        
        List<Object> parametersList;
        
        try {
            parametersList = new ArrayList<>(Arrays.asList(parameters));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            parametersList = new ArrayList<>();
        }
        
        return genericDmlStatement(sql, parametersList);
    }
    
    public boolean insert(String sql, List<Object> parameters) {
        return genericDmlStatement(sql, parameters);
    }
    
    public boolean insert(List<T> databaseObjects, boolean commitOnPartialSuccess) {
        
        if (databaseInterface_.isManualTransactionControl()) {
            logger.error("This functional cannot be called when using manual transaction control");
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        boolean isTransactionSuccess = false;
        
        try {     
            
            if ((databaseObjects == null)) {
                return false;
            }
            else if (databaseObjects.isEmpty()) {
                return true;
            }

            if (!isConnectionValid() || (isConnectionReadOnly() == null) || isConnectionReadOnly()) {
                return false;
            }
            
            databaseInterface_.beginTransaction();

            boolean isBatchInsertSuccess = true;
            
            for (T databaseObject : databaseObjects) { 
                boolean isInsertSuccess = insert(databaseObject);
                
                if (!isInsertSuccess) {
                    isBatchInsertSuccess = false;
                    logger.debug("Failed inserting " + getTableName().toLowerCase() + " in database. DatabaseObject=" + databaseObject.toString());
                }
            }
 
            if (commitOnPartialSuccess) {
                isTransactionSuccess = databaseInterface_.endTransaction(true) && isBatchInsertSuccess;
            }
            else {
                isTransactionSuccess = databaseInterface_.endTransaction(isBatchInsertSuccess) && isBatchInsertSuccess;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isTransactionSuccess = false;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        }

        return isTransactionSuccess;
    }
    
    public boolean update(String sql, Object... parameters) {
        
        List<Object> parametersList;
        
        try {
            parametersList = new ArrayList<>(Arrays.asList(parameters));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            parametersList = new ArrayList<>();
        }
        
        return genericDmlStatement(sql, parametersList);
    }
    
    public boolean update(String sql, List<Object> parameters) {
        return genericDmlStatement(sql, parameters);
    }
    
    public boolean update(List<T> databaseObjects, boolean commitOnPartialSuccess) {
        
        if (databaseInterface_.isManualTransactionControl()) {
            logger.error("This functional cannot be called when using manual transaction control");
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        boolean isTransactionSuccess = false;
        
        try {     
            
            if ((databaseObjects == null)) {
                return false;
            }
            else if (databaseObjects.isEmpty()) {
                return true;
            }

            if (!isConnectionValid() || (isConnectionReadOnly() == null) || isConnectionReadOnly()) {
                return false;
            }
            
            databaseInterface_.beginTransaction();

            boolean isBatchUpdateSuccess = true;
            
            for (T databaseObject : databaseObjects) { 
                boolean isUpdateSuccess = update(databaseObject);
                
                if (!isUpdateSuccess) {
                    isBatchUpdateSuccess = false;
                    logger.debug("Failed updating " + getTableName().toLowerCase() + " in database. DatabaseObject=" + databaseObject.toString());
                }
            }
 
            if (commitOnPartialSuccess) {
                isTransactionSuccess = databaseInterface_.endTransaction(true) && isBatchUpdateSuccess;
            }
            else {
                isTransactionSuccess = databaseInterface_.endTransaction(isBatchUpdateSuccess) && isBatchUpdateSuccess;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isTransactionSuccess = false;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        }

        return isTransactionSuccess;
    }
    
    public boolean delete(String sql, Object... parameters) {
        List<Object> parametersList;
        
        try {
            parametersList = new ArrayList<>(Arrays.asList(parameters));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            parametersList = new ArrayList<>();
        }
        
        return genericDmlStatement(sql, parametersList);
    }
    
    public boolean delete(String sql, List<Object> parameters) {
        return genericDmlStatement(sql, parameters);
    }
    
    public boolean genericDmlStatement(String sql, List<Object> parameters) {
        
        boolean isDmlSuccess = true;
        
        try {
            
            if ((sql == null) || sql.isEmpty() || (parameters == null)) {
                return false;
            }
        
            if (!isConnectionValid() || (isConnectionReadOnly() == null) || isConnectionReadOnly()) {
                return false;
            }
            
            databaseInterface_.createPreparedStatement(sql);
            databaseInterface_.addPreparedStatementParameters(parameters);
            databaseInterface_.executePreparedStatement();
            
            if (databaseInterface_.didPreparedStatementExecuteSuccessfully() != null) {
                isDmlSuccess = databaseInterface_.didPreparedStatementExecuteSuccessfully();
            }
            else {
                isDmlSuccess = false;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));           
            isDmlSuccess = false;
        }      
        finally {
            isDmlSuccess = databaseInterface_.endTransactionConditional(isDmlSuccess) && isDmlSuccess;
            databaseInterface_.cleanupAutomatic();
        }
        
        return isDmlSuccess;
    }
    
    public boolean upsert(T databaseObject) {
        
        boolean isTransactionSuccess = false;
        
        if (databaseObject == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }
       
        try {
            if (!databaseInterface_.isManualTransactionControl()) {
                databaseInterface_.beginTransaction();
            }
          
            boolean doInsert = false, doUpdate = false;
            boolean isUpdateSuccess = true, isInsertSuccess = true;

            T databaseObjectFromDatabase = getDatabaseObject(databaseObject);

            boolean areObjectsEqual;
            if ((databaseObjectFromDatabase != null) && databaseObjectFromDatabase.isEqual(databaseObject)) {
                areObjectsEqual = true;
            }
            else {
                areObjectsEqual = false;
            }
            
            if ((databaseObjectFromDatabase != null) && areObjectsEqual) {
                doInsert = false;
                doUpdate = false;
            }
            else if ((databaseObjectFromDatabase != null) && !areObjectsEqual) {
                doUpdate = true;
            }
            else {
                doInsert = true;
            }

            if (doUpdate) {
                isUpdateSuccess = update(databaseObject);
            }
            else if (doInsert) { 
                isInsertSuccess = insert(databaseObject);
            }

            boolean operationSuccess = isUpdateSuccess && isInsertSuccess;
            
            if (databaseInterface_.isManualTransactionControl()) {
                isTransactionSuccess = databaseInterface_.endTransactionConditional(operationSuccess) && operationSuccess;
            }
            else {
                isTransactionSuccess = databaseInterface_.endTransaction(operationSuccess) && operationSuccess;
            }
            
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isTransactionSuccess = false;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        }
        
        return isTransactionSuccess;
    }    
    
    public boolean upsert(List<T> databaseObjects, boolean commitOnPartialSuccess) {
        
        boolean isTransactionSuccess = false;
        
        if (databaseObjects == null) {
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        if (databaseInterface_.isManualTransactionControl()) {
            logger.error("This functional cannot be called when using manual transaction control");
            databaseInterface_.cleanupAutomatic();
            return false;
        }
        
        try {
            databaseInterface_.beginTransaction();
            
            boolean isBatchUpdateSuccess = true, isBatchInsertSuccess = true;
             
            for (T databaseObject : databaseObjects) {     
                boolean doInsert = false, doUpdate = false;
                boolean isUpdateSuccess = true, isInsertSuccess = true;

                T databaseObjectFromDatabase = getDatabaseObject(databaseObject);

                boolean areObjectsEqual;
                if ((databaseObjectFromDatabase != null) && databaseObjectFromDatabase.isEqual(databaseObject)) {
                    areObjectsEqual = true;
                }
                else {
                    areObjectsEqual = false;
                }

                if ((databaseObjectFromDatabase != null) && areObjectsEqual) {
                    doInsert = false;
                    doUpdate = false;
                }
                else if ((databaseObjectFromDatabase != null) && !areObjectsEqual) {
                    doUpdate = true;
                }
                else {
                    doInsert = true;
                }

                if (doUpdate) {
                    isUpdateSuccess = update(databaseObject);
                }
                else if (doInsert) { 
                    isInsertSuccess = insert(databaseObject);
                }
                
                if (!isUpdateSuccess) {
                    isBatchUpdateSuccess = false;
                    logger.debug("Failed upserting in table: " + getTableName());
                }
                
                if (!isInsertSuccess) {
                    isBatchInsertSuccess = false;
                    logger.debug("Failed upserting in table: " + getTableName());
                }
            }                
            
            boolean operationSuccess = isBatchUpdateSuccess && isBatchInsertSuccess;
            
            if (commitOnPartialSuccess) {
                isTransactionSuccess = databaseInterface_.endTransaction(true) && operationSuccess;
            }
            else {
                isTransactionSuccess = databaseInterface_.endTransaction(operationSuccess) && operationSuccess;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isTransactionSuccess = false;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        }
        
        return isTransactionSuccess;
    }
    
    public List<T> processResultSet(ResultSet resultSet) {
        
        try {     
            if ((resultSet == null) || resultSet.isClosed()) {
                return new ArrayList<>();
            }

            List<T> databaseObjects = new ArrayList<>();

            while (resultSet.next()) {
                T databaseObject = processSingleResultAllColumns(resultSet);
                databaseObjects.add(databaseObject);
            }
            
            return databaseObjects;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
    }
    
    public abstract boolean insert(T t);
    
    public abstract boolean update(T t);
    
    public abstract boolean delete(T t);
    
    public abstract T getDatabaseObject(T t);
    
    public abstract T processSingleResultAllColumns(ResultSet result);
    
    public abstract String getTableName();
    
}
