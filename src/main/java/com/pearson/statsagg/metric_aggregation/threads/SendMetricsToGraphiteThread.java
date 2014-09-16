package com.pearson.statsagg.metric_aggregation.threads;

import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToGraphiteThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToGraphiteThread.class.getName());
    
    private final List<String> outputMessagesForGraphite_;
    private final String graphiteHost_;
    private final int graphitePort_;
    private final int numSendRetries_;
    private final String threadId_;
    private final int sendTimeWarningThreshold_;
    
    public SendMetricsToGraphiteThread(List<String> outputMessagesForGraphite, String graphiteHost, int graphitePort, 
            int numSendRetries, String threadId, int sendTimeWarningThreshold) {
        this.outputMessagesForGraphite_ = outputMessagesForGraphite;
        this.graphiteHost_ = graphiteHost;
        this.graphitePort_ = graphitePort;
        this.numSendRetries_ = numSendRetries;
        this.threadId_ = threadId;
        this.sendTimeWarningThreshold_ = sendTimeWarningThreshold;
    }

    @Override
    public void run() {
        
        if (outputMessagesForGraphite_ != null && !outputMessagesForGraphite_.isEmpty()) {
            long sendToGraphiteTimeStart = System.currentTimeMillis();

            boolean isSendSuccess = sendMetricsToGraphite(outputMessagesForGraphite_, graphiteHost_, graphitePort_, numSendRetries_);

            long sendToGraphiteTimeElasped = System.currentTimeMillis() - sendToGraphiteTimeStart;

            String outputString = "ThreadId=" + threadId_ + ", SendToGraphiteSuccess=" + isSendSuccess + ", SendToGraphiteTime=" + sendToGraphiteTimeElasped;
            if (sendToGraphiteTimeElasped < sendTimeWarningThreshold_) {
                logger.info(outputString);
            }
            else {
                logger.warn(outputString);
            }
        }
        
    }
    
    public static boolean sendMetricsToGraphite(List<String> outputMessagesForGraphite, String graphiteHost, int graphitePort, int numSendRetries) {
        
        if ((outputMessagesForGraphite == null) || outputMessagesForGraphite.isEmpty() || (graphiteHost == null) || (graphiteHost.isEmpty()) || 
                (graphitePort < 0) || (graphitePort > 65535) || (numSendRetries < 0))  {
            return false;
        }
        
        boolean isSendAllSuccess = true;
        
        TcpClient tcpClient = new TcpClient(graphiteHost, graphitePort, true);

        int retryCounter = 0;
        while (!tcpClient.isConnected() && (retryCounter < numSendRetries)) {
            tcpClient.reset();
            retryCounter++;
        }
        
        if (tcpClient.isConnected()) {
            for (String outputMessageForGraphite : outputMessagesForGraphite) {
                boolean isSendSucess = tcpClient.send(outputMessageForGraphite, numSendRetries, false, false);

                if (!isSendSucess) {
                    logger.error("Error sending message to graphite.");
                    isSendAllSuccess = false;
                }
            }
        }
        else {
            logger.error("Error creating TCP connection to graphite.");
            isSendAllSuccess = false;
        }

        tcpClient.close();
        
        return isSendAllSuccess;
    }
        
}