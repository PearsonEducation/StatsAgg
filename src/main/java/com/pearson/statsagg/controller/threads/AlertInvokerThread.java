package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.alerts.AlertThread;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertInvokerThread.class.getName());
    
    private final int threadExecutorShutdownWaitTime_;
    
    public AlertInvokerThread() {
        threadExecutorShutdownWaitTime_ = ApplicationConfiguration.getAlertRoutineInterval() + 5000;
    }
    
    @Override
    public void run() {

        while (continueRunning_) {
            long currentTimeInMilliseconds = System.currentTimeMillis();

            threadExecutor_.execute(new AlertThread(Long.valueOf(currentTimeInMilliseconds)));

            Threads.sleepMilliseconds(ApplicationConfiguration.getAlertRoutineInterval());
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