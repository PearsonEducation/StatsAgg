package com.pearson.statsagg.globals;

import com.pearson.statsagg.threads.invokers.AlertInvokerThread;
import com.pearson.statsagg.threads.invokers.CleanupInvokerThread;
import com.pearson.statsagg.threads.invokers.MetricAssociationOutputBlacklistInvokerThread;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.gauges.Gauge;
import com.pearson.statsagg.metric_aggregation.MetricKeyLastSeen;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v2;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetric;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.threads.invokers.TemplateInvokerThread;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jeffrey Schmidt
 */
public class GlobalVariables {
        
    // generic constants for use by any class that needs to use them
    public static final Byte NEW = 1;
    public static final Byte ALTER = 2;
    public static final Byte REMOVE = 3;
    
    // the prefixes (added on by StatsAgg) for the various types of metrics
    public static String graphiteAggregatedPrefix = "";
    public static String graphitePassthroughPrefix = "";
    public static String openTsdbPrefix = "";
    public static String influxdbPrefix = "";
    
    // the 'invoker' thread for the alert routine. this is global so that the webui can trigger the alert-routine.
    public static AlertInvokerThread alertInvokerThread = null;
    
    // the 'invoker' thread for the template routine. this is global so that the webui can trigger the template-routine.
    public static TemplateInvokerThread templateInvokerThread = null;
    
    // the 'invoker' thread for the cleanup routine. this is global so that the webui can trigger the cleanup-routine.
    public static CleanupInvokerThread cleanupInvokerThread = null;
    
    // the 'invoker' thread for the metric-association output blacklist routine. this is global so that the webui can trigger this routine.
    public static MetricAssociationOutputBlacklistInvokerThread metricAssociationOutputBlacklistInvokerThread = null;
    
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
    
    // Used to track how many metric datapoints are currently in statsagg
    public final static AtomicLong currentDatapointsInMemory = new AtomicLong(0);
    
    // Used to track how many metrics are known to StatsAgg that have a valid association & a stored datapoint
    public final static AtomicLong associatedMetricsWithValuesCount = new AtomicLong(0);
    
    // Used to generate hash keys for incoming metrics
    public final static AtomicLong metricHashKeyGenerator = new AtomicLong(Long.MIN_VALUE);

    // k="Value assigned at metric arrival from the appropriate 'MetricsHashKeyGenerator' object", v="metric object"
    public final static ConcurrentHashMap<Long,StatsdMetric> statsdNotGaugeMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,StatsdMetric> statsdGaugeMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,GraphiteMetric> graphiteAggregatorMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,GraphiteMetric> graphitePassthroughMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,OpenTsdbMetric> openTsdbMetrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,InfluxdbMetric_v1> influxdbV1Metrics = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<Long,InfluxdbMetric_v2> influxdbV2Metrics = new ConcurrentHashMap<>();

    // k=MetricKey, v="Aggregated metric object"
    public final static ConcurrentHashMap<String,StatsdMetricAggregated> statsdMetricsAggregatedMostRecentValue = new ConcurrentHashMap<>(16, 0.75f, 3);

    // k=MetricKey, v=Gauge (kept in sync with the database)
    public final static ConcurrentHashMap<String,Gauge> statsdGaugeCache = new ConcurrentHashMap<>(16, 0.75f, 3);
    
    // k=MetricKey, v=MetricKey (k=v. The cleanup routine will cleanup these metrics ASAP (regardless of whether they're tracked an alert or not).
    public final static ConcurrentHashMap<String,String> immediateCleanupMetrics = new ConcurrentHashMap<>();
    
    // k=MetricKey, v="The most recent timestamp that this metric was received by this program"
    public final static ConcurrentHashMap<String,MetricKeyLastSeen> metricKeysLastSeenTimestamp = new ConcurrentHashMap<>(16, 0.75f, 6); 
    
    // k=MetricKey, v=List<MetricTimestampAndValue> (should be -- synchronizedList(ArrayList<MetricTimestampAndValue>()))
    public final static ConcurrentHashMap<String,List<MetricTimestampAndValue>> recentMetricTimestampsAndValuesByMetricKey = new ConcurrentHashMap<>(16, 0.75f, 6); 
    
    // k=MetricGroupId, v="codes for "New", "Remove", "Alter" 
    public final static ConcurrentHashMap<Integer,Byte> metricGroupChanges = new ConcurrentHashMap<>();
            
    // k=SuspensionId, v="codes for "New", "Remove", "Alter" (only applies to metric suspensions)
    public final static ConcurrentHashMap<Integer,Byte> suspensionChanges = new ConcurrentHashMap<>();
    
    // k=MetricGroupId, v=Set<MetricKey> "is the metric key associated with a specific metric group? only include in the set if the assocation/match is true.">
    public final static ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupId, v=Set<MetricKey> "is the metric key associated with the output blacklist metric group? only include in the set if the assocation/match is true.">
    public static ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=SuspensionId, v=Set<MetricKey> "is the metric key associated with a specific suspension? only include in the set if the assocation/match is true.">
    public final static ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithSuspension = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v="Boolean for "is this metric key associated with ANY metric group"?"
    public final static ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithAnyMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v="Boolean for "is this metric key associated with the output blacklist"?"
    public static ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithOutputBlacklistMetricGroup = new ConcurrentHashMap<>(); 
    
    // k=MetricKey, v="Boolean for "is this metric key associated with ANY suspension"?"
    public final static ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithAnySuspension = new ConcurrentHashMap<>(); 
    
    // k=MetricGroupId, v=string representing a single, merged, match regex statement that is composed of the metric group's associated regexes
    public final static ConcurrentHashMap<Integer,String> mergedMatchRegexesByMetricGroupId = new ConcurrentHashMap<>(); 

    // k=SuspensionId, v=string representing a single, merged, match regex statement that is composed of the suspensions's associated regexes
    public final static ConcurrentHashMap<Integer,String> mergedMatchRegexesBySuspensionId = new ConcurrentHashMap<>(); 

    // k=MetricGroupId, v=string representing a single, merged, blacklist regex statement that is composed of the metric group's associated regexes
    public final static ConcurrentHashMap<Integer,String> mergedBlacklistRegexesByMetricGroupId = new ConcurrentHashMap<>(); 
    
    // k=SuspensionId, v=string representing a single, merged, blacklist regex statement that is composed of the suspension's associated regexes
    public final static ConcurrentHashMap<Integer,String> mergedBlacklistRegexesBySuspensionId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeCautionAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=MetricKey
    public final static ConcurrentHashMap<Integer,List<String>> activeDangerAlertMetricKeysByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer,Alert> pendingCautionAlertsByAlertId = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v=Alert
    public final static ConcurrentHashMap<Integer,Alert> pendingDangerAlertsByAlertId = new ConcurrentHashMap<>(); 

    // k=MetricKey, v=MetricKey
    public final static ConcurrentHashMap<String,String> activeAvailabilityAlerts = new ConcurrentHashMap<>();
   
    // k=AlertId, v=Set<MetricKey>
    public final static ConcurrentHashMap<Integer,Set<String>> activeCautionAvailabilityAlerts = new ConcurrentHashMap<>();
    
    // k=AlertId, v=Set<MetricKey>
    public final static ConcurrentHashMap<Integer,Set<String>> activeDangerAvailabilityAlerts = new ConcurrentHashMap<>();
    
    // k="{metricKey}-{alertId}", v='Alert routine calculated metric value'
    public final static ConcurrentHashMap<String,BigDecimal> activeCautionAlertMetricValues = new ConcurrentHashMap<>(); 
    
    // k="{metricKey}-{alertId}", v='Alert routine calculated metric value'
    public final static ConcurrentHashMap<String,BigDecimal> activeDangerAlertMetricValues = new ConcurrentHashMap<>(); 
    
    // k=AlertId, v='is alert suspended (as of last alert routine run)?'
    public final static ConcurrentHashMap<Integer,Boolean> suspensionStatusByAlertId = new ConcurrentHashMap<>();
    
    // k=AlertId, v=suspension ids that are currently associated with a specific alert
    public final static ConcurrentHashMap<Integer,Set<Integer>> suspensionIdAssociationsByAlertId = new ConcurrentHashMap<>();
    
    // k=AlertId, v=the suspension level (LEVEL_ALERT_NOT_SUSPENDED, LEVEL_SUSPEND_ALERT_NOTIFICATION_ONLY, LEVEL_SUSPEND_ENTIRE_ALERT)
    public final static ConcurrentHashMap<Integer,Integer> suspensionLevelsByAlertId = new ConcurrentHashMap<>();
    
    // k=MetricKey, v=MetricKey -- k=v, "the set of metric keys that are currently suspended"
    public final static ConcurrentHashMap<String,String> suspendedMetricKeys = new ConcurrentHashMap<>();
    
    // The timestamp of the last time the alert routine finished executing. This variable does not persist across application restarts.
    public final static AtomicLong alertRountineLastExecutedTimestamp = new AtomicLong(0);
    
    // Used to lock down the alert routine so that the alert routine can have exclusive access to the data structures it uses
    public final static Object alertRoutineLock = new Object();
    
    // Used to lock down the cleanup routine so that it doesn't clear out any metrics while the metric association routine is running
    public final static Object cleanupOldMetricsLock = new Object();

}
