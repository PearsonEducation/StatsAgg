package com.pearson.statsagg.database_objects.alerts;

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
public class AlertsDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(AlertsDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "alert";
    private static final String LOG_FRIENDLY_NAME = "Alert";
    
    private final Alert alert_;
    
    private AlertsDaoWrapper(Alert alert) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.alert_ = alert;
    }
    
    private AlertsDaoWrapper(Alert alert, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.alert_ = alert;
    }

    private AlertsDaoWrapper alterRecordInDatabase(boolean isAcknowledgementChange) {
        
        if ((alert_ == null) || (alert_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        Alert alertToUpsert = Alert.copy(alert_);
        
        synchronized (GlobalVariables.alertRoutineLock) {
            Connection connection = DatabaseConnections.getConnection(false);
                
            try {
                getReturnString_AlterInitialValue(alertToUpsert.getName());
                
                Alert alertFromDb = AlertsDao.getAlert_FilterByUppercaseName(connection, false, alertToUpsert.getName());
                
                boolean isAlertTemplateIdConflict = Alert.areAlertTemplateIdsInConflict(alertToUpsert, alertFromDb);
                
                if (isAlertTemplateIdConflict) {
                    getReturnString_AlterFail_TemplateConflict(alertToUpsert.getName());
                }
                else if (isNewDatabaseObject_ && (alertFromDb != null)) { 
                    getReturnString_CreateFail_SameNameAlreadyExists(alertToUpsert.getName());
                }
                else {
                    boolean isUpsertSuccess;
                    if (oldDatabaseObjectName_ == null) isUpsertSuccess = AlertsDao.upsert(connection, false, true, alertToUpsert);
                    else {
                        copyStatusMetadataFields(isAcknowledgementChange, alertToUpsert, alertFromDb); 
                        isUpsertSuccess = AlertsDao.upsert(connection, false, true, alertToUpsert, oldDatabaseObjectName_);
                    }

                    if (isUpsertSuccess) getReturnString_AlterSuccess(alertToUpsert.getName());
                    else getReturnString_AlterFail(alertToUpsert.getName());
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
   
    private void copyStatusMetadataFields(boolean isAcknowledgementChange, Alert alertToUpsert, Alert alertFromDb) {
        
        if ((alertToUpsert == null) || (alertFromDb == null)) {
            return;
        }
        
        if (alertToUpsert.isCautionCriteriaEqual(alertFromDb, false)) {
            alertFromDb.copyCautionMetadataFields(alertToUpsert);
            if (!isAcknowledgementChange) alertToUpsert.setIsCautionAlertAcknowledged(alertFromDb.isCautionAlertAcknowledged());
            logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is not modifying caution criteria fields. Caution triggered status will be preserved.");
        }
        else {
            logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is modifying caution alert criteria fields. Caution triggered status will not be preserved.");
        }

        if (alertToUpsert.isDangerCriteriaEqual(alertFromDb, false)) {
            alertFromDb.copyDangerMetadataFields(alertToUpsert);
            if (!isAcknowledgementChange) alertToUpsert.setIsDangerAlertAcknowledged(alertFromDb.isDangerAlertAcknowledged());
            logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is not modifying danger criteria fields. Danger triggered status will be preserved.");
        }
        else {
            logger.info("Alter alert: Alert \"" + alertToUpsert.getName() + "\" is modifying danger alert criteria fields. Danger triggered status will not be preserved.");
        }
    
    }
    
    private AlertsDaoWrapper deleteRecordInDatabase() {
        
        if ((alert_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((alert_ == null) || (alert_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        synchronized (GlobalVariables.alertRoutineLock) {
            Connection connection = DatabaseConnections.getConnection(false);
                
            try {
                String alertName = (alert_ != null) ? alert_.getName() : null;
                getReturnString_DeleteInitialValue(alertName);
                
                boolean didDeleteSucceed = AlertsDao.delete(connection, false, true, alert_);
                
                if (!didDeleteSucceed) getReturnString_DeleteFail(alertName);
                else getReturnString_DeleteSuccess(alertName);
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
       
    public static AlertsDaoWrapper createRecordInDatabase(Alert alert) {
        AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert);
        return alertsDaoWrapper.alterRecordInDatabase(false);
    }
    
    public static AlertsDaoWrapper alterRecordInDatabase(Alert alert) {
        String alertName = (alert != null) ? alert.getName() : null;
        AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert, alertName);
        return alertsDaoWrapper.alterRecordInDatabase(false);
    }
    
    public static AlertsDaoWrapper alterRecordInDatabase(Alert alert, String oldName) {
        AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert, oldName);
        return alertsDaoWrapper.alterRecordInDatabase(false);
    }
    
    public static AlertsDaoWrapper alterRecordInDatabase(Alert alert, String oldName, boolean isAcknowledgementChange) {
        AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert, oldName);
        return alertsDaoWrapper.alterRecordInDatabase(isAcknowledgementChange);
    }
    
    public static AlertsDaoWrapper alterRecordInDatabase(Alert alert, boolean isAcknowledgementChange) {
        String alertName = (alert != null) ? alert.getName() : null;
        AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert, alertName);
        return alertsDaoWrapper.alterRecordInDatabase(isAcknowledgementChange);
    }

    public static AlertsDaoWrapper deleteRecordInDatabase(String alertName) {
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert);
        return alertsDaoWrapper.deleteRecordInDatabase();
    }
    
    public static AlertsDaoWrapper deleteRecordInDatabase(Alert alert) {
        AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert);
        return alertsDaoWrapper.deleteRecordInDatabase();
    }
    
    public static AlertsDaoWrapper changeAlertCautionAcknowledge(String alertName, Boolean isCautionAcknowledged) {
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if ((alert != null) && (alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
            alert.setIsCautionAlertAcknowledged(isCautionAcknowledged);
            AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert, alertName);
            return alertsDaoWrapper.alterRecordInDatabase(true);
        }
        else {
            AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(null);
            return alertsDaoWrapper;
        }
    }
    
    public static AlertsDaoWrapper changeAlertDangerAcknowledge(String alertName, Boolean isDangerAcknowledged) {
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if ((alert != null) && (alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
            alert.setIsDangerAlertAcknowledged(isDangerAcknowledged);
            AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert, alertName);
            return alertsDaoWrapper.alterRecordInDatabase(true);
        }
        else {
            AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(null);
            return alertsDaoWrapper;
        }
    }
    
    public static AlertsDaoWrapper changeAlertAcknowledge(Integer alertId, Boolean isAcknowledged) {
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertId);
        
        boolean doAlterAlert = false;
        
        if ((alert != null) && (alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
            alert.setIsCautionAlertAcknowledged(isAcknowledged);
            doAlterAlert = true;
        }
        
        if ((alert != null) && (alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
            alert.setIsDangerAlertAcknowledged(isAcknowledged);
            doAlterAlert = true;
        }
        
        if (doAlterAlert) {
            String alertName = (alert != null) ? alert.getName() : null;
            AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(alert, alertName);
            return alertsDaoWrapper.alterRecordInDatabase(true);
        }
        else {
            AlertsDaoWrapper alertsDaoWrapper = new AlertsDaoWrapper(null);
            return alertsDaoWrapper;
        }
    }
    
}
