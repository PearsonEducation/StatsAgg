package com.pearson.statsagg.network.http;

import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * This 'Logic' class was created to separate out business logic from 'Alerts'. 
 * The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class AlertsLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(AlertsLogic.class.getName());

    public String alterRecordInDatabase(Alert alert) {
        return alterRecordInDatabase(alert, null);
    }
    
    public String alterRecordInDatabase(Alert alert, String oldName) {
        
        if ((alert == null) || (alert.getName() == null)) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter alert.";
            logger.warn(returnString);
            return returnString;
        }
        
        String returnString;

        synchronized (GlobalVariables.alertRoutineLock) {
           
            boolean isNewAlert = true, isOverwriteExistingAttempt = false;
            AlertsDao alertsDao = new AlertsDao(false);
            Alert alertFromDb = null;
            
            if ((oldName != null) && !oldName.isEmpty()) {
                alertFromDb = alertsDao.getAlertByName(oldName);
                
                if (alertFromDb != null) {
                    alert.setId(alertFromDb.getId());
                    isNewAlert = false;
                }
                else {
                    isNewAlert = true;
                }
            }
            else {
                alertFromDb = alertsDao.getAlertByName(alert.getName());
                if (alertFromDb != null) isOverwriteExistingAttempt = true;
            }
            
            boolean isUpsertSuccess = false;
            Alert newAlertFromDb = null;
            if (!isOverwriteExistingAttempt) {
                isUpsertSuccess = alertsDao.upsert(alert);
                newAlertFromDb = alertsDao.getAlertByName(alert.getName());
            }
            
            alertsDao.close();

            if (isOverwriteExistingAttempt) {
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to create alert. An alert the with same name already exists. AlertName=\"" + alert.getName() + "\".";
                logger.warn(returnString);
            }
            else if (isUpsertSuccess && isNewAlert && (newAlertFromDb != null)) {
                lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Successful alert creation. AlertName=\"" + alert.getName() + "\".";
                logger.info(returnString);
            }
            else if (isUpsertSuccess && (newAlertFromDb != null)) {
                lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Successful alert alteration. AlertName=\"" + alert.getName() + "\".";
                logger.info(returnString);
            }
            else {
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to alter alert. " + "AlertName=\"" + alert.getName() + "\".";
                logger.warn(returnString);
            }
        }
        
        return returnString;
    }
    
    public String deleteRecordInDatabase(String alertName) {
        
        if ((alertName == null) || alertName.isEmpty()) {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Invalid alert name. Cancelling delete operation.";
            logger.warn(returnString);
            return returnString;
        }

        String returnString;

        synchronized (GlobalVariables.alertRoutineLock) {

            AlertsDao alertsDao = new AlertsDao(false);
            Alert alertFromDb = alertsDao.getAlertByName(alertName);

            if (alertFromDb != null) {
                boolean didDeleteSucceed = alertsDao.delete(alertFromDb);

                if (!didDeleteSucceed) {
                    lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Failed to delete alert. AlertName=\"" + alertName + "\".";
                    logger.warn(returnString);
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                    returnString = "Delete alert success. AlertName=\"" + alertName + "\".";
                    logger.info(returnString);
                }
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Alert not found. AlertName=\"" + alertName + "\". Cancelling delete operation.";
                logger.warn(returnString);
            }

            alertsDao.close();
        }
        
        return returnString;
    }
    
}
