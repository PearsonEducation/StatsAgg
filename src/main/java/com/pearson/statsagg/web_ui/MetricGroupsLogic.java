package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import java.util.TreeSet;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * This 'Logic' class was created to separate out business logic from 'MetricGroups'. 
 * The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class MetricGroupsLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsLogic.class.getName());
    
    public String alterRecordInDatabase(MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags) {
        return alterRecordInDatabase(metricGroup, matchRegexes, blacklistRegexes, tags, null);
    }
    
    public String alterRecordInDatabase(MetricGroup metricGroup, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, TreeSet<String> tags, String oldName) {
        
        if ((metricGroup == null) || (metricGroup.getName() == null) || (matchRegexes == null) || matchRegexes.isEmpty()) {
            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Failed to alter metric group.";
            logger.warn(returnString);
            return returnString;
        }

        String returnString;

        synchronized (GlobalVariables.metricGroupChanges) {
            Connection connection = DatabaseConnections.getConnection();
            DatabaseUtils.setAutoCommit(connection, false);
            
            try {
                boolean isNewMetricGroup = true, isOverwriteExistingAttempt = false;
                MetricGroup metricGroupFromDb;

                if ((oldName != null) && !oldName.isEmpty()) {
                    metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, oldName);

                    if (metricGroupFromDb != null) {
                        metricGroup.setId(metricGroupFromDb.getId());
                        isNewMetricGroup = false;
                    }
                    else {
                        isNewMetricGroup = true;
                    }
                }
                else {
                    metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, metricGroup.getName());
                    if (metricGroupFromDb != null) isOverwriteExistingAttempt = true;
                }

                boolean metricGroupUpsertSuccess = false;
                MetricGroup newMetricGroupFromDb = null;
                if (!isOverwriteExistingAttempt) {
                    metricGroupUpsertSuccess = MetricGroupsDao.upsert(connection, false, false, metricGroup);
                    newMetricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, metricGroup.getName());
                }

                if (isOverwriteExistingAttempt) {
                    DatabaseUtils.rollback(connection, false);
                    lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Failed to create metric group. A metric group with same name already exists. MetricGroupName=\"" + metricGroup.getName() + "\"";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.warn(cleanReturnString);
                    return returnString;
                }
                else if (metricGroupUpsertSuccess && (newMetricGroupFromDb != null)) {
                    boolean isAllMetricGroupRegexesUpsertSuccess = true, isAllMetricGroupTagsUpsertSuccess = true, isMetricGroupAssociationRoutineRequired = true;

                    // update regexes, if necessary
                    List<MetricGroupRegex> metricGroupRegexesFromDb = MetricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(connection, false, newMetricGroupFromDb.getId());

                    Set<String> metricGroupMatchRegexesFromDb_Set = new HashSet<>();
                    for (MetricGroupRegex metricGroupRegex : metricGroupRegexesFromDb) if (!metricGroupRegex.isBlacklistRegex()) metricGroupMatchRegexesFromDb_Set.add(metricGroupRegex.getPattern());
                    boolean areMatchRegexSetsEqual = areRegexSetContentsEqual(matchRegexes, metricGroupMatchRegexesFromDb_Set);

                    Set<String> metricGroupBlacklistRegexesFromDb_Set = new HashSet<>();
                    for (MetricGroupRegex metricGroupRegex : metricGroupRegexesFromDb) if (metricGroupRegex.isBlacklistRegex()) metricGroupBlacklistRegexesFromDb_Set.add(metricGroupRegex.getPattern());
                    boolean areBlacklistRegexSetsEqual = areRegexSetContentsEqual(blacklistRegexes, metricGroupBlacklistRegexesFromDb_Set);

                    if (areMatchRegexSetsEqual && areBlacklistRegexSetsEqual) {
                        logger.info("Alter metric group: Metric group \"" + newMetricGroupFromDb.getName() + "\" regular expression set is unchanged. This metric group will retain all metric-key associations.");
                        isMetricGroupAssociationRoutineRequired = false;
                    }
                    else {
                        logger.info("Alter metric group: Metric group \"" + newMetricGroupFromDb.getName() + "\" regular expression set is changed. This metric group will go through the metric-key association routine.");
                        isAllMetricGroupRegexesUpsertSuccess = alterRecordInDatabase_UpsertRegex(connection, matchRegexes, blacklistRegexes, newMetricGroupFromDb);
                    }

                    // update tags
                    isAllMetricGroupTagsUpsertSuccess = alterRecordInDatabase_UpsertTag(connection, tags, newMetricGroupFromDb);

                    if (isAllMetricGroupRegexesUpsertSuccess && isAllMetricGroupTagsUpsertSuccess) {
                        boolean didCommitSucceed = DatabaseUtils.commit(connection, false);

                        if (didCommitSucceed) {
                            if ((newMetricGroupFromDb.getId() != null) && isNewMetricGroup) GlobalVariables.metricGroupChanges.put(newMetricGroupFromDb.getId(), GlobalVariables.NEW);
                            else if ((newMetricGroupFromDb.getId() != null) && !isNewMetricGroup && isMetricGroupAssociationRoutineRequired) GlobalVariables.metricGroupChanges.put(newMetricGroupFromDb.getId(), GlobalVariables.ALTER);

                            lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;

                            if (isNewMetricGroup) returnString = "Successful metric group creation. MetricGroupName=\"" + metricGroup.getName() + "\"";
                            else returnString = "Successful metric group alteration. MetricGroupName=\"" + metricGroup.getName() + "\"";
                            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                            logger.info(cleanReturnString);
                        }
                        else {
                            DatabaseUtils.rollback(connection, false);
                            lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                            returnString = "Failed to create metric group. " + "MetricGroupName=\"" + metricGroup.getName() + "\"" + ", CommitSuccess=" + didCommitSucceed;
                            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                            logger.warn(cleanReturnString);
                        }
                    }
                    else { 
                        DatabaseUtils.rollback(connection, false);
                        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;

                        if (isNewMetricGroup) {
                            returnString = "Failed to create metric group. " + "MetricGroupName=\"" + metricGroup.getName() + "\"" 
                                    + ", MetricGroup_UpsertSuccess=" + metricGroupUpsertSuccess 
                                    + ", MetricGroupRegex_UpsertSuccess=" + isAllMetricGroupRegexesUpsertSuccess
                                    + ", MetricGroupTag_UpsertSuccess=" + isAllMetricGroupTagsUpsertSuccess;
                        }
                        else {
                            returnString = "Failed to alter metric group. " + "MetricGroupName=\"" + metricGroup.getName() + "\"" 
                                    + ", MetricGroup_UpsertSuccess=" + metricGroupUpsertSuccess 
                                    + ", MetricGroupRegex_UpsertSuccess=" + isAllMetricGroupRegexesUpsertSuccess
                                    + ", MetricGroupTag_UpsertSuccess=" + isAllMetricGroupTagsUpsertSuccess;
                        }

                        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                        logger.warn(cleanReturnString);
                    }

                    return returnString;
                }
                else {
                    DatabaseUtils.rollback(connection, false);
                    lastAlterRecordStatus_ = STATUS_CODE_FAILURE;

                    if (isNewMetricGroup) returnString = "Failed to create MetricGroup. " + "MetricGroupName=\"" + metricGroup.getName() + "\"";
                    else returnString = "Failed to alter MetricGroup. " + "MetricGroupName=\"" + metricGroup.getName() + "\"";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.warn(cleanReturnString);
                    return returnString;
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                return "Error creating metric group. MetricGroupName=\"" + metricGroup.getName() + "\".";
            }
            finally {
                DatabaseUtils.cleanup(connection);
            }
        }
    }
    
    public static boolean areRegexSetContentsEqual(Set<String> regexSet1, Set<String> regexSet2) {
        
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
    
    private static boolean alterRecordInDatabase_UpsertRegex(Connection connection, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, MetricGroup newMetricGroupFromDb) {
        
        boolean isAllMetricGroupRegexesUpsertSuccess = true;
                
        MetricGroupRegexesDao.deleteByMetricGroupId(connection, false, false, newMetricGroupFromDb.getId());

        if (matchRegexes != null) {
            for (String matchRegex : matchRegexes) {
                MetricGroupRegex metricGroupRegex = new MetricGroupRegex(-1, newMetricGroupFromDb.getId(), false, matchRegex);
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
                MetricGroupRegex metricGroupRegex = new MetricGroupRegex(-1, newMetricGroupFromDb.getId(), true, blacklistRegex);
                boolean isMetricGroupRegexInsertSuccess = MetricGroupRegexesDao.upsert(connection, false, false, metricGroupRegex);

                if (!isMetricGroupRegexInsertSuccess) {
                    String cleanRegex = StringUtilities.removeNewlinesFromString(blacklistRegex);
                    logger.warn("Failed to alter metric group blacklist regex. Regex=" + cleanRegex);
                    isAllMetricGroupRegexesUpsertSuccess = false;
                }
            }
        }
        
        return isAllMetricGroupRegexesUpsertSuccess;
    }
    
    private static boolean alterRecordInDatabase_UpsertTag(Connection connection, TreeSet<String> tags, MetricGroup newMetricGroupFromDb) {
        
        if (newMetricGroupFromDb == null) {
            return false;
        }
        
        boolean isAllMetricGroupTagsUpsertSuccess = true;
                
        MetricGroupTagsDao.deleteByMetricGroupId(connection, false, false, newMetricGroupFromDb.getId());

        if (tags != null) {
            for (String tag : tags) {
                MetricGroupTag metricGroupTag = new MetricGroupTag(-1, newMetricGroupFromDb.getId(), tag);
                boolean isMetricGroupTagInsertSuccess = MetricGroupTagsDao.upsert(connection, false, false, metricGroupTag);

                if (!isMetricGroupTagInsertSuccess) {
                    String cleanTag = StringUtilities.removeNewlinesFromString(tag, ' ');
                    logger.warn("Failed to alter metric group tag. Tag=" + cleanTag);
                    isAllMetricGroupTagsUpsertSuccess = false;
                }
            }
        }
        
        return isAllMetricGroupTagsUpsertSuccess;
    }
    
    public String deleteRecordInDatabase(String metricGroupName) {
        
        if ((metricGroupName == null) || metricGroupName.isEmpty()) {
            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
            String returnString = "Invalid metric group name. Cancelling delete operation.";
            logger.warn(returnString);
            return returnString;
        }

        String returnString = "Error deleting metric group. MetricGroupName=\"" + metricGroupName + "\".";
        
        synchronized (GlobalVariables.metricGroupChanges) {
            Connection connection = DatabaseConnections.getConnection();
            DatabaseUtils.setAutoCommit(connection, false);
            
            try {
                MetricGroup metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, metricGroupName);

                if (metricGroupFromDb != null) {
                    boolean didRegexDeleteSucceed = MetricGroupRegexesDao.deleteByMetricGroupId(connection, false, false, metricGroupFromDb.getId());
                    boolean didTagDeleteSucceed = MetricGroupTagsDao.deleteByMetricGroupId(connection, false, false, metricGroupFromDb.getId());
                    boolean didMetricGroupDeleteSucceed = MetricGroupsDao.delete(connection, false, false, metricGroupFromDb);
                    
                    if (didRegexDeleteSucceed && didTagDeleteSucceed && didMetricGroupDeleteSucceed) {
                        boolean didCommitSucceed = DatabaseUtils.commit(connection, false);
                        
                        if (!didCommitSucceed) {
                            DatabaseUtils.rollback(connection, false);
                            lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                            returnString = "Failed to delete metric group. MetricGroupName=\"" + metricGroupName + "\".";
                            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                            logger.warn(cleanReturnString);
                        }
                        else {
                            GlobalVariables.metricGroupChanges.put(metricGroupFromDb.getId(), GlobalVariables.REMOVE);
                            lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                            returnString = "Delete metric group success. MetricGroupName=\"" + metricGroupName + "\".";
                            String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                            logger.info(cleanReturnString);
                        }
                    }
                    else {
                        DatabaseUtils.rollback(connection, false);
                        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                        returnString = "Failed to delete metric group regexes and/or tags. MetricGroupName=\"" + metricGroupName + "\".";
                        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                        logger.warn(cleanReturnString);
                    }
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Metric group not found. MetricGroupName=\"" + metricGroupName + "\". Cancelling delete operation.";
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
