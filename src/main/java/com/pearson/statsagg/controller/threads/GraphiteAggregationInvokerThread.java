package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.metric_aggregation.threads.GraphiteAggregationThread;
import com.pearson.statsagg.utilities.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteAggregationInvokerThread extends InvokerThread {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteAggregationInvokerThread.class.getName());

    private final int threadExecutorShutdownWaitTime_;
    
    public GraphiteAggregationInvokerThread() {
        threadExecutorShutdownWaitTime_ = (2 * ApplicationConfiguration.getFlushTimeAgg()) + 3000;
    }
    
    @Override
    public void run() {

        while (continueRunning_) {
            long currentTimeInMilliseconds = System.currentTimeMillis();

            threadExecutor_.execute(new GraphiteAggregationThread(currentTimeInMilliseconds));

            Threads.sleepMilliseconds(ApplicationConfiguration.getFlushTimeAgg());
        }
                
        while (!threadExecutor_.isTerminated()) {
            Threads.sleepSeconds(2);
        }
        
        isShutdown_ = true;
    }

    @Override
    public int getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }

}