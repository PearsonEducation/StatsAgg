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
public class MetricGroupTagsResultSetHandler extends MetricGroupTag implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTagsResultSetHandler.class.getName());
    
    @Override
    public List<MetricGroupTag> handleResultSet(ResultSet resultSet) {
        
        List<MetricGroupTag> metricGroupTags = new ArrayList<>();
        
        try {
            Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    Integer metricGroupId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_id", Integer.class);
                    String tag = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "tag", String.class);

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

