package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.metric_formats.graphite.GraphiteMetricFormat;
import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToGraphiteThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToGraphiteThread.class.getName());
    
    private final List<? extends GraphiteMetricFormat> graphiteMetrics;
    private final String graphiteHost_;
    private final int graphitePort_;
    private final int numSendRetries_;
    private final int maxMetricsPerMessage_;
    private final String threadId_;
    
    public SendMetricsToGraphiteThread(List<? extends GraphiteMetricFormat> graphiteMetrics, String graphiteHost, int graphitePort, 
            int numSendRetries, int maxMetricsPerMessage, String threadId) {
        this.graphiteMetrics = graphiteMetrics;
        this.graphiteHost_ = graphiteHost;
        this.graphitePort_ = graphitePort;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.threadId_ = threadId;
    }

    @Override
    public void run() {
        
        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty()) {
            return;
        }
        
        long sendToGraphiteTimeStart = System.currentTimeMillis();

        boolean isSendSuccess = sendMetricsToGraphite(graphiteMetrics, graphiteHost_, graphitePort_, numSendRetries_, maxMetricsPerMessage_);

        long sendToGraphiteTimeElasped = System.currentTimeMillis() - sendToGraphiteTimeStart;

        String outputString = "ThreadId=" + threadId_ + ", Destination=" + graphiteHost_ + ":" + graphitePort_ + 
                            ", SendToGraphiteSuccess=" + isSendSuccess + ", SendToGraphiteTime=" + sendToGraphiteTimeElasped;

        logger.info(outputString);
    }

    private static boolean sendMetricsToGraphite(List<? extends GraphiteMetricFormat> graphiteMetrics, 
            String graphiteHost, int graphitePort, int numSendRetries, int maxMetricsPerMessage) {
        
        if ((graphiteMetrics == null) || graphiteMetrics.isEmpty() || (graphiteHost == null) || (graphiteHost.isEmpty()) || 
                (graphitePort < 0) || (graphitePort > 65535) || (numSendRetries < 0))  {
            return false;
        }
        
        boolean isSendAllSuccess = true;
        
        // connect to graphite
        TcpClient tcpClient = new TcpClient(graphiteHost, graphitePort, true);
        int retryCounter = 0;
        while (!tcpClient.isConnected() && (retryCounter < numSendRetries)) {
            tcpClient.reset();
            retryCounter++;
        }
        
        // if connecting to graphite failed, give up
        if (!tcpClient.isConnected()) {
            logger.error("Error creating TCP connection to graphite.");
            tcpClient.close();
            return false;
        }
        
        // build multi-metric messages & send to graphite
        int i = 0;
        StringBuilder multiMetricMessage = new StringBuilder();
        for (GraphiteMetricFormat graphiteMetric : graphiteMetrics) {
            if (i == maxMetricsPerMessage) {     
                String graphiteMessage = multiMetricMessage.toString();
                if (!graphiteMessage.isEmpty()) {   
                    boolean isSendSuccess = sendGraphiteMessage(tcpClient, numSendRetries, multiMetricMessage.toString());
                    if (!isSendSuccess) isSendAllSuccess = false;
                }

                multiMetricMessage = new StringBuilder();
                i = 0;
            }

            multiMetricMessage.append(graphiteMetric.getGraphiteFormatString()).append("\n");
            i++;
        }

        String finalGraphiteMessage = multiMetricMessage.toString();
        if (!finalGraphiteMessage.isEmpty()) {   
            boolean isSendSuccess = sendGraphiteMessage(tcpClient, numSendRetries, finalGraphiteMessage);
            if (!isSendSuccess) isSendAllSuccess = false;
        }     
     
        // disconnect from graphite
        tcpClient.close();
        
        return isSendAllSuccess;
    }
        
    private static boolean sendGraphiteMessage(TcpClient tcpClient, int numSendRetries, String graphiteMessage) {
        
        boolean isSendSuccess = true;
        
        if (tcpClient.isConnected()) {
            boolean isSendSucess = tcpClient.send(graphiteMessage, numSendRetries, false, true);

            if (!isSendSucess) {
                logger.error("Error sending message to graphite.");
                isSendSuccess = false;
            }
        }
        else {
            logger.error("Error creating TCP connection to graphite.");
            isSendSuccess = false;
        }
        
        return isSendSuccess;
    }
    
}