package com.pearson.statsagg.alerts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CleanupThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CleanupThread.class.getName());
    
    public static final AtomicBoolean isCleanupThreadCurrentlyRunning = new AtomicBoolean(false);

    protected final Long threadStartTimestampInMilliseconds_;
    protected final String threadId_;

    private final Set<String> immediateCleanupMetricKeys_;
    
    public CleanupThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "C-" + threadStartTimestampInMilliseconds_.toString();
        
        forgetMetrics_PutMetricKeysInSetOfMetricsToForget();
                
        immediateCleanupMetricKeys_ = new HashSet<>(GlobalVariables.immediateCleanupMetrics.keySet());
    }
    
    @Override
    public void run() {
        
        // stops multiple alert threads from running simultaneously 
        if (!isCleanupThreadCurrentlyRunning.compareAndSet(false, true)) {
            logger.warn("ThreadId=" + threadId_ + ", Routine=Cleanup, Message=\"Only 1 cleanup thread can run at a time\"");
            return;
        }
        
        // prevents cleanup from running at the same time as the alert routine
        synchronized (GlobalVariables.alertRoutineLock) {
            String cleanupOutputMessage = "ThreadId=" + threadId_;

            // always remove metric data points 
            AlertsDao alertsDao = new AlertsDao();
            List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
            List<Alert> enabledAlerts = AlertThread.getEnabledAlerts(alerts);
            String cleanupRecentMetricTimestampsAndValuesOutput = cleanupRecentMetricTimestampsAndValues(enabledAlerts);
            cleanupOutputMessage = cleanupOutputMessage + ", " + cleanupRecentMetricTimestampsAndValuesOutput;

            // don't cleanup metric keys when the metric association routine is running
            if (!MetricAssociation.IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.get()) {
                String cleanupMetricsNotRecentlySeenOutput = cleanupMetricsNotRecentlySeen();
                cleanupOutputMessage = cleanupOutputMessage + ", " + cleanupMetricsNotRecentlySeenOutput;
            }
            
            logger.info(cleanupOutputMessage);
        }
        
        isCleanupThreadCurrentlyRunning.set(false);
    }

    private String cleanupMetricsNotRecentlySeen() {
        
        synchronized(GlobalVariables.metricGroupChanges) {
            long cleanupStartTime = System.currentTimeMillis();

            long currentTimeInMilliseconds = System.currentTimeMillis();
            long numberMillisecondsInOneDay = 86400000;
            long metricsRemoved = 0;

            Set<String> metricKeysLastSeenTimestampKeys = GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.keySet();

            for (String metricKey : metricKeysLastSeenTimestampKeys) {
                boolean isImmeadiateCleanup = immediateCleanupMetricKeys_.contains(metricKey); // we should cleanup this metric regardless...
                
                try {
                    if (isImmeadiateCleanup) {
                        synchronized(GlobalVariables.cleanupOldMetricsLock) {
                            removeMetricAssociations(metricKey);
                            metricsRemoved++;
                            GlobalVariables.immediateCleanupMetrics.remove(metricKey);
                            continue;
                        }
                    }
                    
                    Long timestamp = GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.get(metricKey);

                    if (timestamp == null) {
                        synchronized(GlobalVariables.cleanupOldMetricsLock) {
                            removeMetricAssociations(metricKey);
                            metricsRemoved++;
                        }
                    }
                    else {
                        boolean isMetricKeyTrackedByActiveAvailabilityAlert = isMetricKeyTrackedByActiveAvailabilityAlert(metricKey);
                        
                        if (!isMetricKeyTrackedByActiveAvailabilityAlert) { // only cleanup if not tracked by an availability alert
                            long metricNotSeenTimeInMilliseconds = currentTimeInMilliseconds - timestamp;

                            if (metricNotSeenTimeInMilliseconds > numberMillisecondsInOneDay) { // only cleanup if older than 24hrs
                                synchronized(GlobalVariables.cleanupOldMetricsLock) {
                                    removeMetricAssociations(metricKey);
                                    metricsRemoved++;
                                }
                            }
                        }
                    }
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
            
            long cleanupTimeElasped = System.currentTimeMillis() - cleanupStartTime;

            String outputMessage = "CleanupNotRecentlySeenTime=" + cleanupTimeElasped + ", MetricsRemoved=" + metricsRemoved;
            return outputMessage;
        }
    }
    
    private void removeMetricAssociations(String metricKey) {
        
        if (metricKey == null) {
            return;
        }
        
        synchronized(GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup) {
            Set<Integer> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.keySet();

            for (Integer metricGroupId : matchingMetricKeysAssociatedWithMetricGroup) {
                Set<String> matchingMetricKeyAssociationWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);
                matchingMetricKeyAssociationWithMetricGroup.remove(metricKey);
            }
        }
        
        cleanupActiveAvailabilityAlerts(metricKey);
        
        GlobalVariables.statsdGaugeCache.remove(metricKey);
        GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.remove(metricKey);
        GlobalVariables.metricKeysLastSeenTimestamp.remove(metricKey);
        GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.remove(metricKey);
        GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.remove(metricKey);
    }
    
    private void cleanupActiveAvailabilityAlerts(String metricKey) {
        
        if (metricKey == null) {
            return;
        }
        
        // caution + danger
        GlobalVariables.activeAvailabilityAlerts.remove(metricKey);
        
        
        // caution
        List<Integer> alertIdsToRemoveFrom_ActiveCautionAvailabilityAlerts = new ArrayList<>();
        
        for (Integer alertId : GlobalVariables.activeCautionAvailabilityAlerts.keySet()) {
            Set<String> activeCautionAvailabilityMetricKeys = GlobalVariables.activeCautionAvailabilityAlerts.get(alertId);
            
            if (activeCautionAvailabilityMetricKeys != null) {
                activeCautionAvailabilityMetricKeys.remove(metricKey);
                if (activeCautionAvailabilityMetricKeys.isEmpty()) {
                    alertIdsToRemoveFrom_ActiveCautionAvailabilityAlerts.add(alertId);
                }
            }
        }
        
        for (Integer alertId : alertIdsToRemoveFrom_ActiveCautionAvailabilityAlerts) {
            GlobalVariables.activeCautionAvailabilityAlerts.remove(alertId);
        }
        
        
        // danger
        List<Integer> alertIdsToRemoveFrom_ActiveDangerAvailabilityAlerts = new ArrayList<>();
        
        for (Integer alertId : GlobalVariables.activeDangerAvailabilityAlerts.keySet()) {
            Set<String> activeDangerAvailabilityMetricKeys = GlobalVariables.activeDangerAvailabilityAlerts.get(alertId);
            
            if (activeDangerAvailabilityMetricKeys != null) {
                activeDangerAvailabilityMetricKeys.remove(metricKey);
                if (activeDangerAvailabilityMetricKeys.isEmpty()) {
                    alertIdsToRemoveFrom_ActiveDangerAvailabilityAlerts.add(alertId);
                }
            }
        }
        
        for (Integer alertId : alertIdsToRemoveFrom_ActiveDangerAvailabilityAlerts) {
            GlobalVariables.activeDangerAvailabilityAlerts.remove(alertId);
        }
        
    }
    
    private boolean isMetricKeyTrackedByActiveAvailabilityAlert(String metricKey) {
        
        if (metricKey == null) {
            return false;
        }
        
        return (GlobalVariables.activeAvailabilityAlerts != null) && GlobalVariables.activeAvailabilityAlerts.containsKey(metricKey);
    }
    
    private String cleanupRecentMetricTimestampsAndValues(List<Alert> alerts) {

        long cleanupStartTime = System.currentTimeMillis();
        int numValuesRemoved = 0;
        
        Map<String,Long> longestWindowDurationsForMetricKeys = getLongestWindowDurationsForMetricKeys(alerts);
        Set<String> metricKeys = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.keySet();
        
        try {
            // for every metric key that has had a recent datapoint...
            for (String metricKey : metricKeys) {
                boolean isImmeadiateCleanup = immediateCleanupMetricKeys_.contains(metricKey); // we should cleanup this metric regardless...
                
                // lookup the longest window duration associated with this metric key...
                Long windowDuration = longestWindowDurationsForMetricKeys.get(metricKey);

                Set<MetricTimestampAndValue> recentMetricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);

                // remove data that is outside of the window duration (older than 'now' minus 'duration') for this metric key               
                if ((windowDuration != null) && (recentMetricTimestampsAndValues != null)) { 
                    List<MetricTimestampAndValue> metricTimestampAndValuesToRemove = new ArrayList<>();

                    synchronized (recentMetricTimestampsAndValues) {
                        for (MetricTimestampAndValue metricTimestampAndValue : recentMetricTimestampsAndValues) {
                            Long metricValueTimestamp = metricTimestampAndValue.getTimestamp();
                            long metricValueAge = cleanupStartTime - metricValueTimestamp;

                            if ((metricValueAge > windowDuration) || isImmeadiateCleanup) {
                                metricTimestampAndValuesToRemove.add(metricTimestampAndValue);
                            }
                        }

                        for (MetricTimestampAndValue metricTimestampAndValueToRemove : metricTimestampAndValuesToRemove) {
                            recentMetricTimestampsAndValues.remove(metricTimestampAndValueToRemove);
                            numValuesRemoved++;
                        }
                    }
                }
                // the metric isn't currently associated with an alert, so we can get rid this metric key's data
                else if (recentMetricTimestampsAndValues != null) {
                    synchronized (recentMetricTimestampsAndValues) {
                        numValuesRemoved += recentMetricTimestampsAndValues.size();
                        recentMetricTimestampsAndValues.clear();
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("ThreadId=" + threadId_ + ", Routine=CleanupRecentMetricTimestampsAndValues, " 
                    + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        long cleanupTimeElasped = System.currentTimeMillis() - cleanupStartTime;

        String outputMessage = "CleanupRecentMetricTimestampsAndValuesTime=" + cleanupTimeElasped
                + ", NumRecentMetricKeys=" + metricKeys.size()
                + ", NumRecentMetricValuesRemoved=" + numValuesRemoved;

        return outputMessage;
    }
    
    /* Checks the caution & danger durations of all specified alerts to get the longest window duration.
       The window durations of disabled alerts are not used. 
    */
    private Map<String,Long> getLongestWindowDurationsForMetricKeys(List<Alert> alerts) {
        
        if (alerts == null) {
            return new HashMap<>();
        }
        
        Map<String,Long> longestWindowDurationsForMetricKeys = new HashMap<>();
                
        for (Alert alert : alerts) {
            if ((alert.isEnabled() != null) && alert.isEnabled()) {
                List<String> metricKeysAssociatedWithAlert = MetricAssociation.getMetricKeysAssociatedWithAlert(alert);

                for (String metricKey : metricKeysAssociatedWithAlert) {
                    if (GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.containsKey(metricKey)) {
                        updateLongestWindowDurationsForMetricKeys(alert, metricKey, longestWindowDurationsForMetricKeys);
                    }
                }
            }
        }
        
        return longestWindowDurationsForMetricKeys;
    }

    private void updateLongestWindowDurationsForMetricKeys(Alert alert, String metricKey, Map<String,Long> longestWindowDurationsForMetricKeys) {
        
        if ((longestWindowDurationsForMetricKeys == null) || (alert == null) || (metricKey == null)) {
            return;
        }
        
        Long alertLongestWindowDuration = alert.getLongestWindowDuration();
        
        if (!longestWindowDurationsForMetricKeys.containsKey(metricKey) && (alertLongestWindowDuration != null)) {
            longestWindowDurationsForMetricKeys.put(metricKey, alertLongestWindowDuration);
        }
        else {
            Long currentLongestWindowDurationForMetricKey = longestWindowDurationsForMetricKeys.get(metricKey);
            
            if ((alertLongestWindowDuration != null) && (currentLongestWindowDurationForMetricKey != null) 
                    && (alertLongestWindowDuration > currentLongestWindowDurationForMetricKey)) {
                longestWindowDurationsForMetricKeys.put(metricKey, alertLongestWindowDuration);
            }
        }
        
    }

    private void forgetMetrics_PutMetricKeysInSetOfMetricsToForget() {

        HashSet<String> metricKeysToForget = new HashSet<>();
        
        // gets a list of complete metric keys to forget
        if (GlobalVariables.forgetMetrics != null) {         
            Set<String> forgetMetrics = new HashSet<>(GlobalVariables.forgetMetrics.keySet());
            
            for (String metricKey : forgetMetrics) {
                metricKeysToForget.add(metricKey);
                GlobalVariables.forgetMetrics.remove(metricKey);
            }
        }
        
        // gets a list of metric keys to forget by matching against regexs
        if (GlobalVariables.forgetMetricsRegexs != null) {      
            Set<String> forgetMetricsRegexs = new HashSet<>(GlobalVariables.forgetMetricsRegexs.keySet());
            
            for (String metricKeyRegex : forgetMetricsRegexs) {
                Set<String> regexMetricKeysToForget = forgetMetrics_IdentifyMetricKeysViaRegex(metricKeyRegex);
                
                if (regexMetricKeysToForget != null) {
                    metricKeysToForget.addAll(regexMetricKeysToForget);
                }
                
                GlobalVariables.forgetMetricsRegexs.remove(metricKeyRegex);
            }
        }
        
        // tells statsagg to 'forget' these metrics
        for (String metricKeyToForget : metricKeysToForget) {
            GlobalVariables.immediateCleanupMetrics.put(metricKeyToForget, metricKeyToForget);
        }
    }
    
    private Set<String> forgetMetrics_IdentifyMetricKeysViaRegex(String regex) {
        
        if ((regex == null) || regex.isEmpty() || (GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend == null)) {
            return new HashSet<>();
        }
             
        Set<String> metricKeysToForget = new HashSet<>();
        
        Pattern pattern = null;

        try {
            pattern = Pattern.compile(regex);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
       
        if (pattern != null) {
            for (String metricKey : GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.keySet()) {
                Matcher matcher = pattern.matcher(metricKey);

                if (matcher.matches()) {
                    metricKeysToForget.add(metricKey);
                }
            }
        }
         
        return metricKeysToForget;
    }

}
