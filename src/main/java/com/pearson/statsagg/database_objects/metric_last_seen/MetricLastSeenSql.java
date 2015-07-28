package com.pearson.statsagg.database_objects.metric_last_seen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricLastSeenSql {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricLastSeenSql.class.getName());
    
    protected final static String DropTable_MetricLastSeen = 
                    "DROP TABLE METRIC_LAST_SEEN";
    
    protected final static String CreateTable_MetricLastSeen_Derby =  
                    "CREATE TABLE METRIC_LAST_SEEN (" + 
                    "METRIC_KEY_SHA1 VARCHAR(50) NOT NULL, " + 
                    "METRIC_KEY CLOB(1048576) NOT NULL, " + 
                    "LAST_MODIFIED TIMESTAMP NOT NULL " + 
                    ")";
    
    protected final static String CreateTable_MetricLastSeen_MySQL =  
                    "CREATE TABLE METRIC_LAST_SEEN (" + 
                    "METRIC_KEY_SHA1 VARCHAR(50) NOT NULL, " + 
                    "METRIC_KEY MEDIUMTEXT NOT NULL, " + 
                    "LAST_MODIFIED TIMESTAMP NULL DEFAULT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_MetricLastSeen_PrimaryKey =
                    "ALTER TABLE METRIC_LAST_SEEN ADD CONSTRAINT MLS_PK PRIMARY KEY (" + 
                    "METRIC_KEY_SHA1" + 
                    ")";
    
    protected final static String Select_MetricLastSeen_ByPrimaryKey = 
                    "SELECT * FROM METRIC_LAST_SEEN " +
                    "WHERE METRIC_KEY_SHA1 = ?";
    
    protected final static String Select_AllMetricLastSeen = 
                    "SELECT * FROM METRIC_LAST_SEEN";
    
    protected final static String Insert_MetricLastSeen =
                    "INSERT INTO METRIC_LAST_SEEN " +
                    "(METRIC_KEY_SHA1, METRIC_KEY, LAST_MODIFIED) " +
                    "VALUES(?,?,?)";
    
    protected final static String Update_MetricLastSeen_ByPrimaryKey =
                    "UPDATE METRIC_LAST_SEEN " +
                    "SET METRIC_KEY = ?, LAST_MODIFIED = ? " +
                    "WHERE METRIC_KEY_SHA1 = ?";
    
    protected final static String Delete_MetricLastSeen_ByPrimaryKey =
                    "DELETE FROM METRIC_LAST_SEEN " +
                    "WHERE METRIC_KEY_SHA1 = ?";
    
    public static String generateBatchUpsert(int numRows) {
        
        if (numRows <= 0) {
            return null;
        }
        
        StringBuilder upsert = new StringBuilder();
        
        upsert.append("INSERT INTO METRIC_LAST_SEEN(METRIC_KEY_SHA1, METRIC_KEY, LAST_MODIFIED) VALUES");
        
        for (int i = 0; i < numRows; i++) {
            upsert.append("(?,?,?)");
            if ((i + 1) < numRows) upsert.append(",");
        }
        
        upsert.append(" ON DUPLICATE KEY UPDATE METRIC_KEY=VALUES(METRIC_KEY), LAST_MODIFIED=VALUES(LAST_MODIFIED)");
                
        return upsert.toString();
    }
    
}
