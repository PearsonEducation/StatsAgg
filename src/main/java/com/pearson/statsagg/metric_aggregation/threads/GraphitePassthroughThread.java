package com.pearson.statsagg.metric_aggregation.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.utilities.StackTrace;
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

            // returns a list of metric-keys that need to be disregarded by this routine.
            long forgetGraphiteMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            long forgetGraphiteMetricsTimeElasped = System.currentTimeMillis() - forgetGraphiteMetricsTimeStart;  
            
            // get metrics for aggregation, then remove any aggregated metrics that need to be 'forgotten'
            long createMetricsTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetrics = getCurrentGraphitePassthroughMetricsAndRemoveMetricsFromGlobal();
            Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetrics, metricKeysToForget);
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
            
            // update the global lists of the graphite passthrough's most recent values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(graphiteMetrics);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current values with the previous window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<GraphiteMetric> graphiteMetricsMerged = mergePreviousValuesWithCurrentValues(graphiteMetrics, GlobalVariables.graphitePassthroughMetricsMostRecentValue);
            Common.removeMetricKeysFromGraphiteMetricsList(graphiteMetricsMerged, metricKeysToForget);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 
  
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            if (ApplicationConfiguration.isGraphitePassthroughSendPreviousValue()) {
                Common.updateMetricLastSeenTimestamps_MostRecentNew(graphiteMetrics);
                Common.updateMetricLastSeenTimestamps_UpdateOnResend(graphiteMetricsMerged);
            }
            else Common.updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(graphiteMetricsMerged);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(graphiteMetricsMerged);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 

            // send to graphite via tcp
            if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(graphiteMetricsMerged, threadId_);
            
            // send to opentsdb via telnet
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbTelnetOutputModuleEnabled()) SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbTelnetEndpoints(graphiteMetricsMerged, threadId_);
            
            // send to opentsdb via http
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbHttpOutputModuleEnabled()) SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbHttpEndpoints(graphiteMetricsMerged, threadId_);
            
            // total time for this thread took to get & send the graphite metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(graphiteMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + graphiteMetrics.size() 
                    + ", RawMetricRatePerSec=" + (graphiteMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", MetricsProcessedPerSec=" + rate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped
                    + ", NewAndOldMetricCount=" + graphiteMetricsMerged.size() 
                    + ", ForgetMetricsTime=" + forgetGraphiteMetricsTimeElasped
                    ;
            
            if (graphiteMetricsMerged.isEmpty()) {
                logger.debug(aggregationStatistics);
            }
            else {
                logger.info(aggregationStatistics);
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (GraphiteMetric graphiteMetric : graphiteMetrics) {
                    logger.info("Graphite metric= " + graphiteMetric.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    private List<GraphiteMetric> getCurrentGraphitePassthroughMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.graphitePassthroughMetrics == null) {
            return new ArrayList();
        }

        // gets graphite passthrough metrics for this thread 
        List<GraphiteMetric> graphiteMetrics = new ArrayList(GlobalVariables.graphitePassthroughMetrics.size());
        
        for (GraphiteMetric graphiteMetric : GlobalVariables.graphitePassthroughMetrics.values()) {
            if (graphiteMetric.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                graphiteMetrics.add(graphiteMetric);
            }
        }
        
        // removes metrics from the global graphite passthrough metrics map (since they are being operated on by this thread)
        for (GraphiteMetric graphiteMetric : graphiteMetrics) {
            GlobalVariables.graphitePassthroughMetrics.remove(graphiteMetric.getHashKey());
        }

        return graphiteMetrics;
    }
    
    private void updateMetricMostRecentValues(List<GraphiteMetric> graphiteMetrics) {

        if (GlobalVariables.graphitePassthroughMetricsMostRecentValue != null) {
            long timestampInMilliseconds = System.currentTimeMillis();
            int timestampInSeconds = (int) (timestampInMilliseconds / 1000);
            
            for (GraphiteMetric graphiteMetric : GlobalVariables.graphitePassthroughMetricsMostRecentValue.values()) {
                GraphiteMetric updatedGraphiteMetric = new GraphiteMetric(graphiteMetric.getMetricPath(), graphiteMetric.getMetricValue(), 
                        timestampInSeconds, timestampInMilliseconds);
                updatedGraphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                
                GlobalVariables.graphitePassthroughMetricsMostRecentValue.put(updatedGraphiteMetric.getMetricPath(), updatedGraphiteMetric);
            }
        }

        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty()) {
            return;
        }
        
        if ((GlobalVariables.graphitePassthroughMetricsMostRecentValue != null) && ApplicationConfiguration.isGraphitePassthroughSendPreviousValue()) {
            Map<String,GraphiteMetric> mostRecentGraphiteMetricsByMetricPath = GraphiteMetric.getMostRecentGraphiteMetricByMetricPath(graphiteMetrics);
            
            for (GraphiteMetric graphiteMetric : mostRecentGraphiteMetricsByMetricPath.values()) {
                GlobalVariables.graphitePassthroughMetricsMostRecentValue.put(graphiteMetric.getMetricPath(), graphiteMetric);
            }
        }
            
    }    
    
    private List<GraphiteMetric> mergePreviousValuesWithCurrentValues(List<GraphiteMetric> graphiteMetricsNew, 
            Map<String,GraphiteMetric> graphiteMetricsOld) {
        
        if ((graphiteMetricsNew == null) && (graphiteMetricsOld == null)) {
            return new ArrayList<>();
        }
        else if ((graphiteMetricsNew == null) && (graphiteMetricsOld != null)) {
            return new ArrayList<>(graphiteMetricsOld.values());
        }
        else if ((graphiteMetricsNew != null) && (graphiteMetricsOld == null)) {
            return graphiteMetricsNew;
        }
        else if (!ApplicationConfiguration.isGraphitePassthroughSendPreviousValue()) {
            return graphiteMetricsNew;
        }
        
        List<GraphiteMetric> graphiteMetricsMerged = new ArrayList<>(graphiteMetricsNew);
        Map<String,GraphiteMetric> graphiteMetricsOldLocal = new HashMap<>(graphiteMetricsOld);
        
        for (GraphiteMetric graphiteMetricAggregatedNew : graphiteMetricsNew) {
            try {
                graphiteMetricsOldLocal.remove(graphiteMetricAggregatedNew.getMetricPath());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            } 
        }
        
        graphiteMetricsMerged.addAll(graphiteMetricsOldLocal.values());
        
        return graphiteMetricsMerged;
    }
    
}
