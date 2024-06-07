package com.pearson.statsagg.database_objects.metric_group_templates;

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
public class MetricGroupTemplatesResultSetHandler extends MetricGroupTemplate implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplatesResultSetHandler.class.getName());
    
    @Override
    public List<MetricGroupTemplate> handleResultSet(ResultSet resultSet) {
        
        List<MetricGroupTemplate> metricGroupTemplates = new ArrayList<>();
        
        try {
            Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    Integer variableSetListId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variable_set_list_id", Integer.class);
                    String metricGroupNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "metric_group_name_variable", String.class);
                    String descriptionVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "description_variable", String.class);
                    String matchRegexesVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "match_regexes_variable", String.class);
                    String blacklistRegexesVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "blacklist_regexes_variable", String.class);
                    String tagsVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "tags_variable", String.class);
                    Boolean isMarkedForDelete = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_marked_for_delete", Boolean.class);

                    MetricGroupTemplate metricGroupTemplate = new MetricGroupTemplate(id, name, uppercaseName, 
                            variableSetListId, metricGroupNameVariable, descriptionVariable,
                            matchRegexesVariable, blacklistRegexesVariable, tagsVariable,
                            isMarkedForDelete);
            
                    metricGroupTemplates.add(metricGroupTemplate);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return metricGroupTemplates;
    }

}

