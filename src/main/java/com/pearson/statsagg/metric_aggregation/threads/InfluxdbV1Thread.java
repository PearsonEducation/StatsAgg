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
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbStandardizedMetric_v1;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbV1Thread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InfluxdbV1Thread.class.getName());
    
    // Lists of active aggregation thread 'thread-start' timestamps. Used as a hacky mechanism for thread blocking on the aggregation threads. 
    private final static List<Long> activeInfluxdbThreadStartGetMetricsTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    
    private final Long threadStartTimestampInMilliseconds_;
    private final String threadId_;
    
    public InfluxdbV1Thread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "IDB-" + threadStartTimestampInMilliseconds_.toString();
    }

    @Override
    public void run() {
        
        if (threadStartTimestampInMilliseconds_ == null) {
            logger.error(this.getClass().getName() + " has invalid initialization value(s)");
            return;
        }
        
        long threadTimeStart = System.currentTimeMillis();
        
        boolean isSuccessfulAdd = activeInfluxdbThreadStartGetMetricsTimestamps.add(threadStartTimestampInMilliseconds_);
        if (!isSuccessfulAdd) {
            logger.error("There is another active thread of type '" + this.getClass().getName() + "' with the same thread start timestamp. Killing this thread...");
            return;
        }
        
        try {  
            // wait until this is the youngest active thread
            int waitInMsCounter = Common.waitUntilThisIsYoungestActiveThread(threadStartTimestampInMilliseconds_, activeInfluxdbThreadStartGetMetricsTimestamps);
            activeInfluxdbThreadStartGetMetricsTimestamps.remove(threadStartTimestampInMilliseconds_);
            
            // returns a list of metric-keys that need to be disregarded by this routine.
            long forgetInfluxdbStandardizedMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            long forgetInfluxdbStandardizedMetricsTimeElasped = System.currentTimeMillis() - forgetInfluxdbStandardizedMetricsTimeStart;  
            
            // get metrics for aggregation, then remove any aggregated metrics that need to be 'forgotten'
            long createMetricsTimeStart = System.currentTimeMillis();
            List[] influxdbMetrics_OriginalAndStandardized = getCurrentInfluxdbMetricsAndRemoveMetricsFromGlobal();
            List<InfluxdbMetric_v1> influxdbMetrics = influxdbMetrics_OriginalAndStandardized[0];
            List<InfluxdbStandardizedMetric_v1> influxdbStandardizedMetrics = influxdbMetrics_OriginalAndStandardized[1];
            removeMetricKeysFromInfluxdbStandardizedMetricsList(influxdbStandardizedMetrics, metricKeysToForget);
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
                    
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(influxdbStandardizedMetrics);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(influxdbStandardizedMetrics);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // send metrics to output modules
            SendToGraphiteThreadPoolManager.sendMetricsToAllGraphiteOutputModules(influxdbStandardizedMetrics, threadId_);
            SendToOpenTsdbThreadPoolManager.sendMetricsToAllOpenTsdbTelnetOutputModules(influxdbStandardizedMetrics, false, threadId_);
            SendToOpenTsdbThreadPoolManager.sendMetricsToAllOpenTsdbHttpOutputModules(influxdbStandardizedMetrics, false, threadId_);
            SendToInfluxdbV1ThreadPoolManager.sendMetricsToAllInfluxdbHttpOutputModules_Native(influxdbMetrics, threadId_);
                        
            // total time for this thread took to get & send the metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(influxdbStandardizedMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + influxdbStandardizedMetrics.size() 
                    + ", RawMetricRatePerSec=" + (influxdbStandardizedMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", MetricsProcessedPerSec=" + rate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", ForgetMetricsTime=" + forgetInfluxdbStandardizedMetricsTimeElasped
                    ;
            
            if (influxdbStandardizedMetrics.isEmpty()) logger.debug(aggregationStatistics);
            else logger.info(aggregationStatistics);
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (InfluxdbStandardizedMetric_v1 influxdbStandardizedMetric : influxdbStandardizedMetrics) {
                    logger.info("InfluxDB metric= " + influxdbStandardizedMetric.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    // gets influxdb metrics for this thread 
    // also removes metrics from the global influxdb metrics map (since they are being operated on by this thread)
    // in the returned list, [0] is the 'InfluxdbMetric_v1' version of the metrics, and [1] is the 'InfluxdbStandardizedMetric_v1' version of the metrics
    private List[] getCurrentInfluxdbMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.influxdbV1Metrics == null) {
            return new List[2];
        }
        
        List[] influxdbMetrics_OriginalAndStandardized = new List[2];
        List<InfluxdbMetric_v1> influxdbMetrics = new ArrayList(GlobalVariables.influxdbV1Metrics.size());
        List<InfluxdbStandardizedMetric_v1> influxdbStandardizedMetrics = new ArrayList(GlobalVariables.influxdbV1Metrics.size());
        influxdbMetrics_OriginalAndStandardized[0] = influxdbMetrics;
        influxdbMetrics_OriginalAndStandardized[1] = influxdbStandardizedMetrics;
        
        for (InfluxdbMetric_v1 influxdbMetric : GlobalVariables.influxdbV1Metrics.values()) {
            if (influxdbMetric.getMetricsReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                influxdbMetrics.add(influxdbMetric);
                GlobalVariables.influxdbV1Metrics.remove(influxdbMetric.getHashKey());
                
                if ((influxdbMetric.getInfluxdbStandardizedMetrics() == null) || influxdbMetric.getInfluxdbStandardizedMetrics().isEmpty()) continue;

                for (InfluxdbStandardizedMetric_v1 influxdbStandardizedMetric : influxdbMetric.getInfluxdbStandardizedMetrics()) {
                    influxdbStandardizedMetrics.add(influxdbStandardizedMetric);
                }
            }
        }

        return influxdbMetrics_OriginalAndStandardized;
    }
    
    public static void removeMetricKeysFromInfluxdbStandardizedMetricsList(List<InfluxdbStandardizedMetric_v1> influxdbStandardizedMetrics, Set<String> metricKeysToRemove) {
        
        if ((influxdbStandardizedMetrics == null) || influxdbStandardizedMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return;
        }
        
        Map<String,InfluxdbStandardizedMetric_v1> metricsMap = new HashMap<>();
        
        for (InfluxdbStandardizedMetric_v1 influxdbStandardizedMetric : influxdbStandardizedMetrics) {
            String metricKey = influxdbStandardizedMetric.getMetricKey();
            if (metricKey != null) metricsMap.put(metricKey, influxdbStandardizedMetric);
        }
                
        for (String metricKeyToRemove : metricKeysToRemove) {
            Object metric = metricsMap.get(metricKeyToRemove);
            if (metric != null) metricsMap.remove(metricKeyToRemove);
        }
        
        influxdbStandardizedMetrics.clear();
        influxdbStandardizedMetrics.addAll(metricsMap.values());
    }
    
}
