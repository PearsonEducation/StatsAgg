package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.HttpUtils;
import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToOpenTsdbThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToOpenTsdbThread.class.getName());
    
    private static final Map<String,String> OPENTSDB_HTTP_HEADER_PROPERTIES = getOpenTsdbHttpHeaderProperties();
    private static final Map<String,String> OPENTSDB_HTTP_HEADER_PROPERTIES_GZIP = getOpenTsdbHttpHeaderProperties_Gzip();
    
    private final List<? extends OpenTsdbMetricFormat> openTsdbMetrics_;
    private final boolean sanitizeMetrics_;
    private final String openTsdbHost_;
    private final URL openTsdbUrl_;
    private final int openTsdbPort_;
    private final int numSendRetries_;
    private final int maxMetricsPerMessage_;
    private final String threadId_;
    
    // constructor for outputting to opentsdb telnet
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, String openTsdbHost, 
            int openTsdbPort, int numSendRetries, String threadId) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.sanitizeMetrics_ = sanitizeMetrics;
        this.openTsdbHost_ = openTsdbHost;
        this.openTsdbUrl_ = null;
        this.openTsdbPort_ = openTsdbPort;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = -1;
        this.threadId_ = threadId;
    }
    
    // constructor for outputting to opentsdb http
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, URL openTsdbUrl, 
            int numSendRetries, int maxMetricsPerMessage, String threadId) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.sanitizeMetrics_ = sanitizeMetrics;
        this.openTsdbHost_ = null;
        this.openTsdbUrl_ = openTsdbUrl;
        this.openTsdbPort_ = -1;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.threadId_ = threadId;
    }
    
    @Override
    public void run() {
        
        if ((openTsdbMetrics_ == null) || openTsdbMetrics_.isEmpty()) {
            return;
        }
        
        long sendToOpenTsdbTimeStart = System.currentTimeMillis();

        boolean isSendSuccess;
        if (openTsdbHost_ != null) isSendSuccess = sendMetricsToOpenTsdb_Telnet(openTsdbMetrics_, sanitizeMetrics_, openTsdbHost_, openTsdbPort_, numSendRetries_);
        else if (openTsdbUrl_ != null) isSendSuccess = sendMetricsToOpenTsdb_HTTP(openTsdbMetrics_, sanitizeMetrics_, maxMetricsPerMessage_, openTsdbUrl_.toExternalForm(), numSendRetries_);
        else return;

        long sendToOpenTsdbTimeElasped = System.currentTimeMillis() - sendToOpenTsdbTimeStart;

        String outputString = "";         

        if (openTsdbHost_ != null) {
            outputString = "ThreadId=" + threadId_ + ", Destination=" + openTsdbHost_ + ":" + openTsdbPort_ + 
                            ", SendToOpenTsdbTelnetSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;     
        }
        else if (openTsdbUrl_ != null) {
            outputString = "ThreadId=" + threadId_ + ", Destination=\"" + openTsdbUrl_.toExternalForm() + 
                            "\", SendToOpenTsdbTelnetSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;                 
        }

        logger.info(outputString);
    }
    
    public static boolean sendMetricsToOpenTsdb_Telnet(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, 
            String openTsdbHost, int openTsdbPort, int numSendRetries) {
        
        if ((openTsdbMetrics == null) || openTsdbMetrics.isEmpty() || (openTsdbHost == null) || (openTsdbHost.isEmpty()) || 
                (openTsdbPort < 0) || (openTsdbPort > 65535) || (numSendRetries < 0))  {
            return false;
        }
        
        boolean isSendAllSuccess = true;
        
        // connect to opentsdb
        TcpClient tcpClient = new TcpClient(openTsdbHost, openTsdbPort, true);
        int retryCounter = 0;
        while (!tcpClient.isConnected() && (retryCounter < numSendRetries)) {
            tcpClient.reset();
            retryCounter++;
        }
        
        // if connecting to opentsdb failed, give up
        if (!tcpClient.isConnected()) {
            logger.error("Error creating TCP connection to graphite.");
            tcpClient.close();
            return false;
        }
        
        // send metrics to opentsdb
        for (OpenTsdbMetricFormat openTsdbMetric : openTsdbMetrics) {
            boolean isSendSucess = tcpClient.send("put " + openTsdbMetric.getOpenTsdbTelnetFormatString(sanitizeMetrics) + "\n", numSendRetries, false, false);

            if (!isSendSucess) {
                logger.error("Error sending message to OpenTSDB.");
                isSendAllSuccess = false;
            }
        }
        
        // disconnect from opentsdb
        tcpClient.close();
        
        return isSendAllSuccess;
    }
        
    public static boolean sendMetricsToOpenTsdb_HTTP(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, boolean sanitizeMetrics, 
            int maxMetricsPerMessage, String url, int numSendRetries) {
              
        if ((openTsdbMetrics == null) || openTsdbMetrics.isEmpty()) {
            return true;
        } 
        
        if ((url == null) || (maxMetricsPerMessage <= 0) || (numSendRetries < 0)) {
            return false;
        } 
        
        boolean isAllSendSuccess = true;
        
        List openTsdbMetricsList = openTsdbMetrics;
        List<List<? extends OpenTsdbMetricFormat>> partitionedList = Lists.partition(openTsdbMetricsList, maxMetricsPerMessage);
        
        for (List openTsdbMetricsPartitionedList : partitionedList) {
            String openTsdbMetricJson = OpenTsdbMetric.getOpenTsdbJson(openTsdbMetricsPartitionedList, sanitizeMetrics);
            
            if (openTsdbMetricJson != null) {
                String response = HttpUtils.httpRequest(url, OPENTSDB_HTTP_HEADER_PROPERTIES, openTsdbMetricJson, "UTF-8", "POST", numSendRetries, true);
                if (response == null) isAllSendSuccess = false;
            }
            else {
                isAllSendSuccess = false;
            }
        }
        
        return isAllSendSuccess;
    }

    private static Map<String,String> getOpenTsdbHttpHeaderProperties() {
        Map<String,String> openTsdbHttpHeaderProperties = new HashMap<>();
        
        openTsdbHttpHeaderProperties.put("Content-Type", "application/javascript");
        openTsdbHttpHeaderProperties.put("Charset", "UTF-8");
        
        return openTsdbHttpHeaderProperties;
    }
    
    private static Map<String,String> getOpenTsdbHttpHeaderProperties_Gzip() {
        Map<String,String> openTsdbHttpHeaderProperties = new HashMap<>();
        
        openTsdbHttpHeaderProperties.put("Content-Type", "application/javascript");
        openTsdbHttpHeaderProperties.put("Content-Encoding", "gzip");
        openTsdbHttpHeaderProperties.put("Charset", "UTF-8");
        
        return openTsdbHttpHeaderProperties;
    }
    
}