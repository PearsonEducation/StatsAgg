package com.pearson.statsagg.database_objects.variable_set_list_entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetListEntriesSql {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetListEntriesSql.class.getName());
    
    protected final static String Insert_VariableSetListEntry =
                    "INSERT INTO VARIABLE_SET_LIST_ENTRIES " +
                    "(VARIABLE_SET_LIST_ID, VARIABLE_SET_ID) " +
                    "VALUES(?,?)";
    
    protected final static String Update_VariableSetListEntry_ByPrimaryKey =
                    "UPDATE VARIABLE_SET_LIST_ENTRIES " +
                    "SET VARIABLE_SET_LIST_ID = ?, VARIABLE_SET_ID = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_VariableSetListEntry_ByPrimaryKey = 
                    "DELETE FROM VARIABLE_SET_LIST_ENTRIES " +
                    "WHERE ID = ?";
    
    protected final static String Delete_VariableSetListEntry_ByVariableSetListId = 
                    "DELETE FROM VARIABLE_SET_LIST_ENTRIES " +
                    "WHERE VARIABLE_SET_LIST_ID = ?";
    
    protected final static String Select_VariableSetListEntry_ByPrimaryKey = 
                    "SELECT * FROM VARIABLE_SET_LIST_ENTRIES " +
                    "WHERE ID = ?";

    protected final static String Select_AllVariableSetListEntries = 
                    "SELECT * FROM VARIABLE_SET_LIST_ENTRIES";
    
    protected final static String Select_VariableSetListEntries_ByVariableSetListId = 
                    "SELECT * FROM VARIABLE_SET_LIST_ENTRIES " +
                    "WHERE VARIABLE_SET_LIST_ID = ?";
    
    protected final static String Select_VariableSetListEntries_ByVariableSetId = 
                    "SELECT * FROM VARIABLE_SET_LIST_ENTRIES " +
                    "WHERE VARIABLE_SET_ID = ?";
    
    protected final static String Select_VariableSetIds_ByVariableSetListId = 
                    "SELECT VARIABLE_SET_ID FROM VARIABLE_SET_LIST_ENTRIES " +
                    "WHERE VARIABLE_SET_LIST_ID = ?";

    protected final static String Select_DistinctVariableSetIds = 
                    "SELECT DISTINCT(VARIABLE_SET_ID) FROM VARIABLE_SET_LIST_ENTRIES";
}
