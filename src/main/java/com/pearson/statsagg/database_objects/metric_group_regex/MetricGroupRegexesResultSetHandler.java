package com.pearson.statsagg.database_objects.metric_group_regex;

import com.pearson.statsagg.database_objects.metric_group.*;
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
            Set<String> columnNames = DatabaseUtils.getResultSetColumns(resultSet);
            
            while ((columnNames != null) && resultSet.next()) {
                try {
                    String columnName = "ID";
                    Integer id = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) id = null;
                    
                    columnName = "METRIC_GROUP_ID";
                    Integer metricGroupId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) metricGroupId = null;

                    columnName = "IS_BLACKLIST_REGEX";
                    Boolean isBlacklistRegex = (columnNames.contains(columnName)) ? resultSet.getBoolean(columnName) : null;
                    if (resultSet.wasNull()) isBlacklistRegex = null;

                    columnName = "PATTERN";
                    String pattern = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) pattern = null;

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

