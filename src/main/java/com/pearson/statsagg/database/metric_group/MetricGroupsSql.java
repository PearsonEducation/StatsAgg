package com.pearson.statsagg.database.metric_group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupsSql {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsSql.class.getName());
    
    protected final static String DropTable_MetricGroups = 
                    "DROP TABLE METRIC_GROUPS";
    
    protected final static String CreateTable_MetricGroups_Derby =  
                    "CREATE TABLE METRIC_GROUPS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "DESCRIPTION CLOB(1048576) NOT NULL" + 
                    ")";
    
    protected final static String CreateTable_MetricGroups_MySQL =  
                    "CREATE TABLE METRIC_GROUPS (" + 
                    "ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL, " + 
                    "NAME VARCHAR(500) NOT NULL, " + 
                    "UPPERCASE_NAME VARCHAR(500) NOT NULL, " + 
                    "DESCRIPTION MEDIUMTEXT NOT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_MetricGroups_PrimaryKey =
                    "ALTER TABLE METRIC_GROUPS ADD CONSTRAINT MG_PK PRIMARY KEY (" + 
                    "ID" + 
                    ")";
    
    protected final static String CreateIndex_MetricGroups_Unique_Name =
                    "ALTER TABLE METRIC_GROUPS ADD CONSTRAINT MG_U_NAME UNIQUE (" + 
                    "NAME" + 
                    ")";
    
    protected final static String CreateIndex_MetricGroups_Unique_UppercaseName =
                    "ALTER TABLE METRIC_GROUPS ADD CONSTRAINT MG_U_UPPERCASE_NAME UNIQUE (" + 
                    "UPPERCASE_NAME" + 
                    ")";
    
    protected final static String Select_MetricGroup_ByPrimaryKey = 
                    "SELECT * FROM METRIC_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_MetricGroup_ByName = 
                    "SELECT * FROM METRIC_GROUPS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_MetricGroup_Names = 
                    "SELECT NAME FROM METRIC_GROUPS " +
                    "WHERE NAME LIKE ?";
    
    protected final static String Select_AllMetricGroups = 
                    "SELECT * FROM METRIC_GROUPS";
    
    protected final static String Select_DistinctMetricGroupIds = 
                    "SELECT DISTINCT ID FROM METRIC_GROUPS";
    
    protected final static String Insert_MetricGroup =
                    "INSERT INTO METRIC_GROUPS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION) " +
                    "VALUES(?,?,?)";
    
    protected final static String Update_MetricGroup_ByPrimaryKey =
                    "UPDATE METRIC_GROUPS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroup_ByPrimaryKey = 
                    "DELETE FROM METRIC_GROUPS " +
                    "WHERE ID = ?";
    
}
