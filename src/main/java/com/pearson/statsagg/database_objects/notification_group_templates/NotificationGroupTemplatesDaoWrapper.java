package com.pearson.statsagg.database_objects.notification_group_templates;

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
public class NotificationGroupTemplatesDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplatesDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "notification group template";
    private static final String LOG_FRIENDLY_NAME = "NotificationGroupTemplate";
    
    private final NotificationGroupTemplate notificationGroupTemplate_;
    
    private NotificationGroupTemplatesDaoWrapper(NotificationGroupTemplate notificationGroupTemplate) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.notificationGroupTemplate_ = notificationGroupTemplate;
    }
    
    private NotificationGroupTemplatesDaoWrapper(NotificationGroupTemplate notificationGroupTemplate, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.notificationGroupTemplate_ = notificationGroupTemplate;
    }
    
    private NotificationGroupTemplatesDaoWrapper alterRecordInDatabase() {
        
        if ((notificationGroupTemplate_ == null) || (notificationGroupTemplate_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            getReturnString_AlterInitialValue(notificationGroupTemplate_.getName());
            
            NotificationGroupTemplate notificationGroupTemplateFromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate_FilterByUppercaseName(connection, false, notificationGroupTemplate_.getName());
            
            if (isNewDatabaseObject_ && (notificationGroupTemplateFromDb != null)) { 
                getReturnString_CreateFail_SameNameAlreadyExists(notificationGroupTemplate_.getName());
            }
            else {
                boolean isUpsertSuccess;
                if (oldDatabaseObjectName_ == null) isUpsertSuccess = NotificationGroupTemplatesDao.upsert(connection, false, true, notificationGroupTemplate_);
                else isUpsertSuccess = NotificationGroupTemplatesDao.upsert(connection, false, true, notificationGroupTemplate_, oldDatabaseObjectName_);

                if (isUpsertSuccess) getReturnString_AlterSuccess(notificationGroupTemplate_.getName());
                else getReturnString_AlterFail(notificationGroupTemplate_.getName());
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
    
    private NotificationGroupTemplatesDaoWrapper deleteRecordInDatabase() {
        
        if ((notificationGroupTemplate_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((notificationGroupTemplate_ == null) || (notificationGroupTemplate_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            String notificationGroupTemplateName = (notificationGroupTemplate_ != null) ? notificationGroupTemplate_.getName() : null;
            getReturnString_DeleteInitialValue(notificationGroupTemplateName);
            
            boolean didDeleteSucceed = NotificationGroupTemplatesDao.delete(connection, false, true, notificationGroupTemplate_);
            if (!didDeleteSucceed) getReturnString_DeleteFail(notificationGroupTemplateName);
            else getReturnString_DeleteSuccess(notificationGroupTemplateName);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return this;
    }
    
    public static NotificationGroupTemplatesDaoWrapper createRecordInDatabase(NotificationGroupTemplate notificationGroupTemplate) {
        NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = new NotificationGroupTemplatesDaoWrapper(notificationGroupTemplate);
        return notificationGroupTemplatesDaoWrapper.alterRecordInDatabase();
    }
    
    public static NotificationGroupTemplatesDaoWrapper alterRecordInDatabase(NotificationGroupTemplate notificationGroupTemplate) {
        String notificationGroupTemplateName = (notificationGroupTemplate != null) ? notificationGroupTemplate.getName() : null;
        NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = new NotificationGroupTemplatesDaoWrapper(notificationGroupTemplate, notificationGroupTemplateName);
        return notificationGroupTemplatesDaoWrapper.alterRecordInDatabase();
    }
    
    public static NotificationGroupTemplatesDaoWrapper alterRecordInDatabase(NotificationGroupTemplate notificationGroupTemplate, String oldName) {
        NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = new NotificationGroupTemplatesDaoWrapper(notificationGroupTemplate, oldName);
        return notificationGroupTemplatesDaoWrapper.alterRecordInDatabase();
    }
    
    public static NotificationGroupTemplatesDaoWrapper deleteRecordInDatabase(String notificationGroupTemplatesName) {
        NotificationGroupTemplate notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, notificationGroupTemplatesName);
        NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = new NotificationGroupTemplatesDaoWrapper(notificationGroupTemplate);
        return notificationGroupTemplatesDaoWrapper.deleteRecordInDatabase();
    }
    
    public static NotificationGroupTemplatesDaoWrapper deleteRecordInDatabase(NotificationGroupTemplate notificationGroupTemplate) {
        NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = new NotificationGroupTemplatesDaoWrapper(notificationGroupTemplate);
        return notificationGroupTemplatesDaoWrapper.deleteRecordInDatabase();
    }
    
}
