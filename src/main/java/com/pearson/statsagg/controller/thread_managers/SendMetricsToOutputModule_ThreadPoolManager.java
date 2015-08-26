package com.pearson.statsagg.controller.thread_managers;

import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.metric_formats.graphite.GraphiteOutputModule;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbV1HttpOutputModule;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbHttpOutputModule;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbTelnetOutputModule;
import com.pearson.statsagg.metric_formats.graphite.SendMetricsToGraphiteThread;
import com.pearson.statsagg.metric_formats.influxdb.SendMetricsToInfluxdbV1Thread;
import com.pearson.statsagg.metric_formats.opentsdb.SendMetricsToOpenTsdbThread;
import com.pearson.statsagg.metric_formats.SendMetricsToOutputModuleThread;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetricFormat;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetricFormat_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.Threads;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToOutputModule_ThreadPoolManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToOutputModule_ThreadPoolManager.class.getName());
    
    private static final Object startupAndShutdownLock_ = new Object();
    private static final Object outputModuleThreadTrackerLock_ = new Object();
    private static ExecutorService threadExecutor_ = null;
    private static final AtomicBoolean isOpenForBusiness = new AtomicBoolean(false);
    
    private static final AtomicLong threadIdGenerator = new AtomicLong(0);
    private static final ConcurrentHashMap<Long,SendMetricsToOutputModule> sendMetricsToOutputModuleThreads = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,List<Long>> outputModuleThreadTracker = new ConcurrentHashMap<>();
    private static final AtomicBoolean continueRunningCleanup = new AtomicBoolean(false);
    
    private static Thread cleanupOutputThreads_Thread = null;
    
    public static void start() {
        synchronized(startupAndShutdownLock_) {
            if ((threadExecutor_ == null) || ((!threadExecutor_.isShutdown()) && (!threadExecutor_.isTerminated()))) {
                threadExecutor_ = Executors.newCachedThreadPool();
                isOpenForBusiness.set(true);

                continueRunningCleanup.set(true);
                cleanupOutputThreads_Thread = new Thread(new CleanupOutputThreads());
                cleanupOutputThreads_Thread.start();
            }
            else {
                logger.info("Can't create new thread pool - current thread pool isn't terminated");
            }
        }
    }
    
    public static void shutdown() {
        synchronized(startupAndShutdownLock_) {
            if (threadExecutor_ == null) {
                return;
            }
            
            isOpenForBusiness.set(false);
            
            Threads.shutdownThreadExecutor(threadExecutor_, ((2 * ApplicationConfiguration.getFlushTimeAgg()) + 3000), TimeUnit.MILLISECONDS, false, false);
            forceShutdown_SendMetricsToOutputModuleThreads();
            Threads.shutdownThreadExecutor(threadExecutor_, 1000, TimeUnit.MILLISECONDS, true, true);
            
            try {
                continueRunningCleanup.set(false);
                cleanupOutputThreads_Thread.join();
            }
            catch (Exception e) {}
            
            sendMetricsToOutputModuleThreads.clear();
            cleanupOutputThreads_Thread = null;
            threadExecutor_ = null;
        }
    }

    public static void executeThread(SendMetricsToOutputModuleThread sendMetricsToOutputModuleThread, String outputModuleId) {
        
        if (sendMetricsToOutputModuleThread == null) return;
        
        try {
            if (sendMetricsToOutputModuleThreads.size() >= ApplicationConfiguration.getOutputModuleMaxConcurrentThreads()) {
                logger.warn("Can't output because too many output threads are already running. ThreadId=\"" + sendMetricsToOutputModuleThread.getThreadId() + "\", "
                        + "OutputEndpoint=\"" + sendMetricsToOutputModuleThread.getOutputEndpoint() + "\"");
                
                return;
            }
            
            synchronized(outputModuleThreadTrackerLock_) {
                outputModuleThreadTracker.putIfAbsent(outputModuleId, Collections.synchronizedList(new ArrayList<Long>()));
                List<Long> threadIdsAssociatedWithOutputModule = outputModuleThreadTracker.get(outputModuleId);
                
                if (threadIdsAssociatedWithOutputModule.size() >= ApplicationConfiguration.getOutputModuleMaxConcurrentThreadsForSingleModule()) {
                    logger.warn("Can't output because too many instances of a particular output module are already running. "
                            + "ThreadId=\"" + sendMetricsToOutputModuleThread.getThreadId() + "\", "
                            + "OutputEndpoint=\"" + sendMetricsToOutputModuleThread.getOutputEndpoint() + "\", "
                            + "OutputModuleId=\"" + outputModuleId + "\"");

                    return;
                }
                
                if (isOpenForBusiness.get() && (threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) {
                    Thread sendMetricsToOutputModuleThread_Thread = new Thread(sendMetricsToOutputModuleThread);

                    threadExecutor_.execute(sendMetricsToOutputModuleThread_Thread);                

                    SendMetricsToOutputModule sendToOutputModule = new SendMetricsToOutputModule(sendMetricsToOutputModuleThread, sendMetricsToOutputModuleThread_Thread);
                    Long threadId = threadIdGenerator.getAndIncrement();
                    threadIdsAssociatedWithOutputModule.add(threadId);
                    sendMetricsToOutputModuleThreads.put(threadId, sendToOutputModule);
                }
                else {
                    logger.warn("The thread pool is not in a state that can execute the requested thread.");
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    private static void forceShutdown_SendMetricsToOutputModuleThreads() {
        for (SendMetricsToOutputModule sendMetricsToOutputModule : sendMetricsToOutputModuleThreads.values()) {
            sendMetricsToOutputModule.getSendMetricsToOutputModuleThread().shutdown();
        }
    }
    
    private static class CleanupOutputThreads implements Runnable {
    
        @Override
        public void run() {
            
            while (continueRunningCleanup.get() || !sendMetricsToOutputModuleThreads.isEmpty()) {
                int numThreadsCleanedUp = 0;
                
                for (Long threadId : sendMetricsToOutputModuleThreads.keySet()) {
                    SendMetricsToOutputModule sendToOutputModule = sendMetricsToOutputModuleThreads.get(threadId);
                    SendMetricsToOutputModuleThread sendMetricsToOutputModuleThread = sendToOutputModule.getSendMetricsToOutputModuleThread();
                    Thread sendMetricsToOutputModuleThread_Thread = sendToOutputModule.getSendMetricsToOutputModuleThread_Thread();
                    
                    if ((sendMetricsToOutputModuleThread != null) && (sendMetricsToOutputModuleThread.isFinished() || sendMetricsToOutputModuleThread.isShuttingDown())) {
                        if ((sendMetricsToOutputModuleThread_Thread != null) && !sendMetricsToOutputModuleThread_Thread.isAlive()) {
                            // remove the thread from the global track list of "how many total output modules are running"
                            sendMetricsToOutputModuleThreads.remove(threadId);
                            numThreadsCleanedUp++;
                            
                            // remove the thread from the per-output-module list of "how many of 'this particular' output modules are running"
                            for (String outputModuleId : outputModuleThreadTracker.keySet()) {
                                List<Long> threadIdsAssociatedWithOutputModule = outputModuleThreadTracker.get(outputModuleId);
                                if (threadIdsAssociatedWithOutputModule == null) continue;
                                threadIdsAssociatedWithOutputModule.remove(threadId);
                            }
                        }
                    }
                }
                
                if (numThreadsCleanedUp > 0) logger.debug("Cleaned up " + numThreadsCleanedUp + " output threads");
                
                Threads.sleepMilliseconds(50);
            }
            
        }
        
    }
    
    public static boolean isAnyGraphiteOutputModuleEnabled() {
        
        List<GraphiteOutputModule> graphiteOutuputModules = ApplicationConfiguration.getGraphiteOutputModules();
        if (graphiteOutuputModules == null) return false;
        
        for (GraphiteOutputModule graphiteOutputModule : graphiteOutuputModules) {
            if (graphiteOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<GraphiteOutputModule> getEnabledGraphiteOutputModules() {
        
        List<GraphiteOutputModule> graphiteOutuputModules = ApplicationConfiguration.getGraphiteOutputModules();
        if (graphiteOutuputModules == null) return new ArrayList<>();
        
        List<GraphiteOutputModule> enabledGraphiteOutputModules = new ArrayList<>();
        
        for (GraphiteOutputModule graphiteOutputModule : graphiteOutuputModules) {
            if (graphiteOutputModule.isOutputEnabled()) {
                enabledGraphiteOutputModules.add(graphiteOutputModule);
            }
        }
        
        return enabledGraphiteOutputModules;
    }

    public static void sendMetricsToAllGraphiteOutputModules(List<? extends GraphiteMetricFormat> graphiteMetrics, String threadId) {
        
        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty() || (threadId == null) || threadId.isEmpty()) {
            return;
        }
        
        try {
            List<GraphiteOutputModule> graphiteOutuputModules = ApplicationConfiguration.getGraphiteOutputModules();
            if (graphiteOutuputModules == null) return;
                    
            for (GraphiteOutputModule graphiteOutputModule : graphiteOutuputModules) {
                if (!graphiteOutputModule.isOutputEnabled()) continue;
                
                SendMetricsToGraphiteThread sendMetricsToGraphiteThread = new SendMetricsToGraphiteThread(graphiteMetrics, 
                        graphiteOutputModule.isSanitizeMetrics(), graphiteOutputModule.isSubstituteCharacters(),
                        graphiteOutputModule.getHost(), graphiteOutputModule.getPort(), ApplicationConfiguration.getOutputModuleMaxConnectTime(),  
                        graphiteOutputModule.getNumSendRetryAttempts(), graphiteOutputModule.getMaxMetricsPerMessage(), threadId);
            
                SendMetricsToOutputModule_ThreadPoolManager.executeThread(sendMetricsToGraphiteThread, graphiteOutputModule.getUniqueId());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static boolean isAnyOpenTsdbTelnetOutputModuleEnabled() {
        
        List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
        if (openTsdbTelnetOutputModules == null) return false;
        
        for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
            if (openTsdbTelnetOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<OpenTsdbTelnetOutputModule> getEnabledOpenTsdbTelnetOutputModules() {
        
        List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
        if (openTsdbTelnetOutputModules == null) return new ArrayList<>();
        
        List<OpenTsdbTelnetOutputModule> enabledOpenTsdbOutputModules = new ArrayList<>();
        
        for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
            if (openTsdbTelnetOutputModule.isOutputEnabled()) {
                enabledOpenTsdbOutputModules.add(openTsdbTelnetOutputModule);
            }
        }
        
        return enabledOpenTsdbOutputModules;
    }
    
    public static void sendMetricsToAllOpenTsdbTelnetOutputModules(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String threadId) {
        
        try {
            List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
            if (openTsdbTelnetOutputModules == null) return;
                    
            for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
                if (!openTsdbTelnetOutputModule.isOutputEnabled()) continue;
                
                SendMetricsToOpenTsdbThread sendMetricsToTelnetOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, openTsdbTelnetOutputModule.isSanitizeMetrics(), 
                        openTsdbTelnetOutputModule.getHost(), openTsdbTelnetOutputModule.getPort(), ApplicationConfiguration.getOutputModuleMaxConnectTime(),
                        openTsdbTelnetOutputModule.getNumSendRetryAttempts(), threadId);
                                
                SendMetricsToOutputModule_ThreadPoolManager.executeThread(sendMetricsToTelnetOpenTsdbThread, openTsdbTelnetOutputModule.getUniqueId());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static boolean isAnyOpenTsdbHttpOutputModuleEnabled() {
        
        List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = ApplicationConfiguration.getOpenTsdbHttpOutputModules();
        if (openTsdbHttpOutputModules == null) return false;
        
        for (OpenTsdbHttpOutputModule openTsdbHttpOutputModule : openTsdbHttpOutputModules) {
            if (openTsdbHttpOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<OpenTsdbHttpOutputModule> getEnabledOpenTsdbHttpOutputModules() {
        
        List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = ApplicationConfiguration.getOpenTsdbHttpOutputModules();
        if (openTsdbHttpOutputModules == null) return new ArrayList<>();
        
        List<OpenTsdbHttpOutputModule> enabledOpenTsdbOutputModules = new ArrayList<>();
        
        for (OpenTsdbHttpOutputModule openTsdbHttpOutputModule : openTsdbHttpOutputModules) {
            if (openTsdbHttpOutputModule.isOutputEnabled()) {
                enabledOpenTsdbOutputModules.add(openTsdbHttpOutputModule);
            }
        }
        
        return enabledOpenTsdbOutputModules;
    }
    
    public static void sendMetricsToAllOpenTsdbHttpOutputModules(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String threadId) {
        
        try {
            List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = ApplicationConfiguration.getOpenTsdbHttpOutputModules();
            if (openTsdbHttpOutputModules == null) return;
                    
            for (OpenTsdbHttpOutputModule openTsdbHttpOutputModule : openTsdbHttpOutputModules) {
                if (!openTsdbHttpOutputModule.isOutputEnabled()) continue;
                                
                SendMetricsToOpenTsdbThread sendMetricsToHttpOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, openTsdbHttpOutputModule.isSanitizeMetrics(), 
                        openTsdbHttpOutputModule.getUrl(), ApplicationConfiguration.getOutputModuleMaxConnectTime(), ApplicationConfiguration.getOutputModuleMaxReadTime(),  
                        openTsdbHttpOutputModule.getNumSendRetryAttempts(), openTsdbHttpOutputModule.getMaxMetricsPerMessage(), threadId);
                                
                SendMetricsToOutputModule_ThreadPoolManager.executeThread(sendMetricsToHttpOpenTsdbThread, openTsdbHttpOutputModule.getUniqueId());
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
    public static void sendMetricsToAllInfluxdbV1HttpOutputModules_NonNative(List<? extends InfluxdbMetricFormat_v1> influxdbMetrics, String threadId) {
        
        try {
            List<InfluxdbV1HttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbV1HttpOutputModules();
            if (influxdbHttpOutputModules == null) return;
                    
            for (InfluxdbV1HttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
                if (!influxdbHttpOutputModule.isOutputEnabled()) continue;
                
                URL influxdbBaseUrl = new URL(influxdbHttpOutputModule.getUrl());
                
                SendMetricsToInfluxdbV1Thread sendMetricsToHttpInfluxdbThread = new SendMetricsToInfluxdbV1Thread(influxdbMetrics, influxdbBaseUrl, 
                        ApplicationConfiguration.getInfluxdbDefaultDatabaseName(), ApplicationConfiguration.getInfluxdbDefaultDatabaseHttpBasicAuthValue(), 
                        ApplicationConfiguration.getOutputModuleMaxConnectTime(), ApplicationConfiguration.getOutputModuleMaxReadTime(),  
                        influxdbHttpOutputModule.getNumSendRetryAttempts(), influxdbHttpOutputModule.getMaxMetricsPerMessage(), threadId);
                                
                SendMetricsToOutputModule_ThreadPoolManager.executeThread(sendMetricsToHttpInfluxdbThread, influxdbHttpOutputModule.getUniqueId());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    /* 
    Use this method to output 'native' InfluxDB metrics. 'Native' InfluxDB metrics are metrics that were originally received by the InfluxDB listener.
    */
    public static void sendMetricsToAllInfluxdbV1HttpOutputModules_Native(List<InfluxdbMetric_v1> influxdbMetrics, String threadId) {
        
        try {
            List<InfluxdbV1HttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbV1HttpOutputModules();
            if (influxdbHttpOutputModules == null) return;
                    
            for (InfluxdbV1HttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
                if (!influxdbHttpOutputModule.isOutputEnabled()) continue;
                
                URL influxdbBaseUrl = new URL(influxdbHttpOutputModule.getUrl());
                
                SendMetricsToInfluxdbV1Thread sendMetricsToHttpInfluxdbThread = new SendMetricsToInfluxdbV1Thread(influxdbMetrics, influxdbBaseUrl, 
                        ApplicationConfiguration.getOutputModuleMaxConnectTime(), ApplicationConfiguration.getOutputModuleMaxReadTime(), 
                        influxdbHttpOutputModule.getNumSendRetryAttempts(), threadId);
                
                SendMetricsToOutputModule_ThreadPoolManager.executeThread(sendMetricsToHttpInfluxdbThread, influxdbHttpOutputModule.getUniqueId());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }

}
