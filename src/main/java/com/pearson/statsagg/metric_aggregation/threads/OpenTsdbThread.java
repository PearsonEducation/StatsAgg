package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.alerts.MetricAssociation;
import com.pearson.statsagg.controller.thread_managers.SendMetricsToOutputModule_ThreadPoolManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            
            // get metrics
            long getMetricsTimeStart = System.currentTimeMillis();
            List<OpenTsdbMetric> openTsdbMetrics = getCurrentOpenTsdbMetricsAndRemoveMetricsFromGlobal();
            long getMetricsTimeElasped = System.currentTimeMillis() - getMetricsTimeStart; 

            // gets a list of metric-keys that need to be disregarded by this routine & removes them
            long forgetOpenTsdbMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            List<OpenTsdbMetric> openTsdbMetrics_RemovedForgottenMetrics = removeMetricKeysFromOpenTsdbMetricsList(openTsdbMetrics, metricKeysToForget);
            long forgetOpenTsdbMetricsTimeElasped = System.currentTimeMillis() - forgetOpenTsdbMetricsTimeStart;  
            
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps(openTsdbMetrics_RemovedForgottenMetrics);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(openTsdbMetrics_RemovedForgottenMetrics);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // // make sure metric-keys that should be output-blacklist are blacklisted & remove output-blacklist metrics prior to outputting
            long outputBlacklistTimeStart = System.currentTimeMillis();
            long outputBlacklistNewlyProcessedMetricsCount = MetricAssociation.associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(threadId_, Common.getMetricKeysFromMetrics_List(openTsdbMetrics_RemovedForgottenMetrics));
            List<String> outputBlacklistMetricKeys = Common.getOutputBlacklistMetricKeys();
            if (outputBlacklistMetricKeys != null) metricKeysToForget.addAll(outputBlacklistMetricKeys);
            List<OpenTsdbMetric> openTsdbMetrics_RemovedForgottenAndOutputBlacklistedMetrics = removeMetricKeysFromOpenTsdbMetricsList(openTsdbMetrics_RemovedForgottenMetrics, metricKeysToForget);
            long outputBlacklistTimeElasped = System.currentTimeMillis() - outputBlacklistTimeStart; 
            
            // send metrics to output modules
            if (!openTsdbMetrics_RemovedForgottenAndOutputBlacklistedMetrics.isEmpty()) {
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllGraphiteOutputModules(openTsdbMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbTelnetOutputModules(openTsdbMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbHttpOutputModules(openTsdbMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllInfluxdbV1HttpOutputModules_NonNative(openTsdbMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
            }
            
            // total time for this thread took to get & send the metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(openTsdbMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + openTsdbMetrics.size() 
                    + ", RawMetricRatePerSec=" + (openTsdbMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", OutputMetricCount=" + openTsdbMetrics_RemovedForgottenAndOutputBlacklistedMetrics.size() 
                    + ", MetricsProcessedPerSec=" + rate
                    + ", GetMetricsTime=" + getMetricsTimeElasped
                    + ", OutputBlacklistTime=" + outputBlacklistTimeElasped
                    + ", OutputBlacklistNewAssociationCount=" + outputBlacklistNewlyProcessedMetricsCount
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", ForgetMetricsTime=" + forgetOpenTsdbMetricsTimeElasped
                    ;
            
            if (openTsdbMetrics.isEmpty()) logger.debug(aggregationStatistics);
            else logger.info(aggregationStatistics);
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (OpenTsdbMetric openTsdbMetric : openTsdbMetrics_RemovedForgottenMetrics) {
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
    
    public static List<OpenTsdbMetric> removeMetricKeysFromOpenTsdbMetricsList(List<OpenTsdbMetric> openTsdbMetrics, Set<String> metricKeysToRemove) {
        
        if ((openTsdbMetrics == null) || openTsdbMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return openTsdbMetrics;
        }
        
        List<OpenTsdbMetric> openTsdbMetrics_WithMetricsRemoved = new ArrayList<>(openTsdbMetrics.size());

        for (OpenTsdbMetric openTsdbMetric : openTsdbMetrics) {
            String metricKey = openTsdbMetric.getMetricKey();
            
            if ((metricKey != null) && !metricKeysToRemove.contains(metricKey)) {
                openTsdbMetrics_WithMetricsRemoved.add(openTsdbMetric);
            }
        }

        return openTsdbMetrics_WithMetricsRemoved;
    }
    
}
