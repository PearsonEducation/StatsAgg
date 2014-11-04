package com.pearson.statsagg.webui;

import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 This 'Logic' class was created to separate out business logic from 'NotificationGroups'. 
 The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class NotificationGroupsLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsLogic.class.getName());
    
    public String alterRecordInDatabase(NotificationGroup notificationGroup) {
        return alterRecordInDatabase(notificationGroup, null);
    }
    
    public String alterRecordInDatabase(NotificationGroup notificationGroup, String oldName) {
        
        if ((notificationGroup == null) || (notificationGroup.getName() == null)) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter notification group.";
            logger.warn(returnString);
            return returnString;
        }
        
        String returnString;

        boolean isNewNotificationGroup = true, isOverwriteExistingAttempt = false;
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao(false);
        NotificationGroup notificationGroupFromDb;

        if ((oldName != null) && !oldName.isEmpty()) {
            notificationGroupFromDb = notificationGroupsDao.getNotificationGroupByName(oldName);

            if (notificationGroupFromDb != null) {
                notificationGroup.setId(notificationGroupFromDb.getId());
                isNewNotificationGroup = false;
            }
            else {
                isNewNotificationGroup = true;
            }
        }
        else {
            notificationGroupFromDb = notificationGroupsDao.getNotificationGroupByName(notificationGroup.getName());
            if (notificationGroupFromDb != null) isOverwriteExistingAttempt = true;
        }

        boolean isUpsertSuccess = false;
        NotificationGroup newNotificationGroupFromDb = null;
        if (!isOverwriteExistingAttempt) {
            isUpsertSuccess = notificationGroupsDao.upsert(notificationGroup);
            newNotificationGroupFromDb = notificationGroupsDao.getNotificationGroupByName(notificationGroup.getName());
        }

        notificationGroupsDao.close();
        
        if (isOverwriteExistingAttempt) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            
            returnString = "Failed to create notification group. A notification group with the same name already exists. NotificationGroupName=\"" + notificationGroup.getName() + "\"";
            logger.warn(returnString);
        }
        else if (isUpsertSuccess && (newNotificationGroupFromDb != null)) {
            lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
            
            if (isNewNotificationGroup) returnString = "Successful notification group creation. NotificationGroupName=\"" + notificationGroup.getName() + "\"";
            else returnString = "Successful notification group alteration. NotificationGroupName=\"" + notificationGroup.getName() + "\"";
            logger.info(returnString);
        }
        else {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            
            if (isNewNotificationGroup) returnString = "Failed to create notification group. " + "NotificationGroupName=\"" + notificationGroup.getName() + "\"";
            else returnString = "Failed to alter notification group. " + "NotificationGroupName=\"" + notificationGroup.getName() + "\"";
            logger.warn(returnString);
        }
            
        return returnString;
    }
    
    public String deleteRecordInDatabase(String notificationGroupName) {
        
        if ((notificationGroupName == null) || notificationGroupName.isEmpty()) {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Invalid notification group name. Cancelling delete operation.";
            logger.warn(returnString);
            return returnString;
        }

        String returnString;
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao(false);
        NotificationGroup notificationGroupFromDb = notificationGroupsDao.getNotificationGroupByName(notificationGroupName);
        
        if (notificationGroupFromDb != null) {
            boolean didDeleteSucceed = notificationGroupsDao.delete(notificationGroupFromDb);
            
            if (!didDeleteSucceed) {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to delete notification group. NotificationGroupName=\"" + notificationGroupName + "\".";
                logger.warn(returnString);
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                returnString = "Delete notification group success. NotificationGroupName=\"" + notificationGroupName + "\".";
                logger.info(returnString);
            }
        }
        else {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            returnString = "Notification group not found. NotificationGroupName=\"" + notificationGroupName + "\". Cancelling delete operation.";
            logger.warn(returnString);
        }
        
        notificationGroupsDao.close();
        
        return returnString;
    }

}
