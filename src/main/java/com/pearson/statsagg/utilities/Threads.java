package com.pearson.statsagg.utilities;

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
        sleepMilliseconds(milliseconds, true);
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
        sleepSeconds(seconds, true);
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
        sleepSeconds(seconds, true);
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
        sleepMinutes(minutes, true);
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
        sleepMinutes(minutes, true);
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
    
    public static void threadExecutorFixedPool(List threads, int nThreadPoolSize, int timeoutTime, TimeUnit timeoutTimeunit) {
        
        if ((threads == null) || threads.isEmpty() ||(nThreadPoolSize <= 0) || (timeoutTime <= 0) || (timeoutTimeunit == null)) {
            return;
        }
        
        try {
            ExecutorService threadExecutor = Executors.newFixedThreadPool(nThreadPoolSize);
            for (int i = 0; i < threads.size(); i++) {
                threadExecutor.execute((Runnable) threads.get(i));
            }

            threadExecutor.shutdown();
            boolean didTimeout = threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
            
            if (!didTimeout) {
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
    
    public static void threadExecutorCachedPool(List threads, int timeoutTime, TimeUnit timeoutTimeunit) {
        
        if ((threads == null) || threads.isEmpty() || (timeoutTime <= 0) || (timeoutTimeunit == null)) {
            return;
        }
        
        try {
            ExecutorService threadExecutor = Executors.newCachedThreadPool();
            for (int i = 0; i < threads.size(); i++) {
                threadExecutor.execute((Runnable) threads.get(i));
            }

            threadExecutor.shutdown();
            boolean didTimeout = threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
            
            if (!didTimeout) {
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
    
    public static void shutdownThreadExecutor(ExecutorService threadExecutor, Integer timeoutTime, TimeUnit timeoutTimeunit) {

        if (threadExecutor == null) return;
        if ((timeoutTime == null) || (timeoutTime <= 0)) timeoutTime = 1;
        if (timeoutTimeunit == null) timeoutTimeunit = TimeUnit.MILLISECONDS;

        try {
            threadExecutor.shutdown();
            boolean didTimeout = threadExecutor.awaitTermination(timeoutTime, timeoutTimeunit);
            
            if (!didTimeout) {
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
    
}
