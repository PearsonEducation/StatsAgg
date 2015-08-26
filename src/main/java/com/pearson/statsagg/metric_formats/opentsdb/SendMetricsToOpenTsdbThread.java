package com.pearson.statsagg.metric_formats.opentsdb;

import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.pearson.statsagg.metric_formats.SendMetricsToOutputModuleThread;
import com.pearson.statsagg.utilities.HttpRequest;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToOpenTsdbThread extends SendMetricsToOutputModuleThread {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToOpenTsdbThread.class.getName());
    
    private static final Map<String,String> OPENTSDB_HTTP_HEADER_PROPERTIES = getOpenTsdbHttpHeaderProperties();
    private static final Map<String,String> OPENTSDB_HTTP_HEADER_PROPERTIES_GZIP = getOpenTsdbHttpHeaderProperties_Gzip();
    
    private final List<? extends OpenTsdbMetricFormat> openTsdbMetrics_;
    private final boolean sanitizeMetrics_;
    private final String openTsdbHost_;
    private final URL openTsdbUrl_;
    private final int openTsdbPort_;
    private final int connectTimeoutInMs_;
    private final int readTimeoutInMs_;
    private final int numSendRetries_;
    private final int maxMetricsPerMessage_;
    
    private HttpRequest currentHttpRequest_ = null;
    
    // constructor for outputting to opentsdb telnet
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, String openTsdbHost, 
            int openTsdbPort, int connectTimeoutInMs, int numSendRetries, String threadId) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.sanitizeMetrics_ = sanitizeMetrics;
        this.openTsdbHost_ = openTsdbHost;
        this.openTsdbUrl_ = null;
        this.openTsdbPort_ = openTsdbPort;
        this.connectTimeoutInMs_ = connectTimeoutInMs;
        this.readTimeoutInMs_ = -1;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = -1;
        this.threadId_ = threadId;
        
        this.outputEndpoint_ = openTsdbHost_ + ":" + openTsdbPort_;
    }
    
    // constructor for outputting to opentsdb http
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, URL openTsdbUrl, 
            int connectTimeoutInMs, int readTimeoutInMs, int numSendRetries, int maxMetricsPerMessage, String threadId) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.sanitizeMetrics_ = sanitizeMetrics;
        this.openTsdbHost_ = null;
        this.openTsdbUrl_ = openTsdbUrl;
        this.openTsdbPort_ = -1;
        this.connectTimeoutInMs_ = connectTimeoutInMs;
        this.readTimeoutInMs_ = readTimeoutInMs;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.threadId_ = threadId;
        
        if (openTsdbUrl != null) this.outputEndpoint_ = openTsdbUrl_.toExternalForm();
    }
    
    @Override
    public void run() {
        
        if (isShuttingDown_) {
            isFinished_ = true;
            return;
        }
        
        if ((openTsdbMetrics_ == null) || openTsdbMetrics_.isEmpty()) return;
        
        long sendToOpenTsdbTimeStart = System.currentTimeMillis();

        boolean isSendSuccess;
        if (openTsdbHost_ != null) isSendSuccess = sendMetricsToOpenTsdb_Telnet();
        else if (openTsdbUrl_ != null) isSendSuccess = sendMetricsToOpenTsdb_HTTP();
        else return;

        long sendToOpenTsdbTimeElasped = System.currentTimeMillis() - sendToOpenTsdbTimeStart;

        String outputString = "";         

        if (openTsdbHost_ != null) {
            outputString = "ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + 
                            "\", SendToOpenTsdbTelnetSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;     
        }
        else if (openTsdbUrl_ != null) {
            outputString = "ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + 
                            "\", SendToOpenTsdbHttpSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;                 
        }

        logger.info(outputString);
        
        isFinished_ = true;
    }
    
    @Override
    public void shutdown() {
        if (openTsdbHost_ != null) logger.warn("ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + "\", Action=ForceShutdown");
        else if (openTsdbUrl_ != null) logger.warn("ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + "\", Action=ForceShutdown");
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
    
    private boolean sendMetricsToOpenTsdb_Telnet() {

        if ((openTsdbMetrics_ == null) || openTsdbMetrics_.isEmpty()) {
            return true;
        } 
        
        if ((openTsdbHost_ == null) || (openTsdbHost_.isEmpty()) || (openTsdbPort_ < 0) || (openTsdbPort_ > 65535) || (numSendRetries_ < 0) || isShuttingDown_)  {
            return false;
        }
        
        boolean isSendAllSuccess = true;
        
        // connect to opentsdb
        TcpClient tcpClient = new TcpClient(openTsdbHost_, openTsdbPort_, true, connectTimeoutInMs_);
        int retryCounter = 0;
        while (!tcpClient.isConnected() && (retryCounter < numSendRetries_) && !isShuttingDown_) {
            tcpClient.reset();
            retryCounter++;
        }
        
        // if connecting to opentsdb failed, give up
        if (!tcpClient.isConnected()) {
            logger.error("Error creating TCP connection to OpenTSDB telnet. Endpoint=\"" + outputEndpoint_ + "\"");
            tcpClient.close();
            return false;
        }
        
        // send metrics to opentsdb
        for (OpenTsdbMetricFormat openTsdbMetric : openTsdbMetrics_) {
            if (isShuttingDown_) {
                isSendAllSuccess = false;
                continue;
            }
            
            boolean isSendSucess = tcpClient.send("put " + openTsdbMetric.getOpenTsdbTelnetFormatString(sanitizeMetrics_) + "\n", numSendRetries_, false, false);

            if (!isSendSucess) {
                logger.error("Error sending message to OpenTSDB telnet. Endpoint=\"" + outputEndpoint_ + "\"");
                isSendAllSuccess = false;
            }
        }
        
        // disconnect from opentsdb
        tcpClient.close();
        
        return isSendAllSuccess;
    }
    
    private boolean sendMetricsToOpenTsdb_HTTP() {
      
        if ((openTsdbMetrics_ == null) || openTsdbMetrics_.isEmpty()) {
            return true;
        } 
        
        if ((outputEndpoint_ == null) || (maxMetricsPerMessage_ <= 0) || (numSendRetries_ < 0) || (connectTimeoutInMs_ < 0) || (readTimeoutInMs_ < 0) || isShuttingDown_) {
            return false;
        } 
        
        boolean isAllSendSuccess = true;
        
        List openTsdbMetricsList = openTsdbMetrics_;
        List<List<? extends OpenTsdbMetricFormat>> partitionedList = Lists.partition(openTsdbMetricsList, maxMetricsPerMessage_);
                
        for (List openTsdbMetricsPartitionedList : partitionedList) {
            if (isShuttingDown_) {
                isAllSendSuccess = false;
                continue;
            }
            
            String openTsdbMetricJson = OpenTsdbMetric.getOpenTsdbJson(openTsdbMetricsPartitionedList, sanitizeMetrics_);
            
            if (openTsdbMetricJson != null) {
                HttpRequest httpRequest = new HttpRequest(outputEndpoint_, OPENTSDB_HTTP_HEADER_PROPERTIES, openTsdbMetricJson, 
                        "UTF-8", "POST", connectTimeoutInMs_, readTimeoutInMs_, numSendRetries_, true);
                
                currentHttpRequest_ = httpRequest;
                httpRequest.makeRequest();
                
                if (httpRequest.didEncounterConnectionError() && httpRequest.didHitRetryAttemptLimit() && !httpRequest.isHttpRequestSuccess()) {
                    isAllSendSuccess = false;
                    logger.error("Aborting OpenTSDB HTTP output. Couldn't connect to OpenTSDB endpoint. Endpoint=\"" + outputEndpoint_ + "\"");
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

    protected static Map<String,String> getOpenTsdbHttpHeaderProperties() {
        Map<String,String> openTsdbHttpHeaderProperties = new HashMap<>();
        
        openTsdbHttpHeaderProperties.put("Content-Type", "application/javascript");
        openTsdbHttpHeaderProperties.put("Charset", "UTF-8");
        
        return openTsdbHttpHeaderProperties;
    }
    
    protected static Map<String,String> getOpenTsdbHttpHeaderProperties_Gzip() {
        Map<String,String> openTsdbHttpHeaderProperties = new HashMap<>();
        
        openTsdbHttpHeaderProperties.put("Content-Type", "application/javascript");
        openTsdbHttpHeaderProperties.put("Content-Encoding", "gzip");
        openTsdbHttpHeaderProperties.put("Charset", "UTF-8");
        
        return openTsdbHttpHeaderProperties;
    }
    
}