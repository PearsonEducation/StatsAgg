package com.pearson.statsagg.database_objects.metric_group;

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
public class MetricGroupsResultSetHandler extends MetricGroup implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsResultSetHandler.class.getName());
    
    @Override
    public List<MetricGroup> handleResultSet(ResultSet resultSet) {
        
        List<MetricGroup> metricGroups = new ArrayList<>();
        
        try {
            Set<String> columnNames = DatabaseUtils.getResultSetColumns(resultSet);
            
            while ((columnNames != null) && resultSet.next()) {
                try {
                    String columnName = "ID";
                    Integer id = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) id = null;

                    columnName = "NAME";
                    String name = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) name = null;

                    columnName = "UPPERCASE_NAME";
                    String uppercaseName = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) uppercaseName = null;

                    columnName = "DESCRIPTION";
                    String description = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) description = null;

                    MetricGroup metricGroup = new MetricGroup(id, name, uppercaseName, description);
                    metricGroups.add(metricGroup);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return metricGroups;
    }

}

