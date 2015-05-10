package com.pearson.statsagg.globals;

import com.pearson.statsagg.utilities.PropertiesConfigurationWrapper;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseConfiguration {
   
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class.getName());
    
    public static final int VALUE_NOT_SET_CODE = -4444;
    
    public static final int DERBY_EMBEDDED = 1;
    public static final int DERBY_NETWORK = 2;
    public static final int MYSQL = 3;
    public static final int POSTGRES = 4;
    
    private static boolean isInitializeSuccess_ = false;
    private static PropertiesConfigurationWrapper databaseConfiguration_ = null;
    
    private static int cpMaxConnections_ = VALUE_NOT_SET_CODE;
    private static int cpAcquireRetryAttempts_ = VALUE_NOT_SET_CODE;
    private static int cpAcquireRetryDelay_ = VALUE_NOT_SET_CODE;
    private static int cpConnectionTimeout_ = VALUE_NOT_SET_CODE;
    private static boolean cpEnableStatistics_ = false;
    private static boolean cpDefaultAutoCommit_ = false;
    private static int connectionValidityCheckTimeout_ = VALUE_NOT_SET_CODE;
    
    private static int type_ = VALUE_NOT_SET_CODE;
    private static String typeString_ = null;
    private static String hostname_ = null;
    private static String port_ = null;
    private static String databaseLocalPath_ = null;
    private static String databaseName_ = null;
    private static String username_ = null;
    private static String password_ = null;    
    private static String attributes_ = null;
    private static String customJdbc_ = null;
    
    private static String jdbcConnectionString_ = null;
 
    public static boolean initialize(String filePathAndFilename) {
        
        if (filePathAndFilename == null) {
            return false;
        }
        
        databaseConfiguration_ = new PropertiesConfigurationWrapper(filePathAndFilename);
        
        if ((databaseConfiguration_ == null) || !databaseConfiguration_.isValid()) {
            return false;
        }

        isInitializeSuccess_ = setDatabaseConfigurationValues();
        
        jdbcConnectionString_ = createJdbcString();
                
        return isInitializeSuccess_ && (jdbcConnectionString_ != null);
    }
    
    public static boolean initialize(InputStream configurationInputStream) {
        
        if (configurationInputStream == null) {
            return false;
        }
        
        databaseConfiguration_ = new PropertiesConfigurationWrapper(configurationInputStream);
        
        if ((databaseConfiguration_ == null) || !databaseConfiguration_.isValid()) {
            return false;
        }
        
        isInitializeSuccess_ = setDatabaseConfigurationValues();
        
        jdbcConnectionString_ = createJdbcString();
                
        return isInitializeSuccess_ && (jdbcConnectionString_ != null);
    }
    
    private static boolean setDatabaseConfigurationValues() {
        
        try {
            // determine database type
            typeString_ = databaseConfiguration_.safeGetString("db_type", "");
            
            if (typeString_ != null) {
                if (typeString_.equalsIgnoreCase("derby_network")) type_ = DERBY_NETWORK;
                else if (typeString_.equalsIgnoreCase("derby_embedded")) type_ = DERBY_EMBEDDED;
                else if (typeString_.equalsIgnoreCase("mysql")) type_ = MYSQL;
                else if (typeString_.equalsIgnoreCase("postgres")) type_ = POSTGRES;
            }
            
            // load db_localpath first so it can be used by other variables
            databaseLocalPath_ = databaseConfiguration_.safeGetString("db_localpath", "");
            databaseName_ = databaseConfiguration_.safeGetString("db_name", "");
            
            // Variable substitution
            Iterator<String> keys = databaseConfiguration_.getPropertiesConfiguration().getKeys();
            while (keys.hasNext()) {
                String databasePropertyKey = keys.next();
                
                String databasePropertyValue = databaseConfiguration_.safeGetString(databasePropertyKey, null);
                databasePropertyValue = databasePropertyValue.replace("${db_localpath}", databaseLocalPath_);
                databaseConfiguration_.getPropertiesConfiguration().setProperty(databasePropertyKey, databasePropertyValue);
                
                databasePropertyValue = databaseConfiguration_.safeGetString(databasePropertyKey, null);
                databasePropertyValue = databasePropertyValue.replace("${db_name}", databaseName_);
                databaseConfiguration_.getPropertiesConfiguration().setProperty(databasePropertyKey, databasePropertyValue);
                
                databasePropertyValue = databaseConfiguration_.safeGetString(databasePropertyKey, null);
                databasePropertyValue = databasePropertyValue.replace("${file.separator}", File.separator);
                databaseConfiguration_.getPropertiesConfiguration().setProperty(databasePropertyKey, databasePropertyValue);
            }
            
            // Connection pool
            cpMaxConnections_ = databaseConfiguration_.safeGetInt("cp_max_connections", 25);
            cpAcquireRetryAttempts_ = databaseConfiguration_.safeGetInt("cp_acquire_retry_attempts", 3);
            cpAcquireRetryDelay_ = databaseConfiguration_.safeGetInt("cp_acquire_retry_delay", 250);
            cpConnectionTimeout_ = databaseConfiguration_.safeGetInt("cp_connection_timeout", 5000);
            cpEnableStatistics_ = databaseConfiguration_.safeGetBoolean("cp_enable_statistics", false);
            cpDefaultAutoCommit_ = databaseConfiguration_.safeGetBoolean("cp_default_auto_commit", false);  
            connectionValidityCheckTimeout_ = databaseConfiguration_.safeGetInteger("connection_validity_check_timeout", 5);  

            // Standard
            hostname_ = databaseConfiguration_.safeGetString("db_hostname", "");
            port_ = databaseConfiguration_.safeGetString("db_port", "");
            username_ = databaseConfiguration_.safeGetString("db_username", "");
            password_ = databaseConfiguration_.safeGetString("db_password", "");
            attributes_ = databaseConfiguration_.safeGetString("db_attributes", "");
            customJdbc_ = databaseConfiguration_.safeGetString("db_custom_jdbc", null);

            // Derby specific
            keys = databaseConfiguration_.getPropertiesConfiguration().getKeys("derby");
            while (keys.hasNext()) {
                String databasePropertyKey = keys.next();
                String databasePropertyValue = databaseConfiguration_.safeGetString(databasePropertyKey, null);
                Properties systemProperties = System.getProperties();
                systemProperties.put(databasePropertyKey, databasePropertyValue);
            }
            
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    
    }
    
    private static String createJdbcString() {
        
        if (!isInitializeSuccess_) {
            return null;
        }
        
        String jdbc = null;
         
        if ((customJdbc_ != null) && !customJdbc_.isEmpty()) {
            jdbc = customJdbc_;
        }
        else if (type_ == DERBY_NETWORK) {
            jdbc = "jdbc:derby://" + hostname_ + ":" + port_ + "/" + databaseName_ + 
                    ";user=" + username_ + ";password=" + password_ + ";" + attributes_;
        }
        else if (type_ == DERBY_EMBEDDED) {
            jdbc = "jdbc:derby:" + databaseLocalPath_ + File.separator + databaseName_ + ";user=" + 
                    username_ + ";password=" + password_ + ";" + attributes_;
        }
        else if (type_ == MYSQL) {
            jdbc = "jdbc:mysql://" + hostname_ + ":" + port_ + "/" + databaseName_ + 
                    "?user=" + username_ + "&password=" + password_ + "&" + attributes_;
        }
        else if (type_ == POSTGRES) {
            jdbc = "jdbc:postgresql://" + hostname_ + ":" + port_ + "/" + databaseName_ + 
                    "?user=" + username_ + "&password=" + password_ + "&" + attributes_;
        }
        
        return jdbc;
    }

    public static int getCpMaxConnections() {
        return cpMaxConnections_;
    }

    public static int getCpAcquireRetryAttempts() {
        return cpAcquireRetryAttempts_;
    }

    public static int getCpAcquireRetryDelay() {
        return cpAcquireRetryDelay_;
    }

    public static int getCpConnectionTimeout() {
        return cpConnectionTimeout_;
    }
    
    public static boolean isCpEnableStatistics() {
        return cpEnableStatistics_;
    }
    
    public static Boolean getCpDefaultAutoCommit() {
        return cpDefaultAutoCommit_;
    }
    
    public static int getConnectionValidityCheckTimeout() {
        return connectionValidityCheckTimeout_;
    }
    
    public static int getType() {
        return type_;
    }

    public static String getTypeString() {
        return typeString_;
    }

    public static boolean isInitializeSuccess() {
        return isInitializeSuccess_;
    }
    
    public static String getHostname() {
        return hostname_;
    }

    public static String getPort() {
        return port_;
    }
    
    public static String getDatabaseLocalPath() {
        return databaseLocalPath_;
    }

    public static String getDatabaseName() {
        return databaseName_;
    }

    public static String getUsername() {
        return username_;
    }

    public static String getPassword() {
        return password_;
    }
    
    public static String getAttributes() {
        return attributes_;
    }

    public static String getCustomJdbc() {
        return customJdbc_;
    }
    
    public static String getJdbcConnectionString() {
        return jdbcConnectionString_;
    }

    public static void setJdbcConnectionString(String jdbcConnectionString) {
        jdbcConnectionString_ = jdbcConnectionString;
    }

}