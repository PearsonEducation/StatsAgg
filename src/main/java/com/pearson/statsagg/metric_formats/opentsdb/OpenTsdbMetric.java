package com.pearson.statsagg.metric_formats.opentsdb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pearson.statsagg.metric_formats.GenericMetricFormat;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import com.pearson.statsagg.metric_formats.graphite.GraphiteMetricFormat;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetricFormat_v1;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbMetric implements GraphiteMetricFormat, OpenTsdbMetricFormat, GenericMetricFormat, InfluxdbMetricFormat_v1 {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbMetric.class.getName());
    
    private static char[] EXPONENT_CHARS = {'e','E'};
    
    private long hashKey_ = -1;
    
    private final long metricTimestamp_;
    private final BigDecimal metricValue_;
    private final boolean isTimestampInMilliseconds_;
    private long metricReceivedTimestampInMilliseconds_ = -1;
    
    private String metricKey_ = null;
    private final int metricLength_;  // 'metric' refers to the OpenTSDB 'metric name' 

    public OpenTsdbMetric(String metric, long metricTimestampInMilliseconds, BigDecimal metricValue, List<OpenTsdbTag> tags) {
        this.metricTimestamp_ = metricTimestampInMilliseconds;
        this.metricValue_ = metricValue;
        this.isTimestampInMilliseconds_ = true;
        this.metricReceivedTimestampInMilliseconds_ = metricTimestampInMilliseconds;
        
        this.metricKey_ = createAndGetMetricKey(metric, tags);
        
        if (metric != null) this.metricLength_ = metric.length();
        else this.metricLength_ = -1;
    }
    
    public OpenTsdbMetric(String metric, int metricTimestampInSeconds, BigDecimal metricValue, List<OpenTsdbTag> tags) {
        this.metricTimestamp_ = metricTimestampInSeconds;
        this.metricValue_ = metricValue;
        this.isTimestampInMilliseconds_ = false;
        this.metricReceivedTimestampInMilliseconds_ = metricTimestampInSeconds * 1000;
        
        this.metricKey_ = createAndGetMetricKey(metric, tags);
        
        if (metric != null) this.metricLength_ = metric.length();
        else this.metricLength_ = -1;
    }
    
    public OpenTsdbMetric(String metric, long metricTimestamp, BigDecimal metricValue, List<OpenTsdbTag> tags, 
            boolean isTimestampInMilliseconds, long metricReceivedTimestampInMilliseconds) {
        this.metricTimestamp_ = metricTimestamp;
        this.metricValue_ = metricValue;
        this.isTimestampInMilliseconds_ = isTimestampInMilliseconds;
        this.metricReceivedTimestampInMilliseconds_ = metricReceivedTimestampInMilliseconds;
        
        this.metricKey_ = createAndGetMetricKey(metric, tags);
        
        if (metric != null) this.metricLength_ = metric.length();
        else this.metricLength_ = -1;
    }

    public final String createAndGetMetricKey(String metric, List<OpenTsdbTag> tags) {

        if (metricKey_ != null) return metricKey_;
        if (metric == null) return null;
        
        ArrayList sortedUnparseTags = getSortedUnparsedTags(tags);
        if ((sortedUnparseTags == null) || sortedUnparseTags.isEmpty()) return null;
        
        StringBuilder metricKey = new StringBuilder(64);
        
        metricKey.append(metric);
        metricKey.append(" : ");

        for (int i = 0; i < sortedUnparseTags.size(); i++) {
            metricKey.append(sortedUnparseTags.get(i));
            if ((i + 1) != sortedUnparseTags.size()) metricKey.append(" ");
        }
        
        metricKey_ = metricKey.toString();
        
        return metricKey_;
    }
    
    private String getMetricFromMetricKey() {
        if (metricKey_ == null) return null;
        if (metricLength_ < 0) return null;
        if (metricLength_ >= metricKey_.length()) return metricKey_;
            
        return metricKey_.substring(0, metricLength_);
    }

    private List<OpenTsdbTag> getMetricTagsFromMetricKey() {
        if (metricKey_ == null) return new ArrayList<>();
        if (metricLength_ < 1) return new ArrayList<>();
        if ((metricLength_ + 3) >= metricKey_.length()) return new ArrayList<>();
        
        List<OpenTsdbTag> openTsdbTags = OpenTsdbTag.parseTags(metricKey_, metricLength_ + 2);
        return openTsdbTags;
    }
    
    /*
    The input is expected to be an OpenTSDB metric, an OpenTSDB tag key, or an OpenTSDB tag value
    */
    public static String getOpenTsdbSanitizedString(String unsanitizedInput) {

        if (unsanitizedInput == null) {
            return null;
        }

        StringBuilder sanitizedInput = new StringBuilder();
 
        for (int i = 0; i < unsanitizedInput.length(); i++) {
            char character = unsanitizedInput.charAt(i);
                    
            if (Character.isLetterOrDigit(character)) {
                sanitizedInput.append(character);
                continue;
            }
            
            if ((character == '-') || (character == '_') || (character == '.') || (character == '/')) {
                sanitizedInput.append(character);
                continue;
            }
        }

        return sanitizedInput.toString();
    }

    @Override
    public String toString() {        
        return getOpenTsdbTelnetFormatString(false) + " @ " + metricReceivedTimestampInMilliseconds_;
    }

    @Override
    public String getGraphiteFormatString(boolean sanitizeMetric, boolean substituteCharacters) {
        StringBuilder stringBuilder = new StringBuilder();
        
        String metricPath = GraphiteMetric.getGraphiteSanitizedString(getMetric(), sanitizeMetric, substituteCharacters);
        
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
        
        String metric = sanitizeMetric ? getOpenTsdbSanitizedString(getMetric()) : getMetric();
        
        stringBuilder.append(metric).append(" ").append(metricTimestamp_).append(" ").append(getMetricValueString()).append(" ");

        List<OpenTsdbTag> openTsdbTags = getMetricTagsFromMetricKey();
        if ((openTsdbTags != null) && (defaultOpenTsdbTagKey != null)) openTsdbTags.add(new OpenTsdbTag(defaultOpenTsdbTagKey + "=" + defaultOpenTsdbTagValue));
        
        if (openTsdbTags != null) {
            for (int i = 0; i < openTsdbTags.size(); i++) {
                String tagKey = sanitizeMetric ? getOpenTsdbSanitizedString(openTsdbTags.get(i).getTagKey()) : openTsdbTags.get(i).getTagKey();
                String tagValue = sanitizeMetric ? getOpenTsdbSanitizedString(openTsdbTags.get(i).getTagValue()) : openTsdbTags.get(i).getTagValue();

                stringBuilder.append(tagKey).append('=').append(tagValue);
                if ((i + 1) != openTsdbTags.size()) stringBuilder.append(" ");
            }
        }
        
        return stringBuilder.toString();
    }
    
    @Override
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric) {
        return getOpenTsdbJsonFormatString(sanitizeMetric, null, null);
    }
    
    @Override
    public String getOpenTsdbJsonFormatString(boolean sanitizeMetric, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue) {
                
        String metric = sanitizeMetric ? getOpenTsdbSanitizedString(getMetric()) : getMetric();
        List<OpenTsdbTag> openTsdbTags = getMetricTagsFromMetricKey();
        if ((openTsdbTags != null) && (defaultOpenTsdbTagKey != null)) openTsdbTags.add(new OpenTsdbTag(defaultOpenTsdbTagKey + "=" + defaultOpenTsdbTagValue));

        if ((metric == null) || metric.isEmpty()) return null;
        if (metricTimestamp_ < 0) return null;
        if ((getMetricValue() == null)) return null;
        
        StringBuilder openTsdbJson = new StringBuilder();

        openTsdbJson.append("{");

        openTsdbJson.append("\"metric\":\"").append(StringEscapeUtils.escapeJson(metric)).append("\",");
        openTsdbJson.append("\"timestamp\":").append(metricTimestamp_).append(",");
        openTsdbJson.append("\"value\":").append(getMetricValueString()).append(",");

        openTsdbJson.append("\"tags\":{");

        if (openTsdbTags != null) {
            for (int j = 0; j < openTsdbTags.size(); j++) {
                OpenTsdbTag tag = openTsdbTags.get(j);
                
                if (sanitizeMetric) {
                    openTsdbJson.append("\"").append(StringEscapeUtils.escapeJson(getOpenTsdbSanitizedString(tag.getTagKey())));
                    openTsdbJson.append("\":\"").append(StringEscapeUtils.escapeJson(getOpenTsdbSanitizedString(tag.getTagValue()))).append("\"");
                }
                else {
                    openTsdbJson.append("\"").append(StringEscapeUtils.escapeJson(tag.getTagKey()));
                    openTsdbJson.append("\":\"").append(StringEscapeUtils.escapeJson(tag.getTagValue())).append("\"");
                }
                
                if ((j + 1) != openTsdbTags.size()) openTsdbJson.append(",");
            }
        }
        
        openTsdbJson.append("}");

        openTsdbJson.append("}");
        
        return openTsdbJson.toString();
    }
    
    @Override
    public String getInfluxdbV1JsonFormatString() {

        String metric = getMetric();
        List<OpenTsdbTag> openTsdbTags = getMetricTagsFromMetricKey();

        if ((metric == null) || metric.isEmpty()) return null;
        if (metricTimestamp_ < 0) return null;
        if ((getMetricValue() == null)) return null;

        StringBuilder influxdbJson = new StringBuilder();

        influxdbJson.append("{");

        // the metric name, with the prefix already built-in
        influxdbJson.append("\"name\":\"");
        influxdbJson.append(StringEscapeUtils.escapeJson(metric)).append("\",");

        // column order: value, time, tag(s)
        influxdbJson.append("\"columns\":[\"value\",\"time\"");

        if (openTsdbTags != null && !openTsdbTags.isEmpty()) {
            influxdbJson.append(",");
            
            for (int j = 0; j < openTsdbTags.size(); j++) {
                OpenTsdbTag tag = openTsdbTags.get(j);
                influxdbJson.append("\"").append(StringEscapeUtils.escapeJson(tag.getTagKey())).append("\"");
                if ((j + 1) != openTsdbTags.size()) influxdbJson.append(",");
            }
        }

        influxdbJson.append("],");
        
        // only include one point in the points array. note-- timestamp will always be sent to influxdb in milliseconds
        influxdbJson.append("\"points\":[[");
        influxdbJson.append(getMetricValueString()).append(",");
        influxdbJson.append(getMetricTimestampInMilliseconds());
        
        if ((openTsdbTags != null) && !openTsdbTags.isEmpty()) {
            influxdbJson.append(",");
            
            for (int j = 0; j < openTsdbTags.size(); j++) {
                OpenTsdbTag tag = openTsdbTags.get(j);
                influxdbJson.append("\"").append(StringEscapeUtils.escapeJson(tag.getTagValue())).append("\"");
                if ((j + 1) != openTsdbTags.size()) influxdbJson.append(",");
            }
        }
                    
        influxdbJson.append("]]}");

        return influxdbJson.toString();
    }
    
    public static String getOpenTsdbJson(List<? extends OpenTsdbMetricFormat> openTsdbFormatMetrics, boolean sanitizeMetrics) {
        return getOpenTsdbJson(openTsdbFormatMetrics, sanitizeMetrics, null, null);
    }
    
    public static String getOpenTsdbJson(List<? extends OpenTsdbMetricFormat> openTsdbFormatMetrics, boolean sanitizeMetrics, String defaultOpenTsdbTagKey, String defaultOpenTsdbTagValue) {
        
        if (openTsdbFormatMetrics == null) return null;

        StringBuilder openTsdbJson = new StringBuilder();
        
        openTsdbJson.append("[");
        
        for (int i = 0; i < openTsdbFormatMetrics.size(); i++) {
            OpenTsdbMetricFormat openTsdbMetric = openTsdbFormatMetrics.get(i);
            String openTsdbJsonString = openTsdbMetric.getOpenTsdbJsonFormatString(sanitizeMetrics, defaultOpenTsdbTagKey, defaultOpenTsdbTagValue);
            
            if (openTsdbJsonString != null) {
                openTsdbJson.append(openTsdbJsonString);
                if ((i + 1) != openTsdbFormatMetrics.size()) openTsdbJson.append(",");
            }
        }
        
        openTsdbJson.append("]");
        
        return openTsdbJson.toString();
    }
    
    public static OpenTsdbMetric parseOpenTsdbTelnetMetric(String unparsedMetric, String metricPrefix, long metricReceivedTimestampInMilliseconds) {
        
        if (unparsedMetric == null) {
            return null;
        }
        
        try {
            int metricIndexRange = unparsedMetric.indexOf(' ', 0);
            String metric = null;
            if (metricIndexRange > 0) {
                if ((metricPrefix != null) && !metricPrefix.isEmpty()) metric = metricPrefix + unparsedMetric.substring(0, metricIndexRange);
                else metric = unparsedMetric.substring(0, metricIndexRange);
            }

            int metricTimestampIndexRange = unparsedMetric.indexOf(' ', metricIndexRange + 1);
            String metricTimestampString = null;
            long metricTimestamp = -1;
            Boolean isTimestampInMilliseconds = null;
            if (metricTimestampIndexRange > 0) {
                metricTimestampString = unparsedMetric.substring(metricIndexRange + 1, metricTimestampIndexRange);
                metricTimestamp = Long.parseLong(metricTimestampString);
                if (metricTimestampString.length() == 13) isTimestampInMilliseconds = true;
                else if (metricTimestampString.length() <= 10) isTimestampInMilliseconds = false;
            }

            int metricValueIndexRange = unparsedMetric.indexOf(' ', metricTimestampIndexRange + 1);
            
            BigDecimal metricValueBigDecimal = null;
            if (metricValueIndexRange > 0) {
                String metricValueString = unparsedMetric.substring(metricTimestampIndexRange + 1, metricValueIndexRange);
                boolean isMetricValueSizeReasonable = parseOpenTsdbJson_ValidateMetricValue_IsMetricValueSizeReasonable(metricValueString);
                if (isMetricValueSizeReasonable) metricValueBigDecimal = new BigDecimal(metricValueString);
            }
            
            List<OpenTsdbTag> openTsdbTags = OpenTsdbTag.parseTags(unparsedMetric, metricValueIndexRange);
            
            if ((metric == null) || metric.isEmpty() || 
                    (metricValueBigDecimal == null) ||
                    (metricTimestampString == null) || (metricTimestamp == -1) || 
                    (openTsdbTags == null) || (openTsdbTags.isEmpty()) || 
                    (isTimestampInMilliseconds == null) || ((metricTimestampString.length() != 10) && (metricTimestampString.length() != 13))) {
                logger.warn("Metric parse error: \"" + unparsedMetric + "\"");
                return null;
            }
            else {
                OpenTsdbMetric openTsdbMetric = new OpenTsdbMetric(metric, metricTimestamp, metricValueBigDecimal, openTsdbTags, 
                        isTimestampInMilliseconds, metricReceivedTimestampInMilliseconds); 
                
                if ((openTsdbMetric.getMetricKey() != null) && (openTsdbMetric.getMetricTimestampInMilliseconds() > -1)) return openTsdbMetric;
                else return null;
            }
        }
        catch (NumberFormatException e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator());  
            return null;
        }
        catch (Exception e) {
            logger.error("Error on " + unparsedMetric + System.lineSeparator() + e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
            return null;
        }
    }
    
    public static ArrayList getSortedUnparsedTags(List<OpenTsdbTag> openTsdbTags) {
        
        ArrayList<String> sortedUnparsedTags = new ArrayList<>();
        
        if ((openTsdbTags == null) || openTsdbTags.isEmpty()) {
            sortedUnparsedTags.trimToSize();
            return sortedUnparsedTags;
        }
                
        for (OpenTsdbTag openTsdbTag : openTsdbTags) {
            String tag = openTsdbTag.getTag();
            if (tag != null) sortedUnparsedTags.add(tag);
        }
        
        Collections.sort(sortedUnparsedTags);
        sortedUnparsedTags.trimToSize();
        
        return sortedUnparsedTags;
    }
    
    public static List<OpenTsdbMetric> parseOpenTsdbJson(String inputJson, String metricPrefix, long metricsReceivedTimestampInMilliseconds) {
        return parseOpenTsdbJson(inputJson, metricPrefix, metricsReceivedTimestampInMilliseconds, new ArrayList<Integer>());
    }
    
    /* successCountAndFailCount is modified by this method. index-0 will have the successfully parsed metric count, and index-1 will have the metrics with errors count */
    public static List<OpenTsdbMetric> parseOpenTsdbJson(String inputJson, String metricPrefix, long metricsReceivedTimestampInMilliseconds, List<Integer> successCountAndFailCount) {

        if ((inputJson == null) || inputJson.isEmpty()) {
            return new ArrayList<>();
        }

        int successMetricCount = 0, errorMetricCount = 0;
        List<OpenTsdbMetric> openTsdbMetrics = new ArrayList<>();
        JsonElement jsonElement = null;
        JsonArray jsonArray = null;

        try {
            JsonParser parser = new JsonParser();
            jsonElement = parser.parse(inputJson);
            
            if (!jsonElement.isJsonArray()) {
                jsonArray = new JsonArray();
                jsonArray.add(jsonElement);
            }
            else {
                jsonArray = jsonElement.getAsJsonArray();
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (jsonArray == null) return openTsdbMetrics;
               
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement jsonElementOfArray = null;

            try {
                jsonElementOfArray = jsonArray.get(i);
                JsonObject jsonObject_TopLevel = jsonElementOfArray.getAsJsonObject();
                
                String metric = parseOpenTsdbJson_ValidateAndReturn_Metric(jsonObject_TopLevel);
                if (metric == null) continue;
                if ((metricPrefix != null) && !metricPrefix.isEmpty()) metric = metricPrefix + metric;
                    
                OpenTsdbTimestamp openTsdbTimestamp = parseOpenTsdbJson_ValidateAndReturn_MetricTimestamp(jsonObject_TopLevel);
                if (openTsdbTimestamp == null) continue;
                
                BigDecimal metricValue = parseOpenTsdbJson_ValidateAndReturn_MetricValue(jsonObject_TopLevel);
                if (metricValue == null) continue;
                
                List<OpenTsdbTag> openTsdbTags = parseOpenTsdbJson_ValidateAndReturn_Tags(jsonObject_TopLevel);
                if ((openTsdbTags == null) || openTsdbTags.isEmpty())  continue;
                
                OpenTsdbMetric openTsdbMetric = new OpenTsdbMetric(metric, openTsdbTimestamp.getTimestampLong(), metricValue, openTsdbTags, 
                        openTsdbTimestamp.isMilliseconds(), metricsReceivedTimestampInMilliseconds); 

                if ((openTsdbMetric.getMetricKey() != null) && (openTsdbMetric.getMetricTimestampInMilliseconds() > -1)) openTsdbMetrics.add(openTsdbMetric);
            }
            catch (Exception e) {
                if (jsonElementOfArray != null) {
                    try {
                        logger.warn("Metric parse error: " + jsonElementOfArray.getAsString());
                    }
                    catch (Exception e2) {
                        logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                    }
                }
                else {
                    logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }

        successMetricCount = openTsdbMetrics.size();
        errorMetricCount = jsonArray.size() - successMetricCount;
        if (successCountAndFailCount == null) successCountAndFailCount = new ArrayList<>();
        if (!successCountAndFailCount.isEmpty()) successCountAndFailCount.clear();
        successCountAndFailCount.add(successMetricCount);
        successCountAndFailCount.add(errorMetricCount);

        return openTsdbMetrics;
    }

    protected static String parseOpenTsdbJson_ValidateAndReturn_Metric(JsonObject jsonObject) {
        
        try {
            String metric = jsonObject.getAsJsonPrimitive("metric").getAsString();
            
            if ((metric == null) || metric.isEmpty()) {
                logger.warn("Metric parse error. Invalid metric name/path: \"" + jsonObject.toString());
                return null;
            }
            
            return metric;
        }
        catch (Exception e) {               
            try {
                logger.warn("Metric parse error. Invalid metric name/path: \"" + jsonObject.toString());
            }
            catch (Exception e2) {
                logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
            return null;
        }
        
    }

    protected static OpenTsdbTimestamp parseOpenTsdbJson_ValidateAndReturn_MetricTimestamp(JsonObject jsonObject) {
        
        try {
            long metricTimestamp = jsonObject.getAsJsonPrimitive("timestamp").getAsLong();
            
            if (metricTimestamp < 0) {
                logger.warn("Metric parse error. Invalid metric timestamp: \"" + jsonObject.toString());
                return null;
            }
            
            String metricTimestampString = Long.toString(metricTimestamp); 
            
            if ((metricTimestamp <= 2147483647l) && (metricTimestampString.length() <= 10)) {
                return new OpenTsdbTimestamp(metricTimestamp, false);
            }
            else if (metricTimestampString.length() == 13) {
                return new OpenTsdbTimestamp(metricTimestamp, true);
            }
            else {
                logger.warn("Metric parse error. Invalid metric timestamp: \"" + jsonObject.toString());
                return null;
            }
        }
        catch (Exception e) {               
            try {
                logger.warn("Metric parse error. Invalid metric timestamp: \"" + jsonObject.toString());
            }
            catch (Exception e2) {
                logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
            return null;
        }
        
    }
    
    protected static BigDecimal parseOpenTsdbJson_ValidateAndReturn_MetricValue(JsonObject jsonObject) {
        
        try {
            String numericString = jsonObject.getAsJsonPrimitive("value").getAsString();
            
            boolean isMetricValueSizeReasonable = parseOpenTsdbJson_ValidateMetricValue_IsMetricValueSizeReasonable(numericString);
            if (!isMetricValueSizeReasonable) return null;

            BigDecimal metricValue = new BigDecimal(numericString);
            return metricValue;
        }
        catch (Exception e) {    
            try {
                logger.warn("Metric parse error. Invalid metric value: \"" + jsonObject.toString());
            }
            catch (Exception e2) {
                logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
            return null;
        }
        
    }
    
    protected static boolean parseOpenTsdbJson_ValidateMetricValue_IsMetricValueSizeReasonable(String metricValueString) {
        
        if ((metricValueString == null) || metricValueString.isEmpty()) return false;
        
        if (metricValueString.length() > 100) {
            logger.debug("Metric parse error. Metric value can't be more than 100 characters long. Metric value was \"" + metricValueString.length() + "\" characters long. " +
                    "MetricString=\"" + metricValueString + "\"");
            return false;
        }
 
        try {
            int exponentIndex = StringUtils.indexOfAny(metricValueString, EXPONENT_CHARS);

            if (exponentIndex != -1) {
                String exponentValue = metricValueString.substring(exponentIndex + 1);
                
                if (exponentValue.length() > 2) {
                    boolean isNegativeExponent = exponentValue.startsWith("-");
                    
                    if (!isNegativeExponent) {
                        logger.debug("Metric parse error. Exponent is too large. " + "MetricString=\"" + metricValueString + "\"");
                        return false;
                    }
                    else if (exponentValue.length() > 3) {
                        logger.debug("Metric parse error. Exponent is too large. " + "MetricString=\"" + metricValueString + "\"");
                        return false;
                    }
                    else {
                        return true;
                    }
                }
                else {
                    return true;
                }
            }
            else {
                return true;
            }
        }
        catch (Exception e) {   
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        
    }
    
    protected static List<OpenTsdbTag> parseOpenTsdbJson_ValidateAndReturn_Tags(JsonObject jsonObject) {
        
        List<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        
        try {
            JsonObject JsonObject_Tags = jsonObject.getAsJsonObject("tags");

            try {
                for (Map.Entry<String,JsonElement> tagsObject_Entry : JsonObject_Tags.entrySet()) {
                    String tagKey = tagsObject_Entry.getKey();
                    String tagValue = tagsObject_Entry.getValue().getAsString();

                    if ((tagKey != null) && (tagValue != null) && !tagKey.isEmpty() && !tagValue.isEmpty()) {
                        OpenTsdbTag openTsdbTag = new OpenTsdbTag(tagKey + "=" + tagValue);
                        openTsdbTags.add(openTsdbTag);
                    }
                    else {
                        logger.warn("Metric parse error. Invalid metric tag: \"" + jsonObject.toString());
                    }
                }
            }
            catch (Exception e) {   
                logger.warn("Metric parse error. Invalid metric tag: \"" + jsonObject.toString());
            }
        }
        catch (Exception e) {               
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        if (openTsdbTags.isEmpty()) {
            logger.warn("Metric parse error. At least 1 valid tag required: \"" + jsonObject.toString());
        }
            
        return openTsdbTags;
    }
    
    public static List<OpenTsdbMetric> parseOpenTsdbTelnetMetrics(String unparsedMetrics, String metricPrefix, long metricReceivedTimestampInMilliseconds) {
        
        if ((unparsedMetrics == null) || unparsedMetrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<OpenTsdbMetric> openTsdbMetrics = new ArrayList();
            
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
                    OpenTsdbMetric openTsdbMetric = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric.trim(), metricPrefix, metricReceivedTimestampInMilliseconds);
                    if (openTsdbMetric != null) openTsdbMetrics.add(openTsdbMetric);
                }
            }
        }
        catch (Exception e) {
            logger.warn(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return openTsdbMetrics;
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
    
    @Override
    public String getMetricKey() {
        return metricKey_;
    }
    
    public String getMetric() {
        return getMetricFromMetricKey();
    }
    
    public int getMetricLength() {
        return metricLength_;
    }
    
    public long getMetricTimestamp() {
        return metricTimestamp_;
    }
    
    @Override
    public int getMetricTimestampInSeconds() {
        if (!isTimestampInMilliseconds_) return (int) metricTimestamp_;
        else return (int) (metricTimestamp_ / 1000);
    }
    
    @Override
    public long getMetricTimestampInMilliseconds() {
        if (isTimestampInMilliseconds_) return metricTimestamp_;
        else return (metricTimestamp_ * 1000);
    }
    
    public BigDecimal getMetricValue() {
        return metricValue_;
    }
    
    @Override
    public String getMetricValueString() {
        if (metricValue_ == null) return null;
        return MathUtilities.getFastPlainStringWithNoTrailingZeros(metricValue_);
    }
    
    @Override
    public BigDecimal getMetricValueBigDecimal() {
        return metricValue_;
    }

    public List<OpenTsdbTag> getTags() {
        return getMetricTagsFromMetricKey();
    }
    
    public boolean isTimestampInMilliseconds() {
        return isTimestampInMilliseconds_;
    }
    
    @Override
    public long getMetricReceivedTimestampInMilliseconds() {
        return metricReceivedTimestampInMilliseconds_;
    }

}
