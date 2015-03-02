package com.pearson.statsagg.metric_aggregation.threads;

import java.util.List;
import com.pearson.statsagg.utilities.TcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToOpenTsdbThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToOpenTsdbThread.class.getName());
    
    private final List<String> outputMessagesForOpenTsdb_;
    private final String openTsdbHost_;
    private final int openTsdbPort_;
    private final int numSendRetries_;
    private final String threadId_;
    private final int sendTimeWarningThreshold_;
    
    public SendMetricsToOpenTsdbThread(List<String> outputMessagesForOpenTsdb, String openTsdbHost, int openTsdbPort, 
            int numSendRetries, String threadId, int sendTimeWarningThreshold) {
        this.outputMessagesForOpenTsdb_ = outputMessagesForOpenTsdb;
        this.openTsdbHost_ = openTsdbHost;
        this.openTsdbPort_ = openTsdbPort;
        this.numSendRetries_ = numSendRetries;
        this.threadId_ = threadId;
        this.sendTimeWarningThreshold_ = sendTimeWarningThreshold;
    }

    @Override
    public void run() {
        
        if (outputMessagesForOpenTsdb_ != null && !outputMessagesForOpenTsdb_.isEmpty()) {
            long sendToOpenTsdbTimeStart = System.currentTimeMillis();

            boolean isSendSuccess = sendMetricsToOpenTsdb_Telnet(outputMessagesForOpenTsdb_, openTsdbHost_, openTsdbPort_, numSendRetries_);

            long sendToOpenTsdbTimeElasped = System.currentTimeMillis() - sendToOpenTsdbTimeStart;

            String outputString = "ThreadId=" + threadId_ + ", SendToOpenTsdbSuccess=" + isSendSuccess + ", SendToOpenTsdbTime=" + sendToOpenTsdbTimeElasped;
            if (sendToOpenTsdbTimeElasped < sendTimeWarningThreshold_) {
                logger.info(outputString);
            }
            else {
                logger.warn(outputString);
            }
        }
        
    }
    
    public static boolean sendMetricsToOpenTsdb_Telnet(List<String> outputMessagesForOpenTsdb, String openTsdbHost, int openTsdbPort, int numSendRetries) {
        
        if ((outputMessagesForOpenTsdb == null) || outputMessagesForOpenTsdb.isEmpty() || (openTsdbHost == null) || (openTsdbHost.isEmpty()) || 
                (openTsdbPort < 0) || (openTsdbPort > 65535) || (numSendRetries < 0))  {
            return false;
        }
        
        boolean isSendAllSuccess = true;
        
        TcpClient tcpClient = new TcpClient(openTsdbHost, openTsdbPort, true);

        int retryCounter = 0;
        while (!tcpClient.isConnected() && (retryCounter < numSendRetries)) {
            tcpClient.reset();
            retryCounter++;
        }
        
        if (tcpClient.isConnected()) {
            for (String outputMessageForOpenTsdb : outputMessagesForOpenTsdb) {
                boolean isSendSucess = tcpClient.send("put " + outputMessageForOpenTsdb + "\n", numSendRetries, false, false);

                if (!isSendSucess) {
                    logger.error("Error sending message to OpenTSDB.");
                    isSendAllSuccess = false;
                }
            }
        }
        else {
            logger.error("Error creating TCP connection to OpenTSDB.");
            isSendAllSuccess = false;
        }

        tcpClient.close();
        
        return isSendAllSuccess;
    }
        
}