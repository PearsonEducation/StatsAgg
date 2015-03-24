package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.controller.threads.SendToOpenTsdbThreadPoolManager;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.OpenTsdbTelnetOutputModule;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
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
    private final int openTsdbPort_;
    private final int numSendRetries_;
    private final String threadId_;
    private final int sendTimeWarningThreshold_;
    
    public SendMetricsToOpenTsdbThread(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String openTsdbHost, int openTsdbPort, 
            int numSendRetries, String threadId, int sendTimeWarningThreshold) {
        this.openTsdbMetrics_ = openTsdbMetrics;
        this.openTsdbHost_ = openTsdbHost;
        this.openTsdbPort_ = openTsdbPort;
        this.numSendRetries_ = numSendRetries;
        this.threadId_ = threadId;
        this.sendTimeWarningThreshold_ = sendTimeWarningThreshold;
    }

    @Override
    public void run() {
        
        if (openTsdbMetrics_ != null && !openTsdbMetrics_.isEmpty()) {
            long sendToOpenTsdbTimeStart = System.currentTimeMillis();

            boolean isSendSuccess = sendMetricsToOpenTsdb_Telnet(openTsdbMetrics_, openTsdbHost_, openTsdbPort_, numSendRetries_);

            long sendToOpenTsdbTimeElasped = System.currentTimeMillis() - sendToOpenTsdbTimeStart;

            String outputString = "ThreadId=" + threadId_ + ", Destination=" + openTsdbHost_ + ":" + openTsdbPort_ + 
                                ", SendToOpenTsdbTelnetSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;            
            
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
        
    public static void sendMetricsToOpenTsdbEndpoints(List<? extends OpenTsdbMetricFormat> openTsdbMetrics, String threadId) {
        
        try {
            
            List<OpenTsdbTelnetOutputModule> openTsdbOutuputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
            if (openTsdbOutuputModules == null) return;
                    
            for (OpenTsdbTelnetOutputModule openTsdbOutputModule : openTsdbOutuputModules) {
                if (!openTsdbOutputModule.isOutputEnabled()) continue;
                
                SendMetricsToOpenTsdbThread sendMetricsToOpenTsdbThread = new SendMetricsToOpenTsdbThread(openTsdbMetrics, openTsdbOutputModule.getHost(), 
                       openTsdbOutputModule.getPort(), openTsdbOutputModule.getNumSendRetryAttempts(), threadId, (int) ApplicationConfiguration.getFlushTimeAgg());
                
                SendToOpenTsdbThreadPoolManager.executeThread(sendMetricsToOpenTsdbThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static boolean isAnyOpenTsdbOutputModuleEnabled() {
        
        List<OpenTsdbTelnetOutputModule> openTsdbOutuputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
        if (openTsdbOutuputModules == null) return false;
        
        for (OpenTsdbTelnetOutputModule openTsdbOutputModule : openTsdbOutuputModules) {
            if (openTsdbOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<OpenTsdbTelnetOutputModule> getEnabledOpenTsdbOutputModules() {
        
        List<OpenTsdbTelnetOutputModule> openTsdbOutuputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
        if (openTsdbOutuputModules == null) return new ArrayList<>();
        
        List<OpenTsdbTelnetOutputModule> enabledOpenTsdbOutputModules = new ArrayList<>();
        
        for (OpenTsdbTelnetOutputModule openTsdbOutputModule : openTsdbOutuputModules) {
            if (openTsdbOutputModule.isOutputEnabled()) {
                enabledOpenTsdbOutputModules.add(openTsdbOutputModule);
            }
        }
        
        return enabledOpenTsdbOutputModules;
    }
    
}