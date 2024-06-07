package com.pearson.statsagg.database_objects.metric_group_templates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTemplatesSql {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplatesSql.class.getName());
    
    protected final static String Insert_MetricGroupTemplate =
                    "INSERT INTO METRIC_GROUP_TEMPLATES " +
                    "(NAME, UPPERCASE_NAME, VARIABLE_SET_LIST_ID, METRIC_GROUP_NAME_VARIABLE, DESCRIPTION_VARIABLE, " +
                    "MATCH_REGEXES_VARIABLE, BLACKLIST_REGEXES_VARIABLE, TAGS_VARIABLE, IS_MARKED_FOR_DELETE) " +
                    "VALUES(?,?,?,?,?,?,?,?,?)";
    
    protected final static String Update_MetricGroupTemplate_ByPrimaryKey =
                    "UPDATE METRIC_GROUP_TEMPLATES " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, VARIABLE_SET_LIST_ID = ?, METRIC_GROUP_NAME_VARIABLE = ?, DESCRIPTION_VARIABLE = ?, " +
                    "MATCH_REGEXES_VARIABLE = ?, BLACKLIST_REGEXES_VARIABLE = ?, TAGS_VARIABLE = ?, IS_MARKED_FOR_DELETE = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_MetricGroupTemplate_ByPrimaryKey =
                    "DELETE FROM METRIC_GROUP_TEMPLATES " +
                    "WHERE ID = ?";
    
    protected final static String Select_AllMetricGroupTemplates = 
                    "SELECT * FROM METRIC_GROUP_TEMPLATES";
    
    protected final static String Select_MetricGroupTemplateNames = 
                    "SELECT NAME FROM METRIC_GROUP_TEMPLATES";
    
    protected final static String Select_MetricGroupTemplate_ByPrimaryKey = 
                    "SELECT * FROM METRIC_GROUP_TEMPLATES " +
                    "WHERE ID = ?";
    
    protected final static String Select_MetricGroupTemplate_ByName = 
                    "SELECT * FROM METRIC_GROUP_TEMPLATES " +
                    "WHERE NAME = ?";
    
    protected final static String Select_MetricGroupTemplate_ByUppercaseName = 
                    "SELECT * FROM METRIC_GROUP_TEMPLATES " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_MetricGroupTemplate_Names_OrderByName = 
                    "SELECT NAME FROM METRIC_GROUP_TEMPLATES " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";

    protected final static String Select_MetricGroupTemplates_IdAndName = 
                    "SELECT ID, NAME FROM METRIC_GROUP_TEMPLATES ORDER BY ID";
    
}
