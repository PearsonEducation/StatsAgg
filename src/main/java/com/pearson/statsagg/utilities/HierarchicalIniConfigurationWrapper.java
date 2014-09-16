package com.pearson.statsagg.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class HierarchicalIniConfigurationWrapper {
    
    private static final Logger logger = LoggerFactory.getLogger(HierarchicalIniConfigurationWrapper.class.getName());
    
    private String configurationDirectory_ = null;
    private String configurationFilename_ = null;
    private InputStream configurationInputStream_ = null;
    private HierarchicalINIConfiguration hierarchicalIniConfiguration_ = null;
    
    public HierarchicalIniConfigurationWrapper(String filePathAndFilename) {
        readHierarchicalIniConfigurationFile(filePathAndFilename);
    }
    
    public HierarchicalIniConfigurationWrapper(File hierarchicalIniFile) {
        readHierarchicalIniConfigurationFile(hierarchicalIniFile);
    }
    
    public HierarchicalIniConfigurationWrapper(InputStream configurationInputStream) {
        readHierarchicalIniConfigurationFile(configurationInputStream);
    }
    
    private void readHierarchicalIniConfigurationFile(String filePathAndFilename) {
        
        if (filePathAndFilename == null) {
            return;
        }
        
        try {
            File hierarchicalIniConfigurationFile_ = new File(filePathAndFilename);
            boolean doesFileExist = FileIo.doesFileExist(filePathAndFilename);
            
            if (doesFileExist) {
                configurationDirectory_ = hierarchicalIniConfigurationFile_.getParent();
                configurationFilename_ = hierarchicalIniConfigurationFile_.getName();

                hierarchicalIniConfiguration_ = new HierarchicalINIConfiguration();
                hierarchicalIniConfiguration_.setDelimiterParsingDisabled(true);
                hierarchicalIniConfiguration_.setAutoSave(false);
                hierarchicalIniConfiguration_.load(hierarchicalIniConfigurationFile_);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            configurationDirectory_ = null;
            configurationFilename_ = null;
            hierarchicalIniConfiguration_ = null;
        }
    }
    
    private void readHierarchicalIniConfigurationFile(File hierarchicalIniFile) {
        
        if (hierarchicalIniFile == null) {
            return;
        }
        
        try {
            if (hierarchicalIniFile.exists()) {
                configurationDirectory_ = hierarchicalIniFile.getParent();
                configurationFilename_ = hierarchicalIniFile.getName();

                hierarchicalIniConfiguration_ = new HierarchicalINIConfiguration();
                hierarchicalIniConfiguration_.setDelimiterParsingDisabled(true);
                hierarchicalIniConfiguration_.setAutoSave(false);
                hierarchicalIniConfiguration_.load(hierarchicalIniFile);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            configurationDirectory_ = null;
            configurationFilename_ = null;
            hierarchicalIniConfiguration_ = null;
        }
    }
    
    private void readHierarchicalIniConfigurationFile(InputStream configurationInputStream) {
        
        if (configurationInputStream == null) {
            return;
        }
        
        try {
            configurationInputStream_ = configurationInputStream;
            
            hierarchicalIniConfiguration_ = new HierarchicalINIConfiguration();
            hierarchicalIniConfiguration_.setDelimiterParsingDisabled(true);
            hierarchicalIniConfiguration_.setAutoSave(false);
            hierarchicalIniConfiguration_.load(configurationInputStream, null);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            configurationInputStream_ = null;
            hierarchicalIniConfiguration_ = null;
        }
    }
    
    
    public void saveHierarchicalIniConfigurationFile(String filePath, String filename) {
        try {
            File hierarchicalIniConfigurationFile = new File(filePath + File.separator + filename);
            hierarchicalIniConfiguration_.save(hierarchicalIniConfigurationFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public void saveHierarchicalIniConfigurationFile(File hierarchicalIniConfigurationFile) {
        try {
            hierarchicalIniConfiguration_.save(hierarchicalIniConfigurationFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public String saveHierarchicalIniConfigurationToString() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            hierarchicalIniConfiguration_.save(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }
    
    public boolean isValid() {
        
        boolean isConfigFileValid = (configurationDirectory_ != null) && (configurationFilename_ != null) && (hierarchicalIniConfiguration_ != null);
        boolean isConfigInputStreamValue = (configurationInputStream_ != null) && (hierarchicalIniConfiguration_ != null);
        
        if (isConfigFileValid || isConfigInputStreamValue) {
            return true;
        }
        else {
            return false;
        }

    }
    
    public static void saveHierarchicalIniConfigurationFile(String filePath, String filename, HierarchicalINIConfiguration hierarchicalIniConfiguration) {
        try {
            File hierarchicalIniConfigurationFile = new File(filePath + File.separator + filename);
            hierarchicalIniConfiguration.save(hierarchicalIniConfigurationFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public static void saveHierarchicalIniConfigurationFile(File hierarchicalIniConfigurationFile, HierarchicalINIConfiguration hierarchicalIniConfiguration) {
        try {
            hierarchicalIniConfiguration.save(hierarchicalIniConfigurationFile);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    public static String saveHierarchicalIniConfigurationToString(HierarchicalINIConfiguration hierarchicalIniConfiguration) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            hierarchicalIniConfiguration.save(byteArrayOutputStream);
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
    
    public HierarchicalINIConfiguration getHierarchicalIniConfiguration() {
        return hierarchicalIniConfiguration_;
    }

}
