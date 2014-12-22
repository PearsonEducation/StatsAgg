package com.pearson.statsagg.controller;

import com.pearson.statsagg.controller.threads.InvokerThread;
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
import com.pearson.statsagg.controller.threads.InternalStatsInvokerThread;
import com.pearson.statsagg.controller.threads.SendEmailThreadPoolManager;
import com.pearson.statsagg.controller.threads.SendToGraphiteThreadPoolManager;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.gauges.Gauge;
import com.pearson.statsagg.database.gauges.GaugesDao;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.database.metric_last_seen.MetricLastSeen;
import com.pearson.statsagg.database.metric_last_seen.MetricLastSeenDao;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_aggregation.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.network.NettyServer;
import com.pearson.statsagg.network.tcp.TcpServer;
import com.pearson.statsagg.network.udp.UdpServer;
import com.pearson.statsagg.utilities.Threads;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
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
    private StatsdAggregationInvokerThread statsdAggregationInvokerThread_ = null;
    private GraphiteAggregationInvokerThread graphiteAggregationInvokerThread_ = null;
    private GraphitePassthroughInvokerThread graphitePassthroughInvokerThread_ = null;
    private AlertInvokerThread alertInvokerThread_ = null;
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
        
        shutdownSendToGraphiteThreadPool();
                
        shutdownSendEmailThreadPool();
        
        DatabaseConnections.disconnectAndShutdown();
        DatabaseConnections.deregisterJdbcDriver();
        
        shutdownLogger();
        
        logger.info("Initializer - Context Destroyed");
    }
    
    private boolean initializeApplication() {
        
        // load the logger configuration file & initialize it
        InputStream logbackConfigurationInputStream = initializerContext_.getResourceAsStream(File.separator + "WEB-INF" + File.separator + "config" + File.separator + "logback_config.xml");
        boolean isLogbackSuccess = readAndSetLogbackConfiguration(logbackConfigurationInputStream);

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
        if (initializeDatabaseSuccess && ApplicationConfiguration.isStatsdGaugeSendPreviousValue()) {
            long numGaugesFromDatabase = readGaugesFromDatabaseAndAddToGlobalVariables();
            logger.info("Finished adding gauges from database to recent metric global history. NumGaugesFromDbAddedToGlobal=" + numGaugesFromDatabase);
        }
        
        // start the thread pool that is responsible for sending alert emails 
        startSendEmailThreadPool();
        
        // start the thread pool that is responsible for threads sending metrics to graphite 
        startSendToGraphiteThreadPool();
        
        // set last alert executed routine timestamp to '0', which indicates to the rest of the program that it has never been executed
        GlobalVariables.alertRountineLastExecutedTimestamp.set(0);
        
        // starts the thread that calls the statsd & graphite aggregation routines on a timer (specified in the application config file)
        statsdAggregationInvokerThread_ = new StatsdAggregationInvokerThread();
        Thread statsdAggregationInvokerThread = new Thread(statsdAggregationInvokerThread_);
        statsdAggregationInvokerThread.start();
        
        graphiteAggregationInvokerThread_ = new GraphiteAggregationInvokerThread();
        Thread graphiteAggregationInvokerThread = new Thread(graphiteAggregationInvokerThread_);
        graphiteAggregationInvokerThread.start();
        
        graphitePassthroughInvokerThread_ = new GraphitePassthroughInvokerThread();
        Thread graphitePassthroughInvokerThread = new Thread(graphitePassthroughInvokerThread_);
        graphitePassthroughInvokerThread.start();
        
        AlertThread.reset();
        alertInvokerThread_ = new AlertInvokerThread();
        Thread alertInvokerThread = new Thread(alertInvokerThread_);
        alertInvokerThread.start();
        
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
    
    private boolean readAndSetLogbackConfiguration(InputStream inputStream) {

        if (inputStream == null) {
            return false;
        }

        String logbackXmlConfigString = null;
        InputStream inputStreamString = null;
        
        try {
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(inputStream, stringWriter, "UTF-8");
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
            applicationConfigurationInputStream = initializerContext_.getResourceAsStream(File.separator + "WEB-INF" + File.separator + "config" + File.separator + "application.properties"); 
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
        if ((ApplicationConfiguration.getApplicationConfiguration() == null) || !isApplicationConfigSuccess) {
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
            databaseConfigurationInputStream = initializerContext_.getResourceAsStream(File.separator + "WEB-INF" + File.separator + "config" + File.separator + "database.properties");
            if (databaseConfigurationInputStream.available() <= 0) isConfigFileMissing = true;
            else GlobalVariables.isStatsaggUsingInMemoryDatabase.set(false);
        }
        catch (Exception e) {
            isConfigFileMissing = true;
        }
            
        if (isConfigFileMissing) {
            logger.warn("Failed to load database.properties. Using an ephemeral (in-memory) database...");

            StringBuilder defaultDatabase = new StringBuilder("");
            defaultDatabase.append("db_type = derby_embedded\n");
            defaultDatabase.append("db_custom_jdbc = jdbc:derby:memory:statsagg_mem_db;create=true\n");
            defaultDatabase.append("derby.storage.pageCacheSize = 15000\n");

            databaseConfigurationInputStream = new ByteArrayInputStream(defaultDatabase.toString().getBytes());
            GlobalVariables.isStatsaggUsingInMemoryDatabase.set(true);
        }
        
        isDatabaseGetConfigSuccess = readAndSetDatabaseConfiguration(databaseConfigurationInputStream);
        isDatabaseInitializeSuccess = connectToDatabase();
     
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
    
    public boolean initializeDatabaseFromFile(String filePath, String fileName) {
       
        boolean isDatabaseGetConfigSuccess = false;
        boolean isDatabaseInitializeSuccess = false;

        isDatabaseGetConfigSuccess = readAndSetDatabaseConfiguration(filePath, fileName);

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
    
    private static boolean createDatabaseSchemas() {
        MetricLastSeenDao metricLastSeenDao = new MetricLastSeenDao();
        boolean isMetricLastSeenDaoCreateSuccess = metricLastSeenDao.createTable();
        
        GaugesDao gaugesDao = new GaugesDao();
        boolean isGaugesCreateSuccess = gaugesDao.createTable();
   
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        boolean isMetricGroupsCreateSuccess = metricGroupsDao.createTable();
        
        MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
        boolean isMetricGroupRegexsCreateSuccess = metricGroupRegexsDao.createTable();
        
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
                && isMetricGroupRegexsCreateSuccess
                && isMetricGroupTagsCreateSuccess
                && isNotificationGroupsCreateSuccess
                && isAlertsCreateSuccess 
                && isAlertSuspensionsCreateSuccess;
        
        return isSchemaCreateSuccess;
    }

    private static long readMetricLastSeenFromDatabaseAndAddToGlobalVariables() {
        
        MetricLastSeenDao metricLastSeenDao = new MetricLastSeenDao();
        List<MetricLastSeen> metricLastSeens = metricLastSeenDao.getAllDatabaseObjectsInTable();
        
        for (MetricLastSeen metricLastSeen : metricLastSeens) {
            try {
                if ((metricLastSeen.getMetricKey() == null) || (metricLastSeen.getLastModified() == null)) continue;
                
                synchronized (GlobalVariables.recentMetricTimestampsAndValuesByMetricKey) {
                    Set<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricLastSeen.getMetricKey());

                    if (metricTimestampsAndValues == null) {
                        metricTimestampsAndValues = Collections.synchronizedSet(new TreeSet<>(MetricTimestampAndValue.COMPARE_BY_TIMESTAMP));
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
        GaugesDao gaugeDao = new GaugesDao();
        List<Gauge> gauges = gaugeDao.getAllDatabaseObjectsInTable();
        
        for (Gauge gauge : gauges) {
            try {
                StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(gauge.getBucket(), gauge.getMetricValue(), 
                        System.currentTimeMillis(), StatsdMetricAggregated.GAUGE_TYPE);
                statsdMetricAggregated.setHashKey(GlobalVariables.aggregatedMetricHashKeyGenerator.incrementAndGet());

                GlobalVariables.statsdMetricsAggregatedMostRecentValue.putIfAbsent(gauge.getBucket(), statsdMetricAggregated);
                GlobalVariables.statsdGaugeCache.putIfAbsent(gauge.getBucket(), gauge);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return gauges.size();
    }

    private boolean startServerListeners() {
        
        boolean isStartupSuccess = false;
        
        try {
            // start the netty statsd tcp server
            if (ApplicationConfiguration.isStatsdTcpListenerEnabled()) {
                statsdTcpServer_ = new TcpServer(ApplicationConfiguration.getStatsdTcpListenerPort(), TcpServer.SERVER_TYPE_STATSD);
                Thread statsdTcpServerThread = new Thread(statsdTcpServer_);
                statsdTcpServerThread.start();
            }
            
            // start the netty statsd udp server
            if (ApplicationConfiguration.isStatsdUdpListenerEnabled()) {
                statsdUdpServer_ = new UdpServer(ApplicationConfiguration.getStatsdUdpListenerPort(), UdpServer.SERVER_TYPE_STATSD);
                Thread statsdUdpServerThread = new Thread(statsdUdpServer_);
                statsdUdpServerThread.start();
            }
          
            // start the netty graphite aggregator tcp server
            if (ApplicationConfiguration.isGraphiteAggregatorTcpListenerEnabled()) {
                graphiteAggregatorTcpServer_ = new TcpServer(ApplicationConfiguration.getGraphiteAggregatorTcpListenerPort(), TcpServer.SERVER_TYPE_GRAPHITE_AGGREGATOR);
                Thread graphiteAggregatorTcpServerThread = new Thread(graphiteAggregatorTcpServer_);
                graphiteAggregatorTcpServerThread.start();
            }
            
            // start the netty graphite aggregator udp server
            if (ApplicationConfiguration.isGraphiteAggregatorUdpListenerEnabled()) {
                graphiteAggregatorUdpServer_ = new UdpServer(ApplicationConfiguration.getGraphiteAggregatorUdpListenerPort(), UdpServer.SERVER_TYPE_GRAPHITE_AGGREGATOR);
                Thread graphiteAggregatorUdpServerThread = new Thread(graphiteAggregatorUdpServer_);
                graphiteAggregatorUdpServerThread.start();
            }
            
            // start the netty graphite passthrough tcp server
            if (ApplicationConfiguration.isGraphitePassthroughTcpListenerEnabled()) {
                graphitePassthroughTcpServer_ = new TcpServer(ApplicationConfiguration.getGraphitePassthroughTcpListenerPort(), TcpServer.SERVER_TYPE_GRAPHITE_PASSTHROUGH);
                Thread graphitePassthroughTcpServerThread = new Thread(graphitePassthroughTcpServer_);
                graphitePassthroughTcpServerThread.start();
            }
            
            // start the netty graphite passthrough udp server
            if (ApplicationConfiguration.isGraphitePassthroughUdpListenerEnabled()) {
                graphitePassthroughUdpServer_ = new UdpServer(ApplicationConfiguration.getGraphitePassthroughUdpListenerPort(), UdpServer.SERVER_TYPE_GRAPHITE_PASSTHROUGH);
                Thread graphitePassthroughUdpServerThread = new Thread(graphitePassthroughUdpServer_);
                graphitePassthroughUdpServerThread.start();
            }
            
            isStartupSuccess = true;
        }
        catch (Exception e) {
            logger.error(e.toString() + File.separator + StackTrace.getStringFromStackTrace(e));
            logger.error("Failed to connect to database");
            
            isStartupSuccess = false;
        }

        return isStartupSuccess;
    }
    
    private void startSendToGraphiteThreadPool() {
        SendToGraphiteThreadPoolManager.start();
    }
    
    private void startSendEmailThreadPool() {
        SendEmailThreadPoolManager.start();
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
        
        Threads.threadExecutorCachedPool(shutdownServerThreads, 5, TimeUnit.MINUTES);

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
        
        ShutdownInvokerThread_Thread shutdownAlertInvokerThread = new ShutdownInvokerThread_Thread(alertInvokerThread_);
        Thread shutdownAlertInvokerThread_Thread = new Thread(shutdownAlertInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownAlertInvokerThread_Thread);

        ShutdownInvokerThread_Thread shutdownInternalStatsInvokerThread = new ShutdownInvokerThread_Thread(internalStatsInvokerThread_);
        Thread shutdownInternalStatsInvokerThread_Thread = new Thread(shutdownInternalStatsInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownInternalStatsInvokerThread_Thread);
        
        Threads.threadExecutorCachedPool(shutdownThreadInvokerThreads, 5, TimeUnit.MINUTES);

        logger.info("Finish - shutting down invoker threads");
    }
    
    private void shutdownSendToGraphiteThreadPool() {
        logger.info("Start - shutting down 'send to graphite' thread pool");
        
        SendToGraphiteThreadPoolManager.shutdown();
        
        logger.info("Finish - shutting down 'send to graphite' thread pool");
    }
    
    private void shutdownSendEmailThreadPool() {
        logger.info("Start - shutting down 'send email' thread pool");
        
        SendEmailThreadPoolManager.shutdown();
        
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
