package com.pearson.statsagg.metric_aggregation.graphite;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteMetricAggregator {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteMetricAggregator.class.getName());
    
    public static final int GRAPHITE_SCALE = 7;
    public static final int GRAPHITE_PRECISION = 31;
    public static final RoundingMode GRAPHITE_ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final MathContext GRAPHITE_MATH_CONTEXT = new MathContext(GRAPHITE_PRECISION, GRAPHITE_ROUNDING_MODE);
    
    public static List<GraphiteMetricAggregated> aggregateGraphiteMetrics(List<GraphiteMetricRaw> graphiteMetricsRaw) {
        
        if ((graphiteMetricsRaw == null) || graphiteMetricsRaw.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String,List<GraphiteMetricRaw>> graphiteMetricsRawByMetricPath = divideGraphiteMetricsRawByMetricPath(graphiteMetricsRaw);
        
        List<GraphiteMetricAggregated> graphiteMetricsAggregated = aggregateByMetricPath(graphiteMetricsRawByMetricPath);
        
        return graphiteMetricsAggregated;
    }
    
    /*
     * The input metrics are assumed to be unsorted & unprocessed
     */
    public static Map<String,List<GraphiteMetricRaw>> divideGraphiteMetricsRawByMetricPath(List<GraphiteMetricRaw> graphiteMetricsRaw) {
        
        if (graphiteMetricsRaw == null) {
            return new HashMap<>();
        }

        Map<String,List<GraphiteMetricRaw>> graphiteMetricsRawByMetricPath = new HashMap<>(graphiteMetricsRaw.size());
        
        for (GraphiteMetricRaw graphiteMetricRaw : graphiteMetricsRaw) {

            String metricPath = graphiteMetricRaw.getMetricPath();
            List<GraphiteMetricRaw> graphiteMetricRawByMetricPath = graphiteMetricsRawByMetricPath.get(metricPath);
            
            if (graphiteMetricRawByMetricPath != null) {
                graphiteMetricRawByMetricPath.add(graphiteMetricRaw);
            }
            else {
                graphiteMetricRawByMetricPath = new ArrayList<>();
                graphiteMetricRawByMetricPath.add(graphiteMetricRaw);
                graphiteMetricsRawByMetricPath.put(metricPath, graphiteMetricRawByMetricPath);
            }
            
        }
        
        return graphiteMetricsRawByMetricPath;
    }
    
    /* 
     * This method assumes that all of the input graphite metrics are already separated by metric path.
     * The key of the HashMap is the assumed to be the metric path.
     */
    public static List<GraphiteMetricAggregated> aggregateByMetricPath(Map<String,List<GraphiteMetricRaw>> graphiteMetricsRawByMetricPath) {
        
        if ((graphiteMetricsRawByMetricPath == null) || graphiteMetricsRawByMetricPath.isEmpty()) {
            return new ArrayList<>();
        }

        List<GraphiteMetricAggregated> graphiteMetricsAggregated = new ArrayList<>();
        Set<String> metricPathSet = graphiteMetricsRawByMetricPath.keySet();
        
        for (String metricPath : metricPathSet) {
            try {
                List<GraphiteMetricRaw> graphiteMetricsRaw = graphiteMetricsRawByMetricPath.get(metricPath);
                List<GraphiteMetricAggregated> multipleGraphiteMetricsAggregated = aggregate(graphiteMetricsRaw);
            
                if ((multipleGraphiteMetricsAggregated != null) && !multipleGraphiteMetricsAggregated.isEmpty()) {
                    graphiteMetricsAggregated.addAll(multipleGraphiteMetricsAggregated);
                }    
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return graphiteMetricsAggregated;
    }
    
    /* 
     * This method assumes that all of the input graphite metrics share the same metric path
     */
    public static List<GraphiteMetricAggregated> aggregate(List<GraphiteMetricRaw> graphiteMetricsRaw) {
        
        if ((graphiteMetricsRaw == null) || graphiteMetricsRaw.isEmpty()) {
           return new ArrayList<>(); 
        }

        BigDecimal sumMetricValue = BigDecimal.ZERO;
        BigDecimal minimumMetricValue = null;
        BigDecimal maximumMetricValue = null;
        long sumReceivedTimestamp = 0, sumMetricTimestamp = 0;
        int metricCounter = 0;
        
        for (GraphiteMetricRaw graphiteMetricRaw : graphiteMetricsRaw) {

            try {
                BigDecimal metricValue = graphiteMetricRaw.getMetricValue();

                sumMetricValue = sumMetricValue.add(metricValue);
                sumMetricTimestamp += graphiteMetricRaw.getMetricTimestampInMilliseconds();
                sumReceivedTimestamp += graphiteMetricRaw.getMetricReceivedTimestampInMilliseconds();
                
                if (metricCounter == 0) {
                    maximumMetricValue = metricValue;
                    minimumMetricValue = metricValue;
                }
                else {
                    if ((maximumMetricValue != null) && maximumMetricValue.compareTo(metricValue) == -1) {
                        maximumMetricValue = metricValue;
                    }
                    if ((minimumMetricValue != null) && minimumMetricValue.compareTo(metricValue) == 1) {
                        minimumMetricValue = metricValue;
                    }
                }

                metricCounter++;
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        if (metricCounter > 0) {
            List<GraphiteMetricAggregated> graphiteMetricsAggregated = new ArrayList<>();
            
            String metricPath = graphiteMetricsRaw.get(0).getMetricPath();
            BigDecimal responsesPerInterval = new BigDecimal(metricCounter);
            BigDecimal averageMetricValue = MathUtilities.smartBigDecimalScaleChange(sumMetricValue.divide(responsesPerInterval, GRAPHITE_MATH_CONTEXT), GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE);
            minimumMetricValue = (minimumMetricValue != null) ? MathUtilities.smartBigDecimalScaleChange(minimumMetricValue, GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE) : null;
            maximumMetricValue = (maximumMetricValue != null) ? MathUtilities.smartBigDecimalScaleChange(maximumMetricValue, GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE) : null;
            long averagedMetricTimestamp = Math.round((double) sumMetricTimestamp / (double) metricCounter);
            long averagedMetricReceivedTimestamp = Math.round((double) sumReceivedTimestamp / (double) metricCounter);
            
            String aggregatedMetricsSeparator;
            if (ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString() == null) aggregatedMetricsSeparator = "";
            else aggregatedMetricsSeparator = ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString();
            
            GraphiteMetricAggregated graphiteMetricAverageAggregated = new GraphiteMetricAggregated(metricPath + aggregatedMetricsSeparator 
                    + "Avg", averageMetricValue, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
            graphiteMetricAverageAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            graphiteMetricsAggregated.add(graphiteMetricAverageAggregated);
            
            GraphiteMetricAggregated graphiteMetricMaximumAggregated = new GraphiteMetricAggregated(metricPath + aggregatedMetricsSeparator 
                    + "Max", maximumMetricValue, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
            graphiteMetricMaximumAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            graphiteMetricsAggregated.add(graphiteMetricMaximumAggregated);

            GraphiteMetricAggregated graphiteMetricMinimumAggregated = new GraphiteMetricAggregated(metricPath + aggregatedMetricsSeparator 
                    + "Min", minimumMetricValue, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
            graphiteMetricMinimumAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            graphiteMetricsAggregated.add(graphiteMetricMinimumAggregated);
            
            return graphiteMetricsAggregated;
        }
        else {
            return new ArrayList<>();
        }
    }
    
}
