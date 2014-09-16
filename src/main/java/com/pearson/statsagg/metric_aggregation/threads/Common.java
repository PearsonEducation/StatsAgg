package com.pearson.statsagg.metric_aggregation.threads;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.Threads;
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
            GlobalVariables.metricKeysLastSeenTimestamp.put(metricKey, metric.getMetricReceivedTimestampInMilliseconds());
        }
        
    }

    public static boolean updateAlertMetricRecentValues(List<? extends GenericMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return false;
        }
        
        boolean didDoAnyUpdates = false;
        
        for (GenericMetricFormat metric : metrics) {
            String metricKey = metric.getMetricKey();

            synchronized (GlobalVariables.recentMetricTimestampsAndValuesByMetricKey) {
                Set<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);

                if (metricTimestampsAndValues != null) {
                    synchronized (metricTimestampsAndValues) {
                        MetricTimestampAndValue metricTimestampAndValue = new MetricTimestampAndValue(
                                metric.getMetricTimestampInMilliseconds(), metric.getMetricValueBigDecimal(), metric.getMetricHashKey());

                        if (!metricTimestampsAndValues.contains(metricTimestampAndValue)) {
                            metricTimestampsAndValues.add(metricTimestampAndValue);
                        }
                    }
                }
                else {
                    metricTimestampsAndValues = Collections.synchronizedSet(new TreeSet<>(MetricTimestampAndValue.COMPARE_BY_TIMESTAMP));
                    MetricTimestampAndValue metricTimestampAndValue = new MetricTimestampAndValue(
                            metric.getMetricTimestampInMilliseconds(), metric.getMetricValueBigDecimal(), metric.getMetricHashKey());

                    metricTimestampsAndValues.add(metricTimestampAndValue);
                    GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.put(metricKey, metricTimestampsAndValues);
                }
                
                didDoAnyUpdates = true;
            }
        }
        
        return didDoAnyUpdates;
    }

}
