package com.pearson.statsagg.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class PropertiesConfigurationWrapper {
    
    private static final Logger logger = LoggerFactory.getLogger(PropertiesConfigurationWrapper.class.getName());
    
    private String configurationDirectory_ = null;
    private String configurationFilename_ = null;
    private InputStream configurationInputStream_ = null;
    private PropertiesConfiguration propertiesConfiguration_ = null;
    
    public PropertiesConfigurationWrapper(String filePathAndFilename) {
        readPropertiesConfigurationFile(filePathAndFilename);
    }
    
    public PropertiesConfigurationWrapper(File propertiesFile) {
        readPropertiesConfigurationFile(propertiesFile);
    }
    
    public PropertiesConfigurationWrapper(InputStream configurationInputStream) {
        readPropertiesConfigurationFile(configurationInputStream);
    }
    
    private void readPropertiesConfigurationFile(String filePathAndFilename) {
        
        if (filePathAndFilename == null) {
            return;
        }
        
        try {
            File propertiesFile = new File(filePathAndFilename);
            boolean doesFileExist = FileIo.doesFileExist(filePathAndFilename);
            
            if (doesFileExist) {
                configurationDirectory_ = propertiesFile.getParent();
                configurationFilename_ = propertiesFile.getName();

                propertiesConfiguration_ = new PropertiesConfiguration();
                propertiesConfiguration_.setDelimiterParsingDisabled(true);
                propertiesConfiguration_.setAutoSave(false);
                propertiesConfiguration_.load(propertiesFile);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            configurationDirectory_ = null;
            configurationFilename_ = null;
            propertiesConfiguration_ = null;
        }
    }
    
    private void readPropertiesConfigurationFile(File propertiesFile) {
        
        if (propertiesFile == null) {
            return;
        }
        
        try {
            if (propertiesFile.exists()) {
                configurationDirectory_ = propertiesFile.getParent();
                configurationFilename_ = propertiesFile.getName();

                propertiesConfiguration_ = new PropertiesConfiguration();
                propertiesConfiguration_.setDelimiterParsingDisabled(true);
                propertiesConfiguration_.setAutoSave(false);
                propertiesConfiguration_.load(propertiesFile);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            configurationDirectory_ = null;
            configurationFilename_ = null;
            propertiesConfiguration_ = null;
        }
    }
    
    private void readPropertiesConfigurationFile(InputStream configurationInputStream) {
        
        if (configurationInputStream == null) {
            return;
        }
        
        try {
            configurationInputStream_ = configurationInputStream;
            
            propertiesConfiguration_ = new PropertiesConfiguration();
            propertiesConfiguration_.setDelimiterParsingDisabled(true);
            propertiesConfiguration_.setAutoSave(false);
            propertiesConfiguration_.load(configurationInputStream, null);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            configurationInputStream_ = null;
            propertiesConfiguration_ = null;
        }
    }
    
    public void savePropertiesConfigurationFile(String filePath, String filename) {
        try {
            File propertyFile = new File(filePath + File.separator + filename);
            propertiesConfiguration_.save(propertyFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public void savePropertiesConfigurationFile(File propertyFile) {
        try {
            propertiesConfiguration_.save(propertyFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public String savePropertiesConfigurationToString() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            propertiesConfiguration_.save(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public boolean isValid() {
        
        boolean isConfigFileValid = (configurationDirectory_ != null) && (configurationFilename_ != null) && (propertiesConfiguration_ != null);
        boolean isConfigInputStreamValue = (configurationInputStream_ != null) && (propertiesConfiguration_ != null);
        
        if (isConfigFileValid || isConfigInputStreamValue) {
            return true;
        }
        else {
            return false;
        }

    }
    
    public static void savePropertiesConfigurationFile(String filePath, String filename, PropertiesConfiguration propertiesConfiguration) {
        try {
            File propertyFile = new File(filePath + File.separator + filename);
            propertiesConfiguration.save(propertyFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public static void savePropertiesConfigurationFile(File propertyFile, PropertiesConfiguration propertiesConfiguration) {
        try {
            propertiesConfiguration.save(propertyFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    public static String savePropertiesConfigurationToString(PropertiesConfiguration propertiesConfiguration) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            propertiesConfiguration.save(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
        
    public String getConfigurationDirectory() {
        return configurationDirectory_;
    }

    public String getConfigurationFilename() {
        return configurationFilename_;
    }

    public InputStream getConfigurationInputStream() {
        return configurationInputStream_;
    }
    
    public PropertiesConfiguration getPropertiesConfiguration() {
        return propertiesConfiguration_;
    }

}
