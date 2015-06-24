package com.pearson.statsagg.controller.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.InfluxdbV1HttpOutputModule;
import com.pearson.statsagg.metric_aggregation.threads.SendMetricsToInfluxdbV1Thread;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetricFormat_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.utilities.StackTrace;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendToInfluxdbV1ThreadPoolManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SendToInfluxdbV1ThreadPoolManager.class.getName());
    
    private static final Object lock_ = new Object();
    private static ExecutorService threadExecutor_ = Executors.newCachedThreadPool();
    
    public static void start() {
        synchronized(lock_) {
            if ((threadExecutor_ == null) || ((!threadExecutor_.isShutdown()) && (!threadExecutor_.isTerminated()))) {
                threadExecutor_ = Executors.newCachedThreadPool();
            }
            else {
                logger.info("Can't create new thread pool - current thread pool isn't terminated");
            }
        }
    }
    
    public static void shutdown() {
        synchronized(lock_) {
            if (threadExecutor_ == null) {
                return;
            }

            try {
                threadExecutor_.shutdown();

                // reusing the termination delay time from the aggregation invokers because it makes sense in this context
                threadExecutor_.awaitTermination((2 * ApplicationConfiguration.getFlushTimeAgg()) + 3000, TimeUnit.MILLISECONDS);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
    }

    public static void executeThread(SendMetricsToInfluxdbV1Thread sendMetricsToInfluxdbV1Thread) {
        try {
            if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) {
                threadExecutor_.execute(sendMetricsToInfluxdbV1Thread);
            }
            else {
                logger.warn("The thread pool is not in a state that can execute the requested thread.");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    public static boolean isAnyInfluxdbV1HttpOutputModuleEnabled() {
        
        List<InfluxdbV1HttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbV1HttpOutputModules();
        if (influxdbHttpOutputModules == null) return false;
        
        for (InfluxdbV1HttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
            if (influxdbHttpOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<InfluxdbV1HttpOutputModule> getEnabledInfluxdbV1HttpOutputModules() {
        
        List<InfluxdbV1HttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbV1HttpOutputModules();
        if (influxdbHttpOutputModules == null) return new ArrayList<>();
        
        List<InfluxdbV1HttpOutputModule> enabledInfluxdbOutputModules = new ArrayList<>();
        
        for (InfluxdbV1HttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
            if (influxdbHttpOutputModule.isOutputEnabled()) {
                enabledInfluxdbOutputModules.add(influxdbHttpOutputModule);
            }
        }
        
        return enabledInfluxdbOutputModules;
    }
    
    /* 
    Use this method to output 'non-native' InfluxDB metrics. 'Non-native' InfluxDB metrics are metrics that were NOT originally received by the InfluxDB listener.
    Ex -- Graphite, OpenTSDB, etc
    */
    public static void sendMetricsToAllInfluxdbHttpOutputModules_NonNative(List<? extends InfluxdbMetricFormat_v1> influxdbMetrics, String threadId) {
        
        try {
            List<InfluxdbV1HttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbV1HttpOutputModules();
            if (influxdbHttpOutputModules == null) return;
                    
            for (InfluxdbV1HttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
                if (!influxdbHttpOutputModule.isOutputEnabled()) continue;
                
                URL influxdbBaseUrl = new URL(influxdbHttpOutputModule.getUrl());
                
                SendMetricsToInfluxdbV1Thread sendMetricsToHttpInfluxdbThread = new SendMetricsToInfluxdbV1Thread(influxdbMetrics, influxdbBaseUrl, 
                        ApplicationConfiguration.getInfluxdbDefaultDatabaseName(), ApplicationConfiguration.getInfluxdbDefaultDatabaseHttpBasicAuthValue(), 
                        influxdbHttpOutputModule.getNumSendRetryAttempts(), influxdbHttpOutputModule.getMaxMetricsPerMessage(),
                        threadId);
                
                SendToInfluxdbV1ThreadPoolManager.executeThread(sendMetricsToHttpInfluxdbThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    /* 
    Use this method to output 'native' InfluxDB metrics. 'Native' InfluxDB metrics are metrics that were originally received by the InfluxDB listener.
    */
    public static void sendMetricsToAllInfluxdbHttpOutputModules_Native(List<InfluxdbMetric_v1> influxdbMetrics, String threadId) {
        
        try {
            List<InfluxdbV1HttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbV1HttpOutputModules();
            if (influxdbHttpOutputModules == null) return;
                    
            for (InfluxdbV1HttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
                if (!influxdbHttpOutputModule.isOutputEnabled()) continue;
                
                URL influxdbBaseUrl = new URL(influxdbHttpOutputModule.getUrl());
                
                SendMetricsToInfluxdbV1Thread sendMetricsToHttpInfluxdbThread = new SendMetricsToInfluxdbV1Thread(influxdbMetrics, influxdbBaseUrl, 
                        influxdbHttpOutputModule.getNumSendRetryAttempts(), threadId);
                
                SendToInfluxdbV1ThreadPoolManager.executeThread(sendMetricsToHttpInfluxdbThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
}
