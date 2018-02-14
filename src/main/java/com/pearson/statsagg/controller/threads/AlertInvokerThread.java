package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.utilities.InvokerThread;
import com.pearson.statsagg.alerts.AlertThread;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.Threads;
import java.util.concurrent.ThreadPoolExecutor;
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
                Thread alertThread = new Thread(new AlertThread(currentTimeInMilliseconds, true, true));
                alertThread.setPriority(3);
                
                try {
                    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) threadExecutor_;
                    if (AlertThread.isThreadCurrentlyRunning_.get() && (threadPoolExecutor.getActiveCount() == 0)) {
                        logger.warn("Invalid thread state detected (alert thread thinks it is running, but it is not.");
                        AlertThread.isThreadCurrentlyRunning_.set(false);
                    }
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
                
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
        Thread alertThread = new Thread(new AlertThread(System.currentTimeMillis(), runMetricAssociationRoutine, runAlertRoutine));
        alertThread.setPriority(3);
        if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) threadExecutor_.execute(alertThread);
    }
    
    @Override
    public long getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }
    
}