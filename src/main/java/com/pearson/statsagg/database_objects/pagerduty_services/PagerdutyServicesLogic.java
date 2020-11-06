package com.pearson.statsagg.database_objects.pagerduty_services;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import com.pearson.statsagg.web_ui.AbstractDatabaseInteractionLogic;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 This 'Logic' class was created to separate out business logic from 'PagerdutyServices'. 
 The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class PagerdutyServicesLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(PagerdutyServicesLogic.class.getName());
    
    public String alterRecordInDatabase(PagerdutyService pagerdutyService) {
        return alterRecordInDatabase(pagerdutyService, null);
    }
    
    public String alterRecordInDatabase(PagerdutyService pagerdutyService, String oldName) {
        
        if ((pagerdutyService == null) || (pagerdutyService.getName() == null)) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter PagerDuty Service.";
            logger.warn(returnString);
            return returnString;
        }
        
        String returnString;
        boolean isNewPagerdutyService = true, isOverwriteExistingAttempt = false, isUpsertSuccess = false;
        PagerdutyService newPagerdutyServiceFromDb = null;
        
        Connection connection = DatabaseConnections.getConnection();
        DatabaseUtils.setAutoCommit(connection, false);
            
        try {
            PagerdutyService pagerdutyServiceFromDb;

            if ((oldName != null) && !oldName.isEmpty()) {
                pagerdutyServiceFromDb = PagerdutyServicesDao.getPagerdutyService(connection, false, oldName);

                if (pagerdutyServiceFromDb != null) {
                    pagerdutyService.setId(pagerdutyServiceFromDb.getId());
                    isNewPagerdutyService = false;
                }
                else {
                    isNewPagerdutyService = true;
                }
            }
            else {
                pagerdutyServiceFromDb = PagerdutyServicesDao.getPagerdutyService(connection, false, pagerdutyService.getName());
                if (pagerdutyServiceFromDb != null) isOverwriteExistingAttempt = true;
            }

            if (!isOverwriteExistingAttempt) {
                isUpsertSuccess = PagerdutyServicesDao.upsert(connection, false, true, pagerdutyService);
                newPagerdutyServiceFromDb = PagerdutyServicesDao.getPagerdutyService(connection, false, pagerdutyService.getName());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
                
        if (isOverwriteExistingAttempt) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            
            returnString = "Failed to create PagerDuty Service. A PagerDuty Service with the same name already exists. PagerDutyServiceName=\"" + pagerdutyService.getName() + "\"";
            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
            logger.warn(cleanReturnString);
        }
        else if (isUpsertSuccess && (newPagerdutyServiceFromDb != null)) {
            lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
            
            if (isNewPagerdutyService) returnString = "Successful PagerDuty Service creation. PagerDutyServiceName=\"" + pagerdutyService.getName() + "\"";
            else returnString = "Successful PagerDuty Service alteration. PagerDutyServiceName=\"" + pagerdutyService.getName() + "\"";
            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
            logger.info(cleanReturnString);
        }
        else {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            
            if (isNewPagerdutyService) returnString = "Failed to create PagerDuty Service. " + "PagerDutyServiceName=\"" + pagerdutyService.getName() + "\"";
            else returnString = "Failed to alter PagerDuty Service. " + "PagerDutyServiceName=\"" + pagerdutyService.getName() + "\"";
            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
            logger.warn(cleanReturnString);
        }
            
        return returnString;
    }
    
    public String deleteRecordInDatabase(String pagerdutyServiceName) {
        
        if ((pagerdutyServiceName == null) || pagerdutyServiceName.isEmpty()) {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Invalid PagerDuty Service name. Cancelling delete operation.";
            logger.warn(returnString);
            return returnString;
        }

        String returnString = "Error deleting PagerDuty Service. PagerDutyServiceName=\"" + pagerdutyServiceName + "\".";
        
        Connection connection = DatabaseConnections.getConnection();
        DatabaseUtils.setAutoCommit(connection, false);
            
        try {
            PagerdutyService pagerdutyServiceFromDb = PagerdutyServicesDao.getPagerdutyService(connection, false, pagerdutyServiceName);

            if (pagerdutyServiceFromDb != null) {
                boolean didDeleteSucceed = PagerdutyServicesDao.delete(connection, false, true, pagerdutyServiceFromDb);

                if (!didDeleteSucceed) {
                    lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Failed to delete PagerDuty Service. PagerDutyServiceName=\"" + pagerdutyServiceName + "\".";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.warn(cleanReturnString);
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                    returnString = "Delete PagerDuty Service success. PagerDutyServiceName=\"" + pagerdutyServiceName + "\".";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.info(cleanReturnString);
                }
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "PagerDuty Service not found. PagerDutyServiceName=\"" + pagerdutyServiceName + "\". Cancelling delete operation.";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return returnString;
    }

}
