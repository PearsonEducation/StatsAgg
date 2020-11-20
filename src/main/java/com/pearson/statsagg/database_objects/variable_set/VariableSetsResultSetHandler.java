package com.pearson.statsagg.database_objects.variable_set;

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
public class VariableSetsResultSetHandler extends VariableSet implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(VariableSetsResultSetHandler.class.getName());
    
    @Override
    public List<VariableSet> handleResultSet(ResultSet resultSet) {
        
        List<VariableSet> variableSets = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    String description = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "description", String.class);
                    String variables = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variables", String.class);

                    VariableSet variableSet = new VariableSet(id, name, uppercaseName, description, variables);
                    variableSets.add(variableSet);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return variableSets;
    }

}

