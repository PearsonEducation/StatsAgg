package com.pearson.statsagg.metric_aggregation.aggregators;

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
import com.pearson.statsagg.globals.StatsdHistogramConfiguration;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetric;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.globals.StatsdNthPercentiles;
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
    public static final BigDecimal ONE_THOUSAND = new BigDecimal((int) 1000);

    private static String counterMetricPrefix_ = null;
    private static String counterMetricLegacyPrefix_ = null;
    private static String timerMetricPrefix_ = null;
    private static String gaugeMetricPrefix_ = null;
    private static String setMetricPrefix_ = null;
    private static String statsdSuffix_ = null;
    
    public static List<StatsdMetricAggregated> aggregateStatsdMetrics(List<StatsdMetric> statsdMetrics) {
        
        if ((statsdMetrics == null) || statsdMetrics.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Byte,List<StatsdMetric>> statsdMetricsByMetricTypeKey = divideStatsdMetricsByMetricTypeCode(statsdMetrics);
        List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>(statsdMetrics.size());
    
        for (Byte metricTypeKey : statsdMetricsByMetricTypeKey.keySet()) {
            Map<String,List<StatsdMetric>> statsdMetricsByBucket = divideStatsdMetricsByBucket(statsdMetricsByMetricTypeKey.get(metricTypeKey));
            
            List<StatsdMetricAggregated> statsdMetricsAggregatedByBucket = aggregateByBucketAndMetricType(statsdMetricsByBucket, ApplicationConfiguration.getStatsdHistogramConfigurations());

            if ((statsdMetricsAggregatedByBucket != null) && !statsdMetricsAggregatedByBucket.isEmpty()) {
                statsdMetricsAggregated.addAll(statsdMetricsAggregatedByBucket);
            }
        }

        return statsdMetricsAggregated;
    }
    
    public static List<StatsdMetric> getStatsdMetricsByMetricType(List<StatsdMetric> statsdMetrics, String metricType) {
        
        if (statsdMetrics == null || statsdMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StatsdMetric> statsdMetricsByMetricType = new ArrayList<>(statsdMetrics.size());
        
        for (StatsdMetric statsdMetric : statsdMetrics) {
            if ((statsdMetric != null) && statsdMetric.getMetricType().equals(metricType)) {
                statsdMetricsByMetricType.add(statsdMetric);
            }
        }
        
        return statsdMetricsByMetricType;
    }
    
    public static List<StatsdMetric> getStatsdMetricsByMetricTypeCode(List<StatsdMetric> statsdMetrics, Byte metricTypeCode) {
        
        if (statsdMetrics == null || statsdMetrics.isEmpty() || (metricTypeCode == null)) {
            return new ArrayList<>();
        }
        
        List<StatsdMetric> statsdMetricsByMetricTypeCode = new ArrayList<>(statsdMetrics.size());
        
        for (StatsdMetric statsdMetric : statsdMetrics) {
            if ((statsdMetric != null) && (statsdMetric.getMetricTypeCode() == metricTypeCode)) {
                statsdMetricsByMetricTypeCode.add(statsdMetric);
            }
        }
        
        return statsdMetricsByMetricTypeCode;
    }
    
    public static List<StatsdMetric> getStatsdMetricsExcludeMetricType(List<StatsdMetric> statsdMetrics, String metricType) {
        
        if (statsdMetrics == null || statsdMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StatsdMetric> statsdMetricsByMetricType = new ArrayList<>(statsdMetrics.size());
        
        for (StatsdMetric statsdMetric : statsdMetrics) {
            if ((statsdMetric != null) && !statsdMetric.getMetricType().equals(metricType)) {
                statsdMetricsByMetricType.add(statsdMetric);
            }
        }
        
        return statsdMetricsByMetricType;
    }
    
    public static List<StatsdMetric> getStatsdMetricsExcludeMetricTypeCode(List<StatsdMetric> statsdMetrics, Byte metricTypeCode) {
        
        if ((statsdMetrics == null) || statsdMetrics.isEmpty() || (metricTypeCode == null)) {
            return new ArrayList<>();
        }
        
        List<StatsdMetric> statsdMetricsByMetricTypeCode = new ArrayList<>(statsdMetrics.size());
        
        for (StatsdMetric statsdMetric : statsdMetrics) {
            if ((statsdMetric != null) && (statsdMetric.getMetricTypeCode() != metricTypeCode)) {
                statsdMetricsByMetricTypeCode.add(statsdMetric);
            }
        }
        
        return statsdMetricsByMetricTypeCode;
    }
    
    public static Map<Byte,List<StatsdMetric>> divideStatsdMetricsByMetricTypeCode(List<StatsdMetric> statsdMetrics) {
        
        if (statsdMetrics == null) {
            return new HashMap<>();
        }
        
        Map<Byte,List<StatsdMetric>> statsdMetricsByMetricType = new HashMap<>();

        for (StatsdMetric statsdMetric : statsdMetrics) {
            Byte metricTypeCode = statsdMetric.getMetricTypeCode();
            List<StatsdMetric> statsdMetricByMetricType = statsdMetricsByMetricType.get(metricTypeCode);
            
            if (statsdMetricByMetricType != null) {
                statsdMetricByMetricType.add(statsdMetric);
            }
            else {
                statsdMetricByMetricType = new ArrayList<>();
                statsdMetricByMetricType.add(statsdMetric);
                statsdMetricsByMetricType.put(metricTypeCode, statsdMetricByMetricType);
            }
        }
        
        return statsdMetricsByMetricType;
    }
    
    public static Map<String,List<StatsdMetric>> divideStatsdMetricsByBucket(List<StatsdMetric> statsdMetrics) {
        
        if (statsdMetrics == null) {
            return new HashMap<>();
        }
        
        Map<String,List<StatsdMetric>> statsdMetricsByBucket = new HashMap<>();

        for (StatsdMetric statsdMetric : statsdMetrics) {
            String bucket = statsdMetric.getBucket();
            List<StatsdMetric> statsdMetricByBucket = statsdMetricsByBucket.get(bucket);

            if (statsdMetricByBucket != null) {
                statsdMetricByBucket.add(statsdMetric);
            }
            else {
                statsdMetricByBucket = new ArrayList<>();
                statsdMetricByBucket.add(statsdMetric);
                statsdMetricsByBucket.put(bucket, statsdMetricByBucket);
            }
        }
        
        return statsdMetricsByBucket;
    }

    /* 
     * This method assumes that all of the input statsd metrics are already separated by buck name & by metric type.
     * The key of the input Map is the assumed to be: bucketName
     * The values of the input Map are assumed to be arraylists of StatsdMetric objects that have been pre-sorted by metric type
     */
    private static List<StatsdMetricAggregated> aggregateByBucketAndMetricType(Map<String,List<StatsdMetric>> statsdMetricByBucketAndMetricType, 
            List<StatsdHistogramConfiguration> statsdHistogramConfigurations) {
        
        if ((statsdMetricByBucketAndMetricType == null) || statsdMetricByBucketAndMetricType.isEmpty()) {
            return new ArrayList<>();
        }

        List<StatsdMetricAggregated> statsdMetricsAggregated = new ArrayList<>();
        Byte metricTypeCode = null;     
        
        BigDecimal aggregationWindowLengthInMs = new BigDecimal(ApplicationConfiguration.getFlushTimeAgg());
        
        for (String bucket : statsdMetricByBucketAndMetricType.keySet()) {
            List<StatsdMetric> statsdMetricsByBucket = statsdMetricByBucketAndMetricType.get(bucket);
            
            if ((metricTypeCode == null) && (statsdMetricsByBucket != null) && !statsdMetricsByBucket.isEmpty()) {
                metricTypeCode = statsdMetricsByBucket.get(0).getMetricTypeCode();
            }

            StatsdMetricAggregated singleStatsdMetricAggregated = null;
            List<StatsdMetricAggregated> multipleStatsdMetricsAggregated = null;
                
            if ((metricTypeCode != null) && (statsdMetricsByBucket != null) && !statsdMetricsByBucket.isEmpty()) {
                
                if (metricTypeCode == StatsdMetric.COUNTER_TYPE) {
                    multipleStatsdMetricsAggregated = aggregateCounter(statsdMetricsByBucket, 
                            aggregationWindowLengthInMs, 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString(),
                            ApplicationConfiguration.isStatsdUseLegacyNameSpacing());
                }
                else if (metricTypeCode == StatsdMetric.TIMER_TYPE) {
                    multipleStatsdMetricsAggregated = aggregateTimer(statsdMetricsByBucket, 
                            aggregationWindowLengthInMs, 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString(),
                            ApplicationConfiguration.getStatsdNthPercentiles(),
                            statsdHistogramConfigurations,
                            ApplicationConfiguration.isStatsdUseLegacyNameSpacing());
                }
                else if (metricTypeCode == StatsdMetric.GAUGE_TYPE) {
                    String prefixedBucketName = generatePrefix(StatsdMetric.GAUGE_TYPE, ApplicationConfiguration.isStatsdUseLegacyNameSpacing()) + bucket + generateSeparatorAndSuffix();
                    Map<String,Gauge> statsdGaugeCache = GlobalVariables.statsdGaugeCache;
                    Gauge gaugeFromCache = statsdGaugeCache.get(prefixedBucketName);

                    singleStatsdMetricAggregated = aggregateGauge(statsdMetricsByBucket, 
                            gaugeFromCache, 
                            ApplicationConfiguration.getGlobalAggregatedMetricsSeparatorString(),
                            ApplicationConfiguration.isStatsdUseLegacyNameSpacing());
                }
                else if (metricTypeCode == StatsdMetric.SET_TYPE) {
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
    public static List<StatsdMetricAggregated> aggregateCounter(List<StatsdMetric> statsdMetrics, BigDecimal aggregationWindowLengthInMs, 
            String aggregatedMetricsSeparator, boolean useLegacyNameSpacing) {
        
        if ((statsdMetrics == null) || statsdMetrics.isEmpty()) {
           return new ArrayList<>();
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = ".";

        BigDecimal count = BigDecimal.ZERO;
        long sumTimestamp = 0;
        int metricCounter = 0;
        
        for (StatsdMetric statsdMetric : statsdMetrics) {
            
            try {
                BigDecimal metricValue = statsdMetric.getMetricValue();

                sumTimestamp += statsdMetric.getMetricReceivedTimestampInMilliseconds();
                
                if (statsdMetric.getSampleRate() != null) {
                    BigDecimal sampleRate = statsdMetric.getSampleRate();
                    BigDecimal sampleRateMultiplier;
                    
                    if (sampleRate.compareTo(BigDecimal.ZERO) == 1) {
                        sampleRateMultiplier = BigDecimal.ONE.divide(sampleRate, MathContext.DECIMAL64);
                    }
                    else {
                        sampleRateMultiplier = BigDecimal.ONE;
                        
                        logger.warn("Invalid sample rate for counter=\"" + statsdMetrics.get(0).getBucket() 
                                + "\". Value=\"" + statsdMetric.getSampleRate() 
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
                logger.error("Invalid data for counter=\"" + statsdMetric.getBucket() 
                                + "\". Value=\"" + statsdMetric.getMetricValue() + "\"." + System.lineSeparator()
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
                bucket_Count = prefix + "stats_counts" + aggregatedMetricsSeparator + statsdMetrics.get(0).getBucket() + generateSeparatorAndSuffix();
            }
            else {
                bucket_Count = generatePrefix(StatsdMetric.COUNTER_TYPE, useLegacyNameSpacing) + 
                        statsdMetrics.get(0).getBucket() + aggregatedMetricsSeparator + "count" + generateSeparatorAndSuffix();
            }

            StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(bucket_Count, count, averagedTimestamp, StatsdMetricAggregated.COUNTER_TYPE);
            statsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
            statsdMetricsAggregated.add(statsdMetricAggregated);
            
            String bucket_Rate = useLegacyNameSpacing ? 
                    generatePrefix(StatsdMetric.COUNTER_TYPE, useLegacyNameSpacing) + statsdMetrics.get(0).getBucket() + generateSeparatorAndSuffix() : 
                    generatePrefix(StatsdMetric.COUNTER_TYPE, useLegacyNameSpacing) + statsdMetrics.get(0).getBucket() + aggregatedMetricsSeparator + "rate" + generateSeparatorAndSuffix();
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
    public static List<StatsdMetricAggregated> aggregateTimer(List<StatsdMetric> statsdMetrics, BigDecimal aggregationWindowLengthInMs, 
            String aggregatedMetricsSeparator, StatsdNthPercentiles statsdNthPercentiles, List<StatsdHistogramConfiguration> statsdHistogramConfigurations, 
            boolean useLegacyNameSpacing) {
        
        if ((statsdMetrics == null) || statsdMetrics.isEmpty() || (aggregationWindowLengthInMs == null)) {
            return new ArrayList<>(); 
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = ".";
        
        String bucket = statsdMetrics.get(0).getBucket();
        
        List<String> nthPercentageFractional_StatsdFormattedStrings = null;
        if (statsdNthPercentiles != null) nthPercentageFractional_StatsdFormattedStrings = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted();
            
        List<BigDecimal> nthPercentageFractionals = null;
        if (statsdNthPercentiles != null) nthPercentageFractionals = statsdNthPercentiles.getNthPercentiles_Fractional();
        
        List<BigDecimal> metricValues = new ArrayList<>();
        List<BigDecimal> rollingSum = new ArrayList<>();
        List<BigDecimal> rollingSumOfSquares = new ArrayList<>();

        BigDecimal count, countSampled = BigDecimal.ZERO, lower = null, upper = null, sum = BigDecimal.ZERO, sumOfSquares = BigDecimal.ZERO;
        List<BigDecimal> countNthPercentiles = null, meanNthPercentiles = null, lowerNthPercentiles = null, sumNthPercentiles = null, sumOfSquaresNthPercentiles = null, upperNthPercentiles = null;
        List<String> outputPercentageStringsNthPercentiles = null;
        
        int metricCounter = 0, metricCounter_Values = 0;
        long sumTimestamp = 0;

        for (StatsdMetric statsdMetric : statsdMetrics) {
            try {
                BigDecimal metricValue = statsdMetric.getMetricValue();
                metricValues.add(metricValue);
                sumTimestamp += statsdMetric.getMetricReceivedTimestampInMilliseconds();     
                metricCounter++;
                
                if (statsdMetric.getSampleRate() != null) {                    
                    BigDecimal sampleRate = statsdMetric.getSampleRate();
                    BigDecimal sampleRateMultiplier;

                    if (sampleRate.compareTo(BigDecimal.ZERO) == 1) {
                        sampleRateMultiplier = BigDecimal.ONE.divide(sampleRate, MathContext.DECIMAL64);
                    }
                    else {
                        sampleRateMultiplier = BigDecimal.ONE;

                        logger.warn("Invalid sample rate for counter=\"" + statsdMetrics.get(0).getBucket() 
                                + "\". Value=\"" + statsdMetric.getSampleRate() 
                                + "\". Defaulting to sample-rate of 1.0");
                    }

                    countSampled = countSampled.add((BigDecimal.ONE).multiply(sampleRateMultiplier));
                }
                else {
                    countSampled = countSampled.add(BigDecimal.ONE);
                }
            }
            catch (Exception e) {
                logger.error("Invalid data for timer=\"" + statsdMetric.getBucket()
                                + "\". Value=\"" + statsdMetric.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }  
        }
        
        Collections.sort(metricValues);
 
        StatsdHistogramConfiguration statsdHistogramConfiguration = getAppropriateStatsdHistogramConfiguration(statsdHistogramConfigurations, bucket);
        long[] statsdHistogramBinMatchCounts = null;
        if (statsdHistogramConfiguration != null) {
            statsdHistogramBinMatchCounts = new long[statsdHistogramConfiguration.getBins_GraphiteFriendlyString().size()];
            for (int i = 0; i < statsdHistogramBinMatchCounts.length; i++) statsdHistogramBinMatchCounts[i] = 0;
        }
        
        for (BigDecimal metricValue : metricValues) {            
            sum = sum.add(metricValue);
            rollingSum.add(sum);
            
            BigDecimal metricValueSquared = metricValue.multiply(metricValue);
            sumOfSquares = sumOfSquares.add(metricValueSquared);
            rollingSumOfSquares.add(sumOfSquares);
            
            if (metricCounter_Values == 0) {
                upper = metricValue;
                lower = metricValue;
            }
            else {
                if ((upper != null) && upper.compareTo(metricValue) == -1) upper = metricValue;
                if ((lower != null) && lower.compareTo(metricValue) == 1) lower = metricValue;
            }
            
            // create histograms
            if (statsdHistogramConfiguration != null) {                
                for (int i = 0; i < statsdHistogramConfiguration.getBins_GraphiteFriendlyString().size(); i++) {
                    if (statsdHistogramConfiguration.isInfDetected() && ((i+1) == statsdHistogramConfiguration.getBins_GraphiteFriendlyString().size())) {
                        statsdHistogramBinMatchCounts[i]++;
                        break;
                    }
                    
                    if ((statsdHistogramConfiguration.getBins_BigDecimal().size() - 1) >= i) {
                        int compareBinVsMetricValue = statsdHistogramConfiguration.getBins_BigDecimal().get(i).compareTo(metricValue);

                        if (compareBinVsMetricValue == 1) {
                            statsdHistogramBinMatchCounts[i]++;
                            break;
                        }
                    }
                }
            }
            
            metricCounter_Values++;
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
            upperNthPercentiles = new ArrayList<>();
            outputPercentageStringsNthPercentiles = new ArrayList<>();

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
            
            String bucketName = generatePrefix(StatsdMetric.TIMER_TYPE, useLegacyNameSpacing) + bucket;
            long averagedTimestamp = Math.round((double) sumTimestamp / (double) metricCounter);

            count = metricCounter_BigDecimal;
            BigDecimal countPs = MathUtilities.smartBigDecimalScaleChange(countSampled.multiply(ONE_THOUSAND).divide(aggregationWindowLengthInMs, STATSD_MATH_CONTEXT), STATSD_SCALE, STATSD_ROUNDING_MODE);
            sum = MathUtilities.smartBigDecimalScaleChange(sum, STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal mean = MathUtilities.smartBigDecimalScaleChange(sum.divide(count, STATSD_MATH_CONTEXT), STATSD_SCALE, STATSD_ROUNDING_MODE);
            BigDecimal median = MathUtilities.smartBigDecimalScaleChange(MathUtilities.computeMedianOfBigDecimals(metricValues, STATSD_MATH_CONTEXT, true), STATSD_SCALE, STATSD_ROUNDING_MODE);
            lower = (lower != null) ? MathUtilities.smartBigDecimalScaleChange(lower, STATSD_SCALE, STATSD_ROUNDING_MODE) : null;
            BigDecimal standardDeviation = MathUtilities.smartBigDecimalScaleChange(MathUtilities.computePopulationStandardDeviationOfBigDecimals(metricValues), STATSD_SCALE, STATSD_ROUNDING_MODE);
            sumOfSquares = MathUtilities.smartBigDecimalScaleChange(sumOfSquares, STATSD_SCALE, STATSD_ROUNDING_MODE);
            upper = (upper != null) ? MathUtilities.smartBigDecimalScaleChange(upper, STATSD_SCALE, STATSD_ROUNDING_MODE) : null;
            
            if ((statsdHistogramBinMatchCounts != null) && (statsdHistogramBinMatchCounts.length != 0) && (statsdHistogramConfiguration != null)) {
                for (int i = 0; i < statsdHistogramConfiguration.getBins_GraphiteFriendlyString().size(); i++) {
                    String histogramBucket = bucketName + aggregatedMetricsSeparator + "histogram.bin_" + statsdHistogramConfiguration.getBins_GraphiteFriendlyString().get(i) + generateSeparatorAndSuffix();
                    StatsdMetricAggregated statsdHistogramBin = new StatsdMetricAggregated(histogramBucket,
                            new BigDecimal(statsdHistogramBinMatchCounts[i]), averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
                    statsdHistogramBin.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());
                    statsdMetricsAggregated.add(statsdHistogramBin);
                }
            }

            if (countSampled != null) {
                StatsdMetricAggregated statsdCount = new StatsdMetricAggregated(bucketName + aggregatedMetricsSeparator + "count" + generateSeparatorAndSuffix(),  
                        countSampled, averagedTimestamp, StatsdMetricAggregated.TIMER_TYPE);
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
    
    private static StatsdHistogramConfiguration getAppropriateStatsdHistogramConfiguration(List<StatsdHistogramConfiguration> statsdHistogramConfigurations, String bucket) {
        
        if ((statsdHistogramConfigurations == null) || statsdHistogramConfigurations.isEmpty() || (bucket == null)) {
            return null;
        }

        for (StatsdHistogramConfiguration statsdHistogramConfiguration : statsdHistogramConfigurations) {
            String statsdHistogramConfigurationMetric = statsdHistogramConfiguration.getMetric();

            if (statsdHistogramConfigurationMetric.isEmpty() || bucket.contains(statsdHistogramConfigurationMetric)) {
                return statsdHistogramConfiguration;
            }
        }
        
        return null;
    }
    
    /* 
     * This method assumes that all of the input statsd metrics share the same bucket name
     */
    public static StatsdMetricAggregated aggregateGauge(List<StatsdMetric> statsdMetrics, Gauge gaugeInitial, 
            String aggregatedMetricsSeparator, boolean useLegacyNameSpacing) {
        
        if ((statsdMetrics == null) || statsdMetrics.isEmpty()) {
           return null; 
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = ".";

        long sumTimestamp = 0;
        int metricCounter = 0;
        BigDecimal aggregatedMetricValue;

        if (gaugeInitial == null) aggregatedMetricValue = BigDecimal.ZERO;
        else aggregatedMetricValue = gaugeInitial.getMetricValue();

        List<StatsdMetric> statsdMetricsLocal = new ArrayList<>(statsdMetrics);
        Collections.sort(statsdMetricsLocal, StatsdMetric.COMPARE_BY_HASH_KEY);

        for (StatsdMetric statsdMetric : statsdMetricsLocal) {

            try {
                BigDecimal metricValue = statsdMetric.getMetricValue();
                
                if (statsdMetric.doesContainOperator()) {
                    aggregatedMetricValue = aggregatedMetricValue.add(metricValue);
                }
                else {
                    aggregatedMetricValue = metricValue;
                }
                
                sumTimestamp += statsdMetric.getMetricReceivedTimestampInMilliseconds();
                metricCounter++;
            }
            catch (Exception e) {
                logger.error("Invalid data for gauge=\"" + statsdMetric.getBucket()
                                + "\". Value=\"" + statsdMetric.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
        }
        
        if (metricCounter > 0) {
            String bucketName = generatePrefix(StatsdMetric.GAUGE_TYPE, useLegacyNameSpacing) + statsdMetricsLocal.get(0).getBucket() + generateSeparatorAndSuffix();
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
    public static StatsdMetricAggregated aggregateSet(List<StatsdMetric> statsdMetrics, String aggregatedMetricsSeparator, boolean useLegacyNameSpacing) {
        
        if ((statsdMetrics == null) || statsdMetrics.isEmpty()) {
            return null; 
        }
        
        if (aggregatedMetricsSeparator == null) aggregatedMetricsSeparator = ".";
        
        Set<String> metricSet = new HashSet<>();
        long sumTimestamp = 0;
        int metricCounter = 0;
        
        for (StatsdMetric statsdMetric : statsdMetrics) {

            try {
                BigDecimal metricValue = statsdMetric.getMetricValue();
                String metricValueNormalized = metricValue.stripTrailingZeros().toPlainString();
                metricSet.add(metricValueNormalized);
                sumTimestamp += statsdMetric.getMetricReceivedTimestampInMilliseconds();
                metricCounter++;
            }
            catch (Exception e) {
                logger.error("Invalid data for set =\"" + statsdMetric.getBucket()
                                + "\". Value=\"" + statsdMetric.getMetricValue() + "\"." + System.lineSeparator()
                                + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        if (metricCounter > 0) {
            String bucketName = generatePrefix(StatsdMetric.SET_TYPE, useLegacyNameSpacing) + statsdMetrics.get(0).getBucket() + aggregatedMetricsSeparator + "count" + generateSeparatorAndSuffix();
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
            StringBuilder prefix = new StringBuilder();
            if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled()) prefix.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
            if (ApplicationConfiguration.isStatsdMetricNamePrefixEnabled()) prefix.append(ApplicationConfiguration.getStatsdMetricNamePrefixValue()).append(".");
            if (metricTypeKey == null) return prefix.toString();
        }
        else if ((metricTypeKey == StatsdMetric.COUNTER_TYPE) && useLegacyNameSpacing && (counterMetricLegacyPrefix_ != null)) return counterMetricLegacyPrefix_;
        else if ((metricTypeKey == StatsdMetric.COUNTER_TYPE) && !useLegacyNameSpacing && (counterMetricPrefix_ != null)) return counterMetricPrefix_;
        else if ((metricTypeKey == StatsdMetric.TIMER_TYPE) && (timerMetricPrefix_ != null)) return timerMetricPrefix_;
        else if ((metricTypeKey == StatsdMetric.GAUGE_TYPE) && (gaugeMetricPrefix_ != null)) return gaugeMetricPrefix_;
        else if ((metricTypeKey == StatsdMetric.SET_TYPE) && (setMetricPrefix_ != null)) return setMetricPrefix_;

        StringBuilder prefix = new StringBuilder();
        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled()) prefix.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        if (ApplicationConfiguration.isStatsdMetricNamePrefixEnabled() && !useLegacyNameSpacing) prefix.append(ApplicationConfiguration.getStatsdMetricNamePrefixValue()).append(".");
        if (useLegacyNameSpacing) prefix.append("stats").append(".");
        
        if (ApplicationConfiguration.isStatsdCounterMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetric.COUNTER_TYPE) && useLegacyNameSpacing) {
            counterMetricLegacyPrefix_ = prefix.toString();
        }
        else if (ApplicationConfiguration.isStatsdCounterMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetric.COUNTER_TYPE) && !useLegacyNameSpacing) {
            prefix.append(ApplicationConfiguration.getStatsdCounterMetricNamePrefixValue()).append(".");
            counterMetricPrefix_ = prefix.toString();
        }
        else if (ApplicationConfiguration.isStatsdTimerMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetric.TIMER_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdTimerMetricNamePrefixValue()).append(".");
            timerMetricPrefix_ = prefix.toString();
        } 
        else if (ApplicationConfiguration.isStatsdGaugeMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetric.GAUGE_TYPE)) {
            prefix.append(ApplicationConfiguration.getStatsdGaugeMetricNamePrefixValue()).append(".");
            gaugeMetricPrefix_ = prefix.toString();
        }
        else if (ApplicationConfiguration.isStatsdSetMetricNamePrefixEnabled() && (metricTypeKey == StatsdMetric.SET_TYPE)) {
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
