package com.pearson.statsagg.modules;

import com.pearson.statsagg.controller.threads.SendToGraphiteThreadPoolManager;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.metric_aggregation.GraphiteMetricFormat;
import com.pearson.statsagg.metric_aggregation.threads.SendMetricsToGraphiteThread;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class GraphiteOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphiteOutputModule.class.getName());
    
    private final boolean isOutputEnabled_;
    private final String host_;
    private final int port_;
    private final int numSendRetryAttempts_;
    private final int maxMetricsPerMessage_;

    public GraphiteOutputModule(boolean isOutputEnabled, String host, int port, int numSendRetryAttempts, int maxMetricsPerMessage) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.host_ = host;
        this.port_ = port;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
    }
    
    public static void sendMetricsToGraphiteEndpoints(List<? extends GraphiteMetricFormat> outputMessagesForGraphite, String threadId, int sendTimeWarningThresholdInMs) {
        
        try {
            
            List<GraphiteOutputModule> graphiteOutuputModules = ApplicationConfiguration.getGraphiteOutputModules();
            if (graphiteOutuputModules == null) return;
                    
            for (GraphiteOutputModule graphiteOutputModule : graphiteOutuputModules) {
                if (!graphiteOutputModule.isOutputEnabled()) continue;
                
                SendMetricsToGraphiteThread sendMetricsToGraphiteThread = new SendMetricsToGraphiteThread(outputMessagesForGraphite, graphiteOutputModule.getHost(), 
                       graphiteOutputModule.getPort(), graphiteOutputModule.getNumSendRetryAttempts(), graphiteOutputModule.getMaxMetricsPerMessage(), 
                        threadId, sendTimeWarningThresholdInMs);
                
                SendToGraphiteThreadPoolManager.executeThread(sendMetricsToGraphiteThread);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static boolean isAnyGraphiteOutputModuleEnabled() {
        
        List<GraphiteOutputModule> graphiteOutuputModules = ApplicationConfiguration.getGraphiteOutputModules();
        if (graphiteOutuputModules == null) return false;
        
        for (GraphiteOutputModule graphiteOutputModule : graphiteOutuputModules) {
            if (graphiteOutputModule.isOutputEnabled()) {
                return true;
            }
        }
        
        return false;
    }

    public static List<GraphiteOutputModule> getEnabledGraphiteOutputModules() {
        
        List<GraphiteOutputModule> graphiteOutuputModules = ApplicationConfiguration.getGraphiteOutputModules();
        if (graphiteOutuputModules == null) return new ArrayList<>();
        
        List<GraphiteOutputModule> enabledGraphiteOutputModules = new ArrayList<>();
        
        for (GraphiteOutputModule graphiteOutputModule : graphiteOutuputModules) {
            if (graphiteOutputModule.isOutputEnabled()) {
                enabledGraphiteOutputModules.add(graphiteOutputModule);
            }
        }
        
        return enabledGraphiteOutputModules;
    }
    
    public boolean isOutputEnabled() {
        return isOutputEnabled_;
    }

    public String getHost() {
        return host_;
    }

    public int getPort() {
        return port_;
    }

    public int getNumSendRetryAttempts() {
        return numSendRetryAttempts_;
    }
    
    public int getMaxMetricsPerMessage() {
        return maxMetricsPerMessage_;
    }
    
}