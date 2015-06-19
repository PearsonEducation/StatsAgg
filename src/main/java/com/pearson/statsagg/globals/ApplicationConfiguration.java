package com.pearson.statsagg.globals;

import au.com.bytecode.opencsv.CSVReader;
import com.pearson.statsagg.utilities.PropertiesConfigurationWrapper;
import com.pearson.statsagg.webui.HttpLink;
import java.io.InputStream;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class ApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class.getName());
    
    public static final int VALUE_NOT_SET_CODE = -4444;

    private static boolean isUsingDefaultSettings_ = false;
    private static boolean isInitializeSuccess_ = false; 
    private static PropertiesConfigurationWrapper applicationConfiguration_ = null;
    
    private static int flushTimeAgg_ = VALUE_NOT_SET_CODE;
    private static boolean debugModeEnabled_ = false;
    
    private static final List<GraphiteOutputModule> graphiteOutputModules_ = new ArrayList<>();
    private static final List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules_ = new ArrayList<>();
    private static final List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules_ = new ArrayList<>();

    private static boolean statsdTcpListenerEnabled_ = false;
    private static int statsdTcpListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean statsdUdpListenerEnabled_ = false;
    private static int statsdUdpListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean graphiteAggregatorTcpListenerEnabled_ = false;
    private static int graphiteAggregatorTcpListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean graphiteAggregatorUdpListenerEnabled_ = false;
    private static int graphiteAggregatorUdpListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean graphitePassthroughTcpListenerEnabled_ = false;
    private static int graphitePassthroughTcpListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean graphitePassthroughUdpListenerEnabled_ = false;
    private static int graphitePassthroughUdpListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean openTsdbTcpTelnetListenerEnabled_ = false;
    private static int openTsdbTcpTelnetListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean openTsdbHttpListenerEnabled_ = false;
    private static int openTsdbHttpListenerPort_ = VALUE_NOT_SET_CODE;
    private static boolean influxdbHttpListenerEnabled_ = false;
    private static int influxdbHttpListenerPort_ = VALUE_NOT_SET_CODE;
    
    private static boolean globalMetricNamePrefixEnabled_ = false;
    private static String globalMetricNamePrefixValue_ = null;
    private static String globalAggregatedMetricsSeparatorString_ = null;
    private static boolean statsdMetricNamePrefixEnabled_ = false;
    private static String statsdMetricNamePrefixValue_ = null;
    private static boolean statsdMetricNameSuffixEnabled_ = false;
    private static String statsdMetricNameSuffixValue_ = null;
    private static boolean statsdCounterMetricNamePrefixEnabled_ = false;
    private static String statsdCounterMetricNamePrefixValue_ = null;
    private static boolean statsdGaugeMetricNamePrefixEnabled_ = false;
    private static String statsdGaugeMetricNamePrefixValue_ = null;
    private static boolean statsdTimerMetricNamePrefixEnabled_ = false;
    private static String statsdTimerMetricNamePrefixValue_ = null;
    private static boolean statsdSetMetricNamePrefixEnabled_ = false;
    private static String statsdSetMetricNamePrefixValue_ = null;
    private static boolean graphiteAggregatorMetricNamePrefixEnabled_ = false;
    private static String graphiteAggregatorMetricNamePrefixValue_ = null;
    private static boolean graphitePassthroughMetricNamePrefixEnabled_ = false;
    private static String graphitePassthroughMetricNamePrefixValue_ = null;
    private static boolean openTsdbMetricNamePrefixEnabled_ = false;
    private static String openTsdbMetricNamePrefixValue_ = null;
    private static boolean influxdbMetricNamePrefixEnabled_ = false;
    private static String influxdbMetricNamePrefixValue_ = null;
    
    private static boolean statsdCounterSendZeroOnInactive_ = false;
    private static boolean statsdTimerSendZeroOnInactive_ = false;
    private static boolean statsdGaugeSendPreviousValue_ = false;
    private static boolean statsdSetSendZeroOnInactive_ = false;
    private static boolean graphiteAggregatorSendPreviousValue_ = false;
    private static boolean graphitePassthroughSendPreviousValue_ = false;      
    private static boolean openTsdbSendPreviousValue_ = false;  
    private static boolean influxdbSendPreviousValue_ = false;  

    private static StatsdNthPercentiles statsdNthPercentiles_ = null;
    private static List<StatsdHistogramConfiguration> statsdHistogramConfigurations_ = null;
    private static boolean statsdUseLegacyNameSpacing_ = false;
    private static boolean statsdPersistGauges_ = false;
    
    private static String influxdbDefaultDatabaseName_;
    private static String influxdbDefaultDatabaseUsername_;
    private static String influxdbDefaultDatabasePassword_;
    
    private static boolean alertRoutineEnabled_ = false;
    private static int alertRoutineInterval_ = VALUE_NOT_SET_CODE;
    private static boolean alertSendEmailEnabled_ = false;  
    private static int alertMaxMetricsInEmail_ = VALUE_NOT_SET_CODE;
    private static boolean alertOutputAlertStatusToGraphite_ = false;
    private static String alertOutputAlertStatusToGraphiteMetricPrefix_ = null;
    private static String alertStatsAggLocation_ = null;
    private static long alertWaitTimeAfterRestart_ = VALUE_NOT_SET_CODE;
    private static String alertSmtpHost_ = null;
    private static int alertSmtpPort_ = VALUE_NOT_SET_CODE;
    private static String alertSmtpUsername_ = null;
    private static String alertSmtpPassword_ = null;
    private static int alertSmtpConnectionTimeout_ = VALUE_NOT_SET_CODE;
    private static boolean alertSmtpUseSslTls_ = false;
    private static boolean alertSmtpUseStartTls_ = false;
    private static String alertSmtpFromAddress_ = null;
    private static String alertSmtpFromName_ = null;

    private static final List<HttpLink> customActionUrls_ = new ArrayList<>();
    
    
    public static boolean initialize(InputStream configurationInputStream, boolean isUsingDefaultSettings) {
        
        if (configurationInputStream == null) {
            return false;
        }
        
        isUsingDefaultSettings_ = isUsingDefaultSettings;
        applicationConfiguration_ = new PropertiesConfigurationWrapper(configurationInputStream);
        
        if ((applicationConfiguration_ == null) || !applicationConfiguration_.isValid()) {
            return false;
        }
        
        isInitializeSuccess_ = setApplicationConfigurationValues();
        return isInitializeSuccess_;
    }
    
    private static boolean setApplicationConfigurationValues() {
        
        try {
            flushTimeAgg_ = applicationConfiguration_.safeGetInteger("flush_time_agg", 10000);
            debugModeEnabled_ = applicationConfiguration_.safeGetBoolean("debug_mode_enabled", false);
            
            // graphite configuration
            graphiteOutputModules_.addAll(readGraphiteOutputModules());

            // opentsdb configuration
            openTsdbTelnetOutputModules_.addAll(readOpenTsdbTelnetOutputModules());
            
            // opentsdb configuration
            openTsdbHttpOutputModules_.addAll(readOpenTsdbHttpOutputModules());
            
            // listener config
            statsdTcpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_tcp_listener_enabled", true);
            statsdTcpListenerPort_ = applicationConfiguration_.safeGetInt("statsd_tcp_listener_port", 8125);
            statsdUdpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_udp_listener_enabled", true);
            statsdUdpListenerPort_ = applicationConfiguration_.safeGetInt("statsd_udp_listener_port", 8125);
            graphiteAggregatorTcpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("graphite_aggregator_tcp_listener_enabled", true);
            graphiteAggregatorTcpListenerPort_ = applicationConfiguration_.safeGetInt("graphite_aggregator_tcp_listener_port", 22003);
            graphiteAggregatorUdpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("graphite_aggregator_udp_listener_enabled", true);
            graphiteAggregatorUdpListenerPort_ = applicationConfiguration_.safeGetInt("graphite_aggregator_udp_listener_port", 22003);
            graphitePassthroughTcpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("graphite_passthrough_tcp_listener_enabled", true);
            graphitePassthroughTcpListenerPort_ = applicationConfiguration_.safeGetInt("graphite_passthrough_tcp_listener_port", 2003);
            graphitePassthroughUdpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("graphite_passthrough_udp_listener_enabled", true);
            graphitePassthroughUdpListenerPort_ = applicationConfiguration_.safeGetInt("graphite_passthrough_udp_listener_port", 2003);
            openTsdbTcpTelnetListenerEnabled_ = applicationConfiguration_.safeGetBoolean("opentsdb_tcp_telnet_listener_enabled", true);
            openTsdbTcpTelnetListenerPort_ = applicationConfiguration_.safeGetInt("opentsdb_tcp_telnet_listener_port", 4242);
            openTsdbHttpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("opentsdb_http_listener_enabled", true);
            openTsdbHttpListenerPort_ = applicationConfiguration_.safeGetInt("opentsdb_http_listener_port", 4243);
            influxdbHttpListenerEnabled_ = applicationConfiguration_.safeGetBoolean("influxdb_http_listener_enabled", true);
            influxdbHttpListenerPort_ = applicationConfiguration_.safeGetInt("influxdb_http_listener_port", 8086);
            
            // metric naming config
            globalMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("global_metric_name_prefix_enabled", false);
            globalMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("global_metric_name_prefix_value", "statsagg");
            globalAggregatedMetricsSeparatorString_ = applicationConfiguration_.safeGetString("global_aggregated_metrics_separator_string", "."); 
            statsdMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_metric_name_prefix_enabled", false);
            statsdMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("statsd_metric_name_prefix_value", "stats");
            statsdMetricNameSuffixEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_metric_name_suffix_enabled", false);
            statsdMetricNameSuffixValue_ = applicationConfiguration_.safeGetString("statsd_metric_name_suffix_value", "");
            statsdCounterMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_counter_metric_name_prefix_enabled", false);
            statsdCounterMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("statsd_counter_metric_name_prefix_value", "counters");
            statsdGaugeMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_gauge_metric_name_prefix_enabled", false);
            statsdGaugeMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("statsd_gauge_metric_name_prefix_value", "gauges");
            statsdTimerMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_timer_metric_name_prefix_enabled", false);
            statsdTimerMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("statsd_timer_metric_name_prefix_value", "timers");
            statsdSetMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("statsd_set_metric_name_prefix_enabled", false);
            statsdSetMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("statsd_set_metric_name_prefix_value", "sets");
            graphiteAggregatorMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("graphite_aggregator_metric_name_prefix_enabled", false);
            graphiteAggregatorMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("graphite_aggregator_metric_name_prefix_value", "graphite-agg");
            graphitePassthroughMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("graphite_passthrough_metric_name_prefix_enabled", false);
            graphitePassthroughMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("graphite_passthrough_metric_name_prefix_value", "graphite");
            openTsdbMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("opentsdb_metric_name_prefix_enabled", false);
            openTsdbMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("opentsdb_metric_name_prefix_value", "opentsdb");
            influxdbMetricNamePrefixEnabled_ = applicationConfiguration_.safeGetBoolean("influxdb_metric_name_prefix_enabled", false);
            influxdbMetricNamePrefixValue_ = applicationConfiguration_.safeGetString("influxdb_metric_name_prefix_value", "influxdb");
            
            // send previous data config
            statsdCounterSendZeroOnInactive_ = applicationConfiguration_.safeGetBoolean("statsd_counter_send_0_on_inactive", true);
            statsdTimerSendZeroOnInactive_ = applicationConfiguration_.safeGetBoolean("statsd_timer_send_0_on_inactive", true);
            statsdGaugeSendPreviousValue_ = applicationConfiguration_.safeGetBoolean("statsd_gauge_send_previous_value", true);
            statsdSetSendZeroOnInactive_ = applicationConfiguration_.safeGetBoolean("statsd_set_send_0_on_inactive", true);
            graphiteAggregatorSendPreviousValue_ = applicationConfiguration_.safeGetBoolean("graphite_aggregator_send_previous_value", false);
            graphitePassthroughSendPreviousValue_ = applicationConfiguration_.safeGetBoolean("graphite_passthrough_send_previous_value", false);
            openTsdbSendPreviousValue_ = applicationConfiguration_.safeGetBoolean("opentsdb_send_previous_value", false);
            influxdbSendPreviousValue_ = applicationConfiguration_.safeGetBoolean("influxdb_send_previous_value", false);

            // statsd specific variables
            statsdNthPercentiles_ = new StatsdNthPercentiles(applicationConfiguration_.safeGetString("statsd_nth_percentiles", "90"));
            statsdHistogramConfigurations_ = readStatsdHistogramConfiguration(applicationConfiguration_.safeGetString("statsd_histograms", null));
            statsdUseLegacyNameSpacing_ = applicationConfiguration_.safeGetBoolean("statsd_use_legacy_name_spacing", false);
            statsdPersistGauges_ = applicationConfiguration_.safeGetBoolean("statsd_persist_gauges", true);
            
            // influxdb specific variables
            influxdbDefaultDatabaseName_ = applicationConfiguration_.safeGetString("influxdb_default_database_name", "statsagg");
            influxdbDefaultDatabaseUsername_ = applicationConfiguration_.safeGetString("influxdb_default_database_username", "statsagg");
            influxdbDefaultDatabasePassword_ = applicationConfiguration_.safeGetString("influxdb_default_database_password", "statsagg");
            
            // alerting variables
            alertRoutineEnabled_ = applicationConfiguration_.safeGetBoolean("alert_routine_enabled", true);
            alertRoutineInterval_ = applicationConfiguration_.safeGetInteger("alert_routine_interval", 5000);
            alertSendEmailEnabled_ = applicationConfiguration_.safeGetBoolean("alert_send_email_enabled", false);
            alertMaxMetricsInEmail_ = applicationConfiguration_.safeGetInteger("alert_max_metrics_in_email", 100);
            alertOutputAlertStatusToGraphite_ = applicationConfiguration_.safeGetBoolean("alert_output_alert_status_to_graphite", true);
            alertOutputAlertStatusToGraphiteMetricPrefix_ = applicationConfiguration_.safeGetString("alert_output_alert_status_to_graphite_metric_prefix", "StatsAgg-Alerts");
            alertStatsAggLocation_ = applicationConfiguration_.safeGetString("alert_statsagg_location", "");
            alertWaitTimeAfterRestart_ = applicationConfiguration_.safeGetInteger("alert_wait_time_after_restart", 120000);
                    
            alertSmtpHost_ = applicationConfiguration_.safeGetString("alert_smtp_host", "127.0.0.1");
            alertSmtpPort_ = applicationConfiguration_.safeGetInteger("alert_smtp_port", 25);
            alertSmtpConnectionTimeout_ = applicationConfiguration_.safeGetInteger("alert_smtp_connection_timeout", 15000);
            alertSmtpUsername_ = applicationConfiguration_.safeGetString("alert_smtp_username", "");
            alertSmtpPassword_ = applicationConfiguration_.safeGetString("alert_smtp_password", "");
            alertSmtpUseSslTls_ = applicationConfiguration_.safeGetBoolean("alert_smtp_use_ssl_tls", false);
            alertSmtpUseStartTls_ = applicationConfiguration_.safeGetBoolean("alert_smtp_use_starttls", false);
            alertSmtpFromAddress_ = applicationConfiguration_.safeGetString("alert_smtp_from_address", "noreply@noreply.com");
            alertSmtpFromName_ = applicationConfiguration_.safeGetString("alert_smtp_from_name", "StatsAgg");
            
            // website custominzation variables
            customActionUrls_.addAll(readCustomActionUrls());
                    
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
    }
    
    private static List<GraphiteOutputModule> readGraphiteOutputModules() {
        
        List<GraphiteOutputModule> graphiteOutputModules = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            String graphiteOutputModuleKey = "graphite_output_module_" + (i + 1);
            String graphiteOutputModuleValue = applicationConfiguration_.safeGetString(graphiteOutputModuleKey, null);
            
            if (graphiteOutputModuleValue == null) continue;
            
            try {
                CSVReader reader = new CSVReader(new StringReader(graphiteOutputModuleValue));
                List<String[]> csvValuesArray = reader.readAll();

                if ((csvValuesArray != null) && !csvValuesArray.isEmpty() && (csvValuesArray.get(0) != null)) {
                    String[] csvValues = csvValuesArray.get(0);

                    if (csvValues.length >= 4) {                                
                        boolean isOutputEnabled = Boolean.valueOf(csvValues[0]);
                        String host = csvValues[1];
                        int port = Integer.valueOf(csvValues[2]);
                        int numSendRetryAttempts = Integer.valueOf(csvValues[3]);
                        int maxMetricsPerMessage = 1000;
                        if (csvValues.length > 4) maxMetricsPerMessage = Integer.valueOf(csvValues[4]);
                        
                        GraphiteOutputModule graphiteOutputModule = new GraphiteOutputModule(isOutputEnabled, host, port, numSendRetryAttempts, maxMetricsPerMessage);
                        graphiteOutputModules.add(graphiteOutputModule);
                    }
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return graphiteOutputModules;
    }

    private static List<OpenTsdbTelnetOutputModule> readOpenTsdbTelnetOutputModules() {
        
        List<OpenTsdbTelnetOutputModule> openTsdbTelnetOutputModules = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            String openTsdbTelnetOutputModuleKey = "opentsdb_telnet_output_module_" + (i + 1);
            String openTsdbTelnetOutputModuleValue = applicationConfiguration_.safeGetString(openTsdbTelnetOutputModuleKey, null);
            
            if (openTsdbTelnetOutputModuleValue == null) continue;
            
            try {
                CSVReader reader = new CSVReader(new StringReader(openTsdbTelnetOutputModuleValue));
                List<String[]> csvValuesArray = reader.readAll();

                if ((csvValuesArray != null) && !csvValuesArray.isEmpty() && (csvValuesArray.get(0) != null)) {
                    String[] csvValues = csvValuesArray.get(0);

                    if (csvValues.length == 4) {                                
                        boolean isOutputEnabled = Boolean.valueOf(csvValues[0]);
                        String host = csvValues[1];
                        int port = Integer.valueOf(csvValues[2]);
                        int numSendRetryAttempts = Integer.valueOf(csvValues[3]);
                        
                        OpenTsdbTelnetOutputModule openTsdbTelnetOutputModule = new OpenTsdbTelnetOutputModule(isOutputEnabled, host, port, numSendRetryAttempts);
                        openTsdbTelnetOutputModules.add(openTsdbTelnetOutputModule);
                    }
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return openTsdbTelnetOutputModules;
    }
    
    private static List<OpenTsdbHttpOutputModule> readOpenTsdbHttpOutputModules() {
        
        List<OpenTsdbHttpOutputModule> openTsdbHttpOutputModules = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            String openTsdbHttpOutputModuleKey = "opentsdb_http_output_module_" + (i + 1);
            String openTsdbHttpOutputModuleValue = applicationConfiguration_.safeGetString(openTsdbHttpOutputModuleKey, null);
            
            if (openTsdbHttpOutputModuleValue == null) continue;
            
            try {
                CSVReader reader = new CSVReader(new StringReader(openTsdbHttpOutputModuleValue));
                List<String[]> csvValuesArray = reader.readAll();

                if ((csvValuesArray != null) && !csvValuesArray.isEmpty() && (csvValuesArray.get(0) != null)) {
                    String[] csvValues = csvValuesArray.get(0);

                    if (csvValues.length == 4) {                                
                        boolean isOutputEnabled = Boolean.valueOf(csvValues[0]);
                        String url = csvValues[1];
                        int numSendRetryAttempts = Integer.valueOf(csvValues[2]);
                        int maxMetricsPerMessage = Integer.valueOf(csvValues[3]);
                        
                        OpenTsdbHttpOutputModule openTsdbHttpOutputModule = new OpenTsdbHttpOutputModule(isOutputEnabled, url, numSendRetryAttempts, maxMetricsPerMessage);
                        openTsdbHttpOutputModules.add(openTsdbHttpOutputModule);
                    }
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return openTsdbHttpOutputModules;
    }
    
    private static List<StatsdHistogramConfiguration> readStatsdHistogramConfiguration(String unparsedStatsdHistogramConfigurations) {
        List<StatsdHistogramConfiguration> statsdHistogramConfigurations = StatsdHistogramConfiguration.getStatsdHistogramConfigurations(unparsedStatsdHistogramConfigurations);
        return statsdHistogramConfigurations;
    }
    
    private static List<HttpLink> readCustomActionUrls() {
        
        List<HttpLink> customActionUrls = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            String customActionUrlKey = "custom_action_url_" + (i + 1);
            String customActionUrlValue = applicationConfiguration_.safeGetString(customActionUrlKey, null);
            
            if (customActionUrlValue == null) continue;
            
            try {
                CSVReader reader = new CSVReader(new StringReader(customActionUrlValue));
                List<String[]> csvValuesArray = reader.readAll();

                if ((csvValuesArray != null) && !csvValuesArray.isEmpty() && (csvValuesArray.get(0) != null)) {
                    String[] csvValues = csvValuesArray.get(0);

                    if (csvValues.length == 2) {                                
                        String url = csvValues[0];
                        String linkText = csvValues[1];
                        
                        HttpLink httpLink = new HttpLink(url, linkText);
                        customActionUrls.add(httpLink);
                    }

                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
        }
        
        return customActionUrls;
    }

    public static boolean isUsingDefaultSettings() {
        return isUsingDefaultSettings_;
    }

    public static PropertiesConfigurationWrapper getApplicationConfiguration() {
        return applicationConfiguration_;
    }

    public static int getFlushTimeAgg() {
        return flushTimeAgg_;
    }
    
    public static boolean isDebugModeEnabled() {
        return debugModeEnabled_;
    }

    public static List<GraphiteOutputModule> getGraphiteOutputModules() {
        if (graphiteOutputModules_ == null) return null;
        else return new ArrayList<>(graphiteOutputModules_);
    }

    public static List<OpenTsdbTelnetOutputModule> getOpenTsdbTelnetOutputModules() {
        if (openTsdbTelnetOutputModules_ == null) return null;
        else return new ArrayList<>(openTsdbTelnetOutputModules_);
    }

    public static List<OpenTsdbHttpOutputModule> getOpenTsdbHttpOutputModules() {
        if (openTsdbHttpOutputModules_ == null) return null;
        else return new ArrayList<>(openTsdbHttpOutputModules_);
    }
    
    public static boolean isStatsdTcpListenerEnabled() {
        return statsdTcpListenerEnabled_;
    }

    public static int getStatsdTcpListenerPort() {
        return statsdTcpListenerPort_;
    }

    public static boolean isStatsdUdpListenerEnabled() {
        return statsdUdpListenerEnabled_;
    }

    public static int getStatsdUdpListenerPort() {
        return statsdUdpListenerPort_;
    }

    public static boolean isGraphiteAggregatorTcpListenerEnabled() {
        return graphiteAggregatorTcpListenerEnabled_;
    }

    public static int getGraphiteAggregatorTcpListenerPort() {
        return graphiteAggregatorTcpListenerPort_;
    }

    public static boolean isGraphiteAggregatorUdpListenerEnabled() {
        return graphiteAggregatorUdpListenerEnabled_;
    }

    public static int getGraphiteAggregatorUdpListenerPort() {
        return graphiteAggregatorUdpListenerPort_;
    }

    public static boolean isGraphitePassthroughTcpListenerEnabled() {
        return graphitePassthroughTcpListenerEnabled_;
    }

    public static int getGraphitePassthroughTcpListenerPort() {
        return graphitePassthroughTcpListenerPort_;
    }

    public static boolean isGraphitePassthroughUdpListenerEnabled() {
        return graphitePassthroughUdpListenerEnabled_;
    }

    public static int getGraphitePassthroughUdpListenerPort() {
        return graphitePassthroughUdpListenerPort_;
    }

    public static boolean isOpenTsdbTcpTelnetListenerEnabled() {
        return openTsdbTcpTelnetListenerEnabled_;
    }

    public static int getOpenTsdbTcpTelnetListenerPort() {
        return openTsdbTcpTelnetListenerPort_;
    }

    public static boolean isOpenTsdbHttpListenerEnabled() {
        return openTsdbHttpListenerEnabled_;
    }

    public static int getOpenTsdbHttpListenerPort() {
        return openTsdbHttpListenerPort_;
    }
    
    public static boolean isInfluxdbHttpListenerEnabled() {
        return influxdbHttpListenerEnabled_;
    }

    public static int getInfluxdbHttpListenerPort() {
        return influxdbHttpListenerPort_;
    }
    
    public static boolean isGlobalMetricNamePrefixEnabled() {
        return globalMetricNamePrefixEnabled_;
    }

    public static String getGlobalMetricNamePrefixValue() {
        return globalMetricNamePrefixValue_;
    }

    public static String getGlobalAggregatedMetricsSeparatorString() {
        return globalAggregatedMetricsSeparatorString_;
    }
    
    public static boolean isStatsdMetricNamePrefixEnabled() {
        return statsdMetricNamePrefixEnabled_;
    }

    public static String getStatsdMetricNamePrefixValue() {
        return statsdMetricNamePrefixValue_;
    }

    public static boolean isStatsdMetricNameSuffixEnabled() {
        return statsdMetricNameSuffixEnabled_;
    }

    public static String getStatsdMetricNameSuffixValue() {
        return statsdMetricNameSuffixValue_;
    }

    public static boolean isStatsdCounterMetricNamePrefixEnabled() {
        return statsdCounterMetricNamePrefixEnabled_;
    }

    public static String getStatsdCounterMetricNamePrefixValue() {
        return statsdCounterMetricNamePrefixValue_;
    }

    public static boolean isStatsdGaugeMetricNamePrefixEnabled() {
        return statsdGaugeMetricNamePrefixEnabled_;
    }

    public static String getStatsdGaugeMetricNamePrefixValue() {
        return statsdGaugeMetricNamePrefixValue_;
    }

    public static boolean isStatsdTimerMetricNamePrefixEnabled() {
        return statsdTimerMetricNamePrefixEnabled_;
    }

    public static String getStatsdTimerMetricNamePrefixValue() {
        return statsdTimerMetricNamePrefixValue_;
    }

    public static boolean isStatsdSetMetricNamePrefixEnabled() {
        return statsdSetMetricNamePrefixEnabled_;
    }

    public static String getStatsdSetMetricNamePrefixValue() {
        return statsdSetMetricNamePrefixValue_;
    }
    
    public static boolean isGraphiteAggregatorMetricNamePrefixEnabled() {
        return graphiteAggregatorMetricNamePrefixEnabled_;
    }

    public static String getGraphiteAggregatorMetricNamePrefixValue() {
        return graphiteAggregatorMetricNamePrefixValue_;
    }
    
    public static boolean isGraphitePassthroughMetricNamePrefixEnabled() {
        return graphitePassthroughMetricNamePrefixEnabled_;
    }

    public static String getGraphitePassthroughMetricNamePrefixValue() {
        return graphitePassthroughMetricNamePrefixValue_;
    }
    
    public static boolean isOpenTsdbMetricNamePrefixEnabled() {
        return openTsdbMetricNamePrefixEnabled_;
    }

    public static String getOpenTsdbMetricNamePrefixValue() {
        return openTsdbMetricNamePrefixValue_;
    }
    
    public static boolean isInfluxdbMetricNamePrefixEnabled() {
        return influxdbMetricNamePrefixEnabled_;
    }

    public static String getInfluxdbMetricNamePrefixValue() {
        return influxdbMetricNamePrefixValue_;
    }
    
    public static boolean isStatsdCounterSendZeroOnInactive() {
        return statsdCounterSendZeroOnInactive_;
    }

    public static boolean isStatsdTimerSendZeroOnInactive() {
        return statsdTimerSendZeroOnInactive_;
    }

    public static boolean isStatsdGaugeSendPreviousValue() {
        return statsdGaugeSendPreviousValue_;
    }

    public static boolean isStatsdSetSendZeroOnInactive() {
        return statsdSetSendZeroOnInactive_;
    }
    
    public static boolean isGraphiteAggregatorSendPreviousValue() {
        return graphiteAggregatorSendPreviousValue_;
    }
    
    public static boolean isGraphitePassthroughSendPreviousValue() {
        return graphitePassthroughSendPreviousValue_;
    }

    public static boolean isOpenTsdbSendPreviousValue() {
        return openTsdbSendPreviousValue_;
    }
    
    public static boolean isInfluxdbSendPreviousValue() {
        return influxdbSendPreviousValue_;
    }
    
    public static StatsdNthPercentiles getStatsdNthPercentiles() {
        return statsdNthPercentiles_;
    }

    public static List<StatsdHistogramConfiguration> getStatsdHistogramConfigurations() {
        return new ArrayList<>(statsdHistogramConfigurations_);
    }

    public static boolean isStatsdUseLegacyNameSpacing() {
        return statsdUseLegacyNameSpacing_;
    }
    
    public static boolean isStatsdPersistGauges() {
        return statsdPersistGauges_;
    }

    public static String getInfluxdbDefaultDatabaseName() {
        return influxdbDefaultDatabaseName_;
    }

    public static String getInfluxdbDefaultDatabaseUsername() {
        return influxdbDefaultDatabaseUsername_;
    }

    public static String getInfluxdbDefaultDatabasePassword() {
        return influxdbDefaultDatabasePassword_;
    }
    
    public static boolean isAlertRoutineEnabled() {
        return alertRoutineEnabled_;
    }
    
    public static int getAlertRoutineInterval() {
        return alertRoutineInterval_;
    }

    public static boolean isAlertSendEmailEnabled() {
        return alertSendEmailEnabled_;
    }
    
    public static int getAlertMaxMetricsInEmail() {
        return alertMaxMetricsInEmail_;
    }
    
    public static boolean isAlertOutputAlertStatusToGraphite() {
        return alertOutputAlertStatusToGraphite_;
    }

    public static String getAlertOutputAlertStatusToGraphiteMetricPrefix() {
        return alertOutputAlertStatusToGraphiteMetricPrefix_;
    }

    public static String getAlertStatsAggLocation() {
        return alertStatsAggLocation_;
    }

    public static long getAlertWaitTimeAfterRestart() {
        return alertWaitTimeAfterRestart_;
    }
   
    public static String getAlertSmtpHost() {
        return alertSmtpHost_;
    }

    public static int getAlertSmtpPort() {
        return alertSmtpPort_;
    }

    public static String getAlertSmtpUsername() {
        return alertSmtpUsername_;
    }

    public static String getAlertSmtpPassword() {
        return alertSmtpPassword_;
    }

    public static int getAlertSmtpConnectionTimeout() {
        return alertSmtpConnectionTimeout_;
    }
         
    public static boolean isAlertSmtpUseSslTls() {
        return alertSmtpUseSslTls_;
    }
    
    public static boolean isAlertSmtpUseStartTls() {
        return alertSmtpUseStartTls_;
    }
    
    public static String getAlertSmtpFromAddress() {
        return alertSmtpFromAddress_;
    }
    
    public static String getAlertSmtpFromName() {
        return alertSmtpFromName_;
    }

    public static List<HttpLink> getCustomActionUrls() {
        return customActionUrls_;
    }

}
