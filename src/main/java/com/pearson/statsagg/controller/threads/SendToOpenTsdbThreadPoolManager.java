package com.pearson.statsagg.controller.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.OpenTsdbHttpOutputModule;
import com.pearson.statsagg.globals.OpenTsdbTelnetOutputModule;
import com.pearson.statsagg.metric_aggregation.threads.SendMetricsToOpenTsdbThread;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendToOpenTsdbThreadPoolManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SendToOpenTsdbThreadPoolManager.class.getName());
    
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

    public static void executeThread(SendMetricsToOpenTsdbThread sendMetricsToOpenTsdbThread) {
        try {
            if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) {
                threadExecutor_.execute(sendMetricsToOpenTsdbThread);
            }
            else {
                logger.warn("The thread pool is not in a state that can execute the requested thread.");
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
    
    public static void sendMetricsToAllOpenTsdbTelnetOutputModules(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, String threadId) {
        
        try {
            List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
            if (openTsdbTelnetOutputModules == null) return;
                    
            for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
                if (!openTsdbTelnetOutputModule.isOutputEnabled()) continue;
                
                SendMetricsToOpenTsdbThread sendMetricsToTelnetOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, sanitizeMetrics, 
                        openTsdbTelnetOutputModule.getHost(), openTsdbTelnetOutputModule.getPort(), 
                        openTsdbTelnetOutputModule.getNumSendRetryAttempts(), threadId);
                
                SendToOpenTsdbThreadPoolManager.executeThread(sendMetricsToTelnetOpenTsdbThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static void sendMetricsToAllOpenTsdbHttpOutputModules(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, String threadId) {
        
        try {
            List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = ApplicationConfiguration.getOpenTsdbHttpOutputModules();
            if (openTsdbHttpOutputModules == null) return;
                    
            for (OpenTsdbHttpOutputModule openTsdbHttpOutputModule : openTsdbHttpOutputModules) {
                if (!openTsdbHttpOutputModule.isOutputEnabled()) continue;
                
                URL url = new URL(openTsdbHttpOutputModule.getUrl());
                
                SendMetricsToOpenTsdbThread sendMetricsToHttpOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, sanitizeMetrics, url, 
                       openTsdbHttpOutputModule.getNumSendRetryAttempts(), openTsdbHttpOutputModule.getMaxMetricsPerMessage(), threadId);
                
                SendToOpenTsdbThreadPoolManager.executeThread(sendMetricsToHttpOpenTsdbThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
}
