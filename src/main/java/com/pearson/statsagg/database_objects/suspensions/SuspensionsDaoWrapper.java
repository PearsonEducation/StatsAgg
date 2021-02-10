package com.pearson.statsagg.database_objects.suspensions;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.database_objects.AbstractDaoWrapper;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SuspensionsDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(SuspensionsDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "suspension";
    private static final String LOG_FRIENDLY_NAME = "Suspension";
    
    private final Suspension suspension_;
    
    private SuspensionsDaoWrapper(Suspension suspension) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.suspension_ = suspension;
    }
    
    private SuspensionsDaoWrapper(Suspension suspension, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.suspension_ = suspension;
    }
    
    private SuspensionsDaoWrapper alterRecordInDatabase() {
        
        if ((suspension_ == null) || (suspension_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        synchronized (GlobalVariables.suspensionChanges) {
            Connection connection = DatabaseConnections.getConnection(false);
                
            try {
                getReturnString_AlterInitialValue(suspension_.getName());
                
                Suspension suspensionFromDb = SuspensionsDao.getSuspension_FilterByUppercaseName(connection, false, suspension_.getName());
                
                if (isNewDatabaseObject_ && (suspensionFromDb != null)) { 
                    getReturnString_CreateFail_SameNameAlreadyExists(suspension_.getName());
                }
                else {
                    boolean isUpsertSuccess;
                    if (oldDatabaseObjectName_ == null) isUpsertSuccess = SuspensionsDao.upsert(connection, false, true, suspension_);
                    else isUpsertSuccess = SuspensionsDao.upsert(connection, false, true, suspension_, oldDatabaseObjectName_);

                    if (isUpsertSuccess) {
                        Suspension suspensionFromDb_AfterUpsert = SuspensionsDao.getSuspension(connection, false, suspension_.getName());
                        
                        if (isNewDatabaseObject_) GlobalVariables.suspensionChanges.put(suspensionFromDb_AfterUpsert.getId(), GlobalVariables.NEW);
                        else GlobalVariables.suspensionChanges.put(suspensionFromDb_AfterUpsert.getId(), GlobalVariables.ALTER);
                        
                        getReturnString_AlterSuccess(suspension_.getName());
                    }
                    else getReturnString_AlterFail(suspension_.getName());
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                DatabaseUtils.cleanup(connection);
            }
        }
        
        return this;
    }
    
    private SuspensionsDaoWrapper deleteRecordInDatabase() {
        
        if ((suspension_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((suspension_ == null) || (suspension_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        synchronized (GlobalVariables.suspensionChanges) {
            Connection connection = DatabaseConnections.getConnection(false);
                
            try {
                String suspensionName = (suspension_ != null) ? suspension_.getName() : null;
                getReturnString_DeleteInitialValue(suspensionName);
                
                boolean didDeleteSucceed = SuspensionsDao.delete(connection, false, true, suspension_);
                
                if (!didDeleteSucceed) {
                    getReturnString_DeleteFail(suspensionName);
                }
                else {
                    GlobalVariables.suspensionChanges.put(suspension_.getId(), GlobalVariables.REMOVE);
                    getReturnString_DeleteSuccess(suspensionName);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                DatabaseUtils.cleanup(connection);
            }
        }
        
        return this;
    }
    
    public static SuspensionsDaoWrapper createRecordInDatabase(Suspension suspension) {
        SuspensionsDaoWrapper suspensionsDaoWrapper = new SuspensionsDaoWrapper(suspension);
        return suspensionsDaoWrapper.alterRecordInDatabase();
    }
    
    public static SuspensionsDaoWrapper alterRecordInDatabase(Suspension suspension) {
        String suspensionName = (suspension != null) ? suspension.getName() : null;
        SuspensionsDaoWrapper suspensionsDaoWrapper = new SuspensionsDaoWrapper(suspension, suspensionName);
        return suspensionsDaoWrapper.alterRecordInDatabase();
    }
    
    public static SuspensionsDaoWrapper alterRecordInDatabase(Suspension suspension, String oldName) {
        SuspensionsDaoWrapper suspensionsDaoWrapper = new SuspensionsDaoWrapper(suspension, oldName);
        return suspensionsDaoWrapper.alterRecordInDatabase();
    }
    
    public static SuspensionsDaoWrapper deleteRecordInDatabase(String suspensionsName) {
        Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, suspensionsName);
        SuspensionsDaoWrapper suspensionsDaoWrapper = new SuspensionsDaoWrapper(suspension);
        return suspensionsDaoWrapper.deleteRecordInDatabase();
    }
    
    public static SuspensionsDaoWrapper deleteRecordInDatabase(Suspension suspension) {
        SuspensionsDaoWrapper suspensionsDaoWrapper = new SuspensionsDaoWrapper(suspension);
        return suspensionsDaoWrapper.deleteRecordInDatabase();
    }
    
}
