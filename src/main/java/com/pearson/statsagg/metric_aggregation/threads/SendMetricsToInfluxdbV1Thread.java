package com.pearson.statsagg.metric_aggregation.threads;

import com.pearson.statsagg.utilities.HttpUtils;
import java.util.List;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetricFormat_v1;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToInfluxdbV1Thread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToInfluxdbV1Thread.class.getName());
    
    private static final Map<String,String> OPENTSDB_HTTP_HEADER_PROPERTIES = getInfluxdbHttpHeaderProperties();
    
    private final List<? extends InfluxdbMetricFormat_v1> influxdbStatsAggMetrics_;
    private final URL influxdbUrl_;
    private final int numSendRetries_;
    private final int maxMetricsPerMessage_;
    private final String threadId_;
    
    // constructor for outputting to http
    public SendMetricsToInfluxdbV1Thread(List<? extends InfluxdbMetricFormat_v1> influxdbStatsAggMetrics, URL influxdbUrl, int numSendRetries, int maxMetricsPerMessage, String threadId) {
        this.influxdbStatsAggMetrics_ = influxdbStatsAggMetrics;
        this.influxdbUrl_ = influxdbUrl;
        this.numSendRetries_ = numSendRetries;
        this.maxMetricsPerMessage_ = maxMetricsPerMessage;
        this.threadId_ = threadId;
    }
    
    @Override
    public void run() {
        
        if ((influxdbStatsAggMetrics_ != null) && !influxdbStatsAggMetrics_.isEmpty()) {
            long sendToInfluxdbTimeStart = System.currentTimeMillis();

            boolean isSendSuccess = sendMetricsToInfluxdb_HTTP(influxdbStatsAggMetrics_, maxMetricsPerMessage_, influxdbUrl_.toExternalForm(), numSendRetries_);
            
            long sendToInfluxdbTimeElasped = System.currentTimeMillis() - sendToInfluxdbTimeStart;

            String outputString = "ThreadId=" + threadId_ + ", Destination=\"" + influxdbUrl_.toExternalForm() + 
                                "\", SendToInfluxdbTelnetSuccess=" + isSendSuccess + ", SendToInfluxdbTime=" + sendToInfluxdbTimeElasped;                 
                
            logger.info(outputString);
        }
        
    }
        
    public static boolean sendMetricsToInfluxdb_HTTP(List<? extends InfluxdbMetricFormat_v1> influxdbStatsAggMetrics, int maxMetricsPerMessage, String url, int numSendRetries) {
              
        if ((influxdbStatsAggMetrics == null) || influxdbStatsAggMetrics.isEmpty()) {
            return true;
        } 
        
        if ((url == null) || (maxMetricsPerMessage <= 0) || (numSendRetries < 0)) {
            return false;
        } 
        
        boolean isAllSendSuccess = true;
        
        List influxdbStatsAggMetricsList = influxdbStatsAggMetrics;
        List<List<? extends InfluxdbMetricFormat_v1>> partitionedList = Lists.partition(influxdbStatsAggMetricsList, maxMetricsPerMessage);
        
        for (List influxdbStatsAggMetricsPartitionedList : partitionedList) {
            String influxdbMetricJson = InfluxdbMetric_v1.getInfluxdbJson(influxdbStatsAggMetricsPartitionedList);
            
            if (influxdbMetricJson != null) {
                String response = HttpUtils.httpRequest(url, OPENTSDB_HTTP_HEADER_PROPERTIES, influxdbMetricJson, "UTF-8", "POST", numSendRetries);
                if (response == null) isAllSendSuccess = false;
            }
            else {
                isAllSendSuccess = false;
            }
        }
        
        return isAllSendSuccess;
    }
    
//    public static void sendMetricsToInfluxdbTelnetEndpoints(List<? extends InfluxdbMetricFormat_v1> influxdbStatsAggMetrics, String threadId) {
//        
//        try {
//            List<InfluxdbTelnetOutputModule> influxdbTelnetOutputModules = ApplicationConfiguration.getInfluxdbV1OutputModules();
//            if (influxdbTelnetOutputModules == null) return;
//                    
//            for (InfluxdbTelnetOutputModule influxdbTelnetOutputModule : influxdbTelnetOutputModules) {
//                if (!influxdbTelnetOutputModule.isOutputEnabled()) continue;
//                
//                SendMetricsToInfluxdbV1Thread sendMetricsToTelnetInfluxdbThread = new SendMetricsToInfluxdbV1Thread(influxdbStatsAggMetrics, influxdbTelnetOutputModule.getHost(), 
//                       influxdbTelnetOutputModule.getPort(), influxdbTelnetOutputModule.getNumSendRetryAttempts(), threadId);
//                
//                SendToInfluxdbThreadPoolManager.executeThread(sendMetricsToTelnetInfluxdbThread);
//            }
//        }
//        catch (Exception e) {
//            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
//        }
//        
//    }
//    
//    public static void sendMetricsToInfluxdbHttpEndpoints(List<? extends InfluxdbMetricFormat> influxdbStatsAggMetrics, String threadId) {
//        
//        try {
//            List<InfluxdbHttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbHttpOutputModules();
//            if (influxdbHttpOutputModules == null) return;
//                    
//            for (InfluxdbHttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
//                if (!influxdbHttpOutputModule.isOutputEnabled()) continue;
//                
//                URL url = new URL(influxdbHttpOutputModule.getUrl());
//                
//                SendMetricsToInfluxdbV1Thread sendMetricsToHttpInfluxdbThread = new SendMetricsToInfluxdbV1Thread(influxdbStatsAggMetrics, url, 
//                       influxdbHttpOutputModule.getNumSendRetryAttempts(), influxdbHttpOutputModule.getMaxMetricsPerMessage(), threadId);
//                
//                SendToInfluxdbThreadPoolManager.executeThread(sendMetricsToHttpInfluxdbThread);
//            }
//        }
//        catch (Exception e) {
//            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
//        }
//        
//    }
//    
//    public static boolean isAnyInfluxdbTelnetOutputModuleEnabled() {
//        
//        List<InfluxdbTelnetOutputModule> influxdbTelnetOutputModules = ApplicationConfiguration.getInfluxdbOutputModules();
//        if (influxdbTelnetOutputModules == null) return false;
//        
//        for (InfluxdbTelnetOutputModule influxdbTelnetOutputModule : influxdbTelnetOutputModules) {
//            if (influxdbTelnetOutputModule.isOutputEnabled()) {
//                return true;
//            }
//        }
//        
//        return false;
//    }
//
//    public static List<InfluxdbTelnetOutputModule> getEnabledInfluxdbTelnetOutputModules() {
//        
//        List<InfluxdbTelnetOutputModule> influxdbTelnetOutputModules = ApplicationConfiguration.getInfluxdbTelnetOutputModules();
//        if (influxdbTelnetOutputModules == null) return new ArrayList<>();
//        
//        List<InfluxdbTelnetOutputModule> enabledInfluxdbOutputModules = new ArrayList<>();
//        
//        for (InfluxdbTelnetOutputModule influxdbTelnetOutputModule : influxdbTelnetOutputModules) {
//            if (influxdbTelnetOutputModule.isOutputEnabled()) {
//                enabledInfluxdbOutputModules.add(influxdbTelnetOutputModule);
//            }
//        }
//        
//        return enabledInfluxdbOutputModules;
//    }
//    
//    public static boolean isAnyInfluxdbHttpOutputModuleEnabled() {
//        
//        List<InfluxdbHttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbHttpOutputModules();
//        if (influxdbHttpOutputModules == null) return false;
//        
//        for (InfluxdbHttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
//            if (influxdbHttpOutputModule.isOutputEnabled()) {
//                return true;
//            }
//        }
//        
//        return false;
//    }
//
//    public static List<InfluxdbHttpOutputModule> getEnabledInfluxdbHttpOutputModules() {
//        
//        List<InfluxdbHttpOutputModule> influxdbHttpOutputModules = ApplicationConfiguration.getInfluxdbHttpOutputModules();
//        if (influxdbHttpOutputModules == null) return new ArrayList<>();
//        
//        List<InfluxdbHttpOutputModule> enabledInfluxdbOutputModules = new ArrayList<>();
//        
//        for (InfluxdbHttpOutputModule influxdbHttpOutputModule : influxdbHttpOutputModules) {
//            if (influxdbHttpOutputModule.isOutputEnabled()) {
//                enabledInfluxdbOutputModules.add(influxdbHttpOutputModule);
//            }
//        }
//        
//        return enabledInfluxdbOutputModules;
//    }
    
    private static Map<String,String> getInfluxdbHttpHeaderProperties() {
        Map<String,String> influxdbHttpHeaderProperties = new HashMap<>();
        
        influxdbHttpHeaderProperties.put("Content-Type", "application/javascript");
        influxdbHttpHeaderProperties.put("Charset", "UTF-8");
        
        return influxdbHttpHeaderProperties;
    }
    
}