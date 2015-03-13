package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.alerts.CleanupThread;
import com.pearson.statsagg.utilities.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CleanupInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(CleanupInvokerThread.class.getName());
    
    private final int threadExecutorShutdownWaitTime_;
    
    public CleanupInvokerThread() {
        threadExecutorShutdownWaitTime_ = 15000;
    }
    
    @Override
    public void run() {

        synchronized (lockObject_) {
            while (continueRunning_) {
                long currentTimeInMilliseconds = System.currentTimeMillis();

                threadExecutor_.execute(new CleanupThread(currentTimeInMilliseconds));

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
    public int getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }
    
}