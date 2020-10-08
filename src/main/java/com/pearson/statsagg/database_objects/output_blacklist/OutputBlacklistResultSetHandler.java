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
            Set<String> columnNames = DatabaseUtils.getResultSetColumns(resultSet);
            
            while ((columnNames != null) && resultSet.next()) {
                try {
                    String columnName = "ID";
                    Integer id = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) id = null;
                    
                    columnName = "METRIC_GROUP_ID";
                    Integer metricGroupId = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) metricGroupId = null;

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

