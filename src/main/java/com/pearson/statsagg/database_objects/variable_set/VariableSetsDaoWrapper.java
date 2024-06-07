package com.pearson.statsagg.database_objects.variable_set;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.database_objects.AbstractDaoWrapper;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetsDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetsDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "variable set";
    private static final String LOG_FRIENDLY_NAME = "VariableSet";
    
    private final VariableSet variableSet_;
    
    private VariableSetsDaoWrapper(VariableSet variableSet) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.variableSet_ = variableSet;
    }
    
    private VariableSetsDaoWrapper(VariableSet variableSet, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.variableSet_ = variableSet;
    }
    
    private VariableSetsDaoWrapper alterRecordInDatabase() {
        
        if ((variableSet_ == null) || (variableSet_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            getReturnString_AlterInitialValue(variableSet_.getName());
            
            VariableSet variableSetFromDb = VariableSetsDao.getVariableSet_FilterByUppercaseName(connection, false, variableSet_.getName());

            if (isNewDatabaseObject_ && (variableSetFromDb != null)) {
                getReturnString_CreateFail_SameNameAlreadyExists(variableSet_.getName());
            }
            else {
                boolean isUpsertSuccess;
                if (oldDatabaseObjectName_ == null) isUpsertSuccess = VariableSetsDao.upsert(connection, false, true, variableSet_);
                else isUpsertSuccess = VariableSetsDao.upsert(connection, false, true, variableSet_, oldDatabaseObjectName_);
                
                if (isUpsertSuccess) getReturnString_AlterSuccess(variableSet_.getName());
                else getReturnString_AlterFail(variableSet_.getName());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
              
        return this;
    }
    
    private VariableSetsDaoWrapper deleteRecordInDatabase() {
        
        if ((variableSet_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((variableSet_ == null) || (variableSet_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            String variableSetName = (variableSet_ != null) ? variableSet_.getName() : null;
            getReturnString_DeleteInitialValue(variableSetName);
            
            boolean didDeleteSucceed = VariableSetsDao.delete(connection, false, true, variableSet_);
            if (!didDeleteSucceed) getReturnString_DeleteFail(variableSetName);
            else getReturnString_DeleteSuccess(variableSetName);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return this;
    }
    
    public static VariableSetsDaoWrapper createRecordInDatabase(VariableSet variableSet) {
        VariableSetsDaoWrapper variableSetsDaoWrapper = new VariableSetsDaoWrapper(variableSet);
        return variableSetsDaoWrapper.alterRecordInDatabase();
    }
    
    public static VariableSetsDaoWrapper alterRecordInDatabase(VariableSet variableSet) {
        String variableSetName = (variableSet != null) ? variableSet.getName() : null;
        VariableSetsDaoWrapper variableSetsDaoWrapper = new VariableSetsDaoWrapper(variableSet, variableSetName);
        return variableSetsDaoWrapper.alterRecordInDatabase();
    }
    
    public static VariableSetsDaoWrapper alterRecordInDatabase(VariableSet variableSet, String oldName) {
        VariableSetsDaoWrapper variableSetsDaoWrapper = new VariableSetsDaoWrapper(variableSet, oldName);
        return variableSetsDaoWrapper.alterRecordInDatabase();
    }
    
    public static VariableSetsDaoWrapper deleteRecordInDatabase(String variableSetName) {
        VariableSet variableSet = VariableSetsDao.getVariableSet(DatabaseConnections.getConnection(), true, variableSetName);
        VariableSetsDaoWrapper variableSetsDaoWrapper = new VariableSetsDaoWrapper(variableSet);
        return variableSetsDaoWrapper.deleteRecordInDatabase();
    }
    
    public static VariableSetsDaoWrapper deleteRecordInDatabase(VariableSet variableSet) {
        VariableSetsDaoWrapper variableSetsDaoWrapper = new VariableSetsDaoWrapper(variableSet);
        return variableSetsDaoWrapper.deleteRecordInDatabase();
    }
    
}
