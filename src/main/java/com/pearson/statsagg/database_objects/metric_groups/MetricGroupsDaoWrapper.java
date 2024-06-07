package com.pearson.statsagg.database_objects.metric_groups;

import com.pearson.statsagg.globals.DatabaseConnections;
import java.util.TreeSet;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import com.pearson.statsagg.database_objects.AbstractDaoWrapper;
import com.pearson.statsagg.utilities.collection_utils.CollectionUtilities;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupsDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "metric group";
    private static final String LOG_FRIENDLY_NAME = "MetricGroup";
    
    private final MetricGroup metricGroup_;
    
    private MetricGroup metricGroupFromDb_AfterUpsert_ = null;
    private boolean isMetricGroupUpsertSuccess_ = true;
    private boolean isAllMetricGroupRegexesUpsertSuccess_ = true;
    private boolean isAllMetricGroupTagsUpsertSuccess_ = true;
    private boolean isMetricGroupAssociationRoutineRequired_ = true;
    
    public MetricGroupsDaoWrapper(MetricGroup metricGroup) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.metricGroup_ = metricGroup;
    }
    
    public MetricGroupsDaoWrapper(MetricGroup metricGroup, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.metricGroup_ = metricGroup;
    }

    private MetricGroupsDaoWrapper alterRecordInDatabase() {
        
        if ((metricGroup_ == null) || (metricGroup_.getName() == null) || (metricGroup_.getMatchRegexes() == null) || (metricGroup_.getMatchRegexes().isEmpty())) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        synchronized (GlobalVariables.metricGroupChanges) {
            Connection connection = DatabaseConnections.getConnection(false);
                
            try {
                getReturnString_AlterInitialValue(metricGroup_.getName());
                
                MetricGroup metricGroupFromDb = MetricGroupsDao.getMetricGroup_FilterByUppercaseName(connection, false, metricGroup_.getName());
                
                if (isNewDatabaseObject_ && (metricGroupFromDb != null)) {
                    getReturnString_CreateFail_SameNameAlreadyExists(metricGroup_.getName());
                }
                else {
                    boolean isOverallUpsertSuccess = upsertMetricGroupAndRegexesAndTags(connection, metricGroup_);

                    if (isOverallUpsertSuccess) {
                        boolean didCommitSucceed = DatabaseUtils.commit(connection, false);
                        if (didCommitSucceed) {
                            if ((metricGroupFromDb_AfterUpsert_.getId() != null) && isNewDatabaseObject_) GlobalVariables.metricGroupChanges.put(metricGroupFromDb_AfterUpsert_.getId(), GlobalVariables.NEW);
                            else if ((metricGroupFromDb_AfterUpsert_.getId() != null) && !isNewDatabaseObject_ && isMetricGroupAssociationRoutineRequired_) GlobalVariables.metricGroupChanges.put(metricGroupFromDb_AfterUpsert_.getId(), GlobalVariables.ALTER);
                            getReturnString_AlterSuccess(metricGroup_.getName());
                        }
                        else {
                            DatabaseUtils.rollback(connection, false);
                            getReturnString_AlterFail_CommitFail(metricGroup_.getName());
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
        }
        
        return this;
    }
    
    private boolean upsertMetricGroupAndRegexesAndTags(Connection connection, MetricGroup metricGroup) {
        boolean isOverallUpsertSuccess = true;
                
        if (oldDatabaseObjectName_ == null) isMetricGroupUpsertSuccess_ = MetricGroupsDao.upsert(connection, false, false, metricGroup_);
        else isMetricGroupUpsertSuccess_ = MetricGroupsDao.upsert(connection, false, false, metricGroup_, oldDatabaseObjectName_);

        if (isMetricGroupUpsertSuccess_) { // upsert regexes & tags after successful update of the parent metric-group
            metricGroupFromDb_AfterUpsert_ = MetricGroupsDao.getMetricGroup(connection, false, metricGroup_.getName());
            
            boolean haveMetricGroupRegexSetsChanged = haveMetricGroupRegexSetsChanged(connection, metricGroupFromDb_AfterUpsert_, metricGroup.getMatchRegexes(), metricGroup.getBlacklistRegexes());
            isMetricGroupAssociationRoutineRequired_ = haveMetricGroupRegexSetsChanged;
            if (haveMetricGroupRegexSetsChanged) isAllMetricGroupRegexesUpsertSuccess_ = upsertMetricGroupAndRegexesAndTags_UpsertRegexes(connection, metricGroupFromDb_AfterUpsert_, metricGroup.getMatchRegexes(), metricGroup.getBlacklistRegexes());
            
            boolean haveMetricGroupTagsChanged = haveMetricGroupTagsChanged(connection, metricGroupFromDb_AfterUpsert_, metricGroup.getTags()) ;
            if (haveMetricGroupTagsChanged) isAllMetricGroupTagsUpsertSuccess_ = upsertMetricGroupAndRegexesAndTags_UpsertTags(connection, metricGroupFromDb_AfterUpsert_, metricGroup.getTags());
        }
        
        isOverallUpsertSuccess = isMetricGroupUpsertSuccess_ && isAllMetricGroupRegexesUpsertSuccess_ && isAllMetricGroupTagsUpsertSuccess_;
        return isOverallUpsertSuccess;
    }
    
    private boolean upsertMetricGroupAndRegexesAndTags_UpsertRegexes(Connection connection, MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes) {
        
        boolean isAllMetricGroupRegexesUpsertSuccess = true;
                
        try {
            MetricGroupRegexesDao.deleteByMetricGroupId(connection, false, false, metricGroup.getId());

            if (matchRegexes != null) {
                for (String matchRegex : matchRegexes) {
                    MetricGroupRegex metricGroupRegex = new MetricGroupRegex(-1, metricGroup.getId(), false, matchRegex);
                    boolean isMetricGroupRegexInsertSuccess = MetricGroupRegexesDao.upsert(connection, false, false, metricGroupRegex);

                    if (!isMetricGroupRegexInsertSuccess) {
                        String cleanRegex = StringUtilities.removeNewlinesFromString(matchRegex);
                        logger.error("Failed to alter metric group regex. Regex=" + cleanRegex);
                        isAllMetricGroupRegexesUpsertSuccess = false;
                    }
                }
            }

            if (blacklistRegexes != null) {
                for (String blacklistRegex : blacklistRegexes) {
                    MetricGroupRegex metricGroupRegex = new MetricGroupRegex(-1, metricGroup.getId(), true, blacklistRegex);
                    boolean isMetricGroupRegexInsertSuccess = MetricGroupRegexesDao.upsert(connection, false, false, metricGroupRegex);

                    if (!isMetricGroupRegexInsertSuccess) {
                        String cleanRegex = StringUtilities.removeNewlinesFromString(blacklistRegex);
                        logger.error("Failed to alter metric group blacklist regex. Regex=" + cleanRegex);
                        isAllMetricGroupRegexesUpsertSuccess = false;
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isAllMetricGroupRegexesUpsertSuccess = false;
        }
        
        return isAllMetricGroupRegexesUpsertSuccess;
    }
    
    private boolean upsertMetricGroupAndRegexesAndTags_UpsertTags(Connection connection, MetricGroup metricGroup, TreeSet<String> tags) {
        
        if (metricGroup == null) {
            return false;
        }
        
        boolean isAllMetricGroupTagsUpsertSuccess = true;
               
        try {
            boolean isDeleteSuccess = MetricGroupTagsDao.deleteByMetricGroupId(connection, false, false, metricGroup.getId());

            if (isDeleteSuccess && (tags != null)) {
                for (String tag : tags) {
                    MetricGroupTag metricGroupTag = new MetricGroupTag(-1, metricGroup.getId(), tag);
                    boolean isMetricGroupTagInsertSuccess = MetricGroupTagsDao.upsert(connection, false, false, metricGroupTag);

                    if (!isMetricGroupTagInsertSuccess) {
                        String cleanTag = StringUtilities.removeNewlinesFromString(tag, ' ');
                        logger.error("Failed to alter metric group tag. Tag=" + cleanTag);
                        isAllMetricGroupTagsUpsertSuccess = false;
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            isAllMetricGroupTagsUpsertSuccess = false;
        }
        
        return isAllMetricGroupTagsUpsertSuccess;
    }
    
    private boolean haveMetricGroupRegexSetsChanged(Connection connection, MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes) {
        boolean hasMetricGroupRegexSetChanged = true;
        
        try {
            List<MetricGroupRegex> metricGroupRegexesFromDb = MetricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(connection, false, metricGroup.getId());

            Set<String> metricGroupMatchRegexesFromDb_Set = new HashSet<>();
            for (MetricGroupRegex metricGroupRegex : metricGroupRegexesFromDb) if (!metricGroupRegex.isBlacklistRegex()) metricGroupMatchRegexesFromDb_Set.add(metricGroupRegex.getPattern());
            boolean areMatchRegexSetsEqual = CollectionUtilities.areSetContentsEqual(matchRegexes, metricGroupMatchRegexesFromDb_Set);

            Set<String> metricGroupBlacklistRegexesFromDb_Set = new HashSet<>();
            for (MetricGroupRegex metricGroupRegex : metricGroupRegexesFromDb) if (metricGroupRegex.isBlacklistRegex()) metricGroupBlacklistRegexesFromDb_Set.add(metricGroupRegex.getPattern());
            boolean areBlacklistRegexSetsEqual = CollectionUtilities.areSetContentsEqual(blacklistRegexes, metricGroupBlacklistRegexesFromDb_Set);

            if (areMatchRegexSetsEqual && areBlacklistRegexSetsEqual) {
                logger.info("Alter metric group: Metric group \"" + metricGroup.getName() + "\" regular expression set is unchanged. This metric group will retain all metric-key associations.");
                hasMetricGroupRegexSetChanged = false;
            }
            else {
                logger.info("Alter metric group: Metric group \"" + metricGroup.getName() + "\" regular expression set is changed. This metric group will go through the metric-key association routine.");
            }
        }
        catch (Exception e) {
            logger.error("Alter metric group: error reading metric group regexes." + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return hasMetricGroupRegexSetChanged;
    }
    
    private boolean haveMetricGroupTagsChanged(Connection connection, MetricGroup metricGroup, TreeSet<String> tags) {
        boolean haveMetricGroupTagsChanged = true;
        
        try {
            List<MetricGroupTag> metricGroupTagsFromDb = MetricGroupTagsDao.getMetricGroupTagsByMetricGroupId(connection, false, metricGroup.getId());

            Set<String> metricGroupTagsFromDb_Set = new HashSet<>();
            for (MetricGroupTag metricGroupTag : metricGroupTagsFromDb) metricGroupTagsFromDb_Set.add(metricGroupTag.getTag());
            boolean areTagSetsEqual = CollectionUtilities.areSetContentsEqual(tags, metricGroupTagsFromDb_Set);

            if (areTagSetsEqual) {
                logger.info("Alter metric group: Metric group \"" + metricGroup.getName() + "\" tags are unchanged.");
                haveMetricGroupTagsChanged = false;
            }
            else {
                logger.info("Alter metric group: Metric group \"" + metricGroup.getName() + "\" tags are changed.");
            }
        }
        catch (Exception e) {
            logger.error("Alter metric group: error reading metric group tags." + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return haveMetricGroupTagsChanged;
    }
    
    private void getReturnString_AlterFailDetailed() {
        String metricGroupName = (metricGroup_ != null) ? metricGroup_.getName() : "Unknown" ;

        if (isNewDatabaseObject_) {
            returnString_ = "Failed to create metric group. " + "MetricGroupName=\"" + metricGroupName + "\"" 
                    + ", MetricGroup_UpsertSuccess=" + isMetricGroupUpsertSuccess_ 
                    + ", MetricGroupRegex_UpsertSuccess=" + isAllMetricGroupRegexesUpsertSuccess_
                    + ", MetricGroupTag_UpsertSuccess=" + isAllMetricGroupTagsUpsertSuccess_;
        }
        else {
            returnString_ = "Failed to alter metric group. " + "MetricGroupName=\"" + metricGroupName + "\"" 
                    + ", MetricGroup_UpsertSuccess=" + isMetricGroupUpsertSuccess_ 
                    + ", MetricGroupRegex_UpsertSuccess=" + isAllMetricGroupRegexesUpsertSuccess_
                    + ", MetricGroupTag_UpsertSuccess=" + isAllMetricGroupTagsUpsertSuccess_;
        }

        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString_, ' ');
        logger.warn(cleanReturnString);
    }

    private MetricGroupsDaoWrapper deleteRecordInDatabase() {
        
        if ((metricGroup_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((metricGroup_ == null) || (metricGroup_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        synchronized (GlobalVariables.metricGroupChanges) {
            Connection connection = DatabaseConnections.getConnection(false);
            
            try {
                String metricGroupName = (metricGroup_ != null) ? metricGroup_.getName() : null;
                getReturnString_DeleteInitialValue(metricGroupName);
                
                MetricGroup metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, metricGroupName);

                if (metricGroupFromDb != null) {
                    boolean didRegexDeleteSucceed = MetricGroupRegexesDao.deleteByMetricGroupId(connection, false, false, metricGroupFromDb.getId());
                    boolean didTagDeleteSucceed = MetricGroupTagsDao.deleteByMetricGroupId(connection, false, false, metricGroupFromDb.getId());
                    boolean didMetricGroupDeleteSucceed = MetricGroupsDao.delete(connection, false, false, metricGroupFromDb);
                    
                    if (didRegexDeleteSucceed && didTagDeleteSucceed && didMetricGroupDeleteSucceed) {
                        boolean didCommitSucceed = DatabaseUtils.commit(connection, false);
                        
                        if (!didCommitSucceed) {
                            DatabaseUtils.rollback(connection, false);
                            getReturnString_DeleteFail_CommitFail(metricGroupName);
                        }
                        else {
                            GlobalVariables.metricGroupChanges.put(metricGroupFromDb.getId(), GlobalVariables.REMOVE);
                            getReturnString_DeleteSuccess(metricGroupName);
                        }
                    }
                    else {
                        DatabaseUtils.rollback(connection, false);
                        getReturnString_DeleteFail(metricGroupName);
                    }
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
    
    public static MetricGroupsDaoWrapper createRecordInDatabase(MetricGroup metricGroup) {
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup);
        return metricGroupsDaoWrapper.alterRecordInDatabase();
    }
    
    public static MetricGroupsDaoWrapper alterRecordInDatabase(MetricGroup metricGroup) {
        String metricGroupName = (metricGroup != null) ? metricGroup.getName() : null;
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup, metricGroupName);
        return metricGroupsDaoWrapper.alterRecordInDatabase();
    }

    public static MetricGroupsDaoWrapper alterRecordInDatabase(MetricGroup metricGroup, String oldName) {
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup, oldName);
        return metricGroupsDaoWrapper.alterRecordInDatabase();
    }

    public static MetricGroupsDaoWrapper deleteRecordInDatabase(String metricGroupName) {
        MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, metricGroupName);
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup);
        return metricGroupsDaoWrapper.deleteRecordInDatabase();
    }
    
    public static MetricGroupsDaoWrapper deleteRecordInDatabase(MetricGroup metricGroup) {
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup);
        return metricGroupsDaoWrapper.deleteRecordInDatabase();
    }
    
}
