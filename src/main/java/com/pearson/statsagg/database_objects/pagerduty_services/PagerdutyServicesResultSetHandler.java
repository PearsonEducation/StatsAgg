package com.pearson.statsagg.database_objects.pagerduty_services;

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
public class PagerdutyServicesResultSetHandler extends PagerdutyService implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PagerdutyServicesResultSetHandler.class.getName());
    
    @Override
    public List<PagerdutyService> handleResultSet(ResultSet resultSet) {
        
        List<PagerdutyService> pagerdutyServices = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    String description = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "description", String.class);
                    String routingKey = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "routing_key", String.class);

                    PagerdutyService pagerdutyService = new PagerdutyService(id, name, uppercaseName, description, routingKey);
                    pagerdutyServices.add(pagerdutyService);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return pagerdutyServices;
    }

}

