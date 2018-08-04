package com.pearson.statsagg.metric_formats.graphite;

import com.pearson.statsagg.metric_formats.SendMetricsToOutputModuleThread;
import java.util.List;
import com.pearson.statsagg.utilities.network_utils.TcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToGraphiteThread extends SendMetricsToOutputModuleThread {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToGraphiteThread.class.getName());
    
    private final List<? extends GraphiteMetricFormat> graphiteMetrics_;
    private final boolean sanitizeMetrics_;
    private final boolean substituteCharacters_;
    private final String graphiteHost_;
    private final int graphitePort_;
    private final int connectTimeoutInMs_;
    private final int numSendRetries_;
    private final int maxMetricsPerMessage_;
    
    public SendMetricsToGraphiteThread(List<? extends GraphiteMetricFormat> graphiteMetrics, boolean sanitizeMetrics, boolean substituteCharacters,
            String graphiteHost, int graphitePort, int connectTimeoutInMs, int numSendRetries, int maxMetricsPerMessage, String threadId) {
        this.graphiteMetrics_ = graphiteMetrics;
        this.sanitizeMetrics_ = sanitizeMetrics;
        this.substituteCharacters_ = substituteCharacters;
        this.graphiteHost_ = graphiteHost;
        this.graphitePort_ = graphitePort;
        this.connectTimeoutInMs_ = connectTimeoutInMs;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.threadId_ = threadId;
        
        this.outputEndpoint_ = graphiteHost_ + ":" + graphitePort_;
    }

    @Override
    public void run() {
        
        if (isShuttingDown_) {
            isFinished_ = true;
            return;
        }
        
        if ((graphiteMetrics_ == null) || graphiteMetrics_.isEmpty()) return;
        
        long sendToGraphiteTimeStart = System.currentTimeMillis();

        boolean isSendSuccess = sendMetricsToGraphite();

        long sendToGraphiteTimeElasped = System.currentTimeMillis() - sendToGraphiteTimeStart;

        String outputString = "ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + "\"" +
                            ", SendToGraphiteSuccess=" + isSendSuccess + ", SendToGraphiteTime=" + sendToGraphiteTimeElasped;

        logger.info(outputString);
        
        isFinished_ = true;
    }
    
    @Override
    public void shutdown() {
        logger.warn("ThreadId=" + threadId_ + ", Destination=\"" + outputEndpoint_ + "\", Action=ForceShutdown");
        isShuttingDown_ = true;
    }
    
    @Override
    public boolean isFinished() {
        return isFinished_;
    }
    
    private boolean sendMetricsToGraphite() {
        
        if ((graphiteMetrics_ == null) || graphiteMetrics_.isEmpty() || (graphiteHost_ == null) || (graphiteHost_.isEmpty()) || 
                (graphitePort_ < 0) || (graphitePort_ > 65535) || (numSendRetries_ < 0) || isShuttingDown_)  {
            return false;
        }
        
        boolean isSendAllSuccess = true;
        
        // connect to graphite
        TcpClient tcpClient = new TcpClient(graphiteHost_, graphitePort_, true, connectTimeoutInMs_);
        int retryCounter = 0;
        while (!tcpClient.isConnected() && (retryCounter < numSendRetries_) && !isShuttingDown_) {
            tcpClient.reset();
            retryCounter++;
        }
        
        // if connecting to graphite failed, give up
        if (!tcpClient.isConnected()) {
            logger.error("Error creating TCP connection to Graphite. Endpoint=\"" + outputEndpoint_ + "\"");
            tcpClient.close();
            return false;
        }
        
        // build multi-metric messages & send to graphite
        int i = 0;
        StringBuilder multiMetricMessage = new StringBuilder();
        for (GraphiteMetricFormat graphiteMetric : graphiteMetrics_) {
            if (isShuttingDown_) {
                isSendAllSuccess = false;
                continue;
            }
            
            if (i == maxMetricsPerMessage_) {     
                String graphiteMessage = multiMetricMessage.toString();
                if (!graphiteMessage.isEmpty()) {   
                    boolean isSendSuccess = sendGraphiteMessage(tcpClient, numSendRetries_, multiMetricMessage.toString());
                    if (!isSendSuccess) isSendAllSuccess = false;
                }

                multiMetricMessage = new StringBuilder();
                i = 0;
            }

            multiMetricMessage.append(graphiteMetric.getGraphiteFormatString(sanitizeMetrics_, substituteCharacters_)).append("\n");
            i++;
        }

        String finalGraphiteMessage = multiMetricMessage.toString();
        if (!finalGraphiteMessage.isEmpty()) {   
            if (!isShuttingDown_) {
                boolean isSendSuccess = sendGraphiteMessage(tcpClient, numSendRetries_, finalGraphiteMessage);
                if (!isSendSuccess) isSendAllSuccess = false;
            }
            else {
                isSendAllSuccess = false;
            }
        }     
     
        // disconnect from graphite
        tcpClient.close();
        
        return isSendAllSuccess;
    }
        
    private boolean sendGraphiteMessage(TcpClient tcpClient, int numSendRetries, String graphiteMessage) {
        
        boolean isSendSuccess = true;
        
        if (tcpClient.isConnected()) {
            boolean isSendSucess = tcpClient.send(graphiteMessage, numSendRetries, false, true);

            if (!isSendSucess) {
                logger.error("Error sending a message to Graphite. Endpoint=\"" + outputEndpoint_ + "\"");
                isSendSuccess = false;
            }
        }
        else {
            logger.error("Error creating TCP connection to Graphite. Endpoint=\"" + outputEndpoint_ + "\"");
            isSendSuccess = false;
        }
        
        return isSendSuccess;
    }
    
}