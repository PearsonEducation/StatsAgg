package com.pearson.statsagg.database_objects.metric_groups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTagsSql {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTagsSql.class.getName());
    
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
