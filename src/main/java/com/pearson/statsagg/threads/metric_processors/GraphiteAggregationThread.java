package com.pearson.statsagg.threads.metric_processors;

import com.pearson.statsagg.threads.alert_related.MetricAssociation;
import com.pearson.statsagg.threads.thread_managers.SendMetricsToOutputModule_ThreadPoolManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricAggregator;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteAggregationThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteAggregationThread.class.getName());

    // Lists of active aggregation thread 'thread-start' timestamps. Used as a hacky mechanism for thread blocking on the aggregation threads. 
    private final static List<Long> activeGraphiteAggregationThreadStartGetMetricsTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    
    private final Long threadStartTimestampInMilliseconds_;
    private final String threadId_;
    
    public GraphiteAggregationThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "GA-" + threadStartTimestampInMilliseconds_.toString();
    }

    @Override
    public void run() {
        
        if (threadStartTimestampInMilliseconds_ == null) {
            logger.error(this.getClass().getName() + " has invalid initialization value(s)");
            return;
        }
        
        long timeAggregationTimeStart = System.currentTimeMillis();
        
        boolean isSuccessfulAdd = activeGraphiteAggregationThreadStartGetMetricsTimestamps.add(threadStartTimestampInMilliseconds_);
        if (!isSuccessfulAdd) {
            logger.error("There is another active thread of type '" + this.getClass().getName() + "' with the same thread start timestamp. Killing this thread...");
            return;
        }
            
        try {  
            // wait until this is the youngest active thread
            int waitInMsCounter = Common.waitUntilThisIsYoungestActiveThread(threadStartTimestampInMilliseconds_, activeGraphiteAggregationThreadStartGetMetricsTimestamps);
            activeGraphiteAggregationThreadStartGetMetricsTimestamps.remove(threadStartTimestampInMilliseconds_);

            // get metrics
            long getMetricsTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetrics = getCurrentGraphiteAggregatorMetricsAndRemoveMetricsFromGlobal();
            long getMetricsTimeElasped = System.currentTimeMillis() - getMetricsTimeStart; 
            
            // aggregate graphite metrics
            long aggregateTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetricsAggregated = GraphiteMetricAggregator.aggregateGraphiteMetrics(graphiteMetrics);
            long aggregateTimeElasped = System.currentTimeMillis() - aggregateTimeStart; 

            // gets a list of metric-keys that need to be disregarded by this routine & removes them
            long forgetGraphiteMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            List<GraphiteMetric> graphiteMetricsAggregated_RemovedForgottenMetrics = Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetricsAggregated, metricKeysToForget);
            long forgetGraphiteMetricsTimeElasped = System.currentTimeMillis() - forgetGraphiteMetricsTimeStart; 
            
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps(graphiteMetricsAggregated_RemovedForgottenMetrics);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(graphiteMetricsAggregated_RemovedForgottenMetrics);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // // make sure metric-keys that should be output-blacklist are blacklisted & remove output-blacklist metrics prior to outputting
            long outputBlacklistTimeStart = System.currentTimeMillis();
            long outputBlacklistNewlyProcessedMetricsCount = MetricAssociation.associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(threadId_, Common.getMetricKeysFromMetrics_List(graphiteMetricsAggregated_RemovedForgottenMetrics));
            List<String> outputBlacklistMetricKeys = Common.getOutputBlacklistMetricKeys();
            if (outputBlacklistMetricKeys != null) metricKeysToForget.addAll(outputBlacklistMetricKeys);
            List<GraphiteMetric> graphiteMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics = Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetricsAggregated_RemovedForgottenMetrics, metricKeysToForget);
            long outputBlacklistTimeElasped = System.currentTimeMillis() - outputBlacklistTimeStart; 

            // send to metrics to output modules
            if (!graphiteMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics.isEmpty()) {
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllGraphiteOutputModules(graphiteMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbTelnetOutputModules(graphiteMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbHttpOutputModules(graphiteMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllInfluxdbV1HttpOutputModules_NonNative(graphiteMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
            }
            
            // total time for this thread took to aggregate the metrics
            long timeAggregationTimeElasped = System.currentTimeMillis() - timeAggregationTimeStart - waitInMsCounter;
            String aggregationRate = "0";
            if (timeAggregationTimeElasped > 0) aggregationRate = Long.toString(graphiteMetrics.size() / timeAggregationTimeElasped * 1000);
                    
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + timeAggregationTimeElasped 
                    + ", RawMetricCount=" + graphiteMetrics.size() 
                    + ", RawMetricRatePerSec=" + (graphiteMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", AggMetricCount=" + graphiteMetricsAggregated.size() 
                    + ", OutputMetricCount=" + graphiteMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics.size() 
                    + ", MetricsProcessedPerSec=" + aggregationRate
                    + ", GetMetricsTime=" + getMetricsTimeElasped 
                    + ", AggTime=" + aggregateTimeElasped 
                    + ", OutputBlacklistTime=" + outputBlacklistTimeElasped
                    + ", OutputBlacklistNewAssociationCount=" + outputBlacklistNewlyProcessedMetricsCount
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", ForgetMetricsTime=" + forgetGraphiteMetricsTimeElasped;
            
            if (graphiteMetricsAggregated.isEmpty()) logger.debug(aggregationStatistics);
            else logger.info(aggregationStatistics);

            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (GraphiteMetric graphiteMetricAggregated : graphiteMetricsAggregated_RemovedForgottenMetrics) {
                    logger.info("Graphite aggregated metric= " + graphiteMetricAggregated.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    // gets graphite metrics for this thread to aggregate
    // also removes metrics from the global graphite aggregator metrics map (since they are being operated on by this thread)
    private List<GraphiteMetric> getCurrentGraphiteAggregatorMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.graphiteAggregatorMetrics == null) {
            return new ArrayList();
        }

        List<GraphiteMetric> graphiteMetrics = new ArrayList(GlobalVariables.graphiteAggregatorMetrics.size());
        
        for (GraphiteMetric graphiteMetric : GlobalVariables.graphiteAggregatorMetrics.values()) {
            if (graphiteMetric.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                graphiteMetrics.add(graphiteMetric);
                GlobalVariables.graphiteAggregatorMetrics.remove(graphiteMetric.getHashKey());
            }
        }

        return graphiteMetrics;
    }

}
