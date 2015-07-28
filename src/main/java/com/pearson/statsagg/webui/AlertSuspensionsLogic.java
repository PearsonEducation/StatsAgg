package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.utilities.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 This 'Logic' class was created to separate out business logic from 'AlertSuspensions'. 
 The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class AlertSuspensionsLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionsLogic.class.getName());

    public String alterRecordInDatabase(AlertSuspension alertSuspension) {
        return alterRecordInDatabase(alertSuspension, null);
    }
    
    public String alterRecordInDatabase(AlertSuspension alertSuspension, String oldName) {
        
        if ((alertSuspension == null) || (alertSuspension.getName() == null)) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter alert suspension.";
            logger.warn(returnString);
            return returnString;
        }
        
        String returnString;

        boolean isNewAlertSuspension = true, isOverwriteExistingAttempt = false;
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
        AlertSuspension alertSuspensionFromDb;

        if ((oldName != null) && !oldName.isEmpty()) {
            alertSuspensionFromDb = alertSuspensionsDao.getAlertSuspensionByName(oldName);

            if (alertSuspensionFromDb != null) {
                alertSuspension.setId(alertSuspensionFromDb.getId());
                isNewAlertSuspension = false;
            }
            else {
                isNewAlertSuspension = true;
            }
        }
        else {
            alertSuspensionFromDb = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension.getName());
            if (alertSuspensionFromDb != null) isOverwriteExistingAttempt = true;
        }

        boolean isUpsertSuccess = false;
        AlertSuspension newAlertSuspensionFromDb = null;
        if (!isOverwriteExistingAttempt) {
            isUpsertSuccess = alertSuspensionsDao.upsert(alertSuspension);
            newAlertSuspensionFromDb = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension.getName());
        }

        alertSuspensionsDao.close();

        if (isOverwriteExistingAttempt) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            
            returnString = "Failed to create alert suspension. An alert suspension with the same name already exists. AlertSuspensionName=\"" + alertSuspension.getName() + "\"";
            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
            logger.warn(cleanReturnString);
        }
        else if (isUpsertSuccess && (newAlertSuspensionFromDb != null)) {
            lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
            
            if (isNewAlertSuspension) returnString = "Successful alert suspension creation. AlertSuspensionName=\"" + alertSuspension.getName() + "\"";
            else returnString = "Successful alert suspension alteration. AlertSuspensionName=\"" + alertSuspension.getName() + "\"";
            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
            logger.info(cleanReturnString);
        }
        else {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            
            if (isNewAlertSuspension) returnString = "Failed to create alert suspension. " + "AlertSuspensionName=\"" + alertSuspension.getName() + "\"";
            else returnString = "Failed to alter alert suspension. " + "AlertSuspensionName=\"" + alertSuspension.getName() + "\"";
            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
            logger.warn(cleanReturnString);
        }
            
        return returnString;
    }
    
    public String deleteRecordInDatabase(String alertSuspensionName) {
        
        if ((alertSuspensionName == null) || alertSuspensionName.isEmpty()) {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Invalid alert suspension name. Cancelling delete operation.";
            logger.warn(returnString);
            return returnString;
        }

        String returnString;
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
        AlertSuspension alertSuspensionFromDb = alertSuspensionsDao.getAlertSuspensionByName(alertSuspensionName);
                
        if (alertSuspensionFromDb != null) {
            boolean didDeleteSucceed = alertSuspensionsDao.delete(alertSuspensionFromDb);
            
            if (!didDeleteSucceed) {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to delete alert suspension. AlertSuspensionName=\"" + alertSuspensionName + "\".";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Delete alert suspension success. AlertSuspensionName=\"" + alertSuspensionName + "\".";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.info(cleanReturnString);
            }
        }
        else {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            returnString = "Alert suspension not found. AlertSuspensionName=\"" + alertSuspensionName + "\". Cancelling delete operation.";
            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
            logger.warn(cleanReturnString);
        }
        
        alertSuspensionsDao.close();
        
        return returnString;
    }

}
