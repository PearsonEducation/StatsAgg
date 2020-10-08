package com.pearson.statsagg.database_objects;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DDL_Helper {
    
    private static final Logger logger = LoggerFactory.getLogger(DDL_Helper.class.getName());

    public static boolean createTable(Connection connection, boolean closeConnectionOnCompletion, List<String> ddlStatements, String tableName) {
        boolean isAllSuccess = true;
        
        try {
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();

            try {
                for (String ddlStatement : ddlStatements) {
                    DatabaseUtils.createStatement(connection).execute(ddlStatement);
                }
            }
            catch (Exception e) {
                if (e.toString().contains("already exists") && (e.toString().contains("java.sql.SQLException: Table/View") || e.toString().contains("MySQLSyntaxErrorException: Table"))) {
                    logger.warn("Table " + tableName + " already exists.");
                }
                else {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
            
            if (isAllSuccess) DatabaseUtils.commit(connection);
            else DatabaseUtils.rollback(connection);
            
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
        return isAllSuccess;
    }
    
    public static boolean genericDDL(Connection connection, boolean closeConnectionOnCompletion, List<String> ddlStatements) {
        boolean isAllSuccess = true;
        
        try {
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();

            try {
                for (String ddlStatement : ddlStatements) {
                    DatabaseUtils.createStatement(connection).execute(ddlStatement);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
            if (isAllSuccess) DatabaseUtils.commit(connection);
            else DatabaseUtils.rollback(connection);
            
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
        return isAllSuccess;
    }
    
}
