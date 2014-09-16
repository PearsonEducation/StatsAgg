package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.utilities.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InternalStatsInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalStatsInvokerThread.class.getName());
    
    private final int threadExecutorShutdownWaitTime_;
    
    public InternalStatsInvokerThread() {
        threadExecutorShutdownWaitTime_ = 1500;
    }
    
    @Override
    public void run() {

        while (continueRunning_) {
            threadExecutor_.execute(new InternalStatsThread());

            Threads.sleepMilliseconds(15000);
        }
                
        while (!threadExecutor_.isTerminated()) {
            Threads.sleepMilliseconds(500);
        }
        
        isShutdown_ = true;
    }
    
    @Override
    public int getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }
    
}