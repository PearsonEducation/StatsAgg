package com.pearson.statsagg.webui;

import com.pearson.statsagg.database.DatabaseInterface;
import java.util.TreeSet;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * This 'Logic' class was created to separate out business logic from 'MetricGroups'. 
 * The primary advantage of separating out this logic is to make unit-testing easier.
 */
public class MetricGroupsLogic extends AbstractDatabaseInteractionLogic {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsLogic.class.getName());
    
    public String alterRecordInDatabase(MetricGroup metricGroup, TreeSet<String> regexs, TreeSet<String> tags) {
            return alterRecordInDatabase(metricGroup, regexs, tags, null);
    }
    
    public String alterRecordInDatabase(MetricGroup metricGroup, TreeSet<String> regexs, TreeSet<String> tags, String oldName) {
        
        if ((metricGroup == null) || (metricGroup.getName() == null) || (regexs == null) || regexs.isEmpty()) {
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
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
                return returnString;
            }
            else if (metricGroupUpsertSuccess && (newMetricGroupFromDb != null)) {
                boolean isAllMetricGroupRegexsUpsertSuccess = alterRecordInDatabase_UpsertRegex(metricGroupsDao.getDatabaseInterface(), regexs, newMetricGroupFromDb);
                boolean isAllMetricGroupTagsUpsertSuccess = alterRecordInDatabase_UpsertTag(metricGroupsDao.getDatabaseInterface(), tags, newMetricGroupFromDb);

                if (isAllMetricGroupRegexsUpsertSuccess && isAllMetricGroupTagsUpsertSuccess) {
                    boolean didCommitSucceed = metricGroupsDao.getDatabaseInterface().endTransaction(true);
                    metricGroupsDao.close();

                    if (didCommitSucceed) {
                        if (newMetricGroupFromDb.getId() != null) {
                            GlobalVariables.metricGroupChanges.put(newMetricGroupFromDb.getId(), "Alter");
                        }

                        lastAlterRecordStatus_ = STATUS_CODE_SUCCESS;

                        if (isNewMetricGroup) returnString = "Successful metric group creation. MetricGroupName=\"" + metricGroup.getName() + "\"";
                        else returnString = "Successful metric group alteration. MetricGroupName=\"" + metricGroup.getName() + "\"";
                        String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                        logger.info(cleanReturnString);
                    }
                    else {
                        lastAlterRecordStatus_ = STATUS_CODE_FAILURE;
                        
                        returnString = "Failed to create metric group. " + "MetricGroupName=\"" + metricGroup.getName() + "\"" + ", CommitSuccess=" + didCommitSucceed;
                        String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
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
                                + ", MetricGroupRegex_UpsertSuccess=" + isAllMetricGroupRegexsUpsertSuccess
                                + ", MetricGroupTag_UpsertSuccess=" + isAllMetricGroupTagsUpsertSuccess;
                    }
                    else {
                        returnString = "Failed to alter metric group. " + "MetricGroupName=\"" + metricGroup.getName() + "\"" 
                                + ", MetricGroup_UpsertSuccess=" + metricGroupUpsertSuccess 
                                + ", MetricGroupRegex_UpsertSuccess=" + isAllMetricGroupRegexsUpsertSuccess
                                + ", MetricGroupTag_UpsertSuccess=" + isAllMetricGroupTagsUpsertSuccess;
                    }
                    
                    String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
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
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
                return returnString;
            }
        }
        
    }
    
    private static boolean alterRecordInDatabase_UpsertRegex(DatabaseInterface databaseInterface, TreeSet<String> regexs, MetricGroup newMetricGroupFromDb) {
        
        boolean isAllMetricGroupRegexsUpsertSuccess = true;
                
        MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao(databaseInterface);
        metricGroupRegexsDao.deleteByMetricGroupId(newMetricGroupFromDb.getId());

        for (String regex : regexs) {
            MetricGroupRegex metricGroupRegex = new MetricGroupRegex(-1, newMetricGroupFromDb.getId(), regex);
            boolean isMetricGroupRegexInsertSuccess = metricGroupRegexsDao.upsert(metricGroupRegex);

            if (!isMetricGroupRegexInsertSuccess) {
                String cleanRegex = StatsAggHtmlFramework.removeNewlinesFromString(regex);
                logger.warn("Failed to alter metric group regex. Regex=" + cleanRegex);
                isAllMetricGroupRegexsUpsertSuccess = false;
            }
        }
        
        return isAllMetricGroupRegexsUpsertSuccess;
    }
    
    private static boolean alterRecordInDatabase_UpsertTag(DatabaseInterface databaseInterface, TreeSet<String> tags, MetricGroup newMetricGroupFromDb) {
        
        boolean isAllMetricGroupTagsUpsertSuccess = true;
                
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao(databaseInterface);
        metricGroupTagsDao.deleteByMetricGroupId(newMetricGroupFromDb.getId());

        for (String tag : tags) {
            MetricGroupTag metricGroupTag = new MetricGroupTag(-1, newMetricGroupFromDb.getId(), tag);
            boolean isMetricGroupTagInsertSuccess = metricGroupTagsDao.upsert(metricGroupTag);

            if (!isMetricGroupTagInsertSuccess) {
                String cleanTag = StatsAggHtmlFramework.removeNewlinesFromString(tag, ' ');
                logger.warn("Failed to alter metric group tag. Tag=" + cleanTag);
                isAllMetricGroupTagsUpsertSuccess = false;
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
                MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao(false);
                boolean didRegexDeleteSucceed = metricGroupRegexsDao.deleteByMetricGroupId(metricGroupFromDb.getId());

                MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao(false);
                boolean didTagDeleteSucceed = metricGroupTagsDao.deleteByMetricGroupId(metricGroupFromDb.getId());

                if (didRegexDeleteSucceed && didTagDeleteSucceed) {
                    boolean didMetricGroupDeleteSucceed = metricGroupsDao.delete(metricGroupFromDb);

                    if (!didMetricGroupDeleteSucceed) {
                        metricGroupRegexsDao.getDatabaseInterface().endTransaction(false);
                        metricGroupTagsDao.getDatabaseInterface().endTransaction(false);

                        lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                        returnString = "Failed to delete metric group. MetricGroupName=\"" + metricGroupName + "\".";
                        String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                        logger.warn(cleanReturnString);
                    }
                    else {
                        if (metricGroupFromDb.getId() != null) {
                            GlobalVariables.metricGroupChanges.put(metricGroupFromDb.getId(), "Remove");
                        }  
                    }

                    lastDeleteRecordStatus_ = STATUS_CODE_SUCCESS;
                    returnString = "Delete metric group success. MetricGroupName=\"" + metricGroupName + "\".";
                    String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                    logger.info(cleanReturnString);
                }
                else {
                    lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                    returnString = "Failed to delete metric group regexs and/or tags. MetricGroupName=\"" + metricGroupName + "\".";
                    String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                    logger.warn(cleanReturnString);
                }

                metricGroupRegexsDao.close();
                metricGroupTagsDao.close();
            }
            else {
                lastDeleteRecordStatus_ = STATUS_CODE_FAILURE;
                returnString = "Metric group not found. MetricGroupName=\"" + metricGroupName + "\". Cancelling delete operation.";
                String cleanReturnString = StatsAggHtmlFramework.removeNewlinesFromString(returnString, ' ');
                logger.warn(cleanReturnString);
            }

            metricGroupsDao.close();
        }
        
        return returnString;
    }
    
}
