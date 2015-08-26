package com.pearson.statsagg.controller;

import com.pearson.statsagg.controller.thread_managers.SendEmail_ThreadPoolManager;
import com.pearson.statsagg.controller.thread_managers.SendMetricsToOutputModule_ThreadPoolManager;
import com.pearson.statsagg.utilities.InvokerThread;
import com.pearson.statsagg.controller.threads.GraphitePassthroughInvokerThread;
import com.pearson.statsagg.controller.threads.StatsdAggregationInvokerThread;
import com.pearson.statsagg.controller.threads.GraphiteAggregationInvokerThread;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.pearson.statsagg.alerts.AlertThread;
import com.pearson.statsagg.controller.threads.AlertInvokerThread;
import com.pearson.statsagg.controller.threads.CleanupInvokerThread;
import com.pearson.statsagg.controller.threads.InfluxdbV1InvokerThread;
import com.pearson.statsagg.controller.threads.InternalStatsInvokerThread;
import com.pearson.statsagg.controller.threads.OpenTsdbInvokerThread;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.gauges.Gauge;
import com.pearson.statsagg.database_objects.gauges.GaugesDao;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.database_objects.metric_last_seen.MetricLastSeen;
import com.pearson.statsagg.database_objects.metric_last_seen.MetricLastSeenDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.network.JettyServer;
import com.pearson.statsagg.network.NettyServer;
import com.pearson.statsagg.network.http.JettyInfluxdb;
import com.pearson.statsagg.network.http.JettyOpenTsdb;
import com.pearson.statsagg.network.tcp.TcpServer;
import com.pearson.statsagg.network.udp.UdpServer;
import com.pearson.statsagg.utilities.Threads;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Collections;
import org.apache.commons.io.IOUtils;

/**
 * @author Jeffrey Schmidt
 */
public class ContextManager implements ServletContextListener {
        
    private static final Logger logger = LoggerFactory.getLogger(ContextManager.class.getName());
        
    private ServletContext initializerContext_ = null;
    private TcpServer statsdTcpServer_ = null;
    private UdpServer statsdUdpServer_ = null;
    private TcpServer graphiteAggregatorTcpServer_ = null;
    private UdpServer graphiteAggregatorUdpServer_ = null;
    private TcpServer graphitePassthroughTcpServer_ = null;
    private UdpServer graphitePassthroughUdpServer_ = null;
    private TcpServer openTsdbTcpServer_ = null;
    private JettyOpenTsdb jettyOpenTsdb_ = null;
    private JettyInfluxdb jettyInfluxdb_ = null;
    private StatsdAggregationInvokerThread statsdAggregationInvokerThread_ = null;
    private GraphiteAggregationInvokerThread graphiteAggregationInvokerThread_ = null;
    private GraphitePassthroughInvokerThread graphitePassthroughInvokerThread_ = null;
    private OpenTsdbInvokerThread openTsdbInvokerThread_ = null;
    private InfluxdbV1InvokerThread influxdbInvokerThread_ = null;
    private AlertInvokerThread alertInvokerThread_ = null;
    private CleanupInvokerThread cleanupInvokerThread_ = null;
    private InternalStatsInvokerThread internalStatsInvokerThread_ = null;
    
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        System.out.println("Initializer - Context Created");
        
        initializerContext_ = contextEvent.getServletContext();
        
        boolean isApplicationInitializeSuccess = initializeApplication();
        if (!isApplicationInitializeSuccess) {
            logger.error("An error while initializing the application configuration.");
            GlobalVariables.isApplicationInitializeSuccess.set(false);
        }
        else {
            GlobalVariables.isApplicationInitializeSuccess.set(true);
        }
        
        logger.info("Finish - Startup configuration");
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        logger.info("Start Context Destroy...");
        
        initializerContext_ = contextEvent.getServletContext();
        
        Threads.sleepSeconds(1);
        
        GlobalVariables.isApplicationInitializeSuccess.set(false);

        shutdownServerListeners();
        
        shutdownInvokerThreads();
        
        shutdownSendToOutputModuleThreadPoolManager();
                
        shutdownSendEmailThreadPool();
        
        DatabaseConnections.disconnectAndShutdown();
        DatabaseConnections.deregisterJdbcDriver();
        
        Threads.sleepSeconds(1);
        
        shutdownLogger();
        
        logger.info("Initializer - Context Destroyed");
    }
    
    private boolean initializeApplication() {
        
        // load the logger configuration file & initialize it
        boolean isLogbackSuccess = readAndSetLogbackConfiguration();

        // read & set the application configuration
        boolean isApplicationConfigurationSuccess = readAndSetApplicationConfiguration();

        // read the database configuration & getConnection to the database
        boolean initializeDatabaseSuccess = initializeDatabaseFromContext();
        
        // creates the database schema (tables, etc) if it doesn't already exist. 
        if (initializeDatabaseSuccess) {
            boolean createSchemaSuccess = createDatabaseSchemas();
            logger.debug("Create_schema_success=" + createSchemaSuccess);
        }
        
        // reads 'metric last seen' values from the database & loads the relevant values into memory. These are used by availability alerts.
        if (initializeDatabaseSuccess) {
            long numMetrics = readMetricLastSeenFromDatabaseAndAddToGlobalVariables();
            logger.info("Finished reading 'metric last seen' values from database. NumMetricsRead=" + numMetrics);
        }        
        
        // read the gauges from the database & add to the recent metric history global variables
        if (initializeDatabaseSuccess) {
            long numGaugesFromDatabase = readGaugesFromDatabaseAndAddToGlobalVariables();
            logger.info("Finished adding gauges from database to recent metric global history. NumGaugesFromDbAddedToGlobal=" + numGaugesFromDatabase);
        }
        
        // create the prefixes (added on by StatsAgg) for the various types of metrics
        createGraphiteAggregatorMetricPrefix();
        createGraphitePassthroughMetricPrefix();
        createOpenTsdbMetricPrefix();
        createInfluxdbMetricPrefix();
                
        // start the thread pool that is responsible for sending alert emails 
        startSendEmailThreadPool();
        
        // start the thread pool that is responsible for threads sending metrics to the various output modules 
        startSendToOutputModuleThreadPoolManager();
        
        // set last alert executed routine timestamp to '0', which indicates to the rest of the program that it has never been executed
        GlobalVariables.alertRountineLastExecutedTimestamp.set(0);
        
        // starts the threads that calls the aggregation routines on a timer (specified in the application config file)
        statsdAggregationInvokerThread_ = new StatsdAggregationInvokerThread();
        Thread statsdAggregationInvokerThread = new Thread(statsdAggregationInvokerThread_);
        statsdAggregationInvokerThread.start();
        
        graphiteAggregationInvokerThread_ = new GraphiteAggregationInvokerThread();
        Thread graphiteAggregationInvokerThread = new Thread(graphiteAggregationInvokerThread_);
        graphiteAggregationInvokerThread.start();
        
        graphitePassthroughInvokerThread_ = new GraphitePassthroughInvokerThread();
        Thread graphitePassthroughInvokerThread = new Thread(graphitePassthroughInvokerThread_);
        graphitePassthroughInvokerThread.start();
        
        openTsdbInvokerThread_ = new OpenTsdbInvokerThread();
        Thread openTsdbInvokerThread = new Thread(openTsdbInvokerThread_);
        openTsdbInvokerThread.start();
        
        influxdbInvokerThread_ = new InfluxdbV1InvokerThread();
        Thread influxdbInvokerThread = new Thread(influxdbInvokerThread_);
        influxdbInvokerThread.start();
        
        AlertThread.reset();
        alertInvokerThread_ = new AlertInvokerThread();
        Thread alertInvokerThread = new Thread(alertInvokerThread_);
        alertInvokerThread.start();
        GlobalVariables.alertInvokerThread = alertInvokerThread_;
        
        cleanupInvokerThread_ = new CleanupInvokerThread();
        Thread cleanupInvokerThread = new Thread(cleanupInvokerThread_);
        cleanupInvokerThread.start();
        GlobalVariables.cleanupInvokerThread = cleanupInvokerThread_;
        
        internalStatsInvokerThread_ = new InternalStatsInvokerThread();
        Thread internalStatsInvokerThread = new Thread(internalStatsInvokerThread_);
        internalStatsInvokerThread.start();
        
        // startup netty servers
        boolean isStartupServerListenersSuccess = startServerListeners();
   
        // set the start time for statsagg
        GlobalVariables.statsaggStartTimestamp.set(System.currentTimeMillis());

        // return true if all startup routines were successful 
        if (isLogbackSuccess && isApplicationConfigurationSuccess && initializeDatabaseSuccess && isStartupServerListenersSuccess) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private boolean readAndSetLogbackConfiguration() {

        String logbackXmlConfigString = null;
        InputStream inputStreamString = null;
        
        try {
            InputStream logbackConfigurationInputStream;
            String customLogbackConfLocation = System.getProperty("saLogbackConfLocation");

            if ((customLogbackConfLocation == null) || customLogbackConfLocation.isEmpty()) {
                logbackConfigurationInputStream = initializerContext_.getResourceAsStream(File.separator + "WEB-INF" + File.separator + "config" + File.separator + "logback_config.xml");
            }
            else {
                logbackConfigurationInputStream = new FileInputStream(new File(customLogbackConfLocation));
            }
            
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(logbackConfigurationInputStream, stringWriter, "UTF-8");
            logbackXmlConfigString = stringWriter.toString();
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
        
        inputStreamString = IOUtils.toInputStream(logbackXmlConfigString);
        
        if (inputStreamString == null) {
            return false;
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset(); 
            configurator.doConfigure(inputStreamString);
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
            return true;
        } 
        catch (Exception e) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
            return false;
        }
        
    }
    
    // try to load application.properties
    private boolean readAndSetApplicationConfiguration() {
        
        InputStream applicationConfigurationInputStream = null;
        boolean isUsingDefaultSettings = false, isConfigFileMissing = false;
        
        try {
            String customAppConfLocation = System.getProperty("saAppConfLocation");
        
            if ((customAppConfLocation == null) || customAppConfLocation.isEmpty()) {
                applicationConfigurationInputStream = initializerContext_.getResourceAsStream(File.separator + "WEB-INF" + File.separator + "config" + File.separator + "application.properties"); 
            }
            else {
                applicationConfigurationInputStream = new FileInputStream(new File(customAppConfLocation));
            }
            
            if (applicationConfigurationInputStream.available() <= 0) isConfigFileMissing = true;
        }
        catch (Exception e) {
            isConfigFileMissing = true;
        }
        
        if (isConfigFileMissing) {
            logger.warn("Failed to load application.properties. Using StatsAgg application configuration defaults...");
            applicationConfigurationInputStream = new ByteArrayInputStream("".getBytes());
            isUsingDefaultSettings = true;
        }
        
        boolean isApplicationConfigSuccess = ApplicationConfiguration.initialize(applicationConfigurationInputStream, isUsingDefaultSettings);
        if ((ApplicationConfiguration.getApplicationConfiguration() == null) || (ApplicationConfiguration.getApplicationConfiguration().getPropertiesConfiguration() == null) || !isApplicationConfigSuccess) {
            return false;
        }
        
        if (applicationConfigurationInputStream != null) {
            try {
                applicationConfigurationInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return true;
    }
    
    public boolean initializeDatabaseFromContext() {
       
        InputStream databaseConfigurationInputStream = null;
        boolean isDatabaseInitializeSuccess = false, isDatabaseGetConfigSuccess = false, isConfigFileMissing = false;

        try {
            String customDbConfLocation = System.getProperty("saDbConfLocation");
        
            if ((customDbConfLocation == null) || customDbConfLocation.isEmpty()) {
                databaseConfigurationInputStream = initializerContext_.getResourceAsStream(File.separator + "WEB-INF" + File.separator + "config" + File.separator + "database.properties");
            }
            else {
                databaseConfigurationInputStream = new FileInputStream(new File(customDbConfLocation));
            }
            
            if (databaseConfigurationInputStream.available() <= 0) isConfigFileMissing = true;
            else GlobalVariables.isStatsaggUsingInMemoryDatabase.set(false);
        }
        catch (Exception e) {
            isConfigFileMissing = true;
        }
            
        if (isConfigFileMissing) {
            logger.warn("Failed to load database.properties. Using an ephemeral (in-memory) database...");

            databaseConfigurationInputStream = getEphemeralDatabaseConfiguration();
            GlobalVariables.isStatsaggUsingInMemoryDatabase.set(true);
        }
        
        isDatabaseGetConfigSuccess = readAndSetDatabaseConfiguration(databaseConfigurationInputStream);
        isDatabaseInitializeSuccess = connectToDatabase();
     
        if (!isDatabaseGetConfigSuccess || !isDatabaseInitializeSuccess) {
            logger.warn("Failed to connect to database. Using an ephemeral (in-memory) database...");

            if (databaseConfigurationInputStream != null) {
                try {databaseConfigurationInputStream.close();}
                catch (Exception e) {}
            }
            
            databaseConfigurationInputStream = getEphemeralDatabaseConfiguration();
            GlobalVariables.isStatsaggUsingInMemoryDatabase.set(true);
            
            isDatabaseGetConfigSuccess = readAndSetDatabaseConfiguration(databaseConfigurationInputStream);
            isDatabaseInitializeSuccess = connectToDatabase();
        }
        
        if (databaseConfigurationInputStream != null) {
            try {
                databaseConfigurationInputStream.close();
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return isDatabaseGetConfigSuccess && isDatabaseInitializeSuccess;
    }
    
    public InputStream getEphemeralDatabaseConfiguration() {
        StringBuilder defaultDatabase = new StringBuilder();
        defaultDatabase.append("db_type = derby_embedded\n");
        defaultDatabase.append("db_custom_jdbc = jdbc:derby:memory:statsagg_mem_db;create=true\n");
        defaultDatabase.append("derby.storage.pageCacheSize = 15000\n");

        InputStream databaseConfigurationInputStream = new ByteArrayInputStream(defaultDatabase.toString().getBytes());
        return databaseConfigurationInputStream;
    }
    
    public boolean initializeDatabaseFromFile(String filePath, String fileName) {
       
        boolean isDatabaseGetConfigSuccess = false;
        boolean isDatabaseInitializeSuccess = false;

        isDatabaseGetConfigSuccess = readAndSetDatabaseConfiguration(filePath, fileName);

        if (isDatabaseGetConfigSuccess) {
            isDatabaseInitializeSuccess = connectToDatabase();
        }
        
        return isDatabaseGetConfigSuccess && isDatabaseInitializeSuccess;
    }
    
    public boolean initializeDatabaseFromInputStream(InputStream configurationInputStream) {
       
        boolean isDatabaseGetConfigSuccess = false;
        boolean isDatabaseInitializeSuccess = false;

        isDatabaseGetConfigSuccess = readAndSetDatabaseConfiguration(configurationInputStream);

        if (isDatabaseGetConfigSuccess) {
            isDatabaseInitializeSuccess = connectToDatabase();
        }
        
        return isDatabaseGetConfigSuccess && isDatabaseInitializeSuccess;
    }
    
    private boolean readAndSetDatabaseConfiguration(String filePath, String filename) {
        
        DatabaseConfiguration.initialize(filePath + File.separator + filename);
        
        if (DatabaseConfiguration.isInitializeSuccess()) {
            DatabaseConnections.setJdbc(DatabaseConfiguration.getJdbcConnectionString());
            
            return true;
        }
        else {
            logger.error("Failed to read and set database configuration properties");
            return false;
        }
    }
    
    private boolean readAndSetDatabaseConfiguration(InputStream configurationInputStream) {
        
        DatabaseConfiguration.initialize(configurationInputStream);

        if (DatabaseConfiguration.isInitializeSuccess()) {
            DatabaseConnections.setJdbc(DatabaseConfiguration.getJdbcConnectionString());
            
            return true;
        }
        else {
            logger.error("Failed to read and set database configuration properties");
            return false;
        }
    }
    
    private static boolean connectToDatabase() {
        
        DatabaseConnections.setDriver();
        DatabaseConnections.createConnectionPool();
        Connection connection = DatabaseConnections.getConnection();
        
        boolean isSuccessfulConnection = true;
        
        try {
            if ((connection == null) || connection.isClosed() || !connection.isValid(0)) {
                logger.error("Failed to connect to database");
                isSuccessfulConnection = false;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + File.separator + StackTrace.getStringFromStackTrace(e));
            logger.error("Failed to connect to database");
            isSuccessfulConnection = false;
        }
        
        try {
            if (connection != null) {
                connection.close();
            }
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            connection = null;
        }
        
        return isSuccessfulConnection;
    }
    
    public boolean createDatabaseSchemas() {
        MetricLastSeenDao metricLastSeenDao = new MetricLastSeenDao();
        boolean isMetricLastSeenDaoCreateSuccess = metricLastSeenDao.createTable();
        
        GaugesDao gaugesDao = new GaugesDao();
        boolean isGaugesCreateSuccess = gaugesDao.createTable();
   
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        boolean isMetricGroupsCreateSuccess = metricGroupsDao.createTable();
        
        MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao();
        boolean isMetricGroupRegexesCreateSuccess = metricGroupRegexesDao.createTable();
        
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
        boolean isMetricGroupTagsCreateSuccess = metricGroupTagsDao.createTable();
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        boolean isNotificationGroupsCreateSuccess = notificationGroupsDao.createTable();
        
        AlertsDao alertsDao = new AlertsDao();
        boolean isAlertsCreateSuccess = alertsDao.createTable();
        
        AlertSuspensionsDao AlertSuspensionsDao = new AlertSuspensionsDao();
        boolean isAlertSuspensionsCreateSuccess = AlertSuspensionsDao.createTable();
        
        boolean isSchemaCreateSuccess = isMetricLastSeenDaoCreateSuccess 
                && isGaugesCreateSuccess 
                && isMetricGroupsCreateSuccess 
                && isMetricGroupRegexesCreateSuccess
                && isMetricGroupTagsCreateSuccess
                && isNotificationGroupsCreateSuccess
                && isAlertsCreateSuccess 
                && isAlertSuspensionsCreateSuccess;
        
        return isSchemaCreateSuccess;
    }

    public static void createGraphiteAggregatorMetricPrefix() {
        StringBuilder prefixBuilder = new StringBuilder();

        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled() && (ApplicationConfiguration.getGlobalMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        }
        
        if (ApplicationConfiguration.isGraphiteAggregatorMetricNamePrefixEnabled() && (ApplicationConfiguration.getGraphiteAggregatorMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getGraphiteAggregatorMetricNamePrefixValue()).append(".");
        }
        
        GlobalVariables.graphiteAggregatedPrefix = prefixBuilder.toString();
    }
    
    public static void createGraphitePassthroughMetricPrefix() {
        StringBuilder prefixBuilder = new StringBuilder();
        
        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled() && (ApplicationConfiguration.getGlobalMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        }
        
        if (ApplicationConfiguration.isGraphitePassthroughMetricNamePrefixEnabled() && (ApplicationConfiguration.getGraphitePassthroughMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getGraphitePassthroughMetricNamePrefixValue()).append(".");
        }

        GlobalVariables.graphitePassthroughPrefix = prefixBuilder.toString();
    }
    
    public static void createOpenTsdbMetricPrefix() {
        StringBuilder prefixBuilder = new StringBuilder();

        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled() && (ApplicationConfiguration.getGlobalMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        }
        
        if (ApplicationConfiguration.isOpenTsdbMetricNamePrefixEnabled() && (ApplicationConfiguration.getOpenTsdbMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getOpenTsdbMetricNamePrefixValue()).append(".");
        }

        GlobalVariables.openTsdbPrefix = prefixBuilder.toString();
    }
    
    public static void createInfluxdbMetricPrefix() {
        StringBuilder prefixBuilder = new StringBuilder();

        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled() && (ApplicationConfiguration.getGlobalMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        }
        
        if (ApplicationConfiguration.isInfluxdbMetricNamePrefixEnabled() && (ApplicationConfiguration.getInfluxdbMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getInfluxdbMetricNamePrefixValue()).append(".");
        }

        GlobalVariables.influxdbPrefix = prefixBuilder.toString();
    }
    
    private static long readMetricLastSeenFromDatabaseAndAddToGlobalVariables() {
        
        MetricLastSeenDao metricLastSeenDao = new MetricLastSeenDao();
        List<MetricLastSeen> metricLastSeens = metricLastSeenDao.getAllDatabaseObjectsInTable();
        
        for (MetricLastSeen metricLastSeen : metricLastSeens) {
            try {
                if ((metricLastSeen.getMetricKey() == null) || (metricLastSeen.getLastModified() == null)) continue;
                
                synchronized (GlobalVariables.recentMetricTimestampsAndValuesByMetricKey) {
                    List<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricLastSeen.getMetricKey());

                    if (metricTimestampsAndValues == null) {
                        metricTimestampsAndValues = Collections.synchronizedList(new ArrayList<MetricTimestampAndValue>());
                        GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.put(metricLastSeen.getMetricKey(), metricTimestampsAndValues);
                    }
                }
                
                GlobalVariables.metricKeysLastSeenTimestamp.putIfAbsent(metricLastSeen.getMetricKey(), metricLastSeen.getLastModified().getTime());
                GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.putIfAbsent(metricLastSeen.getMetricKey(), metricLastSeen.getLastModified().getTime());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return metricLastSeens.size();
    }
    
    private static long readGaugesFromDatabaseAndAddToGlobalVariables() {
        GaugesDao gaugeDao = new GaugesDao(false);
        
        if (!ApplicationConfiguration.isStatsdGaugeSendPreviousValue() || !ApplicationConfiguration.isStatsdPersistGauges()) {
            gaugeDao.truncateTable();
            gaugeDao.close();
            return 0;
        }
        
        List<Gauge> gauges = gaugeDao.getAllDatabaseObjectsInTable();
        gaugeDao.close();
        
        for (Gauge gauge : gauges) {
            try {
                StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(gauge.getBucket(), gauge.getMetricValue(), 
                        System.currentTimeMillis(), StatsdMetricAggregated.GAUGE_TYPE);
                statsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());

                GlobalVariables.statsdMetricsAggregatedMostRecentValue.putIfAbsent(gauge.getBucket(), statsdMetricAggregated);
                GlobalVariables.metricKeysLastSeenTimestamp.putIfAbsent(gauge.getBucket(), gauge.getLastModified().getTime());
                GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.putIfAbsent(gauge.getBucket(), gauge.getLastModified().getTime());
                GlobalVariables.statsdGaugeCache.putIfAbsent(gauge.getBucket(), gauge);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return gauges.size();
    }

    private boolean startServerListeners() {
        
        boolean isStartupSuccess = true;
        
        try {
            // start the netty statsd tcp server
            if (ApplicationConfiguration.isStatsdTcpListenerEnabled()) {
                statsdTcpServer_ = new TcpServer(ApplicationConfiguration.getStatsdTcpListenerPort(), TcpServer.SERVER_TYPE_STATSD);
                Thread statsdTcpServerThread = new Thread(statsdTcpServer_);
                statsdTcpServerThread.start();
                if (!statsdTcpServer_.isInitializeSuccess()) isStartupSuccess = false;
            }
            
            // start the netty statsd udp server
            if (ApplicationConfiguration.isStatsdUdpListenerEnabled()) {
                statsdUdpServer_ = new UdpServer(ApplicationConfiguration.getStatsdUdpListenerPort(), UdpServer.SERVER_TYPE_STATSD);
                Thread statsdUdpServerThread = new Thread(statsdUdpServer_);
                statsdUdpServerThread.start();
                if (!statsdUdpServer_.isInitializeSuccess()) isStartupSuccess = false;
            }
          
            // start the netty graphite aggregator tcp server
            if (ApplicationConfiguration.isGraphiteAggregatorTcpListenerEnabled()) {
                graphiteAggregatorTcpServer_ = new TcpServer(ApplicationConfiguration.getGraphiteAggregatorTcpListenerPort(), TcpServer.SERVER_TYPE_GRAPHITE_AGGREGATOR);
                Thread graphiteAggregatorTcpServerThread = new Thread(graphiteAggregatorTcpServer_);
                graphiteAggregatorTcpServerThread.start();
                if (!graphiteAggregatorTcpServer_.isInitializeSuccess()) isStartupSuccess = false;
            }
            
            // start the netty graphite aggregator udp server
            if (ApplicationConfiguration.isGraphiteAggregatorUdpListenerEnabled()) {
                graphiteAggregatorUdpServer_ = new UdpServer(ApplicationConfiguration.getGraphiteAggregatorUdpListenerPort(), UdpServer.SERVER_TYPE_GRAPHITE_AGGREGATOR);
                Thread graphiteAggregatorUdpServerThread = new Thread(graphiteAggregatorUdpServer_);
                graphiteAggregatorUdpServerThread.start();
                if (!graphiteAggregatorUdpServer_.isInitializeSuccess()) isStartupSuccess = false;
            }
            
            // start the netty graphite passthrough tcp server
            if (ApplicationConfiguration.isGraphitePassthroughTcpListenerEnabled()) {
                graphitePassthroughTcpServer_ = new TcpServer(ApplicationConfiguration.getGraphitePassthroughTcpListenerPort(), TcpServer.SERVER_TYPE_GRAPHITE_PASSTHROUGH);
                Thread graphitePassthroughTcpServerThread = new Thread(graphitePassthroughTcpServer_);
                graphitePassthroughTcpServerThread.start();
                if (!graphitePassthroughTcpServer_.isInitializeSuccess()) isStartupSuccess = false;
            }
            
            // start the netty graphite passthrough udp server
            if (ApplicationConfiguration.isGraphitePassthroughUdpListenerEnabled()) {
                graphitePassthroughUdpServer_ = new UdpServer(ApplicationConfiguration.getGraphitePassthroughUdpListenerPort(), UdpServer.SERVER_TYPE_GRAPHITE_PASSTHROUGH);
                Thread graphitePassthroughUdpServerThread = new Thread(graphitePassthroughUdpServer_);
                graphitePassthroughUdpServerThread.start();
                if (!graphitePassthroughUdpServer_.isInitializeSuccess()) isStartupSuccess = false;
            }
            
            // start the netty opentsdb tcp server
            if (ApplicationConfiguration.isOpenTsdbTcpTelnetListenerEnabled()) {
                openTsdbTcpServer_ = new TcpServer(ApplicationConfiguration.getOpenTsdbTcpTelnetListenerPort(), TcpServer.SERVER_TYPE_OPENTSDB_TELNET);
                Thread openTsdbTcpServerThread = new Thread(openTsdbTcpServer_);
                openTsdbTcpServerThread.start();
                if (!openTsdbTcpServer_.isInitializeSuccess()) isStartupSuccess = false;
            }
            
            // start the opentsdb jetty http server
            if (ApplicationConfiguration.isOpenTsdbHttpListenerEnabled()) {
                jettyOpenTsdb_ = new JettyOpenTsdb(ApplicationConfiguration.getOpenTsdbHttpListenerPort(), 30000);
                jettyOpenTsdb_.startServer();
            }
            
            // start the influxdb jetty http server
            if (ApplicationConfiguration.isInfluxdbHttpListenerEnabled()) {
                jettyInfluxdb_ = new JettyInfluxdb(ApplicationConfiguration.getInfluxdbHttpListenerPort(), 30000);
                jettyInfluxdb_.startServer();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + File.separator + StackTrace.getStringFromStackTrace(e));
            logger.error("Failed to start a netty server. Please view the log files for more details.");
            
            isStartupSuccess = false;
        }

        return isStartupSuccess;
    }
    
    private void startSendToOutputModuleThreadPoolManager() {
        SendMetricsToOutputModule_ThreadPoolManager.start();
    }
    
    private void startSendEmailThreadPool() {
        SendEmail_ThreadPoolManager.start();
    }
    
    private void shutdownServerListeners() {
        
        logger.info("Start - shutting down server listeners");
        
        List<Thread> shutdownServerThreads = new ArrayList<>();

        ShutdownNettyServer shutdownStatsdTcpServer = new ShutdownNettyServer(statsdTcpServer_);
        Thread threadShutdownStatsdTcpServer_ = new Thread(shutdownStatsdTcpServer);
        shutdownServerThreads.add(threadShutdownStatsdTcpServer_);
        
        ShutdownNettyServer shutdownStatsdUdpServer = new ShutdownNettyServer(statsdUdpServer_);
        Thread threadShutdownStatsdUdpServer_ = new Thread(shutdownStatsdUdpServer);
        shutdownServerThreads.add(threadShutdownStatsdUdpServer_);
        
        ShutdownNettyServer shutdownGraphiteAggregatorTcpServer = new ShutdownNettyServer(graphiteAggregatorTcpServer_);
        Thread threadShutdownGraphiteAggregatorTcpServer_ = new Thread(shutdownGraphiteAggregatorTcpServer);
        shutdownServerThreads.add(threadShutdownGraphiteAggregatorTcpServer_);
        
        ShutdownNettyServer shutdownGraphiteAggregatorUdpServer = new ShutdownNettyServer(graphiteAggregatorUdpServer_);
        Thread threadShutdownGraphiteAggregatorUdpServer_ = new Thread(shutdownGraphiteAggregatorUdpServer);
        shutdownServerThreads.add(threadShutdownGraphiteAggregatorUdpServer_);
        
        ShutdownNettyServer shutdownGraphitePassthroughTcpServer = new ShutdownNettyServer(graphitePassthroughTcpServer_);
        Thread threadShutdownGraphitePassthroughTcpServer_ = new Thread(shutdownGraphitePassthroughTcpServer);
        shutdownServerThreads.add(threadShutdownGraphitePassthroughTcpServer_);
        
        ShutdownNettyServer shutdownGraphitePassthroughUdpServer = new ShutdownNettyServer(graphitePassthroughUdpServer_);
        Thread threadShutdownGraphitePassthroughUdpServer_ = new Thread(shutdownGraphitePassthroughUdpServer);
        shutdownServerThreads.add(threadShutdownGraphitePassthroughUdpServer_);
        
        ShutdownNettyServer shutdownOpenTsdbTcpServer = new ShutdownNettyServer(openTsdbTcpServer_);
        Thread threadShutdownOpenTsdbTcpServer_ = new Thread(shutdownOpenTsdbTcpServer);
        shutdownServerThreads.add(threadShutdownOpenTsdbTcpServer_);
        
        ShutdownJettyServer shutdownOpenTsdbJettyServer = new ShutdownJettyServer(jettyOpenTsdb_);
        Thread threadShutdownOpenTsdbJettyServer_ = new Thread(shutdownOpenTsdbJettyServer);
        shutdownServerThreads.add(threadShutdownOpenTsdbJettyServer_);
        
        ShutdownJettyServer shutdownInfluxdbJettyServer = new ShutdownJettyServer(jettyInfluxdb_);
        Thread threadShutdownInfluxdbJettyServer_ = new Thread(shutdownInfluxdbJettyServer);
        shutdownServerThreads.add(threadShutdownInfluxdbJettyServer_);
        
        Threads.threadExecutorCachedPool(shutdownServerThreads, 30, TimeUnit.SECONDS);

        logger.info("Finish - shutting down server listeners");
    }
    
    private void shutdownLogger() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.stop();
        }
        catch (Exception e) {
            logger.error(e.toString() + File.separator + StackTrace.getStringFromStackTrace(e));
            logger.error("Failed to shutdown logger");
        }
    }
    
    private void shutdownInvokerThreads() {
        logger.info("Start - shutting down invoker threads");
        
        List<Thread> shutdownThreadInvokerThreads = new ArrayList<>();
        
        ShutdownInvokerThread_Thread shutdownStatsdAggregationInvokerThread = new ShutdownInvokerThread_Thread(statsdAggregationInvokerThread_);
        Thread shutdownStatsdAggregationInvokerThread_Thread = new Thread(shutdownStatsdAggregationInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownStatsdAggregationInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownGraphiteAggregationInvokerThread = new ShutdownInvokerThread_Thread(graphiteAggregationInvokerThread_);
        Thread shutdownGraphiteAggregationInvokerThread_Thread = new Thread(shutdownGraphiteAggregationInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownGraphiteAggregationInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownGraphitePassthroughInvokerThread = new ShutdownInvokerThread_Thread(graphitePassthroughInvokerThread_);
        Thread shutdownGraphitePassthroughAggregationInvokerThread_Thread = new Thread(shutdownGraphitePassthroughInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownGraphitePassthroughAggregationInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownOpenTsdbInvokerThread = new ShutdownInvokerThread_Thread(openTsdbInvokerThread_);
        Thread shutdownOpenTsdbAggregationInvokerThread_Thread = new Thread(shutdownOpenTsdbInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownOpenTsdbAggregationInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownInfluxdbInvokerThread = new ShutdownInvokerThread_Thread(influxdbInvokerThread_);
        Thread shutdownInfluxdbAggregationInvokerThread_Thread = new Thread(shutdownInfluxdbInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownInfluxdbAggregationInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownAlertInvokerThread = new ShutdownInvokerThread_Thread(alertInvokerThread_);
        Thread shutdownAlertInvokerThread_Thread = new Thread(shutdownAlertInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownAlertInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownCleanupInvokerThread = new ShutdownInvokerThread_Thread(cleanupInvokerThread_);
        Thread shutdownCleanupInvokerThread_Thread = new Thread(shutdownCleanupInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownCleanupInvokerThread_Thread);

        ShutdownInvokerThread_Thread shutdownInternalStatsInvokerThread = new ShutdownInvokerThread_Thread(internalStatsInvokerThread_);
        Thread shutdownInternalStatsInvokerThread_Thread = new Thread(shutdownInternalStatsInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownInternalStatsInvokerThread_Thread);
        
        Threads.threadExecutorCachedPool(shutdownThreadInvokerThreads, 2, TimeUnit.MINUTES);

        logger.info("Finish - shutting down invoker threads");
    }
    
    private void shutdownSendToOutputModuleThreadPoolManager() {
        logger.info("Start - shutting down 'send to output modules' thread pool");
        SendMetricsToOutputModule_ThreadPoolManager.shutdown();
        logger.info("Finish - shutting down 'send to output modules' thread pool");
    }
    
    private void shutdownSendEmailThreadPool() {
        logger.info("Start - shutting down 'send email' thread pool");
        SendEmail_ThreadPoolManager.shutdown();
        logger.info("Finish - shutting down 'send email' thread pool");
    }
        
    private static class ShutdownNettyServer implements Runnable {
    
        private NettyServer server__;
        
        public ShutdownNettyServer(NettyServer server) {
            this.server__ = server;
        }
        
        @Override
        public void run() {
            if (server__ != null) {
                server__.shutdownServer();
                server__ = null;
            }
        }
        
    }
    
    private static class ShutdownJettyServer implements Runnable {
    
        private JettyServer server__;
        
        public ShutdownJettyServer(JettyServer server) {
            this.server__ = server;
        }
        
        @Override
        public void run() {
            if (server__ != null) {
                server__.stopServer();
                server__ = null;
            }
        }
        
    }
    
    private static class ShutdownInvokerThread_Thread implements Runnable {
    
        private final InvokerThread invokerThread__;
        
        public ShutdownInvokerThread_Thread(InvokerThread invokerThread) {
            this.invokerThread__ = invokerThread;
        }
        
        @Override
        public void run() {
            if (invokerThread__ != null) {
                invokerThread__.shutdown();
                while (!invokerThread__.isShutdown()) {
                    Threads.sleepMilliseconds(100);
                }
            }
        }
        
    }

}
