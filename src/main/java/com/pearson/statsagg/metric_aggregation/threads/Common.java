package com.pearson.statsagg.metric_aggregation.threads;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetric;
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

    public static void updateMetricLastSeenTimestamps_MostRecentNew(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return;
        }
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();
            GlobalVariables.metricKeysLastSeenTimestamp.put(metricKey, metric.getMetricReceivedTimestampInMilliseconds());
        }
        
    }
    
    public static void updateMetricLastSeenTimestamps_UpdateOnResend(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return;
        }
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();
            GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.put(metricKey, metric.getMetricReceivedTimestampInMilliseconds());
        }
        
    }
    
    // use this when 'send previous value' is disabled
    public static void updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return;
        }
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();
            GlobalVariables.metricKeysLastSeenTimestamp.put(metricKey, metric.getMetricReceivedTimestampInMilliseconds());
            GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.put(metricKey, metric.getMetricReceivedTimestampInMilliseconds());
        }
        
    }

    public static boolean updateAlertMetricRecentValues(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return false;
        }
        
        boolean didDoAnyUpdates = false;
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();

            MetricTimestampAndValue metricTimestampAndValue = new MetricTimestampAndValue(
                    metric.getMetricTimestampInMilliseconds(), metric.getMetricValueBigDecimal(), metric.getMetricHashKey());

            List<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);

            if (metricTimestampsAndValues != null) {
                metricTimestampsAndValues.add(metricTimestampAndValue);
            }
            else {
                metricTimestampsAndValues = Collections.synchronizedList(new ArrayList<MetricTimestampAndValue>());
                metricTimestampsAndValues.add(metricTimestampAndValue);
                GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.put(metricKey, metricTimestampsAndValues);
            }

            didDoAnyUpdates = true;
        }
        
        return didDoAnyUpdates;
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
