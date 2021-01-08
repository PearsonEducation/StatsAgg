package com.pearson.statsagg.database_objects.variable_set_list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetListsSql {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetListsSql.class.getName());
    
    protected final static String Insert_VariableSetList =
                    "INSERT INTO VARIABLE_SET_LISTS " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION) " +
                    "VALUES(?,?,?)";
    
    protected final static String Update_VariableSetList_ByPrimaryKey =
                    "UPDATE VARIABLE_SET_LISTS " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_VariableSetList_ByPrimaryKey = 
                    "DELETE FROM VARIABLE_SET_LISTS " +
                    "WHERE ID = ?";
    
    protected final static String Select_VariableSetList_ByPrimaryKey = 
                    "SELECT * FROM VARIABLE_SET_LISTS " +
                    "WHERE ID = ?";
    
    protected final static String Select_VariableSetList_ByName = 
                    "SELECT * FROM VARIABLE_SET_LISTS " +
                    "WHERE NAME = ?";
    
    protected final static String Select_VariableSetList_ByUppercaseName = 
                    "SELECT * FROM VARIABLE_SET_LISTS " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_VariableSetList_Names = 
                    "SELECT NAME FROM VARIABLE_SET_LISTS";
    
    protected final static String Select_VariableSetList_Names_OrderByName = 
                    "SELECT NAME FROM VARIABLE_SET_LISTS " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";
    
    protected final static String Select_AllVariableSetLists = 
                    "SELECT * FROM VARIABLE_SET_LISTS";

    protected final static String Select_DistinctVariableSetListIds = 
                    "SELECT DISTINCT ID FROM VARIABLE_SET_LISTS";
    
    protected final static String Select_AllVariableSetLists_IdsAndNames = 
                    "SELECT ID, NAME FROM VARIABLE_SET_LISTS";
    
}
