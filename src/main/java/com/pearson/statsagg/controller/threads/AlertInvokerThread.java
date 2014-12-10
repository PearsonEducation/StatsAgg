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

        synchronized (lockObject_) {
            while (continueRunning_) {
                long currentTimeInMilliseconds = System.currentTimeMillis();

                threadExecutor_.execute(new AlertThread(currentTimeInMilliseconds));

                try {
                    lockObject_.wait(ApplicationConfiguration.getAlertRoutineInterval());
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