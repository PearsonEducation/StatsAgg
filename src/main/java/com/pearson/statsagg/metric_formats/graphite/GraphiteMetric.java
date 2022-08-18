package com.pearson.statsagg.metric_formats.graphite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.metric_formats.GenericMetricFormat;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetricFormat_v1;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteMetric implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat, InfluxdbMetricFormat_v1 {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteMetric.class.getName());

    private static boolean logMetricFormatErrors_ = true;

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
        this.metricReceivedTimestampInMilliseconds_ = ((long) metricTimestamp) * 1000;
        
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
    public String getGraphiteFormatString(boolean sanitizeMetric, boolean substituteCharacters) {
        StringBuilder stringBuilder = new StringBuilder();
        
        String metricPath = GraphiteMetric.getGraphiteSanitizedString(metricPath_, sanitizeMetric, substituteCharacters);
        
        stringBuilder.append(metricPath).append(" ").append(getMetricValueString()).append(" ").append(getMetricTimestampInSeconds());

        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetric) {
        return getOpenTsdbTelnetFormatString(sanitizeMetric, null, null);
    }
    
    @Override
    public String getOpenTsdbTelnetFormatString(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue) {
        StringBuilder stringBuilder = new StringBuilder();
        
        String metric = sanitizeMetric ? OpenTsdbMetric.getOpenTsdbSanitizedString(metricPath_) : metricPath_;
        
        String tag = (defaultOpenTsdbTagKey == null) ? "Format=Graphite" : (defaultOpenTsdbTagKey + "=" + defaultOpenTsdbTagValue);
        
        stringBuilder.append(metric).append(" ").append(getMetricTimestampInSeconds()).append(" ").append(getMetricValueString()).append(" ").append(tag);

        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric) {
        return getOpenTsdbJsonFormatString(sanitizeMetric, null, null);
    }
    
    @Override
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue) {
                
        if ((metricPath_ == null) || metricPath_.isEmpty()) return null;
        if (getMetricTimestampInSeconds() < 0) return null;
        if ((getMetricValue() == null)) return null;
        
        StringBuilder openTsdbJson = new StringBuilder();

        openTsdbJson.append("{");

        if (sanitizeMetric) openTsdbJson.append("\"metric\":\"").append(StringEscapeUtils.escapeJson(OpenTsdbMetric.getOpenTsdbSanitizedString(metricPath_))).append("\",");
        else openTsdbJson.append("\"metric\":\"").append(StringEscapeUtils.escapeJson(metricPath_)).append("\",");
        
        openTsdbJson.append("\"timestamp\":").append(getMetricTimestampInSeconds()).append(",");
        openTsdbJson.append("\"value\":").append(getMetricValueString()).append(",");

        openTsdbJson.append("\"tags\":{");
        if ((defaultOpenTsdbTagKey == null) || (defaultOpenTsdbTagValue == null)) openTsdbJson.append("\"Format\":\"Graphite\"");
        else openTsdbJson.append("\"").append(defaultOpenTsdbTagKey).append("\":\"").append(defaultOpenTsdbTagValue).append("\"");
        openTsdbJson.append("}");

        openTsdbJson.append("}");
                
        return openTsdbJson.toString();
    }
    
    @Override
    public String getInfluxdbV1JsonFormatString() {

        if ((metricPath_ == null) || metricPath_.isEmpty()) return null;
        if (metricTimestamp_ < 0) return null;
        if ((getMetricValue() == null)) return null;

        StringBuilder influxdbJson = new StringBuilder();

        influxdbJson.append("{");

        // the metric name, with the prefix already built-in
        influxdbJson.append("\"name\":\"").append(StringEscapeUtils.escapeJson(metricPath_)).append("\",");

        // column order: value, time, tag(s)
        influxdbJson.append("\"columns\":[\"value\",\"time\"],");

        // only include one point in the points array. note-- timestamp will always be sent to influxdb in milliseconds
        influxdbJson.append("\"points\":[[");
        influxdbJson.append(getMetricValueString()).append(",");
        influxdbJson.append(getMetricTimestampInMilliseconds());
                    
        influxdbJson.append("]]}");

        return influxdbJson.toString();
    }
    
    /*
    @param  unsanitizedInput  The input is expected to be a Graphite 'metric path'.
    
    @param  sanitizeMetric  When set to true, all input will have back-to-back '.' characters merged into a single '.'.
    Example: "lol...lol" -> "lol.lol"
    
    @param  'substituteCharacters'  When set to true: a few special characters will be turned into characters that Graphite can handle. 
    % -> Pct
    (){}[]/\ -> |
    */
    public static String getGraphiteSanitizedString(String unsanitizedInput, boolean sanitizeMetric, boolean substituteCharacters) {

        if (unsanitizedInput == null) return null;
        if (!sanitizeMetric && !substituteCharacters) return unsanitizedInput;
        
        StringBuilder sanitizedInput = new StringBuilder();

        for (int i = 0; i < unsanitizedInput.length(); i++) {
            char character = unsanitizedInput.charAt(i);

            if (substituteCharacters && Character.isLetterOrDigit(character)) {
                sanitizedInput.append(character);
                continue;
            }

            if (sanitizeMetric && (character == '.')) {
                int iPlusOne = i + 1;
                
                if (((iPlusOne < unsanitizedInput.length()) && (unsanitizedInput.charAt(iPlusOne) != '.')) || (iPlusOne == unsanitizedInput.length())) {
                    sanitizedInput.append(character);
                    continue;
                }
            }

            if (substituteCharacters) {
                if (character == '%') {
                    sanitizedInput.append("Pct");
                    continue;
                }
                
                if (character == ' ') {
                    sanitizedInput.append("_");
                    continue;
                }
                
                if ((character == '\\') || (character == '/') || 
                        (character == '[') || (character == ']') || 
                        (character == '{') || (character == '}') ||
                        (character == '(') || (character == ')')) {
                    sanitizedInput.append("|");
                    continue;
                }

            }
            
            if (sanitizeMetric && (character != '.')) sanitizedInput.append(character);
            else if (!sanitizeMetric) sanitizedInput.append(character);
        }
        
        return sanitizedInput.toString();
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
                String metricValueString = unparsedMetric.substring(metricPathIndexRange + 1, metricValueIndexRange);
                
                if ((metricValueString.length() > 100) && logMetricFormatErrors_) {
                    logger.warn("Metric parse error. Metric value can't be more than 100 characters long. Metric value was \"" + metricValueString.length() + "\" characters long.");
                }
                else {
                    metricValueBigDecimal = new BigDecimal(metricValueString);
                }
            }

            String metricTimestampString = unparsedMetric.substring(metricValueIndexRange + 1, unparsedMetric.length());
            int metricTimestamp = Integer.parseInt(metricTimestampString);
            
            if ((metricPath == null) || metricPath.isEmpty() || (metricValueBigDecimal == null) ||
                    (metricTimestampString == null) || metricTimestampString.isEmpty() || 
                    (metricTimestampString.length() != 10) || (metricTimestamp < 0)) {
                if (logMetricFormatErrors_) logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                GraphiteMetric graphiteMetric = new GraphiteMetric(metricPath, metricValueBigDecimal, metricTimestamp, metricReceivedTimestampInMilliseconds); 
                return graphiteMetric;
            }
        }
        catch (NumberFormatException e) {
            if (logMetricFormatErrors_) logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator());  
            return null;
        }
        catch (Exception e) {
            if (logMetricFormatErrors_) logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
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
    
    @Override
    public void setMetricHashKey(long hashKey) {
        setHashKey(hashKey);
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
        if (metricValue_ == null) return null;
        return MathUtilities.getFastPlainStringWithNoTrailingZeros(metricValue_);
    }
    
    public long getMetricTimestamp() {
        return metricTimestamp_;
    }
   
    @Override
    public int getMetricTimestampInSeconds() {
        if (isMetricTimestampInSeconds_) return (int) metricTimestamp_;
        else return (int) (metricTimestamp_ / 1000);
    }
    
    @Override
    public long getMetricTimestampInMilliseconds() {
        if (!isMetricTimestampInSeconds_) return metricTimestamp_;
        else return (long) (metricTimestamp_ * 1000);
    }
    
    @Override
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

    public boolean isMetricTimestampInSeconds() {
        return isMetricTimestampInSeconds_;
    }
    
    public static boolean isLogMetricFormatErrors() {
        return logMetricFormatErrors_;
    }

    public static void setLogMetricFormatErrors(boolean isLogMetricFormatErrors) {
        logMetricFormatErrors_ = isLogMetricFormatErrors;
    }
    
}
