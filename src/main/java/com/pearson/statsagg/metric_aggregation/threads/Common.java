package com.pearson.statsagg.metric_aggregation.threads;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricKeyLastSeen;
import com.pearson.statsagg.metric_formats.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.Threads;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    public static void removeMetricKeysFromGraphiteMetricsList(List<GraphiteMetric> graphiteMetrics, Set<String> metricKeysToRemove) {
        
        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return;
        }
        
        Map<String, GraphiteMetric> metricsMap = new HashMap<>();
        
        for (GraphiteMetric graphiteMetric : graphiteMetrics) {
            String metricKey = graphiteMetric.getMetricKey();
            if (metricKey != null) metricsMap.put(metricKey, graphiteMetric);
        }
                
        for (String metricKeyToRemove : metricKeysToRemove) {
            Object metric = metricsMap.get(metricKeyToRemove);
            if (metric != null) metricsMap.remove(metricKeyToRemove);
        }
        
        graphiteMetrics.clear();
        graphiteMetrics.addAll(metricsMap.values());
    }
    
}
