package com.pearson.statsagg.globals;

import com.pearson.statsagg.utilities.PropertiesConfigurationWrapper;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.PropertiesConfiguration;
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
    private static PropertiesConfiguration databaseConfiguration_ = null;
    
    private static int cpMaxConnections_ = VALUE_NOT_SET_CODE;
    private static boolean cpEnableStatistics_ = false;
    
    private static int type_ = VALUE_NOT_SET_CODE;
    private static String hostname_ = null;
    private static String port_ = null;
    private static String databaseLocalPath_ = null;
    private static String databaseName_ = null;
    private static String username_ = null;
    private static String password_ = null;    
    private static String attributes_ = null;
    private static boolean defaultAutoCommit_ = true;
    private static int connectionValidityCheckTimeout_ = VALUE_NOT_SET_CODE;
    
    private static String jdbcConnectionString_ = null;
 
    public static boolean initialize(String filePathAndFilename) {
        
        if (filePathAndFilename == null) {
            return false;
        }
        
        PropertiesConfigurationWrapper propertiesConfigurationWrapper = new PropertiesConfigurationWrapper(filePathAndFilename);
        
        if ((propertiesConfigurationWrapper == null) || !propertiesConfigurationWrapper.isValid()) {
            return false;
        }

        databaseConfiguration_ = propertiesConfigurationWrapper.getPropertiesConfiguration();

        isInitializeSuccess_ = setDatabaseConfigurationValues();
        
        jdbcConnectionString_ = createJdbcString();
                
        return isInitializeSuccess_ && (jdbcConnectionString_ != null);
    }
    
    public static boolean initialize(InputStream configurationInputStream) {
        
        if (configurationInputStream == null) {
            return false;
        }
        
        PropertiesConfigurationWrapper propertiesConfigurationWrapper = new PropertiesConfigurationWrapper(configurationInputStream);
        
        if ((propertiesConfigurationWrapper == null) || !propertiesConfigurationWrapper.isValid()) {
            return false;
        }

        databaseConfiguration_ = propertiesConfigurationWrapper.getPropertiesConfiguration();

        isInitializeSuccess_ = setDatabaseConfigurationValues();
        
        jdbcConnectionString_ = createJdbcString();
                
        return isInitializeSuccess_ && (jdbcConnectionString_ != null);
    }
    
    private static boolean setDatabaseConfigurationValues() {
        
        try {
            // determine database type
            String type = databaseConfiguration_.getString("db_type", null);
            if (type.equalsIgnoreCase("derby_network")) {
                type_ = DERBY_NETWORK;
            }
            else if (type.equalsIgnoreCase("derby_embedded")) {
                type_ = DERBY_EMBEDDED;
            }
            else if (type.equalsIgnoreCase("mysql")) {
                type_ = MYSQL;
            }
            else if (type.equalsIgnoreCase("postgres")) {
                type_ = POSTGRES;
            }
            
            // load db_localpath first so it can be used by other variables
            databaseLocalPath_ = databaseConfiguration_.getString("db_localpath", "");
            databaseName_ = databaseConfiguration_.getString("db_name", "");
            
            // Variable substitution
            Iterator<String> keys = databaseConfiguration_.getKeys();
            while (keys.hasNext()) {
                String databasePropertyKey = keys.next();
                
                String databasePropertyValue = databaseConfiguration_.getString(databasePropertyKey, null);
                databasePropertyValue = databasePropertyValue.replace("${db_localpath}", databaseLocalPath_);
                databaseConfiguration_.setProperty(databasePropertyKey, databasePropertyValue);
                
                databasePropertyValue = databaseConfiguration_.getString(databasePropertyKey, null);
                databasePropertyValue = databasePropertyValue.replace("${db_name}", databaseName_);
                databaseConfiguration_.setProperty(databasePropertyKey, databasePropertyValue);
            }
            
            // Connection pool
            cpMaxConnections_ = databaseConfiguration_.getInt("cp_max_connections", VALUE_NOT_SET_CODE);
            cpEnableStatistics_ = databaseConfiguration_.getBoolean("cp_enable_statistics", false);
            
            // Standard
            hostname_ = databaseConfiguration_.getString("db_hostname", "");
            port_ = databaseConfiguration_.getString("db_port", "");
            databaseLocalPath_ = databaseConfiguration_.getString("db_localpath", "");
            databaseName_ = databaseConfiguration_.getString("db_name", "");
            username_ = databaseConfiguration_.getString("db_username", "");
            password_ = databaseConfiguration_.getString("db_password", "");
            attributes_ = databaseConfiguration_.getString("db_attributes", "");
            defaultAutoCommit_ = databaseConfiguration_.getBoolean("db_default_auto_commit", true);  
            connectionValidityCheckTimeout_ = databaseConfiguration_.getInteger("db_connection_validity_check_timeout", 0);  

            // Derby specific
            keys = databaseConfiguration_.getKeys("derby");
            while (keys.hasNext()) {
                String databasePropertyKey = keys.next();
                String databasePropertyValue = databaseConfiguration_.getString(databasePropertyKey, null);
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
         
        if (type_ == DERBY_NETWORK) {
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

    public static boolean isCpEnableStatistics() {
        return cpEnableStatistics_;
    }
    
    public static int getType() {
        return type_;
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

    public static Boolean getDefaultAutoCommit() {
        return defaultAutoCommit_;
    }
    
    public static int getConnectionValidityCheckTimeout() {
        return connectionValidityCheckTimeout_;
    }
    
    public static String getJdbcConnectionString() {
        return jdbcConnectionString_;
    }

    public static void setJdbcConnectionString(String jdbcConnectionString) {
        jdbcConnectionString_ = jdbcConnectionString;
    }
    
}