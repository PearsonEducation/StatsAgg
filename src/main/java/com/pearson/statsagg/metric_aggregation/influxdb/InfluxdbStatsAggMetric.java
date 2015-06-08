package com.pearson.statsagg.metric_aggregation.influxdb;

import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbStatsAggMetric implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbStatsAggMetric.class.getName());

    private long hashKey_ = -1;
    
    private final String metricKey_;
    private final String metricDatabase_;
    private final String metricPrefix_;
    private final String metricPrefixPeriodDelimited_;
    private final String metricName_;
    private final String metricValueName_;
    private final BigDecimal metricValue_;
    private final long metricTimestamp_;
    private final byte metricTimestampPrecision_;
    private final long metricReceivedTimestampInMilliseconds_;
    
    public InfluxdbStatsAggMetric(String metricKey, String metricDatabase, String metricPrefix, String metricPrefixPeriodDelimited, 
            String metricName, String metricValueName, BigDecimal metricValue, long metricTimestamp, byte metricTimestampPrecision, long metricReceivedTimestampInMilliseconds) {
        this.metricKey_ = metricKey;
        this.metricDatabase_ = metricDatabase;
        this.metricPrefix_ = metricPrefix;
        this.metricPrefixPeriodDelimited_ = metricPrefixPeriodDelimited;
        this.metricName_ = metricName;
        this.metricValueName_ = metricValueName;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricTimestampPrecision_ = metricTimestampPrecision;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
    @Override
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder();

        if ((metricPrefixPeriodDelimited_ != null) && !metricPrefixPeriodDelimited_.isEmpty()) stringBuilder.append(metricPrefixPeriodDelimited_);
        stringBuilder.append(metricDatabase_).append(".").append(metricName_);
        if ((metricValueName_ != null) && !metricValueName_.isEmpty()) stringBuilder.append(".").append(metricValueName_);

        stringBuilder.append(" ").append(getMetricValueString()).append(" ").append(getMetricTimestampInSeconds());

        return stringBuilder.toString();
    }

    @Override
    public String getOpenTsdbFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        if ((metricPrefixPeriodDelimited_ != null) && !metricPrefixPeriodDelimited_.isEmpty()) stringBuilder.append(metricPrefixPeriodDelimited_);
        stringBuilder.append(metricDatabase_).append(".").append(metricName_);
        if ((metricValueName_ != null) && !metricValueName_.isEmpty()) stringBuilder.append(".").append(metricValueName_);
        
        stringBuilder.append(" ").append(getMetricTimestampInMilliseconds()).append(" ").append(getMetricValueString());
        
        return stringBuilder.toString();
    }
    
    /*
    For every unique metric key, get the InfluxdbStatsAggMetric with the most recent 'metric timestamp'. 
    
    In the event that multiple InfluxdbStatsAggMetrics share the same 'metric key' and 'metric timestamp', 
    then 'metric received timestamp' is used as a tiebreaker. 
    
    In the event that multiple InfluxdbMetrics also share the same 'metric received timestamp', 
    then this method will return the first InfluxdbStatsAggMetric that it scanned that met these criteria
    */
    public static Map<String,InfluxdbStatsAggMetric> getMostRecentInfluxdbStatsAggMetricByMetricKey(List<InfluxdbStatsAggMetric> influxdbStatsAggMetrics) {
        
        if ((influxdbStatsAggMetrics == null) || influxdbStatsAggMetrics.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, InfluxdbStatsAggMetric> mostRecentInfluxdbStatsAggMetricsByMetricKey = new HashMap<>();

        for (InfluxdbStatsAggMetric influxdbStatsAggMetric : influxdbStatsAggMetrics) {
            try {
                boolean doesAlreadyContainMetricKey = mostRecentInfluxdbStatsAggMetricsByMetricKey.containsKey(influxdbStatsAggMetric.getMetricKey());

                if (doesAlreadyContainMetricKey) {
                    InfluxdbStatsAggMetric currentMostRecentInfluxdbStatsAggMetric = mostRecentInfluxdbStatsAggMetricsByMetricKey.get(influxdbStatsAggMetric.getMetricKey());

                    if (influxdbStatsAggMetric.getMetricTimestampInMilliseconds() > currentMostRecentInfluxdbStatsAggMetric.getMetricTimestampInMilliseconds()) {
                        mostRecentInfluxdbStatsAggMetricsByMetricKey.put(influxdbStatsAggMetric.getMetricKey(), influxdbStatsAggMetric);
                    }
                    else if (influxdbStatsAggMetric.getMetricTimestampInMilliseconds() == currentMostRecentInfluxdbStatsAggMetric.getMetricTimestampInMilliseconds()) {
                        if (influxdbStatsAggMetric.getMetricReceivedTimestampInMilliseconds() > currentMostRecentInfluxdbStatsAggMetric.getMetricReceivedTimestampInMilliseconds()) {
                            mostRecentInfluxdbStatsAggMetricsByMetricKey.put(influxdbStatsAggMetric.getMetricKey(), influxdbStatsAggMetric);
                        }
                    }
                }
                else {
                    mostRecentInfluxdbStatsAggMetricsByMetricKey.put(influxdbStatsAggMetric.getMetricKey(), influxdbStatsAggMetric);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return mostRecentInfluxdbStatsAggMetricsByMetricKey;
    }
    
    public long getHashKey() {
        return hashKey_;
    }
    
    @Override
    public long getMetricHashKey() {
        return getHashKey();
    }
    
    public void setHashKey(long hashKey) {
        this.hashKey_ = hashKey;
    }

    @Override
    public String getMetricKey() {
        return metricKey_;
    }

    public String getMetricDatabase() {
        return metricDatabase_;
    }

    public String getMetricPrefix() {
        return metricPrefix_;
    }

    public String getMetricPrefixPeriodDelimited() {
        return metricPrefixPeriodDelimited_;
    }

    public String getMetricName() {
        return metricName_;
    }

    public String getMetricValueName() {
        return metricValueName_;
    }

    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public String getMetricValueString() {
        if (metricValue_ == null) return null;
        return metricValue_.stripTrailingZeros().toPlainString();
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return metricValue_;
    }
    
    public long getMetricTimestamp() {
        return metricTimestamp_;
    }

    public byte getMetricTimestampPrecision() {
        return metricTimestampPrecision_;
    }
    
    @Override
    public int getMetricTimestampInSeconds() {
        return InfluxdbMetric_v1.getMetricTimestampInSeconds(metricTimestampPrecision_, metricTimestamp_);
    }
    
    @Override
    public long getMetricTimestampInMilliseconds() {
        return InfluxdbMetric_v1.getMetricTimestampInMilliseconds(metricTimestampPrecision_, metricTimestamp_);
    }

    @Override
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }
    
}
