package com.pearson.statsagg.threads.invokers;

import com.pearson.statsagg.utilities.core_utils.InvokerThread;
import com.pearson.statsagg.threads.template_related.TemplateThread;
import com.pearson.statsagg.utilities.core_utils.Threads;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class TemplateInvokerThread extends InvokerThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateInvokerThread.class.getName());
  
    private final long threadExecutorShutdownWaitTime_;
    private final Random randomNumberGenerator_ = new Random();
    private final int averageWaitTimeInMilliseconds_ = 15000;

    public TemplateInvokerThread() {
        threadExecutorShutdownWaitTime_ = averageWaitTimeInMilliseconds_ + 3000;
    }
    
    @Override
    public void run() {

        synchronized (lockObject_) {
            while (continueRunning_) {
                long currentTimeInMilliseconds = System.currentTimeMillis();
                Thread templateThread = new Thread(new TemplateThread(currentTimeInMilliseconds, threadPoolExecutor_));
                templateThread.setPriority(3);
                
                threadExecutor_.execute(templateThread);

                try {
                    // the delay between template routine invocations has been randomized to avoid having the routine 
                    // always execute in sync with the alert-routine/metric-association-routine
                    int minWaitTime = 5000;
                    int maxWaitTime = averageWaitTimeInMilliseconds_ * 2;
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
    
    public void runTemplateThread() {
        Thread templateThread = new Thread(new TemplateThread(System.currentTimeMillis(), threadPoolExecutor_));
        templateThread.setPriority(3);
        if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) threadExecutor_.execute(templateThread);
    }
    
    @Override
    public long getThreadExecutorShutdownWaitTime() {
        return threadExecutorShutdownWaitTime_;
    }
    
}