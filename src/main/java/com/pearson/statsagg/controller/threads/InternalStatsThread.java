package com.pearson.statsagg.controller.threads;

import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.List;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InternalStatsThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InternalStatsThread.class.getName());
    
    @Override
    public void run() {
        calculateAverageNumberOfIncomingMetrics();
        setNumberOfDatabaseInMemory();
    }
    
    private void calculateAverageNumberOfIncomingMetrics() {
        long incomingMetricsRollingAverage = GlobalVariables.incomingMetricsCount.longValue() / 15;
        GlobalVariables.incomingMetricsRollingAverage.set(incomingMetricsRollingAverage);
        GlobalVariables.incomingMetricsCount.set(0);
    }
    
    private void setNumberOfDatabaseInMemory() {
        long currentDatapointsInMemory_local = 0;
        
        for (Entry<String,List<MetricTimestampAndValue>> recentMetricTimestampsAndValuesByMetricKey_Entry : GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.entrySet()) {
            try {
                List<MetricTimestampAndValue> metricTimestampAndValues = recentMetricTimestampsAndValuesByMetricKey_Entry.getValue();
                currentDatapointsInMemory_local += metricTimestampAndValues.size();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        GlobalVariables.currentDatapointsInMemory.set(currentDatapointsInMemory_local);
    }
    
}