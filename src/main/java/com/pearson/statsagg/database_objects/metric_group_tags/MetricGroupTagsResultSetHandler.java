package com.pearson.statsagg.database_objects.metric_group_tags;

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
public class MetricGroupTagsResultSetHandler extends MetricGroupTag implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTagsResultSetHandler.class.getName());
    
    @Override
    public List<MetricGroupTag> handleResultSet(ResultSet resultSet) {
        
        List<MetricGroupTag> metricGroupTags = new ArrayList<>();
        
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

                    columnName = "TAG";
                    String tag = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) tag = null;

                    MetricGroupTag metricGroupTag = new MetricGroupTag(id, metricGroupId, tag);
                    metricGroupTags.add(metricGroupTag);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return metricGroupTags;
    }

}

