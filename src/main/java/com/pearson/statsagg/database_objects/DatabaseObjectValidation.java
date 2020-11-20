package com.pearson.statsagg.database_objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseObjectValidation {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseObjectCommon.class.getName());

    private final boolean isValid_;
    private final String reason_;
    
    public DatabaseObjectValidation(boolean isValid) {
        isValid_ = isValid;
        reason_ = "";
    }
        
    public DatabaseObjectValidation(boolean isValid, String reason) {
        isValid_ = isValid;
        
        if (reason == null) reason_ = "";
        else reason_ = reason;
    }
    

    public boolean isValid() {
        return isValid_;
    }

    public String getReason() {
        return reason_;
    }
    
}
