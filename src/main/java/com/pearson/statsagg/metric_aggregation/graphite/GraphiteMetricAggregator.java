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
    public static final BigDecimal ONE_THOUSAND = new BigDecimal((int) 1000);

    public static List<GraphiteMetric> aggregateGraphiteMetrics(List<GraphiteMetric> graphiteMetrics) {
        
        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String,List<GraphiteMetric>> graphiteMetricsByMetricPath = divideGraphiteMetricsByMetricPath(graphiteMetrics);
        
        List<GraphiteMetric> graphiteMetricsAggregated = aggregateByMetricPath(graphiteMetricsByMetricPath);
        
        return graphiteMetricsAggregated;
    }
    
    /*
     * The input metrics are assumed to be unsorted & unprocessed
     */
    public static Map<String,List<GraphiteMetric>> divideGraphiteMetricsByMetricPath(List<GraphiteMetric> graphiteMetrics) {
        
        if (graphiteMetrics == null) {
            return new HashMap<>();
        }

        Map<String,List<GraphiteMetric>> graphiteMetricsByMetricPath = new HashMap<>(graphiteMetrics.size());
        
        for (GraphiteMetric graphiteMetric : graphiteMetrics) {

            String metricPath = graphiteMetric.getMetricPath();
            List<GraphiteMetric> graphiteMetricByMetricPath = graphiteMetricsByMetricPath.get(metricPath);
            
            if (graphiteMetricByMetricPath != null) {
                graphiteMetricByMetricPath.add(graphiteMetric);
            }
            else {
                graphiteMetricByMetricPath = new ArrayList<>();
                graphiteMetricByMetricPath.add(graphiteMetric);
                graphiteMetricsByMetricPath.put(metricPath, graphiteMetricByMetricPath);
            }
            
        }
        
        return graphiteMetricsByMetricPath;
    }
    
    /* 
     * This method assumes that all of the input graphite metrics are already separated by metric path.
     * The key of the HashMap is the assumed to be the metric path.
     */
    public static List<GraphiteMetric> aggregateByMetricPath(Map<String,List<GraphiteMetric>> graphiteMetricsByMetricPath) {
        
        if ((graphiteMetricsByMetricPath == null) || graphiteMetricsByMetricPath.isEmpty()) {
            return new ArrayList<>();
        }

        List<GraphiteMetric> graphiteMetricsAggregated = new ArrayList<>();
        Set<String> metricPathSet = graphiteMetricsByMetricPath.keySet();
        
        BigDecimal aggregationWindowLengthInMs = new BigDecimal(ApplicationConfiguration.getFlushTimeAgg());
        
        for (String metricPath : metricPathSet) {
            try {
                List<GraphiteMetric> graphiteMetrics = graphiteMetricsByMetricPath.get(metricPath);
                List<GraphiteMetric> multipleGraphiteMetricsAggregated = aggregate(graphiteMetrics, aggregationWindowLengthInMs);
            
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
    public static List<GraphiteMetric> aggregate(List<GraphiteMetric> graphiteMetrics, BigDecimal aggregationWindowLengthInMs) {
        
        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty() || (aggregationWindowLengthInMs == null)) {
           return new ArrayList<>(); 
        }

        List<BigDecimal> metricValues = new ArrayList<>();
        
        BigDecimal sumMetricValues = BigDecimal.ZERO;
        BigDecimal minimumMetricValue = null;
        BigDecimal maximumMetricValue = null;
        long sumReceivedTimestamp = 0, sumMetricTimestamp = 0;
        int metricCounter = 0;
        
        for (GraphiteMetric graphiteMetric : graphiteMetrics) {

            try {
                BigDecimal metricValue = graphiteMetric.getMetricValue();
                metricValues.add(metricValue);
                
                sumMetricValues = sumMetricValues.add(metricValue);
                sumMetricTimestamp += graphiteMetric.getMetricTimestampInMilliseconds();
                sumReceivedTimestamp += graphiteMetric.getMetricReceivedTimestampInMilliseconds();
                
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
            List<GraphiteMetric> graphiteMetricsAggregated = new ArrayList<>();
            
            String metricPath = graphiteMetrics.get(0).getMetricPath();
            BigDecimal metricCount = new BigDecimal(metricCounter);
            BigDecimal rate = MathUtilities.smartBigDecimalScaleChange(metricCount.multiply(ONE_THOUSAND).divide(aggregationWindowLengthInMs, GRAPHITE_MATH_CONTEXT), GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE);
            BigDecimal averageMetricValue = MathUtilities.smartBigDecimalScaleChange(sumMetricValues.divide(metricCount, GRAPHITE_MATH_CONTEXT), GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE);
            minimumMetricValue = (minimumMetricValue != null) ? MathUtilities.smartBigDecimalScaleChange(minimumMetricValue, GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE) : null;
            maximumMetricValue = (maximumMetricValue != null) ? MathUtilities.smartBigDecimalScaleChange(maximumMetricValue, GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE) : null;
            BigDecimal medianMetricValue = MathUtilities.smartBigDecimalScaleChange(MathUtilities.computeMedianOfBigDecimals(metricValues, GRAPHITE_MATH_CONTEXT, false), GRAPHITE_SCALE, GRAPHITE_ROUNDING_MODE);
            long averagedMetricTimestamp = Math.round((double) sumMetricTimestamp / (double) metricCounter);
            long averagedMetricReceivedTimestamp = Math.round((double) sumReceivedTimestamp / (double) metricCounter);
            
            String aggregatedMetricsSeparator;
            if (ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString() == null) aggregatedMetricsSeparator = ".";
            else aggregatedMetricsSeparator = ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString();
            
            if (averageMetricValue != null) {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath + aggregatedMetricsSeparator + "Avg", 
                        averageMetricValue, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
                graphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                graphiteMetricsAggregated.add(graphiteMetric);
            }
            
            if (metricCount != null) {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath + aggregatedMetricsSeparator + "Count", 
                        metricCount, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
                graphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                graphiteMetricsAggregated.add(graphiteMetric);
            }
            
            if (maximumMetricValue != null) {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath + aggregatedMetricsSeparator + "Max", 
                        maximumMetricValue, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
                graphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                graphiteMetricsAggregated.add(graphiteMetric);
            }
            
            if (medianMetricValue != null) {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath + aggregatedMetricsSeparator + "Median", 
                        medianMetricValue, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
                graphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                graphiteMetricsAggregated.add(graphiteMetric);
            }
            
            if (minimumMetricValue != null) {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath + aggregatedMetricsSeparator + "Min", 
                        minimumMetricValue, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
                graphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                graphiteMetricsAggregated.add(graphiteMetric);
            }
            
            if (rate != null) {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath + aggregatedMetricsSeparator + "Rate-Sec", 
                        rate, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
                graphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                graphiteMetricsAggregated.add(graphiteMetric);
            }
            
            if (sumMetricValues != null) {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath + aggregatedMetricsSeparator + "Sum", 
                        sumMetricValues, averagedMetricTimestamp, averagedMetricReceivedTimestamp);
                graphiteMetric.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                graphiteMetricsAggregated.add(graphiteMetric);
            }
            
            return graphiteMetricsAggregated;
        }
        else {
            return new ArrayList<>();
        }
    }
    
}
