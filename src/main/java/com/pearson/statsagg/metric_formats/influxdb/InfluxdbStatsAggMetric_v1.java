package com.pearson.statsagg.metric_formats.influxdb;

import com.pearson.statsagg.metric_formats.GenericMetricFormat;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetricFormat;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * 
 * This object is intended to be compatible with the InfluxDB format used in InfluxDB v0.6x, v0.7x, v0.8x
 */
public class InfluxdbStatsAggMetric_v1 implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbStatsAggMetric_v1.class.getName());

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
    
    private final ArrayList<String> columns_;
    private final ArrayList<Object> point_;
    
    public InfluxdbStatsAggMetric_v1(String metricKey, String metricDatabase, String metricPrefix, String metricPrefixPeriodDelimited, 
            String metricName, String metricValueName, BigDecimal metricValue, long metricTimestamp, byte metricTimestampPrecision, long metricReceivedTimestampInMilliseconds,
            ArrayList<String> columns, ArrayList<Object> point) {
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
        
        this.columns_ = columns;
        this.point_ = point;
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
    public String getOpenTsdbTelnetFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        if ((metricPrefixPeriodDelimited_ != null) && !metricPrefixPeriodDelimited_.isEmpty()) stringBuilder.append(metricPrefixPeriodDelimited_);
        stringBuilder.append(metricDatabase_).append(".").append(metricName_);
        if ((metricValueName_ != null) && !metricValueName_.isEmpty()) stringBuilder.append(".").append(metricValueName_);
        
        stringBuilder.append(" ").append(getMetricTimestampInMilliseconds()).append(" ").append(getMetricValueString());
        
        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbJsonFormatString() {
                        
        StringBuilder metricName = new StringBuilder();
        if ((metricPrefixPeriodDelimited_ != null) && !metricPrefixPeriodDelimited_.isEmpty()) metricName.append(metricPrefixPeriodDelimited_);
        metricName.append(metricDatabase_).append(".").append(metricName_);
        if ((metricValueName_ != null) && !metricValueName_.isEmpty()) metricName.append(".").append(metricValueName_);
        String metric = metricName.toString();

        if ((metric == null) || metric.isEmpty()) return null;
        if (getMetricTimestampInMilliseconds() < 0) return null;
        if ((getMetricValue() == null)) return null;
        
        StringBuilder openTsdbJson = new StringBuilder();

        openTsdbJson.append("{");

        openTsdbJson.append("\"metric\":\"").append(StringEscapeUtils.escapeJson(metric)).append("\",");
        openTsdbJson.append("\"timestamp\":").append(getMetricTimestampInMilliseconds()).append(",");
        openTsdbJson.append("\"value\":").append(getMetricValueString()).append(",");

        openTsdbJson.append("\"tags\":{");
        String openTsdbTags = getOpenTsdbTagsJsonFromInfluxColumnsAndPoints();
        if (openTsdbTags != null) openTsdbJson.append(openTsdbTags);
        openTsdbJson.append("}");

        openTsdbJson.append("}");
        
        return openTsdbJson.toString();
    }

    private String getOpenTsdbTagsJsonFromInfluxColumnsAndPoints() {
        
        if ((columns_ == null) || (point_ == null) || (columns_.size() != point_.size()) || point_.isEmpty()) {
            return null;
        }
        
        StringBuilder openTsdbTagsJson = new StringBuilder();
        
        for (int i = 0; i < columns_.size(); i++) {
            String column = columns_.get(i);
            Object pointColumnValue = point_.get(i);
                        
            if ((pointColumnValue != null) && (pointColumnValue instanceof String)) {
                String pointString = (String) pointColumnValue;
                
                openTsdbTagsJson.append("\"").append(StringEscapeUtils.escapeJson(column)).append("\":\"").append(StringEscapeUtils.escapeJson(pointString)).append("\"");
                
                if ((i + 1) != columns_.size()) openTsdbTagsJson.append(",");
            }
        }

        return openTsdbTagsJson.toString();
    }

    /*
    For every unique metric key, get the InfluxdbStatsAggMetric_v1 with the most recent 'metric timestamp'. 
    
    In the event that multiple InfluxdbStatsAggMetrics share the same 'metric key' and 'metric timestamp', 
    then 'metric received timestamp' is used as a tiebreaker. 
    
    In the event that multiple InfluxdbMetrics also share the same 'metric received timestamp', 
    then this method will return the first InfluxdbStatsAggMetric_v1 that it scanned that met these criteria
    */
    public static Map<String,InfluxdbStatsAggMetric_v1> getMostRecentInfluxdbStatsAggMetricByMetricKey(List<InfluxdbStatsAggMetric_v1> influxdbStatsAggMetrics) {
        
        if ((influxdbStatsAggMetrics == null) || influxdbStatsAggMetrics.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, InfluxdbStatsAggMetric_v1> mostRecentInfluxdbStatsAggMetricsByMetricKey = new HashMap<>();

        for (InfluxdbStatsAggMetric_v1 influxdbStatsAggMetric : influxdbStatsAggMetrics) {
            try {
                boolean doesAlreadyContainMetricKey = mostRecentInfluxdbStatsAggMetricsByMetricKey.containsKey(influxdbStatsAggMetric.getMetricKey());

                if (doesAlreadyContainMetricKey) {
                    InfluxdbStatsAggMetric_v1 currentMostRecentInfluxdbStatsAggMetric = mostRecentInfluxdbStatsAggMetricsByMetricKey.get(influxdbStatsAggMetric.getMetricKey());

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

    public ArrayList<String> getColumns() {
        return columns_;
    }

    public ArrayList<Object> getPoint() {
        return point_;
    }
    
}
