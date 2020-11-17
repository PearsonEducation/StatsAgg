package com.pearson.statsagg.globals;

import com.pearson.statsagg.configuration.DatabaseConfiguration;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseConnections {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnections.class.getName());

    private static HikariDataSource connectionPool_ = null;
    
    public static void setDriver() {
        String driver = null;
        
        try {      
            
            if (DatabaseConfiguration.getType() == DatabaseConfiguration.DERBY_EMBEDDED) {
                driver = "org.apache.derby.jdbc.EmbeddedDriver";
            }
            else if (DatabaseConfiguration.getType() == DatabaseConfiguration.DERBY_NETWORK) {
                driver = "org.apache.derby.jdbc.ClientDriver";
            }            
            else if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
                driver = "com.mysql.jdbc.Driver";
            }
            
            Class.forName(driver).newInstance();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
    }
    
    public static boolean connectToDatabase(HikariConfig hikariConfig) {
        
        try {
            connectionPool_ = new HikariDataSource(hikariConfig);
            
            if (connectionPool_.getConnection().isValid(30)) {
                logger.info("Successfully connected to database @ " + connectionPool_.getJdbcUrl());
                return true;
            }
            else {
                connectionPool_.close();
                return false;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            try {
                if (connectionPool_ != null) {
                    connectionPool_.close();
                }
            }
            catch (Exception e2) {}
            
            return false;
        }
        
    }
    
    public static Connection getConnection() {
        
        if ((connectionPool_ == null) || !connectionPool_.isRunning()) {
            return null;
        }
        
        return DatabaseUtils.getConnection(connectionPool_);
    }
    
    public static Connection getConnection(boolean autoCommit) {
        
        if ((connectionPool_ == null) || !connectionPool_.isRunning()) {
            return null;
        }
        
        Connection connection = DatabaseUtils.getConnection(connectionPool_);
        DatabaseUtils.setAutoCommit(connection, autoCommit);
        
        return connection;
    }
    
    public static DataSource getDatasource() {
        return connectionPool_;
    }
    
    public static void disconnectAndShutdown() {

        try {
            if (connectionPool_ != null) {
                connectionPool_.close();
                connectionPool_ = null;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        if (DatabaseConfiguration.getType() == DatabaseConfiguration.DERBY_EMBEDDED) {
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true;deregister=true");
            }
            catch (Exception e) {
                if (e.toString().contains("java.sql.SQLException: Derby system shutdown")) {
                    logger.info("Database successfully shutdown");
                }
                else if (e.toString().contains("No suitable driver found for jdbc:derby")) {
                    logger.info("No suitable driver found for jdbc:derby");
                }
                else {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
    }

}
