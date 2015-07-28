package com.pearson.statsagg.database_engine;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.StackTrace;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseCleanup {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanup.class.getName());
    
    public static void cleanup(DatabaseInterface databaseInterface) {
        if (databaseInterface.getPreparedStatementParameters() != null) {
            databaseInterface.getPreparedStatementParameters().clear();
        }
        
        cleanup(databaseInterface.getConnection(), databaseInterface.getPreparedStatement(), databaseInterface.getStatement(), databaseInterface.getResults());
    }
    
    public static void cleanup(DatabaseInterface databaseInterface, boolean closeConnection) {
        if (databaseInterface.getPreparedStatementParameters() != null) {
            databaseInterface.getPreparedStatementParameters().clear();
        }
        
        if (closeConnection) {
            cleanup(databaseInterface.getConnection(), databaseInterface.getPreparedStatement(), databaseInterface.getStatement(), databaseInterface.getResults());
        }
        else {
            cleanup(null, databaseInterface.getPreparedStatement(), databaseInterface.getStatement(), databaseInterface.getResults());
        }
    }
    
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
                DatabaseConnections.disconnect(connection);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                connection = null;
            }
        }
    }
    
}
