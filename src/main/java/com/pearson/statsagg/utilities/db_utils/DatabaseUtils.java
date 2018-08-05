package com.pearson.statsagg.utilities.db_utils;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class.getName());
    
    public static final byte CLOSE = 1;
    public static final byte CLOSE_AND_COMMIT = 2;
    public static final byte CLOSE_AND_ROLLBACK = 3;
    
    public static void cleanup(Connection connection) {
        cleanup(null, null, null, null, CLOSE);
    }
    
    public static void cleanup(ResultSet results) {
        cleanup(null, null, null, results, CLOSE);
    }
    
    public static void cleanup(Statement statement) {
        cleanup(null, null, statement, null, CLOSE);
    }
    
    public static void cleanup(Statement statement, ResultSet results) {
        cleanup(null, null, statement, results, CLOSE);
    }
    
    public static void cleanup(Connection connection, Statement statement) {
        cleanup(connection, null, statement, null, CLOSE);
    }
    
    public static void cleanup(Connection connection, Statement statement, byte closeMode) {
        cleanup(connection, null, statement, null, closeMode);
    }

    public static void cleanup(Connection connection, Statement statement, ResultSet results) {
        cleanup(connection, null, statement, results, CLOSE);
    }
    
    public static void cleanup(Connection connection, Statement statement, ResultSet results, byte closeMode) {
        cleanup(connection, null, statement, results, closeMode);
    }

    public static void cleanup(PreparedStatement preparedStatement) {
        cleanup(null, preparedStatement, null, null, CLOSE);
    }
    
    public static void cleanup(PreparedStatement preparedStatement, ResultSet results) {
        cleanup(null, preparedStatement, null, results, CLOSE);
    }
    
    public static void cleanup(Connection connection, PreparedStatement preparedStatement) {
        cleanup(connection, preparedStatement, null, null, CLOSE);
    }
    
    public static void cleanup(Connection connection, PreparedStatement preparedStatement, byte closeMode) {
        cleanup(connection, preparedStatement, null, null, closeMode);
    }
    
    public static void cleanup(Connection connection, PreparedStatement preparedStatement, ResultSet results) {
        cleanup(connection, preparedStatement, null, results, CLOSE);
    }
    
    public static void cleanup(Connection connection, PreparedStatement preparedStatement, ResultSet results, byte closeMode) {
        cleanup(connection, preparedStatement, null, results, closeMode);
    }

    public static void cleanup(Connection connection, PreparedStatement preparedStatement, Statement statement, ResultSet results, byte closeMode) {
        
        if (results != null) {
            try {
                results.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                results = null;
            }
        }
        
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                preparedStatement = null;
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                statement = null;
            }
        }
        
        if (connection != null) {
            try {
                if (closeMode == CLOSE_AND_COMMIT) {
                    disconnectAndCommit(connection);
                }
                else if (closeMode == CLOSE_AND_ROLLBACK) {
                    disconnectAndRollback(connection);
                }
                else disconnect(connection);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                connection = null;
            }
        }
    }
    
    public static Connection connect(String jdbcString) {
        
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(jdbcString);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return connection;
    }
    
    public static Connection connect(String url, String username, String password) {
        
        Connection connection = null;
        
        try {
            connection = DriverManager.getConnection(url, username, password);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return connection;
    }
    
    public static boolean disconnect(Connection connection) {
        
        try {
            if (connection != null) connection.close();
            connection = null;
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));            
            connection = null;
            return false;
        }
        
    }
    
    public static boolean disconnectAndCommit(Connection connection) {
        commit(connection);
        return disconnect(connection);
    }
    
    public static boolean disconnectAndRollback(Connection connection) {
        rollback(connection);
        return disconnect(connection);
    }
    
    public static boolean isResultSetValid(ResultSet results) {
        try {
            if ((results == null) || results.isClosed()) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }   
    }
    
    public static Boolean isConnectionReadOnly(Connection connection) {
        try {
            if ((connection == null) || connection.isClosed()) {
                return null;
            }

            return connection.isReadOnly();
        }
        catch (Exception e) {
            return null;
        }
    }
    
    
    public static boolean isConnectionValid(Connection connection) {
        return isConnectionValid(connection, 0);
    }
    
    public static boolean isConnectionValid(Connection connection, int timeoutInSeconds) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            if (connection == null) {
                return false;
            }
            else if (!connection.isValid(timeoutInSeconds)) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            long timeElapsed = System.currentTimeMillis() - startTime;
            
            logger.error("Method=IsConnectionValid" + ", TimeElapsed=" + timeElapsed + ", Exception=" + e.toString() + 
                    System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            return false;
        } 
        
    }
    
    public static void setAutoCommit(Connection connection, boolean enabled) {
        
        if (connection == null) {
            return;
        }
        
        try {
            connection.setAutoCommit(enabled);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static <T extends DatabaseResultSetHandler, S extends DatabaseObject> List<S> query_Statement(Connection connection, T t, String sql) {
        return query_Statement(connection, t, 1000, sql);
    }
    
    // returns null if there was a fatal error. otherwise returns a list of results.
    public static <T extends DatabaseResultSetHandler, S extends DatabaseObject> List<S> query_Statement(Connection connection, T t, int fetchSize, String sql) {
        
        if (connection == null) {
            logger.error("Connection object cannot be null");
            return null;
        }
        
        if ((sql == null) || sql.isEmpty()) {
            logger.error("Sql cannot be null or empty");
            return null;
        }
        
        if (fetchSize < 1) {
            logger.error("fetchSize cannot be less than 1");
            return null;
        }

        List<S> resultsList = new ArrayList<>();
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            statement.setFetchSize(fetchSize);
            resultSet = statement.executeQuery(sql);           
            resultsList = t.handleResultSet(resultSet);
            return resultsList;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
        }      
        finally {
            cleanup(connection, statement, resultSet);
        }
        
        return resultsList;
    }
    
    public static <T extends DatabaseResultSetHandler, S extends DatabaseObject> List<S> query_PreparedStatement(Connection connection, T t, String sql, List<Object> preparedStatementParameters) {
        return query_PreparedStatement(connection, t, 1000, sql, preparedStatementParameters);
    }
    
    public static <T extends DatabaseResultSetHandler, S extends DatabaseObject> List<S> query_PreparedStatement(Connection connection, T t, String sql, Object... preparedStatementParameters) {
        return query_PreparedStatement(connection, t, 1000, sql, getParametersAsListOfObjects(preparedStatementParameters));
    }
    
    // returns null if there was a fatal error. otherwise returns a list of results.
    public static <T extends DatabaseResultSetHandler, S extends DatabaseObject> List<S> query_PreparedStatement(Connection connection, T t, int fetchSize, String sql, List<Object> preparedStatementParameters) {
        
        if (connection == null) {
            logger.error("Connection object cannot be null");
            return null;
        }
        
        if ((sql == null) || sql.isEmpty()) {
            logger.error("Sql cannot be null or empty");
            return null;
        }
        
        if (t == null) {
            logger.error("DatabaseResultSetHandler cannot be null");
            return null;
        }
        
        if (fetchSize < 1) {
            logger.error("fetchSize cannot be less than 1");
            return null;
        }
        
        List<S> resultsList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setFetchSize(fetchSize);
            setPreparedStatementParameters(preparedStatement, preparedStatementParameters);
            resultSet = preparedStatement.executeQuery();
            resultsList = t.handleResultSet(resultSet);
            return resultsList;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
        }      
        finally {
            cleanup(connection, preparedStatement, resultSet);
        }
        
        return resultsList;
    }
    
    public static int dml_PreparedStatement(Connection connection, String sql, List<Object> parameters) {
        return genericDmlStatement_SingleStatement(connection, true, true, sql, parameters);
    }
    
    public static int dml_PreparedStatement(Connection connection, String sql, Object... parameters) {
        return genericDmlStatement_SingleStatement(connection, true, true, sql, getParametersAsListOfObjects(parameters));
    }
    
    public static int dml_PreparedStatement(Connection connection, boolean cleanupOnCompletion, boolean commitOnCompletion, String sql, List<Object> parameters) {
        return genericDmlStatement_SingleStatement(connection, cleanupOnCompletion, commitOnCompletion, sql, parameters);
    }
    
    public static int dml_PreparedStatement(Connection connection, boolean cleanupOnCompletion, boolean commitOnCompletion, String sql, Object... parameters) {
        return genericDmlStatement_SingleStatement(connection, cleanupOnCompletion, commitOnCompletion, sql, getParametersAsListOfObjects(parameters));
    }

    // returns affected row count if successful
    // returns -1 if there was an error. no rollback is performed.
    private static int genericDmlStatement_SingleStatement(Connection connection, boolean cleanupOnCompletion, boolean commitOnCompletion, String sql, List<Object> parameters) {

        if (connection == null) {
            logger.error("Connection object cannot be null");
            return -1;
        }
        
        if ((sql == null) || sql.isEmpty()) {
            logger.error("Sql cannot be null or empty");
            return -1;
        }
        
        PreparedStatement preparedStatement = null;
        
        try {
            preparedStatement = connection.prepareStatement(sql);
            setPreparedStatementParameters(preparedStatement, parameters);
            int affectedRowCounts = preparedStatement.executeUpdate();
            
            if (commitOnCompletion) {
                boolean wasCommitSuccess = commit(connection);
                if (wasCommitSuccess) return affectedRowCounts;
                else return -1;
            }
            else return affectedRowCounts;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
            return -1;
        }      
        finally {
            cleanup(preparedStatement);
            if (cleanupOnCompletion) cleanup(connection, preparedStatement);
        }
        
    }

    public static List<Integer> dml_PreparedStatement_Batch(Connection connection, String sql, List<List<Object>> listOfParameters) {
        return genericDmlStatement_Batch(connection, true, true, Integer.MAX_VALUE, false, sql, listOfParameters);
    }
    
    public static List<Integer> dml_PreparedStatement_Batch(Connection connection, int batchSize, String sql, List<List<Object>> listOfParameters) {
        return genericDmlStatement_Batch(connection, true, true, batchSize, true, sql, listOfParameters);
    }
    
    public static List<Integer> dml_PreparedStatement_Batch(Connection connection, boolean cleanupOnCompletion, boolean commitOnCompletion,
            int batchSize, boolean commitOnEveryBatch, String sql, List<List<Object>> listOfParameters) {
        return genericDmlStatement_Batch(connection, cleanupOnCompletion, commitOnCompletion, batchSize, commitOnEveryBatch, sql, listOfParameters);
    }
    
    // returns a list of affected row counts if successful
    // returns null if there was a fatal error
    private static List<Integer> genericDmlStatement_Batch(Connection connection, boolean cleanupOnCompletion, boolean commitOnCompletion,
            int batchSize, boolean commitOnEveryBatch, String sql, List<List<Object>> listOfParameters) {

        if (connection == null) {
            logger.error("Connection object cannot be null");
            return null;
        }
        
        if ((sql == null) || sql.isEmpty()) {
            logger.error("Sql cannot be null or empty");
            return null;
        }
        
        if (listOfParameters == null){
            logger.error("List of parameters cannot be null");
            return null;
        }
        
        PreparedStatement preparedStatement = null;
        Boolean isAutocommit = null;
        
        try {
            preparedStatement = connection.prepareStatement(sql);
            isAutocommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            
            List<Integer> affectedRowCounts_List = new ArrayList<>();
            List<List<List<Object>>> listOfParameters_Batches = Lists.partition(listOfParameters, batchSize);
            
            for (List<List<Object>> parametersBatch : listOfParameters_Batches) {
                // add the parameters for every record of the batch into the PreparedStatement
                for (List<Object> parameters : parametersBatch) {
                    try {
                        if (parameters == null) setPreparedStatementParameters(preparedStatement, new ArrayList<>());
                        else setPreparedStatementParameters(preparedStatement, parameters);
                        preparedStatement.addBatch();
                    }
                    catch (Exception e) {
                        logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                    }
                    
                    preparedStatement.clearParameters();
                }
                
                // execute the batch of dml & store the results array
                int[] affectedRowCounts = null;
                try {
                    affectedRowCounts = preparedStatement.executeBatch();
                }
                catch (BatchUpdateException bue) { // if there's an error, record which rows had errors & which didn't
                    logger.error(bue.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(bue));  
                    
                    if (bue.getUpdateCounts().length < parametersBatch.size()) {
                        affectedRowCounts = new int[parametersBatch.size()];
                        int i;
                        for (i = 0; i < bue.getUpdateCounts().length; i++) affectedRowCounts[i] = bue.getUpdateCounts()[i];
                        for (; i < affectedRowCounts.length; i++) affectedRowCounts[i] = Statement.EXECUTE_FAILED;
                    }
                    else {
                        affectedRowCounts = bue.getUpdateCounts();
                    }
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
                
                // commit the batch (if configured to do so). if the commit fails, mark every row in the batch as failed in the results.
                if (commitOnEveryBatch && (affectedRowCounts != null)) {
                    boolean wasCommitSuccess = commit(connection, false);
                    
                    if (wasCommitSuccess) {
                        for (int i = 0; i < affectedRowCounts.length; i++) {
                            Integer affectedRowCount = affectedRowCounts[i];
                            affectedRowCounts_List.add(affectedRowCount);
                        }
                    }
                    else {
                        for (int i = 0; i < affectedRowCounts.length; i++) {
                            affectedRowCounts_List.add(Statement.EXECUTE_FAILED);
                        }
                    }
                }
                else if (affectedRowCounts != null) { // we didn't commit, so store the results of the executeBatch call
                    for (int i = 0; i < affectedRowCounts.length; i++) {
                        Integer affectedRowCount = affectedRowCounts[i];
                        affectedRowCounts_List.add(affectedRowCount);
                    }
                }
                else { // the results of the executeBatch command are invalid, set the results for every record for this batch as failed
                    for (int i = 0; i < parametersBatch.size(); i++) {
                        affectedRowCounts_List.add(Statement.EXECUTE_FAILED);
                    }
                }
            }

            // if we need to commit, but opted to not commit on every individual batch, then the commit happens here. if the commit fails, all results will be marked as failed.
            if (!commitOnEveryBatch && commitOnCompletion) {
                boolean wasCommitSuccess = commit(connection, false);
                
                if (wasCommitSuccess) {
                    return affectedRowCounts_List;
                }
                else {
                    for (int i = 0; i < affectedRowCounts_List.size(); i++) {
                        affectedRowCounts_List.clear();
                        affectedRowCounts_List.add(Statement.EXECUTE_FAILED);
                    }
                }
            }
            
            return affectedRowCounts_List;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
            return null;
        }      
        finally {
            // if the connection is not being closed, set autocommit back to whatever it was before this method started
            if (!cleanupOnCompletion) {
                try {
                    if (isAutocommit != null) {
                        connection.setAutoCommit(isAutocommit);
                    }
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e)); 
                }
            }
            
            cleanup(preparedStatement);
            if (cleanupOnCompletion) cleanup(connection, preparedStatement);
        }
        
    }

    public static boolean commit(Connection connection, boolean checkAutocommit) {
        
        if (connection == null) {
            return false;
        }
        
        try {
            if (!checkAutocommit) connection.commit(); 
            else if (!connection.getAutoCommit()) connection.commit(); 
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
    }
    
    public static boolean commit(Connection connection) {
        return commit(connection, true);
    }
    
    public static boolean rollback(Connection connection, boolean checkAutocommit) {
        
        if (connection == null) {
            return false;
        }
        
        try {
            if (!checkAutocommit) connection.rollback(); 
            else if (!connection.getAutoCommit()) connection.rollback(); 
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    }
    
    public static boolean rollback(Connection connection) {
        return rollback(connection, true);
    }
    
    public static boolean setPreparedStatementParameters(PreparedStatement preparedStatement, List<Object> preparedStatementParameters) {
        
        if (preparedStatement == null) {
            logger.warn("Can't set PreparedStatementParameters - PreparedStatement is null");
            return false;
        } 
        
        if (preparedStatementParameters == null) {
            preparedStatementParameters = new ArrayList<>();
        }
        
        boolean wasSuccess = true;
        
        try {
            AtomicInteger index = new AtomicInteger(1);

            for (Object object : preparedStatementParameters) {
                int result = setPreparedStatementParameter(preparedStatement, preparedStatementParameters, object, index);
                if (result == -1) wasSuccess = false;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }   
        
        return wasSuccess;
    }
    
    private static int setPreparedStatementParameter(PreparedStatement preparedStatement, List<Object> preparedStatementParameters, Object object, AtomicInteger index) {
        
        if ((preparedStatement == null) || (preparedStatementParameters == null)) {
            logger.warn("Can't set preparedStatementParameters - preparedStatementParameters or preparedStatement is null");
            return -1;
        } 

        try {
            if (object == null) {
                preparedStatement.setObject(index.getAndIncrement(), null);
            } 
            else if (object instanceof BigDecimal) {
                preparedStatement.setBigDecimal(index.getAndIncrement(), (BigDecimal) object);
            }
            else if (object instanceof Blob) {
                preparedStatement.setBlob(index.getAndIncrement(), (Blob) object);
            }
            else if (object instanceof Boolean) {
                preparedStatement.setBoolean(index.getAndIncrement(), (Boolean) object);
            }
            else if (object instanceof Byte) {
                preparedStatement.setByte(index.getAndIncrement(), (Byte) object);
            }
            else if (object instanceof byte[]) {
                preparedStatement.setBytes(index.getAndIncrement(), (byte[]) object);
            }
            else if (object instanceof Clob) {
                preparedStatement.setClob(index.getAndIncrement(), (Clob) object);
            }
            else if (object instanceof Double) {
                preparedStatement.setDouble(index.getAndIncrement(), (Double) object);
            }
            else if (object instanceof Float) {
                preparedStatement.setFloat(index.getAndIncrement(), (Float) object);
            }
            else if (object instanceof Integer) {
                preparedStatement.setInt(index.getAndIncrement(), (Integer) object);
            }
            else if (object instanceof List) {
                for (Object listObject : (List) object) {
                    setPreparedStatementParameter(preparedStatement, preparedStatementParameters, listObject, index);
                }
            }
            else if (object instanceof Long) {
                preparedStatement.setLong(index.getAndIncrement(), (Long) object);
            }
            else if (object instanceof NClob) {
                preparedStatement.setNClob(index.getAndIncrement(), (NClob) object);
            }
            else if (object instanceof Ref) {
                preparedStatement.setRef(index.getAndIncrement(), (Ref) object);
            }
            else if (object instanceof RowId) {
                preparedStatement.setRowId(index.getAndIncrement(), (RowId) object);
            }
            else if (object instanceof SQLXML) {
                preparedStatement.setSQLXML(index.getAndIncrement(), (SQLXML) object);
            }
            else if (object instanceof Short) {
                preparedStatement.setShort(index.getAndIncrement(), (Short) object);
            }
            else if (object instanceof String) {
                preparedStatement.setString(index.getAndIncrement(), (String) object);
            }
            else if (object instanceof Time) {
                preparedStatement.setTime(index.getAndIncrement(), (Time) object);
            }
            else if (object instanceof java.sql.Timestamp) {
                preparedStatement.setTimestamp(index.getAndIncrement(), (java.sql.Timestamp) object);
            }
            else if (object instanceof java.sql.Date) {
                preparedStatement.setDate(index.getAndIncrement(), (java.sql.Date) object);
            }
            else if (object instanceof java.util.Date) {
                java.util.Date tempDate = (java.util.Date) object;
                java.sql.Date dateSql = new java.sql.Date(tempDate.getTime());
                preparedStatement.setDate(index.getAndIncrement(), dateSql);
            }
            else {
                if (object instanceof Object) {}
                else {
                    logger.warn("Setting PreparedStatement parameter to 'object' type when object is not an object type");
                }
                
                preparedStatement.setObject(index.getAndIncrement(), object);
            }
            
            return index.get();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return -1;
        }  
        
    }

    public static String getStringFromClob(Clob clob) {
        return getStringFromClob(clob, 8192);
    }
    
    public static String getStringFromClob(Clob clob, int bufferSize) {
        
        if (clob == null) {
            return null;
        }
        
        StringBuilder string = new StringBuilder();
        
        BufferedReader bufferedReader = null;
        
        try {
            bufferedReader = new BufferedReader(clob.getCharacterStream());

            int charactersReadCount = 0;
            char[] buffer = new char[bufferSize];

            while ((charactersReadCount = bufferedReader.read(buffer)) > 0) {
                string.append(buffer, 0, charactersReadCount);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return string.toString();
    }
    
    private static List<Object> getParametersAsListOfObjects(Object... parameters) {
        
        List<Object> parametersList;
        
        try {
            parametersList = new ArrayList<>(Arrays.asList(parameters));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            parametersList = new ArrayList<>();
        }
        
        return parametersList;
    }
    
}
