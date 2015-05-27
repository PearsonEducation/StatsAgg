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
public final class StatsdMetric {
    
    private static final Logger logger = LoggerFactory.getLogger(StatsdMetric.class.getName());
   
    public static final byte COUNTER_TYPE = 1;
    public static final byte TIMER_TYPE = 2;
    public static final byte GAUGE_TYPE = 3;
    public static final byte SET_TYPE = 4;
    public static final byte UNDEFINED_TYPE = 5;
    
    private long hashKey_ = -1;

    private final String bucket_;
    private final BigDecimal metricValue_;
    private final byte metricTypeKey_;
    private final boolean doesContainOperator_;
    private final BigDecimal sampleRate_;
    private final long metricReceivedTimestampInMilliseconds_;
        
    public StatsdMetric(String bucket, BigDecimal metricValue, String metricType, boolean doesContainOperator, BigDecimal sampleRate, long metricReceivedTimestampInMilliseconds) {
        this.bucket_ = bucket;
        this.metricValue_ = metricValue;
        this.metricTypeKey_ = determineMetricTypeKey(metricType);
        this.doesContainOperator_ = doesContainOperator;
        this.sampleRate_ = sampleRate;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
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
    
    private String getMetricTypeString(byte metricType) {
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
        stringBuilder.append(getMetricValueString()).append("|").append(getMetricTypeString(metricTypeKey_));
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
        stringBuilder.append(getMetricValueString()).append("|").append(getMetricTypeString(metricTypeKey_));
        if (sampleRate_ != null) stringBuilder.append("|@").append(getSampleRateString());
        
        return stringBuilder.toString();
    }
    
    public static StatsdMetric parseStatsdMetric(String unparsedMetric) {
        long currentTimestampInMilliseconds = System.currentTimeMillis();
        return StatsdMetric.parseStatsdMetric(unparsedMetric, currentTimestampInMilliseconds);
    }
    
    public static StatsdMetric parseStatsdMetric(String unparsedMetric, long metricReceivedTimestampInMilliseconds) {
        
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
                StatsdMetric statsdMetric = new StatsdMetric(bucketValue, metricValue, metricType, doesContainOperator, sampleRate, metricReceivedTimestampInMilliseconds); 
                return statsdMetric;
            }
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static List<StatsdMetric> parseStatsdMetrics(String unparsedMetrics) {
        long currentTimestampInMilliseconds = System.currentTimeMillis();
        return parseStatsdMetrics(unparsedMetrics, currentTimestampInMilliseconds);
    }
    
    public static List<StatsdMetric> parseStatsdMetrics(String unparsedMetrics, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<StatsdMetric> statsdMetrics = new ArrayList();
        
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
                    StatsdMetric statsdMetric = StatsdMetric.parseStatsdMetric(unparsedMetric.trim(), metricReceivedTimestampInMilliseconds);

                    if (statsdMetric != null) {
                        statsdMetrics.add(statsdMetric);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return statsdMetrics;
    }
    
    public final static Comparator<StatsdMetric> COMPARE_BY_HASH_KEY = new Comparator<StatsdMetric>() {
        
        @Override
        public int compare(StatsdMetric statsdMetric1, StatsdMetric statsdMetric2) {
            if (statsdMetric1.getHashKey() > statsdMetric2.getHashKey()) {
                return 1;
            }
            else if (statsdMetric1.getHashKey() < statsdMetric2.getHashKey()) {
                return -1;
            }
            else {
                return 0;
            }
        }
        
    };

    public static Comparator<StatsdMetric> COMPARE_BY_METRIC_TYPE_KEY = new Comparator<StatsdMetric>() {
        
        @Override
        public int compare(StatsdMetric statsdMetric1, StatsdMetric statsdMetric2) {
            if (statsdMetric1.getMetricTypeKey() > statsdMetric2.getMetricTypeKey()) {
                return 1;
            }
            else if (statsdMetric1.getMetricTypeKey() < statsdMetric2.getMetricTypeKey()) {
                return -1;
            }
            else {
                return 0;
            }
        }
        
    };
    
    public long getHashKey() {
        return this.hashKey_;
    }
    
    public void setHashKey(long hashKey) {
        this.hashKey_ = hashKey;
    }
    
    public String getBucket() {
        return bucket_;
    }

    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    public String getMetricValueString() {
        if (metricValue_ == null) return null;
        return metricValue_.stripTrailingZeros().toPlainString();
    }
    
    public String getMetricType() {
        return getMetricTypeString(metricTypeKey_);
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
        if (sampleRate_ == null) return null;
        return sampleRate_.stripTrailingZeros().toPlainString();
    }
    
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }
    
}
