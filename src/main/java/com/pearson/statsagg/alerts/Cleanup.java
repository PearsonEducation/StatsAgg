package com.pearson.statsagg.alerts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Cleanup {

    private static final Logger logger = LoggerFactory.getLogger(Cleanup.class.getName());
    
    private final String threadId_;
    private final Set<String> immediateCleanupMetricKeys_;
    
    public Cleanup(String threadId) {
        this.threadId_ = threadId;
        
        forgetMetrics_PutMetricKeysInSetOfMetricsToForget();
                
        immediateCleanupMetricKeys_ = new HashSet<>(GlobalVariables.immediateCleanupMetrics.keySet());
    }
    
    public void runCleanupRoutine(List<Alert> alerts) {
        String cleanupOutputMessage = cleanupMetricsNotRecentlySeen();
        logger.info(cleanupOutputMessage);
        
        cleanupOutputMessage = cleanupRecentMetricTimestampsAndValues(alerts);
        logger.info(cleanupOutputMessage);

        for (String metricKey : immediateCleanupMetricKeys_) {
            GlobalVariables.immediateCleanupMetrics.remove(metricKey);
        }
    }
    
    protected String cleanupMetricsNotRecentlySeen() {
        
        long cleanupStartTime = System.currentTimeMillis();
        
        long currentTimeInMilliseconds = System.currentTimeMillis();
        long numberMillisecondsInOneDay = 86400000;
        long metricsRemoved = 0;
        
        Set<String> metricKeysLastSeenTimestampKeys = GlobalVariables.metricKeysLastSeenTimestamp.keySet();

        synchronized(GlobalVariables.metricGroupChanges) {
            for (String metricKey : metricKeysLastSeenTimestampKeys) {
                boolean isImmeadiateCleanup = immediateCleanupMetricKeys_.contains(metricKey); // we should cleanup this metric regardless...
                
                try {
                    Long timestamp = GlobalVariables.metricKeysLastSeenTimestamp.get(metricKey);

                    if (isImmeadiateCleanup) {
                        removeMetricAssociations(metricKey);
                        metricsRemoved++;
                    }
                    if (timestamp != null) {
                        long metricNotSeenTimeInMilliseconds = currentTimeInMilliseconds - timestamp;

                        if (metricNotSeenTimeInMilliseconds > numberMillisecondsInOneDay) { // older than 24hrs
                            removeMetricAssociations(metricKey);
                            metricsRemoved++;
                        }
                    }
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        
        long cleanupTimeElasped = System.currentTimeMillis() - cleanupStartTime;
        
        String outputMessage = "ThreadId=" + threadId_
                + ", Routine=CleanupMetricsNotRecentlySeen"
                + ", CleanupTime=" + cleanupTimeElasped
                + ", MetricsRemoved=" + metricsRemoved;
        
        return outputMessage;
    }
    
    protected void removeMetricAssociations(String metricKey) {
        
        if (metricKey == null) {
            return;
        }

        for (Integer metricGroupId : GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.keySet()) {
            Set<String> matchingMetricKeyAssociationWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);
            matchingMetricKeyAssociationWithMetricGroup.remove(metricKey);
        }
        
        List[] associationLists = GlobalVariables.metricGroupsAssociatedWithMetricKeys.get(metricKey);
        if (associationLists != null) {
            associationLists[0].clear();
            associationLists[0] = null;
            associationLists[1].clear();
            associationLists[1] = null;
            GlobalVariables.metricGroupsAssociatedWithMetricKeys.remove(metricKey);
        }
        
        GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.remove(metricKey);
        
        GlobalVariables.metricKeysLastSeenTimestamp.remove(metricKey);
    }
    
    protected String cleanupRecentMetricTimestampsAndValues(List<Alert> alerts) {

        long cleanupStartTime = System.currentTimeMillis();
        int numValuesRemoved = 0, numKeysRemoved = 0, numTrackedValuesRemoved = 0, numTrackedKeysRemoved = 0;
        
        Map<String,Integer> longestWindowDurationsForMetricKeys = getLongestWindowDurationsForMetricKeys(alerts);
        Set<String> metricKeys = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.keySet();
        
        try {
            // for every metric key that has had a recent datapoint...
            for (String metricKey : metricKeys) {
                boolean isImmeadiateCleanup = immediateCleanupMetricKeys_.contains(metricKey); // we should cleanup this metric regardless...
                
                // lookup the longest window duration associated with this metric key...
                Integer windowDuration = longestWindowDurationsForMetricKeys.get(metricKey);

                synchronized (GlobalVariables.recentMetricTimestampsAndValuesByMetricKey) {
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
                                else { // because recentMetricTimestampsAndValues ordered sorted by timestamp, we can break once we hit the first metric within the window
                                    break;
                                }
                            }
                        }

                        for (MetricTimestampAndValue metricTimestampAndValueToRemove : metricTimestampAndValuesToRemove) {
                            recentMetricTimestampsAndValues.remove(metricTimestampAndValueToRemove);
                            numValuesRemoved++;
                            numTrackedValuesRemoved++;
                        }
                        
                        if (recentMetricTimestampsAndValues.isEmpty() || isImmeadiateCleanup) {
                            GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.remove(metricKey);
                            numKeysRemoved++;
                            numTrackedKeysRemoved++;
                        }
                    }
                    // the metric isn't currently associated with an alert, so we can get rid this metric key's data
                    else if (recentMetricTimestampsAndValues != null) {
                        synchronized (recentMetricTimestampsAndValues) {
                            numValuesRemoved += recentMetricTimestampsAndValues.size();
                            recentMetricTimestampsAndValues.clear();
                        }

                        GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.remove(metricKey);
                        numKeysRemoved++;
                    }
                    else {
                        GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.remove(metricKey);
                        numKeysRemoved++;
                    }      
                }
            }
        }
        catch (Exception e) {
            logger.error("ThreadId=" + threadId_ + ", Routine=CleanupRecentMetricTimestampsAndValues, " 
                    + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        long cleanupTimeElasped = System.currentTimeMillis() - cleanupStartTime;

        String outputMessage = "ThreadId=" + threadId_
                + ", Routine=CleanupRecentMetricTimestampsAndValues"
                + ", CleanupTime=" + cleanupTimeElasped
                + ", NumRecentMetricKeys=" + metricKeys.size()
                + ", NumRecentMetricValuesRemoved=" + numValuesRemoved
                + ", NumRecentMetricKeysRemoved=" + numKeysRemoved
                + ", NumTrackedRecentMetricValuesRemoved=" + numTrackedValuesRemoved
                + ", NumTrackedRecentMetricKeysRemoved=" + numTrackedKeysRemoved;

        return outputMessage;
    }
    
    /* Checks the caution & danger durations of all specified alerts to get the longest window duration.
       The window durations of disabled alerts are not used. 
    */
    private Map<String,Integer> getLongestWindowDurationsForMetricKeys(List<Alert> alerts) {
        
        if (alerts == null) {
            return new HashMap<>();
        }
        
        Map<String,Integer> longestWindowDurationsForMetricKeys = new HashMap<>();
                
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

    private void updateLongestWindowDurationsForMetricKeys(Alert alert, String metricKey, Map<String,Integer> longestWindowDurationsForMetricKeys) {
        
        if ((longestWindowDurationsForMetricKeys == null) || (alert == null) || (metricKey == null)) {
            return;
        }
        
        Integer alertLongestWindowDuration = alert.getLongestWindowDuration();
        
        if (!longestWindowDurationsForMetricKeys.containsKey(metricKey) && (alertLongestWindowDuration != null)) {
            longestWindowDurationsForMetricKeys.put(metricKey, alertLongestWindowDuration);
        }
        else {
            Integer currentLongestWindowDurationForMetricKey = longestWindowDurationsForMetricKeys.get(metricKey);
            
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
        
        if ((regex == null) || regex.isEmpty() || (GlobalVariables.metricKeysLastSeenTimestamp == null)) {
            return new HashSet<>();
        }
             
        Set<String> metricKeysToForget = new HashSet<>();
        
        Pattern pattern = Pattern.compile(regex);
         
        for (String metricKey : GlobalVariables.metricKeysLastSeenTimestamp.keySet()) {
            Matcher matcher = pattern.matcher(metricKey);
            
            if (matcher.matches()) {
                metricKeysToForget.add(metricKey);
            }
        }
         
        return metricKeysToForget;
    }

}
