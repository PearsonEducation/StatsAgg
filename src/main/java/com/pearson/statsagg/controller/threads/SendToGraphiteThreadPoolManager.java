package com.pearson.statsagg.controller.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GraphiteOutputModule;
import com.pearson.statsagg.metric_aggregation.threads.SendMetricsToGraphiteThread;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendToGraphiteThreadPoolManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SendToGraphiteThreadPoolManager.class.getName());
    
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

    public static void executeThread(SendMetricsToGraphiteThread sendMetricsToGraphiteThread) {
        try {
            if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) {
                threadExecutor_.execute(sendMetricsToGraphiteThread);
            }
            else {
                logger.warn("The thread pool is not in a state that can execute the requested thread.");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
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
    
    public static void sendMetricsToGraphiteOutputModule(GraphiteOutputModule graphiteOutputModule, List<? extends GraphiteMetricFormat> graphiteMetrics, String threadId) {
        
        if ((graphiteOutputModule == null) || (graphiteMetrics == null) || graphiteMetrics.isEmpty() || (threadId == null) || threadId.isEmpty()) {
            return;
        }
        
        try {
            SendMetricsToGraphiteThread sendMetricsToGraphiteThread = new SendMetricsToGraphiteThread(graphiteMetrics, 
                    graphiteOutputModule.getHost(), graphiteOutputModule.getPort(), graphiteOutputModule.getNumSendRetryAttempts(), 
                    graphiteOutputModule.getMaxMetricsPerMessage(), threadId);

            SendToGraphiteThreadPoolManager.executeThread(sendMetricsToGraphiteThread);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
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
                
                SendMetricsToGraphiteThread sendMetricsToGraphiteThread = new SendMetricsToGraphiteThread(graphiteMetrics, graphiteOutputModule.getHost(), 
                       graphiteOutputModule.getPort(), graphiteOutputModule.getNumSendRetryAttempts(), graphiteOutputModule.getMaxMetricsPerMessage(), threadId);
                
                SendToGraphiteThreadPoolManager.executeThread(sendMetricsToGraphiteThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
}
