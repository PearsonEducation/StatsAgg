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
            Set<String> columnNames = DatabaseUtils.getResultSetColumns(resultSet);
            
            while ((columnNames != null) && resultSet.next()) {
                try {
                    String columnName = "BUCKET_SHA1";
                    String bucketSha1 = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) bucketSha1 = null;

                    columnName = "BUCKET";
                    String bucket = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) bucket = null;

                    columnName = "METRIC_VALUE";
                    BigDecimal metricValue = (columnNames.contains(columnName)) ? resultSet.getBigDecimal(columnName) : null;
                    if (resultSet.wasNull()) metricValue = null;

                    columnName = "LAST_MODIFIED";
                    Timestamp lastModified = (columnNames.contains(columnName)) ? resultSet.getTimestamp(columnName) : null;
                    if (resultSet.wasNull()) lastModified = null;
                    
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

