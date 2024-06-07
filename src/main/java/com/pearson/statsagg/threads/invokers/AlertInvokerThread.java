package com.pearson.statsagg.threads.invokers;

import com.pearson.statsagg.utilities.core_utils.InvokerThread;
import com.pearson.statsagg.threads.alert_related.AlertThread;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.utilities.core_utils.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertInvokerThread.class.getName());
    
    private final long threadExecutorShutdownWaitTime_;

    public AlertInvokerThread() {
        threadExecutorShutdownWaitTime_ = ApplicationConfiguration.getAlertRoutineInterval() + 5000;
    }
    
    @Override
    public void run() {

        synchronized (lockObject_) {
            while (continueRunning_) {
                long currentTimeInMilliseconds = System.currentTimeMillis();
                Thread alertThread = new Thread(new AlertThread(currentTimeInMilliseconds, true, true, threadPoolExecutor_, 
                        ApplicationConfiguration.getAlertMetricAssociationThreads()));
                alertThread.setPriority(3);
                
                threadExecutor_.execute(alertThread);

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
    
    public void runAlertThread(boolean runMetricAssociationRoutine, boolean runAlertRoutine) {
        Thread alertThread = new Thread(new AlertThread(System.currentTimeMillis(), runMetricAssociationRoutine, runAlertRoutine, 
                threadPoolExecutor_, ApplicationConfiguration.getAlertMetricAssociationThreads()));
        alertThread.setPriority(3);
        if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) threadExecutor_.execute(alertThread);
    }
    
    @Override
    public long getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }
    
}