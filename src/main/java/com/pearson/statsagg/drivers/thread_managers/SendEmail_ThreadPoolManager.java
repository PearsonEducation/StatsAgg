package com.pearson.statsagg.drivers.thread_managers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.pearson.statsagg.alerts.EmailThread;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendEmail_ThreadPoolManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SendEmail_ThreadPoolManager.class.getName());
    
    private static final Object lock_ = new Object();
    private static ExecutorService threadExecutor_ = null;
    
    public static void start() {
        synchronized(lock_) {
            if ((threadExecutor_ == null) || ((!threadExecutor_.isShutdown()) && (!threadExecutor_.isTerminated()))) {
                threadExecutor_ = Executors.newCachedThreadPool();
            }
            else {
                logger.info("Can't create new thread pool - current thread pool isn't terminated");
            }
        }
    }
    
    public static void shutdown() {

        synchronized(lock_) {
            if (threadExecutor_ == null) {
                return;
            }

            try {
                threadExecutor_.shutdown();

                // reusing the termination delay time from the aggregation invokers because it makes sense in this context
                threadExecutor_.awaitTermination((2 * ApplicationConfiguration.getFlushTimeAgg()) + 3000, TimeUnit.MILLISECONDS);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
    }

    public static void executeThread(EmailThread emailThread) {
        try {
            if ((threadExecutor_ != null) && !threadExecutor_.isShutdown() && !threadExecutor_.isTerminated()) {
                threadExecutor_.execute(emailThread);
            }
            else {
                logger.warn("The thread pool is not in a state that can execute the requested thread.");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
}
