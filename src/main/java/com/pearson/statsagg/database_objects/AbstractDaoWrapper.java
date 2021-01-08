package com.pearson.statsagg.database_objects;

import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public abstract class AbstractDaoWrapper {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractDaoWrapper.class.getName());

    public final static int STATUS_CODE_FAILURE = -1;
    public final static int STATUS_CODE_UNKNOWN = 0;
    public final static int STATUS_CODE_SUCCESS = 1;
    
    protected int lastAlterRecordStatus_ = 0;
    protected int lastDeleteRecordStatus_ = 0;
    
    protected final String databaseObjectHumanFriendlyName_;
    protected final String databaseObjectLogFriendlyName_;
    protected final String oldDatabaseObjectName_;
    protected final boolean isNewDatabaseObject_;
    
    protected String returnString_ = "";
    
    public AbstractDaoWrapper(String databaseObjectHumanFriendlyName, String databaseObjectLogFriendlyName, boolean isNewDatabaseObject) {
        this.databaseObjectHumanFriendlyName_ = databaseObjectHumanFriendlyName;
        this.databaseObjectLogFriendlyName_ = databaseObjectLogFriendlyName;
        this.oldDatabaseObjectName_ = null;
        this.isNewDatabaseObject_ = isNewDatabaseObject;
        
        returnString_ = "No operations have been ran for this " + databaseObjectHumanFriendlyName_ + " yet.";
    }
    
    public AbstractDaoWrapper(String databaseObjectHumanFriendlyName, String databaseObjectLogFriendlyName, String oldDatabaseObjectName) {
        this.databaseObjectHumanFriendlyName_ = databaseObjectHumanFriendlyName;
        this.databaseObjectLogFriendlyName_ = databaseObjectLogFriendlyName;
        this.oldDatabaseObjectName_ = oldDatabaseObjectName;
        
        this.isNewDatabaseObject_ = (oldDatabaseObjectName == null) || oldDatabaseObjectName.isEmpty();
        
        returnString_ = "No operations have been ran for this " + databaseObjectHumanFriendlyName_ + " yet.";
    }
    
    protected void getReturnString_AlterInitialValue(String databaseObjectName) {
        returnString_ = "Error creating or altering " + databaseObjectHumanFriendlyName_ + ". " + databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\".";
    }
    
    protected void getReturnString_AlterFail_InitialChecks() {
        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString = "Failed to alter " + databaseObjectHumanFriendlyName_ + ".";
        logger.warn(returnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_CreateFail_SameNameAlreadyExists(String databaseObjectName) {
        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString = "Failed to create " + databaseObjectHumanFriendlyName_ + ". A " + databaseObjectHumanFriendlyName_ + 
                " with the same name already exists. " + databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        
        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.warn(cleanReturnString);
        returnString_ = returnString;
    }

    protected void getReturnString_AlterFail(String databaseObjectName) {
        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString;

        if (isNewDatabaseObject_) {
            returnString = "Failed to create " + databaseObjectHumanFriendlyName_ + ". " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        }
        else {
            returnString = "Failed to alter " + databaseObjectHumanFriendlyName_ + ". " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        }

        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.info(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_AlterFail_TemplateConflict(String databaseObjectName) {
        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString;

        if (isNewDatabaseObject_) {
            returnString = "Failed to create " + databaseObjectHumanFriendlyName_ + " due to a " + databaseObjectHumanFriendlyName_ + " template conflict. " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        }
        else {
            returnString = "Failed to alter " + databaseObjectHumanFriendlyName_ + " due to a " + databaseObjectHumanFriendlyName_ + " template conflict. " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        }

        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.info(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_AlterFail_CommitFail(String databaseObjectName) {
        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString;

        if (isNewDatabaseObject_) {
            returnString = "Failed to create " + databaseObjectHumanFriendlyName_ + ". " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"" + ", CommitSuccess=false";
        }
        else {
            returnString = "Failed to alter " + databaseObjectHumanFriendlyName_ + ". " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"" + ", CommitSuccess=false";
        }

        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.info(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_AlterSuccess(String databaseObjectName) {
        lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;
        String returnString;

        if (isNewDatabaseObject_) {
            returnString = "Successful " + databaseObjectHumanFriendlyName_ + " creation. " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        }
        else {
            returnString = "Successful " + databaseObjectHumanFriendlyName_ + " alteration. " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        }

        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.info(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_DeleteInitialValue(String databaseObjectName) {
        returnString_ = "Error deleting " + databaseObjectHumanFriendlyName_ + ". " + databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\".";
    }
    
    protected void getReturnString_DeleteFail_InitialChecks() {
        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString = "Invalid " + databaseObjectHumanFriendlyName_ + ". Cancelling delete operation.";
        logger.warn(returnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_DeleteFail(String databaseObjectName) {
        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString = "Failed to delete " + databaseObjectHumanFriendlyName_ + ". " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"";
        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.warn(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_DeleteFail_CommitFail(String databaseObjectName) {
        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString = "Failed to delete " + databaseObjectHumanFriendlyName_ + ". " + 
                    databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\"" + ", CommitSuccess=false";
        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.warn(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_DeleteFail_RecordNotFound() {
        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString = "The " + databaseObjectHumanFriendlyName_ + " was not found. Cancelling delete operation.";
        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.warn(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_DeleteFail_RecordNotFound(String databaseObjectName) {
        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
        String returnString = "The " + databaseObjectHumanFriendlyName_ + " was not found. " + 
                databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\". Cancelling delete operation.";
        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.warn(cleanReturnString);
        returnString_ = returnString;
    }
    
    protected void getReturnString_DeleteSuccess(String databaseObjectName) {
        lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
        String returnString = "Delete " + databaseObjectHumanFriendlyName_ + " success. " + databaseObjectLogFriendlyName_ + "Name=\"" + databaseObjectName + "\".";
        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
        logger.info(cleanReturnString);
        returnString_ = returnString;
    }
    
    public int getLastAlterRecordStatus() {
        return lastAlterRecordStatus_;
    }

    public int getLastDeleteRecordStatus() {
        return lastDeleteRecordStatus_;
    }

    public String getReturnString() {
        return returnString_;
    }

}
