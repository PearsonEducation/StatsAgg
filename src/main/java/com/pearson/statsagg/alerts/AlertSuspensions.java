package com.pearson.statsagg.alerts;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.general_purpose.GeneralPurposeDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StringUtilities;
import com.pearson.statsagg.utilities.Threads;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertSuspensions {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensions.class.getName());

    public static final int LEVEL_ALERT_NOT_SUSPENDED = 997;
    public static final int LEVEL_SUSPEND_ALERT_NOTIFICATION_ONLY = 998;
    public static final int LEVEL_SUSPEND_ENTIRE_ALERT = 999;
    
    private final Map<Integer,Alert> alertsByAlertId_;
    private final Map<Integer,Set<String>> matchingMetricKeysAssociatedWithSuspension_ = GlobalVariables.matchingMetricKeysAssociatedWithSuspension;
    
    private final Map<Integer, Boolean> areSuspensionsActive_ = new HashMap<>();
    private final Map<Integer, Set<Integer>> alertSuspensionIdAssociationsByAlertId_ = new HashMap<>();
    private final Map<Integer, Boolean> alertSuspensionStatusByAlertId_ = new HashMap<>();
    private final Map<Integer, Integer> alertSuspensionLevelsByAlertId_ = new HashMap<>();
    private final Map<String,String> suspendedMetricKeys_ = new HashMap<>();
    
    public AlertSuspensions() {
        // gets all alerts from the database.
        AlertsDao alertsDao = new AlertsDao();
        List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
        alertsByAlertId_ = AlertThread.getAlertsByAlertId(alerts);
    }
    
    public AlertSuspensions(Map<Integer,Alert> alertsByAlertId) {
        
        if (alertsByAlertId != null) {
            this.alertsByAlertId_  = alertsByAlertId;
        } 
        else {
            // gets all alerts from the database.
            AlertsDao alertsDao = new AlertsDao();
            List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
            alertsByAlertId_ = AlertThread.getAlertsByAlertId(alerts);
        }
        
    }
    
    public void runAlertSuspensionRoutine() {
        boolean isSuccessfullyDeleteExpiredAlertSuspensions = deleteExpiredAlertSuspensions();
        if (!isSuccessfullyDeleteExpiredAlertSuspensions) {
            logger.error("Error deleting one-time alert suspensions.");
        }
        
        determineAlertSuspensions(alertsByAlertId_);
        updateAlertSuspensionGlobalVariables();
    }
    
    public void determineAlertSuspensions(Map<Integer,Alert> alertsByAlertId) {
        
        if (alertsByAlertId == null) {
            return;
        }
        
        GeneralPurposeDao generalPurposeDao = new GeneralPurposeDao();
        Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlert = generalPurposeDao.getMetricGroupTagsAssociatedWithAlerts();      
        
        for (int alertId : alertsByAlertId.keySet()) {
            if ((metricGroupTagsAssociatedWithAlert != null) && !metricGroupTagsAssociatedWithAlert.containsKey(alertId)) {
                metricGroupTagsAssociatedWithAlert.put(alertId, new HashSet<>());
            }
        }
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        List<AlertSuspension> allSuspensions = alertSuspensionsDao.getAllDatabaseObjectsInTable();
        areAlertSuspensionsActive(allSuspensions);

        // determine alert suspensions
        for (Entry<Integer,Alert> alertEntry : alertsByAlertId.entrySet()) {
            int alertId = alertEntry.getKey();
            Alert alert = alertEntry.getValue();

            Set<Integer> suspensionIdsAssociatedWithAnAlert = getSuspensionIdsAssociatedWithAnAlert(alert, allSuspensions, metricGroupTagsAssociatedWithAlert);
            alertSuspensionIdAssociationsByAlertId_.put(alertId, suspensionIdsAssociatedWithAnAlert);

            boolean isAlertCurrentlySuspended = isAnyAlertSuspensionCurrentlyActiveForAnAlert(allSuspensions, suspensionIdsAssociatedWithAnAlert, areSuspensionsActive_);
            alertSuspensionStatusByAlertId_.put(alertId, isAlertCurrentlySuspended);
            
            int alertSuspensionLevel = getSuspensionLevel(alert, allSuspensions, suspensionIdsAssociatedWithAnAlert, areSuspensionsActive_);
            alertSuspensionLevelsByAlertId_.put(alertId, alertSuspensionLevel);
        }
        
        // determine metric suspensions
        for (AlertSuspension suspension : allSuspensions) {
            if ((suspension.getId() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRICS) &&
                    (suspension.getMetricSuspensionRegexes() != null) && areSuspensionsActive_.containsKey(suspension.getId()) &&
                    areSuspensionsActive_.get(suspension.getId())) {
                
                Set<String> matchingMetricKeys = matchingMetricKeysAssociatedWithSuspension_.get(suspension.getId());
                
                synchronized(matchingMetricKeys) {
                    for (String metricKey : matchingMetricKeys) suspendedMetricKeys_.put(metricKey, metricKey);
                }
            }
        }

    }
    
    private void areAlertSuspensionsActive(List<AlertSuspension> allSuspensions) {
        
        if (areSuspensionsActive_ == null) {
            return;
        }

        for (AlertSuspension suspension : allSuspensions) {
            if (suspension.getId() == null) continue;
            
            boolean isSuspensionCurrentlyActive = false;
                    
            if ((suspension.isEnabled() != null) && suspension.isEnabled()) {
                isSuspensionCurrentlyActive = AlertSuspension.isSuspensionInSuspensionTimeWindow(suspension);
            }
            
            areSuspensionsActive_.put(suspension.getId(), isSuspensionCurrentlyActive);
        }

    }
    
    private void updateAlertSuspensionGlobalVariables() {

        synchronized(GlobalVariables.alertSuspensionStatusByAlertId) {
            GlobalVariables.alertSuspensionStatusByAlertId.clear();
            while (GlobalVariables.alertSuspensionStatusByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.alertSuspensionStatusByAlertId.putAll(alertSuspensionStatusByAlertId_);
            while (GlobalVariables.alertSuspensionStatusByAlertId.size() != alertSuspensionStatusByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.suspensionIdAssociationsByAlertId) {
            GlobalVariables.suspensionIdAssociationsByAlertId.clear();
            while (GlobalVariables.suspensionIdAssociationsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.suspensionIdAssociationsByAlertId.putAll(alertSuspensionIdAssociationsByAlertId_);
            while (GlobalVariables.suspensionIdAssociationsByAlertId.size() != alertSuspensionIdAssociationsByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.alertSuspensionLevelsByAlertId) {
            GlobalVariables.alertSuspensionLevelsByAlertId.clear();
            while (GlobalVariables.alertSuspensionLevelsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.alertSuspensionLevelsByAlertId.putAll(alertSuspensionLevelsByAlertId_);
            while (GlobalVariables.alertSuspensionLevelsByAlertId.size() != alertSuspensionLevelsByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.suspendedMetricKeys) {
            GlobalVariables.suspendedMetricKeys.clear();
            while (GlobalVariables.suspendedMetricKeys.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.suspendedMetricKeys.putAll(suspendedMetricKeys_);
            while (GlobalVariables.suspendedMetricKeys.size() != suspendedMetricKeys_.size()) Threads.sleepMilliseconds(1);
        }
        
    }
    
    public static boolean deleteExpiredAlertSuspensions() {
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        return alertSuspensionsDao.deleteExpired(new Timestamp(System.currentTimeMillis()));
    }
    
    public static Set<Integer> getSuspensionIdsAssociatedWithAnAlert(Alert alert, List<AlertSuspension> alertSuspensions, Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlert) {

        if ((alert == null) || (alert.getId() == null) || (metricGroupTagsAssociatedWithAlert == null)) {
            return new HashSet<>();
        }

        Set<Integer> suspensionIdsAssociatedWithAnAlert = new HashSet<>();
        
        for (AlertSuspension suspension : alertSuspensions) {
            boolean isSuspensionCriteriaMet = false;
            
            if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByAlertName(alert, suspension);
            }
            else if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByMetricGroupTags(metricGroupTagsAssociatedWithAlert.get(alert.getId()), suspension);
            }
            else if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByEverything(metricGroupTagsAssociatedWithAlert.get(alert.getId()), suspension);
            }
            
            if (isSuspensionCriteriaMet) {
                suspensionIdsAssociatedWithAnAlert.add(suspension.getId());
            }
        }

        return suspensionIdsAssociatedWithAnAlert;
    }
    
    public static int getSuspensionLevel(Alert alert, List<AlertSuspension> suspensions, 
            Set<Integer> suspensionIdsAssociatedWithAnAlert, Map<Integer, Boolean> areSuspensionsActive) {
        
        if ((alert == null) || (suspensions == null) || suspensions.isEmpty() || 
                (suspensionIdsAssociatedWithAnAlert == null) || suspensionIdsAssociatedWithAnAlert.isEmpty() || 
                (areSuspensionsActive == null) || areSuspensionsActive.isEmpty()) {
            return LEVEL_ALERT_NOT_SUSPENDED;
        }
        
        boolean isSuspendEntireAlertDetected = false;
        
        for (AlertSuspension suspension : suspensions) {
            if (suspension.getId() == null) continue;
            if (!areSuspensionsActive.containsKey(suspension.getId()) || !areSuspensionsActive.get(suspension.getId())) continue;
            
            if (suspensionIdsAssociatedWithAnAlert.contains(suspension.getId())) {
                if (suspension.isSuspendNotificationOnly()) {
                    return LEVEL_SUSPEND_ALERT_NOTIFICATION_ONLY;
                }
                else {
                    isSuspendEntireAlertDetected = true;
                }
            }
        }
        
        if (isSuspendEntireAlertDetected) return LEVEL_SUSPEND_ENTIRE_ALERT;
        else return LEVEL_ALERT_NOT_SUSPENDED;
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByAlertName(Alert alert, AlertSuspension suspension) {
        
        if ((alert == null) || (alert.getId() == null) || (suspension == null) || (suspension.getAlertId() == null)) {
            return false;
        }
        
        if (suspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_ALERT_ID) return false;
        
        return Objects.equals(suspension.getAlertId(), alert.getId());
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendedByMetricGroupTags(Alert alert, AlertSuspension suspension) {
        
        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return false;
        }
        
        Set<String> metricGroupTagsAssociatedWithAlert = getMetricGroupTagsAssociatedWithAlert(alert);
        return isSuspensionCriteriaMet_SuspendByMetricGroupTags(metricGroupTagsAssociatedWithAlert, suspension);
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByMetricGroupTags(Set<String> metricGroupTagsAssociatedWithAlert, AlertSuspension suspension) {
        
        if ((suspension == null) || (suspension.getMetricGroupTagsInclusive() == null) ||
                (metricGroupTagsAssociatedWithAlert == null) || metricGroupTagsAssociatedWithAlert.isEmpty()) {
            return false;
        }
        
        if (suspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) return false;
        
        boolean isAlertSuspended = false;
        
        Set<String> suspensionMetricGroupTags = StringUtilities.getSetOfStringsFromDelimitedString(suspension.getMetricGroupTagsInclusive(), '\n');
        if ((suspensionMetricGroupTags == null) || suspensionMetricGroupTags.isEmpty()) return false;

        boolean doAllSuspensionTagsMatchAlertMetricGroupTags = true;
        for (String SuspensionMetricGroupTag : suspensionMetricGroupTags) {
            if (!metricGroupTagsAssociatedWithAlert.contains(SuspensionMetricGroupTag)) {
                doAllSuspensionTagsMatchAlertMetricGroupTags = false;
                break;
            }
        }

        if (doAllSuspensionTagsMatchAlertMetricGroupTags) {
            isAlertSuspended = true;
        }
        
        return isAlertSuspended;  
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendedByEverything(Alert alert, AlertSuspension suspension) {
        
        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return false;
        }
        
        Set<String> metricGroupTagsAssociatedWithAlert = getMetricGroupTagsAssociatedWithAlert(alert);
        return isSuspensionCriteriaMet_SuspendByEverything(metricGroupTagsAssociatedWithAlert, suspension);
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByEverything(Set<String> metricGroupTagsAssociatedWithAlert, AlertSuspension suspension) {
  
        if ((suspension == null) || (suspension.getMetricGroupTagsExclusive() == null) || (metricGroupTagsAssociatedWithAlert == null)) {
            return false;
        }
        
        if (suspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_EVERYTHING) return false;
        
        boolean isAlertSuspended = true;
        
        Set<String> suspensionMetricGroupTags = StringUtilities.getSetOfStringsFromDelimitedString(suspension.getMetricGroupTagsExclusive(), '\n');
        if ((suspensionMetricGroupTags == null) || suspensionMetricGroupTags.isEmpty()) return true;
        
        boolean doesAnySuspensionTagMatchAnAlertMetricGroupTag = false;
        for (String suspensionMetricGroupTag : suspensionMetricGroupTags) {
            if (metricGroupTagsAssociatedWithAlert.contains(suspensionMetricGroupTag)) {
                doesAnySuspensionTagMatchAnAlertMetricGroupTag = true;
                break;
            }
        }

        if (doesAnySuspensionTagMatchAnAlertMetricGroupTag) {
            isAlertSuspended = false;
        }
        
        return isAlertSuspended;  
    }

    public static Set<String> getMetricGroupTagsAssociatedWithAlert(Alert alert) {
        
        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return new HashSet<>();
        }
         
        Set<String> metricGroupTagsSet = new HashSet<>();

        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
        List<MetricGroupTag> metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(alert.getMetricGroupId());

        if (metricGroupTags != null) {
            for (MetricGroupTag metricGroupTag : metricGroupTags) {
                metricGroupTagsSet.add(metricGroupTag.getTag());
            }
        }

        return metricGroupTagsSet;
    }
    
    public static boolean isAnyAlertSuspensionCurrentlyActiveForAnAlert(List<AlertSuspension> allSuspensions, 
            Set<Integer> suspensionIdsAssociatedWithAnAlert, Map<Integer, Boolean> suspensionStatusByAlertId) {
        
        if ((allSuspensions == null) || allSuspensions.isEmpty() || 
                (suspensionIdsAssociatedWithAnAlert == null) || suspensionIdsAssociatedWithAnAlert.isEmpty() ||
                (suspensionStatusByAlertId == null) || (suspensionStatusByAlertId.isEmpty())) {
            return false;
        }

        for (AlertSuspension suspension : allSuspensions) {
            if (suspension.getId() == null) continue;
            
            if (suspensionIdsAssociatedWithAnAlert.contains(suspension.getId()) && suspensionStatusByAlertId.containsKey(suspension.getId())) {
                boolean isSuspensionCurrentlyActive = suspensionStatusByAlertId.get(suspension.getId());

                if (isSuspensionCurrentlyActive) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Map<Integer, Set<Integer>> getAlertIdAssociationsBySuspensionId(Map<Integer, Set<Integer>> suspensionIdAssociationsByAlertId) {
        
        if ((suspensionIdAssociationsByAlertId == null) || suspensionIdAssociationsByAlertId.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Integer, Set<Integer>> alertIdAssociationsBySuspensionId = new HashMap<>();
        
        for (Integer alertId : suspensionIdAssociationsByAlertId.keySet()) {
            Set<Integer> suspensionIds = suspensionIdAssociationsByAlertId.get(alertId);
            if (suspensionIds == null) continue;
            
            for (Integer suspensionId : suspensionIds) {
                Set<Integer> alertIds = alertIdAssociationsBySuspensionId.get(suspensionId);
                
                if (alertIds == null) {
                    alertIds = new HashSet<>();
                    alertIds.add(alertId);
                    alertIdAssociationsBySuspensionId.put(suspensionId, alertIds);
                }
                else {
                    alertIds.add(alertId);
                }
                
            }
            
        }
        
        return alertIdAssociationsBySuspensionId;
    }

    public Map<Integer,Alert> getAlertsByAlertId() {
        return alertsByAlertId_;
    }

    public Map<Integer, Boolean> getAlertSuspensionStatusByAlertId() {
        return alertSuspensionStatusByAlertId_;
    }

    public Map<Integer, Set<Integer>> getAlertSuspensionIdAssociationsByAlertId() {
        return alertSuspensionIdAssociationsByAlertId_;
    }

    public Map<Integer, Integer> getAlertSuspensionLevelsByAlertId() {
        return alertSuspensionLevelsByAlertId_;
    }
    
}
