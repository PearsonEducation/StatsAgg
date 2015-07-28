package com.pearson.statsagg.database_objects.general_purpose;

import com.pearson.statsagg.database_engine.DatabaseDao;
import com.pearson.statsagg.database_engine.DatabaseInterface;
import com.pearson.statsagg.utilities.StackTrace;
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
public class GeneralPurposeDao extends DatabaseDao {

    private static final Logger logger = LoggerFactory.getLogger(GeneralPurposeDao.class.getName());
    
    public GeneralPurposeDao(){}
            
    public GeneralPurposeDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public GeneralPurposeDao(DatabaseInterface databaseInterface) {
        super(databaseInterface);
    }
    
    /* 
    Does not include alerts with no tags
    Returns Map<AlertId,Set<Associated Metric Group Tag>>
    */
    public Map<Integer,Set<String>> getMetricGroupTagsAssociatedWithAlerts() {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(GeneralPurposeSql.Select_MetricGroupTagsAssociatedWithAlert, 1000);
            databaseInterface_.addPreparedStatementParameters();
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }
            
            Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlerts = new HashMap<>();
            
            ResultSet resultSet = databaseInterface_.getResults();
            
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
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
     
}
