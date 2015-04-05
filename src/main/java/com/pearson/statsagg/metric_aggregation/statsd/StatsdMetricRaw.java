package com.pearson.statsagg.metric_aggregation.statsd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.pearson.statsagg.utilities.StackTrace;
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
    private final String metricValue_;
    private final String metricType_;
    private final String sampleRate_;
    private final long metricReceivedTimestampInMilliseconds_;
    private final byte metricTypeKey_;
        
    public StatsdMetricRaw(String bucket, String metricValue, String metricType, String sampleRate, long metricReceivedTimestampInMilliseconds) {
        this.bucket_ = bucket;
        this.metricValue_ = metricValue;
        this.metricType_ = metricType;
        this.sampleRate_ = sampleRate;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.metricTypeKey_ = determineMetricTypeKey(metricType);
    }
    
    private byte determineMetricTypeKey(String metricType) {
        
        if ((metricType == null) || metricType.isEmpty()) {
            return UNDEFINED_TYPE;
        }
        
        if (metricType.equals("c")) {
            return COUNTER_TYPE;
        }
        else if (metricType.equals("ms")) {
            return TIMER_TYPE;
        }
        else if (metricType.equals("g")) {
            return GAUGE_TYPE;
        }
        else if (metricType.equals("s")) {
            return SET_TYPE;
        }
        else {
            return UNDEFINED_TYPE;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(bucket_).append(":").append(metricValue_).append("|").append(metricType_);
        
        if (sampleRate_ != null) {
            stringBuilder.append("|@").append(sampleRate_);
        }
        
        stringBuilder.append(" @ ").append(metricReceivedTimestampInMilliseconds_);
        
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
            String metricValue = null;
            if (metricValueIndexRange > 0) {
                metricValue = unparsedMetric.substring(bucketIndexRange + 1, metricValueIndexRange);
            }

            int metricTypeIndexRange = unparsedMetric.indexOf('|', metricValueIndexRange + 1);
            String metricType;
            String sampleRate = null;
            if (metricTypeIndexRange > 0) {
                metricType = unparsedMetric.substring(metricValueIndexRange + 1, metricTypeIndexRange);
                sampleRate = unparsedMetric.substring(metricTypeIndexRange + 2, unparsedMetric.length());
            }
            else {
                metricType = unparsedMetric.substring(metricValueIndexRange + 1, unparsedMetric.length());
            }
            
            if ((bucketValue == null) || bucketValue.isEmpty() || 
                    (metricValue == null) || metricValue.isEmpty() || 
                    (metricType == null) || metricType.isEmpty()) {
                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                StatsdMetricRaw statsdMetricRaw = new StatsdMetricRaw(bucketValue, metricValue, metricType, sampleRate, metricReceivedTimestampInMilliseconds); 
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

    public String getMetricValue() {
        return metricValue_;
    }
    
    public String getMetricValueString() {
        return metricValue_;
    }
    
    public String getMetricType() {
        return metricType_;
    }

    public String getSampleRate() {
        return sampleRate_;
    }
    
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

    public byte getMetricTypeKey() {
        return metricTypeKey_;
    }

}
