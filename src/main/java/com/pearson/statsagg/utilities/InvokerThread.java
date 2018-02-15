package com.pearson.statsagg.utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public abstract class InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(InvokerThread.class.getName());
    
    protected final ExecutorService threadExecutor_ = Executors.newCachedThreadPool();
    protected final ThreadPoolExecutor threadPoolExecutor_;
    protected boolean continueRunning_ = true;
    protected boolean isShutdown_ = false;

    protected final Object lockObject_ = new Object();
    
    public InvokerThread() {
        threadPoolExecutor_ = (ThreadPoolExecutor) threadExecutor_;
    }
    
    @Override
    public void run() {}

    public void shutdown() {

        continueRunning_ = false;

        if (threadExecutor_ == null) {
            return;
        }

        try {
            synchronized (lockObject_) {
                lockObject_.notifyAll();
            }
            
            Threads.shutdownThreadExecutor(threadExecutor_, getThreadExecutorShutdownWaitTime(), TimeUnit.MILLISECONDS, true, false);
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
    
    public ExecutorService getThreadExecutorService() {
        return threadExecutor_;
    }
    
    public abstract long getThreadExecutorShutdownWaitTime();
    
}