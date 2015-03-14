package com.pearson.statsagg.globals;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.gauges.Gauge;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricAggregated;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricRaw;
import com.pearson.statsagg.metric_aggregation.opentsdb.OpenTsdbMetricRaw;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricRaw;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jeffrey Schmidt
 */
public class GlobalVariables {

    // A flag indicating whether statsagg has finished going through its initialization routine. This will only be true if it has gone through the initialization routine successfully.
    public static AtomicBoolean isApplicationInitializeSuccess = new AtomicBoolean(false);
    
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
    public final static ConcurrentHashMap<Long,OpenTsdbMetricRaw> openTsdbMetricsRaw = new ConcurrentHashMap<>();
    
    // k=MetricKey, v="Aggregated metric object"
    public final static ConcurrentHashMap<String,StatsdMetricAggregated> statsdMetricsAggregatedMostRecentValue = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,GraphiteMetricAggregated> graphiteAggregatedMetricsMostRecentValue = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,GraphiteMetricRaw> graphitePassthroughMetricsMostRecentValue = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,OpenTsdbMetricRaw> openTsdbMetricsMostRecentValue = new ConcurrentHashMap<>();

    // k=MetricKey, v=Gauge (kept in sync with the database)
    public final static ConcurrentHashMap<String,Gauge> statsdGaugeCache = new ConcurrentHashMap<>();
    
    // k=MetricKey, v=MetricKey (k=v. Both are a strings that specify the metric key of a metric to 'forget'.
    public final static ConcurrentHashMap<String,String> forgetMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetStatsdMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphiteAggregatedMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphitePassthroughMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetOpenTsdbMetrics = new ConcurrentHashMap<>();

    // k=MetricKeyRegex, v=MetricKeyRegex (k=v. Both are a regexs that specify a pattern that should be matched against known metrics (for 'forgetting').
    public final static ConcurrentHashMap<String,String> forgetMetricsRegexs = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetStatsdMetricsRegexs = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphiteAggregatedMetricsRegexs = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetGraphitePassthroughMetricsRegexs = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,String> forgetOpenTsdbMetricsRegexs = new ConcurrentHashMap<>();

    // k=MetricKey, v=MetricKey (k=v. The cleanup routine will cleanup these metrics ASAP (regardless of whether they're tracked an alert or not).
    public final static ConcurrentHashMap<String,String> immediateCleanupMetrics = new ConcurrentHashMap<>();
    
    // k=MetricGroupId, v="Remove","Alter" 
    public final static ConcurrentHashMap<Integer,String> metricGroupChanges = new ConcurrentHashMap<>();
            
    // k=MetricKey, v="The most timestamp that this metric was received by this program"
    public final static ConcurrentHashMap<String,Long> metricKeysLastSeenTimestamp = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v="The most timestamp that this metric was received by this program. Gets updated if the metric is configured to send 0 or previous value when no new metrics were received."
    public final static ConcurrentHashMap<String,Long> metricKeysLastSeenTimestamp_UpdateOnResend = new ConcurrentHashMap<>(); 

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
    
    // k=MetricGroupRegex-pattern, v="MetricGroupRegex-pattern. If a regex pattern is bad (doesn't compile), then it is stored here so we don't try to recompile it."
    public final static ConcurrentHashMap<String,String> metricGroupRegexBlacklist = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeCautionAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeDangerAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer, Alert> pendingCautionAlertsByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer, Alert> pendingDangerAlertsByAlertId = new ConcurrentHashMap<>(); 

    // k=MetricKey, v=MetricKey
    public static final ConcurrentHashMap<String, String> activeAvailabilityAlerts = new ConcurrentHashMap<>();
   
    // k=AlertId, v=Set<MetricKey>
    public static final ConcurrentHashMap<Integer, Set<String>> activeCautionAvailabilityAlerts = new ConcurrentHashMap<>();
    
    // k=AlertId, v=Set<MetricKey>
    public static final ConcurrentHashMap<Integer, Set<String>> activeDangerAvailabilityAlerts = new ConcurrentHashMap<>();
    
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
    
    // Used to lock down the cleanup routine so that it doesn't clear out any metrics while the metric association routine is running
    public final static Object cleanupOldMetricsLock = new Object();
}
