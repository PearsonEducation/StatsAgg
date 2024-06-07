package com.pearson.statsagg.threads.internal_housekeeping;

import com.pearson.statsagg.threads.alert_related.AlertThread;
import com.pearson.statsagg.threads.alert_related.MetricAssociation;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.gauges.Gauge;
import com.pearson.statsagg.database_objects.gauges.GaugesDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricKeyLastSeen;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
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
    
    protected final ThreadPoolExecutor threadPoolExecutor_;
    protected final Long threadStartTimestampInMilliseconds_;
    protected final String threadId_;

    private Set<String> immediateCleanupMetricKeys_;
    
    public CleanupThread(Long threadStartTimestampInMilliseconds, ThreadPoolExecutor threadPoolExecutor) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "C-" + threadStartTimestampInMilliseconds_.toString();
        this.threadPoolExecutor_ = threadPoolExecutor;
    }
    
    @Override
    public void run() {
        
        try {
            // stops multiple alert threads from running simultaneously 
            if (!isCleanupThreadCurrentlyRunning.compareAndSet(false, true)) {
                if ((threadPoolExecutor_ != null) && (threadPoolExecutor_.getActiveCount() <= 1)) {
                    logger.warn("Invalid clean-thread state detected (detected that statsagg thinks another cleanup-thread is running, but it is not.");
                    isCleanupThreadCurrentlyRunning.set(false);
                }
                else {
                    logger.warn("ThreadId=" + threadId_ + ", Routine=Cleanup, Message=\"Only 1 cleanup thread can run at a time\"");
                    return;
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        long threadStartTime = System.currentTimeMillis();
        immediateCleanupMetricKeys_ = new HashSet<>(GlobalVariables.immediateCleanupMetrics.keySet());

        // prevents cleanup from running at the same time as the alert routine
        synchronized (GlobalVariables.alertRoutineLock) {
            try {
                // always remove metric data points 
                List<Alert> alerts = AlertsDao.getAlerts(DatabaseConnections.getConnection(), true);
                List<Alert> enabledAlerts = AlertThread.getEnabledAlerts(alerts);
                String cleanupRecentMetricTimestampsAndValuesOutput = cleanupRecentMetricTimestampsAndValues(enabledAlerts);
                String cleanupSubroutineOutputMessages = cleanupRecentMetricTimestampsAndValuesOutput;

                // don't cleanup metric keys when the metric association routine is running
                if (!MetricAssociation.IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.get() && !MetricAssociation.IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.get()) {
                    String cleanupMetricsAssociationsOutput = cleanupMetricsAssociations();
                    cleanupSubroutineOutputMessages = cleanupSubroutineOutputMessages + ", " + cleanupMetricsAssociationsOutput;
                }

                long threadEndTime = System.currentTimeMillis();
                String cleanupSummaryMessage = "ThreadId=" + threadId_ + ", CleanupTotalTime=" + (threadEndTime - threadStartTime) + ", ";

                logger.info(cleanupSummaryMessage + cleanupSubroutineOutputMessages);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        isCleanupThreadCurrentlyRunning.set(false);
    }

    private String cleanupMetricsAssociations() {
        
        synchronized(GlobalVariables.metricGroupChanges) {
            synchronized(GlobalVariables.suspensionChanges) {
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
        
        if (GlobalVariables.metricKeysLastSeenTimestamp == null) {
            return 0;
        }
        
        long metricsRemovedCount = 0;
        long currentTimeInMilliseconds = System.currentTimeMillis();
        Set<String> gaugeMetricKeys = new HashSet<>();
        
        for (Entry<String,MetricKeyLastSeen> metricKeysLastSeenTimestamp_Entry : GlobalVariables.metricKeysLastSeenTimestamp.entrySet()) {
            String metricKey = metricKeysLastSeenTimestamp_Entry.getKey();
            MetricKeyLastSeen metricKeyLastSeenTimestamp = metricKeysLastSeenTimestamp_Entry.getValue();
            Long metricKeyLastSeenTimestamp_UpdateOnResend = metricKeyLastSeenTimestamp.getMetricKeyLastSeenTimestamp_UpdateOnResend();
            
            if (metricKeyLastSeenTimestamp_UpdateOnResend == null) {
                if (GlobalVariables.statsdGaugeCache.containsKey(metricKey)) gaugeMetricKeys.add(metricKey);
                removeMetricAssociations(metricKey);
                metricsRemovedCount++;
            }
            else {
                boolean isMetricKeyTrackedByActiveAvailabilityAlert = isMetricKeyTrackedByActiveAvailabilityAlert(metricKey);

                if (!isMetricKeyTrackedByActiveAvailabilityAlert) { // only cleanup if not tracked by an availability alert
                    long metricNotSeenTimeInMilliseconds = currentTimeInMilliseconds - metricKeyLastSeenTimestamp_UpdateOnResend;

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

        for (Set<String> matchingMetricKeyAssociationWithMetricGroup : GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.values()) {
            if (matchingMetricKeyAssociationWithMetricGroup != null) matchingMetricKeyAssociationWithMetricGroup.remove(metricKey);
        }
        
        for (Set<String> matchingMetricKeyAssociationWithMetricGroup : GlobalVariables.matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup.values()) {
            if (matchingMetricKeyAssociationWithMetricGroup != null) matchingMetricKeyAssociationWithMetricGroup.remove(metricKey);
        }
        
        for (Set<String> matchingMetricKeyAssociationWithSuspension : GlobalVariables.matchingMetricKeysAssociatedWithSuspension.values()) {
            if (matchingMetricKeyAssociationWithSuspension != null) matchingMetricKeyAssociationWithSuspension.remove(metricKey);
        }
        
        cleanupActiveAvailabilityAlerts(metricKey);
        
        // removing values from statsdGaugeCache & remove gauges from the db is handled in the 'cleanupGauges' method
        
        GlobalVariables.statsdMetricsAggregatedMostRecentValue.remove(metricKey);
        GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.remove(metricKey);
        GlobalVariables.metricKeysAssociatedWithOutputBlacklistMetricGroup.remove(metricKey);
        GlobalVariables.metricKeysAssociatedWithAnySuspension.remove(metricKey);
        GlobalVariables.metricKeysLastSeenTimestamp.remove(metricKey);
        GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.remove(metricKey);
    }
    
    private Set<String> cleanupGauges(Set<String> gaugeMetricKeys) {
        
        if ((gaugeMetricKeys == null) || gaugeMetricKeys.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> gaugeMetricKeys_SuccessfullyDeleted = new HashSet<>();
        boolean didCommitSucceed = false;
        
        try {
            Connection connection = DatabaseConnections.getConnection();
            DatabaseUtils.setAutoCommit(connection, false);

            for (String metricKey : gaugeMetricKeys) {
                Gauge gauge = GlobalVariables.statsdGaugeCache.get(metricKey);
                String bucketSha1;
                if ((gauge == null) || (gauge.getBucketSha1() == null)) bucketSha1 = DigestUtils.sha1Hex(metricKey);
                else bucketSha1 = gauge.getBucketSha1();

                boolean deleteSuccess = false;
                if (bucketSha1 != null) deleteSuccess = GaugesDao.delete(connection, false, false, bucketSha1);

                if ((bucketSha1 != null) && deleteSuccess) {
                    gaugeMetricKeys_SuccessfullyDeleted.add(metricKey);
                }  
                else {
                    String cleanBucketToForget = StringUtilities.removeNewlinesFromString(metricKey);
                    logger.error("Failed deleting gauge from the database. Gauge=\"" + cleanBucketToForget + "\"");
                }
            }
            
            didCommitSucceed = DatabaseUtils.commit(connection);
            DatabaseUtils.cleanup(connection);
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
            
            for (Entry<Integer,Set<String>> activeCautionAvailabilityAlerts_Entry : GlobalVariables.activeCautionAvailabilityAlerts.entrySet()) {
                Integer alertId = activeCautionAvailabilityAlerts_Entry.getKey();
                Set<String> activeCautionAvailabilityMetricKeys = activeCautionAvailabilityAlerts_Entry.getValue();

                if (activeCautionAvailabilityMetricKeys != null) {
                    activeCautionAvailabilityMetricKeys.remove(metricKey);
                    if (activeCautionAvailabilityMetricKeys.isEmpty()) alertIdsToRemoveFrom_ActiveCautionAvailabilityAlerts.add(alertId);
                }
            }
            
            for (Integer alertId : alertIdsToRemoveFrom_ActiveCautionAvailabilityAlerts) {
                GlobalVariables.activeCautionAvailabilityAlerts.remove(alertId);
            }
        }
        

        // danger
        synchronized(GlobalVariables.activeDangerAvailabilityAlerts) {
            List<Integer> alertIdsToRemoveFrom_ActiveDangerAvailabilityAlerts = new ArrayList<>();
            
            for (Entry<Integer,Set<String>> activeDangerAvailabilityAlerts_Entry : GlobalVariables.activeDangerAvailabilityAlerts.entrySet()) {
                Integer alertId = activeDangerAvailabilityAlerts_Entry.getKey();
                Set<String> activeDangerAvailabilityMetricKeys = activeDangerAvailabilityAlerts_Entry.getValue();

                if (activeDangerAvailabilityMetricKeys != null) {
                    activeDangerAvailabilityMetricKeys.remove(metricKey);
                    if (activeDangerAvailabilityMetricKeys.isEmpty()) alertIdsToRemoveFrom_ActiveDangerAvailabilityAlerts.add(alertId);
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
        int metricKeys_Size = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.keySet().size();
        
        try {
            // for every metric key that has had a recent datapoint...
            for (Entry<String,List<MetricTimestampAndValue>> recentMetricTimestampsAndValuesByMetricKey_Entry : GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.entrySet()) {
                String metricKey = recentMetricTimestampsAndValuesByMetricKey_Entry.getKey();
                List<MetricTimestampAndValue> recentMetricTimestampsAndValues = recentMetricTimestampsAndValuesByMetricKey_Entry.getValue();
                        
                boolean isImmeadiateCleanup = immediateCleanupMetricKeys_.contains(metricKey); // we should cleanup this metric regardless...
                
                // lookup the longest window duration associated with this metric key...
                Long windowDuration = longestWindowDurationsForMetricKeys.get(metricKey);

                // remove data that is outside of the window duration (older than 'now' minus 'duration') for this metric key               
                if ((windowDuration != null) && (recentMetricTimestampsAndValues != null)) { 
                    List<MetricTimestampAndValue> metricTimestampAndValuesToRemove = new ArrayList<>();

                    synchronized (recentMetricTimestampsAndValues) {
                        for (MetricTimestampAndValue metricTimestampAndValue : recentMetricTimestampsAndValues) {
                            long metricValueTimestamp = metricTimestampAndValue.getTimestamp();
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
                + ", NumRecentMetricKeys=" + metricKeys_Size
                + ", NumRecentMetricValuesRemoved=" + numValuesRemoved;

        return outputMessage;
    }
    
    /* 
    Checks the caution & danger durations of all specified alerts to get the longest window duration.
    The window durations of disabled alerts are not used. 
    */
    private Map<String,Long> getLongestWindowDurationsForMetricKeys(List<Alert> alerts) {
        
        if (alerts == null) {
            return new HashMap<>();
        }
        
        Map<String,Long> longestWindowDurationsForMetricKeys = new HashMap<>();
            
        Set<String> suspendedMetricKeys;
        synchronized (GlobalVariables.suspendedMetricKeys) {
            suspendedMetricKeys = new HashSet<>(GlobalVariables.suspendedMetricKeys.keySet());
        }
                    
        for (Alert alert : alerts) {
            if ((alert.isEnabled() != null) && alert.isEnabled()) {
                List<String> metricKeysAssociatedWithAlert = MetricAssociation.getMetricKeysAssociatedWithAlert(alert, suspendedMetricKeys);

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
