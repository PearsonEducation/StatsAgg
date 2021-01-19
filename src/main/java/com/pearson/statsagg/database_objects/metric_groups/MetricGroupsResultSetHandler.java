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
public class MetricGroupsResultSetHandler extends MetricGroup implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsResultSetHandler.class.getName());
    
    @Override
    public List<MetricGroup> handleResultSet(ResultSet resultSet) {
        
        List<MetricGroup> metricGroups = new ArrayList<>();
        
        try {
            Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    String description = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "description", String.class);
                    Integer metricGroupTemplateId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_template_id", Integer.class);
                    Integer variableSetId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variable_set_id", Integer.class);
                    
                    MetricGroup metricGroup = new MetricGroup(id, name, uppercaseName, description, metricGroupTemplateId, variableSetId, null, null, null);
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

