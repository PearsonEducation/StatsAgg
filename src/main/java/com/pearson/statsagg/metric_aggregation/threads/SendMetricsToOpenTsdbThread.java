package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.controller.threads.SendToOpenTsdbThreadPoolManager;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.OpenTsdbHttpOutputModule;
import com.pearson.statsagg.globals.OpenTsdbTelnetOutputModule;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
import java.net.URL;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToOpenTsdbThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToOpenTsdbThread.class.getName());
    
    private final List<? extends OpenTsdbMetricFormat> openTsdbMetrics_;
    private final String openTsdbHost_;
    private final URL openTsdbUrl_;
    private final int openTsdbPort_;
    private final int numSendRetries_;
    private final String threadId_;
    private final int sendTimeWarningThreshold_;
    
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String openTsdbHost, int openTsdbPort, 
            int numSendRetries, String threadId, int sendTimeWarningThreshold) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.openTsdbHost_ = openTsdbHost;
        this.openTsdbUrl_ = null;
        this.openTsdbPort_ = openTsdbPort;
        this.numSendRetries_ = numSendRetries;
        this.threadId_ = threadId;
        this.sendTimeWarningThreshold_ = sendTimeWarningThreshold;
    }

    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, URL openTsdbUrl, int openTsdbPort, 
            int numSendRetries, String threadId, int sendTimeWarningThreshold) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.openTsdbHost_ = null;
        this.openTsdbUrl_ = openTsdbUrl;
        this.openTsdbPort_ = openTsdbPort;
        this.numSendRetries_ = numSendRetries;
        this.threadId_ = threadId;
        this.sendTimeWarningThreshold_ = sendTimeWarningThreshold;
    }
    
    @Override
    public void run() {
        
        if ((openTsdbMetrics_ != null) && !openTsdbMetrics_.isEmpty()) {
            long sendToOpenTsdbTimeStart = System.currentTimeMillis();

            boolean isSendSuccess = false;
            if (openTsdbHost_ != null) isSendSuccess = sendMetricsToOpenTsdb_Telnet(openTsdbMetrics_, openTsdbHost_, openTsdbPort_, numSendRetries_);
            else if (openTsdbUrl_ != null) isSendSuccess = sendMetricsToOpenTsdb_HTTP(openTsdbMetrics_, openTsdbUrl_, openTsdbPort_, numSendRetries_);
            else return;
            
            long sendToOpenTsdbTimeElasped = System.currentTimeMillis() - sendToOpenTsdbTimeStart;

            String outputString = "";         
            
            if (openTsdbHost_ != null) {
                outputString = "ThreadId=" + threadId_ + ", Destination=" + openTsdbHost_ + ":" + openTsdbPort_ + 
                                ", SendToOpenTsdbTelnetSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;     
            }
            else if (openTsdbUrl_ != null) {
                outputString = "ThreadId=" + threadId_ + ", Destination=" + openTsdbUrl_.getPath() + ":" + openTsdbPort_ + 
                                ", SendToOpenTsdbTelnetSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;                 
            }
                
            if (sendToOpenTsdbTimeElasped < sendTimeWarningThreshold_) {
                logger.info(outputString);
            }
            else {
                logger.warn(outputString);
            }
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
            boolean isSendSucess = tcpClient.send("put " + openTsdbMetric.getOpenTsdbFormatString() + "\n", numSendRetries, false, false);

            if (!isSendSucess) {
                logger.error("Error sending message to OpenTSDB.");
                isSendAllSuccess = false;
            }
        }
        
        // disconnect from opentsdb
        tcpClient.close();
        
        return isSendAllSuccess;
    }
        
    public static boolean sendMetricsToOpenTsdb_HTTP(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, URL openTsdbUrl, int openTsdbPort, int numSendRetries) {
        
        if ((openTsdbMetrics == null) || openTsdbMetrics.isEmpty() || (openTsdbUrl == null) || (openTsdbPort < 0) || (openTsdbPort > 65535) || (numSendRetries < 0))  {
            return false;
        }
        
        boolean isSendAllSuccess = true;
        
//        try {
//            String urlParameters = "";
//            byte[] postData = urlParameters.getBytes(Charset.forName("UTF-8"));
//            int postDataLength = postData.length;
//
//            HttpURLConnection httpUrlConnection = (HttpURLConnection) openTsdbUrl.openConnection();
//            httpUrlConnection.setDoOutput(true);
//            httpUrlConnection.setDoInput(true);
//            httpUrlConnection.setInstanceFollowRedirects(false);
//            httpUrlConnection.setRequestMethod("POST");
//                        
//            httpUrlConnection.setRequestProperty("charset", "utf-8");
//            httpUrlConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
//            httpUrlConnection.setUseCaches(false);
//            
//            try (DataOutputStream wr = new DataOutputStream(httpUrlConnection.getOutputStream())) {
//                wr.write(postData);
//            }
//        }
//        catch (Exception e) {
//            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
//        }

        return isSendAllSuccess;
    }
    
    public static void sendMetricsToOpenTsdbTelnetEndpoints(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String threadId) {
        
        try {
            List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
            if (openTsdbTelnetOutputModules == null) return;
                    
            for (OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule : openTsdbTelnetOutputModules) {
                if (!openTsdbTelnetOutputModule.isOutputEnabled()) continue;
                
                SendMetricsToOpenTsdbThread sendMetricsToTelnetOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, openTsdbTelnetOutputModule.getHost(), 
                       openTsdbTelnetOutputModule.getPort(), openTsdbTelnetOutputModule.getNumSendRetryAttempts(), threadId, (int) ApplicationConfiguration.getFlushTimeAgg());
                
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
                       openTsdbHttpOutputModule.getPort(), openTsdbHttpOutputModule.getNumSendRetryAttempts(), threadId, (int) ApplicationConfiguration.getFlushTimeAgg());
                
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
    
}