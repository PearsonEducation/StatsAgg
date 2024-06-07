package com.pearson.statsagg.database_objects.gauges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GaugesSql {
    
    private static final Logger logger = LoggerFactory.getLogger(GaugesSql.class.getName());
    
    protected final static String TruncateTable_Gauges = 
                    "TRUNCATE TABLE GAUGES";
    
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
    
    public static String generateBatchUpsert(Integer numRows) {
        
        if ((numRows == null) || (numRows <= 0)) {
            return null;
        }
        
        StringBuilder upsert = new StringBuilder();
        
        upsert.append("INSERT INTO GAUGES(BUCKET_SHA1, BUCKET, METRIC_VALUE, LAST_MODIFIED) VALUES");
        
        for (int i = 0; i < numRows; i++) {
            upsert.append("(?,?,?,?)");
            if ((i + 1) < numRows) upsert.append(",");
        }
        
        upsert.append(" ON DUPLICATE KEY UPDATE BUCKET=VALUES(BUCKET), METRIC_VALUE=VALUES(METRIC_VALUE), LAST_MODIFIED=VALUES(LAST_MODIFIED)");
                
        return upsert.toString();
    }
    
}
