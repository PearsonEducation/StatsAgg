package com.pearson.statsagg.database_objects.metric_group_tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTagsSql {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTagsSql.class.getName());
    
    protected final static String DropTable_MetricGroupTags = 
                    "DROP TABLE METRIC_GROUP_TAGS";
    
    protected final static String CreateTable_MetricGroupTags_Derby =  
                    "CREATE TABLE METRIC_GROUP_TAGS (" + 
                    "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL," + 
                    "TAG CLOB(65535) NOT NULL" + 
                    ")";
    
    protected final static String CreateTable_MetricGroupTags_MySQL =  
                    "CREATE TABLE METRIC_GROUP_TAGS (" + 
                    "ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL, " + 
                    "METRIC_GROUP_ID INTEGER NOT NULL," + 
                    "TAG TEXT NOT NULL" + 
                    ") " +
                    "ROW_FORMAT=DYNAMIC";
    
    protected final static String CreateIndex_MetricGroupTags_PrimaryKey =
                    "ALTER TABLE METRIC_GROUP_TAGS ADD CONSTRAINT MGT_PK PRIMARY KEY (" + 
                    "ID" + 
                    ")";

    protected final static String CreateIndex_MetricGroupTags_ForeignKey_MetricGroupId =
                    "ALTER TABLE METRIC_GROUP_TAGS " +
                    "ADD CONSTRAINT MGT_MGID_FK FOREIGN KEY (METRIC_GROUP_ID) " + 
                    "REFERENCES METRIC_GROUPS(ID)";
    
    protected final static String Select_MetricGroupTag_ByPrimaryKey = 
                    "SELECT * FROM METRIC_GROUP_TAGS " +
                    "WHERE ID = ?";

    protected final static String Select_MetricGroupTags_ByMetricGroupId = 
                    "SELECT * FROM METRIC_GROUP_TAGS " +
                    "WHERE METRIC_GROUP_ID = ?";
    
    protected final static String Select_AllMetricGroupTags = 
                    "SELECT * FROM METRIC_GROUP_TAGS";
    
    protected final static String Insert_MetricGroupTag =
                    "INSERT INTO METRIC_GROUP_TAGS " +
                    "(METRIC_GROUP_ID, TAG) " +
                    "VALUES(?,?)";
    
    protected final static String Update_MetricGroupTag_ByPrimaryKey =
                    "UPDATE METRIC_GROUP_TAGS " +
                    "SET METRIC_GROUP_ID = ?, TAG = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroupTag_ByPrimaryKey = 
                    "DELETE FROM METRIC_GROUP_TAGS " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroupTag_ByMetricGroupId = 
                    "DELETE FROM METRIC_GROUP_TAGS " +
                    "WHERE METRIC_GROUP_ID = ?";
    
}
