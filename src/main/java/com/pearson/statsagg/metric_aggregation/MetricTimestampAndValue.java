package com.pearson.statsagg.metric_aggregation;

import java.math.BigDecimal;
import java.util.Comparator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricTimestampAndValue {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricTimestampAndValue.class.getName());

    private final Long timestamp_;
    private final BigDecimal metricValue_;
    private final Long metricReceivedHashKey_;
    
    public MetricTimestampAndValue(Long timestamp, BigDecimal metricValue, Long metricReceivedHashKey) {
        this.timestamp_ = timestamp;
        this.metricValue_ = metricValue;
        this.metricReceivedHashKey_ = metricReceivedHashKey;
    }

    public final static Comparator<MetricTimestampAndValue> COMPARE_BY_TIMESTAMP = new Comparator<MetricTimestampAndValue>() {
        
        @Override
        public int compare(MetricTimestampAndValue metricTimestampAndValue1, MetricTimestampAndValue metricTimestampAndValue2) {
            if ((metricTimestampAndValue1.getTimestamp() == null) && (metricTimestampAndValue2.getTimestamp() != null)) return -1;
            else if ((metricTimestampAndValue1.getTimestamp() != null) && (metricTimestampAndValue2.getTimestamp() == null)) return 1;
            else if ((metricTimestampAndValue1.getTimestamp() == null) && (metricTimestampAndValue2.getTimestamp() == null)) return 0;
            
            if (metricTimestampAndValue1.getTimestamp() > metricTimestampAndValue2.getTimestamp()) {
                return 1;
            }
            else if (metricTimestampAndValue1.getTimestamp() < metricTimestampAndValue2.getTimestamp()) {
                return -1;
            }
            else {
                return 0;
            }
        }
        
    };
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 109)
                .append(timestamp_)
                .append(metricValue_)
                .append(metricReceivedHashKey_)
                .toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        
        MetricTimestampAndValue metricTimestampAndValue = (MetricTimestampAndValue) obj;

        boolean isMetricValueEqual = false;
        if ((metricValue_ != null) && (metricTimestampAndValue.getMetricValue() != null)) {
            isMetricValueEqual = metricValue_.compareTo(metricTimestampAndValue.getMetricValue()) == 0;
        }
        else if (metricValue_ == null) {
            isMetricValueEqual = metricTimestampAndValue.getMetricValue() == null;
        }
        
        return new EqualsBuilder()
                .append(timestamp_, metricTimestampAndValue.getTimestamp())
                .append(isMetricValueEqual, true)
                .append(metricReceivedHashKey_, metricTimestampAndValue.getMetricReceivedHashKey())
                .isEquals();
    }
   
    public Long getTimestamp() {
        return timestamp_;
    }

    public BigDecimal getMetricValue() {
        return metricValue_;
    }

    public Long getMetricReceivedHashKey() {
        return metricReceivedHashKey_;
    }
    
}
