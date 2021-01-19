package com.pearson.statsagg.drivers;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import com.pearson.statsagg.threads.alert_related.AlertThread;
import com.pearson.statsagg.threads.alert_related.MetricAssociation;
import com.pearson.statsagg.threads.thread_managers.SendNotification_ThreadPoolManager;
import com.pearson.statsagg.threads.thread_managers.SendMetricsToOutputModule_ThreadPoolManager;
import com.pearson.statsagg.threads.invokers.AlertInvokerThread;
import com.pearson.statsagg.threads.invokers.CleanupInvokerThread;
import com.pearson.statsagg.threads.invokers.GraphiteAggregationInvokerThread;
import com.pearson.statsagg.threads.invokers.GraphitePassthroughInvokerThread;
import com.pearson.statsagg.threads.invokers.InfluxdbV1InvokerThread;
import com.pearson.statsagg.threads.invokers.InternalStatsInvokerThread;
import com.pearson.statsagg.threads.invokers.MetricAssociationOutputBlacklistInvokerThread;
import com.pearson.statsagg.threads.invokers.OpenTsdbInvokerThread;
import com.pearson.statsagg.threads.invokers.StatsdInvokerThread;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.gauges.Gauge;
import com.pearson.statsagg.database_objects.gauges.GaugesDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_last_seen.MetricLastSeen;
import com.pearson.statsagg.database_objects.metric_last_seen.MetricLastSeenDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.flyway.FlywayOperations;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.configuration.DatabaseConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricKeyLastSeen;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_formats.statsd.StatsdMetricAggregated;
import com.pearson.statsagg.servers.JettyServer;
import com.pearson.statsagg.servers.NettyServer;
import com.pearson.statsagg.servers.http.JettyInfluxdb;
import com.pearson.statsagg.servers.http.JettyOpenTsdb;
import com.pearson.statsagg.servers.http.JettyUiAndApi;
import com.pearson.statsagg.servers.tcp.TcpServer;
import com.pearson.statsagg.servers.udp.UdpServer;
import com.pearson.statsagg.threads.invokers.TemplateInvokerThread;
import com.pearson.statsagg.utilities.core_utils.InvokerThread;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.core_utils.Threads;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.file_utils.FileIo;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Driver {
    
    private static final Logger logger = LoggerFactory.getLogger(Driver.class.getName());
        
    private static JettyUiAndApi jettyUiAndApi_ = null;
    private static TcpServer statsdTcpServer_ = null;
    private static UdpServer statsdUdpServer_ = null;
    private static TcpServer graphiteAggregatorTcpServer_ = null;
    private static UdpServer graphiteAggregatorUdpServer_ = null;
    private static TcpServer graphitePassthroughTcpServer_ = null;
    private static UdpServer graphitePassthroughUdpServer_ = null;
    private static TcpServer openTsdbTcpServer_ = null;
    private static JettyOpenTsdb jettyOpenTsdb_ = null;
    private static JettyInfluxdb jettyInfluxdb_ = null;
    private static StatsdInvokerThread statsdAggregationInvokerThread_ = null;
    private static GraphiteAggregationInvokerThread graphiteAggregationInvokerThread_ = null;
    private static GraphitePassthroughInvokerThread graphitePassthroughInvokerThread_ = null;
    private static OpenTsdbInvokerThread openTsdbInvokerThread_ = null;
    private static InfluxdbV1InvokerThread influxdbInvokerThread_ = null;
    private static MetricAssociationOutputBlacklistInvokerThread metricAssociationOutputBlacklistInvokerThread_ = null;
    private static AlertInvokerThread alertInvokerThread_ = null;
    private static TemplateInvokerThread templateInvokerThread_ = null;
    private static CleanupInvokerThread cleanupInvokerThread_ = null;
    private static InternalStatsInvokerThread internalStatsInvokerThread_ = null;
    
    public static void main(String[] args) {

        boolean initializeSuccess = initializeApplication();
        
        if (!initializeSuccess) {
            String errorOutput = "Encountered an error during initialization. Shutting down @ " + new Date();
            System.out.println(errorOutput);
            logger.error(errorOutput);
            System.exit(1);
        }
        else {
            String successOutput = "Successfully initialized @ " + new Date();
            System.out.println(successOutput);
            logger.info(successOutput);
        }

        // maintainance loop -- sit in here forever to keep main from exiting. 
        while(true) {
            Threads.sleepSeconds(15);
        }
    }
    
    private static boolean initializeApplication() {
        // register jvm shutdown routine thread
        Runtime runtime = Runtime.getRuntime(); 
        runtime.addShutdownHook(new ShutdownThread());
        
        // initialize logger
        boolean isLogbackSuccess = initializeApplication_Logger();
        
        // read app config settings
        boolean isApplicationConfigSuccess = initializeApplication_ApplicationConfiguration();

        // read db config settings
        boolean isDatabaseConfigSuccess = initializeApplication_DatabaseConfiguration(false);
        
        // connect to the database
        boolean isDatabaseConnectSuccess = isDatabaseConfigSuccess ? connectToDatabase() : false;
        
        // creates the database schema (tables, etc) if it doesn't already exist. 
        if (isDatabaseConnectSuccess) {
            boolean setupSchemaSuccess = setupDatabaseSchema();
            logger.debug("SetupDatabaseSchemaSuccess=" + setupSchemaSuccess);
        }
        
        // reads 'metric last seen' values from the database & loads the relevant values into memory. These are used by availability alerts.
        if (isDatabaseConnectSuccess) {
            long numMetrics = readMetricLastSeenFromDatabaseAndAddToGlobalVariables();
            logger.info("Finished reading 'metric last seen' values from database. NumMetricsRead=" + numMetrics);
        }        
        
        // read the gauges from the database & add to the recent metric history global variables
        if (isDatabaseConnectSuccess) {
            long numGaugesFromDatabase = readGaugesFromDatabaseAndAddToGlobalVariables();
            logger.info("Finished adding gauges from database to recent metric global history. NumGaugesFromDbAddedToGlobal=" + numGaugesFromDatabase);
        }
        
        // load all metric-group & metric-suspension regexes into global variables
        if (isDatabaseConnectSuccess) {
            List<Integer> allMetricGroupIds = MetricGroupsDao.getAllMetricGroupIds(DatabaseConnections.getConnection(), true);
            MetricAssociation.updateMergedRegexesForMetricGroups(allMetricGroupIds);
            
            List<Integer> allMetricSuspensionIds = SuspensionsDao.getSuspensionIds_BySuspendBy(DatabaseConnections.getConnection(), true, Suspension.SUSPEND_BY_METRICS);
            MetricAssociation.updateMergedRegexesForSuspensions(allMetricSuspensionIds);
        }
        
        // create the prefixes (added on by StatsAgg) for the various types of metrics
        createGraphiteAggregatorMetricPrefix();
        createGraphitePassthroughMetricPrefix();
        createOpenTsdbMetricPrefix();
        createInfluxdbMetricPrefix();
                
        // start the thread pool that is responsible for sending alert emails & other notifications
        startSendNotificationThreadPool();
        
        // start the thread pool that is responsible for threads sending metrics to the various output modules 
        startSendToOutputModuleThreadPoolManager();
        
        // set last alert executed routine timestamp to '0', which indicates to the rest of the program that it has never been executed
        GlobalVariables.alertRountineLastExecutedTimestamp.set(0);
        
        // starts the threads that calls the aggregation routines on a timer (specified in the application config file)
        statsdAggregationInvokerThread_ = new StatsdInvokerThread();
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
        
        metricAssociationOutputBlacklistInvokerThread_ = new MetricAssociationOutputBlacklistInvokerThread();
        Thread metricAssociationOutputBlacklistInvokerThread = new Thread(metricAssociationOutputBlacklistInvokerThread_);
        metricAssociationOutputBlacklistInvokerThread.start();
        GlobalVariables.metricAssociationOutputBlacklistInvokerThread = metricAssociationOutputBlacklistInvokerThread_;
        
        AlertThread.reset();
        alertInvokerThread_ = new AlertInvokerThread();
        Thread alertInvokerThread = new Thread(alertInvokerThread_);
        alertInvokerThread.start();
        GlobalVariables.alertInvokerThread = alertInvokerThread_;
        
        templateInvokerThread_ = new TemplateInvokerThread();
        Thread templateInvokerThread = new Thread(templateInvokerThread_);
        templateInvokerThread.start();
        GlobalVariables.templateInvokerThread = templateInvokerThread_;
        
        cleanupInvokerThread_ = new CleanupInvokerThread();
        Thread cleanupInvokerThread = new Thread(cleanupInvokerThread_);
        cleanupInvokerThread.start();
        GlobalVariables.cleanupInvokerThread = cleanupInvokerThread_;
        
        internalStatsInvokerThread_ = new InternalStatsInvokerThread();
        Thread internalStatsInvokerThread = new Thread(internalStatsInvokerThread_);
        internalStatsInvokerThread.start();

        // startup netty & jetty servers (including the ui jetty server)
        boolean isStartupServerListenersSuccess = startServerListeners();
   
        // set the start time for statsagg
        GlobalVariables.statsaggStartTimestamp.set(System.currentTimeMillis());

        logger.info("Finish - Initialize application");
                
        // return true if all startup routines were successful
        return isLogbackSuccess && isApplicationConfigSuccess && isDatabaseConfigSuccess && isDatabaseConnectSuccess && isStartupServerListenersSuccess;
    }

    private static class ShutdownThread extends Thread {
        
        @Override
        public void run() {
            logger.info("Shutting down application...");

            shutdownServerListeners();

            shutdownInvokerThreads();

            shutdownSendToOutputModuleThreadPoolManager();

            shutdownSendNotificationThreadPool();

            DatabaseConnections.disconnectAndShutdown();

            shutdownLogger();

            logger.info("Application successfully shut down");
        }
    }
    
    public static boolean initializeApplication_Logger() {
        boolean isLogbackSuccess;
        
        String customLogbackConfLocation = System.getProperty("saLogbackConfLocation");
        boolean doesDevLogConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "logback-config-dev.xml");
        boolean doesLogConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "logback-config.xml");
        
        if ((customLogbackConfLocation != null) && !customLogbackConfLocation.isBlank()) isLogbackSuccess = readAndSetLogbackConfiguration(customLogbackConfLocation);
        else if (doesDevLogConfExist) isLogbackSuccess = readAndSetLogbackConfiguration(System.getProperty("user.dir") + File.separator + "conf", "logback-config-dev.xml");
        else if (doesLogConfExist) isLogbackSuccess = readAndSetLogbackConfiguration(System.getProperty("user.dir") + File.separator + "conf", "logback-config.xml");
        else {
            System.out.println("Error: Unable to initialize logger!");
            isLogbackSuccess = false;
        }
        
        return isLogbackSuccess;
    }
    
    private static boolean readAndSetLogbackConfiguration(String filePathAndName) {
        boolean doesConfigFileExist = FileIo.doesFileExist(filePathAndName);
        if (doesConfigFileExist) return readAndSetLogbackConfiguration(new File(filePathAndName));
        else return false;
    }
    
    private static boolean readAndSetLogbackConfiguration(String filePath, String fileName) {
        boolean doesConfigFileExist = FileIo.doesFileExist(filePath, fileName);
        if (doesConfigFileExist) return readAndSetLogbackConfiguration(new File(filePath + File.separator + fileName));
        else return false;
    }
    
    private static boolean readAndSetLogbackConfiguration(File logggerConfigFile) {
        
        boolean doesConfigFileExist = FileIo.doesFileExist(logggerConfigFile);
        
        if (doesConfigFileExist) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                context.reset(); 
                configurator.doConfigure(logggerConfigFile);
                StatusPrinter.printInCaseOfErrorsOrWarnings(context);
                return true;
            } 
            catch (Exception e) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(context);
                return false;
            }
        }
        else {
            return false;
        }
    }
    
    private static boolean initializeApplication_ApplicationConfiguration() {
        boolean isApplicationConfigSuccess;
        
        String customAppConfLocation = System.getProperty("saAppConfLocation");
        boolean doesDevAppConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "application-dev.ini");
        boolean doesAppIniConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "application.ini");
        boolean doesAppPropertiesConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "application.properties");
        
        if ((customAppConfLocation != null) && !customAppConfLocation.isBlank()) isApplicationConfigSuccess = ApplicationConfiguration.initialize(customAppConfLocation);
        else if (doesDevAppConfExist) isApplicationConfigSuccess = ApplicationConfiguration.initialize(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "application-dev.ini");
        else if (doesAppIniConfExist) isApplicationConfigSuccess = ApplicationConfiguration.initialize(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "application.ini");
        else if (doesAppPropertiesConfExist) isApplicationConfigSuccess = ApplicationConfiguration.initialize(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "application.properties");
        else {
            logger.error("Fatal error -- application configuration file is missing!");
            isApplicationConfigSuccess = false;
        }
        
        return isApplicationConfigSuccess;
    }
    
    public static boolean initializeApplication_DatabaseConfiguration(boolean useInMemoryDatabase) {
        boolean isDatabaseConfigSuccess;
        
        String customDbConfLocation = System.getProperty("saDbConfLocation");
        boolean doesDevDbIniConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database-dev.ini");
        boolean doesDevDbPropertiesConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database-dev.properties");
        boolean doesDbIniConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database.ini");
        boolean doesDbPropertiesConfExist = FileIo.doesFileExist(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database.properties");
        
        if (useInMemoryDatabase) {
            isDatabaseConfigSuccess = DatabaseConfiguration.initialize(getEphemeralDatabaseConfiguration());
            GlobalVariables.isStatsaggUsingInMemoryDatabase.set(true);
        }
        else if ((customDbConfLocation != null) && !customDbConfLocation.isBlank()) isDatabaseConfigSuccess = DatabaseConfiguration.initialize(customDbConfLocation);
        else if (doesDevDbIniConfExist) isDatabaseConfigSuccess = DatabaseConfiguration.initialize(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database-dev.ini");
        else if (doesDevDbPropertiesConfExist) isDatabaseConfigSuccess = DatabaseConfiguration.initialize(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database-dev.properties");
        else if (doesDbIniConfExist) isDatabaseConfigSuccess = DatabaseConfiguration.initialize(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database.ini");
        else if (doesDbPropertiesConfExist) isDatabaseConfigSuccess = DatabaseConfiguration.initialize(System.getProperty("user.dir") + File.separator + "conf" + File.separator + "database.properties");
        else {
            logger.warn("Database configuration file is missing or malformed!");
            isDatabaseConfigSuccess = false;
        }

        // falling back to using in-memory derby database if unable to connect to specified database
        if (!isDatabaseConfigSuccess) {
            logger.warn("Failed to load database configuration. Using an ephemeral (in-memory) database...");
            isDatabaseConfigSuccess = DatabaseConfiguration.initialize(getEphemeralDatabaseConfiguration());
            GlobalVariables.isStatsaggUsingInMemoryDatabase.set(true);
        }
        else if (!useInMemoryDatabase) {
            GlobalVariables.isStatsaggUsingInMemoryDatabase.set(false);
        }

        return isDatabaseConfigSuccess;
    }
    
    private static InputStream getEphemeralDatabaseConfiguration() {
        StringBuilder defaultDatabase = new StringBuilder();
        defaultDatabase.append("db_type = derby_embedded\n");
        defaultDatabase.append("db_custom_jdbc = jdbc:derby:memory:statsagg_mem_db;create=true\n");
        defaultDatabase.append("derby.storage.pageCacheSize = 15000\n");

        InputStream databaseConfigurationInputStream = new ByteArrayInputStream(defaultDatabase.toString().getBytes());
        return databaseConfigurationInputStream;
    }

    public static boolean connectToDatabase() {
        
        DatabaseConnections.setDriver();
        DatabaseConnections.connectToDatabase(DatabaseConfiguration.getHikariConfig());
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
        
        DatabaseUtils.cleanup(connection);
        
        return isSuccessfulConnection;
    }
    
    public static boolean setupDatabaseSchema() {
        boolean isFlywayRepairSuccess = true, isFlywayMigrateSuccess = true;

        String flywayDevConfFilePath = System.getProperty("user.dir") + File.separator + "conf" + File.separator + "flyway-dev.properties";
        String flywayConfFilePath = System.getProperty("user.dir") + File.separator + "conf" + File.separator + "flyway.properties";
        boolean doesDevFlywayConfExist = FileIo.doesFileExist(flywayDevConfFilePath);
        if (doesDevFlywayConfExist) flywayConfFilePath = flywayDevConfFilePath;
            
        if (DatabaseConfiguration.isFlywayRepairEnabled()) isFlywayRepairSuccess = FlywayOperations.repair(flywayConfFilePath);
        if (DatabaseConfiguration.isFlywayMigrateEnabled()) isFlywayMigrateSuccess = FlywayOperations.migrate(flywayConfFilePath, false);

        boolean isSchemaCreateSuccess = isFlywayRepairSuccess && isFlywayMigrateSuccess;
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
    
    private static void createOpenTsdbMetricPrefix() {
        StringBuilder prefixBuilder = new StringBuilder();

        if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled() && (ApplicationConfiguration.getGlobalMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
        }
        
        if (ApplicationConfiguration.isOpenTsdbMetricNamePrefixEnabled() && (ApplicationConfiguration.getOpenTsdbMetricNamePrefixValue() != null)) {
            prefixBuilder.append(ApplicationConfiguration.getOpenTsdbMetricNamePrefixValue()).append(".");
        }

        GlobalVariables.openTsdbPrefix = prefixBuilder.toString();
    }
    
    private static void createInfluxdbMetricPrefix() {
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
        
        List<MetricLastSeen> metricLastSeens = MetricLastSeenDao.getMetricLastSeens(DatabaseConnections.getConnection(), true);
        
        for (MetricLastSeen metricLastSeen : metricLastSeens) {
            try {
                if ((metricLastSeen.getMetricKey() == null) || (metricLastSeen.getLastModified() == null)) continue;
                
                synchronized (GlobalVariables.recentMetricTimestampsAndValuesByMetricKey) {
                    List<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricLastSeen.getMetricKey());

                    if (metricTimestampsAndValues == null) {
                        metricTimestampsAndValues = Collections.synchronizedList(new ArrayList<>());
                        GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.put(metricLastSeen.getMetricKey(), metricTimestampsAndValues);
                    }
                }
                
                MetricKeyLastSeen metricKeyLastSeen = new MetricKeyLastSeen(metricLastSeen.getLastModified().getTime(), metricLastSeen.getLastModified().getTime());
                GlobalVariables.metricKeysLastSeenTimestamp.putIfAbsent(metricLastSeen.getMetricKey(), metricKeyLastSeen);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return metricLastSeens.size();
    }
    
    private static long readGaugesFromDatabaseAndAddToGlobalVariables() {
        
        if (!ApplicationConfiguration.isStatsdGaugeSendPreviousValue() || !ApplicationConfiguration.isStatsdPersistGauges()) {
            GaugesDao.truncateTable(DatabaseConnections.getConnection(), true);
            return 0;
        }
        
        List<Gauge> gauges = GaugesDao.getGauges(DatabaseConnections.getConnection(), true);
        
        for (Gauge gauge : gauges) {
            try {
                StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated(gauge.getBucket(), gauge.getMetricValue(), 
                        System.currentTimeMillis(), StatsdMetricAggregated.GAUGE_TYPE);
                statsdMetricAggregated.setHashKey(GlobalVariables.metricHashKeyGenerator.incrementAndGet());

                GlobalVariables.statsdMetricsAggregatedMostRecentValue.putIfAbsent(gauge.getBucket(), statsdMetricAggregated);
                MetricKeyLastSeen metricKeyLastSeen = new MetricKeyLastSeen(gauge.getLastModified().getTime(), gauge.getLastModified().getTime());
                GlobalVariables.metricKeysLastSeenTimestamp.putIfAbsent(gauge.getBucket(), metricKeyLastSeen);
                GlobalVariables.statsdGaugeCache.putIfAbsent(gauge.getBucket(), gauge);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return gauges.size();
    }

    private static boolean startServerListeners() {
        
        boolean isStartupSuccess = true;
        
        try {
            // start the ui & api jetty http server
            if (ApplicationConfiguration.isUiAndApiHttpEnabled()) {
                jettyUiAndApi_ = new JettyUiAndApi(ApplicationConfiguration.getUiAndApiHttpPort(), ApplicationConfiguration.getUiAndApiHttpContext(), 30000);
                jettyUiAndApi_.startServer();
                if (!jettyUiAndApi_.isRunning()) isStartupSuccess = false;
            }
            
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
                if (!jettyOpenTsdb_.isRunning()) isStartupSuccess = false;
            }
            
            // start the influxdb jetty http server
            if (ApplicationConfiguration.isInfluxdbHttpListenerEnabled()) {
                jettyInfluxdb_ = new JettyInfluxdb(ApplicationConfiguration.getInfluxdbHttpListenerPort(), 30000);
                jettyInfluxdb_.startServer();
                if (!jettyInfluxdb_.isRunning()) isStartupSuccess = false;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + File.separator + StackTrace.getStringFromStackTrace(e));
            logger.error("Failed to start a netty server. Please view the log files for more details.");
            
            isStartupSuccess = false;
        }

        return isStartupSuccess;
    }
    
    private static void startSendToOutputModuleThreadPoolManager() {
        SendMetricsToOutputModule_ThreadPoolManager.start();
    }
    
    private static void startSendNotificationThreadPool() {
        SendNotification_ThreadPoolManager.start();
    }
    
    private static void shutdownServerListeners() {
        
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

        ShutdownJettyServer shutdownUiAndApiJettyServer = new ShutdownJettyServer(jettyUiAndApi_);
        Thread threadShutdownUiAndApiJettyServer_ = new Thread(shutdownUiAndApiJettyServer);
        shutdownServerThreads.add(threadShutdownUiAndApiJettyServer_);
        
        Threads.threadExecutorCachedPool(shutdownServerThreads, 30, TimeUnit.SECONDS);

        logger.info("Finish - shutting down server listeners");
    }
    
    private static void shutdownLogger() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.stop();
        }
        catch (Exception e) {
            logger.error(e.toString() + File.separator + StackTrace.getStringFromStackTrace(e));
            logger.error("Failed to shutdown logger");
        }
    }
    
    private static void shutdownInvokerThreads() {
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
        
        ShutdownInvokerThread_Thread shutdownMetricAssociationOutputBlacklistInvokerThread = new ShutdownInvokerThread_Thread(metricAssociationOutputBlacklistInvokerThread_);
        Thread shutdownMetricAssociationOutputBlacklistInvokerThread_Thread = new Thread(shutdownMetricAssociationOutputBlacklistInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownMetricAssociationOutputBlacklistInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownAlertInvokerThread = new ShutdownInvokerThread_Thread(alertInvokerThread_);
        Thread shutdownAlertInvokerThread_Thread = new Thread(shutdownAlertInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownAlertInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownTemplateInvokerThread = new ShutdownInvokerThread_Thread(templateInvokerThread_);
        Thread shutdownTemplateInvokerThread_Thread = new Thread(shutdownTemplateInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownTemplateInvokerThread_Thread);
        
        ShutdownInvokerThread_Thread shutdownCleanupInvokerThread = new ShutdownInvokerThread_Thread(cleanupInvokerThread_);
        Thread shutdownCleanupInvokerThread_Thread = new Thread(shutdownCleanupInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownCleanupInvokerThread_Thread);

        ShutdownInvokerThread_Thread shutdownInternalStatsInvokerThread = new ShutdownInvokerThread_Thread(internalStatsInvokerThread_);
        Thread shutdownInternalStatsInvokerThread_Thread = new Thread(shutdownInternalStatsInvokerThread);
        shutdownThreadInvokerThreads.add(shutdownInternalStatsInvokerThread_Thread);
        
        Threads.threadExecutorCachedPool(shutdownThreadInvokerThreads, 2, TimeUnit.MINUTES);

        logger.info("Finish - shutting down invoker threads");
    }
    
    private static void shutdownSendToOutputModuleThreadPoolManager() {
        logger.info("Start - shutting down 'send to output modules' thread pool");
        SendMetricsToOutputModule_ThreadPoolManager.shutdown();
        logger.info("Finish - shutting down 'send to output modules' thread pool");
    }
    
    private static void shutdownSendNotificationThreadPool() {
        logger.info("Start - shutting down 'send notification' thread pool");
        SendNotification_ThreadPoolManager.shutdown();
        logger.info("Finish - shutting down 'send notification' thread pool");
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
