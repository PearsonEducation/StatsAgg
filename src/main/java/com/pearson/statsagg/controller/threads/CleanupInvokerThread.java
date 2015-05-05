package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.alerts.CleanupThread;
import com.pearson.statsagg.utilities.Threads;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CleanupInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(CleanupInvokerThread.class.getName());
    
    private final int threadExecutorShutdownWaitTime_;
    private final Random randomNumberGenerator_ = new Random();
    private final int cleanupInvokerAverageWaitTimeInMilliseconds_ = 10000;
    
    public CleanupInvokerThread() {
        threadExecutorShutdownWaitTime_ = cleanupInvokerAverageWaitTimeInMilliseconds_ + 3000;
    }
    
    @Override
    public void run() {

        synchronized (lockObject_) {
            while (continueRunning_) {
                long currentTimeInMilliseconds = System.currentTimeMillis();

                threadExecutor_.execute(new CleanupThread(currentTimeInMilliseconds));

                try {
                    // the delay between cleanup routine invokations has been randomized to avoid having the cleanup routine 
                    // always execute in sync with the alert-routine/metric-association-routine
                    int minWaitTime = 5000;
                    int maxWaitTime = cleanupInvokerAverageWaitTimeInMilliseconds_ * 2;
                    int randomWaitTime = randomNumberGenerator_.nextInt(maxWaitTime);
                    
                    int waitTime;
                    if (minWaitTime >= randomWaitTime) waitTime = minWaitTime;
                    else waitTime = randomWaitTime;
                    
                    lockObject_.wait(waitTime);
                }
                catch (Exception e) {}
            }
        }

        while (!threadExecutor_.isTerminated()) {
            Threads.sleepMilliseconds(100);
        }
        
        isShutdown_ = true;
    }
    
    public void runCleanupThread() {
        Thread cleanupThread = new Thread(new CleanupThread(System.currentTimeMillis()));
        if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) threadExecutor_.execute(cleanupThread);
    }
    
    @Override
    public int getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }
    
}