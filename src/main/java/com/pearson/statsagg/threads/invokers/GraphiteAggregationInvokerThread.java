package com.pearson.statsagg.threads.invokers;

import com.pearson.statsagg.utilities.core_utils.InvokerThread;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.threads.metric_processors.GraphiteAggregationThread;
import com.pearson.statsagg.utilities.core_utils.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteAggregationInvokerThread extends InvokerThread {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteAggregationInvokerThread.class.getName());

    private final long threadExecutorShutdownWaitTime_;
    
    public GraphiteAggregationInvokerThread() {
        threadExecutorShutdownWaitTime_ = (2 * ApplicationConfiguration.getFlushTimeAgg()) + 3000;
    }
    
    @Override
    public void run() {

        synchronized (lockObject_) {
            while (continueRunning_) {
                long currentTimeInMilliseconds = System.currentTimeMillis();

                threadExecutor_.execute(new GraphiteAggregationThread(currentTimeInMilliseconds));

                try {
                    lockObject_.wait(ApplicationConfiguration.getFlushTimeAgg());
                }
                catch (Exception e) {}
            }
        }
                
        while (!threadExecutor_.isTerminated()) {
            Threads.sleepMilliseconds(100);
        }
        
        isShutdown_ = true;
    }

    @Override
    public long getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }

}