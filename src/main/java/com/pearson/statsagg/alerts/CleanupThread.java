package com.pearson.statsagg.alerts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.gauges.Gauge;
import com.pearson.statsagg.database.gauges.GaugesDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.webui.StatsAggHtmlFramework;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CleanupThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CleanupThread.class.getName());
    
    public static final long NUM_MILLISECONDS_IN_ONE_DAY = 86400000;
    
    public static final AtomicBoolean isCleanupThreadCurrentlyRunning = new AtomicBoolean(false);

    protected final Long threadStartTimestampInMilliseconds_;
    protected final String threadId_;

    private Set<String> immediateCleanupMetricKeys_;
    
    public CleanupThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "C-" + threadStartTimestampInMilliseconds_.toString();
    }
    
    @Override
    public void run() {
        
        // stops multiple alert threads from running simultaneously 
        if (!isCleanupThreadCurrentlyRunning.compareAndSet(false, true)) {
            logger.warn("ThreadId=" + threadId_ + ", Routine=Cleanup, Message=\"Only 1 cleanup thread can run at a time\"");
            return;
        }
        
        long threadStartTime = System.currentTimeMillis();
        
        immediateCleanupMetricKeys_ = new HashSet<>(GlobalVariables.immediateCleanupMetrics.keySet());

        // prevents cleanup from running at the same time as the alert routine
        synchronized (GlobalVariables.alertRoutineLock) {
            String cleanupSubroutineOutputMessages = "";

            // always remove metric data points 
            AlertsDao alertsDao = new AlertsDao();
            List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
            List<Alert> enabledAlerts = AlertThread.getEnabledAlerts(alerts);
            String cleanupRecentMetricTimestampsAndValuesOutput = cleanupRecentMetricTimestampsAndValues(enabledAlerts);
            cleanupSubroutineOutputMessages = cleanupSubroutineOutputMessages + ", " + cleanupRecentMetricTimestampsAndValuesOutput;

            // don't cleanup metric keys when the metric association routine is running
            if (!MetricAssociation.IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.get()) {
                String cleanupMetricsAssociationsOutput = cleanupMetricsAssociations();
                cleanupSubroutineOutputMessages = cleanupSubroutineOutputMessages + ", " + cleanupMetricsAssociationsOutput;
            }
            
            long threadEndTime = System.currentTimeMillis();
            String cleanupSummaryMessage = "ThreadId=" + threadId_ + ", CleanupTotalTime=" + (threadEndTime - threadStartTime) + ", ";
            
            logger.info(cleanupSummaryMessage + cleanupSubroutineOutputMessages);
        }
        
        isCleanupThreadCurrentlyRunning.set(false);
    }

    private String cleanupMetricsAssociations() {
        
        synchronized(GlobalVariables.metricGroupChanges) {
            long cleanupStartTime = System.currentTimeMillis();
            long metricsRemoved = 0;

            long immediateCleanupMetricKeys_MetricsRemoved = cleanupMetricsAssociations_ImmediateCleanupMetricKeys();
            metricsRemoved += immediateCleanupMetricKeys_MetricsRemoved;
            
            long metricsNotRecentlySeen_MetricsRemoved = cleanupMetricsAssociations_MetricsNotRecentlySeen();
            metricsRemoved += metricsNotRecentlySeen_MetricsRemoved;

            long cleanupTimeElasped = System.currentTimeMillis() - cleanupStartTime;

            String outputMessage = "CleanupMetricsAssociationsTime=" + cleanupTimeElasped + ", MetricsRemoved=" + metricsRemoved;
            return outputMessage;
        }
        
    }
    
    private long cleanupMetricsAssociations_ImmediateCleanupMetricKeys() {
        
        if ((immediateCleanupMetricKeys_ == null) || immediateCleanupMetricKeys_.isEmpty()) {
            return 0;
        }
        
        long metricsRemovedCount = 0;
        Set<String> gaugeMetricKeys = new HashSet<>();
        
        for (String metricKey : immediateCleanupMetricKeys_) {    
            if (GlobalVariables.statsdGaugeCache.containsKey(metricKey)) gaugeMetricKeys.add(metricKey);
            removeMetricAssociations(metricKey);
            metricsRemovedCount++;
        }
        
        Set<String> gaugeMetricKeysSuccessfullyCleanedUp = cleanupGauges(gaugeMetricKeys);
        int numGaugesFailedToCleanup = gaugeMetricKeys.size() - gaugeMetricKeysSuccessfullyCleanedUp.size();
        metricsRemovedCount -= numGaugesFailedToCleanup;
        
        for (String metricKey : immediateCleanupMetricKeys_) {
            boolean isMetricKeyGauge = gaugeMetricKeys.contains(metricKey);
            
            if (!isMetricKeyGauge || (isMetricKeyGauge && gaugeMetricKeysSuccessfullyCleanedUp.contains(metricKey))) {
                GlobalVariables.immediateCleanupMetrics.remove(metricKey);
            }
        }
        
        return metricsRemovedCount;
    }
    
    private long cleanupMetricsAssociations_MetricsNotRecentlySeen() {
        
        if (GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend == null) {
            return 0;
        }
        
        long metricsRemovedCount = 0;
        long currentTimeInMilliseconds = System.currentTimeMillis();
        Set<String> gaugeMetricKeys = new HashSet<>();
        
        for (String metricKey : GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.keySet()) {
            Long timestamp = GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.get(metricKey);

            if (timestamp == null) {
                if (GlobalVariables.statsdGaugeCache.containsKey(metricKey)) gaugeMetricKeys.add(metricKey);
                removeMetricAssociations(metricKey);
                metricsRemovedCount++;
            }
            else {
                boolean isMetricKeyTrackedByActiveAvailabilityAlert = isMetricKeyTrackedByActiveAvailabilityAlert(metricKey);

                if (!isMetricKeyTrackedByActiveAvailabilityAlert) { // only cleanup if not tracked by an availability alert
                    long metricNotSeenTimeInMilliseconds = currentTimeInMilliseconds - timestamp;

                    if (metricNotSeenTimeInMilliseconds > NUM_MILLISECONDS_IN_ONE_DAY) { // only cleanup if older than 24hrs
                        if (GlobalVariables.statsdGaugeCache.containsKey(metricKey)) gaugeMetricKeys.add(metricKey);
                        removeMetricAssociations(metricKey);
                        metricsRemovedCount++;
                    }
                }
            }
        }
        
        Set<String> gaugeMetricKeysSuccessfullyCleanedUp = cleanupGauges(gaugeMetricKeys);
        int numGaugesFailedToCleanup = gaugeMetricKeys.size() - gaugeMetricKeysSuccessfullyCleanedUp.size();
        metricsRemovedCount -= numGaugesFailedToCleanup;
        
        return metricsRemovedCount;
    }
    
    private void removeMetricAssociations(String metricKey) {
        
        if (metricKey == null) {
            return;
        }
                
        Set<Integer> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.keySet();

        for (Integer metricGroupId : matchingMetricKeysAssociatedWithMetricGroup) {
            Set<String> matchingMetricKeyAssociationWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);
            if (matchingMetricKeyAssociationWithMetricGroup != null) matchingMetricKeyAssociationWithMetricGroup.remove(metricKey);
        }
        
        cleanupActiveAvailabilityAlerts(metricKey);
        
        // removing values from statsdGaugeCache & remove gauges from the db is handled elsewhere
        
        GlobalVariables.statsdMetricsAggregatedMostRecentValue.remove(metricKey);
        GlobalVariables.graphiteAggregatedMetricsMostRecentValue.remove(metricKey);
        GlobalVariables.graphitePassthroughMetricsMostRecentValue.remove(metricKey);
        GlobalVariables.openTsdbMetricsMostRecentValue.remove(metricKey);
        
        GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.remove(metricKey);
        GlobalVariables.metricKeysLastSeenTimestamp.remove(metricKey);
        GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.remove(metricKey);
        GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.remove(metricKey);
    }
    
    private Set<String> cleanupGauges(Set<String> gaugeMetricKeys) {
        
        if ((gaugeMetricKeys == null) || gaugeMetricKeys.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> gaugeMetricKeys_SuccessfullyDeleted = new HashSet<>();
        boolean didCommitSucceed = false;
        
        try {
            GaugesDao gaugesDao = new GaugesDao(false);
            gaugesDao.getDatabaseInterface().beginTransaction();

            for (String metricKey : gaugeMetricKeys) {
                Gauge gauge = GlobalVariables.statsdGaugeCache.get(metricKey);
                String bucketSha1;
                if ((gauge == null) || (gauge.getBucketSha1() == null)) bucketSha1 = DigestUtils.sha1Hex(metricKey);
                else bucketSha1 = gauge.getBucketSha1();

                boolean deleteSuccess = false;
                if (bucketSha1 != null) deleteSuccess = gaugesDao.delete(bucketSha1);

                if ((bucketSha1 != null) && deleteSuccess) {
                    gaugeMetricKeys_SuccessfullyDeleted.add(metricKey);
                }  
                else {
                    String cleanBucketToForget = StatsAggHtmlFramework.removeNewlinesFromString(metricKey);
                    logger.error("Failed deleting gauge from the database. Gauge=\"" + cleanBucketToForget + "\"");
                }
            }
            
            didCommitSucceed = gaugesDao.getDatabaseInterface().endTransaction(true);
            gaugesDao.close();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        if (didCommitSucceed) {
            for (String metricKey : gaugeMetricKeys_SuccessfullyDeleted) GlobalVariables.statsdGaugeCache.remove(metricKey);
            return gaugeMetricKeys_SuccessfullyDeleted; 
        }
        else {
            logger.error("Failed to commit StatsD deleted Gauges to the database.");
            return new HashSet<>();
        }
        
    }
    
    private void cleanupActiveAvailabilityAlerts(String metricKey) {
        
        if (metricKey == null) {
            return;
        }
        
        // caution + danger
        synchronized(GlobalVariables.activeAvailabilityAlerts) {
            GlobalVariables.activeAvailabilityAlerts.remove(metricKey);
        }
        
        // caution
        synchronized(GlobalVariables.activeCautionAvailabilityAlerts) {
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
        }
        

        // danger
        synchronized(GlobalVariables.activeDangerAvailabilityAlerts) {
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
        
    }
    
    private boolean isMetricKeyTrackedByActiveAvailabilityAlert(String metricKey) {
        
        if (metricKey == null) {
            return false;
        }
        
        boolean isMetricKeyTrackedByActiveAvailabilityAlert;
        
        synchronized(GlobalVariables.activeAvailabilityAlerts) {
            isMetricKeyTrackedByActiveAvailabilityAlert = ((GlobalVariables.activeAvailabilityAlerts != null) && GlobalVariables.activeAvailabilityAlerts.containsKey(metricKey));
        }
        
        return isMetricKeyTrackedByActiveAvailabilityAlert;
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

}
