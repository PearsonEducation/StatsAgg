package com.pearson.statsagg.threads.template_related;

import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntriesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Common {
    
    private static final Logger logger = LoggerFactory.getLogger(Common.class.getName());

    public static List<VariableSet> getVariableSetsFromVariableSetIdList(Integer variableSetListId) {
        
        if (variableSetListId == null) {
            return new ArrayList<>();
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            
            List<Integer> variableSetIds = VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(connection, false, variableSetListId);
            
            if (variableSetIds == null) return null;
            
            List<VariableSet> variableSets = new ArrayList<>();

            for (Integer variableSetId : variableSetIds) {
                if (variableSetId == null) continue;

                VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetId);
                if (variableSet != null) variableSets.add(variableSet);
            }

            return variableSets;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }

    }
    
    public static String getStringWithVariableSubsistution(String inputString, VariableSet variableSet) {
        if (inputString == null) return null;
        if (inputString.isEmpty()) return inputString;
        if (variableSet == null) return inputString;

        Map<String,String> variables = variableSet.getVariables_Map();
        if ((variables == null) || variables.isEmpty()) return inputString;
        
        String returnString = inputString;

        for (String key : variables.keySet()) {
            if ((key == null) || key.isEmpty()) continue;
            returnString = StringUtils.replace(returnString, ("```" + key + "```"), variables.get(key));
        }
        
        return returnString;
    }
    
    public static Set<String> getUppercaseStringSet(Set<String> inputStrings) {
        if (inputStrings == null) return null;
        if (inputStrings.isEmpty()) return new HashSet<>();
        
        HashSet<String> uppercaseInputStrings = new HashSet<>();
        
        for (String inputString : inputStrings) {
            if (inputString == null) continue;
            uppercaseInputStrings.add(inputString.toUpperCase());
        }
        
        return uppercaseInputStrings;
    }
    
    public static Set<String> getNamesThatTemplateWantsToCreate(Integer variableSetListId, String nameVariable) {
        
        if (variableSetListId == null) {
            return null;
        }
        
        Connection connection = null;
     
        try {
            connection = DatabaseConnections.getConnection();
             
            Set<String> derivedAlertNames = new HashSet<>();
            
            List<Integer> variableSetIdsAssociatedWithAlertTemplate_List =  VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(connection, false, variableSetListId);
            if (variableSetIdsAssociatedWithAlertTemplate_List == null) return null; // if this is null, something went wrong querying the db
            Set<Integer> variableSetIdsAssociatedWithAlertTemplate_Set = new HashSet<>(variableSetIdsAssociatedWithAlertTemplate_List);

            for (Integer variableSetIdAssociatedWithAlertTemplate : variableSetIdsAssociatedWithAlertTemplate_Set) {
                VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetIdAssociatedWithAlertTemplate);
                String derivedAlertName = Common.getStringWithVariableSubsistution(nameVariable, variableSet);
                if (derivedAlertName == null) continue; // invalid data condition
                derivedAlertNames.add(derivedAlertName);
            }
            
            return derivedAlertNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }        
        finally {
            DatabaseUtils.cleanup(connection);  
        }
        
    }
    
    public static Map<Integer,String> getNamesThatTemplateWantsToCreate_ByVariableSetId(Integer variableSetListId, String nameVariable) {
        
        if (variableSetListId == null) {
            return null;
        }
        
        Connection connection = null;
     
        try {
            connection = DatabaseConnections.getConnection();
             
            Map<Integer,String> derivedAlertNames_ByVariableSetId = new HashMap<>();
            
            List<Integer> variableSetIdsAssociatedWithAlertTemplate_List =  VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(connection, false, variableSetListId);
            if (variableSetIdsAssociatedWithAlertTemplate_List == null) return null; // if this is null, something went wrong querying the db
            Set<Integer> variableSetIdsAssociatedWithAlertTemplate_Set = new HashSet<>(variableSetIdsAssociatedWithAlertTemplate_List);

            for (Integer variableSetIdAssociatedWithAlertTemplate : variableSetIdsAssociatedWithAlertTemplate_Set) {
                VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetIdAssociatedWithAlertTemplate);
                String derivedAlertName = Common.getStringWithVariableSubsistution(nameVariable, variableSet);
                if ((derivedAlertName == null) || (variableSet == null) || (variableSet.getId() == null)) continue; // invalid data condition
                derivedAlertNames_ByVariableSetId.put(variableSet.getId(), derivedAlertName);
            }
            
            return derivedAlertNames_ByVariableSetId;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }        
        finally {
            DatabaseUtils.cleanup(connection);  
        }
        
    }
    
}
