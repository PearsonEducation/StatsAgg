package com.pearson.statsagg.database_objects.metric_groups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupsSql {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsSql.class.getName());
    
    protected final static String Insert_MetricGroup =
                    "INSERT INTO METRIC_GROUPS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, METRIC_GROUP_TEMPLATE_ID, VARIABLE_SET_ID) " +
                    "VALUES(?,?,?,?,?)";
    
    protected final static String Update_MetricGroup_ByPrimaryKey =
                    "UPDATE METRIC_GROUPS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, METRIC_GROUP_TEMPLATE_ID = ?, VARIABLE_SET_ID = ? " +
                    "WHERE ID = ?";
    
    protected final static String Update_MetricGroup_Name =
                    "UPDATE METRIC_GROUPS " +
                    "SET NAME = ?, UPPERCASE_NAME = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroup_ByPrimaryKey = 
                    "DELETE FROM METRIC_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_MetricGroup_ByPrimaryKey = 
                    "SELECT * FROM METRIC_GROUPS " +
                    "WHERE ID = ?";
    
    protected final static String Select_MetricGroup_ByName = 
                    "SELECT * FROM METRIC_GROUPS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_MetricGroup_ByUppercaseName = 
                    "SELECT * FROM METRIC_GROUPS " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_MetricGroup_Names_OrderByName = 
                    "SELECT NAME FROM METRIC_GROUPS " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";
    
    protected final static String Select_AllMetricGroups = 
                    "SELECT * FROM METRIC_GROUPS";
    
    protected final static String Select_MetricGroupNames = 
                    "SELECT NAME FROM METRIC_GROUPS";
    
    protected final static String Select_DistinctMetricGroupIds = 
                    "SELECT DISTINCT ID FROM METRIC_GROUPS";
    
    protected final static String Select_MetricGroup_ByMetricGroupTemplateId = 
                    "SELECT * FROM METRIC_GROUPS " +
                    "WHERE METRIC_GROUP_TEMPLATE_ID = ?";
    
}
