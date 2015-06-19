package com.pearson.statsagg.metric_aggregation.threads;

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
                    
            // update the global lists of the opentsdb's most recent values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(openTsdbMetrics);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current values with the previous window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<OpenTsdbMetric> openTsdbMetricsMerged = mergePreviousValuesWithCurrentValues(openTsdbMetrics, GlobalVariables.openTsdbMetricsMostRecentValue);
            removeMetricKeysFromOpenTsdbMetricsList(openTsdbMetricsMerged, metricKeysToForget);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 
  
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            if (ApplicationConfiguration.isOpenTsdbSendPreviousValue()) {
                Common.updateMetricLastSeenTimestamps_MostRecentNew(openTsdbMetrics);
                Common.updateMetricLastSeenTimestamps_UpdateOnResend(openTsdbMetricsMerged);
            }
            else Common.updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(openTsdbMetricsMerged);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(openTsdbMetricsMerged);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // send to graphite via tcp
            if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(openTsdbMetricsMerged, threadId_);
            
            // send to opentsdb via telnet
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbTelnetOutputModuleEnabled()) SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbTelnetEndpoints(openTsdbMetricsMerged, threadId_);
            
            // send to opentsdb via http
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbHttpOutputModuleEnabled()) SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbHttpEndpoints(openTsdbMetricsMerged, threadId_);
            
            // total time for this thread took to get & send the graphite metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(openTsdbMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + openTsdbMetrics.size() 
                    + ", RawMetricRatePerSec=" + (openTsdbMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", MetricsProcessedPerSec=" + rate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped
                    + ", NewAndOldMetricCount=" + openTsdbMetricsMerged.size() 
                    + ", ForgetMetricsTime=" + forgetOpenTsdbMetricsTimeElasped
                    ;
            
            if (openTsdbMetricsMerged.isEmpty()) {
                logger.debug(aggregationStatistics);
            }
            else {
                logger.info(aggregationStatistics);
            }
            
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
    
    private List<OpenTsdbMetric> getCurrentOpenTsdbMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.openTsdbMetrics == null) {
            return new ArrayList();
        }

        // gets opentsdb metrics for this thread 
        List<OpenTsdbMetric> openTsdbMetrics = new ArrayList(GlobalVariables.openTsdbMetrics.size());
        
        for (OpenTsdbMetric openTsdbMetric : GlobalVariables.openTsdbMetrics.values()) {
            if (openTsdbMetric.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                openTsdbMetrics.add(openTsdbMetric);
            }
        }
        
        // removes metrics from the global opentsdb metrics map (since they are being operated on by this thread)
        for (OpenTsdbMetric openTsdbMetric : openTsdbMetrics) {
            GlobalVariables.openTsdbMetrics.remove(openTsdbMetric.getHashKey());
        }

        return openTsdbMetrics;
    }
    
    private void updateMetricMostRecentValues(List<OpenTsdbMetric> openTsdbMetrics) {

        if (GlobalVariables.openTsdbMetricsMostRecentValue != null) {
            long timestampInMilliseconds = System.currentTimeMillis();
            int timestampInSeconds;
            
            for (OpenTsdbMetric openTsdbMetric : GlobalVariables.openTsdbMetricsMostRecentValue.values()) {
                OpenTsdbMetric updatedOpenTsdbMetric;
                
                if (!openTsdbMetric.isTimestampInMilliseconds()) {
                    timestampInSeconds = (int) (timestampInMilliseconds / 1000);
                    
                    updatedOpenTsdbMetric = new OpenTsdbMetric(timestampInSeconds, openTsdbMetric.getMetricValue(), 
                            openTsdbMetric.isTimestampInMilliseconds(), timestampInMilliseconds, openTsdbMetric.getMetricKey(), openTsdbMetric.getMetricLength());
                }
                else {                    
                    updatedOpenTsdbMetric = new OpenTsdbMetric(timestampInMilliseconds, openTsdbMetric.getMetricValue(),
                            openTsdbMetric.isTimestampInMilliseconds(), timestampInMilliseconds, openTsdbMetric.getMetricKey(), openTsdbMetric.getMetricLength());
                }

                updatedOpenTsdbMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                
                GlobalVariables.openTsdbMetricsMostRecentValue.put(updatedOpenTsdbMetric.getMetricKey(), updatedOpenTsdbMetric);
            }
        }

        if ((openTsdbMetrics == null) || openTsdbMetrics.isEmpty()) {
            return;
        }
        
        if ((GlobalVariables.openTsdbMetricsMostRecentValue != null) && ApplicationConfiguration.isOpenTsdbSendPreviousValue()) {
            Map<String,OpenTsdbMetric> mostRecentOpenTsdbMetricsByMetricPath = OpenTsdbMetric.getMostRecentOpenTsdbMetricByMetricKey(openTsdbMetrics);
            
            for (OpenTsdbMetric openTsdbMetric : mostRecentOpenTsdbMetricsByMetricPath.values()) {
                GlobalVariables.openTsdbMetricsMostRecentValue.put(openTsdbMetric.getMetricKey(), openTsdbMetric);
            }
        }
            
    }    
    
    private List<OpenTsdbMetric> mergePreviousValuesWithCurrentValues(List<OpenTsdbMetric> openTsdbMetricsNew, 
            Map<String,OpenTsdbMetric> openTsdbMetricsOld) {
        
        if ((openTsdbMetricsNew == null) && (openTsdbMetricsOld == null)) {
            return new ArrayList<>();
        }
        else if ((openTsdbMetricsNew == null) && (openTsdbMetricsOld != null)) {
            return new ArrayList<>(openTsdbMetricsOld.values());
        }
        else if ((openTsdbMetricsNew != null) && (openTsdbMetricsOld == null)) {
            return openTsdbMetricsNew;
        }
        else if (!ApplicationConfiguration.isOpenTsdbSendPreviousValue()) {
            return openTsdbMetricsNew;
        }
        
        List<OpenTsdbMetric> openTsdbMetricsMerged = new ArrayList<>(openTsdbMetricsNew);
        Map<String,OpenTsdbMetric> openTsdbMetricsOldLocal = new HashMap<>(openTsdbMetricsOld);
        
        for (OpenTsdbMetric openTsdbMetricAggregatedNew : openTsdbMetricsNew) {
            try {
                openTsdbMetricsOldLocal.remove(openTsdbMetricAggregatedNew.getMetricKey());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            } 
        }
        
        openTsdbMetricsMerged.addAll(openTsdbMetricsOldLocal.values());
        
        return openTsdbMetricsMerged;
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
