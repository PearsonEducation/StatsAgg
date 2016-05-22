package com.pearson.statsagg.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class.getName());
    
    public static void cleanup(Connection connection, PreparedStatement preparedStatement) {
        cleanup(connection, preparedStatement, null, null);
    }
    
    public static void cleanup(PreparedStatement preparedStatement) {
        cleanup(null, preparedStatement, null, null);
    }
    
    public static void cleanup(PreparedStatement preparedStatement, ResultSet results) {
        cleanup(null, preparedStatement, null, results);
    }

    public static void cleanup(Connection connection, PreparedStatement preparedStatement, ResultSet results) {
        cleanup(connection, preparedStatement, null, results);
    }
    
    public static void cleanup(Connection connection, Statement statement) {
        cleanup(connection, null, statement, null);
    }
    
    public static void cleanup(Statement statement) {
        cleanup(null, null, statement, null);
    }
    
    public static void cleanup(Statement statement, ResultSet results) {
        cleanup(null, null, statement, results);
    }
    
    public static void cleanup(Connection connection, Statement statement, ResultSet results) {
        cleanup(connection, null, statement, results);
    }
    
    public static void cleanup(ResultSet results) {
        cleanup(null, null, null, results);
    }
    
    public static void cleanup(Connection connection, PreparedStatement preparedStatement, Statement statement, ResultSet results) {
        
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
                disconnect(connection);
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
            if (connection != null) {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));            
        }
       
        try {
            if (connection != null) {
                connection.close();
            }
            
            connection = null;
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));            
            
            connection = null;
            return false;
        }
        
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
                connection.close();
                return false;
            }
            else {
                return true;
            }
        }
        catch (Exception e) {
            try {
                connection.close();
            }
            catch (Exception e2) {}
            
            long timeElapsed = System.currentTimeMillis() - startTime;
            
            logger.error("Method=IsConnectionValid" + ", TimeElapsed=" + timeElapsed + ", Exception=" + e.toString() + 
                    System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            return false;
        }   
    }
    
}
