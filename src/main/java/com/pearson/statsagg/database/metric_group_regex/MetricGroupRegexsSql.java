package com.pearson.statsagg.database.metric_group_regex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupRegexsSql {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupRegexsSql.class.getName());
    
    protected final static String DropTable_MetricGroupRegexs = 
                    "DROP TABLE METRIC_GROUP_REGEXS";
    
    protected final static String CreateTable_MetricGroupRegexs_Derby =  
                    "CREATE TABLE METRIC_GROUP_REGEXS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL," + 
                    "PATTERN CLOB(65535) NOT NULL" + 
                    ")";
    
    protected final static String CreateTable_MetricGroupRegexs_MySQL =  
                    "CREATE TABLE METRIC_GROUP_REGEXS (" + 
                    "ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL, " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL," + 
                    "PATTERN TEXT NOT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_MetricGroupRegexs_PrimaryKey =
                    "ALTER TABLE METRIC_GROUP_REGEXS ADD CONSTRAINT MGR_PK PRIMARY KEY (" + 
                    "ID" + 
                    ")";

    protected final static String CreateIndex_MetricGroupRegexs_ForeignKey_MetricGroupId =
                    "ALTER TABLE METRIC_GROUP_REGEXS " +
                    "ADD CONSTRAINT MGR_MGID_FK FOREIGN KEY (METRIC_GROUP_ID) " + 
                    "REFERENCES METRIC_GROUPS(ID)";
    
    protected final static String Select_MetricGroupRegex_ByPrimaryKey = 
                    "SELECT * FROM METRIC_GROUP_REGEXS " +
                    "WHERE ID = ?";

    protected final static String Select_MetricGroupRegexs_ByMetricGroupId = 
                    "SELECT * FROM METRIC_GROUP_REGEXS " +
                    "WHERE METRIC_GROUP_ID = ?";
    
    protected final static String Select_AllMetricGroupRegexs = 
                    "SELECT * FROM METRIC_GROUP_REGEXS";
    
    protected final static String Insert_MetricGroupRegex =
                    "INSERT INTO METRIC_GROUP_REGEXS " +
                    "(METRIC_GROUP_ID, PATTERN) " +
                    "VALUES(?,?)";
    
    protected final static String Update_MetricGroupRegex_ByPrimaryKey =
                    "UPDATE METRIC_GROUP_REGEXS " +
                    "SET METRIC_GROUP_ID = ?, PATTERN = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroupRegex_ByPrimaryKey = 
                    "DELETE FROM METRIC_GROUP_REGEXS " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroupRegex_ByMetricGroupId = 
                    "DELETE FROM METRIC_GROUP_REGEXS " +
                    "WHERE METRIC_GROUP_ID = ?";
    
}
