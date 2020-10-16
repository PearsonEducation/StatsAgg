package com.pearson.statsagg.database_objects.output_blacklist;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseResultSetHandler;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OutputBlacklistResultSetHandler extends OutputBlacklist implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputBlacklistResultSetHandler.class.getName());
    
    @Override
    public List<OutputBlacklist> handleResultSet(ResultSet resultSet) {
        
        List<OutputBlacklist> outputBlacklists = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    Integer metricGroupId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_id", Integer.class);

                    OutputBlacklist outputBlacklist = new OutputBlacklist(id, metricGroupId);
                    outputBlacklists.add(outputBlacklist);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return outputBlacklists;
    }

}

