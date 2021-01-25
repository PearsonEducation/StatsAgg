package com.pearson.statsagg.database_objects;

/**
 * @author Jeffrey Schmidt
 */
public class DatabaseObjectStatus {

    public static int STATUS_GOOD = 1;
    public static int STATUS_WARNING = 2;
    public static int STATUS_DANGER = 3;
    
    private final int status_;
    private final String reason_;
    
    public DatabaseObjectStatus(int status, String reason) {
        this.status_ = status;
        this.reason_ = reason;
    }

    public int getStatus() {
        return status_;
    }

    public String getReason() {
        return reason_;
    }
    
}
