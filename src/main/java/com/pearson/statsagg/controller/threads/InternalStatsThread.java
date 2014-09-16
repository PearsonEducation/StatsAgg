package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.globals.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InternalStatsThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InternalStatsThread.class.getName());
    
    @Override
    public void run() {
        long incomingMetricsRollingAverage = GlobalVariables.incomingMetricsCount.longValue() / 15;
        GlobalVariables.incomingMetricsRollingAverage.set(incomingMetricsRollingAverage);
        
        GlobalVariables.incomingMetricsCount.set(0);
    }
    
}