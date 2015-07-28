package com.pearson.statsagg.alerts;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.general_purpose.GeneralPurposeDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.Threads;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertSuspensions {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensions.class.getName());

    public static final int ALERT_NOT_SUSPENDED = 997;
    public static final int SUSPEND_ALERT_NOTIFICATION_ONLY = 998;
    public static final int SUSPEND_ENTIRE_ALERT = 999;
    
    private final Map<Integer,Alert> alertsByAlertId_;
    
    private final Map<Integer, Boolean> areAlertSuspensionsActive_ = new HashMap<>();
    private final Map<Integer, Set<Integer>> alertSuspensionIdAssociationsByAlertId_ = new HashMap<>();
    private final Map<Integer, Boolean> alertSuspensionStatusByAlertId_ = new HashMap<>();
    private final Map<Integer, Integer> alertSuspensionLevelsByAlertId_ = new HashMap<>();
    
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
                metricGroupTagsAssociatedWithAlert.put(alertId, new HashSet<String>());
            }
        }
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        List<AlertSuspension> allAlertSuspensions = alertSuspensionsDao.getAllDatabaseObjectsInTable();
        areAlertSuspensionsActive(allAlertSuspensions);

        for (int alertId : alertsByAlertId.keySet()) {
            Alert alert = alertsByAlertId.get(alertId);
            
            Set<Integer> alertSuspensionIdsAssociatedWithAnAlert = getAlertSuspensionIdsAssociatedWithAnAlert(alert, allAlertSuspensions, metricGroupTagsAssociatedWithAlert);
            alertSuspensionIdAssociationsByAlertId_.put(alertId, alertSuspensionIdsAssociatedWithAnAlert);

            boolean isAlertCurrentlySuspended = isAnyAlertSuspensionCurrentlyActiveForAnAlert(allAlertSuspensions, alertSuspensionIdsAssociatedWithAnAlert, areAlertSuspensionsActive_);
            alertSuspensionStatusByAlertId_.put(alertId, isAlertCurrentlySuspended);
            
            int alertSuspensionLevel = getAlertSuspensionLevel(alert, allAlertSuspensions, alertSuspensionIdsAssociatedWithAnAlert, areAlertSuspensionsActive_);
            alertSuspensionLevelsByAlertId_.put(alertId, alertSuspensionLevel);
        }

    }
    
    private void areAlertSuspensionsActive(List<AlertSuspension> allAlertSuspensions) {
        
        if (areAlertSuspensionsActive_ == null) {
            return;
        }

        for (AlertSuspension alertSuspension : allAlertSuspensions) {
            if (alertSuspension.getId() == null) continue;
            
            boolean isAlertSuspensionCurrentlyActive = false;
                    
            if ((alertSuspension.isEnabled() != null) && alertSuspension.isEnabled()) {
                isAlertSuspensionCurrentlyActive = AlertSuspension.isAlertSuspensionInSuspensionTimeWindow(alertSuspension);
            }
            
            areAlertSuspensionsActive_.put(alertSuspension.getId(), isAlertSuspensionCurrentlyActive);
        }

    }
    
    private void updateAlertSuspensionGlobalVariables() {

        synchronized(GlobalVariables.alertSuspensionStatusByAlertId) {
            GlobalVariables.alertSuspensionStatusByAlertId.clear();
            while (GlobalVariables.alertSuspensionStatusByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.alertSuspensionStatusByAlertId.putAll(alertSuspensionStatusByAlertId_);
            while (GlobalVariables.alertSuspensionStatusByAlertId.size() != alertSuspensionStatusByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.alertSuspensionIdAssociationsByAlertId) {
            GlobalVariables.alertSuspensionIdAssociationsByAlertId.clear();
            while (GlobalVariables.alertSuspensionIdAssociationsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.alertSuspensionIdAssociationsByAlertId.putAll(alertSuspensionIdAssociationsByAlertId_);
            while (GlobalVariables.alertSuspensionIdAssociationsByAlertId.size() != alertSuspensionIdAssociationsByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
        synchronized(GlobalVariables.alertSuspensionLevelsByAlertId) {
            GlobalVariables.alertSuspensionLevelsByAlertId.clear();
            while (GlobalVariables.alertSuspensionLevelsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.alertSuspensionLevelsByAlertId.putAll(alertSuspensionLevelsByAlertId_);
            while (GlobalVariables.alertSuspensionLevelsByAlertId.size() != alertSuspensionLevelsByAlertId_.size()) Threads.sleepMilliseconds(1);
        }
        
    }
    
    public static boolean deleteExpiredAlertSuspensions() {
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        return alertSuspensionsDao.deleteExpired(new Timestamp(System.currentTimeMillis()));
    }
    
    public static Set<Integer> getAlertSuspensionIdsAssociatedWithAnAlert(Alert alert, List<AlertSuspension> alertSuspensions, Map<Integer,Set<String>> metricGroupTagsAssociatedWithAlert) {

        if ((alert == null) || (alert.getId() == null) || (metricGroupTagsAssociatedWithAlert == null)) {
            return new HashSet<>();
        }

        Set<Integer> alertSuspensionIdsAssociatedWithAnAlert = new HashSet<>();
        
        for (AlertSuspension alertSuspension : alertSuspensions) {
            boolean isSuspensionCriteriaMet = false;
            
            if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByAlertName(alert, alertSuspension);
            }
            else if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByMetricGroupTags(metricGroupTagsAssociatedWithAlert.get(alert.getId()), alertSuspension);
            }
            else if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) {
                isSuspensionCriteriaMet = isSuspensionCriteriaMet_SuspendByEverything(metricGroupTagsAssociatedWithAlert.get(alert.getId()), alertSuspension);
            }
            
            if (isSuspensionCriteriaMet) {
                alertSuspensionIdsAssociatedWithAnAlert.add(alertSuspension.getId());
            }
        }

        return alertSuspensionIdsAssociatedWithAnAlert;
    }
    
    public static int getAlertSuspensionLevel(Alert alert, List<AlertSuspension> alertSuspensions, 
            Set<Integer> alertSuspensionIdsAssociatedWithAnAlert, Map<Integer, Boolean> areAlertSuspensionsActive_) {
        
        if ((alert == null) || (alertSuspensions == null) || alertSuspensions.isEmpty() || 
                (alertSuspensionIdsAssociatedWithAnAlert == null) || alertSuspensionIdsAssociatedWithAnAlert.isEmpty() || 
                (areAlertSuspensionsActive_ == null) || areAlertSuspensionsActive_.isEmpty()) {
            return ALERT_NOT_SUSPENDED;
        }
        
        boolean isSuspendEntireAlertDetected = false;
        
        for (AlertSuspension alertSuspension : alertSuspensions) {
            if (alertSuspension.getId() == null) continue;
            if (!areAlertSuspensionsActive_.containsKey(alertSuspension.getId()) || !areAlertSuspensionsActive_.get(alertSuspension.getId())) continue;
            
            if (alertSuspensionIdsAssociatedWithAnAlert.contains(alertSuspension.getId())) {
                if (alertSuspension.isSuspendNotificationOnly()) {
                    return SUSPEND_ALERT_NOTIFICATION_ONLY;
                }
                else {
                    isSuspendEntireAlertDetected = true;
                }
            }
        }
        
        if (isSuspendEntireAlertDetected) return SUSPEND_ENTIRE_ALERT;
        else return ALERT_NOT_SUSPENDED;
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByAlertName(Alert alert, AlertSuspension alertSuspension) {
        
        if ((alert == null) || (alert.getId() == null) || (alertSuspension == null) || (alertSuspension.getAlertId() == null)) {
            return false;
        }
        
        if (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_ALERT_ID) return false;
        
        return Objects.equals(alertSuspension.getAlertId(), alert.getId());
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendedByMetricGroupTags(Alert alert, AlertSuspension alertSuspension) {
        
        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return false;
        }
        
        Set<String> metricGroupTagsAssociatedWithAlert = getMetricGroupTagsAssociatedWithAlert(alert);
        return isSuspensionCriteriaMet_SuspendByMetricGroupTags(metricGroupTagsAssociatedWithAlert, alertSuspension);
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByMetricGroupTags(Set<String> metricGroupTagsAssociatedWithAlert, AlertSuspension alertSuspension) {
        
        if ((alertSuspension == null) || (alertSuspension.getMetricGroupTagsInclusive() == null) ||
                (metricGroupTagsAssociatedWithAlert == null) || metricGroupTagsAssociatedWithAlert.isEmpty()) {
            return false;
        }
        
        if (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) return false;
        
        boolean isAlertSuspended = false;
        
        Set<String> alertSuspensionMetricGroupTags = AlertSuspension.getMetricGroupTagsSetFromNewlineDelimitedString(alertSuspension.getMetricGroupTagsInclusive());
        if ((alertSuspensionMetricGroupTags == null) || alertSuspensionMetricGroupTags.isEmpty()) return false;

        boolean doAllAlertSuspensionTagsMatchAlertMetricGroupTags = true;
        for (String alertSuspensionMetricGroupTag : alertSuspensionMetricGroupTags) {
            if (!metricGroupTagsAssociatedWithAlert.contains(alertSuspensionMetricGroupTag)) {
                doAllAlertSuspensionTagsMatchAlertMetricGroupTags = false;
                break;
            }
        }

        if (doAllAlertSuspensionTagsMatchAlertMetricGroupTags) {
            isAlertSuspended = true;
        }
        
        return isAlertSuspended;  
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendedByEverything(Alert alert, AlertSuspension alertSuspension) {
        
        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return false;
        }
        
        Set<String> metricGroupTagsAssociatedWithAlert = getMetricGroupTagsAssociatedWithAlert(alert);
        return isSuspensionCriteriaMet_SuspendByEverything(metricGroupTagsAssociatedWithAlert, alertSuspension);
    }
    
    public static boolean isSuspensionCriteriaMet_SuspendByEverything(Set<String> metricGroupTagsAssociatedWithAlert, AlertSuspension alertSuspension) {
  
        if ((alertSuspension == null) || (alertSuspension.getMetricGroupTagsExclusive() == null) || (metricGroupTagsAssociatedWithAlert == null)) {
            return false;
        }
        
        if (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_EVERYTHING) return false;
        
        boolean isAlertSuspended = true;
        
        Set<String> alertSuspensionMetricGroupTags = AlertSuspension.getMetricGroupTagsSetFromNewlineDelimitedString(alertSuspension.getMetricGroupTagsExclusive());
        if ((alertSuspensionMetricGroupTags == null) || alertSuspensionMetricGroupTags.isEmpty()) return true;
        
        boolean doesAnyAlertSuspensionTagMatchAnAlertMetricGroupTag = false;
        for (String alertSuspensionMetricGroupTag : alertSuspensionMetricGroupTags) {
            if (metricGroupTagsAssociatedWithAlert.contains(alertSuspensionMetricGroupTag)) {
                doesAnyAlertSuspensionTagMatchAnAlertMetricGroupTag = true;
                break;
            }
        }

        if (doesAnyAlertSuspensionTagMatchAnAlertMetricGroupTag) {
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
    
    public static boolean isAnyAlertSuspensionCurrentlyActiveForAnAlert(List<AlertSuspension> allAlertSuspensions, 
            Set<Integer> alertSuspensionIdsAssociatedWithAnAlert, Map<Integer, Boolean> alertSuspensionStatusByAlertId) {
        
        if ((allAlertSuspensions == null) || allAlertSuspensions.isEmpty() || 
                (alertSuspensionIdsAssociatedWithAnAlert == null) || alertSuspensionIdsAssociatedWithAnAlert.isEmpty() ||
                (alertSuspensionStatusByAlertId == null) || (alertSuspensionStatusByAlertId.isEmpty())) {
            return false;
        }

        for (AlertSuspension alertSuspension : allAlertSuspensions) {
            if (alertSuspension.getId() == null) continue;
            
            if (alertSuspensionIdsAssociatedWithAnAlert.contains(alertSuspension.getId()) && alertSuspensionStatusByAlertId.containsKey(alertSuspension.getId())) {
                boolean isAlertSuspensionCurrentlyActive = alertSuspensionStatusByAlertId.get(alertSuspension.getId());

                if (isAlertSuspensionCurrentlyActive) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Map<Integer, Set<Integer>> getAlertIdAssociationsByAlertSuspensionId(Map<Integer, Set<Integer>> alertSuspensionIdAssociationsByAlertId) {
        
        if ((alertSuspensionIdAssociationsByAlertId == null) || alertSuspensionIdAssociationsByAlertId.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Integer, Set<Integer>> alertIdAssociationsByAlertSuspensionId = new HashMap<>();
        
        for (Integer alertId : alertSuspensionIdAssociationsByAlertId.keySet()) {
            Set<Integer> alertSuspensionIds = alertSuspensionIdAssociationsByAlertId.get(alertId);
            if (alertSuspensionIds == null) continue;
            
            for (Integer alertSuspensionId : alertSuspensionIds) {
                Set<Integer> alertIds = alertIdAssociationsByAlertSuspensionId.get(alertSuspensionId);
                
                if (alertIds == null) {
                    alertIds = new HashSet<>();
                    alertIds.add(alertId);
                    alertIdAssociationsByAlertSuspensionId.put(alertSuspensionId, alertIds);
                }
                else {
                    alertIds.add(alertId);
                }
                
            }
            
        }
        
        return alertIdAssociationsByAlertSuspensionId;
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
