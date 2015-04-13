package com.pearson.statsagg.metric_aggregation.graphite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteMetricRaw implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteMetricRaw.class.getName());
    
    private Long hashKey_ = null;
    
    private final String metricPath_;
    private final BigDecimal metricValue_;
    private final long metricTimestamp_;
    private final long metricReceivedTimestampInMilliseconds_;
        
    private final boolean isMetricTimestampInSeconds_;
    
    // metricTimestamp is assumed to be in seconds
    public GraphiteMetricRaw(String metricPath, BigDecimal metricValue, int metricTimestamp, long metricReceivedTimestampInMilliseconds) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.isMetricTimestampInSeconds_ = true;
    }
    
    // metricTimestamp is assumed to be in milliseconds
    public GraphiteMetricRaw(String metricPath, BigDecimal metricValue, long metricTimestamp, long metricReceivedTimestampInMilliseconds) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.isMetricTimestampInSeconds_ = false;
    }
    
    public String createAndGetMetricValueString() {
        if (metricValue_ == null) return null;
        return metricValue_.stripTrailingZeros().toPlainString();
    }

    public long createAndGetMetricTimestampInSeconds() {
        if (isMetricTimestampInSeconds_) return metricTimestamp_;
        else return (long) (metricTimestamp_ / 1000);
    }
    
    public long createAndGetMetricTimestampInMilliseconds() {
        if (!isMetricTimestampInSeconds_) return metricTimestamp_;
        else return (long) (metricTimestamp_ * 1000);
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 13)
                .append(metricPath_)
                .append(metricValue_)
                .append(metricTimestamp_)
                .append(metricReceivedTimestampInMilliseconds_)
                .append(isMetricTimestampInSeconds_)
                .toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        
        GraphiteMetricRaw graphiteMetricRaw = (GraphiteMetricRaw) obj;
        
        boolean isMetricValueEqual = false;
        if ((metricValue_ != null) && (graphiteMetricRaw.getMetricValue() != null)) {
            isMetricValueEqual = metricValue_.compareTo(graphiteMetricRaw.getMetricValue()) == 0;
        }
        else if (metricValue_ == null) {
            isMetricValueEqual = graphiteMetricRaw.getMetricValue() == null;
        }
        
        return new EqualsBuilder()
                .append(metricPath_, graphiteMetricRaw.getMetricPath())
                .append(isMetricValueEqual, true)
                .append(metricTimestamp_, graphiteMetricRaw.getMetricTimestamp())
                .append(metricReceivedTimestampInMilliseconds_, graphiteMetricRaw.getMetricReceivedTimestampInMilliseconds())
                .append(isMetricTimestampInSeconds_, isMetricTimestampInSeconds())
                .isEquals();
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(metricPath_).append(" ")
                .append(getMetricValueString()).append(" ")       
                .append(metricTimestamp_)
                .append(" @ ").append(metricReceivedTimestampInMilliseconds_);
        
        return stringBuilder.toString();
    }
    
    @Override
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(metricPath_).append(" ").append(getMetricValueString()).append(" ").append(getMetricTimestampInSeconds());

        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbFormatString() {
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append(metricPath_).append(" ").append(getMetricTimestampInSeconds()).append(" ").append(getMetricValueString()).append(" Format=Graphite");

        return stringBuilder.toString();
    }
    
    public static GraphiteMetricRaw parseGraphiteMetricRaw(String unparsedMetric, String metricPrefix, long metricReceivedTimestampInMilliseconds) {
        
        if (unparsedMetric == null) {
            return null;
        }
        
        try {
            int metricPathIndexRange = unparsedMetric.indexOf(' ', 0);
            String metricPath = null;
            if (metricPathIndexRange > 0) {
                if ((metricPrefix != null) && !metricPrefix.isEmpty()) metricPath = metricPrefix + unparsedMetric.substring(0, metricPathIndexRange);
                else metricPath = unparsedMetric.substring(0, metricPathIndexRange);
            }

            int metricValueIndexRange = unparsedMetric.indexOf(' ', metricPathIndexRange + 1);
            BigDecimal metricValueBigDecimal = null;
            if (metricValueIndexRange > 0) {
                String metricValue = unparsedMetric.substring(metricPathIndexRange + 1, metricValueIndexRange);
                metricValueBigDecimal = new BigDecimal(metricValue);
            }

            String metricTimestampString = unparsedMetric.substring(metricValueIndexRange + 1, unparsedMetric.length());
            int metricTimestamp = Integer.parseInt(metricTimestampString);
            
            if ((metricPath == null) || metricPath.isEmpty() || (metricValueBigDecimal == null) ||
                    (metricTimestampString == null) || metricTimestampString.isEmpty() || 
                    (metricTimestampString.length() != 10) || (metricTimestamp < 0)) {
                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                GraphiteMetricRaw graphiteMetricRaw = new GraphiteMetricRaw(metricPath, metricValueBigDecimal, metricTimestamp, metricReceivedTimestampInMilliseconds); 
                return graphiteMetricRaw;
            }
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static List<GraphiteMetricRaw> parseGraphiteMetricsRaw(String unparsedMetrics, String metricPrefix, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<GraphiteMetricRaw> graphiteMetricsRaw = new ArrayList();
            
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
                    GraphiteMetricRaw graphiteMetricRaw = GraphiteMetricRaw.parseGraphiteMetricRaw(unparsedMetric.trim(), metricPrefix, metricReceivedTimestampInMilliseconds);

                    if (graphiteMetricRaw != null) {
                        graphiteMetricsRaw.add(graphiteMetricRaw);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return graphiteMetricsRaw;
    }
    
    /*
    For every unique metric path, get the GraphiteMetricRaw with the most recent 'metric timestamp'. 
    
    In the event that multiple GraphiteMetricRaws share the same 'metric path' and 'metric timestamp', 
    then 'metric received timestamp' is used as a tiebreaker. 
    
    In the event that multiple GraphiteMetricRaws also share the same 'metric received timestamp', 
    then this method will return the first GraphiteMetricRaw that it scanned that met these criteria
    */
    public static Map<String,GraphiteMetricRaw> getMostRecentGraphiteMetricRawByMetricPath(List<GraphiteMetricRaw> graphiteMetricsRaw) {
        
        if (graphiteMetricsRaw == null || graphiteMetricsRaw.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String,GraphiteMetricRaw> mostRecentGraphiteMetricsByMetricPath = new HashMap<>();
        
        for (GraphiteMetricRaw graphiteMetricRaw : graphiteMetricsRaw) {
            try {
                boolean doesAlreadyContainMetricPath = mostRecentGraphiteMetricsByMetricPath.containsKey(graphiteMetricRaw.getMetricPath());

                if (doesAlreadyContainMetricPath) {
                    GraphiteMetricRaw currentMostRecentGraphiteMetricRaw = mostRecentGraphiteMetricsByMetricPath.get(graphiteMetricRaw.getMetricPath());

                    if (graphiteMetricRaw.getMetricTimestampInMilliseconds() > currentMostRecentGraphiteMetricRaw.getMetricTimestampInMilliseconds()) {
                        mostRecentGraphiteMetricsByMetricPath.put(graphiteMetricRaw.getMetricPath(), graphiteMetricRaw);
                    }
                    else if (graphiteMetricRaw.getMetricTimestampInMilliseconds() == currentMostRecentGraphiteMetricRaw.getMetricTimestampInMilliseconds()) {
                        if (graphiteMetricRaw.getMetricReceivedTimestampInMilliseconds() > currentMostRecentGraphiteMetricRaw.getMetricReceivedTimestampInMilliseconds()) {
                            mostRecentGraphiteMetricsByMetricPath.put(graphiteMetricRaw.getMetricPath(), graphiteMetricRaw);
                        }
                    }
                }
                else {
                    mostRecentGraphiteMetricsByMetricPath.put(graphiteMetricRaw.getMetricPath(), graphiteMetricRaw);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return mostRecentGraphiteMetricsByMetricPath;
    }

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

    public String getMetricPath() {
        return metricPath_;
    }
    
    @Override
    public String getMetricKey() {
        return getMetricPath();
    }
    
    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return metricValue_;
    }
    
    @Override
    public String getMetricValueString() {
        return createAndGetMetricValueString();
    }
    
    public long getMetricTimestamp() {
        return metricTimestamp_;
    }
   
    public long getMetricTimestampInSeconds() {
        return createAndGetMetricTimestampInSeconds();
    }
    
    @Override
    public long getMetricTimestampInMilliseconds() {
        return createAndGetMetricTimestampInMilliseconds();
    }
    
    @Override
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

    public boolean isMetricTimestampInSeconds() {
        return isMetricTimestampInSeconds_;
    }

}
