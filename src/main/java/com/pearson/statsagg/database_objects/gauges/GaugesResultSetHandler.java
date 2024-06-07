package com.pearson.statsagg.database_objects.gauges;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseResultSetHandler;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.math.BigDecimal;
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
public class GaugesResultSetHandler extends Gauge implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GaugesResultSetHandler.class.getName());
    
    @Override
    public List<Gauge> handleResultSet(ResultSet resultSet) {
        
        List<Gauge> gauges = new ArrayList<>();
        
        try {
            Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);
            
            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    String bucketSha1 = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "bucket_sha1", String.class);
                    String bucket = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "bucket", String.class);
                    BigDecimal metricValue = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_value", BigDecimal.class);
                    Timestamp lastModified = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "last_modified", Timestamp.class);

                    Gauge gauge = new Gauge(bucketSha1, bucket, metricValue, lastModified);
                    gauges.add(gauge);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return gauges;
    }

}

