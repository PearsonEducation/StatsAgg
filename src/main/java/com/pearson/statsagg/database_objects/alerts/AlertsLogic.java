package com.pearson.statsagg.database_objects.alerts;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import com.pearson.statsagg.web_ui.AbstractDatabaseInteractionLogic;
import java.sql.Connection;
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
           
            boolean isNewAlert = true, isOverwriteExistingAttempt = false, isUpsertSuccess = false;
            Alert newAlertFromDb = null;
            Connection connection = DatabaseConnections.getConnection();
            DatabaseUtils.setAutoCommit(connection, false);
                
            try {
                Alert alertFromDb;

                if ((oldName != null) && !oldName.isEmpty()) {
                    alertFromDb = AlertsDao.getAlert(connection, false, oldName);

                    if (alertFromDb != null) {
                        isNewAlert = false;
                        alertToUpsert.setId(alertFromDb.getId());

                        if (alertToUpsert.isCautionCriteriaEqual(alertFromDb)) {
                            alertFromDb.copyCautionMetadataFields(alertToUpsert);
                            if (!isAcknowledgementChange) alertToUpsert.setIsCautionAlertAcknowledged(alertFromDb.isCautionAlertAcknowledged());
                            logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is not modifying caution criteria fields. Caution triggered status will be preserved.");
                        }
                        else {
                            logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is modifying caution alert criteria fields. Caution triggered status will not be preserved.");
                        }

                        if (alertToUpsert.isDangerCriteriaEqual(alertFromDb)) {
                            alertFromDb.copyDangerMetadataFields(alertToUpsert);
                            if (!isAcknowledgementChange) alertToUpsert.setIsDangerAlertAcknowledged(alertFromDb.isDangerAlertAcknowledged());

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
                    alertFromDb = AlertsDao.getAlert(connection, false, alertToUpsert.getName());
                    if (alertFromDb != null) isOverwriteExistingAttempt = true;
                }

                if (!isOverwriteExistingAttempt) {
                    isUpsertSuccess = AlertsDao.upsert(connection, false, true, alertToUpsert);
                    newAlertFromDb = AlertsDao.getAlert(connection, false, alertToUpsert.getName());
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
                returnString = "Failed to create alert. An alert the with same name already exists. AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }
            else if (isUpsertSuccess && isNewAlert && (newAlertFromDb != null)) {
                lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Successful alert creation. AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.info(cleanReturnString);
            }
            else if (isUpsertSuccess && (newAlertFromDb != null)) {
                lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Successful alert alteration. AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.info(cleanReturnString);
            }
            else {
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to alter alert. " + "AlertName=\"" + alertToUpsert.getName() + "\".";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
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
        
        String returnString = "Error to deleting alert. AlertName=\"" + StringUtilities.removeNewlinesFromString(alertName) + "\".";
                        
        synchronized (GlobalVariables.alertRoutineLock) {            
            Connection connection = DatabaseConnections.getConnection();
            DatabaseUtils.setAutoCommit(connection, false);
            
            try {
                Alert alertFromDb = AlertsDao.getAlert(connection, false, alertName);

                if (alertFromDb != null) {
                    boolean didDeleteSucceed = AlertsDao.delete(connection, false, true, alertFromDb);

                    if (!didDeleteSucceed) {
                        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                        returnString = "Failed to delete alert. AlertName=\"" + alertName + "\".";
                        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                        logger.warn(cleanReturnString);
                    }
                    else {
                        lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                        returnString = "Delete alert success. AlertName=\"" + alertName + "\".";
                        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                        logger.info(cleanReturnString);
                    }
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Alert not found. AlertName=\"" + alertName + "\". Cancelling delete operation.";
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
        }
        
        return returnString;
    }
    
    public static String changeAlertCautionAcknowledge(String alertName, Boolean isCautionAcknowledged) {
        
        if ((alertName == null) || (isCautionAcknowledged == null)) {
            return null;
        }
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if ((alert != null) && (alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
            alert.setIsCautionAlertAcknowledged(isCautionAcknowledged);
            AlertsLogic alertsLogic = new AlertsLogic();
            return alertsLogic.alterRecordInDatabase(alert, alertName, true);
        }
        
        return null;
    }
    
    public static String changeAlertDangerAcknowledge(String alertName, Boolean isDangerAcknowledged) {
        
        if ((alertName == null) || (isDangerAcknowledged == null)) {
            return null;
        }
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if ((alert != null) && (alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
            alert.setIsDangerAlertAcknowledged(isDangerAcknowledged);
            AlertsLogic alertsLogic = new AlertsLogic();
            return alertsLogic.alterRecordInDatabase(alert, alertName, true);
        }
        
        return null;
    }
    
    public static String changeAlertAcknowledge(Integer alertId, Boolean isAcknowledged) {
        
        if ((alertId == null) || (isAcknowledged == null)) {
            return null;
        }
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertId);
        
        if (alert != null) {
            boolean doAlertAlert = false;
            
            if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
                alert.setIsCautionAlertAcknowledged(isAcknowledged);
                doAlertAlert = true;
            }
            if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
                alert.setIsDangerAlertAcknowledged(isAcknowledged);
                doAlertAlert = true;
            }
            
            if (doAlertAlert) {
                AlertsLogic alertsLogic = new AlertsLogic();
                return alertsLogic.alterRecordInDatabase(alert, alert.getName(), true);
            }
        }
        
        return null;
    }
    
}
