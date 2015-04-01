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
    private final String metricValue_;
    private final String metricTimestamp_;
    private Integer metricTimestampInt_ = null;
    private Long metricReceivedTimestampInMilliseconds_ = null;
    
    private BigDecimal metricValueBigDecimal_ = null;
    private Long metricTimestampInMilliseconds_ = null;

    public GraphiteMetricRaw(String metricPath, String metricValue, String metricTimestamp, Long metricReceivedTimestampInMilliseconds) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
    public GraphiteMetricRaw(String metricPath, String metricValue, String metricTimestamp, Integer metricTimestampInt, Long metricReceivedTimestampInMilliseconds) {
        this.metricPath_ = metricPath;
        this.metricValue_ = metricValue;
        this.metricTimestamp_ = metricTimestamp;
        this.metricTimestampInt_ = metricTimestampInt;
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

    public Integer createAndGetMetricTimestampInt() {
        
        try {
            if (metricTimestampInt_ == null) {
                metricTimestampInt_ = Integer.valueOf(metricTimestamp_);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricTimestampInt_ = null;
        }
        
        return metricTimestampInt_;
    }
    
    public Long createAndGetMetricTimestampInMilliseconds() {
        
        try {
            if (metricTimestampInMilliseconds_ == null) {
                createAndGetMetricTimestampInt();
                
                if (metricTimestampInt_ != null) {
                    metricTimestampInMilliseconds_ = (long) metricTimestampInt_ * 1000;
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricTimestampInMilliseconds_ = null;
        }
        
        return metricTimestampInMilliseconds_;
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metricPath_).append(" ")
                .append(metricValue_).append(" ")       
                .append(metricTimestamp_)
                .append(" @ ").append(metricReceivedTimestampInMilliseconds_);
        
        return stringBuilder.toString();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 59)
                .append(metricPath_)
                .append(metricValue_)
                .append(metricTimestamp_)
                .append(metricReceivedTimestampInMilliseconds_)
                .toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        
        GraphiteMetricRaw graphiteMetricRaw = (GraphiteMetricRaw) obj;
        
        return new EqualsBuilder()
                .append(metricPath_, graphiteMetricRaw.getMetricPath())
                .append(metricValue_, graphiteMetricRaw.getMetricValue())
                .append(metricTimestamp_, graphiteMetricRaw.getMetricTimestamp())
                .append(metricReceivedTimestampInMilliseconds_, graphiteMetricRaw.getMetricReceivedTimestampInMilliseconds())
                .isEquals();
    }
    
    @Override
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metricPath_).append(" ").append(metricValue_).append(" ").append(metricTimestamp_);

        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbFormatString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metricPath_).append(" ").append(metricTimestamp_).append(" ").append(metricValue_).append(" Format=Graphite");

        return stringBuilder.toString();
    }
    
    public static GraphiteMetricRaw parseGraphiteMetricRaw(String unparsedMetric, long metricReceivedTimestampInMilliseconds) {
        
        if (unparsedMetric == null) {
            return null;
        }
        
        try {
            int metricPathIndexRange = unparsedMetric.indexOf(' ', 0);
            String metricPath = null;
            if (metricPathIndexRange > 0) {
                metricPath = unparsedMetric.substring(0, metricPathIndexRange);
            }

            int metricValueIndexRange = unparsedMetric.indexOf(' ', metricPathIndexRange + 1);
            String metricValue = null;
            if (metricValueIndexRange > 0) {
                metricValue = unparsedMetric.substring(metricPathIndexRange + 1, metricValueIndexRange);
            }

            String metricTimestamp = unparsedMetric.substring(metricValueIndexRange + 1, unparsedMetric.length());

            if ((metricPath == null) || metricPath.isEmpty() || 
                    (metricValue == null) || metricValue.isEmpty() || 
                    (metricTimestamp == null) || metricTimestamp.isEmpty() || (metricTimestamp.length() != 10)) {
                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                GraphiteMetricRaw graphiteMetricRaw = new GraphiteMetricRaw(metricPath, metricValue, metricTimestamp, metricReceivedTimestampInMilliseconds); 
                return graphiteMetricRaw;
            }
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static List<GraphiteMetricRaw> parseGraphiteMetricsRaw(String unparsedMetrics, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<GraphiteMetricRaw> graphiteMetricsRaw = new ArrayList();
            
        try {
            int currentIndex = 0;
            int newLineLocation = 0;

            while(newLineLocation != -1) {
                newLineLocation = unparsedMetrics.indexOf('\n', currentIndex);

                String unparsedMetric = null;

                if (newLineLocation == -1) {
                    unparsedMetric = unparsedMetrics.substring(currentIndex, unparsedMetrics.length());
                }
                else {
                    unparsedMetric = unparsedMetrics.substring(currentIndex, newLineLocation);
                    currentIndex = newLineLocation + 1;
                }

                if ((unparsedMetric != null) && !unparsedMetric.isEmpty()) {
                    GraphiteMetricRaw graphiteMetricRaw = GraphiteMetricRaw.parseGraphiteMetricRaw(unparsedMetric.trim(), metricReceivedTimestampInMilliseconds);

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

                    if (graphiteMetricRaw.getMetricTimestampInt() > currentMostRecentGraphiteMetricRaw.getMetricTimestampInt()) {
                        mostRecentGraphiteMetricsByMetricPath.put(graphiteMetricRaw.getMetricPath(), graphiteMetricRaw);
                    }
                    else if (graphiteMetricRaw.getMetricTimestampInt().intValue() == currentMostRecentGraphiteMetricRaw.getMetricTimestampInt()) {
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
    
    public static List<GraphiteMetricRaw> createPrefixedGraphiteMetricsRaw(List<GraphiteMetricRaw> graphiteMetricsRaw, 
            boolean useGlobalPrefix, String globalPrefixValue,
            boolean useGraphitePrefix, String graphitePrefixValue) {
        
        if (graphiteMetricsRaw == null || graphiteMetricsRaw.isEmpty()) {
            return new ArrayList<>();
        }
        
        int prefixedGraphiteMetricsRawListInitialSize = (int) (graphiteMetricsRaw.size() * 1.3);
        List<GraphiteMetricRaw> prefixedGraphiteMetricsRaw = new ArrayList<>(prefixedGraphiteMetricsRawListInitialSize);
        
        for (GraphiteMetricRaw graphiteMetricRaw : graphiteMetricsRaw) {
            String prefixedMetricPath = createPrefixedGraphiteMetricPath(graphiteMetricRaw, useGlobalPrefix, globalPrefixValue, 
                    useGraphitePrefix, graphitePrefixValue);
            
            GraphiteMetricRaw prefixedGraphiteMetricRaw = new GraphiteMetricRaw(prefixedMetricPath, graphiteMetricRaw.getMetricValue(), 
                    graphiteMetricRaw.getMetricTimestamp(), graphiteMetricRaw.getMetricTimestampInt(), graphiteMetricRaw.getMetricReceivedTimestampInMilliseconds());
            prefixedGraphiteMetricRaw.setHashKey(graphiteMetricRaw.getHashKey());
            
            prefixedGraphiteMetricsRaw.add(prefixedGraphiteMetricRaw);
        }
        
        return prefixedGraphiteMetricsRaw;
    }
    
    public static String createPrefixedGraphiteMetricPath(GraphiteMetricRaw graphiteMetricRaw, 
            boolean useGlobalPrefix, String globalPrefixValue,
            boolean useGraphitePrefix, String graphitePrefixValue) {
        
        if (graphiteMetricRaw == null) {
            return null;
        }
        
        if (!useGlobalPrefix && !useGraphitePrefix) {
            return graphiteMetricRaw.metricPath_;
        }
        
        StringBuilder prefix = new StringBuilder(graphiteMetricRaw.getMetricPath().length() + 50);
        
        if (useGlobalPrefix) prefix.append(globalPrefixValue).append(".");
        if (useGraphitePrefix) prefix.append(graphitePrefixValue).append(".");
        
        prefix.append(graphiteMetricRaw.getMetricPath());
        
        return prefix.toString();
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
    
    public String getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return createAndGetMetricValueBigDecimal();
    }
    
    public String getMetricTimestamp() {
        return metricTimestamp_;
    }

    public Integer getMetricTimestampInt() {
        return createAndGetMetricTimestampInt();
    }

    @Override
    public Long getMetricTimestampInMilliseconds() {
        return createAndGetMetricTimestampInMilliseconds();
    }
    
    @Override
    public Long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

    public void setMetricReceivedTimestampInMilliseconds(Long metricReceivedTimestampInMilliseconds) {
        metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
    }
    
}
