package com.pearson.statsagg.metric_aggregation.opentsdb;

import com.pearson.statsagg.metric_aggregation.GenericMetricFormat;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.boon.Boon;
import org.boon.core.value.LazyValueMap;
import org.boon.core.value.ValueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbMetricRaw implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbMetricRaw.class.getName());
    
    private Long hashKey_ = null;
    
    private final String metric_;
    private final String metricTimestamp_;
    private final String metricValue_;
    private final List<OpenTsdbTag> tags_;
    private Long metricReceivedTimestampInMilliseconds_ = null;
    
    private String metricKey_ = null;
    private Long metricTimestampInMilliseconds_ = null;
    private BigDecimal metricValueBigDecimal_ = null;
    private List<String> sortedUnparsedTags_ = null;
    
    public OpenTsdbMetricRaw(String metric, String metricTimestamp, String metricValue, List<OpenTsdbTag> tags, 
            List<String> sortedUnparsedtags, Long metricReceivedTimestampInMilliseconds) {
        this.metric_ = metric;
        this.metricTimestamp_ = metricTimestamp;
        this.metricValue_ = metricValue;
        this.tags_ = tags;
        this.sortedUnparsedTags_ = sortedUnparsedtags;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.metricKey_ = createAndGetMetricKey();
    }
    
    public OpenTsdbMetricRaw(String metric, String metricTimestamp, String metricValue, List<OpenTsdbTag> tags, 
            List<String> sortedUnparsedtags, Long metricTimestampInMilliseconds, Long metricReceivedTimestampInMilliseconds) {
        this.metric_ = metric;
        this.metricTimestamp_ = metricTimestamp;
        this.metricValue_ = metricValue;
        this.tags_ = tags;
        this.sortedUnparsedTags_ = sortedUnparsedtags;
        this.metricTimestampInMilliseconds_ = metricTimestampInMilliseconds;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.metricKey_ = createAndGetMetricKey();
    }    
    
    public final String createAndGetMetricKey() {

        if (metricKey_ != null) return metricKey_;
        if (metric_ == null) return null;
        
        StringBuilder metricKey = new StringBuilder("");
        
        metricKey.append(metric_);
        
        if ((sortedUnparsedTags_ != null) && !sortedUnparsedTags_.isEmpty()) {
            metricKey.append(" : ");
            
            for (int i = 0; i < sortedUnparsedTags_.size(); i++) {
                metricKey.append(sortedUnparsedTags_.get(i));
                if ((i + 1) != sortedUnparsedTags_.size()) metricKey.append(" ");
            }
        }
        
        metricKey_ = metricKey.toString();
        
        return metricKey_;
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

    public Long createAndGetMetricTimestampInMilliseconds() {
        
        try {
            if ((metricTimestampInMilliseconds_ == null)) {
                if (metricTimestamp_.length() == 10) metricTimestampInMilliseconds_ = Long.valueOf(metricTimestamp_) * 1000;
                else if (metricTimestamp_.length() == 13) metricTimestampInMilliseconds_ = Long.valueOf(metricTimestamp_);
                else logger.warn("OpenTSDB metric=\"" + metric_ + "\" has an invalid timestamp: \"" + metricTimestamp_ + "\"");
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
        
        stringBuilder.append(metric_).append(" ").append(metricTimestamp_).append(" ").append(metricValue_).append(" ");

        if (tags_ != null) {
            for (int i = 0; i < tags_.size(); i++) {
                stringBuilder.append(tags_.get(i).getUnparsedTag());
                if ((i + 1) != tags_.size()) stringBuilder.append(" ");
            }
        }
        
        return stringBuilder.toString();
    }

    @Override
    public String getGraphiteFormatString() {
        StringBuilder stringBuilder = new StringBuilder("");
        
        stringBuilder.append(metric_).append(" ").append(metricValue_).append(" ").append(metricTimestamp_);
        
        return stringBuilder.toString();
    }

    @Override
    public String getOpenTsdbFormatString() {
        return toString();
    }
    
    public static String getOpenTsdbJson(List<OpenTsdbMetricRaw> openTsdbMetricRaws) {
        
        if (openTsdbMetricRaws == null) return null;

        StringBuilder openTsdbJson = new StringBuilder("");
        
        openTsdbJson.append("[");
        
        for (int i = 0; i < openTsdbMetricRaws.size(); i++) {
            OpenTsdbMetricRaw openTsdbMetricRaw = openTsdbMetricRaws.get(i);
            
            if ((openTsdbMetricRaw.getMetric() == null) || openTsdbMetricRaw.getMetric().isEmpty()) continue;
            if ((openTsdbMetricRaw.getMetricTimestamp() == null) || openTsdbMetricRaw.getMetricTimestamp().isEmpty()) continue;
            if ((openTsdbMetricRaw.getMetricValue() == null) || openTsdbMetricRaw.getMetricValue().isEmpty()) continue; 
            if ((openTsdbMetricRaw.getTags() == null) || openTsdbMetricRaw.getTags().isEmpty()) continue;

            openTsdbJson.append("{");
            
            openTsdbJson.append("\"metric\":\"").append(openTsdbMetricRaw.getMetric()).append("\",");
            openTsdbJson.append("\"timestamp\":").append(openTsdbMetricRaw.getMetricTimestamp()).append(",");
            openTsdbJson.append("\"value\":").append(openTsdbMetricRaw.getMetricTimestamp()).append(",");
            
            openTsdbJson.append("\"tags\":{");
            
            for (int j = 0; j < openTsdbMetricRaw.getTags().size(); j++) {
                OpenTsdbTag tag = openTsdbMetricRaw.getTags().get(j);
                openTsdbJson.append("\"").append(tag.getKey()).append("\":\"").append(tag.getValue()).append("\"");
                if ((j + 1) != openTsdbMetricRaw.getTags().size()) openTsdbJson.append(",");
            }

            openTsdbJson.append("}");
            
            openTsdbJson.append("}");
            
            if ((i + 1) != openTsdbMetricRaws.size()) openTsdbJson.append(",");
        }
        
        openTsdbJson.append("]");
        
        return openTsdbJson.toString();
    }
    
    public static OpenTsdbMetricRaw parseOpenTsdbMetricRaw(String unparsedMetric, long metricReceivedTimestampInMilliseconds) {
        
        if (unparsedMetric == null) {
            return null;
        }
        
        try {
            int metricIndexRange = unparsedMetric.indexOf(' ', 0);
            String metric = null;
            if (metricIndexRange > 0) {
                metric = unparsedMetric.substring(0, metricIndexRange);
            }

            int metricTimestampIndexRange = unparsedMetric.indexOf(' ', metricIndexRange + 1);
            String metricTimestamp = null;
            if (metricTimestampIndexRange > 0) {
                metricTimestamp = unparsedMetric.substring(metricIndexRange + 1, metricTimestampIndexRange);
            }

            int metricValueIndexRange = unparsedMetric.indexOf(' ', metricTimestampIndexRange + 1);
            String metricValue = null;
            if (metricValueIndexRange > 0) {
                metricValue = unparsedMetric.substring(metricTimestampIndexRange + 1, metricValueIndexRange);
            }
            
            String metricTags_String = unparsedMetric.substring(metricValueIndexRange + 1, unparsedMetric.length());
            List<OpenTsdbTag> openTsdbTags = OpenTsdbTag.parseRawTags(metricTags_String);
            List<String> sortedUnparsedTags = new ArrayList<>();
            for (OpenTsdbTag openTsdbTag : openTsdbTags) if (openTsdbTag.getUnparsedTag() != null) sortedUnparsedTags.add(openTsdbTag.getUnparsedTag());
            Collections.sort(sortedUnparsedTags);
            
            if ((metric == null) || metric.isEmpty() || 
                    (metricValue == null) || metricValue.isEmpty() || 
                    (metricTimestamp == null) || metricTimestamp.isEmpty() || 
                    ((metricTimestamp.length() != 10) && (metricTimestamp.length() != 13))) {
                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                if ((openTsdbTags == null) || openTsdbTags.isEmpty() || sortedUnparsedTags.isEmpty()) {
                    logger.warn("No tags associated with metric: \"" + metric + "\"");
                }
                
                OpenTsdbMetricRaw openTsdbRaw = new OpenTsdbMetricRaw(metric.trim(), metricTimestamp.trim(), 
                        metricValue.trim(), openTsdbTags, sortedUnparsedTags, metricReceivedTimestampInMilliseconds); 
                
                if ((openTsdbRaw.getMetricValueBigDecimal() != null) && (openTsdbRaw.getMetricTimestampInMilliseconds() != null) && (openTsdbRaw.getMetricKey() != null)) {
                    return openTsdbRaw;
                }
                else return null;
            }
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static List<OpenTsdbMetricRaw> parseOpenTsdbJson(String inputJson, long metricsReceivedTimestampInMilliseconds) {
        return parseOpenTsdbJson(inputJson, metricsReceivedTimestampInMilliseconds, new ArrayList<Integer>());
    }
    
    /* successCountAndFailCount is modified by this method. index-0 will have the successfully parsed metric count, and index-1 will have the metrics with errors count */
    public static List<OpenTsdbMetricRaw> parseOpenTsdbJson(String inputJson, long metricsReceivedTimestampInMilliseconds, List<Integer> successCountAndFailCount) {

        if ((inputJson == null) || inputJson.isEmpty()) {
            return new ArrayList<>();
        }

        int successMetricCount = 0, errorMetricCount = 0, totalMetricCount = 0;
        
        ValueList parsedJsonObject = null;
        
        try {
            parsedJsonObject = (ValueList) Boon.fromJson(inputJson);
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (parsedJsonObject == null) return new ArrayList<>();
            
        StringBuilder openTsdbMetricsString = new StringBuilder("");
                
        for (Object openTsdbMetricJsonObject : parsedJsonObject) {
            try {
                LazyValueMap openTsdbMetric = (LazyValueMap) openTsdbMetricJsonObject;
                
                String metric = (String) openTsdbMetric.get("metric");
                
                Object timestampObject = openTsdbMetric.get("timestamp");
                String timestampString = null;
                if (timestampObject instanceof Integer) timestampString = Integer.toString((Integer) timestampObject);
                else if (timestampObject instanceof Long) timestampString = Long.toString((Long) timestampObject);
                else if (timestampObject instanceof Double) timestampString = Double.toString((Double) timestampObject);
                else if (timestampObject instanceof Float) timestampString = Float.toString((Float) timestampObject);
                else if (timestampObject instanceof String) timestampString = (String) timestampObject;
                
                Object valueObject = openTsdbMetric.get("value");
                String valueString = null;
                if (valueObject instanceof Double) valueString = Double.toString((Double) valueObject);
                else if (valueObject instanceof Integer) valueString = Integer.toString((Integer) valueObject);
                else if (valueObject instanceof Long) valueString = Long.toString((Long) valueObject);
                else if (valueObject instanceof Float) valueString = Float.toString((Float) valueObject);
                else if (valueObject instanceof String) valueString = (String) valueObject;
                
                LazyValueMap tagsObject = (LazyValueMap) openTsdbMetric.get("tags");
                StringBuilder tagsString = new StringBuilder("");
                int tagCounter = 0;
                for (String tagKey : tagsObject.keySet()) {
                    tagsString.append(tagKey).append("=").append(tagsObject.get(tagKey));
                    if ((tagCounter + 1) < tagsObject.size()) tagsString.append(" ");
                }
                
                String openTsdbMetricString = metric + " " + timestampString + " " + valueString + " " + tagsString.toString() + "\n";
                openTsdbMetricsString.append(openTsdbMetricString);
                
                totalMetricCount++;
            }
            catch (Exception e) {
                totalMetricCount++;                
                logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = OpenTsdbMetricRaw.parseOpenTsdbMetricsRaw(openTsdbMetricsString.toString(), metricsReceivedTimestampInMilliseconds);
        
        successMetricCount = openTsdbMetricsRaw.size();
        errorMetricCount = totalMetricCount - successMetricCount;
        if (successCountAndFailCount == null) successCountAndFailCount = new ArrayList<>();
        if (!successCountAndFailCount.isEmpty()) successCountAndFailCount.clear();
        successCountAndFailCount.add(successMetricCount);
        successCountAndFailCount.add(errorMetricCount);

        return openTsdbMetricsRaw;
    }
    
    public static List<OpenTsdbMetricRaw> parseOpenTsdbMetricsRaw(String unparsedMetrics, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = new ArrayList();
            
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
                    OpenTsdbMetricRaw openTsdbMetricRaw = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric.trim(), metricReceivedTimestampInMilliseconds);
                    if (openTsdbMetricRaw != null) openTsdbMetricsRaw.add(openTsdbMetricRaw);
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return openTsdbMetricsRaw;
    }
    
    /*
    For every unique metric key, get the OpenTsdbMetricRaw with the most recent 'metric timestamp'. 
    
    In the event that multiple OpenTsdbMetricRaws share the same 'metric key' and 'metric timestamp', 
    then 'metric received timestamp' is used as a tiebreaker. 
    
    In the event that multiple OpenTsdbMetricRaws also share the same 'metric received timestamp', 
    then this method will return the first OpenTsdbMetricRaw that it scanned that met these criteria
    */
    public static Map<String,OpenTsdbMetricRaw> getMostRecentOpenTsdbMetricRawByMetricKey(List<OpenTsdbMetricRaw> openTsdbMetricsRaw) {
        
        if (openTsdbMetricsRaw == null || openTsdbMetricsRaw.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, OpenTsdbMetricRaw> mostRecentOpenTsdbMetricsByMetricKey = new HashMap<>();

        for (OpenTsdbMetricRaw openTsdbMetricRaw : openTsdbMetricsRaw) {
            try {
                boolean doesAlreadyContainMetricKey = mostRecentOpenTsdbMetricsByMetricKey.containsKey(openTsdbMetricRaw.getMetricKey());

                if (doesAlreadyContainMetricKey) {
                    OpenTsdbMetricRaw currentMostRecentOpenTsdbMetricRaw = mostRecentOpenTsdbMetricsByMetricKey.get(openTsdbMetricRaw.getMetricKey());

                    if (openTsdbMetricRaw.getMetricTimestampInMilliseconds() > currentMostRecentOpenTsdbMetricRaw.getMetricTimestampInMilliseconds()) {
                        mostRecentOpenTsdbMetricsByMetricKey.put(openTsdbMetricRaw.getMetricKey(), openTsdbMetricRaw);
                    }
                    else if (openTsdbMetricRaw.getMetricTimestampInMilliseconds().equals(currentMostRecentOpenTsdbMetricRaw.getMetricTimestampInMilliseconds())) {
                        if (openTsdbMetricRaw.getMetricReceivedTimestampInMilliseconds() > currentMostRecentOpenTsdbMetricRaw.getMetricReceivedTimestampInMilliseconds()) {
                            mostRecentOpenTsdbMetricsByMetricKey.put(openTsdbMetricRaw.getMetricKey(), openTsdbMetricRaw);
                        }
                    }
                }
                else {
                    mostRecentOpenTsdbMetricsByMetricKey.put(openTsdbMetricRaw.getMetricKey(), openTsdbMetricRaw);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return mostRecentOpenTsdbMetricsByMetricKey;
    }
    
    public static List<OpenTsdbMetricRaw> createPrefixedOpenTsdbMetricsRaw(List<OpenTsdbMetricRaw> openTsdbMetricsRaw, 
            boolean useGlobalPrefix, String globalPrefixValue,
            boolean useOpenTsdbPrefix, String openTsdbPrefixValue) {
        
        if ((openTsdbMetricsRaw == null) || openTsdbMetricsRaw.isEmpty()) {
            return new ArrayList<>();
        }
        
        int prefixedOpenTsdbMetricsRawListInitialSize = (int) (openTsdbMetricsRaw.size() * 1.3);
        List<OpenTsdbMetricRaw> prefixedOpenTsdbMetricsRaw = new ArrayList<>(prefixedOpenTsdbMetricsRawListInitialSize);
        
        for (OpenTsdbMetricRaw openTsdbMetricRaw : openTsdbMetricsRaw) {
            String prefixedMetric = createPrefixedOpenTsdbMetric(openTsdbMetricRaw, useGlobalPrefix, globalPrefixValue, 
                    useOpenTsdbPrefix, openTsdbPrefixValue);
            
            OpenTsdbMetricRaw prefixedOpenTsdbMetricRaw = new OpenTsdbMetricRaw(prefixedMetric, openTsdbMetricRaw.getMetricTimestamp(), 
                    openTsdbMetricRaw.getMetricValue(), openTsdbMetricRaw.getTags(), openTsdbMetricRaw.getSortedUnparsedTags(), openTsdbMetricRaw.getMetricTimestampInMilliseconds(), 
                    openTsdbMetricRaw.getMetricReceivedTimestampInMilliseconds());

            prefixedOpenTsdbMetricRaw.setHashKey(openTsdbMetricRaw.getHashKey());
            
            prefixedOpenTsdbMetricsRaw.add(prefixedOpenTsdbMetricRaw);
        }
        
        return prefixedOpenTsdbMetricsRaw;
    }
    
    public static String createPrefixedOpenTsdbMetric(OpenTsdbMetricRaw openTsdbMetricRaw, 
            boolean useGlobalPrefix, String globalPrefixValue,
            boolean useOpenTsdbPrefix, String openTsdbPrefixValue) {
        
        if (openTsdbMetricRaw == null) {
            return null;
        }
        
        if (!useGlobalPrefix && !useOpenTsdbPrefix) {
            return openTsdbMetricRaw.getMetric();
        }
        
        StringBuilder prefix = new StringBuilder("");
        
        if (useGlobalPrefix) prefix.append(globalPrefixValue).append(".");
        if (useOpenTsdbPrefix) prefix.append(openTsdbPrefixValue).append(".");
        
        prefix.append(openTsdbMetricRaw.getMetric());
        
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
    
    @Override
    public String getMetricKey() {
        return createAndGetMetricKey();
    }
    
    public String getMetric() {
        return metric_;
    }
    
    public String getMetricTimestamp() {
        return metricTimestamp_;
    }
    
    @Override
    public Long getMetricTimestampInMilliseconds() {
        return createAndGetMetricTimestampInMilliseconds();
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

    public List<String> getSortedUnparsedTags() {
        return sortedUnparsedTags_;
    }

}
