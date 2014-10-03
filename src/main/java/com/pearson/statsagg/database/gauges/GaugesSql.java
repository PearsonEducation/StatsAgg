package com.pearson.statsagg.database.gauges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GaugesSql {
    
    private static final Logger logger = LoggerFactory.getLogger(GaugesSql.class.getName());
    
    protected final static String DropTable_Gauges = 
                    "DROP TABLE GAUGES";
    
    protected final static String CreateTable_Gauges_Derby =  
                    "CREATE TABLE GAUGES (" + 
                    "BUCKET_SHA1 VARCHAR(50) NOT NULL, " + 
                    "BUCKET CLOB(1048576) NOT NULL, " + 
                    "METRIC_VALUE DECIMAL(31,7) NOT NULL, " +
                    "LAST_MODIFIED TIMESTAMP NOT NULL " + 
                    ")";
    
    protected final static String CreateTable_Gauges_MySQL =  
                    "CREATE TABLE GAUGES (" + 
                    "BUCKET_SHA1 VARCHAR(50) NOT NULL, " + 
                    "BUCKET MEDIUMTEXT NOT NULL, " + 
                    "METRIC_VALUE DECIMAL(65,7) NOT NULL, " +
                    "LAST_MODIFIED TIMESTAMP NOT NULL " + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_Gauges_PrimaryKey =
                    "ALTER TABLE GAUGES ADD CONSTRAINT G_PK PRIMARY KEY (" + 
                    "BUCKET_SHA1" + 
                    ")";
    
    protected final static String Select_Gauge_ByPrimaryKey = 
                    "SELECT * FROM GAUGES " +
                    "WHERE BUCKET_SHA1 = ?";
    
    protected final static String Select_AllGauges = 
                    "SELECT * FROM GAUGES";
    
    protected final static String Insert_Gauge =
                    "INSERT INTO GAUGES " +
                    "(BUCKET_SHA1, BUCKET, METRIC_VALUE, LAST_MODIFIED) " +
                    "VALUES(?,?,?,?)";
    
    protected final static String Update_Gauge_ByPrimaryKey =
                    "UPDATE GAUGES " +
                    "SET BUCKET = ?, METRIC_VALUE = ?, LAST_MODIFIED = ? " +
                    "WHERE BUCKET_SHA1 = ?";
    
    protected final static String Delete_Gauge_ByPrimaryKey =
                    "DELETE FROM GAUGES " +
                    "WHERE BUCKET_SHA1 = ?";
    
    
    
    public static String generateBatchUpsert(int numRows) {
        
        if (numRows <= 0) {
            return null;
        }
        
        StringBuilder upsert = new StringBuilder("");
        
        upsert.append("INSERT INTO GAUGES(BUCKET_SHA1, BUCKET, METRIC_VALUE, LAST_MODIFIED) VALUES");
        
        for (int i = 0; i < numRows; i++) {
            upsert.append("(?,?,?,?)");
            if ((i + 1) < numRows) upsert.append(",");
        }
        
        upsert.append(" ON DUPLICATE KEY UPDATE BUCKET=VALUES(BUCKET), METRIC_VALUE=VALUES(METRIC_VALUE), LAST_MODIFIED=VALUES(LAST_MODIFIED)");
                
        return upsert.toString();
    }
    
}
