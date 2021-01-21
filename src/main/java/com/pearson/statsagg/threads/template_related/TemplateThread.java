package com.pearson.statsagg.threads.template_related;

import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.alerts.AlertsDaoWrapper;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDao;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDaoWrapper;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDaoWrapper;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklistDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntriesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class TemplateThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateThread.class.getName());
    
    private static final AtomicBoolean isThreadCurrentlyRunning_ = new AtomicBoolean(false);
    
    protected final ThreadPoolExecutor threadPoolExecutor_;
    protected final Long threadStartTimestampInMilliseconds_;
    protected final String threadId_;
    
    public TemplateThread(Long threadStartTimestampInMilliseconds, ThreadPoolExecutor threadPoolExecutor) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadPoolExecutor_ = threadPoolExecutor;
        this.threadId_ = "T-" + threadStartTimestampInMilliseconds_.toString();
    }
    
    @Override
    public void run() {

        try {
            // stops multiple template threads from running simultaneously 
            if (!isThreadCurrentlyRunning_.compareAndSet(false, true)) {
                if ((threadPoolExecutor_ != null) && (threadPoolExecutor_.getActiveCount() <= 1)) {
                    logger.warn("Invalid template thread state detected (StatsAgg thinks another template thread it is running, but it is not.");
                    isThreadCurrentlyRunning_.set(false);
                }
                else {
                    logger.warn("ThreadId=" + threadId_ + ", Routine=Template, Message=\"Only 1 template thread can run at a time\"");
                    return;
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        try {
            long templateRoutineStartTime = System.currentTimeMillis();
            
            // update names
            updateAlertNamesToMatchAlertTemplate(); // if an alert template updated the 'alert name variable', update the derived alert names
            updateMetricGroupNamesToMatchMetricGroupTemplate(); // if an metric group template updated the 'metric group name variable', update the derived metric group names

            // delete objects that the templates no longer want to exist
            deleteAlertsThatAreNoLongerPartOfTemplates();
            deleteMetricGroupsThatAreNoLongerPartOfTemplates();
            
            // delete templates that are marked for deletion
            deleteMetricGroupsTemplatesThatAreMarkedForDeletion();
                    
            // create and/or alter objects based on templates
            createOrAlterMetricGroupsFromMetricGroupTemplate();
            createOrAlterAlertsFromAlertTemplate(); // 
            
            long templateRoutineTimeElapsed = System.currentTimeMillis() - templateRoutineStartTime;

            String outputMessage = "ThreadId=" + threadId_
                    + ", Routine=Template"
                    + ", TemplateRoutineTime=" + templateRoutineTimeElapsed
                    ;

            logger.info(outputMessage);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        isThreadCurrentlyRunning_.set(false);
    }
    
    private void updateMetricGroupNamesToMatchMetricGroupTemplate() {
        
        Connection connection = DatabaseConnections.getConnection();
        Map<Integer,MetricGroupTemplate> metricGroupTemplates_ById = MetricGroupTemplatesDao.getMetricGroupTemplates_ById(connection, false);
        Map<String,MetricGroup> metricGroups_ByName = MetricGroupsDao.getMetricGroups_ByName(connection, false);
        Map<Integer,VariableSet> variableSets_ById = VariableSetsDao.getVariableSets_ById(connection, false);
        DatabaseUtils.cleanup(connection);
        
        if (metricGroups_ByName == null) { // if this is null, something went wrong querying the db, best to take no action
            return; 
        } 
        
        Set<String> uppercaseChangedMetricGroupNames = new HashSet<>();
        
        for (MetricGroup metricGroup : metricGroups_ByName.values()) {
            try {
                if (metricGroup == null) continue;
                if (metricGroup.getName() == null) continue; // invalid data condition
                if (metricGroupTemplates_ById == null) continue; // if this is null, something went wrong querying the db, best to take no action
                if (variableSets_ById == null) continue; // if this is null, something went wrong querying the db, best to take no action
                if ((metricGroup.getMetricGroupTemplateId() == null) && (metricGroup.getVariableSetId() == null)) continue; // not dervied from an metric group template
                if ((metricGroup.getMetricGroupTemplateId() != null) && (metricGroup.getVariableSetId() == null)) continue; // invalid data condition
                if ((metricGroup.getMetricGroupTemplateId() == null) && (metricGroup.getVariableSetId() != null)) continue; // invalid data condition

                MetricGroupTemplate metricGroupTemplate = metricGroupTemplates_ById.get(metricGroup.getMetricGroupTemplateId());
                if (metricGroupTemplate == null) continue; // metric group template doesn't exist, metric group is not template-based, so there's nothing to rename
                
                List<Integer> variableSetIdsAssociatedWithMetricGroupTemplate_List =  VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(DatabaseConnections.getConnection(), true, metricGroupTemplate.getVariableSetListId());
                if (variableSetIdsAssociatedWithMetricGroupTemplate_List == null) continue; // if this is null, something went wrong querying the db, best to take no action
                Set<Integer> variableSetIdsAssociatedWithMetricGroupTemplate_Set = new HashSet<>(variableSetIdsAssociatedWithMetricGroupTemplate_List);
                
                if (variableSetIdsAssociatedWithMetricGroupTemplate_Set.contains(metricGroup.getVariableSetId()) && variableSets_ById.containsKey(metricGroup.getVariableSetId())) {
                    VariableSet variableSet = variableSets_ById.get(metricGroup.getVariableSetId());
                    if (variableSet == null) continue; // invalid data condition
                    
                    String metricGroupName_New = Common.getStringWithVariableSubsistution(metricGroupTemplate.getMetricGroupNameVariable(), variableSet);
                    if (metricGroupName_New == null) continue; // invalid data condition
                    if (metricGroupName_New.equals(metricGroup.getName())) continue; // metricGroup names match, nothing to do
                    boolean isOnlyCaseChange = metricGroupName_New.equalsIgnoreCase(metricGroup.getName());
                    
                    Set<String> uppercaseMetricGroupNames = Common.getUppercaseStringSet(metricGroups_ByName.keySet());
                    if (uppercaseMetricGroupNames == null) continue; // invalid data condition
                    uppercaseMetricGroupNames.addAll(uppercaseChangedMetricGroupNames);
                    if (!isOnlyCaseChange && uppercaseMetricGroupNames.contains(metricGroupName_New.toUpperCase())) continue; // can't update metric group name because another metric group with the same name already exists
  
                    // update the metricGroup name to match what is desired by the metric group template
                    boolean wasMetricGroupNameUpdateSuccess = MetricGroupsDao.update_Name(DatabaseConnections.getConnection(), true, true, metricGroup.getId(), metricGroupName_New);
                    if (wasMetricGroupNameUpdateSuccess) {
                        logger.info("Changed MetricGroupName=\"" + metricGroup.getName() + "\" to \"" + metricGroupName_New + "\"");
                        uppercaseChangedMetricGroupNames.add(metricGroupName_New.toUpperCase());
                    }
                    else logger.warn("Failed to change MetricGroupName=\"" + metricGroup.getName() + "\" to \"" + metricGroupName_New + "\"");
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
    }
    
    private void deleteMetricGroupsThatAreNoLongerPartOfTemplates() {
        
        Connection connection = DatabaseConnections.getConnection();
        Map<Integer,MetricGroupTemplate> metricGroupTemplates_ById = MetricGroupTemplatesDao.getMetricGroupTemplates_ById(connection, false);
        Map<String,MetricGroup> metricGroups_ByName = MetricGroupsDao.getMetricGroups_ByName(connection, false);
        Set<Integer> metricGroupIdsAssociatedWithAlerts = AlertsDao.getMetricGroupIdsAssociatedWithAlerts(connection, false);
        OutputBlacklist outputBlacklist = OutputBlacklistDao.getOutputBlacklist_SingleRow(connection, false);
        DatabaseUtils.cleanup(connection);
        
        if (metricGroups_ByName == null) { // if this is null, something went wrong querying the db, best to take no action
            return; 
        } 
        
        for (MetricGroup metricGroup : metricGroups_ByName.values()) {
            try {
                if (metricGroup == null) continue;
                if (metricGroupTemplates_ById == null) continue; // if this is null, something went wrong querying the db, best to take no action
                if (metricGroupIdsAssociatedWithAlerts == null) continue; // if this is null, something went wrong querying the db, best to take no action
                if ((metricGroup.getId() != null) && metricGroupIdsAssociatedWithAlerts.contains(metricGroup.getId())) continue; // can't delete a metric group if it is associated with an alert
                if ((outputBlacklist != null) && (outputBlacklist.getMetricGroupId() != null) && (metricGroup.getId() != null) && outputBlacklist.getMetricGroupId().equals(metricGroup.getId())); // can't delete a metric group if it is associated with the output blacklist
                
                // if the metric group doesn't have a template id, but does have a variable set id, then it should be deleted (unsupported data condition)
                if ((metricGroup.getMetricGroupTemplateId() == null) && (metricGroup.getVariableSetId() != null)) {
                    MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroup);
                    continue;
                }
                else if (metricGroup.getMetricGroupTemplateId() == null) continue;

                // if the metric group has a template id, but does not have a variable set id, then it should be deleted (unsupported data condition)
                if ((metricGroup.getMetricGroupTemplateId() != null) && (metricGroup.getVariableSetId() == null)) {
                    MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroup);
                    continue;
                }
                
                // delete the metric group if the metric group template that created it no longer exists
                MetricGroupTemplate metricGroupTemplate = metricGroupTemplates_ById.get(metricGroup.getMetricGroupTemplateId());
                if (metricGroupTemplate == null) {
                    MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroup);
                    continue;
                }

                // delete the metric group if there is no longer a corresponding variable set list entry
                if (metricGroup.getVariableSetId() != null) {
                    List<Integer> variableSetIdsAssociatedWithMetricGroupTemplate_List =  VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(DatabaseConnections.getConnection(), true, metricGroupTemplate.getVariableSetListId());
                    if (variableSetIdsAssociatedWithMetricGroupTemplate_List == null) continue; // if this is null, something went wrong querying the db, best to take no action
                    Set<Integer> variableSetIdsAssociatedWithMetricGroupTemplate_Set = new HashSet<>(variableSetIdsAssociatedWithMetricGroupTemplate_List);
                    
                    if (!variableSetIdsAssociatedWithMetricGroupTemplate_Set.contains(metricGroup.getVariableSetId())) {
                        MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroup);
                        continue;
                    }
                }
                
                // delete metric groups that have metric group names that don't align with what the metric group template wants the metric group names to be
                if (metricGroupTemplate.getVariableSetListId() != null) {
                    Set<String> metricGroupNamesThatMetricGroupTemplateWantsToCreate = Common.getNamesThatTemplateWantsToCreate(metricGroupTemplate.getVariableSetListId(), metricGroupTemplate.getMetricGroupNameVariable());
                    if (metricGroupNamesThatMetricGroupTemplateWantsToCreate == null) continue; // if this is null, something went wrong querying the db, best to take no action

                    if (!metricGroupNamesThatMetricGroupTemplateWantsToCreate.contains((metricGroup.getName()))) {
                        MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroup);
                    }
                }
                
                // delete metric groups when the metric group template itself is marked for deletion
                if ((metricGroupTemplate.isMarkedForDelete() != null) && metricGroupTemplate.isMarkedForDelete()) {
                    MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroup);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

    }
    
    private void deleteMetricGroupsTemplatesThatAreMarkedForDeletion() {
        
        Connection connection = DatabaseConnections.getConnection();
        List<MetricGroupTemplate> metricGroupTemplates = MetricGroupTemplatesDao.getMetricGroupTemplates(connection, false);
        DatabaseUtils.cleanup(connection);
        
        if (metricGroupTemplates == null) { // if this is null, something went wrong querying the db, best to take no action
            return; 
        } 
        
        for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
            try {
                if (metricGroupTemplate == null) continue; // if this is null, something went wrong. best to take no action
                if (metricGroupTemplate.isMarkedForDelete() == null) continue; // invalid data condition
                if (metricGroupTemplate.getId() == null) continue; // invalid data condition
                
                List<MetricGroup> metricGroupsAssociatedWithMetricGroupTemplate = MetricGroupsDao.getMetricGroups_FilterByMetricGroupTemplateId(DatabaseConnections.getConnection(), true, metricGroupTemplate.getId());
                if (metricGroupsAssociatedWithMetricGroupTemplate == null) continue; // if this is null, something went wrong. best to take no action
                
                // if there are no metric groups associated with the template, and the template is marked for deletion, then delete the template
                if (metricGroupTemplate.isMarkedForDelete() && (metricGroupsAssociatedWithMetricGroupTemplate.size() <= 0)) {
                    MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase(metricGroupTemplate);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

    }
    
    private void updateAlertNamesToMatchAlertTemplate() {
        
        Connection connection = DatabaseConnections.getConnection();
        Map<Integer,AlertTemplate> alertTemplates_ById = AlertTemplatesDao.getAlertTemplates_ById(connection, false);
        Map<String,Alert> alerts_ByName = AlertsDao.getAlerts_ByName(connection, false);
        Map<Integer,VariableSet> variableSets_ById = VariableSetsDao.getVariableSets_ById(connection, false);
        DatabaseUtils.cleanup(connection);
        
        if (alerts_ByName == null) { // if this is null, something went wrong querying the db, best to take no action
            return; 
        } 
        
        Set<String> uppercaseChangedAlertNames = new HashSet<>();
        
        for (Alert alert : alerts_ByName.values()) {
            try {
                if (alert == null) continue;
                if (alert.getName() == null) continue; // invalid data condition
                if (alertTemplates_ById == null) continue; // if this is null, something went wrong querying the db, best to take no action
                if (variableSets_ById == null) continue; // if this is null, something went wrong querying the db, best to take no action
                if ((alert.getAlertTemplateId() == null) && (alert.getVariableSetId() == null)) continue; // not dervied from an alert template
                if ((alert.getAlertTemplateId() != null) && (alert.getVariableSetId() == null)) continue; // invalid data condition
                if ((alert.getAlertTemplateId() == null) && (alert.getVariableSetId() != null)) continue; // invalid data condition

                AlertTemplate alertTemplate = alertTemplates_ById.get(alert.getAlertTemplateId());
                if (alertTemplate == null) continue; // alert template doesn't exist, alert is not template-based, so there's nothing to rename
                
                List<Integer> variableSetIdsAssociatedWithAlertTemplate_List =  VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(DatabaseConnections.getConnection(), true, alertTemplate.getVariableSetListId());
                if (variableSetIdsAssociatedWithAlertTemplate_List == null) continue; // if this is null, something went wrong querying the db, best to take no action
                Set<Integer> variableSetIdsAssociatedWithAlertTemplate_Set = new HashSet<>(variableSetIdsAssociatedWithAlertTemplate_List);
                
                if (variableSetIdsAssociatedWithAlertTemplate_Set.contains(alert.getVariableSetId()) && variableSets_ById.containsKey(alert.getVariableSetId())) {
                    VariableSet variableSet = variableSets_ById.get(alert.getVariableSetId());
                    if (variableSet == null) continue; // invalid data condition
                    
                    String alertName_New = Common.getStringWithVariableSubsistution(alertTemplate.getAlertNameVariable(), variableSet);
                    if (alertName_New == null) continue; // invalid data condition
                    if (alertName_New.equals(alert.getName())) continue; // alert names match, nothing to do
                    boolean isOnlyCaseChange = alertName_New.equalsIgnoreCase(alert.getName());
                    
                    Set<String> uppercaseAlertNames = Common.getUppercaseStringSet(alerts_ByName.keySet());
                    if (uppercaseAlertNames == null) continue; // invalid data condition
                    uppercaseAlertNames.addAll(uppercaseChangedAlertNames);
                    if (!isOnlyCaseChange && uppercaseAlertNames.contains(alertName_New.toUpperCase())) continue; // can't update alert name because another alert with the same name already exists
  
                    // update the alert name to match what is desired by the alert template
                    boolean wasAlertNameUpdateSuccess = AlertsDao.update_Name(DatabaseConnections.getConnection(), true, true, alert.getId(), alertName_New);
                    if (wasAlertNameUpdateSuccess) {
                        logger.info("Changed AlertName=\"" + alert.getName() + "\" to \"" + alertName_New + "\"");
                        uppercaseChangedAlertNames.add(alertName_New.toUpperCase());
                    }
                    else logger.warn("Failed to change AlertName=\"" + alert.getName() + "\" to \"" + alertName_New + "\"");
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
    }
    
    private void deleteAlertsThatAreNoLongerPartOfTemplates() {
        
        Connection connection = DatabaseConnections.getConnection();
        Map<Integer,AlertTemplate> alertTemplates_ById = AlertTemplatesDao.getAlertTemplates_ById(connection, false);
        Map<String,Alert> alerts_ByName = AlertsDao.getAlerts_ByName(connection, false);
        DatabaseUtils.cleanup(connection);
        
        if (alerts_ByName == null) { // if this is null, something went wrong querying the db, best to take no action
            return; 
        } 
        
        for (Alert alert : alerts_ByName.values()) {
            try {
                if (alert == null) continue;
                if (alertTemplates_ById == null) continue; // if this is null, something went wrong querying the db, best to take no action

                // if the alert doesn't have a template id, but does have a variable set id, then it should be deleted (unsupported data condition)
                if ((alert.getAlertTemplateId() == null) && (alert.getVariableSetId() != null)) {
                    AlertsDaoWrapper.deleteRecordInDatabase(alert);
                    continue;
                }
                else if (alert.getAlertTemplateId() == null) continue;

                // if the alert has a template id, but does not have a variable set id, then it should be deleted (unsupported data condition)
                if ((alert.getAlertTemplateId() != null) && (alert.getVariableSetId() == null)) {
                    AlertsDaoWrapper.deleteRecordInDatabase(alert);
                    continue;
                }
                
                // delete the alert if the alert template that created it no longer exists
                AlertTemplate alertTemplate = alertTemplates_ById.get(alert.getAlertTemplateId());
                if (alertTemplate == null) {
                    AlertsDaoWrapper.deleteRecordInDatabase(alert);
                    continue;
                }

                // delete the alert if there is no longer a corresponding variable set list entry
                if (alert.getVariableSetId() != null) {
                    List<Integer> variableSetIdsAssociatedWithAlertTemplate_List =  VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(DatabaseConnections.getConnection(), true, alertTemplate.getVariableSetListId());
                    if (variableSetIdsAssociatedWithAlertTemplate_List == null) continue; // if this is null, something went wrong querying the db, best to take no action
                    Set<Integer> variableSetIdsAssociatedWithAlertTemplate_Set = new HashSet<>(variableSetIdsAssociatedWithAlertTemplate_List);
                    
                    if (!variableSetIdsAssociatedWithAlertTemplate_Set.contains(alert.getVariableSetId())) {
                        AlertsDaoWrapper.deleteRecordInDatabase(alert);
                        continue;
                    }
                }
                
                // delete alerts that have alert names that don't align with what the alert template wants the alert names to be
                if (alertTemplate.getVariableSetListId() != null) {
                    Set<String> alertNamesThatAlertTemplateWantsToCreate = Common.getNamesThatTemplateWantsToCreate(alertTemplate.getVariableSetListId(), alertTemplate.getAlertNameVariable());
                    if (alertNamesThatAlertTemplateWantsToCreate == null) continue; // if this is null, something went wrong querying the db, best to take no action

                    if (!alertNamesThatAlertTemplateWantsToCreate.contains((alert.getName()))) {
                        AlertsDaoWrapper.deleteRecordInDatabase(alert);
                    }
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

    }
    
    private void createOrAlterAlertsFromAlertTemplate() {
        
        Connection connection = DatabaseConnections.getConnection();
        List<AlertTemplate> alertTemplates = AlertTemplatesDao.getAlertTemplates(connection, false);
        Map<String,Alert> alerts_ByName = AlertsDao.getAlerts_ByName(connection, false);
        Map<String,MetricGroup> metricGroups_ByName = MetricGroupsDao.getMetricGroups_ByName(connection, false);
        Map<String,NotificationGroup> notificationGroups_ByName = NotificationGroupsDao.getNotificationGroups_ByName(connection, false);
        DatabaseUtils.cleanup(connection);
        
        // if any of these are null, something went wrong querying the db, best to take no action
        if ((alertTemplates == null) || (alerts_ByName == null) || (metricGroups_ByName == null) || (notificationGroups_ByName == null)) { 
            return;
        }
        
        for (AlertTemplate alertTemplate : alertTemplates) {
            try {
                if (alertTemplate == null) continue;
                
                List<VariableSet> variableSets = Common.getVariableSetsFromVariableSetIdList(alertTemplate.getVariableSetListId());
                if (variableSets == null) continue; // if this is null, something went wrong querying the db, best to take no action
                
                for (VariableSet variableSet : variableSets) {
                    if (variableSet == null) continue;

                    Alert alert = createAlertFromAlertTemplate(alertTemplate, variableSet, alerts_ByName, metricGroups_ByName, notificationGroups_ByName);
                    if (alert == null) continue;
                    
                    String alertName = Common.getStringWithVariableSubsistution(alertTemplate.getAlertNameVariable(), variableSet);
                    Alert alertFromDb = alerts_ByName.get(alertName);
                    boolean isAlertEqualToAlertInDatabase = alert.isEqual(alertFromDb);

                    if (!isAlertEqualToAlertInDatabase) {
                        DatabaseObjectValidation databaseObjectValidation = Alert.isValid(alert);
                        boolean isAlertTemplateIdConflict = Alert.areAlertTemplateIdsInConflict(alert, alertFromDb);
                        
                        if (!isAlertTemplateIdConflict && databaseObjectValidation.isValid()) {
                            if (alertFromDb != null) AlertsDaoWrapper.alterRecordInDatabase(alert, alertFromDb.getName());
                            else AlertsDaoWrapper.alterRecordInDatabase(alert);
                        }
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
        
    }
    
    public static Alert createAlertFromAlertTemplate(AlertTemplate alertTemplate, VariableSet variableSet,
            Map<String, Alert> alerts_ByName,
            Map<String, MetricGroup> metricGroups_ByName,
            Map<String, NotificationGroup> notificationGroups_ByName) {

        if (alertTemplate == null) return null;
        
        try {
            Integer variableSetId = (variableSet != null) ? variableSet.getId() : null;

            String description = Common.getStringWithVariableSubsistution(alertTemplate.getDescriptionVariable(), variableSet);  

            String alertName = Common.getStringWithVariableSubsistution(alertTemplate.getAlertNameVariable(), variableSet);
            Alert alertFromDb = (alerts_ByName != null) ? alerts_ByName.get(alertName) : null;
            Integer alertId = (alertFromDb != null) ? alertFromDb.getId() : null;

            String metricGroupName = Common.getStringWithVariableSubsistution(alertTemplate.getMetricGroupNameVariable(), variableSet);
            MetricGroup metricGroupFromDb = (metricGroups_ByName != null) ? metricGroups_ByName.get(metricGroupName) : null;
            Integer metricGroupId = (metricGroupFromDb != null) ? metricGroupFromDb.getId() : null;

            String cautionNotificationGroupName = Common.getStringWithVariableSubsistution(alertTemplate.getCautionNotificationGroupNameVariable(), variableSet);
            NotificationGroup cautionNotificationGroupFromDb = notificationGroups_ByName.get(cautionNotificationGroupName);
            Integer cautionNotificationGroupId = (cautionNotificationGroupFromDb != null) ? cautionNotificationGroupFromDb.getId() : null;

            String cautionPostiveNotificationGroupName = Common.getStringWithVariableSubsistution(alertTemplate.getCautionPositiveNotificationGroupNameVariable(), variableSet);
            NotificationGroup cautionPostiveNotificationGroupFromDb = notificationGroups_ByName.get(cautionPostiveNotificationGroupName);
            Integer cautionPositiveNotificationGroupId = (cautionPostiveNotificationGroupFromDb != null) ? cautionPostiveNotificationGroupFromDb.getId() : null;

            String dangerNotificationGroupName = Common.getStringWithVariableSubsistution(alertTemplate.getDangerNotificationGroupNameVariable(), variableSet);
            NotificationGroup dangerNotificationGroupFromDb = notificationGroups_ByName.get(dangerNotificationGroupName);
            Integer dangerNotificationGroupId = (dangerNotificationGroupFromDb != null) ? dangerNotificationGroupFromDb.getId() : null;

            String dangerPostiveNotificationGroupName = Common.getStringWithVariableSubsistution(alertTemplate.getDangerPositiveNotificationGroupNameVariable(), variableSet);
            NotificationGroup dangerPostiveNotificationGroupFromDb = notificationGroups_ByName.get(dangerPostiveNotificationGroupName);
            Integer dangerPositiveNotificationGroupId = (dangerPostiveNotificationGroupFromDb != null) ? dangerPostiveNotificationGroupFromDb.getId() : null;

            Alert alert = Alert.createAlertFromAlertTemplate(alertTemplate, variableSetId, description, alertId, alertName, metricGroupId,
                    cautionNotificationGroupId, cautionPositiveNotificationGroupId, dangerNotificationGroupId, dangerPositiveNotificationGroupId);

            return alert;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }

    private void createOrAlterMetricGroupsFromMetricGroupTemplate() {
        
        Connection connection = DatabaseConnections.getConnection();
        List<MetricGroupTemplate> metricGroupTemplates = MetricGroupTemplatesDao.getMetricGroupTemplates(connection, false);
        Map<String,MetricGroup> metricGroups_ByName = MetricGroupsDao.getMetricGroups_ByName(connection, false);
        DatabaseUtils.cleanup(connection);
        
        // if any of these are null, something went wrong querying the db, best to take no action
        if ((metricGroupTemplates == null) || (metricGroups_ByName == null)) { 
            return;
        }
        
        for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
            try {
                if (metricGroupTemplate == null) continue;
                if ((metricGroupTemplate.isMarkedForDelete() == null) || metricGroupTemplate.isMarkedForDelete()) continue;
                
                List<VariableSet> variableSets = Common.getVariableSetsFromVariableSetIdList(metricGroupTemplate.getVariableSetListId());
                if (variableSets == null) continue; // if this is null, something went wrong querying the db, best to take no action
                
                for (VariableSet variableSet : variableSets) {
                    if (variableSet == null) continue;

                    MetricGroup metricGroup = createMetricGroupFromMetricGroupTemplate(metricGroupTemplate, variableSet, metricGroups_ByName);
                    if (metricGroup == null) continue;
                    
                    String metricGroupName = Common.getStringWithVariableSubsistution(metricGroupTemplate.getMetricGroupNameVariable(), variableSet);
                    MetricGroup metricGroupFromDb = metricGroups_ByName.get(metricGroupName);
                    boolean isMetricGroupEqualToMetricGroupInDatabase = metricGroup.isEqual(metricGroupFromDb);

                    if (!isMetricGroupEqualToMetricGroupInDatabase) {
                        DatabaseObjectValidation databaseObjectValidation = MetricGroup.isValid(metricGroup);
                        boolean isMetricGroupTemplateIdConflict = MetricGroup.areMetricGroupTemplateIdsInConflict(metricGroup, metricGroupFromDb);
                        
                        if (!isMetricGroupTemplateIdConflict && databaseObjectValidation.isValid()) {
                            if (metricGroupFromDb != null) MetricGroupsDaoWrapper.alterRecordInDatabase(metricGroup, metricGroupFromDb.getName());
                            else MetricGroupsDaoWrapper.alterRecordInDatabase(metricGroup);
                        }
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
        
    }
    
    public static MetricGroup createMetricGroupFromMetricGroupTemplate(MetricGroupTemplate metricGroupTemplate, VariableSet variableSet, Map<String, MetricGroup> metricGroups_ByName) {

        if (metricGroupTemplate == null) return null;
        
        try {
            Integer variableSetId = (variableSet != null) ? variableSet.getId() : null;

            String metricGroupName = Common.getStringWithVariableSubsistution(metricGroupTemplate.getMetricGroupNameVariable(), variableSet);
            MetricGroup metricGroupFromDb = (metricGroups_ByName != null) ? metricGroups_ByName.get(metricGroupName) : null;
            Integer metricGroupId = (metricGroupFromDb != null) ? metricGroupFromDb.getId() : null;

            String description = Common.getStringWithVariableSubsistution(metricGroupTemplate.getDescriptionVariable(), variableSet);  

            List<String> matchRegexesList = null;
            String matchRegexes = Common.getStringWithVariableSubsistution(metricGroupTemplate.getMatchRegexesVariable(), variableSet);  
            if (matchRegexes != null) matchRegexesList = StringUtilities.getListOfStringsFromDelimitedString(matchRegexes.trim(), '\n');
            TreeSet<String> matchRegexesSortedSet = (matchRegexesList == null) ? null : new TreeSet<>(matchRegexesList);

            List<String> blacklistRegexesList = null;
            String blacklistRegexes = Common.getStringWithVariableSubsistution(metricGroupTemplate.getBlacklistRegexesVariable(), variableSet);  
            if (blacklistRegexes != null) blacklistRegexesList = StringUtilities.getListOfStringsFromDelimitedString(blacklistRegexes.trim(), '\n');
            TreeSet<String> blacklistRegexesSortedSet = (blacklistRegexesList == null) ? null : new TreeSet<>(blacklistRegexesList);

            List<String> tagsList = null;
            String tags = Common.getStringWithVariableSubsistution(metricGroupTemplate.getTagsVariable(), variableSet);  
            if (tags != null) tagsList = StringUtilities.getListOfStringsFromDelimitedString(tags.trim(), '\n');
            TreeSet<String> tagsSortedSet = (tagsList == null) ? null : new TreeSet<>(tagsList);

            MetricGroup metricGroup = MetricGroup.createMetricGroupFromMetricGroupTemplate(metricGroupTemplate, variableSetId, metricGroupId, metricGroupName, 
                    description, matchRegexesSortedSet, blacklistRegexesSortedSet, tagsSortedSet);
            
            return metricGroup;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
}
