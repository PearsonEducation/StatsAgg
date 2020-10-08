package com.pearson.statsagg.globals;

import com.pearson.statsagg.utilities.config_utils.PropertiesConfigurationWrapper;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.zaxxer.hikari.HikariConfig;
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

    private static boolean flywayMigrateEnabled_ = false;
    private static boolean flywayRepairEnabled_ = false;
   
    private static HikariConfig hikariConfig_ = null;
    
    public static boolean initialize(String configurationFilepathAndFilename) {

        try {
            if (configurationFilepathAndFilename == null) {
                return false;
            }

            databaseConfiguration_ = new PropertiesConfigurationWrapper(configurationFilepathAndFilename);

            if ((databaseConfiguration_ == null) || !databaseConfiguration_.isValid()) {
                return false;
            }

            isInitializeSuccess_ = setDatabaseConfigurationValues();

            jdbcConnectionString_ = createJdbcString();

            return isInitializeSuccess_ && (jdbcConnectionString_ != null);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
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
            Iterator<String> derbyKeys = databaseConfiguration_.getPropertiesConfiguration().getKeys();
            while (derbyKeys.hasNext()) {
                String databasePropertyKey = derbyKeys.next();
                
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

            // jdbc connection string variables
            hostname_ = databaseConfiguration_.safeGetString("db_hostname", "");
            port_ = databaseConfiguration_.safeGetString("db_port", "");
            username_ = databaseConfiguration_.safeGetString("db_username", "");
            password_ = databaseConfiguration_.safeGetString("db_password", "");
            attributes_ = databaseConfiguration_.safeGetString("db_attributes", "");
            customJdbc_ = databaseConfiguration_.safeGetString("db_custom_jdbc", null);

            // flyway specific 
            flywayMigrateEnabled_ = databaseConfiguration_.safeGetBoolean("flyway_migrate_enabled", true);
            flywayRepairEnabled_ = databaseConfiguration_.safeGetBoolean("flyway_repair_enabled", true);
            
            // Derby specific
            derbyKeys = databaseConfiguration_.getPropertiesConfiguration().getKeys("derby");
            while (derbyKeys.hasNext()) {
                String databasePropertyKey = derbyKeys.next();
                String databasePropertyValue = databaseConfiguration_.safeGetString(databasePropertyKey, null);
                Properties systemProperties = System.getProperties();
                systemProperties.put(databasePropertyKey, databasePropertyValue);
            }
            
            // create the hikaricp configuration
            Properties hikaricpProperties = getHikariProperties(databaseConfiguration_.getPropertiesConfiguration().getKeys());
            hikariConfig_ = new HikariConfig(hikaricpProperties);

            // set legacy connection pool variables in the hikaricp config
            if (hikariConfig_ != null) {
                if (!hikaricpProperties.containsKey("poolName")) hikariConfig_.setPoolName("StatsAgg");
                if (!hikaricpProperties.containsKey("maximumPoolSize")) hikariConfig_.setMaximumPoolSize(50);
                if (!hikaricpProperties.containsKey("maxLifetime")) hikariConfig_.setMaxLifetime(30000);
                if (!hikaricpProperties.containsKey("connectionTimeout")) hikariConfig_.setConnectionTimeout(5000);
                if (!hikaricpProperties.containsKey("registerMbeans")) hikariConfig_.setRegisterMbeans(true);
                hikariConfig_.setAutoCommit(true);
                hikariConfig_.setJdbcUrl(createJdbcString());
            }
            
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    
    }
    
    private static Properties getHikariProperties(Iterator<String> databaseConfigurationKeys) {
        
        if (databaseConfigurationKeys == null) {
            return new Properties();
        }
        
        Properties hikaricpProperties = new Properties();
        
        // remove all the legacy connection pool, legacy jdbc/connection-string, and derby-specific configurations -- leaving just hikaricp variables
        while (databaseConfigurationKeys.hasNext()) {
            String databasePropertyKey = databaseConfigurationKeys.next();
            String databasePropertyValue = databaseConfiguration_.safeGetString(databasePropertyKey, null);
            if ((databasePropertyValue != null) && !databasePropertyValue.isEmpty()) {
                if (databasePropertyKey.startsWith("db_")) continue; // jdbc connection string variables
                if (databasePropertyKey.startsWith("cp_")) continue; // legacy connection pool settings to ignore
                if (databasePropertyKey.startsWith("derby")) continue; // derby-specific variables
                if (databasePropertyKey.startsWith("flyway")) continue; // flyway-specific variables
                if (databasePropertyKey.contains("connection_validity_check_timeout")) continue; // legacy variable that isn't used anymore

                hikaricpProperties.put(databasePropertyKey, databasePropertyValue);
            }
        }
                
        return hikaricpProperties;
    }
    
    private static String createJdbcString() {
        
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

    public static boolean isFlywayMigrateEnabled() {
        return flywayMigrateEnabled_;
    }

    public static boolean isFlywayRepairEnabled() {
        return flywayRepairEnabled_;
    }
    
    public static HikariConfig getHikariConfig() {
        return hikariConfig_;
    }
    
}