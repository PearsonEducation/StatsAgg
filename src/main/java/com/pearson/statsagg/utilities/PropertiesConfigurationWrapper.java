package com.pearson.statsagg.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
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
    
    public boolean safeGetBoolean(String key, boolean defaultValue) {
        
        boolean returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getBoolean(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Boolean safeGetBoolean(String key, Boolean defaultValue) {
        
        Boolean returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getBoolean(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public byte safeGetByte(String key, byte defaultValue) {
        
        byte returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getByte(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Byte safeGetByte(String key, Byte defaultValue) {
        
        Byte returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getByte(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public short safeGetShort(String key, short defaultValue) {
        
        short returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getShort(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Short safeGetShort(String key, Short defaultValue) {
        
        Short returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getShort(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public int safeGetInt(String key, int defaultValue) {
        
        int returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getInt(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Integer safeGetInteger(String key, Integer defaultValue) {
        
        Integer returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getInteger(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public long safeGetLong(String key, long defaultValue) {
        
        long returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getLong(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Long safeGetLong(String key, Long defaultValue) {
        
        Long returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getLong(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public float safeGetFloat(String key, float defaultValue) {
        
        float returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getFloat(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Float safeGetFloat(String key, Float defaultValue) {
        
        Float returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getFloat(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public double safeGetDouble(String key, double defaultValue) {
        
        double returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getDouble(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Double safeGetDouble(String key, Double defaultValue) {
        
        Double returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getDouble(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public BigInteger safeGetBigInteger(String key, BigInteger defaultValue) {
        
        BigInteger returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getBigInteger(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public BigDecimal safeGetBigDecimal(String key, BigDecimal defaultValue) {
        
        BigDecimal returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getBigDecimal(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public String safeGetString(String key, String defaultValue) {
        
        String returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getString(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public String[] safeGetStringArray(String key) {
        
        String[] returnValue = null;
                
        try {
            returnValue = propertiesConfiguration_.getStringArray(key);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public List<Object> safeGetList(String key, List<Object> defaultValue) {
        
        List<Object> returnValue = defaultValue;
                
        try {
            returnValue = propertiesConfiguration_.getList(key, defaultValue);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
    }
    
    public Properties safeGetProperties(String key, Properties defaults) {
        
        Properties returnValue = defaults;
                
        try {
            returnValue = propertiesConfiguration_.getProperties(key, defaults);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnValue;
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
