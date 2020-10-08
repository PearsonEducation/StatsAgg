package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 This 'Logic' class was created to separate out business logic from 'Suspensions'. 
 The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class SuspensionsLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(SuspensionsLogic.class.getName());
    
    public String alterRecordInDatabase(Suspension suspension) {
        return alterRecordInDatabase(suspension, null);
    }
    
    public String alterRecordInDatabase(Suspension suspension, String oldName) {
        
        if ((suspension == null) || (suspension.getName() == null)) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter suspension.";
            logger.warn(returnString);
            return returnString;
        }
        
        String returnString;
        
        synchronized (GlobalVariables.suspensionChanges) {
            boolean isNewSuspension = true, isOverwriteExistingAttempt = false, isUpsertSuccess = false;
            Suspension newSuspensionFromDb = null;
            Connection connection = DatabaseConnections.getConnection();
            DatabaseUtils.setAutoCommit(connection, false);
            
            try {
                Suspension suspensionFromDb;

                if ((oldName != null) && !oldName.isEmpty()) {
                    suspensionFromDb = SuspensionsDao.getSuspension(connection, false, oldName);

                    if (suspensionFromDb != null) {
                        suspension.setId(suspensionFromDb.getId());
                        isNewSuspension = false;
                    }
                    else {
                        isNewSuspension = true;
                    }
                }
                else {
                    suspensionFromDb = SuspensionsDao.getSuspension(connection, false, suspension.getName());
                    if (suspensionFromDb != null) isOverwriteExistingAttempt = true;
                }

                if (!isOverwriteExistingAttempt) {
                    isUpsertSuccess = SuspensionsDao.upsert(connection, false, true, suspension);
                    newSuspensionFromDb = SuspensionsDao.getSuspension(connection, false, suspension.getName());
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

        String returnString = "Error to deleting suspension. SuspensionName=\"" + StringUtilities.removeNewlinesFromString(suspensionName) + "\".";

        synchronized (GlobalVariables.suspensionChanges) {
            Connection connection = DatabaseConnections.getConnection();
            DatabaseUtils.setAutoCommit(connection, false);
            
            try {
                Suspension suspensionFromDb = SuspensionsDao.getSuspension(connection, false, suspensionName);

                if (suspensionFromDb != null) {
                    boolean didDeleteSucceed = SuspensionsDao.delete(connection, false, true, suspensionFromDb);

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

}
