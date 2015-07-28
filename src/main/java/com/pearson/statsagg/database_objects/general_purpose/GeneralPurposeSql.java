package com.pearson.statsagg.database_objects.general_purpose;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GeneralPurposeSql {
    
    private static final Logger logger = LoggerFactory.getLogger(GeneralPurposeSql.class.getName());
    
    protected final static String Select_MetricGroupTagsAssociatedWithAlert =
                    "SELECT ALERTS.ID AS A_ID, METRIC_GROUP_TAGS.TAG AS TAG " +
                    "FROM ALERTS " +
                    "JOIN METRIC_GROUP_TAGS ON ALERTS.METRIC_GROUP_ID = METRIC_GROUP_TAGS.METRIC_GROUP_ID";
    
}
