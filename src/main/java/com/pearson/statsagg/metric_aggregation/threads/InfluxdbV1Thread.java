package com.pearson.statsagg.metric_aggregation.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbStatsAggMetric_v1;
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
            long forgetInfluxdbStatsAggMetricsTimeStart = System.currentTimeMillis();
            Set<String> metricKeysToForget = new HashSet(GlobalVariables.immediateCleanupMetrics.keySet());
            long forgetInfluxdbStatsAggMetricsTimeElasped = System.currentTimeMillis() - forgetInfluxdbStatsAggMetricsTimeStart;  
            
            // get metrics for aggregation, then remove any aggregated metrics that need to be 'forgotten'
            long createMetricsTimeStart = System.currentTimeMillis();
            List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetrics = getCurrentInfluxdbStatsAggMetricsAndRemoveMetricsFromGlobal();
            removeMetricKeysFromInfluxdbStatsAggMetricsList(influxdbStatsAggMetrics, metricKeysToForget);
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
                    
            // update the global lists of the influxdb's most recent values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(influxdbStatsAggMetrics);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current values with the previous window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetricsMerged = mergePreviousValuesWithCurrentValues(influxdbStatsAggMetrics, GlobalVariables.influxdbStatsAggMetricsV1MostRecentValue);
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
            
            // send to graphite via tcp
            if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(influxdbStatsAggMetricsMerged, threadId_);
            
            // send to influxdb via telnet
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbTelnetOutputModuleEnabled()) SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbTelnetEndpoints(influxdbStatsAggMetricsMerged, threadId_);

            // send to opentsdb via http
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbHttpOutputModuleEnabled()) SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbHttpEndpoints(influxdbStatsAggMetricsMerged, threadId_);
                        
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
                for (InfluxdbStatsAggMetric_v1 influxdbStatsAggMetric : influxdbStatsAggMetrics) {
                    logger.info("InfluxDB metric= " + influxdbStatsAggMetric.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    // gets influxdb metrics for this thread 
    // also removes metrics from the global influxdb metrics map (since they are being operated on by this thread)
    private List<InfluxdbStatsAggMetric_v1> getCurrentInfluxdbStatsAggMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.influxdbV1Metrics == null) {
            return new ArrayList();
        }

        List<InfluxdbMetric_v1> influxdbMetrics = new ArrayList(GlobalVariables.influxdbV1Metrics.size());
        List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetrics = new ArrayList(GlobalVariables.influxdbV1Metrics.size());
        
        for (InfluxdbMetric_v1 influxdbMetric : GlobalVariables.influxdbV1Metrics.values()) {
            if (influxdbMetric.getMetricsReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                influxdbMetrics.add(influxdbMetric);
                GlobalVariables.influxdbV1Metrics.remove(influxdbMetric.getHashKey());
                
                if ((influxdbMetric.getInfluxdbStatsAggMetrics() == null) || influxdbMetric.getInfluxdbStatsAggMetrics().isEmpty()) continue;

                for (InfluxdbStatsAggMetric_v1 influxdbStatsAggMetric : influxdbMetric.getInfluxdbStatsAggMetrics()) {
                    influxdbStatsAggMetrics.add(influxdbStatsAggMetric);
                }
            }
        }

        return influxdbStatsAggMetrics;
    }
    
    private void updateMetricMostRecentValues(List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetrics) {

        if (GlobalVariables.influxdbStatsAggMetricsV1MostRecentValue != null) {
            long timestampInMilliseconds = System.currentTimeMillis();
            long timestampInSeconds = timestampInMilliseconds / 1000;
            long timestampInMicroseconds = timestampInMilliseconds * 1000;

            for (InfluxdbStatsAggMetric_v1 influxdbStatsAggMetric : GlobalVariables.influxdbStatsAggMetricsV1MostRecentValue.values()) {
                long timestamp;
                
                if (influxdbStatsAggMetric.getMetricTimestampPrecision() == InfluxdbMetric_v1.TIMESTAMP_PRECISION_MICROSECONDS) timestamp = timestampInMilliseconds;
                else if (influxdbStatsAggMetric.getMetricTimestampPrecision() == InfluxdbMetric_v1.TIMESTAMP_PRECISION_SECONDS) timestamp = timestampInSeconds;
                else if (influxdbStatsAggMetric.getMetricTimestampPrecision() == InfluxdbMetric_v1.TIMESTAMP_PRECISION_MICROSECONDS) timestamp = timestampInMicroseconds;
                else timestamp = timestampInMilliseconds;
                
                InfluxdbStatsAggMetric_v1 updatedInfluxdbStatsAggMetric = new InfluxdbStatsAggMetric_v1(influxdbStatsAggMetric.getMetricKey(), 
                        influxdbStatsAggMetric.getMetricDatabase(), influxdbStatsAggMetric.getMetricPrefix(), influxdbStatsAggMetric.getMetricPrefixPeriodDelimited(),
                        influxdbStatsAggMetric.getMetricName(), influxdbStatsAggMetric.getMetricValueName(),
                        influxdbStatsAggMetric.getMetricValue(), timestamp, influxdbStatsAggMetric.getMetricTimestampPrecision(), timestampInMilliseconds,
                        influxdbStatsAggMetric.getColumns(), influxdbStatsAggMetric.getPoint());
                updatedInfluxdbStatsAggMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                
                GlobalVariables.influxdbStatsAggMetricsV1MostRecentValue.put(updatedInfluxdbStatsAggMetric.getMetricKey(), updatedInfluxdbStatsAggMetric);
            }
        }

        if ((influxdbStatsAggMetrics == null) || influxdbStatsAggMetrics.isEmpty()) {
            return;
        }
        
        if ((GlobalVariables.influxdbStatsAggMetricsV1MostRecentValue != null) && ApplicationConfiguration.isInfluxdbSendPreviousValue()) {
            Map<String,InfluxdbStatsAggMetric_v1> mostRecentInfluxdbStatsAggMetricsByMetricKey = InfluxdbStatsAggMetric_v1.getMostRecentInfluxdbStatsAggMetricByMetricKey(influxdbStatsAggMetrics);
            
            for (InfluxdbStatsAggMetric_v1 influxdbStatsAggMetric : mostRecentInfluxdbStatsAggMetricsByMetricKey.values()) {
                GlobalVariables.influxdbStatsAggMetricsV1MostRecentValue.put(influxdbStatsAggMetric.getMetricKey(), influxdbStatsAggMetric);
            }
        }

    }    
    
    private List<InfluxdbStatsAggMetric_v1> mergePreviousValuesWithCurrentValues(List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetricsNew, 
            Map<String,InfluxdbStatsAggMetric_v1> influxdbStatsAggMetricsOld) {
        
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
        
        List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetricsMerged = new ArrayList<>(influxdbStatsAggMetricsNew);
        Map<String,InfluxdbStatsAggMetric_v1> influxdbStatsAggMetricsOldLocal = new HashMap<>(influxdbStatsAggMetricsOld);
        
        for (InfluxdbStatsAggMetric_v1 influxdbStatsAggMetricAggregatedNew : influxdbStatsAggMetricsNew) {
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
    
    public static void removeMetricKeysFromInfluxdbStatsAggMetricsList(List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetrics, Set<String> metricKeysToRemove) {
        
        if ((influxdbStatsAggMetrics == null) || influxdbStatsAggMetrics.isEmpty() || (metricKeysToRemove == null) || metricKeysToRemove.isEmpty()) {
            return;
        }
        
        Map<String,InfluxdbStatsAggMetric_v1> metricsMap = new HashMap<>();
        
        for (InfluxdbStatsAggMetric_v1 influxdbStatsAggMetric : influxdbStatsAggMetrics) {
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
