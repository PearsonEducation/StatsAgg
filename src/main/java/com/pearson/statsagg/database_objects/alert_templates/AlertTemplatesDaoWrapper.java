package com.pearson.statsagg.database_objects.alert_templates;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.database_objects.AbstractDaoWrapper;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplatesDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplatesDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "alert template";
    private static final String LOG_FRIENDLY_NAME = "AlertTemplate";
    
    private final AlertTemplate alertTemplate_;
    public final static Map<Integer,Byte> localSuspensionChanges = new HashMap<>();

    private AlertTemplatesDaoWrapper(AlertTemplate alertTemplate) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.alertTemplate_ = alertTemplate;
    }
    
    private AlertTemplatesDaoWrapper(AlertTemplate alertTemplate, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.alertTemplate_ = alertTemplate;
    }

    private AlertTemplatesDaoWrapper alterRecordInDatabase() {
        
        if ((alertTemplate_ == null) || (alertTemplate_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }

        Connection connection = DatabaseConnections.getConnection(false);

        try {
            getReturnString_AlterInitialValue(alertTemplate_.getName());

            AlertTemplate alertTemplateFromDb = AlertTemplatesDao.getAlertTemplate_FilterByUppercaseName(connection, false, alertTemplate_.getName());

            if (isNewDatabaseObject_ && (alertTemplateFromDb != null)) { 
                getReturnString_CreateFail_SameNameAlreadyExists(alertTemplate_.getName());
            }
            else {
                boolean isUpsertSuccess;
                if (oldDatabaseObjectName_ == null) isUpsertSuccess = AlertTemplatesDao.upsert(connection, false, true, alertTemplate_);
                else isUpsertSuccess = AlertTemplatesDao.upsert(connection, false, true, alertTemplate_, oldDatabaseObjectName_);

                if (isUpsertSuccess) getReturnString_AlterSuccess(alertTemplate_.getName());
                else getReturnString_AlterFail(alertTemplate_.getName());
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

    private AlertTemplatesDaoWrapper deleteRecordInDatabase() {
        
        if ((alertTemplate_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((alertTemplate_ == null) || (alertTemplate_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        Connection connection = null;

        try {
            String alertTemplateName = (alertTemplate_ != null) ? alertTemplate_.getName() : null;
            getReturnString_DeleteInitialValue(alertTemplateName);

            synchronized (GlobalVariables.alertRoutineLock) {
                synchronized (GlobalVariables.suspensionChanges) {
                    connection = DatabaseConnections.getConnection(false);
                    
                    boolean didDeleteAlertsAndSuspensionsSucceed = deleteAlertsAndSuspensionsAssociatedWithAlertTemplate(connection, alertTemplate_);
                    boolean didDeleteAlertTemplateSucceed = AlertTemplatesDao.delete(connection, false, false, alertTemplate_);

                    if (didDeleteAlertsAndSuspensionsSucceed && didDeleteAlertTemplateSucceed) {
                        boolean didCommitSucceed = DatabaseUtils.commit(connection, false);

                        if (didCommitSucceed) {
                            GlobalVariables.suspensionChanges.putAll(localSuspensionChanges);
                            getReturnString_DeleteSuccess(alertTemplateName);
                        }
                        else {
                            DatabaseUtils.rollback(connection);
                            getReturnString_DeleteFail(alertTemplateName);
                        }
                    }
                    else {
                        DatabaseUtils.rollback(connection);
                        getReturnString_DeleteFail(alertTemplateName);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
            localSuspensionChanges.clear();
        }
        
        return this;
    }
    
    private boolean deleteAlertsAndSuspensionsAssociatedWithAlertTemplate(Connection connection, AlertTemplate alertTemplate) {
        
        if ((alertTemplate == null) || (alertTemplate.getId() == null)) {
            return false;
        }
        
        try {
            boolean isAllDeleteSuccess = true;
            
            List<Alert> alertsAssociatedWithAlertTemplate = AlertsDao.getAlerts_FilterByAlertTemplateId(connection, false, alertTemplate.getId());
            if (alertsAssociatedWithAlertTemplate == null) return false; // null means there was a problem querying the db
            
            for (Alert alertAssociatedWithAlertTemplate : alertsAssociatedWithAlertTemplate) {
                if ((alertAssociatedWithAlertTemplate == null) || (alertAssociatedWithAlertTemplate.getId() == null)) continue;

                List<Suspension> suspensionsAssociatedWithAlert = SuspensionsDao.getSuspensions_FilterByAlertId(connection, false, alertAssociatedWithAlertTemplate.getId());
                if (suspensionsAssociatedWithAlert == null) isAllDeleteSuccess = false; // null means there was a problem querying the db

                // delete suspensions
                if (suspensionsAssociatedWithAlert != null) {
                    for (Suspension suspensionAssociatedWithAlert : suspensionsAssociatedWithAlert) {
                        if (suspensionAssociatedWithAlert == null) continue;

                        boolean isSuspensionDeleteSuccess = SuspensionsDao.delete(connection, false, false, suspensionAssociatedWithAlert);
                        if (!isSuspensionDeleteSuccess) isAllDeleteSuccess = false;
                        else localSuspensionChanges.put(suspensionAssociatedWithAlert.getId(), GlobalVariables.REMOVE);
                    }
                }
                
                // delete alert
                boolean isAlertDeleteSuccess = AlertsDao.delete(connection, false, false, alertAssociatedWithAlertTemplate);
                if (!isAlertDeleteSuccess) isAllDeleteSuccess = false;
            }
            
            return isAllDeleteSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
    }
    
    public static AlertTemplatesDaoWrapper createRecordInDatabase(AlertTemplate alertTemplate) {
        AlertTemplatesDaoWrapper alertTemplatesDaoWrapper = new AlertTemplatesDaoWrapper(alertTemplate);
        return alertTemplatesDaoWrapper.alterRecordInDatabase();
    }
    
    public static AlertTemplatesDaoWrapper alterRecordInDatabase(AlertTemplate alertTemplate) {
        String alertTemplateName = (alertTemplate != null) ? alertTemplate.getName() : null;
        AlertTemplatesDaoWrapper alertTemplatesDaoWrapper = new AlertTemplatesDaoWrapper(alertTemplate, alertTemplateName);
        return alertTemplatesDaoWrapper.alterRecordInDatabase();
    }
    
    public static AlertTemplatesDaoWrapper alterRecordInDatabase(AlertTemplate alertTemplate, String oldName) {
        AlertTemplatesDaoWrapper alertTemplatesDaoWrapper = new AlertTemplatesDaoWrapper(alertTemplate, oldName);
        return alertTemplatesDaoWrapper.alterRecordInDatabase();
    }

    public static AlertTemplatesDaoWrapper deleteRecordInDatabase(String alertTemplateName) {
        AlertTemplate alertTemplate = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, alertTemplateName);
        AlertTemplatesDaoWrapper alertTemplatesDaoWrapper = new AlertTemplatesDaoWrapper(alertTemplate);
        return alertTemplatesDaoWrapper.deleteRecordInDatabase();
    }
    
    public static AlertTemplatesDaoWrapper deleteRecordInDatabase(AlertTemplate alertTemplate) {
        AlertTemplatesDaoWrapper alertTemplatesDaoWrapper = new AlertTemplatesDaoWrapper(alertTemplate);
        return alertTemplatesDaoWrapper.deleteRecordInDatabase();
    }
    
}
