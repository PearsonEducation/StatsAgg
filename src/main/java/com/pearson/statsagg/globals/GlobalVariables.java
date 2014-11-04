package com.pearson.statsagg.globals;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricAggregated;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricRaw;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricRaw;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jeffrey Schmidt
 */
public class GlobalVariables {

    // A flag that indicates whether the StatsAgg is using an in-memory database or not
    public static AtomicBoolean isStatsaggUsingInMemoryDatabase = new AtomicBoolean(false);
    
    // Used to determine how long StatsAgg has been running. Should be set by the initialization routine.
    public final static AtomicLong statsaggStartTimestamp = new AtomicLong(0);
    
    // Used to track how many metrics are flowing into StatsAgg
    public final static AtomicLong incomingMetricsCount = new AtomicLong(0);
    
    // Used to track how many metrics are flowing into StatsAgg
    public final static AtomicLong incomingMetricsRollingAverage = new AtomicLong(0);
    
    // Used to track how many metrics are known to StatsAgg that have a valid association & a stored datapoint
    public final static AtomicLong associatedMetricsWithValuesCount = new AtomicLong(0);
    
    // Used to generate hash keys for incoming metrics
    public final static AtomicLong rawMetricHashKeyGenerator = new AtomicLong(Long.MIN_VALUE);
    public final static AtomicLong aggregatedMetricHashKeyGenerator = new AtomicLong(Long.MIN_VALUE);

    // k="Value assigned at raw metric arrival from the appropriate 'MetricsHashKeyGenerator' object", v="Raw metric object"
    public final static ConcurrentHashMap<Long,StatsdMetricRaw> statsdMetricsRaw = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,GraphiteMetricRaw> graphiteAggregatorMetricsRaw = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,GraphiteMetricRaw> graphitePassthroughMetricsRaw = new ConcurrentHashMap<>();
    
    // k=MetricKey, v="Aggregated metric object"
    public final static ConcurrentHashMap<String,StatsdMetricAggregated> statsdMetricsAggregatedMostRecentValue = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,GraphiteMetricAggregated> graphiteAggregatedMetricsMostRecentValue = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,GraphiteMetricRaw> graphitePassthroughMetricsMostRecentValue = new ConcurrentHashMap<>();

    // k=MetricKey, v="SHA-1(MetricKey)"
    public final static ConcurrentHashMap<String,String> statsdGaugeBucketDigests = new ConcurrentHashMap<>();
    
    // k=MetricKey, v=MetricKey (k=v. Both are a strings that specify the metric key of a metric to 'forget'.
    public final static ConcurrentHashMap<String,String> forgetMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetStatsdMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphiteAggregatedMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphitePassthroughMetrics = new ConcurrentHashMap<>();
    
    // k=MetricKeyRegex, v=MetricKeyRegex (k=v. Both are a regexs that specify a pattern that should be matched against known metrics (for 'forgetting').
    public final static ConcurrentHashMap<String,String> forgetMetricsRegexs = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetStatsdMetricsRegexs = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphiteAggregatedMetricsRegexs = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphitePassthroughMetricsRegexs = new ConcurrentHashMap<>();
    
    // k=MetricKey, v=MetricKey (k=v. The cleanup routine will cleanup these metrics ASAP (regardless of whether they're tracked an alert or not).
    public final static ConcurrentHashMap<String,String> immediateCleanupMetrics = new ConcurrentHashMap<>();
    
    // k=MetricGroupId, v="Remove","Alter" 
    public final static ConcurrentHashMap<Integer,String> metricGroupChanges = new ConcurrentHashMap<>();
            
    // k=MetricKey, v="The most timestamp that this metric was received by this program"
    public final static ConcurrentHashMap<String,Long> metricKeysLastSeenTimestamp = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v=[0] "list of metric groups ids with negative associations with the metric key", [1] "list of metric groups ids with positive associations with the metric key"
    public final static ConcurrentHashMap<String,ArrayList[]> metricGroupsAssociatedWithMetricKeys = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupId, v=Set<MetricKey> "is the metric key associated with a specific metric group? only include in the set if the assocation/match is true.">
    public final static ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v="Boolean for "is this metric key associated with ANY metric group"?
    public final static ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithAnyMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupId, v=string representing a single, merged, regex statement that is composed of the metric group's associated regexs
    public final static ConcurrentHashMap<Integer,String> mergedRegexsForMetricGroups = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v=List<MetricTimestampAndValue> (should be -- synchronizedSet(TreeSet<MetricTimestampAndValue>()))
    public final static ConcurrentHashMap<String,Set<MetricTimestampAndValue>> recentMetricTimestampsAndValuesByMetricKey = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupRegex-pattern, v="MetricGroupRegex-pattern compiled pattern. This is a cache for compiled regex patterns."
    public final static ConcurrentHashMap<String,Pattern> metricGroupRegexPatterns = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeCautionAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeDangerAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer, Alert> pendingCautionAlertsByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer, Alert> pendingDangerAlertsByAlertId = new ConcurrentHashMap<>(); 
    
    // k="{metricKey}-{alertId}", v='Alert routine calculated metric value'
    public final static ConcurrentHashMap<String,BigDecimal> activeCautionAlertMetricValues = new ConcurrentHashMap<>(); 
    
    // k="{metricKey}-{alertId}", v='Alert routine calculated metric value'
    public final static ConcurrentHashMap<String,BigDecimal> activeDangerAlertMetricValues = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v='is alert suspended (as of last alert routine run)?'
    public final static ConcurrentHashMap<Integer, Boolean> alertSuspensionStatusByAlertId = new ConcurrentHashMap<>();
    
    // k=AlertId, v=alert suspension ids that are currently associated with a specific alert
    public final static ConcurrentHashMap<Integer, Set<Integer>> alertSuspensionIdAssociationsByAlertId = new ConcurrentHashMap<>();
    
    // k=AlertId, v=the alert suspension level (ALERT_NOT_SUSPENDED, SUSPEND_ALERT_NOTIFICATION_ONLY, SUSPEND_ENTIRE_ALERT)
    public final static ConcurrentHashMap<Integer, Integer> alertSuspensionLevelsByAlertId = new ConcurrentHashMap<>();
    
    // The timestamp of the last time the alert routine finished executing. This variable does not persist across application restarts.
    public final static AtomicLong alertRountineLastExecutedTimestamp = new AtomicLong(0);
    
    // Used to lock down the alert routine so that the alert routine can have exclusive access to the data structures it uses
    public final static Object alertRoutineLock = new Object();
}
