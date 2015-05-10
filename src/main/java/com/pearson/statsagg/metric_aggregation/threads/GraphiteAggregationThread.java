package com.pearson.statsagg.metric_aggregation.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricAggregator;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetric;
import com.pearson.statsagg.utilities.StackTrace;
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
            
            // returns a list of metric-keys that need to be disregarded by this routine.
            long forgetGraphiteMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            long forgetGraphiteMetricsTimeElasped = System.currentTimeMillis() - forgetGraphiteMetricsTimeStart;  
            
            // get metrics for aggregation, then remove any aggregated metrics that need to be 'forgotten'
            long createMetricsTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetrics = getCurrentGraphiteAggregatorMetricsAndRemoveMetricsFromGlobal();
            Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetrics, metricKeysToForget);
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
            
            // aggregate graphite metrics
            long aggregateTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetricsAggregated = GraphiteMetricAggregator.aggregateGraphiteMetrics(graphiteMetrics);
            long aggregateTimeElasped = System.currentTimeMillis() - aggregateTimeStart; 
            
            // update the global lists of the graphite aggregator's most recent aggregated values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(graphiteMetricsAggregated);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current aggregated values with the previous aggregated window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetricsAggregatedMerged = mergePreviouslyAggregatedValuesWithCurrentAggregatedValues(graphiteMetricsAggregated, GlobalVariables.graphiteAggregatedMetricsMostRecentValue);
            Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetricsAggregatedMerged, metricKeysToForget);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 
            
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            if (ApplicationConfiguration.isGraphiteAggregatorSendPreviousValue()) {
                Common.updateMetricLastSeenTimestamps_MostRecentNew(graphiteMetricsAggregated);
                Common.updateMetricLastSeenTimestamps_UpdateOnResend(graphiteMetricsAggregatedMerged);
            }
            else Common.updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(graphiteMetricsAggregatedMerged);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(graphiteMetricsAggregatedMerged);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 

            // send to graphite
            if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) {
                SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(graphiteMetricsAggregatedMerged, threadId_, ApplicationConfiguration.getFlushTimeAgg());
            }
            
            // send to opentsdb via telnet
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbTelnetOutputModuleEnabled()) {
                SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbTelnetEndpoints(graphiteMetricsAggregatedMerged, threadId_);
            }
            
            // total time for this thread took to aggregate the graphite metrics
            long timeAggregationTimeElasped = System.currentTimeMillis() - timeAggregationTimeStart - waitInMsCounter;
            String aggregationRate = "0";
            if (timeAggregationTimeElasped > 0) aggregationRate = Long.toString(graphiteMetrics.size() / timeAggregationTimeElasped * 1000);
                    
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + timeAggregationTimeElasped 
                    + ", RawMetricCount=" + graphiteMetrics.size() 
                    + ", RawMetricRatePerSec=" + (graphiteMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", AggMetricCount=" + graphiteMetricsAggregated.size() 
                    + ", MetricsProcessedPerSec=" + aggregationRate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped 
                    + ", AggTime=" + aggregateTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped
                    + ", AggNewAndOldMetricCount=" + graphiteMetricsAggregatedMerged.size()
                    + ", ForgetMetricsTime=" + forgetGraphiteMetricsTimeElasped;
            
            if (graphiteMetricsAggregatedMerged.isEmpty()) {
                logger.debug(aggregationStatistics);
            }
            else {
                logger.info(aggregationStatistics);
            }

            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (GraphiteMetric graphiteMetricAggregated : graphiteMetricsAggregatedMerged) {
                    logger.info("Graphite aggregated metric= " + graphiteMetricAggregated.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
        
    private List<GraphiteMetric> getCurrentGraphiteAggregatorMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.graphiteAggregatorMetrics == null) {
            return new ArrayList();
        }

        // gets graphite aggregator metrics for this thread to aggregate & send to graphite
        List<GraphiteMetric> graphiteMetrics = new ArrayList(GlobalVariables.graphiteAggregatorMetrics.size());
        
        for (GraphiteMetric graphiteMetric : GlobalVariables.graphiteAggregatorMetrics.values()) {
            if (graphiteMetric.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                graphiteMetrics.add(graphiteMetric);
            }
        }
        
        // removes metrics from the global graphite aggregator metrics map (since they are being operated on by this thread)
        for (GraphiteMetric graphiteMetric : graphiteMetrics) {
            GlobalVariables.graphiteAggregatorMetrics.remove(graphiteMetric.getHashKey());
        }

        return graphiteMetrics;
    }
    
    private void updateMetricMostRecentValues(List<GraphiteMetric> graphiteMetricsAggregated) {
        
        long timestampInMilliseconds = System.currentTimeMillis();
        
        if (GlobalVariables.graphiteAggregatedMetricsMostRecentValue != null) {
            for (GraphiteMetric graphiteMetricAggregated : GlobalVariables.graphiteAggregatedMetricsMostRecentValue.values()) {
                GraphiteMetric updatedGraphiteMetricAggregated = new GraphiteMetric(graphiteMetricAggregated.getMetricPath(),
                        graphiteMetricAggregated.getMetricValue(), timestampInMilliseconds, timestampInMilliseconds);
                updatedGraphiteMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                
                GlobalVariables.graphiteAggregatedMetricsMostRecentValue.put(updatedGraphiteMetricAggregated.getMetricPath(), updatedGraphiteMetricAggregated);
            }
        }
        
        if ((graphiteMetricsAggregated == null) || graphiteMetricsAggregated.isEmpty()) {
            return;
        }
        
        if ((GlobalVariables.graphiteAggregatedMetricsMostRecentValue != null) && ApplicationConfiguration.isGraphiteAggregatorSendPreviousValue()) {
            for (GraphiteMetric graphiteMetricAggregated : graphiteMetricsAggregated) {
                GlobalVariables.graphiteAggregatedMetricsMostRecentValue.put(graphiteMetricAggregated.getMetricPath(), graphiteMetricAggregated);
            }
        }

    }    
    
    private List<GraphiteMetric> mergePreviouslyAggregatedValuesWithCurrentAggregatedValues(List<GraphiteMetric> graphiteMetricsAggregatedNew, 
            Map<String,GraphiteMetric> graphiteMetricsAggregatedOld) {
        
        if ((graphiteMetricsAggregatedNew == null) && (graphiteMetricsAggregatedOld == null)) {
            return new ArrayList<>();
        }
        else if ((graphiteMetricsAggregatedNew == null) && (graphiteMetricsAggregatedOld != null)) {
            return new ArrayList<>(graphiteMetricsAggregatedOld.values());
        }
        else if ((graphiteMetricsAggregatedNew != null) && (graphiteMetricsAggregatedOld == null)) {
            return graphiteMetricsAggregatedNew;
        }
        else if (!ApplicationConfiguration.isGraphiteAggregatorSendPreviousValue()) {
            return graphiteMetricsAggregatedNew;
        }
        
        List<GraphiteMetric> graphiteMetricsAggregatedMerged = new ArrayList<>(graphiteMetricsAggregatedNew);
        Map<String,GraphiteMetric> graphiteMetricsAggregatedOldLocal = new HashMap<>(graphiteMetricsAggregatedOld);
        
        for (GraphiteMetric graphiteMetricAggregatedNew : graphiteMetricsAggregatedNew) {
            try {
                graphiteMetricsAggregatedOldLocal.remove(graphiteMetricAggregatedNew.getMetricPath());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            } 
        }
        
        graphiteMetricsAggregatedMerged.addAll(graphiteMetricsAggregatedOldLocal.values());
        
        return graphiteMetricsAggregatedMerged;
    }

}
