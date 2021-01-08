package com.pearson.statsagg.database_objects.variable_set;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetsSql {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetsSql.class.getName());
    
    protected final static String Insert_VariableSet =
                    "INSERT INTO VARIABLE_SETS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, VARIABLES) " +
                    "VALUES(?,?,?,?)";
    
    protected final static String Update_VariableSet_ByPrimaryKey =
                    "UPDATE VARIABLE_SETS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, VARIABLES = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_VariableSet_ByPrimaryKey = 
                    "DELETE FROM VARIABLE_SETS " +
                    "WHERE ID = ?";
    
    protected final static String Select_AllVariableSets = 
                    "SELECT * FROM VARIABLE_SETS";
    
    protected final static String Select_VariableSet_ByPrimaryKey = 
                    "SELECT * FROM VARIABLE_SETS " +
                    "WHERE ID = ?";
    
    protected final static String Select_VariableSet_ByName = 
                    "SELECT * FROM VARIABLE_SETS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_VariableSet_ByUppercaseName = 
                    "SELECT * FROM VARIABLE_SETS " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected static final String selectVariableSetNames_ByListOfVariableSetIds_OrderByNameAsc(List<Integer> variableSetIds) {
        StringBuilder parameters = new StringBuilder();
        
        if ((variableSetIds != null) && !variableSetIds.isEmpty()) {
            for (int i = 0; i < variableSetIds.size(); i++) {
                parameters.append("?");
                if ((i+1) < variableSetIds.size()) parameters.append(",");
            }
        }
        
        return "SELECT NAME FROM VARIABLE_SETS WHERE ID IN ( " + parameters.toString() + " ) ORDER BY NAME ASC";
    }

}
