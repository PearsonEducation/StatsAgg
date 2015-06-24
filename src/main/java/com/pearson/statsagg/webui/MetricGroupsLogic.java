package com.pearson.statsagg.webui;

import com.pearson.statsagg.database.DatabaseInterface;
import java.util.TreeSet;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StringUtilities;
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
    
    public static final Byte NEW = 1;
    public static final Byte ALTER = 2;
    public static final Byte REMOVE = 3;
    
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
            boolean isNewMetricGroup = true, isOverwriteExistingAttempt = false;
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao(false);
            metricGroupsDao.getDatabaseInterface().setIsManualTransactionControl(true);
            metricGroupsDao.getDatabaseInterface().beginTransaction();
            MetricGroup metricGroupFromDb;

            if ((oldName != null) && !oldName.isEmpty()) {
                metricGroupFromDb = metricGroupsDao.getMetricGroupByName(oldName);
                
                if (metricGroupFromDb != null) {
                    metricGroup.setId(metricGroupFromDb.getId());
                    isNewMetricGroup = false;
                }
                else {
                    isNewMetricGroup = true;
                }
            }
            else {
                metricGroupFromDb = metricGroupsDao.getMetricGroupByName(metricGroup.getName());
                if (metricGroupFromDb != null) isOverwriteExistingAttempt = true;
            }
            
            boolean metricGroupUpsertSuccess = false;
            MetricGroup newMetricGroupFromDb = null;
            if (!isOverwriteExistingAttempt) {
                metricGroupUpsertSuccess = metricGroupsDao.upsert(metricGroup);
                newMetricGroupFromDb = metricGroupsDao.getMetricGroupByName(metricGroup.getName());
            }
 
            if (isOverwriteExistingAttempt) {
                metricGroupsDao.getDatabaseInterface().endTransaction(false);
                metricGroupsDao.close();
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Failed to create metric group. A metric group with same name already exists. MetricGroupName=\"" + metricGroup.getName() + "\"";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
                return returnString;
            }
            else if (metricGroupUpsertSuccess && (newMetricGroupFromDb != null)) {
                boolean isAllMetricGroupRegexesUpsertSuccess = true, isAllMetricGroupTagsUpsertSuccess = true, isMetricGroupAssociationRoutineRequired = true;
                
                // update regexes, if necessary
                MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao(metricGroupsDao.getDatabaseInterface());
                List<MetricGroupRegex> metricGroupRegexesFromDb = metricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(newMetricGroupFromDb.getId());
                
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
                    isAllMetricGroupRegexesUpsertSuccess = alterRecordInDatabase_UpsertRegex(metricGroupsDao.getDatabaseInterface(), matchRegexes, blacklistRegexes, newMetricGroupFromDb);
                }

                // update tags
                isAllMetricGroupTagsUpsertSuccess = alterRecordInDatabase_UpsertTag(metricGroupsDao.getDatabaseInterface(), tags, newMetricGroupFromDb);

                if (isAllMetricGroupRegexesUpsertSuccess && isAllMetricGroupTagsUpsertSuccess) {
                    boolean didCommitSucceed = metricGroupsDao.getDatabaseInterface().endTransaction(true);
                    metricGroupsDao.close();

                    if (didCommitSucceed) {
                        if ((newMetricGroupFromDb.getId() != null) && isNewMetricGroup) GlobalVariables.metricGroupChanges.put(newMetricGroupFromDb.getId(), NEW);
                        else if ((newMetricGroupFromDb.getId() != null) && !isNewMetricGroup && isMetricGroupAssociationRoutineRequired) GlobalVariables.metricGroupChanges.put(newMetricGroupFromDb.getId(), ALTER);

                        lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;

                        if (isNewMetricGroup) returnString = "Successful metric group creation. MetricGroupName=\"" + metricGroup.getName() + "\"";
                        else returnString = "Successful metric group alteration. MetricGroupName=\"" + metricGroup.getName() + "\"";
                        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                        logger.info(cleanReturnString);
                    }
                    else {
                        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                        
                        returnString = "Failed to create metric group. " + "MetricGroupName=\"" + metricGroup.getName() + "\"" + ", CommitSuccess=" + didCommitSucceed;
                        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                        logger.warn(cleanReturnString);
                    }
                }
                else { 
                    metricGroupsDao.getDatabaseInterface().endTransaction(false);
                    metricGroupsDao.close();
                
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
                metricGroupsDao.getDatabaseInterface().endTransaction(false);
                metricGroupsDao.close();
                
                lastAlterRecordStatus_ = STATUS_CODE_FAILURE;

                if (isNewMetricGroup) returnString = "Failed to create MetricGroup. " + "MetricGroupName=\"" + metricGroup.getName() + "\"";
                else returnString = "Failed to alter MetricGroup. " + "MetricGroupName=\"" + metricGroup.getName() + "\"";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
                return returnString;
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
    
    private static boolean alterRecordInDatabase_UpsertRegex(DatabaseInterface databaseInterface, TreeSet<String> matchRegexes, TreeSet<String> blacklistRegexes, MetricGroup newMetricGroupFromDb) {
        
        boolean isAllMetricGroupRegexesUpsertSuccess = true;
                
        MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao(databaseInterface);
        metricGroupRegexesDao.deleteByMetricGroupId(newMetricGroupFromDb.getId());

        if (matchRegexes != null) {
            for (String matchRegex : matchRegexes) {
                MetricGroupRegex metricGroupRegex = new MetricGroupRegex(-1, newMetricGroupFromDb.getId(), false, matchRegex);
                boolean isMetricGroupRegexInsertSuccess = metricGroupRegexesDao.upsert(metricGroupRegex);

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
                boolean isMetricGroupRegexInsertSuccess = metricGroupRegexesDao.upsert(metricGroupRegex);

                if (!isMetricGroupRegexInsertSuccess) {
                    String cleanRegex = StringUtilities.removeNewlinesFromString(blacklistRegex);
                    logger.warn("Failed to alter metric group blacklist regex. Regex=" + cleanRegex);
                    isAllMetricGroupRegexesUpsertSuccess = false;
                }
            }
        }
        
        return isAllMetricGroupRegexesUpsertSuccess;
    }
    
    private static boolean alterRecordInDatabase_UpsertTag(DatabaseInterface databaseInterface, TreeSet<String> tags, MetricGroup newMetricGroupFromDb) {
        
        boolean isAllMetricGroupTagsUpsertSuccess = true;
                
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao(databaseInterface);
        metricGroupTagsDao.deleteByMetricGroupId(newMetricGroupFromDb.getId());

        if (tags != null) {
            for (String tag : tags) {
                MetricGroupTag metricGroupTag = new MetricGroupTag(-1, newMetricGroupFromDb.getId(), tag);
                boolean isMetricGroupTagInsertSuccess = metricGroupTagsDao.upsert(metricGroupTag);

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

        String returnString;
        
        synchronized (GlobalVariables.metricGroupChanges) {
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao(false);
            MetricGroup metricGroupFromDb = metricGroupsDao.getMetricGroupByName(metricGroupName);

            if (metricGroupFromDb != null) {
                MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao(false);
                boolean didRegexDeleteSucceed = metricGroupRegexesDao.deleteByMetricGroupId(metricGroupFromDb.getId());

                MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao(false);
                boolean didTagDeleteSucceed = metricGroupTagsDao.deleteByMetricGroupId(metricGroupFromDb.getId());

                if (didRegexDeleteSucceed && didTagDeleteSucceed) {
                    boolean didMetricGroupDeleteSucceed = metricGroupsDao.delete(metricGroupFromDb);

                    if (!didMetricGroupDeleteSucceed) {
                        metricGroupRegexesDao.getDatabaseInterface().endTransaction(false);
                        metricGroupTagsDao.getDatabaseInterface().endTransaction(false);

                        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                        returnString = "Failed to delete metric group. MetricGroupName=\"" + metricGroupName + "\".";
                        String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                        logger.warn(cleanReturnString);
                    }
                    else if (metricGroupFromDb.getId() != null) {
                        GlobalVariables.metricGroupChanges.put(metricGroupFromDb.getId(), REMOVE);
                    }

                    lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                    returnString = "Delete metric group success. MetricGroupName=\"" + metricGroupName + "\".";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.info(cleanReturnString);
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Failed to delete metric group regexes and/or tags. MetricGroupName=\"" + metricGroupName + "\".";
                    String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                    logger.warn(cleanReturnString);
                }

                metricGroupRegexesDao.close();
                metricGroupTagsDao.close();
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Metric group not found. MetricGroupName=\"" + metricGroupName + "\". Cancelling delete operation.";
                String cleanReturnString = StringUtilities.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }

            metricGroupsDao.close();
        }
        
        return returnString;
    }
    
}
