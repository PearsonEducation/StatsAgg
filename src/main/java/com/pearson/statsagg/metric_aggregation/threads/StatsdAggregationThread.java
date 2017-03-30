package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.alerts.MetricAssociation;
import com.pearson.statsagg.controller.thread_managers.SendMetricsToOutputModule_ThreadPoolManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.database_objects.gauges.Gauge;
import com.pearson.statsagg.database_objects.gauges.GaugesDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.aggregators.StatsdMetricAggregator;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetric;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StatsdAggregationThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StatsdAggregationThread.class.getName());

    // Lists of active aggregation thread 'thread-start' timestamps. Used as a hacky mechanism for thread blocking on the aggregation threads. 
    private final static List<Long> activeStatsdAggregationThreadStartTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    private final static List<Long> activeStatsdAggregationThreadStartGetMetricsTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    
    private final Long threadStartTimestampInMilliseconds_;
    private final String threadId_;
    
    public StatsdAggregationThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "S-" + threadStartTimestampInMilliseconds_.toString();
    }

    @Override
    public void run() {
        
        if (threadStartTimestampInMilliseconds_ == null) {
            logger.error(this.getClass().getName() + " has invalid initialization value(s)");
            return;
        }
        
        long timeAggregationTimeStart = System.currentTimeMillis();
        
        boolean isSuccessfulAdd = activeStatsdAggregationThreadStartTimestamps.add(threadStartTimestampInMilliseconds_);
        if (!isSuccessfulAdd) {
            logger.error("There is another active thread of type '" + this.getClass().getName() + "' with the same thread start timestamp. Killing this thread...");
            return;
        }
        
        isSuccessfulAdd = activeStatsdAggregationThreadStartGetMetricsTimestamps.add(threadStartTimestampInMilliseconds_);
        if (!isSuccessfulAdd) {
            logger.error("There is another active thread of type '" + this.getClass().getName() + "' with the same thread start timestamp. Killing this thread...");
            return;
        }
        
        try {
            // wait until this is the youngest active thread
            int waitInMsCounter = Common.waitUntilThisIsYoungestActiveThread(threadStartTimestampInMilliseconds_, activeStatsdAggregationThreadStartGetMetricsTimestamps);
            activeStatsdAggregationThreadStartGetMetricsTimestamps.remove(threadStartTimestampInMilliseconds_);

            // get metrics
            long getMetricsTimeStart = System.currentTimeMillis();
            List<StatsdMetric> statsdMetricsGauges = getCurrentStatsdMetricsAndRemoveMetricsFromGlobal(GlobalVariables.statsdGaugeMetrics);
            List<StatsdMetric> statsdMetricsNotGauges = getCurrentStatsdMetricsAndRemoveMetricsFromGlobal(GlobalVariables.statsdNotGaugeMetrics);
            long totalNumberOfStatsdMetrics = statsdMetricsGauges.size() + statsdMetricsNotGauges.size();
            long getMetricsTimeElasped = System.currentTimeMillis() - getMetricsTimeStart; 
            
            // aggregate everything except gauges, then remove any aggregated metrics that need to be 'forgotten'
            long aggregateNotGaugeTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregatedNotGauges = StatsdMetricAggregator.aggregateStatsdMetrics(statsdMetricsNotGauges);
            long aggregateNotGaugeTimeElasped = System.currentTimeMillis() - aggregateNotGaugeTimeStart; 
            
            // wait until this is the youngest active thread
            waitInMsCounter += Common.waitUntilThisIsYoungestActiveThread(threadStartTimestampInMilliseconds_, activeStatsdAggregationThreadStartTimestamps);
            
           // aggregate gauges, then remove any aggregated metrics that need to be 'forgotten'
            long aggregateGaugeTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregatedGauges = StatsdMetricAggregator.aggregateStatsdMetrics(statsdMetricsGauges);
            long aggregateGaugeTimeElasped = System.currentTimeMillis() - aggregateGaugeTimeStart; 
            
            // gets a list of buckets that need to be disregarded by this routine & removes them 
            long forgetStatsdMetricsTimeStart = System.currentTimeMillis();
            Set<String> bucketsToForget = new HashSet<>(GlobalVariables.immediateCleanupMetrics.keySet());
            List<StatsdMetricAggregated> statsdMetricsAggregatedNotGauges_RemovedForgottenMetrics = removeBucketsFromStatsdMetricsList(statsdMetricsAggregatedNotGauges, bucketsToForget);
            List<StatsdMetricAggregated> statsdMetricsAggregatedGauges_RemovedForgottenMetrics = removeBucketsFromStatsdMetricsList(statsdMetricsAggregatedGauges, bucketsToForget);
            long forgetStatsdMetricsTimeElasped = System.currentTimeMillis() - forgetStatsdMetricsTimeStart;  
            
            // merge aggregated non-aggregatedGauge & aggregateGauge metrics
            long mergeAggregatedMetricsTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregated_RemovedForgottenMetrics = new ArrayList<>(statsdMetricsAggregatedNotGauges_RemovedForgottenMetrics);
            statsdMetricsAggregated_RemovedForgottenMetrics.addAll(statsdMetricsAggregatedGauges_RemovedForgottenMetrics);
            long mergeAggregatedMetricsTimeElasped = System.currentTimeMillis() - mergeAggregatedMetricsTimeStart; 
            
            // updates gauges db (blocking)
            long updateDatabaseTimeStart = System.currentTimeMillis();
            updateStatsdGaugesInDatabaseAndCache(statsdMetricsAggregatedGauges_RemovedForgottenMetrics);
            long updateDatabaseTimeElasped = System.currentTimeMillis() - updateDatabaseTimeStart; 
            
            // update the global lists of statsd's most recent aggregated values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(statsdMetricsAggregated_RemovedForgottenMetrics);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current aggregated values with the previous aggregated window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregatedMerged = mergePreviouslyAggregatedValuesWithCurrentAggregatedValues(statsdMetricsAggregated_RemovedForgottenMetrics, GlobalVariables.statsdMetricsAggregatedMostRecentValue);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 

            // removes any metrics that should have been discarded, but weren't because they were merged with the last run's values
            long forgetStatsdMetrics_Merged_TimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregatedMerged_RemovedForgottenMetrics = removeBucketsFromStatsdMetricsList(statsdMetricsAggregatedMerged, bucketsToForget);
            long forgetStatsdMetrics_Merged_TimeElasped = System.currentTimeMillis() - forgetStatsdMetrics_Merged_TimeStart;  
            
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps(statsdMetricsAggregated_RemovedForgottenMetrics, statsdMetricsAggregatedMerged_RemovedForgottenMetrics);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(statsdMetricsAggregatedMerged_RemovedForgottenMetrics);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart;                 

            // // make sure metric-keys that should be output-blacklist are blacklisted & remove output-blacklist metrics prior to outputting
            long outputBlacklistTimeStart = System.currentTimeMillis();
            long outputBlacklistNewlyProcessedMetricsCount = MetricAssociation.associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(threadId_, Common.getMetricKeysFromMetrics_List(statsdMetricsAggregated_RemovedForgottenMetrics));
            List<String> outputBlacklistMetricKeys = Common.getOutputBlacklistMetricKeys();
            if (outputBlacklistMetricKeys != null) bucketsToForget.addAll(outputBlacklistMetricKeys);
            List<StatsdMetricAggregated> statsdMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics = removeBucketsFromStatsdMetricsList(statsdMetricsAggregated_RemovedForgottenMetrics, bucketsToForget);
            long outputBlacklistTimeElasped = System.currentTimeMillis() - outputBlacklistTimeStart; 
            
            // send metrics to output modules
            if (!statsdMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics.isEmpty()) {
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllGraphiteOutputModules(statsdMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbTelnetOutputModules(statsdMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllOpenTsdbHttpOutputModules(statsdMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
                SendMetricsToOutputModule_ThreadPoolManager.sendMetricsToAllInfluxdbV1HttpOutputModules_NonNative(statsdMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics, threadId_);
            }
            
            // total time for this thread took to aggregate the metrics
            long timeAggregationTimeElasped = System.currentTimeMillis() - timeAggregationTimeStart - waitInMsCounter;
            String aggregationRate = "0";
            if (timeAggregationTimeElasped > 0) {
                aggregationRate = Long.toString(totalNumberOfStatsdMetrics / timeAggregationTimeElasped * 1000);
            }

            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + timeAggregationTimeElasped 
                    + ", RawMetricCount=" + totalNumberOfStatsdMetrics 
                    + ", RawMetricRatePerSec=" + (totalNumberOfStatsdMetrics / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", AggMetricCount=" + statsdMetricsAggregatedMerged.size()       
                    + ", OutputMetricCount=" + statsdMetricsAggregated_RemovedForgottenAndOutputBlacklistedMetrics.size() 
                    + ", MetricsProcessedPerSec=" + aggregationRate
                    + ", GetMetricsTime=" + getMetricsTimeElasped 
                    + ", UpdateDbTime=" + updateDatabaseTimeElasped 
                    + ", AggNotGaugeTime=" + aggregateNotGaugeTimeElasped 
                    + ", AggGaugeTime=" + aggregateGaugeTimeElasped 
                    + ", AggMergeMetricTime=" + mergeAggregatedMetricsTimeElasped 
                    + ", OutputBlacklistTime=" + outputBlacklistTimeElasped
                    + ", OutputBlacklistNewAssociationCount=" + outputBlacklistNewlyProcessedMetricsCount
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped 
                    + ", AggNewAndOldMetricCount=" + statsdMetricsAggregatedMerged_RemovedForgottenMetrics.size() 
                    + ", ForgetMetricsTime=" + (forgetStatsdMetricsTimeElasped + forgetStatsdMetrics_Merged_TimeElasped);
            
            if (statsdMetricsAggregatedMerged.isEmpty()) logger.debug(aggregationStatistics);
            else logger.info(aggregationStatistics);

            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregatedMerged_RemovedForgottenMetrics) {
                    logger.info("StatsdAggregatedMetric=\"" + statsdMetricAggregated.toString() + "\"");
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            activeStatsdAggregationThreadStartTimestamps.remove(threadStartTimestampInMilliseconds_);
        }
        
    }
    
    // gets statsd metrics for this thread to aggregate
    // also removes metrics from the statsd metrics map (since they are being operated on by this thread)
    private List<StatsdMetric> getCurrentStatsdMetricsAndRemoveMetricsFromGlobal(ConcurrentHashMap<Long,StatsdMetric> statsdMetrics) {

        if (statsdMetrics == null) {
            return new ArrayList();
        }

        List<StatsdMetric> statsdMetricsToReturn = new ArrayList(statsdMetrics.size());
        
        for (StatsdMetric statsdMetric : statsdMetrics.values()) {
            if (statsdMetric.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                statsdMetricsToReturn.add(statsdMetric);
                statsdMetrics.remove(statsdMetric.getHashKey());
            }
        }

        return statsdMetricsToReturn;
    }
    
    private void updateStatsdGaugesInDatabaseAndCache(List<StatsdMetricAggregated> statsdMetricsAggregatedGauges) {
        
        if ((statsdMetricsAggregatedGauges == null) || statsdMetricsAggregatedGauges.isEmpty() || !ApplicationConfiguration.isStatsdGaugeSendPreviousValue()) {
            return;
        }
        
        int arrayListInitialSize = (int) (statsdMetricsAggregatedGauges.size() * 1.3);
        List<Gauge> gaugesToPutInDatabase = new ArrayList<>(arrayListInitialSize);
        
        for (StatsdMetricAggregated statsdMetricsAggregatedGauge : statsdMetricsAggregatedGauges) {
            Gauge gaugeFromCache = GlobalVariables.statsdGaugeCache.get(statsdMetricsAggregatedGauge.getBucket());
            String bucketSha1;
            if (gaugeFromCache == null) bucketSha1 = DigestUtils.sha1Hex(statsdMetricsAggregatedGauge.getBucket());
            else bucketSha1 = gaugeFromCache.getBucketSha1();
            
            Timestamp gaugeTimestamp = new Timestamp(statsdMetricsAggregatedGauge.getTimestampInMilliseconds());
            
            Gauge gauge = new Gauge(bucketSha1, statsdMetricsAggregatedGauge.getBucket(), statsdMetricsAggregatedGauge.getMetricValue(), gaugeTimestamp);
            
            if (gauge.isValid()) {
                gaugesToPutInDatabase.add(gauge);

                // put new gauge value in local cache
                GlobalVariables.statsdGaugeCache.put(statsdMetricsAggregatedGauge.getBucket(), gauge);
            }
        }
        
        if (ApplicationConfiguration.isStatsdPersistGauges()) {
            GaugesDao gaugesDao = new GaugesDao(false);
            boolean upsertSucess = gaugesDao.batchUpsert(gaugesToPutInDatabase);
            gaugesDao.close();
            
            if (!upsertSucess) {
                logger.error("Failed upserting gauges in database.");
            }
        }
    }
    
    private void updateMetricMostRecentValues(List<StatsdMetricAggregated> statsdMetricsAggregated) {
        
        long timestampInMilliseconds = System.currentTimeMillis();
        
        if (GlobalVariables.statsdMetricsAggregatedMostRecentValue != null) {
            for (StatsdMetricAggregated statsdMetricAggregated : GlobalVariables.statsdMetricsAggregatedMostRecentValue.values()) {
                StatsdMetricAggregated updatedStatsdMetricAggregated = null;
                
                if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.COUNTER_TYPE) && ApplicationConfiguration.isStatsdCounterSendZeroOnInactive()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), BigDecimal.ZERO, timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.TIMER_TYPE) && ApplicationConfiguration.isStatsdTimerSendZeroOnInactive()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), BigDecimal.ZERO, timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.GAUGE_TYPE) && ApplicationConfiguration.isStatsdGaugeSendPreviousValue()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), statsdMetricAggregated.getMetricValue(), timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.SET_TYPE) && ApplicationConfiguration.isStatsdSetSendZeroOnInactive()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), BigDecimal.ZERO, timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                }

                if (updatedStatsdMetricAggregated != null) {
                    updatedStatsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    GlobalVariables.statsdMetricsAggregatedMostRecentValue.put(updatedStatsdMetricAggregated.getBucket(), updatedStatsdMetricAggregated);
                }
            }
        }
        
        if ((statsdMetricsAggregated == null) || statsdMetricsAggregated.isEmpty()) {
            return;
        }
        
        if (GlobalVariables.statsdMetricsAggregatedMostRecentValue != null) {
            for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
                if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.COUNTER_TYPE) && ApplicationConfiguration.isStatsdCounterSendZeroOnInactive()) {
                    GlobalVariables.statsdMetricsAggregatedMostRecentValue.put(statsdMetricAggregated.getBucket(), statsdMetricAggregated);
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.TIMER_TYPE) && ApplicationConfiguration.isStatsdTimerSendZeroOnInactive()) {
                    GlobalVariables.statsdMetricsAggregatedMostRecentValue.put(statsdMetricAggregated.getBucket(), statsdMetricAggregated);
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.GAUGE_TYPE) && ApplicationConfiguration.isStatsdGaugeSendPreviousValue()) {
                    GlobalVariables.statsdMetricsAggregatedMostRecentValue.put(statsdMetricAggregated.getBucket(), statsdMetricAggregated);
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.SET_TYPE) && ApplicationConfiguration.isStatsdSetSendZeroOnInactive()) {
                    GlobalVariables.statsdMetricsAggregatedMostRecentValue.put(statsdMetricAggregated.getBucket(), statsdMetricAggregated);
                }
            }
        }

    }
    
    private List<StatsdMetricAggregated> mergePreviouslyAggregatedValuesWithCurrentAggregatedValues(List<StatsdMetricAggregated> statsdMetricsAggregatedNew, 
            Map<String,StatsdMetricAggregated> statsdMetricsAggregatedOld) {
        
        if ((statsdMetricsAggregatedNew == null) && (statsdMetricsAggregatedOld == null)) {
            return new ArrayList<>();
        }
        else if ((statsdMetricsAggregatedNew == null) && (statsdMetricsAggregatedOld != null)) {
            return new ArrayList<>(statsdMetricsAggregatedOld.values());
        }
        else if ((statsdMetricsAggregatedNew != null) && (statsdMetricsAggregatedOld == null)) {
            return statsdMetricsAggregatedNew;
        }
        
        List<StatsdMetricAggregated> statsdMetricsAggregatedMerged = new ArrayList<>(statsdMetricsAggregatedNew);
        Map<String,StatsdMetricAggregated> statsdMetricsAggregatedOldLocal = new HashMap<>(statsdMetricsAggregatedOld);
        
        for (StatsdMetricAggregated statsdMetricAggregatedNew : statsdMetricsAggregatedNew) {
            try {
                statsdMetricsAggregatedOldLocal.remove(statsdMetricAggregatedNew.getBucket());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            } 
        }
        
        statsdMetricsAggregatedMerged.addAll(statsdMetricsAggregatedOldLocal.values());
        
        return statsdMetricsAggregatedMerged;
    }

    public static List<StatsdMetricAggregated> removeBucketsFromStatsdMetricsList(List<StatsdMetricAggregated> statsdMetrics, Set<String> bucketsToRemove) {
        
        if ((statsdMetrics == null) || statsdMetrics.isEmpty() || (bucketsToRemove == null) || bucketsToRemove.isEmpty()) {
            return statsdMetrics;
        }
        
        List<StatsdMetricAggregated> statsdMetrics_WithMetricsRemoved = new ArrayList<>(statsdMetrics.size());

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetrics) {
            String bucket = statsdMetricAggregated.getBucket();
            
            if ((bucket != null) && !bucketsToRemove.contains(bucket)) {
                statsdMetrics_WithMetricsRemoved.add(statsdMetricAggregated);
            }
        }

        return statsdMetrics_WithMetricsRemoved;
    }
    
}
