package com.pearson.statsagg.metric_aggregation.threads;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.Threads;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

            Set<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);

            if (metricTimestampsAndValues != null) {
                synchronized (metricTimestampsAndValues) {
                    if (!metricTimestampsAndValues.contains(metricTimestampAndValue)) {
                        metricTimestampsAndValues.add(metricTimestampAndValue);
                    }
                }
            }
            else {
                metricTimestampsAndValues = Collections.synchronizedSet(new HashSet<MetricTimestampAndValue>());
                metricTimestampsAndValues.add(metricTimestampAndValue);
                GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.put(metricKey, metricTimestampsAndValues);
            }

            didDoAnyUpdates = true;
        }
        
        return didDoAnyUpdates;
    }
    
    public static void forgetGenericMetrics(ConcurrentHashMap<String,String> forgetGenericMetrics, 
            ConcurrentHashMap<String,String> forgetGenericMetricsRegexs,
            ConcurrentHashMap<String,? extends GenericMetricFormat> genericMetricsMostRecentValue,
            ConcurrentHashMap<String,String> immediateCleanupMetrics) {

        HashSet<String> metricKeysToForget = new HashSet<>();
        
        // gets a list of complete metric keys to forget
        if (forgetGenericMetrics != null) {         
            Set<String> forgetMetrics = new HashSet<>(forgetGenericMetrics.keySet());
            
            for (String metricPath : forgetMetrics) {
                metricKeysToForget.add(metricPath);
                forgetGenericMetrics.remove(metricPath);
            }
        }
        
        // gets a list of metric keys to forget by matching against regexs
        if (forgetGenericMetricsRegexs != null) {      
            Set<String> forgetGenericMetricsKeyRegexs_Keys = new HashSet<>(forgetGenericMetricsRegexs.keySet());
            
            for (String metricKeyRegex : forgetGenericMetricsKeyRegexs_Keys) {
                Set<String> regexMetricKeysToForget = Common.forgetGenericMetrics_IdentifyMetricPathsViaRegex(metricKeyRegex, genericMetricsMostRecentValue);
                
                if (regexMetricKeysToForget != null) {
                    metricKeysToForget.addAll(regexMetricKeysToForget);
                }
                
                forgetGenericMetricsRegexs.remove(metricKeyRegex);
            }
        }
        
        // 'forgets' the metrics
        if (!metricKeysToForget.isEmpty()) {
            forgetGenericMetrics_Forget(metricKeysToForget, genericMetricsMostRecentValue, immediateCleanupMetrics);
        }
        
    }
    
    public static Set<String> forgetGenericMetrics_IdentifyMetricPathsViaRegex(String regex, ConcurrentHashMap<String,? extends GenericMetricFormat> metricsMostRecentValue) {
        
        if ((regex == null) || regex.isEmpty() || (metricsMostRecentValue == null)) {
            return new HashSet<>();
        }
        
        Set<String> metricKeysToForget = new HashSet<>();
        
        try {
            Pattern pattern = null;

            try {
                pattern = Pattern.compile(regex);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        
            if (pattern != null) {
                for (GenericMetricFormat genericMetric : metricsMostRecentValue.values()) {
                    Matcher matcher = pattern.matcher(genericMetric.getMetricKey());

                    if (matcher.matches()) {
                        metricKeysToForget.add(genericMetric.getMetricKey());
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return metricKeysToForget;
    }
    
    public static void forgetGenericMetrics_Forget(Set<String> metricKeysToForget, 
            ConcurrentHashMap<String,? extends GenericMetricFormat> metricsMostRecentValue,
            ConcurrentHashMap<String,String> immediateCleanupMetrics) {
        
        if ((metricKeysToForget == null) || metricKeysToForget.isEmpty() || (metricsMostRecentValue == null)) {
            return;
        }
            
        for (String metricKeyToForget : metricKeysToForget) {
            immediateCleanupMetrics.put(metricKeyToForget, metricKeyToForget);
            metricsMostRecentValue.remove(metricKeyToForget);
        }
        
    }
    
}
