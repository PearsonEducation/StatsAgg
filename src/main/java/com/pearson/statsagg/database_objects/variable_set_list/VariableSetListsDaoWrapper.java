package com.pearson.statsagg.database_objects.variable_set_list;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.database_objects.AbstractDaoWrapper;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntriesDao;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntry;
import com.pearson.statsagg.utilities.collection_utils.CollectionUtilities;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetListsDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetListsDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "variable set list";
    private static final String LOG_FRIENDLY_NAME = "VariableSetList";
    
    private final VariableSetList variableSetList_;

    private VariableSetList variableSetListFromDb_AfterUpsert_ = null;
    private boolean isVariableSetListUpsertSuccess_ = true;
    private boolean isVariableSetListEntriesUpsertSuccess_ = true;
    
    private VariableSetListsDaoWrapper(VariableSetList variableSetList) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.variableSetList_ = variableSetList;
    }
    
    private VariableSetListsDaoWrapper(VariableSetList variableSetList, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.variableSetList_ = variableSetList;
    }
    
    private VariableSetListsDaoWrapper alterRecordInDatabase(TreeSet<String> variableSetNames) {
        
        if ((variableSetList_ == null) || (variableSetList_.getName() == null) || (variableSetNames == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            getReturnString_AlterInitialValue(variableSetList_.getName());
            
            VariableSetList variableSetListFromDb = VariableSetListsDao.getVariableSetList_FilterByUppercaseName(connection, false, variableSetList_.getName());

            if (isNewDatabaseObject_ && (variableSetListFromDb != null)) {
                getReturnString_CreateFail_SameNameAlreadyExists(variableSetList_.getName());
            }
            else {
                boolean isOverallUpsertSuccess = upsertVariableSetListAndVariableSetListEntries(connection, variableSetNames);
                
                if (isOverallUpsertSuccess) {
                    boolean didCommitSucceed = DatabaseUtils.commit(connection, false);
                    
                    if (didCommitSucceed) {
                        if (variableSetListFromDb_AfterUpsert_ != null) getReturnString_AlterSuccess(variableSetListFromDb_AfterUpsert_.getName());
                        else getReturnString_AlterSuccess("Unknown");
                    }
                    else {
                        DatabaseUtils.rollback(connection, false);
                        getReturnString_AlterFail_CommitFail(variableSetList_.getName());
                    }
                }
                else {
                    DatabaseUtils.rollback(connection, false);
                    getReturnString_AlterFailDetailed();
                }
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
    
    private boolean upsertVariableSetListAndVariableSetListEntries(Connection connection, TreeSet<String> variableSetNames) {
        boolean isOverallUpsertSuccess = true;
                
        try {
            if (oldDatabaseObjectName_ == null) isVariableSetListUpsertSuccess_ = VariableSetListsDao.upsert(connection, false, false, variableSetList_);
            else isVariableSetListUpsertSuccess_ = VariableSetListsDao.upsert(connection, false, true, variableSetList_, oldDatabaseObjectName_);

            if (isVariableSetListUpsertSuccess_) { 
                variableSetListFromDb_AfterUpsert_ = VariableSetListsDao.getVariableSetList(connection, false, variableSetList_.getName());
                boolean haveVariableSetEntriesListChanged = haveVariableSetEntriesListChanged(connection, variableSetListFromDb_AfterUpsert_, variableSetNames);
                if (haveVariableSetEntriesListChanged) isVariableSetListEntriesUpsertSuccess_ = upsertVariableSetListAndVariableSetListEntries_VariableSetListEntries(connection, variableSetListFromDb_AfterUpsert_, variableSetNames);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        isOverallUpsertSuccess = isVariableSetListUpsertSuccess_ && isVariableSetListEntriesUpsertSuccess_;
        return isOverallUpsertSuccess;
    }
    
    private boolean haveVariableSetEntriesListChanged(Connection connection, VariableSetList variableSetList, TreeSet<String> variableSetNames) {
        boolean haveVariableSetListEntriesChanged = true;
        
        try {
            List<VariableSetListEntry> variableSetListEntriesFromDb = VariableSetListEntriesDao.getVariableSetListEntries_ByVariableSetListId(connection, false, variableSetList.getId());

            Set<String> variableSetNamesFromDb = new HashSet<>();
            
            // could optimize with a "where in (?,? ...)" query
            for (VariableSetListEntry variableSetListEntry : variableSetListEntriesFromDb) {
                if ((variableSetListEntry == null) || variableSetListEntry.getVariableSetId() == null) continue;
                
                VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetListEntry.getVariableSetId());
                if ((variableSet == null) || (variableSet.getName() == null)) continue;
                
                variableSetNamesFromDb.add(variableSet.getName());
            }
            
            boolean areVariableSetsEqual = CollectionUtilities.areSetContentsEqual(variableSetNames, variableSetNamesFromDb);

            if (areVariableSetsEqual) {
                logger.info("Alter variable set list: Variable set list \"" + variableSetList.getName() + "\" entries are unchanged.");
                haveVariableSetListEntriesChanged = false;
            }
            else {
                logger.info("Alter variable set list: Variable set list \"" + variableSetList.getName() + "\" entries are changed.");
            }
        }
        catch (Exception e) {
            logger.error("Alter variable set list: error reading variable set list entries. " + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return haveVariableSetListEntriesChanged;
    }
    
    private boolean upsertVariableSetListAndVariableSetListEntries_VariableSetListEntries(Connection connection, VariableSetList variableSetList, TreeSet<String> variableSetNames) {
        
        if (variableSetList == null) {
            return false;
        }
        
        boolean isAllVariableSetListEntryInsertSuccess = true;
               
        try {
            boolean isDeleteSuccess = VariableSetListEntriesDao.delete_ByVariableSetListId(connection, false, false, variableSetList.getId());
            
            if (!isDeleteSuccess) {
                String cleanVariableSetListName = StringUtilities.removeNewlinesFromString(variableSetList.getName(), ' ');
                logger.error("Failed to delete variable set list entries (which is part of updating them). VariableSetListName=\"" + cleanVariableSetListName + "\".");
            }
            
            if (isDeleteSuccess && (variableSetNames != null)) {
                for (String variableSetName : variableSetNames) {
                    if (variableSetName == null) continue;
                    
                    VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetName);
                    if ((variableSet == null) || (variableSet.getId() == null)) continue;
                
                    VariableSetListEntry variableSetListEntry = new VariableSetListEntry(-1, variableSetList.getId(), variableSet.getId());
                    boolean isVariableSetListEntryInsertSuccess = VariableSetListEntriesDao.upsert(connection, false, false, variableSetListEntry);

                    if (!isVariableSetListEntryInsertSuccess) {
                        String cleanVariableSetListName = StringUtilities.removeNewlinesFromString(variableSetList.getName(), ' ');
                        String cleanVariableSetName = StringUtilities.removeNewlinesFromString(variableSetName, ' ');
                        logger.error("Failed to insert variable set list entry. VariableSetListName=\"" + cleanVariableSetListName + "\". " + 
                                "VariableSetName=\"" + cleanVariableSetName + "\". ");
                        isAllVariableSetListEntryInsertSuccess = false;
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isAllVariableSetListEntryInsertSuccess = false;
        }
        
        return isAllVariableSetListEntryInsertSuccess;
    }
    
    private void getReturnString_AlterFailDetailed() {
        String variableSetListName = (variableSetList_ != null) ? variableSetList_.getName() : "Unknown" ;
            
        if (isNewDatabaseObject_) {
            returnString_ = "Failed to create " + HUMAN_FRIENDLY_NAME + ". " + LOG_FRIENDLY_NAME + "=\"" + variableSetListName + "\"" 
                    + ", VariableSetList_UpsertSuccess=" + isVariableSetListUpsertSuccess_ 
                    + ", VariableSetListEntries_UpsertSuccess=" + isVariableSetListEntriesUpsertSuccess_;
        }
        else {
            returnString_ = "Failed to alter " + HUMAN_FRIENDLY_NAME + ". " + LOG_FRIENDLY_NAME + "=\"" + variableSetListName + "\"" 
                    + ", VariableSetList_UpsertSuccess=" + isVariableSetListUpsertSuccess_ 
                    + ", VariableSetListEntries_UpsertSuccess=" + isVariableSetListEntriesUpsertSuccess_;
        }

        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString_, ' ');
        logger.warn(cleanReturnString);
    }
    
    private VariableSetListsDaoWrapper deleteRecordInDatabase() {
        
        if ((variableSetList_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((variableSetList_ == null) || (variableSetList_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        Connection connection = DatabaseConnections.getConnection(false);
            
        try {
            String variableSetListName = (variableSetList_ != null) ? variableSetList_.getName() : null;
            getReturnString_DeleteInitialValue(variableSetListName);

            boolean didVariableSetListEntriesDeleteSucceed = VariableSetListEntriesDao.delete_ByVariableSetListId(connection, false, false, variableSetList_.getId());
            boolean didVariableSetListDeleteSucceed = VariableSetListsDao.delete(connection, false, false, variableSetList_);

            if (didVariableSetListDeleteSucceed && didVariableSetListEntriesDeleteSucceed) {
                boolean didCommitSucceed = DatabaseUtils.commit(connection, false);

                if (!didCommitSucceed) {
                    DatabaseUtils.rollback(connection, false);
                    getReturnString_DeleteFail_CommitFail(variableSetListName);
                }
                else {
                    getReturnString_DeleteSuccess(variableSetListName);
                }
            }
            else {
                DatabaseUtils.rollback(connection, false);
                getReturnString_DeleteFail(variableSetListName);
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
    
    public static VariableSetListsDaoWrapper createRecordInDatabase(VariableSetList variableSetList, TreeSet<String> variableSetNames) {
        VariableSetListsDaoWrapper variableSetListsDaoWrapper = new VariableSetListsDaoWrapper(variableSetList);
        return variableSetListsDaoWrapper.alterRecordInDatabase(variableSetNames);
    }
    
    public static VariableSetListsDaoWrapper createRecordInDatabase(VariableSetList variableSetList, List<Integer> variableSetIds) {
        List<String> variableSetNames = VariableSetsDao.getVariableSetNames_OrderedByName(DatabaseConnections.getConnection(), true, variableSetIds);
        TreeSet<String> variableSetNames_TreeSet = (variableSetNames != null) ? new TreeSet(variableSetNames) : new TreeSet();
        return createRecordInDatabase(variableSetList, variableSetNames_TreeSet);
    }

    public static VariableSetListsDaoWrapper alterRecordInDatabase(VariableSetList variableSetList, TreeSet<String> variableSetNames) {
        String variableSetListName = (variableSetList != null) ? variableSetList.getName() : null;
        VariableSetListsDaoWrapper variableSetListsDaoWrapper = new VariableSetListsDaoWrapper(variableSetList, variableSetListName);
        return variableSetListsDaoWrapper.alterRecordInDatabase(variableSetNames);
    }
    
    public static VariableSetListsDaoWrapper alterRecordInDatabase(VariableSetList variableSetList, TreeSet<String> variableSetNames, String oldName) {
        VariableSetListsDaoWrapper variableSetListsDaoWrapper = new VariableSetListsDaoWrapper(variableSetList, oldName);
        return variableSetListsDaoWrapper.alterRecordInDatabase(variableSetNames);
    }
    
    public static VariableSetListsDaoWrapper deleteRecordInDatabase(String variableSetListName) {
        VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, variableSetListName);
        VariableSetListsDaoWrapper variableSetListsDaoWrapper = new VariableSetListsDaoWrapper(variableSetList);
        return variableSetListsDaoWrapper.deleteRecordInDatabase();
    }
    
    public static VariableSetListsDaoWrapper deleteRecordInDatabase(VariableSetList variableSetList) {
        VariableSetListsDaoWrapper variableSetListsDaoWrapper = new VariableSetListsDaoWrapper(variableSetList);
        return variableSetListsDaoWrapper.deleteRecordInDatabase();
    }
    
}
