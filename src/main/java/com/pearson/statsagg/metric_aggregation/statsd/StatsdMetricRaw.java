package com.pearson.statsagg.metric_aggregation.statsd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public final class StatsdMetricRaw {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsdMetricRaw.class.getName());
   
    public static final byte COUNTER_TYPE = 1;
    public static final byte TIMER_TYPE = 2;
    public static final byte GAUGE_TYPE = 3;
    public static final byte SET_TYPE = 4;
    public static final byte UNDEFINED_TYPE = 5;
    
    private Long hashKey_ = null;

    private final String bucket_;
    private final BigDecimal metricValue_;
    private final byte metricTypeKey_;
    private final boolean doesContainOperator_;
    private final BigDecimal sampleRate_;
    private final long metricReceivedTimestampInMilliseconds_;
        
    public StatsdMetricRaw(String bucket, BigDecimal metricValue, String metricType, boolean doesContainOperator, BigDecimal sampleRate, long metricReceivedTimestampInMilliseconds) {
        this.bucket_ = bucket;
        this.metricValue_ = metricValue;
        this.metricTypeKey_ = determineMetricTypeKey(metricType);
        this.doesContainOperator_ = doesContainOperator;
        this.sampleRate_ = sampleRate;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
    private String createAndGetMetricValueString() {
        if (metricValue_ == null) return null;
        return metricValue_.stripTrailingZeros().toPlainString();
    }
    
    private String createAndGetSampleRateString() {
        if (sampleRate_ == null) return null;
        return sampleRate_.stripTrailingZeros().toPlainString();
    }
    
    private byte determineMetricTypeKey(String metricType) {
        
        if ((metricType == null) || metricType.isEmpty()) {
            return UNDEFINED_TYPE;
        }
        
        if (metricType.equals("c")) return COUNTER_TYPE;
        else if (metricType.equals("ms")) return TIMER_TYPE;
        else if (metricType.equals("g")) return GAUGE_TYPE;
        else if (metricType.equals("s")) return SET_TYPE;
        else return UNDEFINED_TYPE;
    }
    
    private String createAndGetMetricTypeString(byte metricType) {
        if (metricType == COUNTER_TYPE) return "c";
        else if (metricType == TIMER_TYPE) return "ms";        
        else if (metricType == GAUGE_TYPE) return "g";  
        else if (metricType == SET_TYPE) return "s";  
        else return null;
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        boolean isMetricValueGreaterThanOrEqualToZero = false;
        if (metricValue_ != null) {
            int compareResult = metricValue_.compareTo(BigDecimal.ZERO);
            if (compareResult != -1) isMetricValueGreaterThanOrEqualToZero = true; 
        }
        
        stringBuilder.append(bucket_).append(":");
        if ((metricTypeKey_ == GAUGE_TYPE) && doesContainOperator_ && isMetricValueGreaterThanOrEqualToZero) stringBuilder.append("+");
        stringBuilder.append(getMetricValueString()).append("|").append(createAndGetMetricTypeString(metricTypeKey_));
        if (sampleRate_ != null) stringBuilder.append("|@").append(getSampleRateString());
        stringBuilder.append(" @ ").append(metricReceivedTimestampInMilliseconds_);
        
        return stringBuilder.toString();
    }
    
    public String getStatsdMetricFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        boolean isMetricValueGreaterThanOrEqualToZero = false;
        if (metricValue_ != null) {
            int compareResult = metricValue_.compareTo(BigDecimal.ZERO);
            if (compareResult != -1) isMetricValueGreaterThanOrEqualToZero = true; 
        }
        
        stringBuilder.append(bucket_).append(":");
        if ((metricTypeKey_ == GAUGE_TYPE) && doesContainOperator_ && isMetricValueGreaterThanOrEqualToZero) stringBuilder.append("+");
        stringBuilder.append(getMetricValueString()).append("|").append(createAndGetMetricTypeString(metricTypeKey_));
        if (sampleRate_ != null) stringBuilder.append("|@").append(getSampleRateString());
        
        return stringBuilder.toString();
    }
    
    public static StatsdMetricRaw parseStatsdMetricRaw(String unparsedMetric) {
        long currentTimestampInMilliseconds = System.currentTimeMillis();
        return parseStatsdMetricRaw(unparsedMetric, currentTimestampInMilliseconds);
    }
    
    public static StatsdMetricRaw parseStatsdMetricRaw(String unparsedMetric, long metricReceivedTimestampInMilliseconds) {
        
        if (unparsedMetric == null) {
            return null;
        }
        
        try {
            int bucketIndexRange = unparsedMetric.indexOf(':', 0);
            String bucketValue = null;
            if (bucketIndexRange > 0) {
                bucketValue = unparsedMetric.substring(0, bucketIndexRange);
            }

            int metricValueIndexRange = unparsedMetric.indexOf('|', bucketIndexRange + 1);
            
            BigDecimal metricValue = null;
            boolean doesContainOperator = false;
            if (metricValueIndexRange > 0) {
                String metricValueString = unparsedMetric.substring(bucketIndexRange + 1, metricValueIndexRange);
                doesContainOperator = StringUtils.containsAny(metricValueString, "+-");
                metricValue = new BigDecimal(metricValueString);
            }

            int metricTypeIndexRange = unparsedMetric.indexOf('|', metricValueIndexRange + 1);
            String metricType;
            BigDecimal sampleRate = null;
            if (metricTypeIndexRange > 0) {
                metricType = unparsedMetric.substring(metricValueIndexRange + 1, metricTypeIndexRange).trim();
                String sampleRateString = unparsedMetric.substring(metricTypeIndexRange + 1, unparsedMetric.length()).trim();
                if ((sampleRateString != null) && sampleRateString.contains("@") && sampleRateString.length() > 1) sampleRate = new BigDecimal(sampleRateString.substring(1));
            }
            else {
                metricType = unparsedMetric.substring(metricValueIndexRange + 1, unparsedMetric.length()).trim();
            }
            
            if ((bucketValue == null) || bucketValue.isEmpty() || (metricValue == null) || (metricType == null) || metricType.isEmpty() || 
                    (metricType.equals("ms") && (metricValue.compareTo(BigDecimal.ZERO) == -1))) {
                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                StatsdMetricRaw statsdMetricRaw = new StatsdMetricRaw(bucketValue, metricValue, metricType, doesContainOperator, sampleRate, metricReceivedTimestampInMilliseconds); 
                return statsdMetricRaw;
            }
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static List<StatsdMetricRaw> parseStatsdMetricsRaw(String unparsedMetrics) {
        long currentTimestampInMilliseconds = System.currentTimeMillis();
        return parseStatsdMetricsRaw(unparsedMetrics, currentTimestampInMilliseconds);
    }
    
    public static List<StatsdMetricRaw> parseStatsdMetricsRaw(String unparsedMetrics, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StatsdMetricRaw> statsdMetricsRaw = new ArrayList();
        
        try {
            int currentIndex = 0;
            int newLineLocation = 0;

            while(newLineLocation != -1) {
                newLineLocation = unparsedMetrics.indexOf('\n', currentIndex);

                String unparsedMetric;

                if (newLineLocation == -1) {
                    unparsedMetric = unparsedMetrics.substring(currentIndex, unparsedMetrics.length());
                }
                else {
                    unparsedMetric = unparsedMetrics.substring(currentIndex, newLineLocation);
                    currentIndex = newLineLocation + 1;
                }

                if ((unparsedMetric != null) && !unparsedMetric.isEmpty()) {
                    StatsdMetricRaw statsdMetricRaw = StatsdMetricRaw.parseStatsdMetricRaw(unparsedMetric.trim(), metricReceivedTimestampInMilliseconds);

                    if (statsdMetricRaw != null) {
                        statsdMetricsRaw.add(statsdMetricRaw);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return statsdMetricsRaw;
    }
    
    public final static Comparator<StatsdMetricRaw> COMPARE_BY_HASH_KEY = new Comparator<StatsdMetricRaw>() {
        
        @Override
        public int compare(StatsdMetricRaw statsdMetricRaw1, StatsdMetricRaw statsdMetricRaw2) {
            if (statsdMetricRaw1.getHashKey() > statsdMetricRaw2.getHashKey()) {
                return 1;
            }
            else if (statsdMetricRaw1.getHashKey() < statsdMetricRaw2.getHashKey()) {
                return -1;
            }
            else {
                return 0;
            }
        }
        
    };

    public static Comparator<StatsdMetricRaw> COMPARE_BY_METRIC_TYPE_KEY = new Comparator<StatsdMetricRaw>() {
        
        @Override
        public int compare(StatsdMetricRaw statsdMetricRaw1, StatsdMetricRaw statsdMetricRaw2) {
            if (statsdMetricRaw1.getMetricTypeKey() > statsdMetricRaw2.getMetricTypeKey()) {
                return 1;
            }
            else if (statsdMetricRaw1.getMetricTypeKey() < statsdMetricRaw2.getMetricTypeKey()) {
                return -1;
            }
            else {
                return 0;
            }
        }
        
    };
    
    public Long getHashKey() {
        return this.hashKey_;
    }
    
    public void setHashKey(Long hashKey) {
        this.hashKey_ = hashKey;
    }
    
    public String getBucket() {
        return bucket_;
    }

    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    public String getMetricValueString() {
        return createAndGetMetricValueString();
    }
    
    public String getMetricType() {
        return createAndGetMetricTypeString(metricTypeKey_);
    }

    public byte getMetricTypeKey() {
        return metricTypeKey_;
    }
    
    public boolean doesContainOperator() {
        return doesContainOperator_;
    }
    
    public BigDecimal getSampleRate() {
        return sampleRate_;
    }
    
    public String getSampleRateString() {
        return createAndGetSampleRateString();
    }
    
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }
    
}
