package com.pearson.statsagg.database;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import java.sql.Clob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInterface.class.getName());
    
    private Connection connection_ = null;
    private PreparedStatement preparedStatement_ = null;
    private List<Object> preparedStatementParameters_ = null;
    private Statement statement_ = null;
    private ResultSet results_ = null;
    private Boolean didPreparedStatementExecuteSuccessfully_ = null;
    private boolean isManualTransactionControl_ = false;
    private boolean isTransactionOpen_ = false;
    private boolean closeConnectionAfterOperation_ = true;
    private Integer validityCheckTimeout_ = null;
    
    public DatabaseInterface(Connection connection) {
        connection_ = connection;
    }

    public void beginTransaction() {
        
        if (connection_ == null) {
            return;
        }
        
        try {           
            if (connection_.getAutoCommit()) {
                logger.warn("Cannot being a transaction when autocommit mode is enabled");
            }
            else {
                if (!isTransactionOpen_) {
                    isTransactionOpen_ = true;
                }
                else {
                    logger.debug("Request to being a transaction on a DatabaseInterface when transaction is already open");
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }   
    }
    
    public void cleanupAutomatic() {
        if (!isTransactionOpen_ && closeConnectionAfterOperation_) {
            close();
        }
        else {
            cleanupForNextStatement();
        }
    }

    public void cleanupForNextStatement() {
        DatabaseCleanup.cleanup(statement_);
        DatabaseCleanup.cleanup(preparedStatement_);
        DatabaseCleanup.cleanup(results_);
         
        if (preparedStatementParameters_ != null) {
            preparedStatementParameters_.clear();
        }
        
        didPreparedStatementExecuteSuccessfully_ = null;
    }
    
    public void close() {
        
        if (isTransactionOpen_ == true) {
            logger.warn("Closing a DatabaseInterface with an open transaction. Rolling back transaction...");
            logger.warn(StackTrace.getStringFromStackTrace(Thread.currentThread().getStackTrace()));
            rollback();
        }
        
        if (preparedStatementParameters_ != null) {
            preparedStatementParameters_.clear();
            preparedStatementParameters_ = null;
        }

        didPreparedStatementExecuteSuccessfully_ = null;
        
        DatabaseCleanup.cleanup(this);
    }
    
    public Statement createStatement() {
        try {
            if ((connection_ == null) || connection_.isClosed()) {
                return null;
            }

            statement_ = connection_.createStatement();
            return statement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public Statement createStatement(int fetchSize) {
        try {
            if ((connection_ == null) || connection_.isClosed() || (fetchSize <= 0)) {
                return null;
            }

            statement_ = connection_.createStatement();
            statement_.setFetchSize(fetchSize);
            return statement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public Statement setStatement(Statement statement) {
        try {
            if (connection_ == null) {
                return null;
            }
   
            statement_ = statement;
            return statement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public Statement setStatement(Statement statement, int fetchSize) {
        try {
            if (connection_ == null) {
                return null;
            }

            statement_ = statement;
            statement_.setFetchSize(fetchSize);
            return statement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public PreparedStatement createPreparedStatement(String sql) {
        try {
            if ((connection_ == null) || connection_.isClosed()) {
                return null;
            }
   
            preparedStatement_ = connection_.prepareStatement(sql);
            preparedStatementParameters_ = new ArrayList<>();
            
            return preparedStatement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public PreparedStatement createPreparedStatement(String sql, List<Object> preparedStatementParameters) {
        try {
            if ((connection_ == null) || connection_.isClosed()) {
                return null;
            }
   
            preparedStatement_ = connection_.prepareStatement(sql);
            preparedStatementParameters_ = preparedStatementParameters;
            
            return preparedStatement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public PreparedStatement createPreparedStatement(String sql, int fetchSize) {
        try {
            if ((connection_ == null) || connection_.isClosed()) {
                return null;
            }
   
            preparedStatement_ = connection_.prepareStatement(sql);
            preparedStatement_.setFetchSize(fetchSize);
            preparedStatementParameters_ = new ArrayList<>();
            return preparedStatement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public PreparedStatement createPreparedStatement(String sql, int fetchSize, List<Object> preparedStatementParameters) {
        try {
            if ((connection_ == null) || connection_.isClosed()) {
                return null;
            }
   
            preparedStatement_ = connection_.prepareStatement(sql);
            preparedStatement_.setFetchSize(fetchSize);
            preparedStatementParameters_ = preparedStatementParameters;
            return preparedStatement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public PreparedStatement setPreparedStatement(PreparedStatement preparedStatement) {
        try {
            if (connection_ == null) {
                return null;
            }
   
            preparedStatement_ = preparedStatement;
            preparedStatementParameters_ = new ArrayList<>();
            return preparedStatement_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public ResultSet executePreparedStatement() {
        try {
            if ((connection_ == null) || (preparedStatement_ == null)) {
                return null;
            }
   
            if (results_ != null) {
                if (!results_.isClosed()) {
                    logger.error("Cannot execute PreparedStatement - DatabaseInterface already has an open ResultSet");
                    return null;
                }
            }
            
            setPreparedStatementParameters();
            
            boolean result = preparedStatement_.execute();
            
            if (result) {
                results_ = preparedStatement_.getResultSet();
            }
             
            didPreparedStatementExecuteSuccessfully_ = true;
            return results_;
        }
        catch (Exception e) {
            didPreparedStatementExecuteSuccessfully_ = false;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }   
    }
    
    public boolean executeBatchPreparedStatement() {
        try {
            if ((connection_ == null) || (preparedStatement_ == null)) {
                return false;
            }
   
            if (results_ != null) {
                if (!results_.isClosed()) {
                    logger.error("Cannot execute PreparedStatement - DatabaseInterface already has an open ResultSet");
                    return false;
                }
            }
            
            int[] results = preparedStatement_.executeBatch();

            if (results == null || (results.length == 0)) {
                logger.warn("PreparedStatement_ExecuteBatch command did not execute any statements");
            }
            else {
                for (int resultIndex : results) {
                    if (results[resultIndex] == PreparedStatement.EXECUTE_FAILED) {
                        logger.error("PreparedStatement executeBatch failed");
                        return false;
                    }
                }
            }
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }   
    }
    
    public List<Object> addPreparedStatementParameters(Object... objects) {
        try {
            if ((connection_ == null) || (preparedStatement_ == null) || (preparedStatementParameters_ == null) || (objects == null)) {
                return new ArrayList<>();
            } 
            
            preparedStatementParameters_.addAll(Arrays.asList(objects));
            
            return preparedStatementParameters_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }  
    }
    
    public List<Object> addPreparedStatementParameters(List<Object> objects) {
        try {
            if ((connection_ == null) || (preparedStatement_ == null) || (preparedStatementParameters_ == null) || (objects == null)) {
                return new ArrayList<>();
            } 
            
            preparedStatementParameters_.addAll(objects);
            
            return preparedStatementParameters_;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }  
    }
    
    public boolean commit() {
        
        if (connection_ == null) {
            return false;
        }
        
        try {
            if (!connection_.getAutoCommit()) {
                connection_.commit();
            }
            else {
                logger.debug("Cannot commit when Auto-Commit is enabled");
            }

            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {       
            isTransactionOpen_ = false;
        }
    }
    
    public void rollback() {
        
        if (connection_ == null) {
            return;
        }
        
        try {
            if (!connection_.getAutoCommit()) {
                connection_.rollback();
            }
            else {
                logger.debug("Cannot rollback when Auto-Commit is enabled");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {       
            isTransactionOpen_ = false;
        }
    }
    
    public boolean endTransaction(boolean commitTransaction) {
        
        boolean endTransactionSuccess = false;
        
        if (commitTransaction) {
            endTransactionSuccess = commit();
        }
        else {
            rollback();
        }
        
        isTransactionOpen_ = false;
        return endTransactionSuccess;
    }
    
    public boolean endTransactionConditional(boolean commitTransaction) {
        
        boolean endTransactionSuccess = true;
        
        if (commitTransaction && !isTransactionOpen()) {
            endTransactionSuccess = commit();
        }
        else if (!isTransactionOpen()) {
            rollback();
        }
        
        return endTransactionSuccess;
    }
    
    public Boolean isConnectionReadOnly() {
        try {
            if ((connection_ == null) || connection_.isClosed()) {
                return null;
            }

            return connection_.isReadOnly();
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public boolean isConnectionValid() {
        if ((validityCheckTimeout_ == null) || (validityCheckTimeout_ < 0)) {
            return isConnectionValid(0);
        }
        else {
            return isConnectionValid(validityCheckTimeout_);
        }
    }
    
    public boolean isConnectionValid(int timeoutInSeconds) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            if (connection_ == null) {
                return false;
            }
            else if (!connection_.isValid(timeoutInSeconds)) {
                connection_.close();
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            try {
                connection_.close();
            }
            catch (Exception e2) {}
            
            long timeElapsed = System.currentTimeMillis() - startTime;
            
            logger.error("Method=IsConnectionValid" + ", TimeElapsed=" + timeElapsed + ", Exception=" + e.toString() + 
                    System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            return false;
        }   
    }
    
    public boolean isResultSetValid() {
        try {
            if ((results_ == null) || results_.isClosed()) {
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
    
    // this method is a work in progress. additional functionality will be added as needed.
    private int setPreparedStatementParameter(Object object, int index) {
        
        if ((preparedStatement_ == null) || (preparedStatementParameters_ == null)) {
            logger.warn("Can't set preparedStatementParameters - preparedStatementParameters or preparedStatement is null");
            return -1;
        } 
        
        try {
            if (object == null) {
                preparedStatement_.setObject(index++, null);
            } 
            else if (object instanceof BigDecimal) {
                preparedStatement_.setBigDecimal(index++, (BigDecimal) object);
            }
            else if (object instanceof Blob) {
                preparedStatement_.setBlob(index++, (Blob) object);
            }
            else if (object instanceof Boolean) {
                preparedStatement_.setBoolean(index++, (Boolean) object);
            }
            else if (object instanceof Byte) {
                preparedStatement_.setByte(index++, (Byte) object);
            }
            else if (object instanceof byte[]) {
                preparedStatement_.setBytes(index++, (byte[]) object);
            }
            else if (object instanceof Clob) {
                preparedStatement_.setClob(index++, (Clob) object);
            }
            else if (object instanceof Double) {
                preparedStatement_.setDouble(index++, (Double) object);
            }
            else if (object instanceof Float) {
                preparedStatement_.setFloat(index++, (Float) object);
            }
            else if (object instanceof Integer) {
                preparedStatement_.setInt(index++, (Integer) object);
            }
            else if (object instanceof List) {
                for (Object listObject : (List) object) {
                    setPreparedStatementParameter(listObject, index++);
                }
            }
            else if (object instanceof Long) {
                preparedStatement_.setLong(index++, (Long) object);
            }
            else if (object instanceof Short) {
                preparedStatement_.setShort(index++, (Short) object);
            }
            else if (object instanceof String) {
                preparedStatement_.setString(index++, (String) object);
            }
            else if (object instanceof java.sql.Timestamp) {
                preparedStatement_.setTimestamp(index++, (java.sql.Timestamp) object);
            }
            else if (object instanceof java.sql.Date) {
                preparedStatement_.setDate(index++, (java.sql.Date) object);
            }
            else if (object instanceof java.util.Date) {
                java.util.Date tempDate = (java.util.Date) object;
                java.sql.Date dateSql = new java.sql.Date(tempDate.getTime());
                preparedStatement_.setDate(index++, dateSql);
            }
            else {
                if (object instanceof Object) {}
                else {
                    logger.warn("Setting PreparedStatement parameter to 'object' type when object is not an object type");
                }
                
                preparedStatement_.setObject(index++, object);
            }
            
            return index;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return -1;
        }  
    }
    
    public void setPreparedStatementParameters() {
        
        if ((preparedStatement_ == null) || (preparedStatementParameters_ == null)) {
            logger.warn("Can't set preparedStatementParameters - preparedStatementParameters or preparedStatement is null");
            return;
        } 
        
        try {
            int index = 1;

            for (Object object : preparedStatementParameters_) {
                int incrementedIndex = setPreparedStatementParameter(object, index);
                index = incrementedIndex;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return;
        }   
    }

    public static String getStringFromClob(Clob clob) {
        return getStringFromClob(clob, 8192);
    }
    
    public static String getStringFromClob(Clob clob, int bufferSize) {
        
        if (clob == null) {
            return null;
        }
        
        StringBuilder string = new StringBuilder("");
        
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
    
    public Connection getConnection() {
        return connection_;
    }
    
    public Statement getStatement() {
        return statement_;
    }
    
    public PreparedStatement getPreparedStatement() {
        return preparedStatement_;
    }
    
    public List<Object> getPreparedStatementParameters() {
        return preparedStatementParameters_;
    }
    
    public ResultSet getResults() {
        return results_;
    }
    
    public Boolean didPreparedStatementExecuteSuccessfully() {
        return didPreparedStatementExecuteSuccessfully_;
    }
    
    public boolean isManualTransactionControl() {
        return isManualTransactionControl_;
    }
    
    public void setIsManualTransactionControl(boolean isManualTransactionControl) {
        this.isManualTransactionControl_ = isManualTransactionControl;
    }
    
    public boolean isTransactionOpen() {
        return isTransactionOpen_;
    }
    
    public void setTransactionOpen(boolean isTransactionOpen) {
        this.isTransactionOpen_ = isTransactionOpen;
    }
    
    public boolean isCloseConnectionAfterOperation() {
        return closeConnectionAfterOperation_;
    }
    
    public void setCloseConnectionAfterOperation(boolean closeConnectionAfterOperation) {
        this.closeConnectionAfterOperation_ = closeConnectionAfterOperation;
    }
    
    public Integer getValidityCheckTimeout() {
        return validityCheckTimeout_;
    }

    public void setValidityCheckTimeout(Integer validityCheckTimeout) {
        this.validityCheckTimeout_ = validityCheckTimeout;
    }
    
}
