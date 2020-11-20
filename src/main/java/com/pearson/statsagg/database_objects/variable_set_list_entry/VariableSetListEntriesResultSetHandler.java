package com.pearson.statsagg.database_objects.variable_set_list_entry;

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
public class VariableSetListEntriesResultSetHandler extends VariableSetListEntry implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(VariableSetListEntriesResultSetHandler.class.getName());
    
    @Override
    public List<VariableSetListEntry> handleResultSet(ResultSet resultSet) {
        
        List<VariableSetListEntry> variableSetListEntries = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    Integer variableSetListId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variable_set_list_id", Integer.class);
                    Integer variableSetId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variable_set_id", Integer.class);

                    VariableSetListEntry variableSetListEntry = new VariableSetListEntry(id, variableSetListId, variableSetId);
                    variableSetListEntries.add(variableSetListEntry);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return variableSetListEntries;
    }

}

