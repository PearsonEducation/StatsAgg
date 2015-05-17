package com.pearson.statsagg.webui;

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
        return alterRecordInDatabase(alert, null, false);
    }
    
    public String alterRecordInDatabase(Alert alert, String oldName, boolean isAcknowledgementChange) {
        
        if ((alert == null) || (alert.getName() == null)) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter alert.";
            logger.warn(returnString);
            return returnString;
        }
        
        Alert alertToUpsert = Alert.copy(alert);
        
        String returnString;

        synchronized (GlobalVariables.alertRoutineLock) {
           
            boolean isNewAlert = true, isOverwriteExistingAttempt = false;
            AlertsDao alertsDao = new AlertsDao(false);
            Alert alertFromDb = null;
            
            if ((oldName != null) && !oldName.isEmpty()) {
                alertFromDb = alertsDao.getAlertByName(oldName);
                
                if (alertFromDb != null) {
                    isNewAlert = false;
                    alertToUpsert.setId(alertFromDb.getId());
                    
                    if (alertToUpsert.isCautionCriteriaEqual(alertFromDb)) {
                        alertFromDb.copyCautionMetadataFields(alertToUpsert);
                        if (!isAcknowledgementChange) alertToUpsert.setIsCautionAcknowledged(alertFromDb.isCautionAcknowledged());
                        logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is not modifying caution criteria fields. Caution triggered status will be preserved.");
                    }
                    else {
                        logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is modifying caution alert criteria fields. Caution triggered status will not be preserved.");
                    }
                    
                    if (alertToUpsert.isDangerCriteriaEqual(alertFromDb)) {
                        alertFromDb.copyDangerMetadataFields(alertToUpsert);
                        if (!isAcknowledgementChange) alertToUpsert.setIsDangerAcknowledged(alertFromDb.isDangerAcknowledged());

                        logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is not modifying danger criteria fields. Danger triggered status will be preserved.");
                    }
                    else {
                        logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is modifying danger alert criteria fields. Danger triggered status will not be preserved.");
                    }
                }
                else {
                    isNewAlert = true;
                }
            }
            else {
                alertFromDb = alertsDao.getAlertByName(alertToUpsert.getName());
                if (alertFromDb != null) isOverwriteExistingAttempt = true;
            }
            
            boolean isUpsertSuccess = false;
            Alert newAlertFromDb = null;
            if (!isOverwriteExistingAttempt) {
                isUpsertSuccess = alertsDao.upsert(alertToUpsert);
                newAlertFromDb = alertsDao.getAlertByName(alertToUpsert.getName());
            }
            
            alertsDao.close();
            
            if (isOverwriteExistingAttempt) {
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to create alert. An alert the with same name already exists. AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }
            else if (isUpsertSuccess && isNewAlert && (newAlertFromDb != null)) {
                lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Successful alert creation. AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.info(cleanReturnString);
            }
            else if (isUpsertSuccess && (newAlertFromDb != null)) {
                lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Successful alert alteration. AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.info(cleanReturnString);
            }
            else {
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to alter alert. " + "AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
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
                    String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                    logger.warn(cleanReturnString);
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                    returnString = "Delete alert success. AlertName=\"" + alertName + "\".";
                    String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                    logger.info(cleanReturnString);
                }
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Alert not found. AlertName=\"" + alertName + "\". Cancelling delete operation.";
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }

            alertsDao.close();
        }
        
        return returnString;
    }
    
    protected static String changeAlertCautionAcknowledge(String alertName, Boolean isCautionAcknowledged) {
        
        if ((alertName == null) || (isCautionAcknowledged == null)) {
            return null;
        }
        
        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(alertName);
        
        if ((alert != null) && (alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
            alert.setIsCautionAcknowledged(isCautionAcknowledged);
            AlertsLogic alertsLogic = new AlertsLogic();
            return alertsLogic.alterRecordInDatabase(alert, alertName, true);
        }
        
        return null;
    }
    
    protected static String changeAlertDangerAcknowledge(String alertName, Boolean isDangerAcknowledged) {
        
        if ((alertName == null) || (isDangerAcknowledged == null)) {
            return null;
        }
        
        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(alertName);
        
        if ((alert != null) && (alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
            alert.setIsDangerAcknowledged(isDangerAcknowledged);
            AlertsLogic alertsLogic = new AlertsLogic();
            return alertsLogic.alterRecordInDatabase(alert, alertName, true);
        }
        
        return null;
    }
    
    protected static String changeAlertAcknowledge(String alertName, Boolean isAcknowledged) {
        
        if ((alertName == null) || (isAcknowledged == null)) {
            return null;
        }
        
        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(alertName);
        
        if (alert != null) {
            boolean doAlertAlert = false;
            
            if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
                alert.setIsCautionAcknowledged(isAcknowledged);
                doAlertAlert = true;
            }
            if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
                alert.setIsDangerAcknowledged(isAcknowledged);
                doAlertAlert = true;
            }
            
            if (doAlertAlert) {
                AlertsLogic alertsLogic = new AlertsLogic();
                return alertsLogic.alterRecordInDatabase(alert, alertName, true);
            }
        }
        
        return null;
    }
    
}
