package com.pearson.statsagg.database_engine;

import com.pearson.statsagg.globals.DatabaseConnections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseDao {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDao.class.getName());
    
    protected DatabaseInterface databaseInterface_ = null;
    private boolean isConnectionValid_ = false;
    private Boolean isConnectionReadOnly_ = null;
    
    public DatabaseDao() {
        databaseInterface_ = new DatabaseInterface(DatabaseConnections.getConnection());   
        isConnectionValid_ = databaseInterface_.isConnectionValid();
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public DatabaseDao(Integer validityCheckTimeout) {
        databaseInterface_ = new DatabaseInterface(DatabaseConnections.getConnection());
        databaseInterface_.setValidityCheckTimeout(validityCheckTimeout);
        isConnectionValid_ = databaseInterface_.isConnectionValid(validityCheckTimeout);
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public DatabaseDao(boolean closeConnectionAfterOperation) {
        databaseInterface_ = new DatabaseInterface(DatabaseConnections.getConnection());   
        isConnectionValid_ = databaseInterface_.isConnectionValid();
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public DatabaseDao(boolean closeConnectionAfterOperation, Integer validityCheckTimeout) {
        databaseInterface_ = new DatabaseInterface(DatabaseConnections.getConnection());   
        databaseInterface_.setValidityCheckTimeout(validityCheckTimeout);
        isConnectionValid_ = databaseInterface_.isConnectionValid(validityCheckTimeout);
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseDao databaseDao) {
        
        if (databaseDao == null) {
            return;
        }
        
        databaseInterface_ = databaseDao.getDatabaseInterface();  
        databaseInterface_.cleanupForNextStatement();
        isConnectionValid_ = databaseDao.isConnectionValid();
        isConnectionReadOnly_ = databaseDao.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseInterface databaseInterface) {
        
        if (databaseInterface == null) {
            return;
        }
        
        databaseInterface_ = databaseInterface;  
        databaseInterface_.cleanupForNextStatement();
        isConnectionValid_ = databaseInterface_.isConnectionValid();
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseDao databaseDao, Integer validityCheckTimeout) {
        
        if (databaseDao == null) {
            return;
        }
        
        databaseInterface_ = databaseDao.getDatabaseInterface();  
        databaseInterface_.cleanupForNextStatement();
        databaseInterface_.setValidityCheckTimeout(validityCheckTimeout);
        isConnectionValid_ = databaseDao.isConnectionValid();
        isConnectionReadOnly_ = databaseDao.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseInterface databaseInterface, Integer validityCheckTimeout) {
        
        if (databaseInterface == null) {
            return;
        }
        
        databaseInterface_ = databaseInterface;  
        databaseInterface_.cleanupForNextStatement();
        databaseInterface_.setValidityCheckTimeout(validityCheckTimeout);
        isConnectionValid_ = databaseInterface_.isConnectionValid(validityCheckTimeout);
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseDao databaseDao, boolean closeConnectionAfterOperation) {
        
        if (databaseDao == null) {
            return;
        }
        
        databaseInterface_ = databaseDao.getDatabaseInterface();  
        databaseInterface_.cleanupForNextStatement();
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
        isConnectionValid_ = databaseDao.isConnectionValid();
        isConnectionReadOnly_ = databaseDao.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseInterface databaseInterface, boolean closeConnectionAfterOperation) {
        
        if (databaseInterface == null) {
            return;
        }
        
        databaseInterface_ = databaseInterface;  
        databaseInterface_.cleanupForNextStatement();
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
        isConnectionValid_ = databaseInterface_.isConnectionValid();
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseDao databaseDao, boolean closeConnectionAfterOperation, Integer validityCheckTimeout) {
        
        if (databaseDao == null) {
            return;
        }
        
        databaseInterface_ = databaseDao.getDatabaseInterface();  
        databaseInterface_.cleanupForNextStatement();
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
        databaseInterface_.setValidityCheckTimeout(validityCheckTimeout);
        isConnectionValid_ = databaseDao.isConnectionValid();
        isConnectionReadOnly_ = databaseDao.isConnectionReadOnly();
    }
    
    public DatabaseDao(DatabaseInterface databaseInterface, boolean closeConnectionAfterOperation, Integer validityCheckTimeout) {
        
        if (databaseInterface == null) {
            return;
        }
        
        databaseInterface_ = databaseInterface;  
        databaseInterface_.cleanupForNextStatement();
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
        databaseInterface_.setValidityCheckTimeout(validityCheckTimeout);
        isConnectionValid_ = databaseInterface_.isConnectionValid(validityCheckTimeout);
        isConnectionReadOnly_ = databaseInterface_.isConnectionReadOnly();
    }
    
    public void reset() {
        boolean closeConnectionAfterOperation = databaseInterface_.isCloseConnectionAfterOperation();
        
        databaseInterface_.close();
        databaseInterface_ = null;
        
        databaseInterface_ = new DatabaseInterface(DatabaseConnections.getConnection());  
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
        
        isConnectionValid_ = false;
        isConnectionReadOnly_ = null;
    }
    
    public void close() {
        databaseInterface_.close();
        databaseInterface_ = null;
        
        isConnectionValid_ = false;
        isConnectionReadOnly_ = null;
    }
    
    public DatabaseInterface getDatabaseInterface() {
        return databaseInterface_;
    }

    public boolean isConnectionValid() {
        return isConnectionValid_;
    }
    
    public Boolean isConnectionReadOnly() {
        return isConnectionReadOnly_;
    }
    
} 
