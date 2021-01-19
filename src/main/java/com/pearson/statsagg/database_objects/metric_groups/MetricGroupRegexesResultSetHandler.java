package com.pearson.statsagg.database_objects.metric_groups;

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
public class MetricGroupRegexesResultSetHandler extends MetricGroupRegex implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupRegexesResultSetHandler.class.getName());
    
    @Override
    public List<MetricGroupRegex> handleResultSet(ResultSet resultSet) {
        
        List<MetricGroupRegex> metricGroupRegexes = new ArrayList<>();
        
        try {
            Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    Integer metricGroupId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_id", Integer.class);
                    Boolean isBlacklistRegex = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_blacklist_regex", Boolean.class);
                    String pattern = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "pattern", String.class);

                    MetricGroupRegex metricGroupRegex = new MetricGroupRegex(id, metricGroupId, isBlacklistRegex, pattern);
                    metricGroupRegexes.add(metricGroupRegex);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return metricGroupRegexes;
    }

}

