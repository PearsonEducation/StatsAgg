package com.pearson.statsagg.metric_aggregation.statsd;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pearson.statsagg.database.gauges.Gauge;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StatsdMetricAggregator {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsdMetricAggregator.class.getName());
        
    public static final int STATSD_SCALE = 7;
    public static final int STATSD_PRECISION = 31;
    public static final RoundingMode STATSD_ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final MathContext STATSD_MATH_CONTEXT = new MathContext(STATSD_PRECISION, STATSD_ROUNDING_MODE);
    
    public static List<StatsdMetricAggregated> aggregateStatsdMetrics(List<StatsdMetricRaw> statsdMetricsRaw) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Byte,List<StatsdMetricRaw>> statsdMetricsRawByMetricTypeKey = divideStatsdMetricsRawByMetricTypeKey(statsdMetricsRaw);
        List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>(statsdMetricsRaw.size());
    
        for (Byte metricTypeKey : statsdMetricsRawByMetricTypeKey.keySet()) {
            
            Map<String,List<StatsdMetricRaw>> statsdMetricsRawByBucket = divideStatsdMetricsRawByBucket(statsdMetricsRawByMetricTypeKey.get(metricTypeKey));
            
            List<StatsdMetricAggregated> statsdMetricsAggregatedByBucket;
            
            if (metricTypeKey == StatsdMetricRaw.GAUGE_TYPE) { 
                statsdMetricsAggregatedByBucket = aggregateByBucketAndMetricTypeKey(statsdMetricsRawByBucket);
            }
            else {
                statsdMetricsAggregatedByBucket = aggregateByBucketAndMetricTypeKey(statsdMetricsRawByBucket);
            }
            
            if ((statsdMetricsAggregatedByBucket != null) && !statsdMetricsAggregatedByBucket.isEmpty()) {
                statsdMetricsAggregated.addAll(statsdMetricsAggregatedByBucket);
            }
        }

        return statsdMetricsAggregated;
    }
    
    public static List<StatsdMetricRaw> getStatsdMetricsRawByMetricType(List<StatsdMetricRaw> statsdMetricsRaw, String metricType) {
        
        if (statsdMetricsRaw == null || statsdMetricsRaw.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StatsdMetricRaw> statsdMetricsRawByMetricType = new ArrayList<>(statsdMetricsRaw.size());
        
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            if ((statsdMetricRaw != null) && statsdMetricRaw.getMetricType().equals(metricType)) {
                statsdMetricsRawByMetricType.add(statsdMetricRaw);
            }
        }
        
        return statsdMetricsRawByMetricType;
    }
    
    public static List<StatsdMetricRaw> getStatsdMetricsRawByMetricTypeKey(List<StatsdMetricRaw> statsdMetricsRaw, Byte metricTypeKey) {
        
        if (statsdMetricsRaw == null || statsdMetricsRaw.isEmpty() || (metricTypeKey == null)) {
            return new ArrayList<>();
        }
        
        List<StatsdMetricRaw> statsdMetricsRawByMetricTypeKey = new ArrayList<>(statsdMetricsRaw.size());
        
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            if ((statsdMetricRaw != null) && (statsdMetricRaw.getMetricTypeKey() == metricTypeKey)) {
                statsdMetricsRawByMetricTypeKey.add(statsdMetricRaw);
            }
        }
        
        return statsdMetricsRawByMetricTypeKey;
    }
    
    public static List<StatsdMetricRaw> getStatsdMetricsRawExcludeMetricType(List<StatsdMetricRaw> statsdMetricsRaw, String metricType) {
        
        if (statsdMetricsRaw == null || statsdMetricsRaw.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StatsdMetricRaw> statsdMetricsRawByMetricType = new ArrayList<>(statsdMetricsRaw.size());
        
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            if ((statsdMetricRaw != null) && !statsdMetricRaw.getMetricType().equals(metricType)) {
                statsdMetricsRawByMetricType.add(statsdMetricRaw);
            }
        }
        
        return statsdMetricsRawByMetricType;
    }
    
    public static List<StatsdMetricRaw> getStatsdMetricsRawExcludeMetricTypeKey(List<StatsdMetricRaw> statsdMetricsRaw, Byte metricTypeKey) {
        
        if (statsdMetricsRaw == null || statsdMetricsRaw.isEmpty() || (metricTypeKey == null)) {
            return new ArrayList<>();
        }
        
        List<StatsdMetricRaw> statsdMetricsRawByMetricTypeKey = new ArrayList<>(statsdMetricsRaw.size());
        
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            if ((statsdMetricRaw != null) && (statsdMetricRaw.getMetricTypeKey() != metricTypeKey)) {
                statsdMetricsRawByMetricTypeKey.add(statsdMetricRaw);
            }
        }
        
        return statsdMetricsRawByMetricTypeKey;
    }
    
    public static Map<Byte,List<StatsdMetricRaw>> divideStatsdMetricsRawByMetricTypeKey(List<StatsdMetricRaw> statsdMetricsRaw) {
        
        if (statsdMetricsRaw == null) {
            return new HashMap<>();
        }
        
        Map<Byte,List<StatsdMetricRaw>> statsdMetricsRawByMetricType = new HashMap<>(statsdMetricsRaw.size() / 2);

        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            Byte metricType = statsdMetricRaw.getMetricTypeKey();
            List<StatsdMetricRaw> statsdMetricRawByMetricType = statsdMetricsRawByMetricType.get(metricType);
            
            if (statsdMetricRawByMetricType != null) {
                statsdMetricRawByMetricType.add(statsdMetricRaw);
            }
            else {
                statsdMetricRawByMetricType = new ArrayList<>();
                statsdMetricRawByMetricType.add(statsdMetricRaw);
                statsdMetricsRawByMetricType.put(metricType, statsdMetricRawByMetricType);
            }
        }
        
        return statsdMetricsRawByMetricType;
    }
    
    public static Map<String,List<StatsdMetricRaw>> divideStatsdMetricsRawByBucket(List<StatsdMetricRaw> statsdMetricsRaw) {
        
        if (statsdMetricsRaw == null) {
            return new HashMap<>();
        }
        
        Map<String,List<StatsdMetricRaw>> statsdMetricsRawByBucket = new HashMap<>(statsdMetricsRaw.size());

        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            String bucket = statsdMetricRaw.getBucket();
            List<StatsdMetricRaw> statsdMetricRawByBucket = statsdMetricsRawByBucket.get(bucket);

            if (statsdMetricRawByBucket != null) {
                statsdMetricRawByBucket.add(statsdMetricRaw);
            }
            else {
                statsdMetricRawByBucket = new ArrayList<>();
                statsdMetricRawByBucket.add(statsdMetricRaw);
                statsdMetricsRawByBucket.put(bucket, statsdMetricRawByBucket);
            }
        }
        
        return statsdMetricsRawByBucket;
    }

    /* 
     * This method assumes that all of the input statsd metrics are already separated by buck name & by metric type.
     * The key of the input Map is the assumed to be: bucketName
     * The values of the input Map are assumed to be arraylists of StatsdMetricRaw objects that have been pre-sorted by metric type
     * The gaugeDao object is only required for gauge aggregation. It can be null for other metric types.
     */
    private static List<StatsdMetricAggregated> aggregateByBucketAndMetricTypeKey(Map<String,List<StatsdMetricRaw>> statsdMetricRawByBucketAndMetricType) {
        
        if ((statsdMetricRawByBucketAndMetricType == null) || statsdMetricRawByBucketAndMetricType.isEmpty()) {
            return new ArrayList<>();
        }

        List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>();
        Byte metricTypeKey = null;     
        
        for (String bucket : statsdMetricRawByBucketAndMetricType.keySet()) {
            List<StatsdMetricRaw> statsdMetricsByBucket = statsdMetricRawByBucketAndMetricType.get(bucket);
            
            if ((metricTypeKey == null) && (statsdMetricsByBucket != null) && !statsdMetricsByBucket.isEmpty()) {
                metricTypeKey = statsdMetricsByBucket.get(0).getMetricTypeKey();
            }

            StatsdMetricAggregated singleStatsdMetricAggregated = null;
            List<StatsdMetricAggregated> multipleStatsdMetricsAggregated = null;
                
            if ((metricTypeKey != null) && (statsdMetricsByBucket != null) && !statsdMetricsByBucket.isEmpty()) {
                
                if (metricTypeKey == StatsdMetricRaw.COUNTER_TYPE) {
                    multipleStatsdMetricsAggregated = aggregateCounter(statsdMetricsByBucket, 
                            ApplicationConfiguration.getFlushTimeAgg(), 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString());
                }
                else if (metricTypeKey == StatsdMetricRaw.TIMER_TYPE) {
                    multipleStatsdMetricsAggregated = aggregateTimer(statsdMetricsByBucket, 
                            ApplicationConfiguration.getFlushTimeAgg(), 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString());
                }
                else if (metricTypeKey == StatsdMetricRaw.GAUGE_TYPE) {
                    String prefixedBucketName = generatePrefix(StatsdMetricRaw.GAUGE_TYPE) + bucket;
                    Map<String,Gauge> statsdGaugeCache = GlobalVariables.statsdGaugeCache;
                    Gauge gaugeFromCache = statsdGaugeCache.get(prefixedBucketName);

                    singleStatsdMetricAggregated = aggregateGauge(statsdMetricsByBucket, gaugeFromCache);
                }
                else if (metricTypeKey == StatsdMetricRaw.SET_TYPE) {
                    singleStatsdMetricAggregated = aggregateSet(statsdMetricsByBucket);
                }
            }
            
            if (singleStatsdMetricAggregated != null) {
                statsdMetricsAggregated.add(singleStatsdMetricAggregated);
            }

            if ((multipleStatsdMetricsAggregated != null) && !multipleStatsdMetricsAggregated.isEmpty()) {
                statsdMetricsAggregated.addAll(multipleStatsdMetricsAggregated);
            }
        }

        return statsdMetricsAggregated;
    }
    
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static List<StatsdMetricAggregated> aggregateCounter(List<StatsdMetricRaw> statsdMetricsRaw, long flushIntervalInMilliseconds, String aggregatedMetricsSeparator) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
           return null; 
        }
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = "";

        BigDecimal aggregatedMetricValue = BigDecimal.ZERO;
        long sumTimestamp = 0;
        int metricCounter = 0;
        
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            
            try {
                BigDecimal metricValue = new BigDecimal(statsdMetricRaw.getMetricValue());

                sumTimestamp += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
                
                if (statsdMetricRaw.getSampleRate() != null) {
                    BigDecimal sampleRate = new BigDecimal(statsdMetricRaw.getSampleRate());
                    BigDecimal sampleRateMultiplier;
                    
                    if (sampleRate.compareTo(BigDecimal.ZERO) == 1) {
                        sampleRateMultiplier = new BigDecimal(1).divide(sampleRate, MathContext.DECIMAL64);
                    }
                    else {
                        sampleRateMultiplier = new BigDecimal(1);
                        
                        logger.warn("Invalid sample rate for counter=\"" + statsdMetricsRaw.get(0).getBucket() 
                                + "\". Value=\"" + statsdMetricRaw.getSampleRate() 
                                + "\". Defaulting to sample-rate of 1.0");
                    }
                    
                    aggregatedMetricValue = aggregatedMetricValue.add(metricValue.multiply(sampleRateMultiplier));
                }
                else {
                    aggregatedMetricValue = aggregatedMetricValue.add(metricValue);
                }

                metricCounter++;
            }
            catch (Exception e) {
                logger.error("Invalid data for counter=\"" + statsdMetricRaw.getBucket() 
                                + "\". Value=\"" + statsdMetricRaw.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        if (metricCounter > 0) {
            List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>();
            
            String bucketName = generatePrefix(StatsdMetricRaw.COUNTER_TYPE) + statsdMetricsRaw.get(0).getBucket();
            aggregatedMetricValue = MathUtilities.smartBigDecimalScaleChange(aggregatedMetricValue, STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal perSecondRate = MathUtilities.smartBigDecimalScaleChange(
                    aggregatedMetricValue.multiply(new BigDecimal((int) 1000))
                    .divide(new BigDecimal(flushIntervalInMilliseconds), STATSD_MATH_CONTEXT), 
                    STATSD_SCALE, STATSD_ROUNDING_MODE);
            long averagedTimestamp = Math.round((double) ((double) sumTimestamp / (double) metricCounter));

            String metricLabel = "Count";
            StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + metricLabel, aggregatedMetricValue, averagedTimestamp, StatsdMetricAggregated.COUNTER_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdMetricAggregated);
            
            metricLabel = "PerSecondRate";
            statsdMetricAggregated = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + metricLabel, perSecondRate, averagedTimestamp, StatsdMetricAggregated.COUNTER_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdMetricAggregated);

            return statsdMetricsAggregated;
        }
        else {
            return null;
        }
    }
    
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static List<StatsdMetricAggregated> aggregateTimer(List<StatsdMetricRaw> statsdMetricsRaw, long aggregationWindowLengthInMs, String aggregatedMetricsSeparator) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
           return new ArrayList<>(); 
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = "";
        
        List<BigDecimal> metricValues = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal sumOfSquares = BigDecimal.ZERO;
        BigDecimal minimumMetricValue = null;
        BigDecimal maximumMetricValue = null;
        
        long sumTimestamp = 0;
        int metricCounter = 0;

        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {

            try {
                BigDecimal metricValue = new BigDecimal(statsdMetricRaw.getMetricValue());

                metricValues.add(metricValue);
                sum = sum.add(metricValue);
                
                BigDecimal metricValueSquared = metricValue.multiply(metricValue);
                sumOfSquares = sumOfSquares.add(metricValueSquared);
                
                sumTimestamp += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
                
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
                logger.error("Invalid data for timer=\"" + statsdMetricRaw.getBucket()
                                + "\". Value=\"" + statsdMetricRaw.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }  
        }
        
        if (metricCounter > 0) {
            List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>();
            
            String bucketName = generatePrefix(StatsdMetricRaw.TIMER_TYPE) + statsdMetricsRaw.get(0).getBucket();

            sum = MathUtilities.smartBigDecimalScaleChange(sum, STATSD_SCALE, STATSD_ROUNDING_MODE);
            sumOfSquares = MathUtilities.smartBigDecimalScaleChange(sumOfSquares, STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal responsesPerInterval = new BigDecimal(metricCounter);
            BigDecimal averageMetricValue = MathUtilities.smartBigDecimalScaleChange(sum.divide(responsesPerInterval, STATSD_MATH_CONTEXT), STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal medianMetricValue = MathUtilities.smartBigDecimalScaleChange(MathUtilities.computeMedianOfBigDecimals(metricValues, STATSD_MATH_CONTEXT), STATSD_SCALE, STATSD_ROUNDING_MODE);
            minimumMetricValue = (minimumMetricValue != null) ? MathUtilities.smartBigDecimalScaleChange(minimumMetricValue, STATSD_SCALE, STATSD_ROUNDING_MODE) : null;
            maximumMetricValue = (maximumMetricValue != null) ? MathUtilities.smartBigDecimalScaleChange(maximumMetricValue, STATSD_SCALE, STATSD_ROUNDING_MODE) : null;
            BigDecimal responsesPerSecond = MathUtilities.smartBigDecimalScaleChange(
                    responsesPerInterval.multiply(new BigDecimal((int) 1000))
                    .divide(new BigDecimal(aggregationWindowLengthInMs), STATSD_MATH_CONTEXT), 
                    STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal standardDeviationResult = MathUtilities.smartBigDecimalScaleChange(MathUtilities.computePopulationStandardDeviationOfBigDecimals(metricValues), STATSD_SCALE, STATSD_ROUNDING_MODE);
                    
            long averagedTimestamp = Math.round((double) ((double) sumTimestamp / (double) metricCounter));

            StatsdMetricAggregated statsdTimerAverageResponseTime = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "mean",  
                    averageMetricValue, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdTimerAverageResponseTime.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdTimerAverageResponseTime);

            StatsdMetricAggregated statsdTimerMedianResponseTime = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "median",  
                    medianMetricValue, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdTimerMedianResponseTime.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdTimerMedianResponseTime);
            
            StatsdMetricAggregated statsdTimerMaximumResponseTime = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "upper",  
                    maximumMetricValue, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdTimerMaximumResponseTime.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdTimerMaximumResponseTime);
            
            StatsdMetricAggregated statsdTimerMinimumResponseTime = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "lower",  
                    minimumMetricValue, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdTimerMinimumResponseTime.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdTimerMinimumResponseTime);
            
            StatsdMetricAggregated statsdTimerResponsesPerInterval = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "count", 
                    responsesPerInterval, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdTimerResponsesPerInterval.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdTimerResponsesPerInterval);
            
            StatsdMetricAggregated statsdTimerResponsesPerSecond = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "count_ps", 
                    responsesPerSecond, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdTimerResponsesPerSecond.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdTimerResponsesPerSecond);
    
            StatsdMetricAggregated statsdSum = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "sum",  
                    sum, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdSum.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdSum);
            
            StatsdMetricAggregated statsdSumOfSquares = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "sum_squares",  
                    sumOfSquares, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdSumOfSquares.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdSumOfSquares);
            
            StatsdMetricAggregated statsdStandardDeviation = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "std",  
                    standardDeviationResult, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
            statsdStandardDeviation.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdStandardDeviation);
            
            return statsdMetricsAggregated;
        }
        else {
            return new ArrayList<>();
        }
    }
    
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static StatsdMetricAggregated aggregateGauge(List<StatsdMetricRaw> statsdMetricsRaw, Gauge gaugeInitial) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
           return null; 
        }

        BigDecimal aggregatedMetricValue;
        long sumTimestamp = 0;
        int metricCounter = 0;
        
        if (gaugeInitial == null) {
            aggregatedMetricValue = BigDecimal.ZERO;
        }
        else {
            aggregatedMetricValue = gaugeInitial.getMetricValue();
        }
  
        List<StatsdMetricRaw> statsdMetricsRawLocal = new ArrayList<>(statsdMetricsRaw);
        Collections.sort(statsdMetricsRawLocal, StatsdMetricRaw.COMPARE_BY_HASH_KEY);

        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRawLocal) {

            try {
                BigDecimal metricValue = new BigDecimal(statsdMetricRaw.getMetricValue());
                
                if (statsdMetricRaw.getMetricValue().contains("+") || statsdMetricRaw.getMetricValue().contains("-")) {
                    aggregatedMetricValue = aggregatedMetricValue.add(metricValue);
                }
                else {
                    aggregatedMetricValue = metricValue;
                }
                
                sumTimestamp += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
                metricCounter++;
            }
            catch (Exception e) {
                logger.error("Invalid data for gauge=\"" + statsdMetricRaw.getBucket()
                                + "\". Value=\"" + statsdMetricRaw.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
        }
        
        if (metricCounter > 0) {
            String bucketName = generatePrefix(StatsdMetricRaw.GAUGE_TYPE) + statsdMetricsRawLocal.get(0).getBucket();
            aggregatedMetricValue = MathUtilities.smartBigDecimalScaleChange(aggregatedMetricValue, STATSD_SCALE, STATSD_ROUNDING_MODE);
            long averagedTimestamp = Math.round((double) ((double) sumTimestamp / (double) metricCounter));
            StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(bucketName, aggregatedMetricValue, averagedTimestamp, StatsdMetricAggregated.GAUGE_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            return statsdMetricAggregated;
        }
        else {
            return null;
        }
    }
        
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static StatsdMetricAggregated aggregateSet(List<StatsdMetricRaw> statsdMetricsRaw) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
           return null; 
        }
        
        Set<String> metricSet = new HashSet<>();
        long sumTimestamp = 0;
        int metricCounter = 0;
        
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {

            try {
                BigDecimal metricValue = new BigDecimal(statsdMetricRaw.getMetricValue());
                String metricValueNormalized = metricValue.toPlainString();
                metricSet.add(metricValueNormalized);
                sumTimestamp += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
                metricCounter++;
            }
            catch (Exception e) {
                logger.error("Invalid data for set =\"" + statsdMetricRaw.getBucket()
                                + "\". Value=\"" + statsdMetricRaw.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        if (metricCounter > 0) {
            String bucketName = generatePrefix(StatsdMetricRaw.SET_TYPE) + statsdMetricsRaw.get(0).getBucket();
            BigDecimal uniqueMetricValueCount = new BigDecimal(metricSet.size());
            long averagedTimestamp = Math.round((double) ((double) sumTimestamp / (double) metricCounter));
            StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(bucketName, uniqueMetricValueCount, averagedTimestamp, StatsdMetricAggregated.SET_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());
            return statsdMetricAggregated;
        }
        else {
            return null;
        }
    }
    
    private static String generatePrefix(Byte metricTypeKey) {
        
        StringBuilder prefix = new StringBuilder("");
        
        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled()) {
            prefix.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        }
        
        if (ApplicationConfiguration.isStatsdMetricNamePrefixEnabled()) {
            prefix.append(ApplicationConfiguration.getStatsdMetricNamePrefixValue()).append(".");
        }
        
        if (metricTypeKey == null) {
            return prefix.toString();
        }

        if (ApplicationConfiguration.isStatsdCounterMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.COUNTER_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdCounterMetricNamePrefixValue()).append(".");
        }
        else if (ApplicationConfiguration.isStatsdTimerMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.TIMER_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdTimerMetricNamePrefixValue()).append(".");
        }
        else if (ApplicationConfiguration.isStatsdGaugeMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.GAUGE_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdGaugeMetricNamePrefixValue()).append(".");
        }
        else if (ApplicationConfiguration.isStatsdSetMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.SET_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdSetMetricNamePrefixValue()).append(".");
        }
        
        return prefix.toString();
    }

}
