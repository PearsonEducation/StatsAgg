package com.pearson.statsagg.database_objects.metric_groups;

import com.pearson.statsagg.globals.DatabaseConnections;
import java.util.TreeSet;
import com.pearson.statsagg.database_objects.metric_group_regexes.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regexes.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import com.pearson.statsagg.database_objects.AbstractDaoWrapper;
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

    private MetricGroupsDaoWrapper alterRecordInDatabase(TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {
        
        if ((metricGroup_ == null) || (metricGroup_.getName() == null) || (matchRegexes == null) || (tags == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }
        
        synchronized (GlobalVariables.metricGroupChanges) {
            Connection connection = DatabaseConnections.getConnection(false);
                
            try {
                getReturnString_AlterInitialValue(metricGroup_.getName());
                
                MetricGroup metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, metricGroup_.getName());
                
                if (isNewDatabaseObject_ && (metricGroupFromDb != null)) {
                    getReturnString_CreateFail_SameNameAlreadyExists(metricGroup_.getName());
                }
                else {
                    boolean isOverallUpsertSuccess = upsertMetricGroupAndRegexesAndTags(connection, matchRegexes, blacklistRegexes, tags);

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
    
    private boolean hasMetricGroupRegexSetChanged(Connection connection, MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes) {
        boolean hasMetricGroupRegexSetChanged = true;
        
        List<MetricGroupRegex> metricGroupRegexesFromDb = MetricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(connection, false, metricGroup.getId());

        Set<String> metricGroupMatchRegexesFromDb_Set = new HashSet<>();
        for (MetricGroupRegex metricGroupRegex : metricGroupRegexesFromDb) if (!metricGroupRegex.isBlacklistRegex()) metricGroupMatchRegexesFromDb_Set.add(metricGroupRegex.getPattern());
        boolean areMatchRegexSetsEqual = areRegexSetContentsEqual(matchRegexes, metricGroupMatchRegexesFromDb_Set);

        Set<String> metricGroupBlacklistRegexesFromDb_Set = new HashSet<>();
        for (MetricGroupRegex metricGroupRegex : metricGroupRegexesFromDb) if (metricGroupRegex.isBlacklistRegex()) metricGroupBlacklistRegexesFromDb_Set.add(metricGroupRegex.getPattern());
        boolean areBlacklistRegexSetsEqual = areRegexSetContentsEqual(blacklistRegexes, metricGroupBlacklistRegexesFromDb_Set);

        if (areMatchRegexSetsEqual && areBlacklistRegexSetsEqual) {
            logger.info("Alter metric group: Metric group \"" + metricGroup.getName() + "\" regular expression set is unchanged. This metric group will retain all metric-key associations.");
            hasMetricGroupRegexSetChanged = false;
        }
        else {
            logger.info("Alter metric group: Metric group \"" + metricGroup.getName() + "\" regular expression set is changed. This metric group will go through the metric-key association routine.");
        }
        
        return hasMetricGroupRegexSetChanged;
    }
    
    private static boolean areRegexSetContentsEqual(Set<String> regexSet1, Set<String> regexSet2) {
        
        if ((regexSet1 == null) && (regexSet2 != null)) return false;
        if ((regexSet1 != null) && (regexSet2 == null)) return false;
        if ((regexSet1 == null) && (regexSet2 == null)) return true;
        if ((regexSet1 != null) && (regexSet2 != null) && (regexSet1.size() != regexSet2.size())) return false;
        
        if ((regexSet1 != null) && (regexSet2 != null)) {
            for (String regex : regexSet1) {
                if (!regexSet2.contains(regex)) return false;
            }
        }

        return true;
    }
    
    private boolean upsertMetricGroupAndRegexesAndTags(Connection connection, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {
        boolean isOverallUpsertSuccess = true;
                
        if (oldDatabaseObjectName_ == null) isMetricGroupUpsertSuccess_ = MetricGroupsDao.upsert(connection, false, false, metricGroup_);
        else isMetricGroupUpsertSuccess_ = MetricGroupsDao.upsert(connection, false, false, metricGroup_, oldDatabaseObjectName_);

        if (isMetricGroupUpsertSuccess_) { // upsert regexes & tags after successful update of the parent metric-group
            metricGroupFromDb_AfterUpsert_ = MetricGroupsDao.getMetricGroup(connection, false, metricGroup_.getName());
            isMetricGroupAssociationRoutineRequired_ = hasMetricGroupRegexSetChanged(connection, metricGroupFromDb_AfterUpsert_, matchRegexes, blacklistRegexes);
            if (isMetricGroupAssociationRoutineRequired_) isAllMetricGroupRegexesUpsertSuccess_ = upsertMetricGroupAndRegexesAndTags_UpsertRegexes(connection, metricGroupFromDb_AfterUpsert_, matchRegexes, blacklistRegexes);
            isAllMetricGroupTagsUpsertSuccess_ = upsertMetricGroupAndRegexesAndTags_UpsertTags(connection, metricGroupFromDb_AfterUpsert_, tags);
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
                        logger.warn("Failed to alter metric group regex. Regex=" + cleanRegex);
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
                        logger.warn("Failed to alter metric group blacklist regex. Regex=" + cleanRegex);
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
            MetricGroupTagsDao.deleteByMetricGroupId(connection, false, false, metricGroup.getId());

            if (tags != null) {
                for (String tag : tags) {
                    MetricGroupTag metricGroupTag = new MetricGroupTag(-1, metricGroup.getId(), tag);
                    boolean isMetricGroupTagInsertSuccess = MetricGroupTagsDao.upsert(connection, false, false, metricGroupTag);

                    if (!isMetricGroupTagInsertSuccess) {
                        String cleanTag = StringUtilities.removeNewlinesFromString(tag, ' ');
                        logger.warn("Failed to alter metric group tag. Tag=" + cleanTag);
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
    
    private void getReturnString_AlterFailDetailed() {
        if (isNewDatabaseObject_) {
            returnString_ = "Failed to create metric group. " + "MetricGroupName=\"" + metricGroup_.getName() + "\"" 
                    + ", MetricGroup_UpsertSuccess=" + isMetricGroupUpsertSuccess_ 
                    + ", MetricGroupRegex_UpsertSuccess=" + isAllMetricGroupRegexesUpsertSuccess_
                    + ", MetricGroupTag_UpsertSuccess=" + isAllMetricGroupTagsUpsertSuccess_;
        }
        else {
            returnString_ = "Failed to alter metric group. " + "MetricGroupName=\"" + metricGroup_.getName() + "\"" 
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
    
    public static MetricGroupsDaoWrapper createRecordInDatabase(MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup);
        return metricGroupsDaoWrapper.alterRecordInDatabase(matchRegexes, blacklistRegexes, tags);
    }
    
    public static MetricGroupsDaoWrapper alterRecordInDatabase(MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {
        String metricGroupName = (metricGroup != null) ? metricGroup.getName() : null;
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup, metricGroupName);
        return metricGroupsDaoWrapper.alterRecordInDatabase(matchRegexes, blacklistRegexes, tags);
    }

    public static MetricGroupsDaoWrapper alterRecordInDatabase(MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags, String oldName) {
        MetricGroupsDaoWrapper metricGroupsDaoWrapper = new MetricGroupsDaoWrapper(metricGroup, oldName);
        return metricGroupsDaoWrapper.alterRecordInDatabase(matchRegexes, blacklistRegexes, tags);
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
