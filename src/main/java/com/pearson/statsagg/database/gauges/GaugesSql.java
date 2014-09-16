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
    
    protected final static String CreateTable_Gauges =  
                    "CREATE TABLE GAUGES (" + 
                    "BUCKET VARCHAR(32000) NOT NULL, " + 
                    "METRIC_VALUE DECIMAL(31,7) NOT NULL, " +
                    "LAST_MODIFIED TIMESTAMP NOT NULL " + 
                    ")";
    
    protected final static String CreateIndex_Gauges_PrimaryKey =
                    "ALTER TABLE GAUGES ADD CONSTRAINT G_PK PRIMARY KEY (" + 
                    "BUCKET" + 
                    ")";
    
    protected final static String Select_Gauge_ByPrimaryKey = 
                    "SELECT * FROM GAUGES " +
                    "WHERE BUCKET = ?";
    
    protected final static String Select_AllGauges = 
                    "SELECT * FROM GAUGES";
    
    protected final static String Insert_Gauge =
                    "INSERT INTO GAUGES " +
                    "(BUCKET, METRIC_VALUE, LAST_MODIFIED) " +
                    "VALUES(?,?,?)";
    
    protected final static String Update_Gauge_ByPrimaryKey =
                    "UPDATE GAUGES " +
                    "SET BUCKET = ?, METRIC_VALUE = ?, LAST_MODIFIED = ? " +
                    "WHERE BUCKET = ?";
    
    protected final static String Delete_Gauge_ByPrimaryKey =
                    "DELETE FROM GAUGES " +
                    "WHERE BUCKET = ?";
    
}
