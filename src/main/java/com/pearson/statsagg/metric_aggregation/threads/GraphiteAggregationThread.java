package com.pearson.statsagg.metric_aggregation.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricAggregated;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricAggregator;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricRaw;
import com.pearson.statsagg.modules.GraphiteOutputModule;
import com.pearson.statsagg.utilities.StackTrace;
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
            
            // get metrics for aggregation
            long createMetricsTimeStart = System.currentTimeMillis();
            List<GraphiteMetricRaw> graphiteMetricsRaw = getCurrentGraphiteAggregatorMetricsAndRemoveMetricsFromGlobal();
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
            
            // aggregate graphite metrics
            long aggregateTimeStart = System.currentTimeMillis();
            List<GraphiteMetricAggregated> graphiteMetricsAggregated = GraphiteMetricAggregator.aggregateGraphiteMetrics(graphiteMetricsRaw);
            long aggregateTimeElasped = System.currentTimeMillis() - aggregateTimeStart; 
            
            // update the global lists of the graphite aggregator's most recent aggregated values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(graphiteMetricsAggregated);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current aggregated values with the previous aggregated window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<GraphiteMetricAggregated> graphiteMetricsAggregatedMerged = mergePreviouslyAggregatedValuesWithCurrentAggregatedValues(graphiteMetricsAggregated, 
                    GlobalVariables.graphiteAggregatedMetricsMostRecentValue);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 
            
            // updates the global list that tracks the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps(graphiteMetricsAggregatedMerged);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(graphiteMetricsAggregatedMerged);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // 'forget' metrics
            long forgetGraphiteMetricsTimeStart = System.currentTimeMillis();
            forgetGraphiteAggregatedMetrics();
            long forgetGraphiteMetricsTimeElasped = System.currentTimeMillis() - forgetGraphiteMetricsTimeStart;  
            
            long generateGraphiteStringsTimeElasped = 0;
            if (GraphiteOutputModule.isAnyGraphiteOutputModuleEnabled()) {
                // generate messages for graphite
                long generateGraphiteStringsTimeStart = System.currentTimeMillis();
                List<String> graphiteOutputMessagesForGraphite = GraphiteOutputModule.buildMultiMetricGraphiteMessages(graphiteMetricsAggregatedMerged, ApplicationConfiguration.getGraphiteMaxBatchSize());
                generateGraphiteStringsTimeElasped = System.currentTimeMillis() - generateGraphiteStringsTimeStart; 
            
                // send to graphite
                GraphiteOutputModule.sendMetricsToGraphiteEndpoints(graphiteOutputMessagesForGraphite, threadId_);
            }
            
            // total time for this thread took to aggregate the graphite metrics
            long timeAggregationTimeElasped = System.currentTimeMillis() - timeAggregationTimeStart - waitInMsCounter;
            String aggregationRate = "0";
            if (timeAggregationTimeElasped > 0) {
                aggregationRate = Long.toString(graphiteMetricsRaw.size() / timeAggregationTimeElasped * 1000);
            }
                    
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", NewRawMetricCount=" + graphiteMetricsRaw.size() 
                    + ", AggMetricCount=" + graphiteMetricsAggregated.size() 
                    + ", AggTotalTime=" + timeAggregationTimeElasped 
                    + ", AggMetricsPerSec=" + aggregationRate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped 
                    + ", AggTime=" + aggregateTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped
                    + ", AggNewAndOldMetricCount=" + graphiteMetricsAggregatedMerged.size()
                    + ", ForgetMetricsTime=" + forgetGraphiteMetricsTimeElasped
                    + ", GenGraphiteStringsTime=" + generateGraphiteStringsTimeElasped;
            
            if (graphiteMetricsAggregatedMerged.isEmpty()) {
                logger.debug(aggregationStatistics);
            }
            else {
                logger.info(aggregationStatistics);
            }

            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (GraphiteMetricAggregated graphiteMetricAggregated : graphiteMetricsAggregatedMerged) {
                    logger.info("Graphite aggregated metric= " + graphiteMetricAggregated.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
        
    private List<GraphiteMetricRaw> getCurrentGraphiteAggregatorMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.graphiteAggregatorMetricsRaw == null) {
            return new ArrayList();
        }

        // gets graphite aggregator metrics for this thread to aggregate & send to graphite
        List<GraphiteMetricRaw> graphiteMetricsRaw = new ArrayList(GlobalVariables.graphiteAggregatorMetricsRaw.size());
        
        for (GraphiteMetricRaw graphiteMetricRaw : GlobalVariables.graphiteAggregatorMetricsRaw.values()) {
            if (graphiteMetricRaw.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                graphiteMetricsRaw.add(graphiteMetricRaw);
            }
        }
        
        // removes metrics from the global graphite aggregator metrics map (since they are being operated on by this thread)
        for (GraphiteMetricRaw graphiteMetricRaw : graphiteMetricsRaw) {
            GlobalVariables.graphiteAggregatorMetricsRaw.remove(graphiteMetricRaw.getHashKey());
        }

        return graphiteMetricsRaw;
    }
    
    private void updateMetricMostRecentValues(List<GraphiteMetricAggregated> graphiteMetricsAggregated) {
        
        long timestampInMilliseconds = System.currentTimeMillis();
        
        if (GlobalVariables.graphiteAggregatedMetricsMostRecentValue != null) {
            for (GraphiteMetricAggregated graphiteMetricAggregated : GlobalVariables.graphiteAggregatedMetricsMostRecentValue.values()) {
                GraphiteMetricAggregated updatedGraphiteMetricAggregated = new GraphiteMetricAggregated(graphiteMetricAggregated.getMetricPath(),
                        graphiteMetricAggregated.getMetricValue(), timestampInMilliseconds, timestampInMilliseconds);
                updatedGraphiteMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
                
                GlobalVariables.graphiteAggregatedMetricsMostRecentValue.put(updatedGraphiteMetricAggregated.getMetricPath(), updatedGraphiteMetricAggregated);
            }
        }
        
        if ((graphiteMetricsAggregated == null) || graphiteMetricsAggregated.isEmpty()) {
            return;
        }
        
        if ((GlobalVariables.graphiteAggregatedMetricsMostRecentValue != null) && ApplicationConfiguration.isGraphiteAggregatorSendPreviousValue()) {
            for (GraphiteMetricAggregated graphiteMetricAggregated : graphiteMetricsAggregated) {
                GlobalVariables.graphiteAggregatedMetricsMostRecentValue.put(graphiteMetricAggregated.getMetricPath(), graphiteMetricAggregated);
            }
        }

    }    
    
    private List<GraphiteMetricAggregated> mergePreviouslyAggregatedValuesWithCurrentAggregatedValues(List<GraphiteMetricAggregated> graphiteMetricsAggregatedNew, 
            Map<String,GraphiteMetricAggregated> graphiteMetricsAggregatedOld) {
        
        if ((graphiteMetricsAggregatedNew == null) && (graphiteMetricsAggregatedOld == null)) {
            return new ArrayList<>();
        }
        else if ((graphiteMetricsAggregatedNew == null) && (graphiteMetricsAggregatedOld != null)) {
            return new ArrayList<>(graphiteMetricsAggregatedOld.values());
        }
        else if ((graphiteMetricsAggregatedNew != null) && (graphiteMetricsAggregatedOld == null)) {
            return graphiteMetricsAggregatedNew;
        }
        
        List<GraphiteMetricAggregated> graphiteMetricsAggregatedMerged = new ArrayList<>(graphiteMetricsAggregatedNew);
        Map<String,GraphiteMetricAggregated> graphiteMetricsAggregatedOldLocal = new HashMap<>(graphiteMetricsAggregatedOld);
        
        for (GraphiteMetricAggregated graphiteMetricAggregatedNew : graphiteMetricsAggregatedNew) {
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
    
    private void forgetGraphiteAggregatedMetrics() {

        HashSet<String> metricPathsToForget = new HashSet<>();
        
        // gets a list of complete metric paths to forget
        if (GlobalVariables.forgetGraphiteAggregatedMetrics != null) {  
            Set<String> forgetGraphiteAggregatedMetrics = new HashSet<>(GlobalVariables.forgetGraphiteAggregatedMetrics.keySet());
            
            for (String metricPath : forgetGraphiteAggregatedMetrics) {
                metricPathsToForget.add(metricPath);
                GlobalVariables.forgetGraphiteAggregatedMetrics.remove(metricPath);
            }
        }
        
        // gets a list of metric paths to forget by matching against regexs
        if (GlobalVariables.forgetGraphiteAggregatedMetricsRegexs != null) {     
            Set<String> forgetGraphiteAggregatedMetricsRegexs = new HashSet<>(GlobalVariables.forgetGraphiteAggregatedMetricsRegexs.keySet());
            
            for (String metricPathRegex : forgetGraphiteAggregatedMetricsRegexs) {
                Set<String> regexMetricPathsToForget = forgetGraphiteAggregatedMetrics_IdentifyMetricPathsViaRegex(metricPathRegex);
                
                if (regexMetricPathsToForget != null) {
                    metricPathsToForget.addAll(regexMetricPathsToForget);
                }
                
                GlobalVariables.forgetGraphiteAggregatedMetricsRegexs.remove(metricPathRegex);
            }
        }
   
        // 'forgets' the graphite aggregated metrics
        if (!metricPathsToForget.isEmpty()) {
            forgetGraphiteAggregatedMetrics_Forget(metricPathsToForget);
        }
        
    }
    
    private Set<String> forgetGraphiteAggregatedMetrics_IdentifyMetricPathsViaRegex(String regex) {
        
        if ((regex == null) || regex.isEmpty() || (GlobalVariables.graphiteAggregatedMetricsMostRecentValue == null)) {
            return new HashSet<>();
        }
        
        Set<String> metricPathsToForget = new HashSet<>();
        
        Pattern pattern = Pattern.compile(regex);
         
        for (GraphiteMetricAggregated graphiteMetricAggregated : GlobalVariables.graphiteAggregatedMetricsMostRecentValue.values()) {
            Matcher matcher = pattern.matcher(graphiteMetricAggregated.getMetricPath());
            
            if (matcher.matches()) {
                metricPathsToForget.add(graphiteMetricAggregated.getMetricPath());
            }
        }
         
        return metricPathsToForget;
    }

    private void forgetGraphiteAggregatedMetrics_Forget(Set<String> metricPathsToForget) {
        
        if ((metricPathsToForget == null) || metricPathsToForget.isEmpty() || (GlobalVariables.graphiteAggregatedMetricsMostRecentValue == null)) {
            return;
        }
            
        for (String metricPathToForget : metricPathsToForget) {
            GlobalVariables.immediateCleanupMetrics.put(metricPathToForget, metricPathToForget);
            GlobalVariables.graphiteAggregatedMetricsMostRecentValue.remove(metricPathToForget);
        }

    }

}
