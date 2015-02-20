package com.pearson.statsagg.metric_aggregation.opentsdb;

import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricRaw;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbRaw implements GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbRaw.class.getName());
    
    private Long hashKey_ = null;
    
    private final String metricKey_;
    private final String metricTimestamp_;
    private final String metricValue_;
    private final List<OpenTsdbTag> tags_;
    private Long metricReceivedTimestampInMilliseconds_ = null;
    
    private BigDecimal metricValueBigDecimal_ = null;
    private Long metricTimestampInMilliseconds_ = null;
    
    public OpenTsdbRaw(String metricPath, String metricTimestamp, String metricValue, List<OpenTsdbTag> tags, Long metricReceivedTimestampInMilliseconds) {
        this.metricKey_ = metricPath;
        this.metricTimestamp_ = metricTimestamp;
        this.metricValue_ = metricValue;
        this.tags_ = tags;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
    public OpenTsdbRaw(String metricPath, String metricTimestamp, String metricValue, List<OpenTsdbTag> tags, Long metricTimestampInMilliseconds, Long metricReceivedTimestampInMilliseconds) {
        this.metricKey_ = metricPath;
        this.metricTimestamp_ = metricTimestamp;
        this.metricValue_ = metricValue;
        this.tags_ = tags;
        this.metricTimestampInMilliseconds_ = metricTimestampInMilliseconds;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }    
    
    
    
    
    public BigDecimal createAndGetMetricValueBigDecimal() {
        
        try {
            if (metricValueBigDecimal_ == null) {
                metricValueBigDecimal_ = new BigDecimal(metricValue_);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricValueBigDecimal_ = null;
        }
        
        return metricValueBigDecimal_;
    }

    public Long createAndGetMetricTimestamp() {
        
        try {
            if ((metricTimestampInMilliseconds_ == null)) {
                if (metricTimestamp_.length() == 10) metricTimestampInMilliseconds_ = Long.valueOf(metricTimestamp_) * 1000;
                else if (metricTimestamp_.length() == 13) metricTimestampInMilliseconds_ = Long.valueOf(metricTimestamp_);
                else logger.warn("OpenTSDB metricKey=\"" + metricKey_ + "\" has an invalid timestamp: \"" + metricTimestamp_ + "\"");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricTimestampInMilliseconds_ = null;
        }
        
        return metricTimestampInMilliseconds_;
    }
    
//    public static OpenTsdbRaw parseOpenTsdbRaw(String unparsedMetric, long metricReceivedTimestampInMilliseconds) {
//        
//        if (unparsedMetric == null) {
//            return null;
//        }
//        
//        try {
//            int metricPathIndexRange = unparsedMetric.indexOf(' ', 0);
//            String metricPath = null;
//            if (metricPathIndexRange > 0) {
//                metricPath = unparsedMetric.substring(0, metricPathIndexRange);
//            }
//
//            int metricTimestampIndexRange = unparsedMetric.indexOf(' ', metricPathIndexRange + 1);
//            String metricTimestamp = null;
//            if (metricTimestampIndexRange > 0) {
//                metricTimestamp = unparsedMetric.substring(metricPathIndexRange + 1, metricTimestampIndexRange);
//            }
//
//            int metricValueIndexRange = unparsedMetric.indexOf(' ', metricTimestampIndexRange + 1);
//            String metricValue = null;
//            if (metricValueIndexRange > 0) {
//                metricValue = unparsedMetric.substring(metricTimestampIndexRange + 1, metricValueIndexRange);
//            }
//            
//            //String metricValue = unparsedMetric.substring(metricTimestampIndexRange + 1, unparsedMetric.length());
//
//            if ((metricPath == null) || metricPath.isEmpty() || 
//                    (metricValue == null) || metricValue.isEmpty() || 
//                    (metricTimestamp == null) || metricTimestamp.isEmpty() || (metricTimestamp.length() != 10)) {
//                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
//                return null;
//            }
//            else {
//                OpenTsdbRaw openTsdbRaw = new OpenTsdbRaw(metricPath, metricValue, metricTimestamp, metricReceivedTimestampInMilliseconds); 
//                return openTsdbRaw;
//            }
//        }
//        catch (Exception e) {
//            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
//            return null;
//        }
//    }
    
    public Long getHashKey() {
        return this.hashKey_;
    }
    
    @Override
    public Long getMetricHashKey() {
        return getHashKey();
    }
    
    public void setHashKey(Long hashKey) {
        this.hashKey_ = hashKey;
    }
    
    @Override
    public String getMetricKey() {
        return metricKey_;
    }
    
    public String getMetricTimestamp() {
        return metricTimestamp_;
    }
    
    @Override
    public Long getMetricTimestampInMilliseconds() {
        return metricTimestampInMilliseconds_;
    }
    
    public String getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return createAndGetMetricValueBigDecimal();
    }

    public List<OpenTsdbTag> getTags() {
        return tags_;
    }
    
    @Override
    public Long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

    public void setMetricReceivedTimestampInMilliseconds(Long metricReceivedTimestampInMilliseconds) {
        metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }

}
