package com.pearson.statsagg.database_objects.notification_groups;

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
public class NotificationGroupsDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "notification group";
    private static final String LOG_FRIENDLY_NAME = "NotificationGroup";
    
    private final NotificationGroup notificationGroup_;
    
    private NotificationGroupsDaoWrapper(NotificationGroup notificationGroup) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.notificationGroup_ = notificationGroup;
    }
    
    private NotificationGroupsDaoWrapper(NotificationGroup notificationGroup, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.notificationGroup_ = notificationGroup;
    }
    
    private NotificationGroupsDaoWrapper alterRecordInDatabase() {
        
        if ((notificationGroup_ == null) || (notificationGroup_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            getReturnString_AlterInitialValue(notificationGroup_.getName());
            
            NotificationGroup notificationGroupFromDb = NotificationGroupsDao.getNotificationGroup_FilterByUppercaseName(connection, false, notificationGroup_.getName());
            
            if (isNewDatabaseObject_ && (notificationGroupFromDb != null)) { 
                getReturnString_CreateFail_SameNameAlreadyExists(notificationGroup_.getName());
            }
            else {
                boolean isUpsertSuccess;
                if (oldDatabaseObjectName_ == null) isUpsertSuccess = NotificationGroupsDao.upsert(connection, false, true, notificationGroup_);
                else isUpsertSuccess = NotificationGroupsDao.upsert(connection, false, true, notificationGroup_, oldDatabaseObjectName_);

                if (isUpsertSuccess) getReturnString_AlterSuccess(notificationGroup_.getName());
                else getReturnString_AlterFail(notificationGroup_.getName());
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
    
    private NotificationGroupsDaoWrapper deleteRecordInDatabase() {
        
        if ((notificationGroup_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((notificationGroup_ == null) || (notificationGroup_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            String notificationGroupName = (notificationGroup_ != null) ? notificationGroup_.getName() : null;
            getReturnString_DeleteInitialValue(notificationGroupName);
            
            boolean didDeleteSucceed = NotificationGroupsDao.delete(connection, false, true, notificationGroup_);
            if (!didDeleteSucceed) getReturnString_DeleteFail(notificationGroupName);
            else getReturnString_DeleteSuccess(notificationGroupName);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return this;
    }
    
    public static NotificationGroupsDaoWrapper createRecordInDatabase(NotificationGroup notificationGroup) {
        NotificationGroupsDaoWrapper notificationGroupsDaoWrapper = new NotificationGroupsDaoWrapper(notificationGroup);
        return notificationGroupsDaoWrapper.alterRecordInDatabase();
    }
    
    public static NotificationGroupsDaoWrapper alterRecordInDatabase(NotificationGroup notificationGroup) {
        String notificationGroupName = (notificationGroup != null) ? notificationGroup.getName() : null;
        NotificationGroupsDaoWrapper notificationGroupsDaoWrapper = new NotificationGroupsDaoWrapper(notificationGroup, notificationGroupName);
        return notificationGroupsDaoWrapper.alterRecordInDatabase();
    }
    
    public static NotificationGroupsDaoWrapper alterRecordInDatabase(NotificationGroup notificationGroup, String oldName) {
        NotificationGroupsDaoWrapper notificationGroupsDaoWrapper = new NotificationGroupsDaoWrapper(notificationGroup, oldName);
        return notificationGroupsDaoWrapper.alterRecordInDatabase();
    }
    
    public static NotificationGroupsDaoWrapper deleteRecordInDatabase(String notificationGroupsName) {
        NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupsName);
        NotificationGroupsDaoWrapper notificationGroupsDaoWrapper = new NotificationGroupsDaoWrapper(notificationGroup);
        return notificationGroupsDaoWrapper.deleteRecordInDatabase();
    }
    
    public static NotificationGroupsDaoWrapper deleteRecordInDatabase(NotificationGroup notificationGroup) {
        NotificationGroupsDaoWrapper notificationGroupsDaoWrapper = new NotificationGroupsDaoWrapper(notificationGroup);
        return notificationGroupsDaoWrapper.deleteRecordInDatabase();
    }
    
}
