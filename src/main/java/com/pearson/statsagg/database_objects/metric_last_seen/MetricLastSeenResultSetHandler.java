package com.pearson.statsagg.database_objects.metric_last_seen;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseResultSetHandler;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricLastSeenResultSetHandler extends MetricLastSeen implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricLastSeenResultSetHandler.class.getName());
    
    @Override
    public List<MetricLastSeen> handleResultSet(ResultSet resultSet) {
        
        List<MetricLastSeen> metricLastSeens = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    String metricKeySha1 = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_key_sha1", String.class);
                    String metricKey = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_key", String.class);
                    Timestamp lastModified = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "last_modified", Timestamp.class);

                    MetricLastSeen metricLastSeen = new MetricLastSeen(metricKeySha1, metricKey, lastModified);
                    metricLastSeens.add(metricLastSeen);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return metricLastSeens;
    }

}

