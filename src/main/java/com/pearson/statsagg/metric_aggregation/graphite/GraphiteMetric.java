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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteMetric implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteMetric.class.getName());
    
    private long hashKey_ = -1;
    
    private final String metricPath_;
    private final BigDecimal metricValue_;
    private final long metricTimestamp_;
    private final long metricReceivedTimestampInMilliseconds_;
        
    private final boolean isMetricTimestampInSeconds_;
    
    // metricTimestamp is assumed to be in seconds
    public GraphiteMetric(String metricPath, BigDecimal metricValue, int metricTimestamp) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricReceivedTimestampInMilliseconds_ = metricTimestamp * 1000;
        
        this.isMetricTimestampInSeconds_ = true;
    }
    
    // metricTimestamp is assumed to be in seconds
    public GraphiteMetric(String metricPath, BigDecimal metricValue, int metricTimestamp, long metricReceivedTimestampInMilliseconds) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.isMetricTimestampInSeconds_ = true;
    }
    
    // metricTimestamp is assumed to be in milliseconds
    public GraphiteMetric(String metricPath, BigDecimal metricValue, long metricTimestamp, long metricReceivedTimestampInMilliseconds) {
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

    public int createAndGetMetricTimestampInSeconds() {
        if (isMetricTimestampInSeconds_) return (int) metricTimestamp_;
        else return (int) (metricTimestamp_ / 1000);
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
        
        GraphiteMetric graphiteMetric = (GraphiteMetric) obj;
        
        boolean isMetricValueEqual = false;
        if ((metricValue_ != null) && (graphiteMetric.getMetricValue() != null)) {
            isMetricValueEqual = metricValue_.compareTo(graphiteMetric.getMetricValue()) == 0;
        }
        else if (metricValue_ == null) {
            isMetricValueEqual = graphiteMetric.getMetricValue() == null;
        }
        
        return new EqualsBuilder()
                .append(metricPath_, graphiteMetric.getMetricPath())
                .append(isMetricValueEqual, true)
                .append(metricTimestamp_, graphiteMetric.getMetricTimestamp())
                .append(metricReceivedTimestampInMilliseconds_, graphiteMetric.getMetricReceivedTimestampInMilliseconds())
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
    
    // need to optimize
    public static String getGraphiteFormattedMetricPath(String metricPath) {

        if (metricPath == null) {
            return null;
        }

        String formattedGraphiteMetricPath = metricPath;
 
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "%", "Pct");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, " ", "_");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "\"", "");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "/", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "\\", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "[", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "]", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "{", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "}", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "(", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, ")", "|");
                
        while (formattedGraphiteMetricPath.contains("..")) {
            formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "..", ".");
        }
        
        return formattedGraphiteMetricPath;
    }
    
    public static GraphiteMetric parseGraphiteMetric(String unparsedMetric, String metricPrefix, long metricReceivedTimestampInMilliseconds) {
        
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
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath, metricValueBigDecimal, metricTimestamp, metricReceivedTimestampInMilliseconds); 
                return graphiteMetric;
            }
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static List<GraphiteMetric> parseGraphiteMetrics(String unparsedMetrics, long metricReceivedTimestampInMilliseconds) {
        return parseGraphiteMetrics(unparsedMetrics, null, metricReceivedTimestampInMilliseconds);
    }
    
    public static List<GraphiteMetric> parseGraphiteMetrics(String unparsedMetrics, String metricPrefix, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<GraphiteMetric> graphiteMetrics = new ArrayList();
            
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
                    GraphiteMetric graphiteMetric = GraphiteMetric.parseGraphiteMetric(unparsedMetric.trim(), metricPrefix, metricReceivedTimestampInMilliseconds);

                    if (graphiteMetric != null) {
                        graphiteMetrics.add(graphiteMetric);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return graphiteMetrics;
    }
    
    /*
    For every unique metric path, get the GraphiteMetric with the most recent 'metric timestamp'. 
    
    In the event that multiple GraphiteMetrics share the same 'metric path' and 'metric timestamp', 
    then 'metric received timestamp' is used as a tiebreaker. 
    
    In the event that multiple GraphiteMetrics also share the same 'metric received timestamp', 
    then this method will return the first GraphiteMetric that it scanned that met these criteria
    */
    public static Map<String,GraphiteMetric> getMostRecentGraphiteMetricByMetricPath(List<GraphiteMetric> graphiteMetrics) {
        
        if (graphiteMetrics == null || graphiteMetrics.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String,GraphiteMetric> mostRecentGraphiteMetricsByMetricPath = new HashMap<>();
        
        for (GraphiteMetric graphiteMetric : graphiteMetrics) {
            try {
                boolean doesAlreadyContainMetricPath = mostRecentGraphiteMetricsByMetricPath.containsKey(graphiteMetric.getMetricPath());

                if (doesAlreadyContainMetricPath) {
                    GraphiteMetric currentMostRecentGraphiteMetric = mostRecentGraphiteMetricsByMetricPath.get(graphiteMetric.getMetricPath());

                    if (graphiteMetric.getMetricTimestampInMilliseconds() > currentMostRecentGraphiteMetric.getMetricTimestampInMilliseconds()) {
                        mostRecentGraphiteMetricsByMetricPath.put(graphiteMetric.getMetricPath(), graphiteMetric);
                    }
                    else if (graphiteMetric.getMetricTimestampInMilliseconds() == currentMostRecentGraphiteMetric.getMetricTimestampInMilliseconds()) {
                        if (graphiteMetric.getMetricReceivedTimestampInMilliseconds() > currentMostRecentGraphiteMetric.getMetricReceivedTimestampInMilliseconds()) {
                            mostRecentGraphiteMetricsByMetricPath.put(graphiteMetric.getMetricPath(), graphiteMetric);
                        }
                    }
                }
                else {
                    mostRecentGraphiteMetricsByMetricPath.put(graphiteMetric.getMetricPath(), graphiteMetric);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return mostRecentGraphiteMetricsByMetricPath;
    }

    public long getHashKey() {
        return this.hashKey_;
    }
    
    @Override
    public long getMetricHashKey() {
        return getHashKey();
    }
    
    public void setHashKey(long hashKey) {
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
   
    @Override
    public int getMetricTimestampInSeconds() {
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
