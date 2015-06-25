package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.controller.threads.SendToGraphiteThreadPoolManager;
import com.pearson.statsagg.controller.threads.SendToInfluxdbV1ThreadPoolManager;
import com.pearson.statsagg.controller.threads.SendToOpenTsdbThreadPoolManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbThread.class.getName());
    
    // Lists of active aggregation thread 'thread-start' timestamps. Used as a hacky mechanism for thread blocking on the aggregation threads. 
    private final static List<Long> activeOpenTsdbThreadStartGetMetricsTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    
    private final Long threadStartTimestampInMilliseconds_;
    private final String threadId_;
    
    public OpenTsdbThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "OTSDB-" + threadStartTimestampInMilliseconds_.toString();
    }

    @Override
    public void run() {
        
        if (threadStartTimestampInMilliseconds_ == null) {
            logger.error(this.getClass().getName() + " has invalid initialization value(s)");
            return;
        }
        
        long threadTimeStart = System.currentTimeMillis();
        
        boolean isSuccessfulAdd = activeOpenTsdbThreadStartGetMetricsTimestamps.add(threadStartTimestampInMilliseconds_);
        if (!isSuccessfulAdd) {
            logger.error("There is another active thread of type '" + this.getClass().getName() + "' with the same thread start timestamp. Killing this thread...");
            return;
        }
        
        try {  
            // wait until this is the youngest active thread
            int waitInMsCounter = Common.waitUntilThisIsYoungestActiveThread(threadStartTimestampInMilliseconds_, activeOpenTsdbThreadStartGetMetricsTimestamps);
            activeOpenTsdbThreadStartGetMetricsTimestamps.remove(threadStartTimestampInMilliseconds_);
            
            // returns a list of metric-keys that need to be disregarded by this routine.
            long forgetOpenTsdbMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            long forgetOpenTsdbMetricsTimeElasped = System.currentTimeMillis() - forgetOpenTsdbMetricsTimeStart;  
            
            // get metrics for aggregation, then remove any aggregated metrics that need to be 'forgotten'
            long createMetricsTimeStart = System.currentTimeMillis();
            List<OpenTsdbMetric> openTsdbMetrics = getCurrentOpenTsdbMetricsAndRemoveMetricsFromGlobal();
            removeMetricKeysFromOpenTsdbMetricsList(openTsdbMetrics, metricKeysToForget);
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
  
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(openTsdbMetrics);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(openTsdbMetrics);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // send metrics to output modules
            SendToGraphiteThreadPoolManager.sendMetricsToAllGraphiteOutputModules(openTsdbMetrics, threadId_);
            SendToOpenTsdbThreadPoolManager.sendMetricsToAllOpenTsdbTelnetOutputModules(openTsdbMetrics, false, threadId_);
            SendToOpenTsdbThreadPoolManager.sendMetricsToAllOpenTsdbHttpOutputModules(openTsdbMetrics, false, threadId_);
            SendToInfluxdbV1ThreadPoolManager.sendMetricsToAllInfluxdbHttpOutputModules_NonNative(openTsdbMetrics, threadId_);
            
            // total time for this thread took to get & send the metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(openTsdbMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + openTsdbMetrics.size() 
                    + ", RawMetricRatePerSec=" + (openTsdbMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", MetricsProcessedPerSec=" + rate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", ForgetMetricsTime=" + forgetOpenTsdbMetricsTimeElasped
                    ;
            
            if (openTsdbMetrics.isEmpty()) logger.debug(aggregationStatistics);
            else logger.info(aggregationStatistics);
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (OpenTsdbMetric openTsdbMetric : openTsdbMetrics) {
                    logger.info("OpenTsdb metric= " + openTsdbMetric.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    // gets opentsdb metrics for this thread 
    // also removes metrics from the global opentsdb metrics map (since they are being operated on by this thread)
    private List<OpenTsdbMetric> getCurrentOpenTsdbMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.openTsdbMetrics == null) {
            return new ArrayList();
        }

        List<OpenTsdbMetric> openTsdbMetrics = new ArrayList(GlobalVariables.openTsdbMetrics.size());
        
        for (OpenTsdbMetric openTsdbMetric : GlobalVariables.openTsdbMetrics.values()) {
            if (openTsdbMetric.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                openTsdbMetrics.add(openTsdbMetric);
                GlobalVariables.openTsdbMetrics.remove(openTsdbMetric.getHashKey());
            }
        }

        return openTsdbMetrics;
    }

    public static void removeMetricKeysFromOpenTsdbMetricsList(List<OpenTsdbMetric> openTsdbMetrics, Set<String> metricKeysToRemove) {
        
        if ((openTsdbMetrics == null) || openTsdbMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return;
        }
        
        Map<String,OpenTsdbMetric> metricsMap = new HashMap<>();
        
        for (OpenTsdbMetric openTsdbMetric : openTsdbMetrics) {
            String metricKey = openTsdbMetric.getMetricKey();
            if (metricKey != null) metricsMap.put(metricKey, openTsdbMetric);
        }
                
        for (String metricKeyToRemove : metricKeysToRemove) {
            Object metric = metricsMap.get(metricKeyToRemove);
            if (metric != null) metricsMap.remove(metricKeyToRemove);
        }
        
        openTsdbMetrics.clear();
        openTsdbMetrics.addAll(metricsMap.values());
    }
    
}
