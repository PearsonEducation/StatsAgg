package com.pearson.statsagg.flyway;

import com.pearson.statsagg.configuration.DatabaseConfiguration;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.file_utils.FileIo;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class FlywayOperations {
    
    private static final Logger logger = LoggerFactory.getLogger(FlywayOperations.class.getName());

    public static boolean repair(String flywayConfigurationPath) {
        
        boolean wasSuccessful = false;
        Flyway flyway = createFlywayInstance(flywayConfigurationPath, false);

        try {
            if (flyway != null) {
                flyway.repair();
                wasSuccessful = true;
            }
            else {
                logger.error("Error running flyway repair");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
        }
        
        return wasSuccessful;
    }
    
    public static boolean migrate(String flywayConfigurationPath, boolean performBaseline) {

        boolean wasSuccessful = false;
        
        Flyway flyway = createFlywayInstance(flywayConfigurationPath, performBaseline);

        try {
            if (flyway != null) {
                flyway.migrate();
                wasSuccessful = true;
            }
            else {
                logger.error("Error running flyway migrate");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
            
            if (!performBaseline && e.toString().contains("non-empty schema") && e.toString().contains("baseline")) {
                logger.info("Attempting to baseline existing StatsAgg database. Baseline assumes StatsAgg has already been updated to versoin 1.5. "
                        + "If StatsAgg was never deployed & you're seeing this message, then it's likely that Flyway doesn't like how your database schema was setup "
                        + "(ie-- tables already exist in the schema). You may need to resolve this issue manually "
                        + "(ie-- start with a fresh schema).");
                
                wasSuccessful = migrate(flywayConfigurationPath, true);
            }
            
        }
        
        return wasSuccessful;
    }
    
    private static Properties readFlywayPropertiesFile(String flywayConfigurationPath) {
        
        if ((flywayConfigurationPath == null) || flywayConfigurationPath.trim().isEmpty()) {
            return new Properties();
        }
        
        Properties properties = new Properties();
        
        try {
            String flywayProperties = FileIo.readFileToString(flywayConfigurationPath);
            if ((flywayProperties == null) || flywayProperties.isEmpty()) return properties;

            try (InputStream inputStream = new ByteArrayInputStream(flywayProperties.getBytes())) {
                properties.load(inputStream);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
        }
        
        return properties;
    }
    
    private static Flyway createFlywayInstance(String flywayConfigurationPath, boolean performBaseline) {
        
        Flyway flyway = null;
        
        try {
            Properties flywayProperties = readFlywayPropertiesFile(flywayConfigurationPath);
            if (flywayProperties == null) return null;
            
            if ((DatabaseConfiguration.getType() == DatabaseConfiguration.DERBY_EMBEDDED) || (DatabaseConfiguration.getType() == DatabaseConfiguration.DERBY_NETWORK)) {
                if (flywayProperties.containsKey("flyway.sqlMigrationPrefix")) {
                    logger.info("Overriding flyway.sqlMigrationPrefix value to VD");
                    flywayProperties.remove("flyway.sqlMigrationPrefix");
                }

                if (flywayProperties.containsKey("flyway.repeatableSqlMigrationPrefix")) {
                    logger.info("Overriding flyway.repeatableSqlMigrationPrefix value to RD");
                    flywayProperties.remove("flyway.repeatableSqlMigrationPrefix");
                }
                
                flywayProperties.put("flyway.sqlMigrationPrefix", "VD");
                flywayProperties.put("flyway.repeatableSqlMigrationPrefix", "RD");
            }
            else if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
                if (flywayProperties.containsKey("flyway.sqlMigrationPrefix")) {
                    logger.info("Overriding flyway.sqlMigrationPrefix value to VM");
                    flywayProperties.remove("flyway.sqlMigrationPrefix");
                }

                if (flywayProperties.containsKey("flyway.repeatableSqlMigrationPrefix")) {
                    logger.info("Overriding flyway.repeatableSqlMigrationPrefix value to RM");
                    flywayProperties.remove("flyway.repeatableSqlMigrationPrefix");
                }
                
                flywayProperties.put("flyway.sqlMigrationPrefix", "VM");
                flywayProperties.put("flyway.repeatableSqlMigrationPrefix", "RM");
            }

            if (performBaseline) { // baseline an old/existing statsagg deployment with scripts used to build version 1.5
                flywayProperties.remove("flyway.baselineOnMigrate");
                flywayProperties.remove("flyway.baselineVersion");
                flywayProperties.put("flyway.baselineOnMigrate", "true");
                flywayProperties.put("flyway.baselineVersion", "9");
            }
            
            FluentConfiguration fluentConfiguration = Flyway.configure().configuration(flywayProperties);

            flyway = fluentConfiguration.dataSource(DatabaseConnections.getDatasource()).load();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));   
        }
        
        return flyway;
    }
    
}
