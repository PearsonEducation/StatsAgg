package com.pearson.statsagg.utilities.core_utils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Threads {
    
    private static final Logger logger = LoggerFactory.getLogger(Threads.class.getName());
    
    public static void sleepMilliseconds(long milliseconds) {
        sleepMilliseconds(milliseconds, false);
    }
    
    public static void sleepMilliseconds(long milliseconds, boolean logSleepTime) {
        
        if (milliseconds <= 0) {
            return;
        }
        
        try {
            if (logSleepTime) {
                logger.debug("Sleeping for " + milliseconds + " milliseconds");
            }
            
            Thread.sleep(milliseconds);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public static void sleepSeconds(int seconds) {
        sleepSeconds(seconds, false);
    }
    
    public static void sleepSeconds(int seconds, boolean logSleepTime) {
        
        if (seconds <= 0) {
            return;
        }
        
        try {
            if (logSleepTime) {
                logger.debug("Sleeping for " + seconds + " seconds");
            }
            
            Thread.sleep((long) (seconds * 1000));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public static void sleepSeconds(double seconds) {
        sleepSeconds(seconds, false);
    }
    
    public static void sleepSeconds(double seconds, boolean logSleepTime) {
        
        if (seconds <= 0) {
            return;
        }
        
        try {
            if (logSleepTime) {
                logger.debug("Sleeping for " + seconds + " seconds");
            }
            
            Thread.sleep((long) (seconds * 1000));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public static void sleepMinutes(int minutes) {
        sleepMinutes(minutes, false);
    }
    
    public static void sleepMinutes(int minutes, boolean logSleepTime) {
        
        if (minutes <= 0) {
            return;
        }
        
        try {
            if (logSleepTime) {
                logger.debug("Sleeping for " + minutes + " minutes");
            }
            
            Thread.sleep((long) (60 * minutes * 1000));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public static void sleepMinutes(double minutes) {
        sleepMinutes(minutes, false);
    }
    
    public static void sleepMinutes(double minutes, boolean logSleepTime) {
        
        if (minutes <= 0) {
            return;
        }
        
        try {
            if (logSleepTime) {
                logger.debug("Sleeping for " + minutes + " minutes");
            }
            
            Thread.sleep((long) (60 * minutes * 1000));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public static void threadExecutorFixedPool(List threads, int nThreadPoolSize, long timeoutTime, TimeUnit timeoutTimeunit) {
        
        if ((threads == null) || threads.isEmpty() ||(nThreadPoolSize <= 0) || (timeoutTime <= 0) || (timeoutTimeunit == null)) {
            return;
        }
        
        try {
            ExecutorService threadExecutor = Executors.newFixedThreadPool(nThreadPoolSize);
            for (Object thread : threads) {
                threadExecutor.execute((Runnable) thread);
            }

            threadExecutor.shutdown();
            boolean didFinishWithoutTimeout = threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
            
            if (!didFinishWithoutTimeout) {
                try {
                    threadExecutor.shutdownNow();
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
                
                try {
                    threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

    }
    
    public static void threadExecutorCachedPool(List threads, long timeoutTime, TimeUnit timeoutTimeunit) {
        
        if ((threads == null) || threads.isEmpty() || (timeoutTime <= 0) || (timeoutTimeunit == null)) {
            return;
        }
        
        try {
            ExecutorService threadExecutor = Executors.newCachedThreadPool();
            
            for (Object thread : threads) {
                threadExecutor.execute((Runnable) thread);
            }

            threadExecutor.shutdown();
            boolean didFinishWithoutTimeout = threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
            
            if (!didFinishWithoutTimeout) {
                try {
                    threadExecutor.shutdownNow();
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
                
                try {
                    threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

    }
    
    public static void shutdownThreadExecutor(ExecutorService threadExecutor, Long timeoutTime, TimeUnit timeoutTimeunit, 
            boolean forceShutdownIfTimeoutReached, boolean waitForeverForForceShutdownToFinish) {

        if (threadExecutor == null) {
            logger.error("ThreadExecutor cannot be null");
            return;
        }
        
        if ((timeoutTime == null) || (timeoutTime <= 0)) {
            logger.error("TimeoutTime cannot be null or less than 0");
            return;
        }
        
        if (timeoutTimeunit == null) {
            logger.error("Timeout-Timeunit cannot be null");
            return;
        }

        boolean didFinishWithoutTimeout = false;
        
        try {
            threadExecutor.shutdown();
            didFinishWithoutTimeout = threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        if (forceShutdownIfTimeoutReached) {
            try {
                if (!didFinishWithoutTimeout) {
                    try {
                        threadExecutor.shutdownNow();
                    }
                    catch (Exception e) {
                        logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                    }
                }    
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }

            if (waitForeverForForceShutdownToFinish) {
                try {
                    threadExecutor.awaitTermination(99999999, TimeUnit.DAYS);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
    }
    
}
