package com.pearson.statsagg.database_objects.general_purpose;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
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
        
        try {
            PreparedStatement preparedStatement = DatabaseUtils.createPreparedStatement(connection, GeneralPurposeSql.Select_MetricGroupTagsAssociatedWithAlert);
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean isResultSetValid = DatabaseUtils.isResultSetValid(resultSet);
            if (!isResultSetValid) return null;

            Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlerts = new HashMap<>();

            while (resultSet.next()) {
                Integer alertId = resultSet.getInt("A_ID");
                if (resultSet.wasNull()) alertId = null;
                
                String tag = resultSet.getString("TAG");
                if (resultSet.wasNull()) tag = null;
                
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
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        } 
        
    }
  
}
