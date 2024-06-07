package com.pearson.statsagg.database_objects.pagerduty_services;

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
public class PagerdutyServicesDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(PagerdutyServicesDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "PagerDuty Service";
    private static final String LOG_FRIENDLY_NAME = "PagerDutyService";
    
    private final PagerdutyService pagerdutyService_;
    
    private PagerdutyServicesDaoWrapper(PagerdutyService pagerdutyService) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.pagerdutyService_ = pagerdutyService;
    }
    
    private PagerdutyServicesDaoWrapper(PagerdutyService pagerdutyService, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.pagerdutyService_ = pagerdutyService;
    }
    
    private PagerdutyServicesDaoWrapper alterRecordInDatabase() {
        
        if ((pagerdutyService_ == null) || (pagerdutyService_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            getReturnString_AlterInitialValue(pagerdutyService_.getName());
            
            PagerdutyService pagerdutyServiceFromDb = PagerdutyServicesDao.getPagerdutyService_FilterByUppercaseName(connection, false, pagerdutyService_.getName());
            
            if (isNewDatabaseObject_ && (pagerdutyServiceFromDb != null)) {
                getReturnString_CreateFail_SameNameAlreadyExists(pagerdutyService_.getName());
            }
            else {
                boolean isUpsertSuccess;
                if (oldDatabaseObjectName_ == null) isUpsertSuccess = PagerdutyServicesDao.upsert(connection, false, true, pagerdutyService_);
                else isUpsertSuccess = PagerdutyServicesDao.upsert(connection, false, true, pagerdutyService_, oldDatabaseObjectName_);

                if (isUpsertSuccess) getReturnString_AlterSuccess(pagerdutyService_.getName());
                else getReturnString_AlterFail(pagerdutyService_.getName());
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
    
    private PagerdutyServicesDaoWrapper deleteRecordInDatabase() {
        
        if ((pagerdutyService_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((pagerdutyService_ == null) || (pagerdutyService_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            String pagerdutyServiceName = (pagerdutyService_ != null) ? pagerdutyService_.getName() : null;
            getReturnString_DeleteInitialValue(pagerdutyServiceName);
            
            boolean didDeleteSucceed = PagerdutyServicesDao.delete(connection, false, true, pagerdutyService_);
            if (!didDeleteSucceed) getReturnString_DeleteFail(pagerdutyServiceName);
            else getReturnString_DeleteSuccess(pagerdutyServiceName);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return this;
    }
    
    public static PagerdutyServicesDaoWrapper createRecordInDatabase(PagerdutyService pagerdutyService) {
        PagerdutyServicesDaoWrapper pagerdutyServicesDaoWrapper = new PagerdutyServicesDaoWrapper(pagerdutyService);
        return pagerdutyServicesDaoWrapper.alterRecordInDatabase();
    }
    
    public static PagerdutyServicesDaoWrapper alterRecordInDatabase(PagerdutyService pagerdutyService) {
        String pagerdutyServiceName = (pagerdutyService != null) ? pagerdutyService.getName() : null;
        PagerdutyServicesDaoWrapper pagerdutyServicesDaoWrapper = new PagerdutyServicesDaoWrapper(pagerdutyService, pagerdutyServiceName);
        return pagerdutyServicesDaoWrapper.alterRecordInDatabase();
    }
    
    public static PagerdutyServicesDaoWrapper alterRecordInDatabase(PagerdutyService pagerdutyService, String oldName) {
        PagerdutyServicesDaoWrapper pagerdutyServicesDaoWrapper = new PagerdutyServicesDaoWrapper(pagerdutyService, oldName);
        return pagerdutyServicesDaoWrapper.alterRecordInDatabase();
    }
    
    public static PagerdutyServicesDaoWrapper deleteRecordInDatabase(String pagerdutyServiceName) {
        PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, pagerdutyServiceName);
        PagerdutyServicesDaoWrapper pagerdutyServicesDaoWrapper = new PagerdutyServicesDaoWrapper(pagerdutyService);
        return pagerdutyServicesDaoWrapper.deleteRecordInDatabase();
    }
    
    public static PagerdutyServicesDaoWrapper deleteRecordInDatabase(PagerdutyService pagerdutyService) {
        PagerdutyServicesDaoWrapper pagerdutyServicesDaoWrapper = new PagerdutyServicesDaoWrapper(pagerdutyService);
        return pagerdutyServicesDaoWrapper.deleteRecordInDatabase();
    }
    
}
