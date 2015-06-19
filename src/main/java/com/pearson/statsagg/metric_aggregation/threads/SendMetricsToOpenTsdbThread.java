package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.controller.threads.SendToOpenTsdbThreadPoolManager;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.OpenTsdbHttpOutputModule;
import com.pearson.statsagg.globals.OpenTsdbTelnetOutputModule;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.HttpUtils;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
import java.net.URL;
import java.util.ArrayList;
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
    private final String openTsdbHost_;
    private final URL openTsdbUrl_;
    private final int openTsdbPort_;
    private final int numSendRetries_;
    private final int maxMetricsPerMessage_;
    private final String threadId_;
    
    // constructor for outputting to opentsdb telnet
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String openTsdbHost, int openTsdbPort, int numSendRetries, String threadId) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.openTsdbHost_ = openTsdbHost;
        this.openTsdbUrl_ = null;
        this.openTsdbPort_ = openTsdbPort;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = -1;
        this.threadId_ = threadId;
    }
    
    // constructor for outputting to opentsdb http
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, URL openTsdbUrl, int numSendRetries, int maxMetricsPerMessage, String threadId) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.openTsdbHost_ = null;
        this.openTsdbUrl_ = openTsdbUrl;
        this.openTsdbPort_ = -1;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.threadId_ = threadId;
    }
    
    @Override
    public void run() {
        
        if ((openTsdbMetrics_ != null) && !openTsdbMetrics_.isEmpty()) {
            long sendToOpenTsdbTimeStart = System.currentTimeMillis();

            boolean isSendSuccess;
            if (openTsdbHost_ != null) isSendSuccess = sendMetricsToOpenTsdb_Telnet(openTsdbMetrics_, openTsdbHost_, openTsdbPort_, numSendRetries_);
            else if (openTsdbUrl_ != null) isSendSuccess = sendMetricsToOpenTsdb_HTTP(openTsdbMetrics_, maxMetricsPerMessage_, openTsdbUrl_.toExternalForm(), numSendRetries_);
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
        
    }
    
    public static boolean sendMetricsToOpenTsdb_Telnet(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String openTsdbHost, int openTsdbPort, int numSendRetries) {
        
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
            boolean isSendSucess = tcpClient.send("put " + openTsdbMetric.getOpenTsdbTelnetFormatString() + "\n", numSendRetries, false, false);

            if (!isSendSucess) {
                logger.error("Error sending message to OpenTSDB.");
                isSendAllSuccess = false;
            }
        }
        
        // disconnect from opentsdb
        tcpClient.close();
        
        return isSendAllSuccess;
    }
        
    public static boolean sendMetricsToOpenTsdb_HTTP(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, int maxMetricsPerMessage, String url, int numSendRetries) {
              
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
            String openTsdbMetricJson = OpenTsdbMetric.getOpenTsdbJson(openTsdbMetricsPartitionedList);
            
            if (openTsdbMetricJson != null) {
                String response = HttpUtils.httpRequest(url, OPENTSDB_HTTP_HEADER_PROPERTIES, openTsdbMetricJson, "UTF-8", "POST", numSendRetries);
                if (response == null) isAllSendSuccess = false;
            }
            else {
                isAllSendSuccess = false;
            }
        }
        
        return isAllSendSuccess;
    }
    
    public static void sendMetricsToOpenTsdbTelnetEndpoints(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String threadId) {
        
        try {
            List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
            if (openTsdbTelnetOutputModules == null) return;
                    
            for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
                if (!openTsdbTelnetOutputModule.isOutputEnabled()) continue;
                
                SendMetricsToOpenTsdbThread sendMetricsToTelnetOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, openTsdbTelnetOutputModule.getHost(), 
                       openTsdbTelnetOutputModule.getPort(), openTsdbTelnetOutputModule.getNumSendRetryAttempts(), threadId);
                
                SendToOpenTsdbThreadPoolManager.executeThread(sendMetricsToTelnetOpenTsdbThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static void sendMetricsToOpenTsdbHttpEndpoints(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String threadId) {
        
        try {
            List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = ApplicationConfiguration.getOpenTsdbHttpOutputModules();
            if (openTsdbHttpOutputModules == null) return;
                    
            for (OpenTsdbHttpOutputModule openTsdbHttpOutputModule : openTsdbHttpOutputModules) {
                if (!openTsdbHttpOutputModule.isOutputEnabled()) continue;
                
                URL url = new URL(openTsdbHttpOutputModule.getUrl());
                
                SendMetricsToOpenTsdbThread sendMetricsToHttpOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, url, 
                       openTsdbHttpOutputModule.getNumSendRetryAttempts(), openTsdbHttpOutputModule.getMaxMetricsPerMessage(), threadId);
                
                SendToOpenTsdbThreadPoolManager.executeThread(sendMetricsToHttpOpenTsdbThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static boolean isAnyOpenTsdbTelnetOutputModuleEnabled() {
        
        List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
        if (openTsdbTelnetOutputModules == null) return false;
        
        for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
            if (openTsdbTelnetOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<OpenTsdbTelnetOutputModule> getEnabledOpenTsdbTelnetOutputModules() {
        
        List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
        if (openTsdbTelnetOutputModules == null) return new ArrayList<>();
        
        List<OpenTsdbTelnetOutputModule> enabledOpenTsdbOutputModules = new ArrayList<>();
        
        for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
            if (openTsdbTelnetOutputModule.isOutputEnabled()) {
                enabledOpenTsdbOutputModules.add(openTsdbTelnetOutputModule);
            }
        }
        
        return enabledOpenTsdbOutputModules;
    }
    
    public static boolean isAnyOpenTsdbHttpOutputModuleEnabled() {
        
        List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = ApplicationConfiguration.getOpenTsdbHttpOutputModules();
        if (openTsdbHttpOutputModules == null) return false;
        
        for (OpenTsdbHttpOutputModule openTsdbHttpOutputModule : openTsdbHttpOutputModules) {
            if (openTsdbHttpOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<OpenTsdbHttpOutputModule> getEnabledOpenTsdbHttpOutputModules() {
        
        List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = ApplicationConfiguration.getOpenTsdbHttpOutputModules();
        if (openTsdbHttpOutputModules == null) return new ArrayList<>();
        
        List<OpenTsdbHttpOutputModule> enabledOpenTsdbOutputModules = new ArrayList<>();
        
        for (OpenTsdbHttpOutputModule openTsdbHttpOutputModule : openTsdbHttpOutputModules) {
            if (openTsdbHttpOutputModule.isOutputEnabled()) {
                enabledOpenTsdbOutputModules.add(openTsdbHttpOutputModule);
            }
        }
        
        return enabledOpenTsdbOutputModules;
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