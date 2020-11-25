package com.pearson.statsagg.database_objects.variable_set_list;

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
public class VariableSetListsResultSetHandler extends VariableSetList implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(VariableSetListsResultSetHandler.class.getName());
    
    @Override
    public List<VariableSetList> handleResultSet(ResultSet resultSet) {
        
        List<VariableSetList> variableSetLists = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    String description = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "description", String.class);

                    VariableSetList variableSetList = new VariableSetList(id, name, uppercaseName, description);
                    variableSetLists.add(variableSetList);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return variableSetLists;
    }

}

