package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.alerts.MetricAssociation;
import com.pearson.statsagg.controller.thread_managers.SendMetricsToOutputModule_ThreadPoolManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbStandardizedMetric;
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
            
            // get metrics
            long getMetricsTimeStart = System.currentTimeMillis();
            List[] influxdbMetrics_OriginalAndStandardized = getCurrentInfluxdbMetricsAndRemoveMetricsFromGlobal();
            List<InfluxdbMetric_v1> influxdbMetrics = influxdbMetrics_OriginalAndStandardized[0];
            List<InfluxdbStandardizedMetric> influxdbStandardizedMetrics = influxdbMetrics_OriginalAndStandardized[1];
            long getMetricsTimeElasped = System.currentTimeMillis() - getMetricsTimeStart; 
            
            // returns a list of metric-keys that need to be disregarded by this routine.
            long forgetInfluxdbStandardizedMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            List<InfluxdbStandardizedMetric> influxdbStandardizedMetrics_RemovedForgottenMetrics = removeMetricKeysFromInfluxdbStandardizedMetricsList(influxdbStandardizedMetrics, metricKeysToForget);
            // todo -- support 'forgetting' InfluxdbMetric_v1 (influxdb native)
            long forgetInfluxdbStandardizedMetricsTimeElasped = System.currentTimeMillis() - forgetInfluxdbStandardizedMetricsTimeStart;  
            
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps(influxdbStandardizedMetrics_RemovedForgottenMetrics);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(influxdbStandardizedMetrics_RemovedForgottenMetrics);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // // make sure metric-keys that should be output-blacklist are blacklisted & remove output-blacklist metrics prior to outputting
            long outputBlacklistTimeStart = System.currentTimeMillis();
            long outputBlacklistNewlyProcessedMetricsCount = MetricAssociation.associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(threadId_, Common.getMetricKeysFromMetrics_List(influxdbStandardizedMetrics_RemovedForgottenMetrics));
            List<String> outputBlacklistMetricKeys = Common.getOutputBlacklistMetricKeys();
            if (outputBlacklistMetricKeys != null) metricKeysToForget.addAll(outputBlacklistMetricKeys);
            List<InfluxdbStandardizedMetric> influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics = removeMetricKeysFromInfluxdbStandardizedMetricsList(influxdbStandardizedMetrics_RemovedForgottenMetrics, metricKeysToForget);
            long outputBlacklistTimeElasped = System.currentTimeMillis() - outputBlacklistTimeStart; 
            
            // send metrics to output modules
            if (!influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics.isEmpty()) SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllGraphiteOutputModules(influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
            if (!influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics.isEmpty()) SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbTelnetOutputModules(influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
            if (!influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics.isEmpty()) SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbHttpOutputModules(influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
            if (!influxdbMetrics.isEmpty()) SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllInfluxdbV1HttpOutputModules_Native(influxdbMetrics, threadId_);
 
            // total time for this thread took to get & send the metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(influxdbStandardizedMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + influxdbStandardizedMetrics.size() 
                    + ", RawMetricRatePerSec=" + (influxdbStandardizedMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", OutputMetricCount=" + influxdbStandardizedMetrics_RemovedForgottenAndOutputBlacklistedMetrics.size() 
                    + ", MetricsProcessedPerSec=" + rate
                    + ", GetMetricsTime=" + getMetricsTimeElasped
                    + ", OutputBlacklistTime=" + outputBlacklistTimeElasped
                    + ", OutputBlacklistNewAssociationCount=" + outputBlacklistNewlyProcessedMetricsCount
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", ForgetMetricsTime=" + forgetInfluxdbStandardizedMetricsTimeElasped
                    ;
            
            if (influxdbStandardizedMetrics.isEmpty()) logger.debug(aggregationStatistics);
            else logger.info(aggregationStatistics);
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (InfluxdbStandardizedMetric influxdbStandardizedMetric : influxdbStandardizedMetrics_RemovedForgottenMetrics) {
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
    // in the returned list, [0] is the 'InfluxdbMetric_v1' version of the metrics, and [1] is the 'InfluxdbStandardizedMetric' version of the metrics
    private List[] getCurrentInfluxdbMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.influxdbV1Metrics == null) {
            return new List[2];
        }
        
        List[] influxdbMetrics_OriginalAndStandardized = new List[2];
        List<InfluxdbMetric_v1> influxdbMetrics = new ArrayList(GlobalVariables.influxdbV1Metrics.size());
        List<InfluxdbStandardizedMetric> influxdbStandardizedMetrics = new ArrayList(GlobalVariables.influxdbV1Metrics.size());
        influxdbMetrics_OriginalAndStandardized[0] = influxdbMetrics;
        influxdbMetrics_OriginalAndStandardized[1] = influxdbStandardizedMetrics;
        
        for (InfluxdbMetric_v1 influxdbMetric : GlobalVariables.influxdbV1Metrics.values()) {
            if (influxdbMetric.getMetricsReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                influxdbMetrics.add(influxdbMetric);
                GlobalVariables.influxdbV1Metrics.remove(influxdbMetric.getHashKey());
                
                if ((influxdbMetric.getInfluxdbStandardizedMetrics() == null) || influxdbMetric.getInfluxdbStandardizedMetrics().isEmpty()) continue;

                for (InfluxdbStandardizedMetric influxdbStandardizedMetric : influxdbMetric.getInfluxdbStandardizedMetrics()) {
                    influxdbStandardizedMetrics.add(influxdbStandardizedMetric);
                }
            }
        }

        return influxdbMetrics_OriginalAndStandardized;
    }
    
    public static List<InfluxdbStandardizedMetric> removeMetricKeysFromInfluxdbStandardizedMetricsList(List<InfluxdbStandardizedMetric> influxdbStandardizedMetrics, Set<String> metricKeysToRemove) {
        
        if ((influxdbStandardizedMetrics == null) || influxdbStandardizedMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return influxdbStandardizedMetrics;
        }
        
        List<InfluxdbStandardizedMetric> influxdbStandardizedMetrics_WithMetricsRemoved = new ArrayList<>(influxdbStandardizedMetrics.size());

        for (InfluxdbStandardizedMetric influxdbStandardizedMetric : influxdbStandardizedMetrics) {
            String metricKey = influxdbStandardizedMetric.getMetricKey();
            
            if ((metricKey != null) && !metricKeysToRemove.contains(metricKey)) {
                influxdbStandardizedMetrics_WithMetricsRemoved.add(influxdbStandardizedMetric);
            }
        }

        return influxdbStandardizedMetrics_WithMetricsRemoved;
    }
    
}