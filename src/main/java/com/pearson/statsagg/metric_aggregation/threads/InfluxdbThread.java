package com.pearson.statsagg.metric_aggregation.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.influxdb.InfluxdbMetric;
import com.pearson.statsagg.metric_aggregation.influxdb.InfluxdbStatsAggMetric;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InfluxdbThread.class.getName());
    
    // Lists of active aggregation thread 'thread-start' timestamps. Used as a hacky mechanism for thread blocking on the aggregation threads. 
    private final static List<Long> activeInfluxdbThreadStartGetMetricsTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    
    private final Long threadStartTimestampInMilliseconds_;
    private final String threadId_;
    
    public InfluxdbThread(Long threadStartTimestampInMilliseconds) {
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
            long forgetInfluxdbStatsAggMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            long forgetInfluxdbStatsAggMetricsTimeElasped = System.currentTimeMillis() - forgetInfluxdbStatsAggMetricsTimeStart;  
            
            // get metrics for aggregation, then remove any aggregated metrics that need to be 'forgotten'
            long createMetricsTimeStart = System.currentTimeMillis();
            List<InfluxdbStatsAggMetric> influxdbStatsAggMetrics = getCurrentInfluxdbStatsAggMetricsAndRemoveMetricsFromGlobal();
            removeMetricKeysFromInfluxdbStatsAggMetricsList(influxdbStatsAggMetrics, metricKeysToForget);
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
                    
            // update the global lists of the influxdb's most recent values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(influxdbStatsAggMetrics);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current values with the previous window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<InfluxdbStatsAggMetric> influxdbStatsAggMetricsMerged = mergePreviousValuesWithCurrentValues(influxdbStatsAggMetrics, GlobalVariables.InfluxdbStatsAggMetricsMostRecentValue);
            removeMetricKeysFromInfluxdbStatsAggMetricsList(influxdbStatsAggMetricsMerged, metricKeysToForget);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 
  
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            if (ApplicationConfiguration.isInfluxdbSendPreviousValue()) {
                Common.updateMetricLastSeenTimestamps_MostRecentNew(influxdbStatsAggMetrics);
                Common.updateMetricLastSeenTimestamps_UpdateOnResend(influxdbStatsAggMetricsMerged);
            }
            else Common.updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(influxdbStatsAggMetricsMerged);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(influxdbStatsAggMetricsMerged);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 
            
            // send to graphite
            if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) {
                SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(influxdbStatsAggMetricsMerged, threadId_);
            }
            
            // send to influxdb via telnet
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbTelnetOutputModuleEnabled()) {
                SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbTelnetEndpoints(influxdbStatsAggMetricsMerged, threadId_);
            }
            
            // total time for this thread took to get & send the graphite metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) rate = Long.toString(influxdbStatsAggMetrics.size() / threadTimeElasped * 1000);
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + influxdbStatsAggMetrics.size() 
                    + ", RawMetricRatePerSec=" + (influxdbStatsAggMetrics.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", MetricsProcessedPerSec=" + rate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped
                    + ", NewAndOldMetricCount=" + influxdbStatsAggMetricsMerged.size() 
                    + ", ForgetMetricsTime=" + forgetInfluxdbStatsAggMetricsTimeElasped
                    ;
            
            if (influxdbStatsAggMetricsMerged.isEmpty()) {
                logger.debug(aggregationStatistics);
            }
            else {
                logger.info(aggregationStatistics);
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (InfluxdbStatsAggMetric influxdbStatsAggMetric : influxdbStatsAggMetrics) {
                    logger.info("InfluxDB metric= " + influxdbStatsAggMetric.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    private List<InfluxdbStatsAggMetric> getCurrentInfluxdbStatsAggMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.influxdbMetrics == null) {
            return new ArrayList();
        }

        // gets influxdb metrics for this thread 
        List<InfluxdbMetric> influxdbMetrics = new ArrayList(GlobalVariables.influxdbMetrics.size());
        List<InfluxdbStatsAggMetric> influxdbStatsAggMetrics = new ArrayList(GlobalVariables.influxdbMetrics.size());
        
        for (InfluxdbMetric influxdbMetric : GlobalVariables.influxdbMetrics.values()) {
            if (influxdbMetric.getMetricsReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                influxdbMetrics.add(influxdbMetric);

                if ((influxdbMetric.getInfluxdbStatsAggMetrics() == null) || influxdbMetric.getInfluxdbStatsAggMetrics().isEmpty()) continue;

                for (InfluxdbStatsAggMetric influxdbStatsAggMetric : influxdbMetric.getInfluxdbStatsAggMetrics()) {
                    influxdbStatsAggMetrics.add(influxdbStatsAggMetric);
                }
            }
        }
        
        // removes metrics from the global influxdb metrics map (since they are being operated on by this thread)
        for (InfluxdbMetric influxdbMetric : influxdbMetrics) {
            GlobalVariables.influxdbMetrics.remove(influxdbMetric.getHashKey());
        }

        return influxdbStatsAggMetrics;
    }
    
    private void updateMetricMostRecentValues(List<InfluxdbStatsAggMetric> influxdbStatsAggMetrics) {

        if (GlobalVariables.InfluxdbStatsAggMetricsMostRecentValue != null) {
            long timestampInMilliseconds = System.currentTimeMillis();
            long timestampInSeconds = timestampInMilliseconds / 1000;
            long timestampInMicroseconds = timestampInMilliseconds * 1000;

            for (InfluxdbStatsAggMetric influxdbStatsAggMetric : GlobalVariables.InfluxdbStatsAggMetricsMostRecentValue.values()) {
                long timestamp;
                
                if (influxdbStatsAggMetric.getMetricTimestampPrecision() == InfluxdbMetric.TIMESTAMP_PRECISION_MICROSECONDS) timestamp = timestampInMilliseconds;
                else if (influxdbStatsAggMetric.getMetricTimestampPrecision() == InfluxdbMetric.TIMESTAMP_PRECISION_SECONDS) timestamp = timestampInSeconds;
                else if (influxdbStatsAggMetric.getMetricTimestampPrecision() == InfluxdbMetric.TIMESTAMP_PRECISION_MICROSECONDS) timestamp = timestampInMicroseconds;
                else timestamp = timestampInMilliseconds;
                
                InfluxdbStatsAggMetric updatedInfluxdbStatsAggMetric = new InfluxdbStatsAggMetric(influxdbStatsAggMetric.getMetricKey(), influxdbStatsAggMetric.getMetricValue(), 
                        timestamp, influxdbStatsAggMetric.getMetricTimestampPrecision(), timestampInMilliseconds);
                updatedInfluxdbStatsAggMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                
                GlobalVariables.InfluxdbStatsAggMetricsMostRecentValue.put(updatedInfluxdbStatsAggMetric.getMetricKey(), updatedInfluxdbStatsAggMetric);
            }
        }

        if ((influxdbStatsAggMetrics == null) || influxdbStatsAggMetrics.isEmpty()) {
            return;
        }
        
        if ((GlobalVariables.InfluxdbStatsAggMetricsMostRecentValue != null) && ApplicationConfiguration.isInfluxdbSendPreviousValue()) {
            Map<String,InfluxdbStatsAggMetric> mostRecentInfluxdbStatsAggMetricsByMetricKey = InfluxdbStatsAggMetric.getMostRecentInfluxdbStatsAggMetricByMetricKey(influxdbStatsAggMetrics);
            
            for (InfluxdbStatsAggMetric influxdbStatsAggMetric : mostRecentInfluxdbStatsAggMetricsByMetricKey.values()) {
                GlobalVariables.InfluxdbStatsAggMetricsMostRecentValue.put(influxdbStatsAggMetric.getMetricKey(), influxdbStatsAggMetric);
            }
        }

    }    
    
    private List<InfluxdbStatsAggMetric> mergePreviousValuesWithCurrentValues(List<InfluxdbStatsAggMetric> influxdbStatsAggMetricsNew, 
            Map<String,InfluxdbStatsAggMetric> influxdbStatsAggMetricsOld) {
        
        if ((influxdbStatsAggMetricsNew == null) && (influxdbStatsAggMetricsOld == null)) {
            return new ArrayList<>();
        }
        else if ((influxdbStatsAggMetricsNew == null) && (influxdbStatsAggMetricsOld != null)) {
            return new ArrayList<>(influxdbStatsAggMetricsOld.values());
        }
        else if ((influxdbStatsAggMetricsNew != null) && (influxdbStatsAggMetricsOld == null)) {
            return influxdbStatsAggMetricsNew;
        }
        else if (!ApplicationConfiguration.isInfluxdbSendPreviousValue()) {
            return influxdbStatsAggMetricsNew;
        }
        
        List<InfluxdbStatsAggMetric> influxdbStatsAggMetricsMerged = new ArrayList<>(influxdbStatsAggMetricsNew);
        Map<String,InfluxdbStatsAggMetric> influxdbStatsAggMetricsOldLocal = new HashMap<>(influxdbStatsAggMetricsOld);
        
        for (InfluxdbStatsAggMetric influxdbStatsAggMetricAggregatedNew : influxdbStatsAggMetricsNew) {
            try {
                influxdbStatsAggMetricsOldLocal.remove(influxdbStatsAggMetricAggregatedNew.getMetricKey());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            } 
        }
        
        influxdbStatsAggMetricsMerged.addAll(influxdbStatsAggMetricsOldLocal.values());
        
        return influxdbStatsAggMetricsMerged;
    }
    
    public static void removeMetricKeysFromInfluxdbStatsAggMetricsList(List<InfluxdbStatsAggMetric> influxdbStatsAggMetrics, Set<String> metricKeysToRemove) {
        
        if ((influxdbStatsAggMetrics == null) || influxdbStatsAggMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return;
        }
        
        Map<String,InfluxdbStatsAggMetric> metricsMap = new HashMap<>();
        
        for (InfluxdbStatsAggMetric influxdbStatsAggMetric : influxdbStatsAggMetrics) {
            String metricKey = influxdbStatsAggMetric.getMetricKey();
            if (metricKey != null) metricsMap.put(metricKey, influxdbStatsAggMetric);
        }
                
        for (String metricKeyToRemove : metricKeysToRemove) {
            Object metric = metricsMap.get(metricKeyToRemove);
            if (metric != null) metricsMap.remove(metricKeyToRemove);
        }
        
        influxdbStatsAggMetrics.clear();
        influxdbStatsAggMetrics.addAll(metricsMap.values());
    }
    
}
