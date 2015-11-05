package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 This 'Logic' class was created to separate out business logic from 'Suspensions'. 
 The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class AlertSuspensionsLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionsLogic.class.getName());
    
    public String alterRecordInDatabase(AlertSuspension suspension) {
        return alterRecordInDatabase(suspension, null);
    }
    
    public String alterRecordInDatabase(AlertSuspension suspension, String oldName) {
        
        if ((suspension == null) || (suspension.getName() == null)) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter alert suspension.";
            logger.warn(returnString);
            return returnString;
        }
        
        String returnString;
        
        synchronized (GlobalVariables.suspensionChanges) {
            boolean isNewSuspension = true, isOverwriteExistingAttempt = false;
            AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
            AlertSuspension suspensionFromDb;

            if ((oldName != null) && !oldName.isEmpty()) {
                suspensionFromDb = alertSuspensionsDao.getSuspensionByName(oldName);

                if (suspensionFromDb != null) {
                    suspension.setId(suspensionFromDb.getId());
                    isNewSuspension = false;
                }
                else {
                    isNewSuspension = true;
                }
            }
            else {
                suspensionFromDb = alertSuspensionsDao.getSuspensionByName(suspension.getName());
                if (suspensionFromDb != null) isOverwriteExistingAttempt = true;
            }

            boolean isUpsertSuccess = false;
            AlertSuspension newSuspensionFromDb = null;
            if (!isOverwriteExistingAttempt) {
                isUpsertSuccess = alertSuspensionsDao.upsert(suspension);
                newSuspensionFromDb = alertSuspensionsDao.getSuspensionByName(suspension.getName());
            }

            alertSuspensionsDao.close();

            if (isOverwriteExistingAttempt) {
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;

                returnString = "Failed to create suspension. An suspension with the same name already exists. SuspensionName=\"" + suspension.getName() + "\"";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }
            else if (isUpsertSuccess && (newSuspensionFromDb != null)) {
                lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;

                if (isNewSuspension) GlobalVariables.suspensionChanges.put(newSuspensionFromDb.getId(), GlobalVariables.NEW);
                else GlobalVariables.suspensionChanges.put(newSuspensionFromDb.getId(), GlobalVariables.ALTER);

                if (isNewSuspension) returnString = "Successful suspension creation. SuspensionName=\"" + suspension.getName() + "\"";
                else returnString = "Successful suspension alteration. SuspensionName=\"" + suspension.getName() + "\"";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.info(cleanReturnString);
            }
            else {
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;

                if (isNewSuspension) returnString = "Failed to create suspension. " + "SuspensionName=\"" + suspension.getName() + "\"";
                else returnString = "Failed to alter suspension. " + "SuspensionName=\"" + suspension.getName() + "\"";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }
        }
        
        return returnString;
    }
    
    public String deleteRecordInDatabase(String suspensionName) {
        
        if ((suspensionName == null) || suspensionName.isEmpty()) {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Invalid suspension name. Cancelling delete operation.";
            logger.warn(returnString);
            return returnString;
        }

        String returnString;
        
        synchronized (GlobalVariables.suspensionChanges) {
            AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
            AlertSuspension suspensionFromDb = alertSuspensionsDao.getSuspensionByName(suspensionName);

            if (suspensionFromDb != null) {
                boolean didDeleteSucceed = alertSuspensionsDao.delete(suspensionFromDb);

                if (!didDeleteSucceed) {
                    lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Failed to delete suspension. SuspensionName=\"" + suspensionName + "\".";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.warn(cleanReturnString);
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                    GlobalVariables.suspensionChanges.put(suspensionFromDb.getId(), GlobalVariables.REMOVE);
                    returnString = "Delete suspension success. SuspensionName=\"" + suspensionName + "\".";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.info(cleanReturnString);
                }
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Suspension not found. SuspensionName=\"" + suspensionName + "\". Cancelling delete operation.";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }

            alertSuspensionsDao.close();
        }
        
        return returnString;
    }

}
