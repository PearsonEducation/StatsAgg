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
    private static final BigDecimal ONE_THOUSAND = new BigDecimal((int) 1000);

    private static String statsdSuffix_ = null;
    private static String counterMetricPrefix_ = null;
    private static String counterMetricLegacyPrefix_ = null;
    private static String timerMetricPrefix_ = null;
    private static String gaugeMetricPrefix_ = null;
    private static String setMetricPrefix_ = null;
    
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
        
        BigDecimal aggregationWindowLengthInMs = new BigDecimal(ApplicationConfiguration.getFlushTimeAgg());
        
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
                            aggregationWindowLengthInMs, 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString(),
                            ApplicationConfiguration.isStatsdUseLegacyNameSpacing());
                }
                else if (metricTypeKey == StatsdMetricRaw.TIMER_TYPE) {
                    multipleStatsdMetricsAggregated = aggregateTimer(statsdMetricsByBucket, 
                            aggregationWindowLengthInMs, 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString(),
                            ApplicationConfiguration.getStatsdNthPercentiles(),
                            ApplicationConfiguration.isStatsdUseLegacyNameSpacing());
                }
                else if (metricTypeKey == StatsdMetricRaw.GAUGE_TYPE) {
                    String prefixedBucketName = generatePrefix(StatsdMetricRaw.GAUGE_TYPE, ApplicationConfiguration.isStatsdUseLegacyNameSpacing()) + bucket + generateSeparatorAndSuffix();
                    Map<String,Gauge> statsdGaugeCache = GlobalVariables.statsdGaugeCache;
                    Gauge gaugeFromCache = statsdGaugeCache.get(prefixedBucketName);

                    singleStatsdMetricAggregated = aggregateGauge(statsdMetricsByBucket, 
                            gaugeFromCache, 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString(),
                            ApplicationConfiguration.isStatsdUseLegacyNameSpacing());
                }
                else if (metricTypeKey == StatsdMetricRaw.SET_TYPE) {
                    singleStatsdMetricAggregated = aggregateSet(statsdMetricsByBucket, 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString(),
                            ApplicationConfiguration.isStatsdUseLegacyNameSpacing());
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
    public static List<StatsdMetricAggregated> aggregateCounter(List<StatsdMetricRaw> statsdMetricsRaw, BigDecimal aggregationWindowLengthInMs, 
            String aggregatedMetricsSeparator, boolean useLegacyNameSpacing) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
           return new ArrayList<>();
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = "";

        BigDecimal count = BigDecimal.ZERO;
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
                        sampleRateMultiplier = BigDecimal.ONE.divide(sampleRate, MathContext.DECIMAL64);
                    }
                    else {
                        sampleRateMultiplier = BigDecimal.ONE;
                        
                        logger.warn("Invalid sample rate for counter=\"" + statsdMetricsRaw.get(0).getBucket() 
                                + "\". Value=\"" + statsdMetricRaw.getSampleRate() 
                                + "\". Defaulting to sample-rate of 1.0");
                    }
                    
                    count = count.add(metricValue.multiply(sampleRateMultiplier));
                }
                else {
                    count = count.add(metricValue);
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
            
            count = MathUtilities.smartBigDecimalScaleChange(count, STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal ratePs = MathUtilities.smartBigDecimalScaleChange(
                    count.multiply(ONE_THOUSAND)
                    .divide(aggregationWindowLengthInMs, STATSD_MATH_CONTEXT), 
                    STATSD_SCALE, STATSD_ROUNDING_MODE);
            long averagedTimestamp = Math.round((double) sumTimestamp / (double) metricCounter);
                        
            String bucket_Count;
            if (useLegacyNameSpacing) {
                String prefix = "";
                if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled()) prefix = ApplicationConfiguration.getGlobalMetricNamePrefixValue() + aggregatedMetricsSeparator;
                bucket_Count = prefix + "stats_counts" + aggregatedMetricsSeparator + statsdMetricsRaw.get(0).getBucket() + generateSeparatorAndSuffix();
            }
            else {
                bucket_Count = generatePrefix(StatsdMetricRaw.COUNTER_TYPE, useLegacyNameSpacing) + 
                        statsdMetricsRaw.get(0).getBucket() + aggregatedMetricsSeparator + "count" + generateSeparatorAndSuffix();
            }

            StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(bucket_Count, count, averagedTimestamp, StatsdMetricAggregated.COUNTER_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdMetricAggregated);
            
            String bucket_Rate = useLegacyNameSpacing ? 
                    generatePrefix(StatsdMetricRaw.COUNTER_TYPE, useLegacyNameSpacing) + statsdMetricsRaw.get(0).getBucket() + generateSeparatorAndSuffix() : 
                    generatePrefix(StatsdMetricRaw.COUNTER_TYPE, useLegacyNameSpacing) + statsdMetricsRaw.get(0).getBucket() + aggregatedMetricsSeparator + "rate" + generateSeparatorAndSuffix();
            statsdMetricAggregated = new StatsdMetricAggregated(bucket_Rate, ratePs, averagedTimestamp, StatsdMetricAggregated.COUNTER_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdMetricAggregated);

            return statsdMetricsAggregated;
        }
        else {
            return new ArrayList<>();
        }
    }
    
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static List<StatsdMetricAggregated> aggregateTimer(List<StatsdMetricRaw> statsdMetricsRaw, BigDecimal aggregationWindowLengthInMs, 
            String aggregatedMetricsSeparator, StatsdNthPercentiles statsdNthPercentiles, boolean useLegacyNameSpacing) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
           return new ArrayList<>(); 
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = "";
        
        List<String> nthPercentageFractional_StatsdFormattedStrings = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted();
        List<BigDecimal> nthPercentageFractionals = statsdNthPercentiles.getNthPercentiles_Fractional();
        
        List<BigDecimal> metricValues = new ArrayList<>();
        List<BigDecimal> rollingSum = new ArrayList<>();
        List<BigDecimal> rollingSumOfSquares = new ArrayList<>();

        BigDecimal lower = null, upper = null, sum = BigDecimal.ZERO, sumOfSquares = BigDecimal.ZERO;
        List<BigDecimal> countNthPercentiles = null, meanNthPercentiles = null, lowerNthPercentiles = null, sumNthPercentiles = null, sumOfSquaresNthPercentiles = null, upperNthPercentiles = null;
        List<String> outputPercentageStringsNthPercentiles = null;
        
        int metricCounter = 0;
        long sumTimestamp = 0;

        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            try {
                BigDecimal metricValue = new BigDecimal(statsdMetricRaw.getMetricValue());
                metricValues.add(metricValue);
                sumTimestamp += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
            }
            catch (Exception e) {
                logger.error("Invalid data for timer=\"" + statsdMetricRaw.getBucket()
                                + "\". Value=\"" + statsdMetricRaw.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }  
        }
        
        Collections.sort(metricValues);
        
        for (BigDecimal metricValue : metricValues) {            
            sum = sum.add(metricValue);
            rollingSum.add(sum);
            
            BigDecimal metricValueSquared = metricValue.multiply(metricValue);
            sumOfSquares = sumOfSquares.add(metricValueSquared);
            rollingSumOfSquares.add(sumOfSquares);

            if (metricCounter == 0) {
                upper = metricValue;
                lower = metricValue;
            }
            else {
                if ((upper != null) && upper.compareTo(metricValue) == -1) upper = metricValue;
                if ((lower != null) && lower.compareTo(metricValue) == 1) lower = metricValue;
            }

            metricCounter++;
        }
        
        BigDecimal metricCounter_BigDecimal = new BigDecimal(metricCounter);
        int metricCounterMinusOne = metricCounter - 1;
        
        // calculate nth pct values
        if ((metricCounter > 0) && (nthPercentageFractionals != null) && !nthPercentageFractionals.isEmpty()) {
            countNthPercentiles = new ArrayList<>();
            lowerNthPercentiles = new ArrayList<>();
            meanNthPercentiles = new ArrayList<>();
            sumNthPercentiles = new ArrayList<>();
            sumOfSquaresNthPercentiles = new ArrayList<>();
            outputPercentageStringsNthPercentiles = new ArrayList<>();
            upperNthPercentiles = new ArrayList<>();
            
            for (int i = 0; i < nthPercentageFractionals.size(); i++) {
                BigDecimal nthPercentageFractional = nthPercentageFractionals.get(i);
                int indexOfNthPercentile;
                boolean isNthPercentageNegative = false;
                int nthPercentageFractionalComparedToZero = nthPercentageFractional.compareTo(BigDecimal.ZERO);
                
                if (nthPercentageFractionalComparedToZero < 0) {
                    nthPercentageFractional = nthPercentageFractional.abs();
                    isNthPercentageNegative = true;
                }
                    
                if ((nthPercentageFractionalComparedToZero > 0) || (nthPercentageFractionalComparedToZero < 0)) { // positive or negative nth percentile
                    BigDecimal nthPercentileOfMetricValues = nthPercentageFractional.multiply(metricCounter_BigDecimal, STATSD_MATH_CONTEXT);
                    indexOfNthPercentile = nthPercentileOfMetricValues.setScale(0, RoundingMode.HALF_UP).intValue() - 1;
                    if (indexOfNthPercentile > metricCounterMinusOne) indexOfNthPercentile = metricCounterMinusOne;      
                }
                else indexOfNthPercentile = -1;   // nth percentile = 0 (invalid)
                
                if (indexOfNthPercentile > -1) {
                    BigDecimal countNthPercentile, lowerNthPercentile = null, meanNthPercentile = null, sumNthPercentile = null, sumOfSquaresNthPercentile = null, upperNthPercentile = null;
                    
                    if (!isNthPercentageNegative) {
                        countNthPercentile = new BigDecimal(indexOfNthPercentile + 1);
                        sumNthPercentile = MathUtilities.smartBigDecimalScaleChange(rollingSum.get(indexOfNthPercentile), STATSD_SCALE, STATSD_ROUNDING_MODE);
                        meanNthPercentile = MathUtilities.smartBigDecimalScaleChange(sumNthPercentile.divide(countNthPercentile, STATSD_MATH_CONTEXT), STATSD_SCALE, STATSD_ROUNDING_MODE);
                        sumOfSquaresNthPercentile = MathUtilities.smartBigDecimalScaleChange(rollingSumOfSquares.get(indexOfNthPercentile), STATSD_SCALE, STATSD_ROUNDING_MODE);
                        upperNthPercentile = MathUtilities.smartBigDecimalScaleChange(metricValues.get(indexOfNthPercentile), STATSD_SCALE, STATSD_ROUNDING_MODE);
                    }
                    else {
                        int indexForSumSubtractor = metricCounter - indexOfNthPercentile - 2;
                        countNthPercentile = new BigDecimal(indexOfNthPercentile + 1);
                        lowerNthPercentile = MathUtilities.smartBigDecimalScaleChange(metricValues.get(metricCounter - indexOfNthPercentile - 1), STATSD_SCALE, STATSD_ROUNDING_MODE);
                        if (indexForSumSubtractor >= 0) sumNthPercentile = MathUtilities.smartBigDecimalScaleChange(rollingSum.get(metricCounterMinusOne).subtract(rollingSum.get(indexForSumSubtractor)), STATSD_SCALE, STATSD_ROUNDING_MODE);
                        if (sumNthPercentile != null) meanNthPercentile = MathUtilities.smartBigDecimalScaleChange(sumNthPercentile.divide(countNthPercentile, STATSD_MATH_CONTEXT), STATSD_SCALE, STATSD_ROUNDING_MODE);
                        if (sumNthPercentile != null) sumOfSquaresNthPercentile = MathUtilities.smartBigDecimalScaleChange(rollingSumOfSquares.get(metricCounterMinusOne).subtract(rollingSumOfSquares.get(indexForSumSubtractor)), STATSD_SCALE, STATSD_ROUNDING_MODE);
                    }
                    
                    countNthPercentiles.add(countNthPercentile);
                    lowerNthPercentiles.add(lowerNthPercentile);
                    meanNthPercentiles.add(meanNthPercentile);
                    sumNthPercentiles.add(sumNthPercentile);
                    sumOfSquaresNthPercentiles.add(sumOfSquaresNthPercentile);
                    upperNthPercentiles.add(upperNthPercentile);
                    outputPercentageStringsNthPercentiles.add(nthPercentageFractional_StatsdFormattedStrings.get(i));
                }
            }
        }
        
        // create metrics for output
        if (metricCounter > 0) {
            List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>();
            
            String bucketName = generatePrefix(StatsdMetricRaw.TIMER_TYPE, useLegacyNameSpacing) + statsdMetricsRaw.get(0).getBucket();
            long averagedTimestamp = Math.round((double) sumTimestamp / (double) metricCounter);

            BigDecimal count = metricCounter_BigDecimal;
            BigDecimal countPs = MathUtilities.smartBigDecimalScaleChange(
                    count.multiply(ONE_THOUSAND)
                    .divide(aggregationWindowLengthInMs, STATSD_MATH_CONTEXT), 
                    STATSD_SCALE, STATSD_ROUNDING_MODE);
            sum = MathUtilities.smartBigDecimalScaleChange(sum, STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal mean = MathUtilities.smartBigDecimalScaleChange(sum.divide(count, STATSD_MATH_CONTEXT), STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal median = MathUtilities.smartBigDecimalScaleChange(MathUtilities.computeMedianOfBigDecimals(metricValues, STATSD_MATH_CONTEXT, true), STATSD_SCALE, STATSD_ROUNDING_MODE);
            lower = (lower != null) ? MathUtilities.smartBigDecimalScaleChange(lower, STATSD_SCALE, STATSD_ROUNDING_MODE) : null;
            BigDecimal standardDeviation = MathUtilities.smartBigDecimalScaleChange(MathUtilities.computePopulationStandardDeviationOfBigDecimals(metricValues), STATSD_SCALE, STATSD_ROUNDING_MODE);
            sumOfSquares = MathUtilities.smartBigDecimalScaleChange(sumOfSquares, STATSD_SCALE, STATSD_ROUNDING_MODE);
            upper = (upper != null) ? MathUtilities.smartBigDecimalScaleChange(upper, STATSD_SCALE, STATSD_ROUNDING_MODE) : null;
            
            if (count != null) {
                StatsdMetricAggregated statsdCount = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "count" + generateSeparatorAndSuffix(),  
                        count, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdCount.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdCount);
            }
    
            if ((countNthPercentiles != null) && !countNthPercentiles.isEmpty()) {
                for (int i = 0; i < countNthPercentiles.size(); i++) {
                    if ((outputPercentageStringsNthPercentiles == null) || (outputPercentageStringsNthPercentiles.get(i) == null)) continue;
                    BigDecimal countNthPercentile = countNthPercentiles.get(i);
                    if (countNthPercentile == null) continue;
                    StatsdMetricAggregated statsdCountNthPercentile = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + 
                            "count_" + outputPercentageStringsNthPercentiles.get(i) + generateSeparatorAndSuffix(),  
                            countNthPercentile, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                    statsdCountNthPercentile.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    statsdMetricsAggregated.add(statsdCountNthPercentile);
                }
            }
            
            if (countPs != null) {
                StatsdMetricAggregated statsdCountPs = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "count_ps" + generateSeparatorAndSuffix(), 
                        countPs, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdCountPs.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdCountPs);
            }
            
            if (lower != null) {
                StatsdMetricAggregated statsdLower = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "lower" + generateSeparatorAndSuffix(),  
                        lower, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdLower.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdLower);
            }
            
            if ((lowerNthPercentiles != null) && !lowerNthPercentiles.isEmpty()) {
                for (int i = 0; i < lowerNthPercentiles.size(); i++) {
                    if ((outputPercentageStringsNthPercentiles == null) || (outputPercentageStringsNthPercentiles.get(i) == null)) continue;
                    BigDecimal lowerNthPercentile = lowerNthPercentiles.get(i);
                    if (lowerNthPercentile == null) continue;
                    StatsdMetricAggregated statsdLowerNthPercentile = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator 
                            + "lower_" + outputPercentageStringsNthPercentiles.get(i) + generateSeparatorAndSuffix(),   
                            lowerNthPercentile, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                    statsdLowerNthPercentile.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    statsdMetricsAggregated.add(statsdLowerNthPercentile);
                }
            }
            
            if (mean != null) {
                StatsdMetricAggregated statsdMean = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "mean" + generateSeparatorAndSuffix(), 
                        mean, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdMean.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdMean);
            }
            
            if ((meanNthPercentiles != null) && !meanNthPercentiles.isEmpty()) {
                for (int i = 0; i < meanNthPercentiles.size(); i++) {
                    if ((outputPercentageStringsNthPercentiles == null) || (outputPercentageStringsNthPercentiles.get(i) == null)) continue;
                    BigDecimal meanNthPercentile = meanNthPercentiles.get(i);
                    if (meanNthPercentile == null) continue;
                    StatsdMetricAggregated statsdMeanNthPercentile = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + 
                            "mean_" + outputPercentageStringsNthPercentiles.get(i) + generateSeparatorAndSuffix(),  
                            meanNthPercentile, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                    statsdMeanNthPercentile.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    statsdMetricsAggregated.add(statsdMeanNthPercentile);
                }
            }
            
            if (median != null) {
                StatsdMetricAggregated statsdMedian = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "median" + generateSeparatorAndSuffix(),   
                        median, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdMedian.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdMedian);
            }

            if (sum != null) {
                StatsdMetricAggregated statsdSum = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "sum" + generateSeparatorAndSuffix(),   
                        sum, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdSum.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdSum);
            }
            
            if ((sumNthPercentiles != null) && !sumNthPercentiles.isEmpty()) {
                for (int i = 0; i < sumNthPercentiles.size(); i++) {
                    if ((outputPercentageStringsNthPercentiles == null) || (outputPercentageStringsNthPercentiles.get(i) == null)) continue;
                    BigDecimal sumNthPercentile = sumNthPercentiles.get(i);
                    if (sumNthPercentile == null) continue;
                    StatsdMetricAggregated statsdSumNthPercentile = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + 
                            "sum_" + outputPercentageStringsNthPercentiles.get(i) + generateSeparatorAndSuffix(),  
                            sumNthPercentile, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                    statsdSumNthPercentile.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    statsdMetricsAggregated.add(statsdSumNthPercentile);
                }
            }
            
            if (sumOfSquares != null) {
                StatsdMetricAggregated statsdSumOfSquares = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "sum_squares" + generateSeparatorAndSuffix(),  
                        sumOfSquares, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdSumOfSquares.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdSumOfSquares);
            }
            
            if ((sumOfSquaresNthPercentiles != null) && !sumOfSquaresNthPercentiles.isEmpty()) {
                for (int i = 0; i < sumOfSquaresNthPercentiles.size(); i++) {
                    if ((outputPercentageStringsNthPercentiles == null) || (outputPercentageStringsNthPercentiles.get(i) == null)) continue;
                    BigDecimal sumOfSquaresNthPercentile = sumOfSquaresNthPercentiles.get(i);
                    if (sumOfSquaresNthPercentile == null) continue;
                    StatsdMetricAggregated statsdSumOfSquares_NthPercentile = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + 
                            "sum_squares_" + outputPercentageStringsNthPercentiles.get(i) + generateSeparatorAndSuffix(),   
                            sumOfSquaresNthPercentile, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                    statsdSumOfSquares_NthPercentile.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    statsdMetricsAggregated.add(statsdSumOfSquares_NthPercentile);
                }
            }
            
            if (standardDeviation != null) {
                StatsdMetricAggregated statsdStandardDeviation = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "std" + generateSeparatorAndSuffix(),  
                        standardDeviation, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdStandardDeviation.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdStandardDeviation);
            }
            
            if (upper != null) {
                StatsdMetricAggregated statsdUpper = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "upper" + generateSeparatorAndSuffix(),  
                        upper, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                statsdUpper.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                statsdMetricsAggregated.add(statsdUpper);
            }
            
            if ((upperNthPercentiles != null) && !upperNthPercentiles.isEmpty()) {
                for (int i = 0; i < upperNthPercentiles.size(); i++) {
                    if ((outputPercentageStringsNthPercentiles == null) || (outputPercentageStringsNthPercentiles.get(i) == null)) continue;
                    BigDecimal upperNthPercentile = upperNthPercentiles.get(i);
                    if (upperNthPercentile == null) continue;
                    StatsdMetricAggregated statsdUpperNthPercentile = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator 
                            + "upper_" + outputPercentageStringsNthPercentiles.get(i) + generateSeparatorAndSuffix(),   
                            upperNthPercentile, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                    statsdUpperNthPercentile.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    statsdMetricsAggregated.add(statsdUpperNthPercentile);
                }
            }
            
            return statsdMetricsAggregated;
        }
        else {
            return new ArrayList<>();
        }
    }
    
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static StatsdMetricAggregated aggregateGauge(List<StatsdMetricRaw> statsdMetricsRaw, Gauge gaugeInitial, 
            String aggregatedMetricsSeparator, boolean useLegacyNameSpacing) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
           return null; 
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = "";

        long sumTimestamp = 0;
        int metricCounter = 0;
        BigDecimal aggregatedMetricValue;

        if (gaugeInitial == null) aggregatedMetricValue = BigDecimal.ZERO;
        else aggregatedMetricValue = gaugeInitial.getMetricValue();

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
            String bucketName = generatePrefix(StatsdMetricRaw.GAUGE_TYPE, useLegacyNameSpacing) + statsdMetricsRawLocal.get(0).getBucket() + generateSeparatorAndSuffix();
            long averagedTimestamp = Math.round((double) sumTimestamp / (double) metricCounter);
            aggregatedMetricValue = MathUtilities.smartBigDecimalScaleChange(aggregatedMetricValue, STATSD_SCALE, STATSD_ROUNDING_MODE);
            StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(bucketName, aggregatedMetricValue, averagedTimestamp, StatsdMetricAggregated.GAUGE_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            return statsdMetricAggregated;
        }
        else {
            return null;
        }
    }
        
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static StatsdMetricAggregated aggregateSet(List<StatsdMetricRaw> statsdMetricsRaw, String aggregatedMetricsSeparator, boolean useLegacyNameSpacing) {
        
        if ((statsdMetricsRaw == null) || statsdMetricsRaw.isEmpty()) {
            return null; 
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = "";
        
        Set<String> metricSet = new HashSet<>();
        long sumTimestamp = 0;
        int metricCounter = 0;
        
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {

            try {
                BigDecimal metricValue = new BigDecimal(statsdMetricRaw.getMetricValue());
                String metricValueNormalized = metricValue.stripTrailingZeros().toPlainString();
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
            String bucketName = generatePrefix(StatsdMetricRaw.SET_TYPE, useLegacyNameSpacing) + statsdMetricsRaw.get(0).getBucket() + aggregatedMetricsSeparator + "count" + generateSeparatorAndSuffix();
            long averagedTimestamp = Math.round((double) sumTimestamp / (double) metricCounter);
            BigDecimal uniqueMetricValueCount = new BigDecimal(metricSet.size());
            StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(bucketName, uniqueMetricValueCount, averagedTimestamp, StatsdMetricAggregated.SET_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            return statsdMetricAggregated;
        }
        else {
            return null;
        }
    }
    
    private static String generatePrefix(Byte metricTypeKey, boolean useLegacyNameSpacing) {
                
        if (metricTypeKey == null) {
            StringBuilder prefix = new StringBuilder("");
            if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled()) prefix.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
            if (ApplicationConfiguration.isStatsdMetricNamePrefixEnabled()) prefix.append(ApplicationConfiguration.getStatsdMetricNamePrefixValue()).append(".");
            if (metricTypeKey == null) return prefix.toString();
        }
        else if ((metricTypeKey == StatsdMetricRaw.COUNTER_TYPE) && useLegacyNameSpacing && (counterMetricLegacyPrefix_ != null)) return counterMetricLegacyPrefix_;
        else if ((metricTypeKey == StatsdMetricRaw.COUNTER_TYPE) && !useLegacyNameSpacing && (counterMetricPrefix_ != null)) return counterMetricPrefix_;
        else if ((metricTypeKey == StatsdMetricRaw.TIMER_TYPE) && (timerMetricPrefix_ != null)) return timerMetricPrefix_;
        else if ((metricTypeKey == StatsdMetricRaw.GAUGE_TYPE) && (gaugeMetricPrefix_ != null)) return gaugeMetricPrefix_;
        else if ((metricTypeKey == StatsdMetricRaw.SET_TYPE) && (setMetricPrefix_ != null)) return setMetricPrefix_;

        StringBuilder prefix = new StringBuilder("");
        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled()) prefix.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        if (ApplicationConfiguration.isStatsdMetricNamePrefixEnabled() && !useLegacyNameSpacing) prefix.append(ApplicationConfiguration.getStatsdMetricNamePrefixValue()).append(".");
        if (useLegacyNameSpacing) prefix.append("stats").append(".");
        
        if (ApplicationConfiguration.isStatsdCounterMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.COUNTER_TYPE) && useLegacyNameSpacing) {
            counterMetricLegacyPrefix_ = prefix.toString();
        }
        else if (ApplicationConfiguration.isStatsdCounterMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.COUNTER_TYPE) && !useLegacyNameSpacing) {
            prefix.append(ApplicationConfiguration.getStatsdCounterMetricNamePrefixValue()).append(".");
            counterMetricPrefix_ = prefix.toString();
        }
        else if (ApplicationConfiguration.isStatsdTimerMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.TIMER_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdTimerMetricNamePrefixValue()).append(".");
            timerMetricPrefix_ = prefix.toString();
        } 
        else if (ApplicationConfiguration.isStatsdGaugeMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.GAUGE_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdGaugeMetricNamePrefixValue()).append(".");
            gaugeMetricPrefix_ = prefix.toString();
        }
        else if (ApplicationConfiguration.isStatsdSetMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetricRaw.SET_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdSetMetricNamePrefixValue()).append(".");
            setMetricPrefix_ = prefix.toString();
        }
        
        return prefix.toString();
    }
    
    private static String generateSeparatorAndSuffix() {
        
        if (statsdSuffix_ != null) {
            return statsdSuffix_;
        }
        
        if (ApplicationConfiguration.isStatsdMetricNameSuffixEnabled()) {
            statsdSuffix_ = "." + ApplicationConfiguration.getStatsdMetricNameSuffixValue();
        }
        else {
            statsdSuffix_ = "";
        }
        
        return statsdSuffix_;
    }

}
