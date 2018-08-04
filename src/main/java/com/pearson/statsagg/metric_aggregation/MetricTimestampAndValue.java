package com.pearson.statsagg.metric_aggregation;

import com.pearson.statsagg.utilities.math_utils.MathUtilities;
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

    private final long timestamp_;
    private final BigDecimal metricValue_;
    private final long metricReceivedHashKey_;
        
    private boolean useFastHashCode_ = true;
    
    public MetricTimestampAndValue(long timestamp, BigDecimal metricValue, long metricReceivedHashKey) {
        this.timestamp_ = timestamp;
        this.metricValue_ = metricValue;
        this.metricReceivedHashKey_ = metricReceivedHashKey;
    }
    
    public final static Comparator<MetricTimestampAndValue> COMPARE_BY_TIMESTAMP = new Comparator<MetricTimestampAndValue>() {
        
        @Override
        public int compare(MetricTimestampAndValue metricTimestampAndValue1, MetricTimestampAndValue metricTimestampAndValue2) {
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
        if (useFastHashCode_) {
            return new HashCodeBuilder(71, 109)
                    .append(metricReceivedHashKey_)
                    .toHashCode();
        }
        else {
            return new HashCodeBuilder(71, 109)
                    .append(timestamp_)
                    .append(metricValue_)
                    .append(metricReceivedHashKey_)
                    .append(useFastHashCode_)
                    .toHashCode();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        
        MetricTimestampAndValue metricTimestampAndValue = (MetricTimestampAndValue) obj;

        boolean areMetricValuesNumericallyEqual = MathUtilities.areBigDecimalsNumericallyEqual(metricValue_, metricTimestampAndValue.getMetricValue());

        return new EqualsBuilder()
                .append(timestamp_, metricTimestampAndValue.getTimestamp())
                .append(areMetricValuesNumericallyEqual, true)
                .append(metricReceivedHashKey_, metricTimestampAndValue.getMetricReceivedHashKey())
                .append(useFastHashCode_, metricTimestampAndValue.useFastHashCode())
                .isEquals();
    }
   
    public long getTimestamp() {
        return timestamp_;
    }

    public BigDecimal getMetricValue() {
        return metricValue_;
    }

    public long getMetricReceivedHashKey() {
        return metricReceivedHashKey_;
    }

    public boolean useFastHashCode() {
        return useFastHashCode_;
    }

    public void setUseFastHashCode(boolean useFastHashCode) {
        this.useFastHashCode_ = useFastHashCode;
    }

}
