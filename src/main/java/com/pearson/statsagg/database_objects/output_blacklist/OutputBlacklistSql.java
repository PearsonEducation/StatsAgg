package com.pearson.statsagg.database_objects.output_blacklist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OutputBlacklistSql {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputBlacklistSql.class.getName());

    protected final static String Select_OutputBlacklist_ByPrimaryKey = 
                    "SELECT * FROM OUTPUT_BLACKLIST " +
                    "WHERE ID = ?";
    
    protected final static String Select_AllOutputBlacklist = 
                    "SELECT * FROM OUTPUT_BLACKLIST";
    
    protected final static String Insert_OutputBlacklist =
                    "INSERT INTO OUTPUT_BLACKLIST " +
                    "(METRIC_GROUP_ID) " +
                    "VALUES(?)";
    
    protected final static String Update_OutputBlacklist_ByPrimaryKey =
                    "UPDATE OUTPUT_BLACKLIST " +
                    "SET METRIC_GROUP_ID = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_OutputBlacklist_ByPrimaryKey =
                    "DELETE FROM OUTPUT_BLACKLIST " +
                    "WHERE ID = ?";
    
}
