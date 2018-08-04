package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.alerts.MetricAssociation;
import com.pearson.statsagg.controller.thread_managers.SendMetricsToOutputModule_ThreadPoolManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphitePassthroughThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GraphitePassthroughThread.class.getName());
    
    // Lists of active aggregation thread 'thread-start' timestamps. Used as a hacky mechanism for thread blocking on the aggregation threads. 
    private final static List<Long> activeGraphitePassthroughThreadStartGetMetricsTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    
    private final Long threadStartTimestampInMilliseconds_;
    private final String threadId_;
    
    public GraphitePassthroughThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "GP-" + threadStartTimestampInMilliseconds_.toString();
    }

    @Override
    public void run() {
        
        if (threadStartTimestampInMilliseconds_ == null) {
            logger.error(this.getClass().getName() + " has invalid initialization value(s)");
            return;
        }
        
        long threadTimeStart = System.currentTimeMillis();
        
        boolean isSuccessfulAdd = activeGraphitePassthroughThreadStartGetMetricsTimestamps.add(threadStartTimestampInMilliseconds_);
        if (!isSuccessfulAdd) {
            logger.error("There is another active thread of type '" + this.getClass().getName() + "' with the same thread start timestamp. Killing this thread...");
            return;
        }
        
        try {  
            // wait until this is the youngest active thread
            int waitInMsCounter = Common.waitUntilThisIsYoungestActiveThread(threadStartTimestampInMilliseconds_, activeGraphitePassthroughThreadStartGetMetricsTimestamps);
            activeGraphitePassthroughThreadStartGetMetricsTimestamps.remove(threadStartTimestampInMilliseconds_);

            // get metrics
            long getMetricsTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetrics = getCurrentGraphitePassthroughMetricsAndRemoveMetricsFromGlobal();
            long getMetricsTimeElasped = System.currentTimeMillis() - getMetricsTimeStart; 

            // gets a list of metric-keys that need to be disregarded by this routine & removes them
            long forgetGraphiteMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            List<GraphiteMetric> graphiteMetrics_RemovedForgottenMetrics = Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetrics, metricKeysToForget);
            long forgetGraphiteMetricsTimeElasped = System.currentTimeMillis() - forgetGraphiteMetricsTimeStart;  
            
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps(graphiteMetrics_RemovedForgottenMetrics);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(graphiteMetrics_RemovedForgottenMetrics);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 

            // // make sure metric-keys that should be output-blacklist are blacklisted & remove output-blacklist metrics prior to outputting
            long outputBlacklistTimeStart = System.currentTimeMillis();
            long outputBlacklistNewlyProcessedMetricsCount = MetricAssociation.associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(threadId_, Common.getMetricKeysFromMetrics_List(graphiteMetrics_RemovedForgottenMetrics));
            List<String> outputBlacklistMetricKeys = Common.getOutputBlacklistMetricKeys();
            if (outputBlacklistMetricKeys != null) metricKeysToForget.addAll(outputBlacklistMetricKeys);
            List<GraphiteMetric> graphiteMetrics_RemovedForgottenAndOutputBlacklistedMetrics = Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetrics_RemovedForgottenMetrics, metricKeysToForget);
            long outputBlacklistTimeElasped = System.currentTimeMillis() - outputBlacklistTimeStart; 
            
            // send to metrics output modules
            if (!graphiteMetrics_RemovedForgottenAndOutputBlacklistedMetrics.isEmpty()) {
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllGraphiteOutputModules(graphiteMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbTelnetOutputModules(graphiteMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbHttpOutputModules(graphiteMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllInfluxdbV1HttpOutputModules_NonNative(graphiteMetrics_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
            }
            
            // total time for this thread took to get & send the metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(graphiteMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + graphiteMetrics.size() 
                    + ", RawMetricRatePerSec=" + (graphiteMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", OutputMetricCount=" + graphiteMetrics_RemovedForgottenAndOutputBlacklistedMetrics.size() 
                    + ", MetricsProcessedPerSec=" + rate
                    + ", GetMetricsTime=" + getMetricsTimeElasped
                    + ", OutputBlacklistTime=" + outputBlacklistTimeElasped
                    + ", OutputBlacklistNewAssociationCount=" + outputBlacklistNewlyProcessedMetricsCount
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", ForgetMetricsTime=" + forgetGraphiteMetricsTimeElasped
                    ;
            
            if (graphiteMetrics.isEmpty()) logger.debug(aggregationStatistics);
            else logger.info(aggregationStatistics);
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (GraphiteMetric graphiteMetric : graphiteMetrics_RemovedForgottenMetrics) {
                    logger.info("Graphite metric= " + graphiteMetric.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    // gets graphite metrics for this thread 
    // also removes metrics from the global graphite passthrough metrics map (since they are being operated on by this thread)
    private List<GraphiteMetric> getCurrentGraphitePassthroughMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.graphitePassthroughMetrics == null) {
            return new ArrayList();
        }

        List<GraphiteMetric> graphiteMetrics = new ArrayList(GlobalVariables.graphitePassthroughMetrics.size());
        
        for (GraphiteMetric graphiteMetric : GlobalVariables.graphitePassthroughMetrics.values()) {
            if (graphiteMetric.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                graphiteMetrics.add(graphiteMetric);
                GlobalVariables.graphitePassthroughMetrics.remove(graphiteMetric.getHashKey());
            }
        }
        
        return graphiteMetrics;
    }

}
