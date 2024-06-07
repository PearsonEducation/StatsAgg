package com.pearson.statsagg.threads.output;

import com.pearson.statsagg.utilities.web_utils.HttpUtils;
import java.util.List;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetricFormat_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.utilities.web_utils.HttpRequest;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToInfluxdbV1Thread extends SendMetricsToOutputModuleThread {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToInfluxdbV1Thread.class.getName());
        
    private final List<InfluxdbMetric_v1> nativeInfluxdbMetrics_;
    private final List<? extends InfluxdbMetricFormat_v1> influxdbMetrics_;
    private final URL influxdbBaseUrl_;
    private final String defaultDatabaseHttpAuthValue_;
    private final String defaultDatabaseName_;
    private final int connectTimeoutInMs_;
    private final int readTimeoutInMs_;
    private final int numSendRetries_;
    private final int maxMetricsPerMessage_;
    private final boolean isNativeInfluxdbMetrics;
    
    private HttpRequest currentHttpRequest_ = null;

    /* 
    When using this constructor, running this thread will output to the 'default' InfluxDB database name, username, and password (specified in the application configuration)
    Generally speaking, this is intended to be used for Graphite, StatsD, and OpenTSDB formatted metrics
    */
    public SendMetricsToInfluxdbV1Thread(List<? extends InfluxdbMetricFormat_v1> influxdbMetrics, URL influxdbBaseUrl, 
            String defaultDatabaseName, String defaultDatabaseHttpAuthValue, 
            int connectTimeoutInMs, int readTimeoutInMs, int numSendRetries, int maxMetricsPerMessage, String threadId) {
        this.nativeInfluxdbMetrics_ = null;
        this.influxdbMetrics_ = influxdbMetrics;
        this.influxdbBaseUrl_ = influxdbBaseUrl;
        this.defaultDatabaseName_ = defaultDatabaseName;
        this.defaultDatabaseHttpAuthValue_ = defaultDatabaseHttpAuthValue;
        this.connectTimeoutInMs_ = connectTimeoutInMs;
        this.readTimeoutInMs_ = readTimeoutInMs;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.isNativeInfluxdbMetrics = false;
        this.threadId_ = threadId;
        
        if (influxdbBaseUrl_ != null) this.outputEndpoint_ = influxdbBaseUrl_.toExternalForm();
    }
    
    /* 
    When using this constructor, running this thread will output to the InfluxDB that was originally received (and stored in a InfluxdbMetric_v1 object)
    Only 'native' InfluxDB metrics can use this constructor (InfluxdbMetric_v1 objects)
    */
    public SendMetricsToInfluxdbV1Thread(List<InfluxdbMetric_v1> influxdbMetrics, URL influxdbBaseUrl, 
            int connectTimeoutInMs, int readTimeoutInMs, int numSendRetries, String threadId) {
        this.nativeInfluxdbMetrics_ = influxdbMetrics;
        this.influxdbMetrics_ = null;
        this.influxdbBaseUrl_ = influxdbBaseUrl;
        this.defaultDatabaseName_ = null;
        this.defaultDatabaseHttpAuthValue_ = null;
        this.connectTimeoutInMs_ = connectTimeoutInMs;
        this.readTimeoutInMs_ = readTimeoutInMs;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = -1;
        this.isNativeInfluxdbMetrics = true;
        this.threadId_ = threadId;
        
        if (influxdbBaseUrl_ != null) this.outputEndpoint_ = influxdbBaseUrl_.toExternalForm();
    }
    
    @Override
    public void run() {
        
        if (isShuttingDown_) {
            isFinished_ = true;
            return;
        }
        
        if (isNativeInfluxdbMetrics && ((nativeInfluxdbMetrics_ == null) || nativeInfluxdbMetrics_.isEmpty())) return;
        if (!isNativeInfluxdbMetrics && ((influxdbMetrics_ == null) || influxdbMetrics_.isEmpty())) return;

        long sendToInfluxdbTimeStart = System.currentTimeMillis();
        boolean isSendSuccess;
        
        if (isNativeInfluxdbMetrics) {
            isSendSuccess = sendNativeInfluxdbMetricsToInfluxdb_HTTP();
        }
        else {
            Map<String,String> influxdbHttpHeaderProperties = getInfluxdbHttpHeaderProperties(defaultDatabaseHttpAuthValue_);
            isSendSuccess = sendNonNativeMetricsToInfluxdb_HTTP(influxdbHttpHeaderProperties);
        }

        long sendToInfluxdbTimeElasped = System.currentTimeMillis() - sendToInfluxdbTimeStart;

        String outputString = "ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + 
                            "\", SendToInfluxdbHttpSuccess=" + isSendSuccess + ", SendToInfluxdbTime=" + sendToInfluxdbTimeElasped;                 

        logger.info(outputString);
        
        isFinished_ = true;
    }
    
    @Override
    public void shutdown() {
        logger.warn("ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + "\", Action=ForceShutdown");
        isShuttingDown_ = true;
        
        try {
            if (currentHttpRequest_ != null) {
                currentHttpRequest_.setContinueRetrying(false);
                currentHttpRequest_.closeResources();
                currentHttpRequest_ = null;
            }
        }
        catch (Exception e) {}
    }

    @Override
    public boolean isFinished() {
        return isFinished_;
    }
    
    /*
    Merges several metrics together & sends to InfluxDB in larger, multi-metric, HTTP POSTs.
    This assumes that all metrics are not native InfluxDB metrics (ex -- Graphite, OpenTSDB, etc).
    */
    private boolean sendNonNativeMetricsToInfluxdb_HTTP(Map<String,String> influxdbHttpHeaderProperties) {
              
        if ((influxdbMetrics_ == null) || influxdbMetrics_.isEmpty()) {
            return true;
        } 
        
        if ((influxdbBaseUrl_ == null) || (maxMetricsPerMessage_ <= 0) || (numSendRetries_ < 0) || (connectTimeoutInMs_ < 0) || (readTimeoutInMs_ < 0) || isShuttingDown_) {
            return false;
        } 
        
        boolean isAllSendSuccess = true;
        
        List influxdbMetricsList = influxdbMetrics_;
        List<List<? extends InfluxdbMetricFormat_v1>> partitionedList = Lists.partition(influxdbMetricsList, maxMetricsPerMessage_);
        
        for (List influxdbStandardizedMetricsPartitionedList : partitionedList) {
            if (isShuttingDown_) {
                isAllSendSuccess = false;
                continue;
            }
            
            String influxdbMetricJson = InfluxdbMetric_v1.getInfluxdbJson(influxdbStandardizedMetricsPartitionedList);
            
            if (influxdbMetricJson != null) {
                String influxdbFullUrl = createInfluxdbUrl(outputEndpoint_, defaultDatabaseName_);
                
                HttpRequest httpRequest = new HttpRequest(influxdbFullUrl, influxdbHttpHeaderProperties, influxdbMetricJson, 
                        "UTF-8", "POST", connectTimeoutInMs_, readTimeoutInMs_, numSendRetries_, true);
                
                currentHttpRequest_ = httpRequest;
                httpRequest.makeRequest();
                
                if (httpRequest.didEncounterConnectionError() && httpRequest.didHitRetryAttemptLimit() && !httpRequest.isHttpRequestSuccess()) {
                    isAllSendSuccess = false;
                    logger.error("Aborting InfluxDB V1 output. Couldn't connect to InfluxDB HTTP endpoint. Endpoint=\"" + outputEndpoint_ + "\"");
                    break;
                }
                
                if (!httpRequest.isHttpRequestSuccess()) isAllSendSuccess = false;
            }
            else {
                isAllSendSuccess = false;
            }
        }
        
        return isAllSendSuccess;
    }
    
    /*
    Sends each InfluxDB metric to InfluxDB as separate HTTP POSTs.
    */
    private boolean sendNativeInfluxdbMetricsToInfluxdb_HTTP() {
              
        if ((nativeInfluxdbMetrics_ == null) || nativeInfluxdbMetrics_.isEmpty()) {
            return true;
        } 
        
        if ((influxdbBaseUrl_ == null) || (numSendRetries_ < 0) || (connectTimeoutInMs_ < 0) || (readTimeoutInMs_ < 0) || isShuttingDown_) {
            return false;
        } 
        
        boolean isAllSendSuccess = true;

        for (InfluxdbMetric_v1 influxdbMetric : nativeInfluxdbMetrics_) {
            if (isShuttingDown_) {
                isAllSendSuccess = false;
                continue;
            }
            
            String influxdbMetricJson = InfluxdbMetric_v1.getInfluxdbJson(influxdbMetric);
            
            if (influxdbMetricJson != null) {
                String influxdbFullUrl = createInfluxdbUrl(influxdbMetric, outputEndpoint_);
                Map<String,String> influxdbHttpHeaderProperties = getInfluxdbHttpHeaderProperties(influxdbMetric);
                
                HttpRequest httpRequest = new HttpRequest(influxdbFullUrl, influxdbHttpHeaderProperties, influxdbMetricJson, 
                        "UTF-8", "POST", connectTimeoutInMs_, readTimeoutInMs_, numSendRetries_, true);
                
                currentHttpRequest_ = httpRequest;
                httpRequest.makeRequest();
                
                if (httpRequest.didEncounterConnectionError() && httpRequest.didHitRetryAttemptLimit() && !httpRequest.isHttpRequestSuccess()) {
                    isAllSendSuccess = false;
                    logger.error("Aborting InfluxDB V1 output. Couldn't connect to InfluxDB HTTP endpoint. Endpoint=\"" + outputEndpoint_ + "\"");
                    break;
                }
                
                if (!httpRequest.isHttpRequestSuccess()) isAllSendSuccess = false;
            }
            else {
                isAllSendSuccess = false;
            }
        }
        
        return isAllSendSuccess;
    }
    
    /*
    Intended for use with non-native InfluxDB metrics (ex -- Graphite, OpenTSDB, etc)
    */
    protected static String createInfluxdbUrl(String baseUrl, String databaseName) {
     
        if ((databaseName == null) || (baseUrl == null)) {
            return null;
        }
        
        StringBuilder influxdbUrl = new StringBuilder();
        
        // create the base url
        influxdbUrl.append(baseUrl);
        if (!baseUrl.endsWith("/")) influxdbUrl.append("/");
        
        // add the influxdb database name to the url
        influxdbUrl.append("db/").append(HttpUtils.urlEncode(databaseName, "UTF-8")).append("/series");

        return influxdbUrl.toString();
    }
    
    /*
    Intended for use with native InfluxDB metrics
    */
    protected static String createInfluxdbUrl(InfluxdbMetric_v1 influxdbMetric, String baseUrl) {
     
        if ((influxdbMetric == null) || (baseUrl == null)) {
            return null;
        }
        
        StringBuilder influxdbUrl = new StringBuilder();
        
        // create the base uri
        influxdbUrl.append(baseUrl);
        if (!baseUrl.endsWith("/")) influxdbUrl.append("/");
        
        // add the influxdb database name to the url
        influxdbUrl.append("db/").append((HttpUtils.urlEncode(influxdbMetric.getDatabase(), "UTF-8"))).append("/series");

        // create the influxdb url query string parameters
        if ((influxdbMetric.getUsername() != null) || (influxdbMetric.getPassword() != null) || 
                (influxdbMetric.getTimePrecisionCode() != com.pearson.statsagg.metric_formats.influxdb.Common.TIMESTAMP_PRECISION_UNKNOWN)) {
            influxdbUrl.append("?");
        }

        if (influxdbMetric.getUsername() != null) influxdbUrl.append("u=").append(HttpUtils.urlEncode(influxdbMetric.getUsername(), "UTF-8"));
        if ((influxdbMetric.getUsername() != null) && (influxdbMetric.getPassword() != null)) influxdbUrl.append("&");
        if (influxdbMetric.getPassword() != null) influxdbUrl.append("p=").append(HttpUtils.urlEncode(influxdbMetric.getPassword(), "UTF-8"));

        if (((influxdbMetric.getUsername() != null) || (influxdbMetric.getPassword() != null)) && 
                (influxdbMetric.getTimePrecisionCode() != com.pearson.statsagg.metric_formats.influxdb.Common.TIMESTAMP_PRECISION_UNKNOWN)) {
            influxdbUrl.append("&");
        }

        if (influxdbMetric.getTimePrecisionCode() != com.pearson.statsagg.metric_formats.influxdb.Common.TIMESTAMP_PRECISION_UNKNOWN) {
            String timePrecisionString = InfluxdbMetric_v1.getTimePrecisionStringFromTimePrecisionCode(influxdbMetric.getTimePrecisionCode());
            if (timePrecisionString != null) influxdbUrl.append("time_precision=").append(timePrecisionString);
        }

        return influxdbUrl.toString();
    }
    
    /*
    Intended for use with non-native InfluxDB metrics (ex -- Graphite, OpenTSDB, etc)
    */
    protected static Map<String,String> getInfluxdbHttpHeaderProperties(String defaultDatabaseHttpAuthValue) {
        Map<String,String> influxdbHttpHeaderProperties = new HashMap<>();
        
        if (defaultDatabaseHttpAuthValue != null) influxdbHttpHeaderProperties.put("Authorization", defaultDatabaseHttpAuthValue);         
        influxdbHttpHeaderProperties.put("Content-Type", "application/javascript");
        influxdbHttpHeaderProperties.put("Charset", "UTF-8");
        
        return influxdbHttpHeaderProperties;
    }
    
    /*
    Intended for use with native InfluxDB metrics
    */
    protected static Map<String,String> getInfluxdbHttpHeaderProperties(InfluxdbMetric_v1 influxdbMetric) {
        Map<String,String> influxdbHttpHeaderProperties = new HashMap<>();
        
        if ((influxdbMetric != null) && (influxdbMetric.getBasicAuth() != null)) influxdbHttpHeaderProperties.put("Authorization", influxdbMetric.getBasicAuth());         
        influxdbHttpHeaderProperties.put("Content-Type", "application/javascript");
        influxdbHttpHeaderProperties.put("Charset", "UTF-8");
        
        return influxdbHttpHeaderProperties;
    }
    
}