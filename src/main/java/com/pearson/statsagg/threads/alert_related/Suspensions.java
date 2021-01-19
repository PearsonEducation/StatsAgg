package com.pearson.statsagg.threads.alert_related;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.general_purpose.GeneralPurposeDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import com.pearson.statsagg.utilities.core_utils.Threads;
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
public class Suspensions {
    
    private static final Logger logger = LoggerFactory.getLogger(Suspensions.class.getName());

    public static final int LEVEL_ALERT_NOT_SUSPENDED = 997;
    public static final int LEVEL_SUSPEND_ALERT_NOTIFICATION_ONLY = 998;
    public static final int LEVEL_SUSPEND_ENTIRE_ALERT = 999;
    
    private final Map<Integer,Alert> alertsByAlertId_;
    private final Map<Integer,Set<String>> matchingMetricKeysAssociatedWithSuspension_ = GlobalVariables.matchingMetricKeysAssociatedWithSuspension;
    
    private final Map<Integer, Boolean> areSuspensionsActive_ = new HashMap<>();
    private final Map<Integer, Set<Integer>> suspensionIdAssociationsByAlertId_ = new HashMap<>();
    private final Map<Integer, Boolean> suspensionStatusByAlertId_ = new HashMap<>();
    private final Map<Integer, Integer> suspensionLevelsByAlertId_ = new HashMap<>();
    private final Map<String,String> suspendedMetricKeys_ = new HashMap<>();
    
    public Suspensions() {
        // gets all alerts from the database.
        List<Alert> alerts = AlertsDao.getAlerts(DatabaseConnections.getConnection(), true);
        alertsByAlertId_ = AlertThread.getAlertsByAlertId(alerts);
    }
    
    public Suspensions(Map<Integer,Alert> alertsByAlertId) {
        
        if (alertsByAlertId != null) {
            this.alertsByAlertId_  = alertsByAlertId;
        } 
        else {
            // gets all alerts from the database.
            List<Alert> alerts = AlertsDao.getAlerts(DatabaseConnections.getConnection(), true);
            alertsByAlertId_ = AlertThread.getAlertsByAlertId(alerts);
        }
        
    }
    
    public void runSuspensionRoutine() {
        boolean isSuccessfullyDeleteExpiredSuspensions = deleteExpiredSuspensions();
        if (!isSuccessfullyDeleteExpiredSuspensions) {
            logger.error("Error deleting one-time suspensions.");
        }
        
        determineSuspensions(alertsByAlertId_);
        updateSuspensionGlobalVariables();
    }
    
    public void determineSuspensions(Map<Integer,Alert> alertsByAlertId) {
        
        if (alertsByAlertId == null) {
            return;
        }
        
        Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlert = GeneralPurposeDao.getMetricGroupTagsAssociatedWithAlerts(DatabaseConnections.getConnection(), true);      
        
        for (int alertId : alertsByAlertId.keySet()) {
            if ((metricGroupTagsAssociatedWithAlert != null) && !metricGroupTagsAssociatedWithAlert.containsKey(alertId)) {
                metricGroupTagsAssociatedWithAlert.put(alertId, new HashSet<>());
            }
        }
        
        List<Suspension> allSuspensions = SuspensionsDao.getSuspensions(DatabaseConnections.getConnection(), true);
        updateAreSuspensionsActive(allSuspensions);

        // determine suspensions
        for (Entry<Integer,Alert> alertEntry : alertsByAlertId.entrySet()) {
            int alertId = alertEntry.getKey();
            Alert alert = alertEntry.getValue();

            Set<Integer> suspensionIdsAssociatedWithAnAlert = getSuspensionIdsAssociatedWithAnAlert(alert, allSuspensions, metricGroupTagsAssociatedWithAlert);
            suspensionIdAssociationsByAlertId_.put(alertId, suspensionIdsAssociatedWithAnAlert);

            boolean isAlertCurrentlySuspended = isAnySuspensionCurrentlyActiveForAnAlert(allSuspensions, suspensionIdsAssociatedWithAnAlert, areSuspensionsActive_);
            suspensionStatusByAlertId_.put(alertId, isAlertCurrentlySuspended);
            
            int suspensionLevel = getSuspensionLevel(alert, allSuspensions, suspensionIdsAssociatedWithAnAlert, areSuspensionsActive_);
            suspensionLevelsByAlertId_.put(alertId, suspensionLevel);
        }
        
        // determine metric suspensions
        for (Suspension suspension : allSuspensions) {
            if ((suspension.getId() != null) && (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRICS) &&
                    (suspension.getMetricSuspensionRegexes() != null) && areSuspensionsActive_.containsKey(suspension.getId()) &&
                    areSuspensionsActive_.get(suspension.getId()) && (matchingMetricKeysAssociatedWithSuspension_ != null)) {
                
                Set<String> matchingMetricKeys = matchingMetricKeysAssociatedWithSuspension_.get(suspension.getId());
                
                if (matchingMetricKeys != null) {
                    synchronized(matchingMetricKeys) {
                        for (String metricKey : matchingMetricKeys) suspendedMetricKeys_.put(metricKey, metricKey);
                    }
                }
            }
        }

    }
    
    private void updateAreSuspensionsActive(List<Suspension> allSuspensions) {
        
        if (areSuspensionsActive_ == null) {
            return;
        }

        for (Suspension suspension : allSuspensions) {
            if (suspension.getId() == null) continue;
            
            boolean isSuspensionCurrentlyActive = false;
                    
            if ((suspension.isEnabled() != null) && suspension.isEnabled()) {
                isSuspensionCurrentlyActive = Suspension.isSuspensionInSuspensionTimeWindow(suspension);
            }
            
            areSuspensionsActive_.put(suspension.getId(), isSuspensionCurrentlyActive);
        }

    }
    
    private void updateSuspensionGlobalVariables() {

        synchronized(GlobalVariables.suspensionStatusByAlertId) {
            GlobalVariables.suspensionStatusByAlertId.clear();
            while (GlobalVariables.suspensionStatusByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.suspensionStatusByAlertId.putAll(suspensionStatusByAlertId_);
            while (GlobalVariables.suspensionStatusByAlertId.size() != suspensionStatusByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.suspensionIdAssociationsByAlertId) {
            GlobalVariables.suspensionIdAssociationsByAlertId.clear();
            while (GlobalVariables.suspensionIdAssociationsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.suspensionIdAssociationsByAlertId.putAll(suspensionIdAssociationsByAlertId_);
            while (GlobalVariables.suspensionIdAssociationsByAlertId.size() != suspensionIdAssociationsByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.suspensionLevelsByAlertId) {
            GlobalVariables.suspensionLevelsByAlertId.clear();
            while (GlobalVariables.suspensionLevelsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.suspensionLevelsByAlertId.putAll(suspensionLevelsByAlertId_);
            while (GlobalVariables.suspensionLevelsByAlertId.size() != suspensionLevelsByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.suspendedMetricKeys) {
            GlobalVariables.suspendedMetricKeys.clear();
            while (GlobalVariables.suspendedMetricKeys.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.suspendedMetricKeys.putAll(suspendedMetricKeys_);
            while (GlobalVariables.suspendedMetricKeys.size() != suspendedMetricKeys_.size()) Threads.sleepMilliseconds(1);
        }
        
    }
    
    public static boolean deleteExpiredSuspensions() {
        return SuspensionsDao.deleteExpired(DatabaseConnections.getConnection(), true, true, new Timestamp(System.currentTimeMillis()));
    }
    
    public static Set<Integer> getSuspensionIdsAssociatedWithAnAlert(Alert alert, List<Suspension> suspensions, Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlert) {

        if ((alert == null) || (alert.getId() == null) || (metricGroupTagsAssociatedWithAlert == null)) {
            return new HashSet<>();
        }

        Set<Integer> suspensionIdsAssociatedWithAnAlert = new HashSet<>();
        
        for (Suspension suspension : suspensions) {
            boolean isSuspensionCriteriaMet = false;
            
            if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_ALERT_ID) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByAlertName(alert, suspension);
            }
            else if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByMetricGroupTags(metricGroupTagsAssociatedWithAlert.get(alert.getId()), suspension);
            }
            else if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_EVERYTHING) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByEverything(metricGroupTagsAssociatedWithAlert.get(alert.getId()), suspension);
            }
            
            if (isSuspensionCriteriaMet) {
                suspensionIdsAssociatedWithAnAlert.add(suspension.getId());
            }
        }

        return suspensionIdsAssociatedWithAnAlert;
    }
    
    public static int getSuspensionLevel(Alert alert, List<Suspension> suspensions, 
            Set<Integer> suspensionIdsAssociatedWithAnAlert, Map<Integer, Boolean> areSuspensionsActive) {
        
        if ((alert == null) || (suspensions == null) || suspensions.isEmpty() || 
                (suspensionIdsAssociatedWithAnAlert == null) || suspensionIdsAssociatedWithAnAlert.isEmpty() || 
                (areSuspensionsActive == null) || areSuspensionsActive.isEmpty()) {
            return LEVEL_ALERT_NOT_SUSPENDED;
        }
        
        boolean isSuspendEntireAlertDetected = false;
        
        for (Suspension suspension : suspensions) {
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
    
    public static boolean isSuspensionCriteriaMet_SuspendByAlertName(Alert alert, Suspension suspension) {
        
        if ((alert == null) || (alert.getId() == null) || (suspension == null) || (suspension.getAlertId() == null)) {
            return false;
        }
        
        if (suspension.getSuspendBy() != Suspension.SUSPEND_BY_ALERT_ID) return false;
        
        return Objects.equals(suspension.getAlertId(), alert.getId());
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendedByMetricGroupTags(Alert alert, Suspension suspension) {
        
        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return false;
        }
        
        Set<String> metricGroupTagsAssociatedWithAlert = getMetricGroupTagsAssociatedWithAlert(alert);
        return isSuspensionCriteriaMet_SuspendByMetricGroupTags(metricGroupTagsAssociatedWithAlert, suspension);
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByMetricGroupTags(Set<String> metricGroupTagsAssociatedWithAlert, Suspension suspension) {
        
        if ((suspension == null) || (suspension.getMetricGroupTagsInclusive() == null) ||
                (metricGroupTagsAssociatedWithAlert == null) || metricGroupTagsAssociatedWithAlert.isEmpty()) {
            return false;
        }
        
        if (suspension.getSuspendBy() != Suspension.SUSPEND_BY_METRIC_GROUP_TAGS) return false;
        
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
    
    public static boolean isSuspensionCriteriaMet_SuspendedByEverything(Alert alert, Suspension suspension) {
        
        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return false;
        }
        
        Set<String> metricGroupTagsAssociatedWithAlert = getMetricGroupTagsAssociatedWithAlert(alert);
        return isSuspensionCriteriaMet_SuspendByEverything(metricGroupTagsAssociatedWithAlert, suspension);
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByEverything(Set<String> metricGroupTagsAssociatedWithAlert, Suspension suspension) {
  
        if ((suspension == null) || (suspension.getMetricGroupTagsExclusive() == null) || (metricGroupTagsAssociatedWithAlert == null)) {
            return false;
        }
        
        if (suspension.getSuspendBy() != Suspension.SUSPEND_BY_EVERYTHING) return false;
        
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
        List<MetricGroupTag> metricGroupTags = MetricGroupTagsDao.getMetricGroupTagsByMetricGroupId(DatabaseConnections.getConnection(), true, alert.getMetricGroupId());

        if (metricGroupTags != null) {
            for (MetricGroupTag metricGroupTag : metricGroupTags) {
                metricGroupTagsSet.add(metricGroupTag.getTag());
            }
        }

        return metricGroupTagsSet;
    }
    
    public static boolean isAnySuspensionCurrentlyActiveForAnAlert(List<Suspension> allSuspensions, 
            Set<Integer> suspensionIdsAssociatedWithAnAlert, Map<Integer, Boolean> suspensionStatusByAlertId) {
        
        if ((allSuspensions == null) || allSuspensions.isEmpty() || 
                (suspensionIdsAssociatedWithAnAlert == null) || suspensionIdsAssociatedWithAnAlert.isEmpty() ||
                (suspensionStatusByAlertId == null) || (suspensionStatusByAlertId.isEmpty())) {
            return false;
        }

        for (Suspension suspension : allSuspensions) {
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

    public Map<Integer, Boolean> getSuspensionStatusByAlertId() {
        return suspensionStatusByAlertId_;
    }

    public Map<Integer, Set<Integer>> getSuspensionIdAssociationsByAlertId() {
        return suspensionIdAssociationsByAlertId_;
    }

    public Map<Integer, Integer> getSuspensionLevelsByAlertId() {
        return suspensionLevelsByAlertId_;
    }
    
}
