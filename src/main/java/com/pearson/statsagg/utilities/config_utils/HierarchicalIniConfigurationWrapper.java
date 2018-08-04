package com.pearson.statsagg.utilities.config_utils;

import com.pearson.statsagg.utilities.file_utils.FileIo;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
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
    
    public boolean safeGetBoolean(String key, boolean defaultValue) {
        
        boolean returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getBoolean(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Boolean safeGetBoolean(String key, Boolean defaultValue) {
        
        Boolean returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getBoolean(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public byte safeGetByte(String key, byte defaultValue) {
        
        byte returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getByte(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Byte safeGetByte(String key, Byte defaultValue) {
        
        Byte returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getByte(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public short safeGetShort(String key, short defaultValue) {
        
        short returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getShort(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Short safeGetShort(String key, Short defaultValue) {
        
        Short returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getShort(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public int safeGetInt(String key, int defaultValue) {
        
        int returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getInt(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Integer safeGetInteger(String key, Integer defaultValue) {
        
        Integer returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getInteger(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public long safeGetLong(String key, long defaultValue) {
        
        long returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getLong(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Long safeGetLong(String key, Long defaultValue) {
        
        Long returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getLong(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public float safeGetFloat(String key, float defaultValue) {
        
        float returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getFloat(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Float safeGetFloat(String key, Float defaultValue) {
        
        Float returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getFloat(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public double safeGetDouble(String key, double defaultValue) {
        
        double returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getDouble(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Double safeGetDouble(String key, Double defaultValue) {
        
        Double returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getDouble(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public BigInteger safeGetBigInteger(String key, BigInteger defaultValue) {
        
        BigInteger returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getBigInteger(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public BigDecimal safeGetBigDecimal(String key, BigDecimal defaultValue) {
        
        BigDecimal returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getBigDecimal(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public String safeGetString(String key, String defaultValue) {
        
        String returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getString(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public String[] safeGetStringArray(String key) {
        
        String[] returnValue = null;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getStringArray(key);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public List<Object> safeGetList(String key, List<Object> defaultValue) {
        
        List<Object> returnValue = defaultValue;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getList(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Properties safeGetProperties(String key, Properties defaults) {
        
        Properties returnValue = defaults;
                
        try {
            returnValue = hierarchicalIniConfiguration_.getProperties(key, defaults);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
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
