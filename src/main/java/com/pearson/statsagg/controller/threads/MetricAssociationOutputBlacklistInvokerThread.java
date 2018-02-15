package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.utilities.InvokerThread;
import com.pearson.statsagg.alerts.MetricAssociationOutputBlacklistThread;
import com.pearson.statsagg.utilities.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAssociationOutputBlacklistInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricAssociationOutputBlacklistInvokerThread.class.getName());
    
    private final long threadExecutorShutdownWaitTime_;
    private final long routineInterval_ = 15000;
    
    public MetricAssociationOutputBlacklistInvokerThread() {
        threadExecutorShutdownWaitTime_ = routineInterval_ + 5000;
    }
    
    @Override
    public void run() {

        synchronized (lockObject_) {
            while (continueRunning_) {
                long currentTimeInMilliseconds = System.currentTimeMillis();
                Thread metricAssociationOutputBlacklistThread = new Thread(new MetricAssociationOutputBlacklistThread(currentTimeInMilliseconds, threadPoolExecutor_));
                metricAssociationOutputBlacklistThread.setPriority(3);
                threadExecutor_.execute(metricAssociationOutputBlacklistThread);

                try {
                    lockObject_.wait(routineInterval_);
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
    
    public void runMetricAssociationOutputBlacklistThread() {
        Thread metricAssociationOutputBlacklistThread = new Thread(new MetricAssociationOutputBlacklistThread(System.currentTimeMillis(), null));
        metricAssociationOutputBlacklistThread.setPriority(3);
        if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) threadExecutor_.execute(metricAssociationOutputBlacklistThread);
    }
    
}