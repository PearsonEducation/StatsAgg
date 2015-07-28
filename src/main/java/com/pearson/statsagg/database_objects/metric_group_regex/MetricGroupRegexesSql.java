package com.pearson.statsagg.database_objects.metric_group_regex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupRegexesSql {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupRegexesSql.class.getName());
    
    protected final static String DropTable_MetricGroupRegexes = 
                    "DROP TABLE METRIC_GROUP_REGEXES";
    
    protected final static String CreateTable_MetricGroupRegexes_Derby =  
                    "CREATE TABLE METRIC_GROUP_REGEXES (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL," + 
                    "IS_BLACKLIST_REGEX BOOLEAN NOT NULL," + 
                    "PATTERN CLOB(65535) NOT NULL" + 
                    ")";
    
    protected final static String CreateTable_MetricGroupRegexes_MySQL =  
                    "CREATE TABLE METRIC_GROUP_REGEXES (" + 
                    "ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL, " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL," + 
                    "IS_BLACKLIST_REGEX BOOLEAN NOT NULL," + 
                    "PATTERN TEXT NOT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_MetricGroupRegexes_PrimaryKey =
                    "ALTER TABLE METRIC_GROUP_REGEXES ADD CONSTRAINT MGR_PK PRIMARY KEY (" + 
                    "ID" + 
                    ")";

    protected final static String CreateIndex_MetricGroupRegexes_ForeignKey_MetricGroupId =
                    "ALTER TABLE METRIC_GROUP_REGEXES " +
                    "ADD CONSTRAINT MGR_MGID_FK FOREIGN KEY (METRIC_GROUP_ID) " + 
                    "REFERENCES METRIC_GROUPS(ID)";
    
    protected final static String Select_MetricGroupRegex_ByPrimaryKey = 
                    "SELECT * FROM METRIC_GROUP_REGEXES " +
                    "WHERE ID = ?";

    protected final static String Select_MetricGroupRegexes_ByMetricGroupId = 
                    "SELECT * FROM METRIC_GROUP_REGEXES " +
                    "WHERE METRIC_GROUP_ID = ?";
    
    protected final static String Select_AllMetricGroupRegexes = 
                    "SELECT * FROM METRIC_GROUP_REGEXES";
    
    protected final static String Insert_MetricGroupRegex =
                    "INSERT INTO METRIC_GROUP_REGEXES " +
                    "(METRIC_GROUP_ID, IS_BLACKLIST_REGEX, PATTERN) " +
                    "VALUES(?,?,?)";
    
    protected final static String Update_MetricGroupRegex_ByPrimaryKey =
                    "UPDATE METRIC_GROUP_REGEXES " +
                    "SET METRIC_GROUP_ID = ?, IS_BLACKLIST_REGEX = ?, PATTERN = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroupRegex_ByPrimaryKey = 
                    "DELETE FROM METRIC_GROUP_REGEXES " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroupRegex_ByMetricGroupId = 
                    "DELETE FROM METRIC_GROUP_REGEXES " +
                    "WHERE METRIC_GROUP_ID = ?";
    
}
