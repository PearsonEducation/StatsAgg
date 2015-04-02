package com.pearson.statsagg.metric_aggregation.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.opentsdb.OpenTsdbMetricRaw;
import com.pearson.statsagg.utilities.StackTrace;
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
            
            // get metrics for aggregation
            long createMetricsTimeStart = System.currentTimeMillis();
            List<OpenTsdbMetricRaw> openTsdbMetricsRaw = getCurrentOpenTsdbMetricsAndRemoveMetricsFromGlobal();
            long createMetricsTimeElasped = System.currentTimeMillis() - createMetricsTimeStart; 
                    
            // update the global lists of the opentsdb's most recent values
            long updateMostRecentDataValueForMetricsTimeStart = System.currentTimeMillis();
            updateMetricMostRecentValues(openTsdbMetricsRaw);
            long updateMostRecentDataValueForMetricsTimeElasped = System.currentTimeMillis() - updateMostRecentDataValueForMetricsTimeStart; 
            
            // merge current values with the previous window's values (if the application is configured to do this)
            long mergeRecentValuesTimeStart = System.currentTimeMillis();
            List<OpenTsdbMetricRaw> openTsdbMetricsRawMerged = mergePreviousValuesWithCurrentValues(openTsdbMetricsRaw, GlobalVariables.openTsdbMetricsMostRecentValue);
            long mergeRecentValuesTimeElasped = System.currentTimeMillis() - mergeRecentValuesTimeStart; 
  
            // updates the global lists that track the last time a metric was received. 
            long updateMetricLastSeenTimestampTimeStart = System.currentTimeMillis();
            if (ApplicationConfiguration.isOpenTsdbSendPreviousValue()) {
                Common.updateMetricLastSeenTimestamps_MostRecentNew(openTsdbMetricsRaw);
                Common.updateMetricLastSeenTimestamps_UpdateOnResend(openTsdbMetricsRawMerged);
            }
            else Common.updateMetricLastSeenTimestamps_UpdateOnResend_And_MostRecentNew(openTsdbMetricsRawMerged);
            long updateMetricLastSeenTimestampTimeElasped = System.currentTimeMillis() - updateMetricLastSeenTimestampTimeStart; 
            
            // updates metric value recent value history. this stores the values that are used by the alerting thread.
            long updateAlertMetricKeyRecentValuesTimeStart = System.currentTimeMillis();
            Common.updateAlertMetricRecentValues(openTsdbMetricsRawMerged);
            long updateAlertMetricKeyRecentValuesTimeElasped = System.currentTimeMillis() - updateAlertMetricKeyRecentValuesTimeStart; 

            // 'forget' metrics
            long forgetOpenTsdbMetricsTimeStart = System.currentTimeMillis();
            Common.forgetGenericMetrics(GlobalVariables.forgetOpenTsdbMetrics, GlobalVariables.forgetOpenTsdbMetricsRegexs, GlobalVariables.openTsdbMetricsMostRecentValue, GlobalVariables.immediateCleanupMetrics);
            long forgetOpenTsdbMetricsTimeElasped = System.currentTimeMillis() - forgetOpenTsdbMetricsTimeStart;  
            
            // send to graphite
            if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) {
                SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(openTsdbMetricsRawMerged, threadId_, ApplicationConfiguration.getFlushTimeAgg());
            }
            
            // send to opentsdb via telnet
            if (SendMetricsToOpenTsdbThread.isAnyOpenTsdbTelnetOutputModuleEnabled()) {
                SendMetricsToOpenTsdbThread.sendMetricsToOpenTsdbTelnetEndpoints(openTsdbMetricsRawMerged, threadId_);
            }
            
            // total time for this thread took to get & send the graphite metrics
            long threadTimeElasped = System.currentTimeMillis() - threadTimeStart - waitInMsCounter;
            String rate = "0";
            if (threadTimeElasped > 0) {
                rate = Long.toString(openTsdbMetricsRaw.size() / threadTimeElasped * 1000);
            }
            
            String aggregationStatistics = "ThreadId=" + threadId_
                    + ", AggTotalTime=" + threadTimeElasped 
                    + ", RawMetricCount=" + openTsdbMetricsRaw.size() 
                    + ", RawMetricRatePerSec=" + (openTsdbMetricsRaw.size() / ApplicationConfiguration.getFlushTimeAgg() * 1000)
                    + ", MetricsProcessedPerSec=" + rate
                    + ", CreateMetricsTime=" + createMetricsTimeElasped
                    + ", UpdateRecentValuesTime=" + updateMostRecentDataValueForMetricsTimeElasped 
                    + ", UpdateMetricsLastSeenTime=" + updateMetricLastSeenTimestampTimeElasped 
                    + ", UpdateAlertRecentValuesTime=" + updateAlertMetricKeyRecentValuesTimeElasped
                    + ", MergeNewAndOldMetricsTime=" + mergeRecentValuesTimeElasped
                    + ", NewAndOldMetricCount=" + openTsdbMetricsRawMerged.size() 
                    + ", ForgetMetricsTime=" + forgetOpenTsdbMetricsTimeElasped;
            
            if (openTsdbMetricsRawMerged.isEmpty()) {
                logger.debug(aggregationStatistics);
            }
            else {
                logger.info(aggregationStatistics);
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                for (OpenTsdbMetricRaw openTsdbMetricRaw : openTsdbMetricsRaw) {
                    logger.info("OpenTsdb metric= " + openTsdbMetricRaw.toString());
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    private List<OpenTsdbMetricRaw> getCurrentOpenTsdbMetricsAndRemoveMetricsFromGlobal() {

        if (GlobalVariables.openTsdbMetricsRaw == null) {
            return new ArrayList();
        }

        // gets opentsdb metrics for this thread 
        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = new ArrayList(GlobalVariables.openTsdbMetricsRaw.size());
        
        for (OpenTsdbMetricRaw openTsdbMetricRaw : GlobalVariables.openTsdbMetricsRaw.values()) {
            if (openTsdbMetricRaw.getMetricReceivedTimestampInMilliseconds() <= threadStartTimestampInMilliseconds_) {
                openTsdbMetricsRaw.add(openTsdbMetricRaw);
            }
        }
        
        // removes metrics from the global opentsdb metrics map (since they are being operated on by this thread)
        for (OpenTsdbMetricRaw openTsdbMetricRaw : openTsdbMetricsRaw) {
            GlobalVariables.openTsdbMetricsRaw.remove(openTsdbMetricRaw.getHashKey());
        }

        return openTsdbMetricsRaw;
    }
    
    private void updateMetricMostRecentValues(List<OpenTsdbMetricRaw> openTsdbMetricsRaw) {
        
        if (GlobalVariables.openTsdbMetricsMostRecentValue != null) {
            long timestampInMilliseconds = System.currentTimeMillis();
            int timestampInSeconds;
            String timestampInSecondsString, timestampInMillisecondsString;
            
            for (OpenTsdbMetricRaw openTsdbMetricRaw : GlobalVariables.openTsdbMetricsMostRecentValue.values()) {
                OpenTsdbMetricRaw updatedOpenTsdbMetricRaw;
                
                if (openTsdbMetricRaw.getMetricTimestamp().length() == 10) {
                    timestampInSeconds = (int) (timestampInMilliseconds / 1000);
                    timestampInSecondsString = Integer.toString(timestampInSeconds);
                    
                    updatedOpenTsdbMetricRaw = new OpenTsdbMetricRaw(openTsdbMetricRaw.getMetric(), timestampInSecondsString,
                            openTsdbMetricRaw.getMetricValue(), openTsdbMetricRaw.getTags(), timestampInMilliseconds,
                            openTsdbMetricRaw.getMetricKey(), timestampInMilliseconds, openTsdbMetricRaw.getMetricValueBigDecimal());
                }
                else {
                    timestampInMillisecondsString = Long.toString(timestampInMilliseconds);
                    
                    updatedOpenTsdbMetricRaw = new OpenTsdbMetricRaw(openTsdbMetricRaw.getMetric(), timestampInMillisecondsString,
                            openTsdbMetricRaw.getMetricValue(), openTsdbMetricRaw.getTags(), timestampInMilliseconds,
                            openTsdbMetricRaw.getMetricKey(), timestampInMilliseconds, openTsdbMetricRaw.getMetricValueBigDecimal());
                }

                updatedOpenTsdbMetricRaw.setHashKey(openTsdbMetricRaw.getHashKey());
                
                GlobalVariables.openTsdbMetricsMostRecentValue.put(updatedOpenTsdbMetricRaw.getMetricKey(), updatedOpenTsdbMetricRaw);
            }
        }

        if ((openTsdbMetricsRaw == null) || openTsdbMetricsRaw.isEmpty()) {
            return;
        }
        
        if ((GlobalVariables.openTsdbMetricsMostRecentValue != null) && ApplicationConfiguration.isOpenTsdbSendPreviousValue()) {
            Map<String,OpenTsdbMetricRaw> mostRecentOpenTsdbMetricsByMetricPath = OpenTsdbMetricRaw.getMostRecentOpenTsdbMetricRawByMetricKey(openTsdbMetricsRaw);
            
            for (OpenTsdbMetricRaw openTsdbMetricRaw : mostRecentOpenTsdbMetricsByMetricPath.values()) {
                GlobalVariables.openTsdbMetricsMostRecentValue.put(openTsdbMetricRaw.getMetricKey(), openTsdbMetricRaw);
            }
        }
            
    }    
    
    private List<OpenTsdbMetricRaw> mergePreviousValuesWithCurrentValues(List<OpenTsdbMetricRaw> openTsdbMetricsRawNew, 
            Map<String,OpenTsdbMetricRaw> openTsdbMetricsRawOld) {
        
        if ((openTsdbMetricsRawNew == null) && (openTsdbMetricsRawOld == null)) {
            return new ArrayList<>();
        }
        else if ((openTsdbMetricsRawNew == null) && (openTsdbMetricsRawOld != null)) {
            return new ArrayList<>(openTsdbMetricsRawOld.values());
        }
        else if ((openTsdbMetricsRawNew != null) && (openTsdbMetricsRawOld == null)) {
            return openTsdbMetricsRawNew;
        }
        else if (!ApplicationConfiguration.isOpenTsdbSendPreviousValue()) {
            return openTsdbMetricsRawNew;
        }
        
        List<OpenTsdbMetricRaw> openTsdbMetricsRawMerged = new ArrayList<>(openTsdbMetricsRawNew);
        Map<String,OpenTsdbMetricRaw> openTsdbMetricsRawOldLocal = new HashMap<>(openTsdbMetricsRawOld);
        
        for (OpenTsdbMetricRaw openTsdbMetricAggregatedNew : openTsdbMetricsRawNew) {
            try {
                openTsdbMetricsRawOldLocal.remove(openTsdbMetricAggregatedNew.getMetricKey());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            } 
        }
        
        openTsdbMetricsRawMerged.addAll(openTsdbMetricsRawOldLocal.values());
        
        return openTsdbMetricsRawMerged;
    }

}
