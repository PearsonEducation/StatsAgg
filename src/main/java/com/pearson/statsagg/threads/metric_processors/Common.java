package com.pearson.statsagg.threads.metric_processors;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklistDao;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricKeyLastSeen;
import com.pearson.statsagg.metric_formats.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import com.pearson.statsagg.utilities.core_utils.Threads;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Common {
    
    private static final Logger logger = LoggerFactory.getLogger(Common.class.getName());

    public static int waitUntilThisIsYoungestActiveThread(long threadStartTimestampInMilliseconds, List<Long> threadTimestamps) {
        
        Long currentYoungestAggregationThreadStartTimestamp;
        synchronized(threadTimestamps) {
            currentYoungestAggregationThreadStartTimestamp = MathUtilities.getSmallestValue(threadTimestamps);
        }

        int waitInMsCounter = 0;
        
        while ((currentYoungestAggregationThreadStartTimestamp != null) && (threadStartTimestampInMilliseconds != currentYoungestAggregationThreadStartTimestamp)) {
            int waitInMs = 100;
            Threads.sleepMilliseconds(waitInMs);
            waitInMsCounter += waitInMs;

            synchronized(threadTimestamps) {
                currentYoungestAggregationThreadStartTimestamp = MathUtilities.getSmallestValue(threadTimestamps);
            }

            if ((waitInMsCounter % 1000) == 0) {
                logger.debug("Wait for youngest active statsd aggregation thread taking too long - " + waitInMsCounter +  "ms. Timestamp = " + threadStartTimestampInMilliseconds);
            }
        }
        
        return waitInMsCounter;
    }
    
    public static List<String> getMetricKeysFromMetrics_List(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> metricKeys = new ArrayList<>(metrics.size() * 2);
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();
            metricKeys.add(metricKey);
        }
        
        return metricKeys;
    }
    
    public static Set<String> getMetricKeysFromMetrics_Set(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> metricKeys = new HashSet<>(metrics.size() * 2);
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();
            metricKeys.add(metricKey);
        }
        
        return metricKeys;
    }
    
    public static void updateMetricLastSeenTimestamps(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return;
        }
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();
            Long metricReceivedTimestampInMilliseconds = metric.getMetricReceivedTimestampInMilliseconds();
            MetricKeyLastSeen metricKeyLastSeen = new MetricKeyLastSeen(metricReceivedTimestampInMilliseconds, metricReceivedTimestampInMilliseconds);
            GlobalVariables.metricKeysLastSeenTimestamp.put(metricKey, metricKeyLastSeen);
        }
        
    }

    public static void updateMetricLastSeenTimestamps(List<? extends GenericMetricFormat> currentMetrics, List<? extends GenericMetricFormat> resendMetrics) {
        
        if (resendMetrics == null) {
            return;
        }
        
        Map<String,GenericMetricFormat> currentMetricsMap = new HashMap<>();
        if (currentMetrics != null) for (GenericMetricFormat metric : currentMetrics) currentMetricsMap.put(metric.getMetricKey(), metric);
        
        for (GenericMetricFormat resendMetric : resendMetrics) {
            String metricKey = resendMetric.getMetricKey();
            
            GenericMetricFormat currentMetric = currentMetricsMap.get(metricKey);
            Long metricKeyLastSeenTimestamp_Current = (currentMetric != null) ? currentMetric.getMetricReceivedTimestampInMilliseconds() : null;
            
            Long metricKeyLastSeenTimestamp_UpdateOnResend = resendMetric.getMetricReceivedTimestampInMilliseconds();
            
            MetricKeyLastSeen metricKeyLastSeen = new MetricKeyLastSeen(metricKeyLastSeenTimestamp_Current, metricKeyLastSeenTimestamp_UpdateOnResend);
            GlobalVariables.metricKeysLastSeenTimestamp.put(metricKey, metricKeyLastSeen);
        }
        
    }

    public static void updateAlertMetricRecentValues(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return;
        }
                
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();

            MetricTimestampAndValue metricTimestampAndValue = new MetricTimestampAndValue(metric.getMetricTimestampInMilliseconds(), metric.getMetricValueBigDecimal(), metric.getMetricHashKey());

            List<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);

            if (metricTimestampsAndValues != null) {
                metricTimestampsAndValues.add(metricTimestampAndValue);
            }
            else {
                metricTimestampsAndValues = Collections.synchronizedList(new ArrayList<>());
                metricTimestampsAndValues.add(metricTimestampAndValue);
                GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.put(metricKey, metricTimestampsAndValues);
            }
        }

    }

    public static List<GraphiteMetric> removeMetricKeysFromGraphiteMetricsList(List<GraphiteMetric> graphiteMetrics, Set<String> metricKeysToRemove) {
        
        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return graphiteMetrics;
        }
        
        List<GraphiteMetric> graphiteMetrics_WithMetricsRemoved = new ArrayList<>(graphiteMetrics.size());

        for (GraphiteMetric graphiteMetric : graphiteMetrics) {
            String metricKey = graphiteMetric.getMetricKey();
            
            if ((metricKey != null) && !metricKeysToRemove.contains(metricKey)) {
                graphiteMetrics_WithMetricsRemoved.add(graphiteMetric);
            }
        }

        return graphiteMetrics_WithMetricsRemoved;
    }
    
    public static List<String> getOutputBlacklistMetricKeys() {
        
        List<OutputBlacklist> outputBlacklists = OutputBlacklistDao.getOutputBlacklists(DatabaseConnections.getConnection(), true);
        if (outputBlacklists.size() > 1) logger.warn("There should not be more than one output blacklist row in the database.");
        
        if (outputBlacklists.size() > 0) {
            OutputBlacklist outputBlacklist = outputBlacklists.get(0);
            if ((outputBlacklist == null) || (outputBlacklist.getMetricGroupId() == null)) return new ArrayList<>();
            
            Set<String> metricKeys = GlobalVariables.matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup.get(outputBlacklist.getMetricGroupId());
            if (metricKeys == null) return new ArrayList<>();
            List<String> metricKeys_Local = null;
            
            synchronized(metricKeys) {
                metricKeys_Local = new ArrayList<>(metricKeys);
            }
            
            return metricKeys_Local;
        }
        else {
            return new ArrayList<>();
        }
        
    }
    
}
