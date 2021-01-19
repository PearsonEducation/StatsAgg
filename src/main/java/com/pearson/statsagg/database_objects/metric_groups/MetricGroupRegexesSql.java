package com.pearson.statsagg.database_objects.metric_groups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupRegexesSql {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupRegexesSql.class.getName());

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
