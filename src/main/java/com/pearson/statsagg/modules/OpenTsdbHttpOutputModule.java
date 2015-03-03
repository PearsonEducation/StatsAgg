package com.pearson.statsagg.modules;

import com.pearson.statsagg.controller.threads.SendToOpenTsdbThreadPoolManager;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.metric_aggregation.OpenTsdbMetricFormat;
import com.pearson.statsagg.metric_aggregation.threads.SendMetricsToOpenTsdbThread;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbHttpOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbHttpOutputModule.class.getName());
    
    private final boolean isOutputEnabled_;
    private final String host_;
    private final int port_;
    private final int numSendRetryAttempts_;
    
    public OpenTsdbHttpOutputModule(boolean isOutputEnabled, String host, int port, int numSendRetryAttempts) {
        this.isOutputEnabled_ = isOutputEnabled;
        this.host_ = host;
        this.port_ = port;
        this.numSendRetryAttempts_ = numSendRetryAttempts;
    }
    
    public static List<String> buildOpenTsdbMessages(List<? extends OpenTsdbMetricFormat> metrics) {
        
        if ((metrics == null) || metrics.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> messages = new ArrayList<>();

        for (OpenTsdbMetricFormat metric : metrics) {
            String message = metric.getOpenTsdbFormatString();
            if (message != null) messages.add(message);
        }

        return messages;
    }
    
//    public static void sendMetricsToOpenTsdbEndpoints(List<String> outputMessagesForOpenTsdb, String threadId) {
//        
//        try {
//            
//            List<OpenTsdbHttpOutputModule> openTsdbOutuputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
//            if (openTsdbOutuputModules == null) return;
//                    
//            for (OpenTsdbHttpOutputModule openTsdbOutputModule : openTsdbOutuputModules) {
//                if (!openTsdbOutputModule.isOutputEnabled()) continue;
//                
//                SendMetricsToOpenTsdbThread sendMetricsToOpenTsdbThread = new SendMetricsToOpenTsdbThread(outputMessagesForOpenTsdb, openTsdbOutputModule.getHost(), 
//                       openTsdbOutputModule.getPort(), openTsdbOutputModule.getNumSendRetryAttempts(), threadId, (int) ApplicationConfiguration.getFlushTimeAgg());
//                
//                SendToOpenTsdbThreadPoolManager.executeThread(sendMetricsToOpenTsdbThread);
//            }
//        }
//        catch (Exception e) {
//            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
//        }
//        
//    }
//    
//    public static boolean isAnyOpenTsdbOutputModuleEnabled() {
//        
//        List<OpenTsdbHttpOutputModule> openTsdbOutuputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
//        if (openTsdbOutuputModules == null) return false;
//        
//        for (OpenTsdbHttpOutputModule openTsdbOutputModule : openTsdbOutuputModules) {
//            if (openTsdbOutputModule.isOutputEnabled()) {
//                return true;
//            }
//        }
//        
//        return false;
//    }
//
//    public static List<OpenTsdbHttpOutputModule> getEnabledOpenTsdbOutputModules() {
//        
//        List<OpenTsdbHttpOutputModule> openTsdbOutuputModules = ApplicationConfiguration.getOpenTsdbTelnetOutputModules();
//        if (openTsdbOutuputModules == null) return new ArrayList<>();
//        
//        List<OpenTsdbHttpOutputModule> enabledOpenTsdbOutputModules = new ArrayList<>();
//        
//        for (OpenTsdbHttpOutputModule openTsdbOutputModule : openTsdbOutuputModules) {
//            if (openTsdbOutputModule.isOutputEnabled()) {
//                enabledOpenTsdbOutputModules.add(openTsdbOutputModule);
//            }
//        }
//        
//        return enabledOpenTsdbOutputModules;
//    }
    
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
    
}


