package com.pearson.statsagg.controller.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public abstract class InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(InvokerThread.class.getName());
    
    protected boolean continueRunning_ = true;
    protected boolean isShutdown_ = false;
    protected ExecutorService threadExecutor_ = Executors.newCachedThreadPool();

    @Override
    public void run() {}

    public void shutdown() {

        continueRunning_ = false;

        if (threadExecutor_ == null) {
            return;
        }

        try {
            threadExecutor_.shutdown();
            threadExecutor_.awaitTermination(getThreadExecutorShutdownWaitTime(), TimeUnit.MILLISECONDS);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    public boolean continueRunning() {
        return continueRunning_;
    }

    public void setContinueRunning(boolean continueRunning) {
        continueRunning_ = continueRunning;
    }
    
    public boolean isShutdown() {
        return isShutdown_;
    }
    
    public abstract int getThreadExecutorShutdownWaitTime();
    
}