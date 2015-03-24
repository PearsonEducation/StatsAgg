package com.pearson.statsagg.metric_aggregation.threads;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.database.gauges.Gauge;
import com.pearson.statsagg.database.gauges.GaugesDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricAggregator;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricRaw;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.webui.StatsAggHtmlFramework;
import java.math.BigDecimal;
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
            
            // get metrics for aggregation
            long createMetricsTimeStart = System.currentTimeMillis();
            List<StatsdMetricRaw> statsdMetricsRaw = getCurrentStatsdMetricsAndRemoveMetricsFromGlobal();
            List<StatsdMetricRaw> statsdMetricsRawGauges = StatsdMetricAggregator.getStatsdMetricsRawByMetricTypeKey(statsdMetricsRaw, StatsdMetricRaw.GAUGE_TYPE);
            List<StatsdMetricRaw> statsdMetricsRawNotGauges = StatsdMetricAggregator.getStatsdMetricsRawExcludeMetricTypeKey(statsdMetricsRaw, StatsdMetricRaw.GAUGE_TYPE);
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
            
            // aggregate everything except gauges
            long aggregateNotGaugeTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregatedNotGauges = StatsdMetricAggregator.aggregateStatsdMetrics(statsdMetricsRawNotGauges);
            long aggregateNotGaugeTimeElasped = System.currentTimeMillis() - aggregateNotGaugeTimeStart; 
            
            // wait until this is the youngest active thread
            waitInMsCounter += Common.waitUntilThisIsYoungestActiveThread(threadStartTimestampInMilliseconds_, activeStatsdAggregationThreadStartTimestamps);
            
           // aggregate gauges
            long aggregateGaugeTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregatedGauges = StatsdMetricAggregator.aggregateStatsdMetrics(statsdMetricsRawGauges);
            long aggregateGaugeTimeElasped = System.currentTimeMillis() - aggregateGaugeTimeStart; 
            
            // merge aggregated non-aggregatedGauge & aggregateGauge metrics
            long mergeAggregatedMetricsTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>(statsdMetricsAggregatedNotGauges);
            statsdMetricsAggregated.addAll(statsdMetricsAggregatedGauges);
            long mergeAggregatedMetricsTimeElasped = System.currentTimeMillis() - mergeAggregatedMetricsTimeStart; 
            
            // updates gauges db (blocking)
            long updateDatabaseTimeStart = System.currentTimeMillis();
            updateStatsdGaugesInDatabaseAndCache(statsdMetricsAggregatedGauges);
            long updateDatabaseTimeElasped = System.currentTimeMillis() - updateDatabaseTimeStart; 
            
            // update the global lists of statsd's most recent aggregated values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(statsdMetricsAggregated);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current aggregated values with the previous aggregated window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<StatsdMetricAggregated> statsdMetricsAggregatedMerged = mergePreviouslyAggregatedValuesWithCurrentAggregatedValues(statsdMetricsAggregated, 
                    GlobalVariables.statsdMetricsAggregatedMostRecentValue);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 

            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            Common.updateMetricLastSeenTimestamps(statsdMetricsAggregated);
            Common.updateMetricLastSeenTimestamps_UpdateOnResend(statsdMetricsAggregatedMerged);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(statsdMetricsAggregatedMerged);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart;                 
                    
            // 'forget' metrics
            long forgetStatsdMetricsTimeStart = System.currentTimeMillis();
            forgetStatsdMetrics();
            long forgetStatsdMetricsTimeElasped = System.currentTimeMillis() - forgetStatsdMetricsTimeStart;  

            // send to graphite
            if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) {
                SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(statsdMetricsAggregatedMerged, threadId_, ApplicationConfiguration.getFlushTimeAgg());
            }
            
            // send to opentsdb via telnet
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbTelnetOutputModuleEnabled()) {
                SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbTelnetEndpoints(statsdMetricsAggregatedMerged, threadId_);
            }
            
            // total time for this thread took to aggregate the statsd metrics
            long timeAggregationTimeElasped = System.currentTimeMillis() - timeAggregationTimeStart - waitInMsCounter;
            String aggregationRate = "0";
            if (timeAggregationTimeElasped > 0) {
                aggregationRate = Long.toString(statsdMetricsRaw.size() / timeAggregationTimeElasped * 1000);
            }

            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", NewRawMetricCount=" + statsdMetricsRaw.size() 
                    + ", AggMetricCount=" + statsdMetricsAggregated.size()                    
                    + ", AggTotalTime=" + timeAggregationTimeElasped 
                    + ", AggMetricsPerSec=" + aggregationRate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped 
                    + ", UpdateDbTime=" + updateDatabaseTimeElasped 
                    + ", AggNotGaugeTime=" + aggregateNotGaugeTimeElasped 
                    + ", AggGaugeTime=" + aggregateGaugeTimeElasped 
                    + ", AggMergeMetricTime=" + mergeAggregatedMetricsTimeElasped 
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped 
                    + ", AggNewAndOldMetricCount=" + statsdMetricsAggregatedMerged.size() 
                    + ", ForgetMetricsTime=" + forgetStatsdMetricsTimeElasped;
            
            if (statsdMetricsAggregatedMerged.isEmpty()) {
                logger.debug(aggregationStatistics);
            }
            else {
                logger.info(aggregationStatistics);
            }

            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregatedMerged) {
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

    private List<StatsdMetricRaw> getCurrentStatsdMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.statsdMetricsRaw == null) {
            return new ArrayList();
        }

        // gets statsd metrics for this thread to aggregate & send to statsd
        List<StatsdMetricRaw> statsdMetricsRaw = new ArrayList(GlobalVariables.statsdMetricsRaw.size());
        
        for (StatsdMetricRaw statsdMetricRaw : GlobalVariables.statsdMetricsRaw.values()) {
            if (statsdMetricRaw.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                statsdMetricsRaw.add(statsdMetricRaw);
            }
        }
        
        // removes metrics from the global statsd metrics map (since they are being operated on by this thread)
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            GlobalVariables.statsdMetricsRaw.remove(statsdMetricRaw.getHashKey());
        }

        return statsdMetricsRaw;
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
        
        GaugesDao gaugesDao = new GaugesDao(false);
        boolean upsertSucess = gaugesDao.batchUpsert(gaugesToPutInDatabase);
        gaugesDao.close();
        
        if (!upsertSucess) {
            logger.error("Failed upserting gauges in database.");
        }
    }
    
    private void updateMetricMostRecentValues(List<StatsdMetricAggregated> statsdMetricsAggregated) {
        
        long timestampInMilliseconds = System.currentTimeMillis();
        
        if (GlobalVariables.statsdMetricsAggregatedMostRecentValue != null) {
            for (StatsdMetricAggregated statsdMetricAggregated : GlobalVariables.statsdMetricsAggregatedMostRecentValue.values()) {
                StatsdMetricAggregated updatedStatsdMetricAggregated = null;
                
                if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.COUNTER_TYPE) && ApplicationConfiguration.isStatsdCounterSendZeroOnInactive()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), BigDecimal.ZERO, timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                    updatedStatsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.TIMER_TYPE) && ApplicationConfiguration.isStatsdTimerSendZeroOnInactive()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), BigDecimal.ZERO, timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                    updatedStatsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.GAUGE_TYPE) && ApplicationConfiguration.isStatsdGaugeSendPreviousValue()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), statsdMetricAggregated.getMetricValue(), timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                    updatedStatsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
                }
                else if ((statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.SET_TYPE) && ApplicationConfiguration.isStatsdSetSendZeroOnInactive()) {
                    updatedStatsdMetricAggregated = new StatsdMetricAggregated(statsdMetricAggregated.getBucket(), BigDecimal.ZERO, timestampInMilliseconds, statsdMetricAggregated.getMetricTypeKey());
                    updatedStatsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
                }

                if (updatedStatsdMetricAggregated != null) {
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
    
    private void forgetStatsdMetrics() {

        HashSet<String> bucketsToForget = new HashSet<>();
        
        // gets a list of complete buckets to forget
        if (GlobalVariables.forgetStatsdMetrics != null) { 
            Set<String> forgetStatsdMetrics = new HashSet<>(GlobalVariables.forgetStatsdMetrics.keySet());
            
            for (String bucket : forgetStatsdMetrics) {
                bucketsToForget.add(bucket);
                GlobalVariables.forgetStatsdMetrics.remove(bucket);
            }
        }
        
        // gets a list of buckets to forget by matching against regexs
        if (GlobalVariables.forgetStatsdMetricsRegexs != null) {     
            Set<String> forgetStatsdMetricsRegexs = new HashSet<>(GlobalVariables.forgetStatsdMetricsRegexs.keySet());
            
            for (String bucketRegex : forgetStatsdMetricsRegexs) {
                Set<String> regexBucketsToForget = Common.forgetGenericMetrics_IdentifyMetricPathsViaRegex(bucketRegex, GlobalVariables.statsdMetricsAggregatedMostRecentValue);
                
                if (regexBucketsToForget != null) {
                    bucketsToForget.addAll(regexBucketsToForget);
                }
                
                GlobalVariables.forgetStatsdMetricsRegexs.remove(bucketRegex);
            }
        }

        // 'forgets' the statsd metrics
        if (!bucketsToForget.isEmpty()) {
            GaugesDao gaugesDao = new GaugesDao(false);
            forgetStatsdMetrics_Forget(bucketsToForget, gaugesDao);
            gaugesDao.close();
        }
        
    }

    private void forgetStatsdMetrics_Forget(Set<String> bucketsToForget, GaugesDao gaugesDao) {
        
        if ((bucketsToForget == null) || bucketsToForget.isEmpty() || (GlobalVariables.statsdMetricsAggregatedMostRecentValue == null)) {
            return;
        }
            
        for (String bucketToForget : bucketsToForget) {
            GlobalVariables.immediateCleanupMetrics.put(bucketToForget, bucketToForget);            
            StatsdMetricAggregated statsdMetricAggregated = GlobalVariables.statsdMetricsAggregatedMostRecentValue.get(bucketToForget);
            
            if ((statsdMetricAggregated != null) && (statsdMetricAggregated.getMetricTypeKey() == StatsdMetricAggregated.GAUGE_TYPE)) {
                if (gaugesDao != null) {
                    Gauge gauge = GlobalVariables.statsdGaugeCache.get(bucketToForget);
                    String bucketSha1;
                    if (gauge == null) bucketSha1 = DigestUtils.sha1Hex(bucketToForget);
                    else bucketSha1 = gauge.getBucketSha1();
                    
                    boolean deleteSuccess = false;
                    if (bucketSha1 != null) deleteSuccess = gaugesDao.delete(bucketSha1);

                    if ((bucketSha1 != null) && deleteSuccess) {
                        GlobalVariables.statsdMetricsAggregatedMostRecentValue.remove(bucketToForget);
                        GlobalVariables.statsdGaugeCache.remove(bucketToForget);
                    }  
                    else {
                        String cleanBucketToForget = StatsAggHtmlFramework.removeNewlinesFromString(bucketToForget);
                        logger.error("Failed deleting gauge from the database. Gauge=\"" + cleanBucketToForget + "\"");
                    }
                }
            }
            else {
                GlobalVariables.statsdMetricsAggregatedMostRecentValue.remove(bucketToForget);
            }
        }
    }
    
}
