package com.pearson.statsagg.database_objects.general_purpose;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.db_utils.PreparedStatementAndResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GeneralPurposeDao {

    private static final Logger logger = LoggerFactory.getLogger(GeneralPurposeDao.class.getName());
    
    /* 
    Does not include alerts with no tags
    Returns Map<AlertId,Set<Associated Metric Group Tag>>
    */
    public static Map<Integer,Set<String>> getMetricGroupTagsAssociatedWithAlerts(Connection connection, boolean closeConnectionOnCompletion) {
        
        PreparedStatementAndResultSet preparedStatementAndResultSet = null;
        
        try {
            preparedStatementAndResultSet = DatabaseUtils.query_PreparedStatement(connection, GeneralPurposeSql.Select_MetricGroupTagsAssociatedWithAlert);
            Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlerts = new HashMap<>();

            while (preparedStatementAndResultSet.getResultSet().next()) {
                Integer alertId = DatabaseUtils.getResultSetValue(preparedStatementAndResultSet.getResultSet(), "a_id", Integer.class);
                String tag = DatabaseUtils.getResultSetValue(preparedStatementAndResultSet.getResultSet(), "tag", String.class);

                if ((alertId != null) && (tag != null) && !tag.isEmpty()) {
                    if (!metricGroupTagsAssociatedWithAlerts.containsKey(alertId)) {
                        Set<String> metricGroupTags = new HashSet<>();
                        metricGroupTags.add(tag);
                        metricGroupTagsAssociatedWithAlerts.put(alertId, metricGroupTags);
                    }
                    else {
                        Set<String> metricGroupTags = metricGroupTagsAssociatedWithAlerts.get(alertId);
                        metricGroupTags.add(tag);
                    }
                }
            }

            return metricGroupTagsAssociatedWithAlerts;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection, preparedStatementAndResultSet);
            else DatabaseUtils.cleanup(preparedStatementAndResultSet);
        } 
        
    }
  
}
