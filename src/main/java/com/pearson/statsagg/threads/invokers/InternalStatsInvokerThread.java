package com.pearson.statsagg.threads.invokers;

import com.pearson.statsagg.threads.internal_housekeeping.InternalStatsThread;
import com.pearson.statsagg.utilities.core_utils.InvokerThread;
import com.pearson.statsagg.utilities.core_utils.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InternalStatsInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalStatsInvokerThread.class.getName());
    
    private final long threadExecutorShutdownWaitTime_;
    
    public InternalStatsInvokerThread() {
        threadExecutorShutdownWaitTime_ = 1500;
    }
    
    @Override
    public void run() {

        synchronized (lockObject_) {
            while (continueRunning_) {
                threadExecutor_.execute(new InternalStatsThread());

                try {
                    lockObject_.wait(15000);
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