package com.pearson.statsagg.alerts;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import com.pearson.statsagg.controller.threads.SendEmailThreadPoolManager;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetricAggregated;
import com.pearson.statsagg.modules.GraphiteOutputModule;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.Threads;
import com.pearson.statsagg.webui.StatsAggHtmlFramework;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AlertThread.class.getName());
        
    public static final int ALERT_SCALE = 7;
    public static final int ALERT_PRECISION = 31;
    public static final RoundingMode ALERT_ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final MathContext ALERT_MATH_CONTEXT = new MathContext(ALERT_PRECISION, ALERT_ROUNDING_MODE);
    
    private static final AtomicBoolean isThreadCurrentlyRunning_ = new AtomicBoolean(false);
    private static final AtomicLong alertRoutineExecutionCounter_ = new AtomicLong(0);
    private static final Map<Integer, Alert> pendingCautionAlertsByAlertId_ = new HashMap<>();
    private static final Map<Integer, Alert> pendingDangerAlertsByAlertId_ = new HashMap<>();
    
    protected final Long threadStartTimestampInMilliseconds_;
    protected final String threadId_;
    protected final String statsAggLocation_;
    
    private final List<Alert> enabledAlerts_ = new ArrayList<>();
    private Map<Integer,Alert> alertsByAlertId_ = null;    
    
    private final Map<Integer, List<String>> activeCautionAlertMetricKeysByAlertId_ = new ConcurrentHashMap<>();
    private final Map<Integer, List<String>> activeDangerAlertMetricKeysByAlertId_ = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> activeCautionAlertMetricValues_ = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> activeDangerAlertMetricValues_ = new ConcurrentHashMap<>();
    
    private final AtomicLong activeCautionAlertMetricKeysByAlertId_Counter_ = new AtomicLong(0);
    private final AtomicLong activeDangerAlertMetricKeysByAlertId_Counter_ = new AtomicLong(0);
    private final AtomicLong activeCautionAlertMetricValues_Counter_ = new AtomicLong(0);
    private final AtomicLong activeDangerAlertMetricValues_Counter_ = new AtomicLong(0);
    
    private AlertSuspensions alertSuspensions_ = null;
    
    public AlertThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "A-" + threadStartTimestampInMilliseconds_.toString();
        this.statsAggLocation_ = ApplicationConfiguration.getAlertStatsAggLocation();
    }
    
    @Override
    public void run() {
  
        // stops multiple alert threads from running simultaneously 
        if (!isThreadCurrentlyRunning_.compareAndSet(false, true)) {
            logger.warn("ThreadId=" + threadId_ + ", Routine=AlertAndCleanup, Message=\"Only 1 alert thread can run at a time\"");
            return;
        }
        
        // if the alert routine is enabled, run the metric association routine
        long metricAssociationTimeElasped = 0;
        if (ApplicationConfiguration.isAlertRoutineEnabled()) {
            long metricAssociationStartTime = System.currentTimeMillis();
            MetricAssociation.associateMetricKeysWithMetricGroups();
            metricAssociationTimeElasped = System.currentTimeMillis() - metricAssociationStartTime; 
        }
        
        long alertRoutineTimeElasped = 0, alertSuspensionRoutineTimeElapsed = 0;
        synchronized (GlobalVariables.alertRoutineLock) {
            // gets all alerts from the database.
            AlertsDao alertsDao = new AlertsDao();
            List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
            alertsByAlertId_ = getAlertsByAlertId(alerts);
            
            // run the alert suspension routine
            long alertSuspensionRoutineStartTime = System.currentTimeMillis();
            alertSuspensions_ = new AlertSuspensions(alertsByAlertId_);
            alertSuspensions_.runAlertSuspensionRoutine();
            alertSuspensionRoutineTimeElapsed = System.currentTimeMillis() - alertSuspensionRoutineStartTime; 
            
            // if the alert routine is enabled, run the alerting routine
            if (ApplicationConfiguration.isAlertRoutineEnabled()) {
                long alertRoutineStartTime = System.currentTimeMillis();
                runAlertRoutine(alerts);
                alertRoutineTimeElasped = System.currentTimeMillis() - alertRoutineStartTime; 
                
                if (GraphiteOutputModule.isAnyGraphiteOutputModuleEnabled()) {
                    // generate messages for graphite
                    List<GraphiteMetricAggregated> alertStatusMetricsForGraphite = generateAlertStatusMetricsForGraphite(alerts);
                    List<String> outputMessagesForGraphite = GraphiteOutputModule.buildMultiMetricGraphiteMessages(alertStatusMetricsForGraphite,
                            ApplicationConfiguration.getGraphiteMaxBatchSize());

                    // send to graphite
                    GraphiteOutputModule.sendMetricsToGraphiteEndpoints(outputMessagesForGraphite, threadId_);
                }
            }
        }

        // cleans up recent metric data that isn't being specifically tracked by an alert
        long cleanupStartTime = System.currentTimeMillis();
        Cleanup cleanup = new Cleanup(threadId_);
        cleanup.runCleanupRoutine(enabledAlerts_);
        long cleanupTimeElasped = System.currentTimeMillis() - cleanupStartTime; 

        String outputMessage = "ThreadId=" + threadId_
                + ", Routine=AlertAndCleanup"
                + ", MetricAssociationTime=" + metricAssociationTimeElasped
                + ", AlertRoutineTime=" + alertRoutineTimeElasped
                + ", AlertSuspensionRoutineTime=" + alertSuspensionRoutineTimeElapsed
                + ", TotalCleanupTime=" + cleanupTimeElasped
                ;

        logger.info(outputMessage);

        GlobalVariables.associatedMetricsWithValuesCount.set(GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.size());

        isThreadCurrentlyRunning_.set(false);
        
    }

    private void runAlertRoutine(List<Alert> alerts) {
        
        if (alerts == null) {
            return;
        }
        
        // gets a list of enabled alerts. 
        enabledAlerts_.addAll(getEnabledAlerts(alerts));
        
        // if this is the first time running the alert routine, get enabled alerts that think they're already 'active' & put them in the 'pending' Sets
        if (alertRoutineExecutionCounter_.get() == 0) {
            alertRecoveryRoutine_DeterminePendingAlerts(enabledAlerts_);
        }
        
        // gets a list of alerts that are both enabled & in a 'caution-active' state
        List<Alert> enabledAndActiveCautionAlerts = getActiveCautionAlerts(enabledAlerts_);
        for (Alert alert : enabledAndActiveCautionAlerts) {
            activeCautionAlertMetricKeysByAlertId_.put(alert.getId(), new ArrayList<String>());
            activeCautionAlertMetricKeysByAlertId_Counter_.incrementAndGet();
        }
        
        // gets a list of alerts that are both enabled & in a 'danger-active' state
        List<Alert> enabledAndActiveDangerAlerts = getActiveDangerAlerts(enabledAlerts_);
        for (Alert alert : enabledAndActiveDangerAlerts) {
            activeDangerAlertMetricKeysByAlertId_.put(alert.getId(), new ArrayList<String>());
            activeDangerAlertMetricKeysByAlertId_Counter_.incrementAndGet();
        }
        
        // for each enabled alert, run the alert routine (check alert criteria, send email, persist alert status).
        // if an alert routine thread takes longer than the alert routine internal * 3, it will be terminated
        determineAlertStatus(enabledAlerts_, ApplicationConfiguration.getAlertRoutineInterval() * 3);
        
        // alert recovery routine -- only applies to alerts that were in an 'active' state when the application was started
        alertRecoveryRoutine_HasReachedRecoveryTimeout(GlobalVariables.statsaggStartTimestamp.longValue(), ApplicationConfiguration.getAlertWaitTimeAfterRestart());
        alertRecoveryRoutine_HasReachedPreviousState();
        alertRecoveryRoutine_HasReachedWindowDuration(GlobalVariables.statsaggStartTimestamp.longValue());
        
        // save the alert status into the db & email the notification groups (if any) 
        for (Integer alertId : activeCautionAlertMetricKeysByAlertId_.keySet()) {
            takeActionOnCautionAlert(alertsByAlertId_.get(alertId), ApplicationConfiguration.getAlertMaxMetricsInEmail());
        }
        
        // save the alert status into the db & email the notification groups (if any) 
        for (Integer alertId : activeDangerAlertMetricKeysByAlertId_.keySet()) {
            takeActionOnDangerAlert(alertsByAlertId_.get(alertId), ApplicationConfiguration.getAlertMaxMetricsInEmail());
        }
        
        // increment the alert routine execution counter
        alertRoutineExecutionCounter_.incrementAndGet();
        
        // updates all global variables related to the alert-routine
        updateAlertGlobalVariables();
    }
    
    /*
    Reset all static variables associated with the alert routine
    */
    public static void reset() {
        isThreadCurrentlyRunning_.set(false);
        alertRoutineExecutionCounter_.set(0);
        pendingCautionAlertsByAlertId_.clear();
        pendingDangerAlertsByAlertId_.clear();
    }
    
    /*
    Alert recovery routine - stage 1
    If alerts were in a triggered state before the applicaiton was started (due to application restart), then you don't want alerts to instantly send 'positive' notifications
    when the application comes back online. The 'alert recovery routine' mitigates this behavior by only allowing alerts that were triggered before the application was started
    to update their status after certain criteria has been met.
    
    This stage of the 'Alert recovery routine' is only executed once per application launch, and its purpose is to identify alerts that were triggered before the application 
    was (re)started.
    */
    private void alertRecoveryRoutine_DeterminePendingAlerts(List<Alert> alerts) {
        
        if (alerts == null) {
            return;
        }
        
        for (Alert alert : alerts) {
        
            if (alert.isCautionAlertActive()) {
                pendingCautionAlertsByAlertId_.put(alert.getId(), alert);
            }
            
            if (alert.isDangerAlertActive()) {
                pendingDangerAlertsByAlertId_.put(alert.getId(), alert);
            }
            
        }
        
    }
    
    /*
    Alert recovery routine -- stage 2a
    Alerts are allowed to be sent if a user-specified time has been reached
    */
    private void alertRecoveryRoutine_HasReachedRecoveryTimeout(long applicationStartupTimeInMs, long alertRecoveryTimeoutTimeInMs) {
        
        long currentTimeInMs = System.currentTimeMillis();
        long timeSinceStartup =  currentTimeInMs - applicationStartupTimeInMs;
        
        if (timeSinceStartup >= alertRecoveryTimeoutTimeInMs) {
            if ((pendingCautionAlertsByAlertId_ != null) && !pendingCautionAlertsByAlertId_.isEmpty()) {
                pendingCautionAlertsByAlertId_.clear();
                logger.info("Routine=AlertRecovery, Message=\"Timeout reached after " + timeSinceStartup + "ms. All caution alerts are allowed to alert.\"");
            }
            
            if ((pendingDangerAlertsByAlertId_ != null) && !pendingDangerAlertsByAlertId_.isEmpty()) {
                pendingDangerAlertsByAlertId_.clear();
                logger.info("Routine=AlertRecovery, Message=\"Timeout reached after " + timeSinceStartup + "ms. All danger alerts are allowed to alert.\"");
            }
        }
        
    }
    
    /*
    Alert recovery routine -- stage 2b
    Alerts are allowed to be sent if the alert has reached its previous alert-triggered state
    */
    private void alertRecoveryRoutine_HasReachedPreviousState() {
        
        if ((pendingCautionAlertsByAlertId_ != null) && !pendingCautionAlertsByAlertId_.isEmpty()) {
            List<Integer> pendingCautionAlertIdsLocal = new ArrayList<>(pendingCautionAlertsByAlertId_.keySet());
            
            for (Integer alertId : pendingCautionAlertIdsLocal) {
                boolean hasAlertReachedPreviousState = false;
                
                if ((activeCautionAlertMetricKeysByAlertId_.containsKey(alertId)) && 
                        (activeCautionAlertMetricKeysByAlertId_.get(alertId) != null) && 
                        (!activeCautionAlertMetricKeysByAlertId_.get(alertId).isEmpty())) {
                    hasAlertReachedPreviousState = true;
                }

                if (hasAlertReachedPreviousState) {
                    Alert alert = pendingCautionAlertsByAlertId_.get(alertId);
                    pendingCautionAlertsByAlertId_.remove(alertId);
                    String cleanAlertName = StatsAggHtmlFramework.removeNewlinesFromString(alert.getName(), ' ');
                    logger.info("Routine=AlertRecovery, AlertName=\"" + cleanAlertName + "\", Message=\"Caution alerting enabled after reaching previous window state\"");
                }
            }
        }

        if ((pendingDangerAlertsByAlertId_ != null) && !pendingDangerAlertsByAlertId_.isEmpty()) {
            List<Integer> pendingDangerAlertIdsLocal = new ArrayList<>(pendingDangerAlertsByAlertId_.keySet());
            
            for (Integer alertId : pendingDangerAlertIdsLocal) {
                boolean hasAlertReachedPreviousState = false;
                
                if ((activeDangerAlertMetricKeysByAlertId_.containsKey(alertId)) && 
                        (activeDangerAlertMetricKeysByAlertId_.get(alertId) != null) && 
                        (!activeDangerAlertMetricKeysByAlertId_.get(alertId).isEmpty())) {
                    hasAlertReachedPreviousState = true;
                }
                
                if (hasAlertReachedPreviousState) {
                    Alert alert = pendingDangerAlertsByAlertId_.get(alertId);
                    pendingDangerAlertsByAlertId_.remove(alertId);
                    String cleanAlertName = StatsAggHtmlFramework.removeNewlinesFromString(alert.getName(), ' ');
                    logger.info("Routine=AlertRecovery, AlertName=\"" + cleanAlertName + "\", Message=\"Danger alerting enabled after reaching previous window state\"");
                }
            }
        }
    }
    
    /*
    Alert recovery routine -- stage 2c
    Alerts are allowed to be sent if the application has been running for longer than the alert criteria's window duration
    */
    private void alertRecoveryRoutine_HasReachedWindowDuration(long applicationStartupTimeInMs) {
        
        long currentTimeInMs = System.currentTimeMillis();
        long timeSinceStartup =  currentTimeInMs - applicationStartupTimeInMs;
            
        if ((pendingCautionAlertsByAlertId_ != null) && !pendingCautionAlertsByAlertId_.isEmpty()) {
            List<Integer> pendingCautionAlertIdsLocal = new ArrayList<>(pendingCautionAlertsByAlertId_.keySet());

            for (Integer alertId : pendingCautionAlertIdsLocal) {
                Alert alert = pendingCautionAlertsByAlertId_.get(alertId);
                
                if ((alert.getCautionWindowDuration() != null) && (timeSinceStartup >= alert.getCautionWindowDuration())) {
                    pendingCautionAlertsByAlertId_.remove(alertId);
                    String cleanAlertName = StatsAggHtmlFramework.removeNewlinesFromString(alert.getName(), ' ');
                    logger.info("Routine=AlertRecovery, AlertName=\"" + cleanAlertName + "\", Message=\"Caution alerting enabled after reaching window duration (" + timeSinceStartup + "ms)\"");
                }
            }
        }

        if ((pendingDangerAlertsByAlertId_ != null) && !pendingDangerAlertsByAlertId_.isEmpty()) {
            List<Integer> pendingDangerAlertIdsLocal = new ArrayList<>(pendingDangerAlertsByAlertId_.keySet());
            
            for (Integer alertId : pendingDangerAlertIdsLocal) {
                Alert alert = pendingDangerAlertsByAlertId_.get(alertId);
                
                if ((alert.getDangerWindowDuration() != null) && (timeSinceStartup >= alert.getDangerWindowDuration())) {
                    pendingDangerAlertsByAlertId_.remove(alertId);
                    String cleanAlertName = StatsAggHtmlFramework.removeNewlinesFromString(alert.getName(), ' ');
                    logger.info("Routine=AlertRecovery, AlertName=\"" + cleanAlertName + "\", Message=\"Danger alerting enabled after reaching window duration (" + timeSinceStartup + "ms)\"");
                }
            }
        }
    }
    
    private void updateAlertGlobalVariables() {
        
        synchronized(GlobalVariables.activeCautionAlertMetricKeysByAlertId) {
            GlobalVariables.activeCautionAlertMetricKeysByAlertId.clear();
            while (GlobalVariables.activeCautionAlertMetricKeysByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.activeCautionAlertMetricKeysByAlertId.putAll(activeCautionAlertMetricKeysByAlertId_);
            while (GlobalVariables.activeCautionAlertMetricKeysByAlertId.size() != activeCautionAlertMetricKeysByAlertId_.size()) {
                logger.warn("Message=\"activeCautionAlertMetricKeysByAlertId size mismatch\", Size=" + GlobalVariables.activeCautionAlertMetricKeysByAlertId.size() +
                    ", Expected=" + activeCautionAlertMetricKeysByAlertId_.size());
                Threads.sleepMilliseconds(10);
            }
        }
        
        synchronized(GlobalVariables.activeDangerAlertMetricKeysByAlertId) {
            GlobalVariables.activeDangerAlertMetricKeysByAlertId.clear();
            while (GlobalVariables.activeDangerAlertMetricKeysByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.activeDangerAlertMetricKeysByAlertId.putAll(activeDangerAlertMetricKeysByAlertId_);
            while (GlobalVariables.activeDangerAlertMetricKeysByAlertId.size() != activeDangerAlertMetricKeysByAlertId_.size()) {
                logger.warn("Message=\"activeDangerAlertMetricKeysByAlertId size mismatch\", Size=" + GlobalVariables.activeDangerAlertMetricKeysByAlertId.size() +
                    ", Expected=" + activeDangerAlertMetricKeysByAlertId_.size());
                Threads.sleepMilliseconds(10);
            }
        }
        
        synchronized(GlobalVariables.activeCautionAlertMetricValues) {
            GlobalVariables.activeCautionAlertMetricValues.clear();
            while (GlobalVariables.activeCautionAlertMetricValues.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.activeCautionAlertMetricValues.putAll(activeCautionAlertMetricValues_);
            while (GlobalVariables.activeCautionAlertMetricValues.size() != activeCautionAlertMetricValues_.size()) {
                logger.warn("Message=\"activeCautionAlertMetricValues size mismatch\", Size=" + GlobalVariables.activeCautionAlertMetricValues.size() + 
                    ", Expected=" + activeCautionAlertMetricValues_.size());
                Threads.sleepMilliseconds(10);
            }
        }
        
        synchronized(GlobalVariables.activeDangerAlertMetricValues) {
            GlobalVariables.activeDangerAlertMetricValues.clear();
            while (GlobalVariables.activeDangerAlertMetricValues.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.activeDangerAlertMetricValues.putAll(activeDangerAlertMetricValues_);
            while (GlobalVariables.activeDangerAlertMetricValues.size() != activeDangerAlertMetricValues_.size()) {
                logger.warn("Message=\"activeDangerAlertMetricValues size mismatch\", Size=" + GlobalVariables.activeDangerAlertMetricValues.size() +
                    ", Expected=" + activeDangerAlertMetricValues_.size());
                Threads.sleepMilliseconds(10);
            }
        }
        
        synchronized(GlobalVariables.pendingCautionAlertsByAlertId) {
            GlobalVariables.pendingCautionAlertsByAlertId.clear();
            while (GlobalVariables.pendingCautionAlertsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.pendingCautionAlertsByAlertId.putAll(pendingCautionAlertsByAlertId_);
            while (GlobalVariables.pendingCautionAlertsByAlertId.size() != pendingCautionAlertsByAlertId_.size()) {
                logger.warn("Message=\"pendingCautionAlertsByAlertId size mismatch\", Size=" + GlobalVariables.pendingCautionAlertsByAlertId.size() +
                    ", Expected=" + pendingCautionAlertsByAlertId_.size());
                Threads.sleepMilliseconds(10);
            }
        }
        
        synchronized(GlobalVariables.pendingDangerAlertsByAlertId) {
            GlobalVariables.pendingDangerAlertsByAlertId.clear();
            while (GlobalVariables.pendingDangerAlertsByAlertId.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.pendingDangerAlertsByAlertId.putAll(pendingDangerAlertsByAlertId_);
            while (GlobalVariables.pendingDangerAlertsByAlertId.size() != pendingDangerAlertsByAlertId_.size()) {
                logger.warn("Message=\"pendingDangerAlertsByAlertId size mismatch\", Size=" + GlobalVariables.pendingDangerAlertsByAlertId.size() +
                    ", Expected=" + pendingDangerAlertsByAlertId_.size());
                Threads.sleepMilliseconds(10);
            }
        }

        GlobalVariables.alertRountineLastExecutedTimestamp.set(System.currentTimeMillis());
    }

    private void determineAlertStatus(List<Alert> alerts, int threadTimeoutInMilliseconds) {
        
        if (alerts == null) {
            return;
        }

        Map<Integer,List<Alert>> alertsByCpuCore = AlertThread.separateAlertsByCpuCore(alerts);

        List<Thread> determineAlertStatusThreads = new ArrayList<>();
        for (List<Alert> alertsSingleCore : alertsByCpuCore.values()) {
            Thread determineAlertStatus_Thread = new Thread(new determineAlertStatus_Thread(alertsSingleCore, this));
            determineAlertStatusThreads.add(determineAlertStatus_Thread);
        }

        Threads.threadExecutorFixedPool(determineAlertStatusThreads, alertsByCpuCore.size(), threadTimeoutInMilliseconds, TimeUnit.MILLISECONDS);

        waitForConcurrentHashMapsToSettle();
    }
    
    private void waitForConcurrentHashMapsToSettle() {
        while (activeCautionAlertMetricKeysByAlertId_.size() != activeCautionAlertMetricKeysByAlertId_Counter_.intValue()) {
            logger.warn("Message=\"activeCautionAlertMetricKeysByAlertId size mismatch\", Size=" + activeCautionAlertMetricKeysByAlertId_.size() +
                    ", Expected=" + activeCautionAlertMetricKeysByAlertId_Counter_.intValue());
            Threads.sleepMilliseconds(10);
        }
        
        while (activeDangerAlertMetricKeysByAlertId_.size() != activeDangerAlertMetricKeysByAlertId_Counter_.intValue()) {
            logger.warn("Message=\"activeDangerAlertMetricKeysByAlertId size mismatch\", Size=" + activeDangerAlertMetricKeysByAlertId_.size() + 
                    ", Expected=" + activeDangerAlertMetricKeysByAlertId_Counter_.intValue());
            Threads.sleepMilliseconds(10);
        }
        
        while (activeCautionAlertMetricValues_.size() != activeCautionAlertMetricValues_Counter_.intValue()) {
            logger.warn("Message=\"activeCautionAlertMetricValues size mismatch\", Size=" + activeCautionAlertMetricValues_.size() +
                    ", Expected=" + activeCautionAlertMetricValues_Counter_.intValue());
            Threads.sleepMilliseconds(10);
        }
        
        while (activeDangerAlertMetricValues_.size() != activeDangerAlertMetricValues_Counter_.intValue()) {
            logger.warn("Message=\"activeDangerAlertMetricValues size mismatch\", Size=" + activeDangerAlertMetricValues_.size() +
                    ", Expected=" + activeDangerAlertMetricValues_Counter_.intValue());
            Threads.sleepMilliseconds(10);
        }
    }
    
    private static class determineAlertStatus_Thread implements Runnable {
  
        private final List<Alert> alerts__;
        private final AlertThread alertThread__;
        
        public determineAlertStatus_Thread(List<Alert> alerts, AlertThread alertThread) {
            this.alerts__ = alerts;
            this.alertThread__ = alertThread;
        }
        
        @Override
        public void run() {
  
            if ((alertThread__ == null) || (alerts__ == null) || alerts__.isEmpty()) {
                return;
            }
            
            for (Alert alert : alerts__) {
                boolean isCautionAlertCriteriaValid = alert.isCautionAlertCriteriaValid();
                boolean isDangerAlertCriteriaValid = alert.isDangerAlertCriteriaValid();
                
                List<String> metricKeysAssociatedWithAlert = MetricAssociation.getMetricKeysAssociatedWithAlert(alert);
                
                for (String metricKey : metricKeysAssociatedWithAlert) {
                    if (GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.containsKey(metricKey)) {
                        Set<MetricTimestampAndValue> recentMetricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);
                        List<MetricTimestampAndValue> recentMetricTimestampsAndValuesLocal = null;

                        if (recentMetricTimestampsAndValues != null) {
                            synchronized(recentMetricTimestampsAndValues) {
                                recentMetricTimestampsAndValuesLocal = new ArrayList<>(recentMetricTimestampsAndValues);
                            }

                            if (!recentMetricTimestampsAndValuesLocal.isEmpty()) {
                                if (isCautionAlertCriteriaValid) {
                                    determineAlertStatus_Caution(alert, alertThread__, recentMetricTimestampsAndValuesLocal, metricKey);
                                }

                                if (isDangerAlertCriteriaValid) {
                                    determineAlertStatus_Danger(alert, alertThread__, recentMetricTimestampsAndValuesLocal, metricKey);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void determineAlertStatus_Caution(Alert alert, AlertThread alertThread, List<MetricTimestampAndValue> recentMetricTimestampsAndValues, String metricKey) {
        
        if ((alert == null) || (recentMetricTimestampsAndValues == null) || (metricKey == null)) {
            return;
        }
        
        BigDecimal activeAlertValue = isAlertActive(alertThread.threadStartTimestampInMilliseconds_, recentMetricTimestampsAndValues, alert.getCautionAlertType(),
                alert.getCautionWindowDuration(), alert.getCautionOperator(), alert.getCautionCombination(), alert.getCautionCombinationCount(), 
                alert.getCautionThreshold(), alert.getCautionMinimumSampleCount());
        
        if (activeAlertValue != null) {
            List<String> activeCautionAlertMetricKeys = alertThread.activeCautionAlertMetricKeysByAlertId_.get(alert.getId());

            if (activeCautionAlertMetricKeys == null) {
                alertThread.activeCautionAlertMetricKeysByAlertId_.put(alert.getId(), new ArrayList<String>());
                alertThread.activeCautionAlertMetricKeysByAlertId_Counter_.incrementAndGet();
                activeCautionAlertMetricKeys = alertThread.activeCautionAlertMetricKeysByAlertId_.get(alert.getId());
            }

            if (activeCautionAlertMetricKeys != null) {
                activeCautionAlertMetricKeys.add(metricKey);
            }
            
            alertThread.activeCautionAlertMetricValues_.put(metricKey + "-" + alert.getId(), activeAlertValue);
            alertThread.activeCautionAlertMetricValues_Counter_.incrementAndGet();
        }
        
    }
    
    private static void determineAlertStatus_Danger(Alert alert, AlertThread alertThread, List<MetricTimestampAndValue> recentMetricTimestampsAndValues, String metricKey) {
        
        if ((alert == null) || (recentMetricTimestampsAndValues == null) || (metricKey == null)) {
            return;
        }
        
        BigDecimal activeAlertValue = isAlertActive(alertThread.threadStartTimestampInMilliseconds_, recentMetricTimestampsAndValues, alert.getDangerAlertType(),
                alert.getDangerWindowDuration(), alert.getDangerOperator(), alert.getDangerCombination(), alert.getDangerCombinationCount(), 
                alert.getDangerThreshold(), alert.getDangerMinimumSampleCount());

        if (activeAlertValue != null) {
            List<String> activeDangerAlertMetricKeys = alertThread.activeDangerAlertMetricKeysByAlertId_.get(alert.getId());

            if (activeDangerAlertMetricKeys == null) {
                alertThread.activeDangerAlertMetricKeysByAlertId_.put(alert.getId(), new ArrayList<String>());
                alertThread.activeDangerAlertMetricKeysByAlertId_Counter_.incrementAndGet();
                activeDangerAlertMetricKeys = alertThread.activeDangerAlertMetricKeysByAlertId_.get(alert.getId());
            }

            if (activeDangerAlertMetricKeys != null) {
                activeDangerAlertMetricKeys.add(metricKey);
            }
            
            alertThread.activeDangerAlertMetricValues_.put(metricKey + "-" + alert.getId(), activeAlertValue);
            alertThread.activeDangerAlertMetricValues_Counter_.incrementAndGet();
        }
        
    }
    
    private void takeActionOnCautionAlert(Alert alert, int maxMetricsInEmail) {
        
        if ((alert == null) || (alertSuspensions_ == null)) {
            return;
        }
        
        // if the 'alert recovery routine' has not cleared the caution alert to allow, then do not allow it to take any action
        if ((pendingCautionAlertsByAlertId_ != null) && pendingCautionAlertsByAlertId_.containsKey(alert.getId())) {
            return;
        }
        
        boolean doUpdateAlertInDb = false;
        long currentTimeInMs = System.currentTimeMillis();
        
        List<String> metricKeys = new ArrayList<>();
        if (activeCautionAlertMetricKeysByAlertId_ != null) {
            metricKeys = activeCautionAlertMetricKeysByAlertId_.get(alert.getId());
        }
 
        boolean isCautionAlertActive = true;
        if (metricKeys.isEmpty()) {
            isCautionAlertActive = false;
        }
        
        String newCautionActiveAlertsSet = appendActiveAlertsToSet(metricKeys, alert.getCautionActiveAlertsSet(), maxMetricsInEmail + 1);
        alert.setCautionActiveAlertsSet(newCautionActiveAlertsSet);
        
        // the alert is suspended (the entire alert, not just notifications)
        if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) != null) && alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) &&
                (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) != null) && (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) == AlertSuspensions.SUSPEND_ENTIRE_ALERT)) {
            alert.setIsCautionAlertActive(false);
            alert.setCautionAlertLastSentTimestamp(null);
            alert.setCautionActiveAlertsSet(null);
            doUpdateAlertInDb = true;
        }
        // not active -> active
        else if ((((alert.isCautionAlertActive() != null) && !alert.isCautionAlertActive()) || (alert.isCautionAlertActive() == null)) && isCautionAlertActive) {
            alert.setIsCautionAlertActive(true);
            alert.setCautionAlertLastSentTimestamp(new Timestamp(currentTimeInMs));
            
            if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                Alert alertCopy = Alert.copy(alert);
                EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, activeCautionAlertMetricValues_, false, statsAggLocation_);
                SendEmailThreadPoolManager.executeThread(emailThread);
            }

            doUpdateAlertInDb = true;
        } 
        // active -> not active
        else if (alert.isCautionAlertActive() && !isCautionAlertActive) {
            alert.setIsCautionAlertActive(false);
            
            if ((alert.isAlertOnPositive() != null) && alert.isAlertOnPositive() && (alert.getCautionAlertType() != null) && (alert.getCautionAlertType() == Alert.TYPE_THRESHOLD)) {
                if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                    Alert alertCopy = Alert.copy(alert);
                    EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, activeCautionAlertMetricValues_, true, statsAggLocation_);
                    SendEmailThreadPoolManager.executeThread(emailThread);
                }
            }
            
            alert.setCautionActiveAlertsSet(null);
            alert.setCautionAlertLastSentTimestamp(null);
            doUpdateAlertInDb = true;
        } 
        // active -> active
        else if (alert.isCautionAlertActive() && isCautionAlertActive) {
            if (alert.isAllowResendAlert() && (alert.getCautionAlertLastSentTimestamp() != null)) {
                long timeSinceLastNotificationInMs = currentTimeInMs - alert.getCautionAlertLastSentTimestamp().getTime();
                
                if (timeSinceLastNotificationInMs >= alert.getSendAlertEveryNumMilliseconds()) {
                    if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                        Alert alertCopy = Alert.copy(alert);
                        EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, activeCautionAlertMetricValues_, false, statsAggLocation_);
                        SendEmailThreadPoolManager.executeThread(emailThread);
                    }
                    
                    alert.setCautionAlertLastSentTimestamp(new Timestamp(currentTimeInMs));
                    doUpdateAlertInDb = true;
                }
            }
        }
        
        if (doUpdateAlertInDb) {
            AlertsDao alertDao = new AlertsDao(false);
            Alert alertFromDb = alertDao.getAlert(alert.getId());

            if (alertFromDb != null) {
                alertDao.upsert(alert);
            }

            alertDao.close();
        }

    }
    
    private void takeActionOnDangerAlert(Alert alert, int maxMetricsInEmail) {
        
        if ((alert == null) || (alertSuspensions_ == null)) {
            return;
        }
        
        // if the 'alert recovery routine' has not cleared the danger alert to allow, then do not allow it to take any action
        if ((pendingDangerAlertsByAlertId_ != null) && pendingDangerAlertsByAlertId_.containsKey(alert.getId())) {
            return;
        }
        
        boolean doUpdateAlertInDb = false;
        long currentTimeInMs = System.currentTimeMillis();

        List<String> metricKeys = new ArrayList<>();
        if (activeDangerAlertMetricKeysByAlertId_ != null) {
            metricKeys = activeDangerAlertMetricKeysByAlertId_.get(alert.getId());
        }

        boolean isDangerAlertActive = true;
        if (metricKeys.isEmpty()) {
            isDangerAlertActive = false;
        }
        
        String newDangerActiveAlertsSet = appendActiveAlertsToSet(metricKeys, alert.getDangerActiveAlertsSet(), maxMetricsInEmail + 1);
        alert.setDangerActiveAlertsSet(newDangerActiveAlertsSet);
        
        // the alert is suspended (the entire alert, not just notifications)
        if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) != null) && alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) &&
                (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) != null) && (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) == AlertSuspensions.SUSPEND_ENTIRE_ALERT)) {
            alert.setIsDangerAlertActive(false);
            alert.setDangerAlertLastSentTimestamp(null);
            alert.setDangerActiveAlertsSet(null);
            doUpdateAlertInDb = true;
        }
        // not active -> active
        else if ((((alert.isDangerAlertActive() != null) && !alert.isDangerAlertActive()) || (alert.isDangerAlertActive() == null)) && isDangerAlertActive) {
            alert.setIsDangerAlertActive(true);
            alert.setDangerAlertLastSentTimestamp(new Timestamp(currentTimeInMs));
            
            if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                Alert alertCopy = Alert.copy(alert);
                EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_DANGER, metricKeys, activeDangerAlertMetricValues_, false, statsAggLocation_);
                SendEmailThreadPoolManager.executeThread(emailThread);
            }
            
            doUpdateAlertInDb = true;
        } 
        // active -> not active
        else if (alert.isDangerAlertActive() && !isDangerAlertActive) {
            alert.setIsDangerAlertActive(false);
            
            if ((alert.isAlertOnPositive() != null) && alert.isAlertOnPositive() && (alert.getDangerAlertType() != null) && (alert.getDangerAlertType() == Alert.TYPE_THRESHOLD)) {
                if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                    Alert alertCopy = Alert.copy(alert);
                    EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_DANGER, metricKeys, activeDangerAlertMetricValues_, true, statsAggLocation_);
                    SendEmailThreadPoolManager.executeThread(emailThread);
                }
            }
            
            alert.setDangerActiveAlertsSet(null);
            alert.setDangerAlertLastSentTimestamp(null);
            doUpdateAlertInDb = true;
        } 
        // active -> active
        else if (alert.isDangerAlertActive() && isDangerAlertActive) {
            if (alert.isAllowResendAlert() && (alert.getDangerAlertLastSentTimestamp() != null)) {
                long timeSinceLastNotificationInMs = currentTimeInMs - alert.getDangerAlertLastSentTimestamp().getTime();
                
                if (timeSinceLastNotificationInMs >= alert.getSendAlertEveryNumMilliseconds()) {
                    if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                        Alert alertCopy = Alert.copy(alert);
                        EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_DANGER, metricKeys, activeDangerAlertMetricValues_, false, statsAggLocation_);
                        SendEmailThreadPoolManager.executeThread(emailThread);
                    }
                    
                    alert.setDangerAlertLastSentTimestamp(new Timestamp(currentTimeInMs));
                    doUpdateAlertInDb = true;
                }
            }
        }
        
        if (doUpdateAlertInDb) {
            AlertsDao alertDao = new AlertsDao(false);
            Alert alertFromDb = alertDao.getAlert(alert.getId());

            if (alertFromDb != null) {
                alertDao.upsert(alert);
            }

            alertDao.close();
        }
        
    }

    public static Map<Integer,List<Alert>> separateAlertsByCpuCore(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Integer,List<Alert>> alertsByCpuCore = new HashMap<>();
        
        int numCoresOnSystem = Runtime.getRuntime().availableProcessors();
        if (numCoresOnSystem < 1) numCoresOnSystem = 1;
        
        int currentCore = 0;
        for (Alert alert : alerts) {
            List<Alert> alertsForSingleCore = alertsByCpuCore.get(currentCore);

            if (alertsForSingleCore == null) {
                alertsForSingleCore = new ArrayList<>();
                alertsByCpuCore.put(currentCore, alertsForSingleCore);
            }

            alertsForSingleCore.add(alert);
            
            currentCore++;
            if (currentCore > (numCoresOnSystem - 1)) currentCore = 0;
        }
 
        return alertsByCpuCore;
    }
    
    public static String appendActiveAlertsToSet(List<String> currentActiveMetricKeys, String previousActiveMetricKeys, int limit) {
        
        if ((currentActiveMetricKeys == null) || currentActiveMetricKeys.isEmpty()) {
            if (previousActiveMetricKeys == null) return "";
            else return previousActiveMetricKeys;
        }
        
        Set<String> metricKeys = new TreeSet<>();
        
        if (previousActiveMetricKeys != null) {
            String[] previousActiveMetricKeysStrings = StringUtils.split(previousActiveMetricKeys, '\n');
            metricKeys.addAll(Arrays.asList(previousActiveMetricKeysStrings));
        }
        
        int currentMetricKeyCount = metricKeys.size();
        
        for (String metricKey : currentActiveMetricKeys) {
            if (currentMetricKeyCount >= limit) {
                break;
            }
            
            metricKeys.add(metricKey.trim());
            currentMetricKeyCount++;
        }
        
        StringBuilder activeMetricKeys = new StringBuilder("");
        
        int outputMetricKeyCount = 0;
        for (String metricKey : metricKeys) {
            if (outputMetricKeyCount >= limit) {
                break;
            }
            
            activeMetricKeys.append(metricKey).append("\n");
            outputMetricKeyCount++;
        }
        
        return activeMetricKeys.toString();
    }
    
    public static Map<Integer,Alert> getAlertsByAlertId(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Integer,Alert> alertsByAlertId = new HashMap<>();
        
        for (Alert alert : alerts) {
            alertsByAlertId.put(alert.getId(), alert);
        }
        
        return alertsByAlertId;
    }
    
    public static List<Alert> getEnabledAlerts(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Alert> enabledAlerts = new ArrayList<>();
                 
        for (Alert alert : alerts) {
            if (alert.isEnabled()) {
                enabledAlerts.add(alert);
            }
        }
        
        return enabledAlerts;
    }
   
    public static List<Alert> getActiveCautionAlerts(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Alert> activeAlerts = new ArrayList<>();
        
        for (Alert alert : alerts) {
            if (alert.isCautionAlertActive()) {
                activeAlerts.add(alert);
            }
        }
        
        return activeAlerts;
    }
    
    public static List<Alert> getActiveDangerAlerts(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Alert> activeAlerts = new ArrayList<>();
        
        for (Alert alert : alerts) {
            if (alert.isDangerAlertActive()) {
                activeAlerts.add(alert);
            }
        }
        
        return activeAlerts;
    }

    public static List<Alert> getActiveAlerts(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Alert> activeAlerts = new ArrayList<>();
        
        for (Alert alert : alerts) {
            if (alert.isCautionAlertActive() || alert.isDangerAlertActive()) {
                activeAlerts.add(alert);
            }
        }
        
        return activeAlerts;
    }
        
    /*
    If the alert is not active, then this method returns null.
    If the alert is active, then this method returns a value that fits the context of the alert critera.
    */
    public static BigDecimal isAlertActive(long threadStartTimestampInMilliseconds, List<MetricTimestampAndValue> sortedRecentMetricTimestampsAndValues, 
            Integer alertType, Integer windowDuration, Integer operator, Integer combination, Integer combinationCount, BigDecimal threshold, Integer minimumSampleCount) {

        if ((sortedRecentMetricTimestampsAndValues == null) || sortedRecentMetricTimestampsAndValues.isEmpty()) {
            return null;
        }

        // get a list of timestamps that are within the alert window
        long startTimestamp = threadStartTimestampInMilliseconds - windowDuration;
        int[] timestampStartAndEndIndexes = getStartAndEndIndexesOfTimestamps(startTimestamp, threadStartTimestampInMilliseconds, sortedRecentMetricTimestampsAndValues);
        if (timestampStartAndEndIndexes == null) return null;    
        List<MetricTimestampAndValue> sortedRecentMetricTimestampsAndValuesInWindow = new ArrayList<>(sortedRecentMetricTimestampsAndValues.subList(timestampStartAndEndIndexes[0], timestampStartAndEndIndexes[1]));
        sortedRecentMetricTimestampsAndValuesInWindow.add(sortedRecentMetricTimestampsAndValues.get(timestampStartAndEndIndexes[1])); // needed b/c subList excludes the ending index element
        
        if ((alertType != null) && (alertType == Alert.TYPE_AVAILABILITY)) {
            if (sortedRecentMetricTimestampsAndValuesInWindow.isEmpty()) {
                return new BigDecimal(1);
            }
            else {
                return null;
            }
        }
        else if ((alertType != null) && (alertType == Alert.TYPE_THRESHOLD)) {
            // minimum sample count check
            boolean doesMeetMinimumSampleCountCriteria = doesMeetMinimumSampleCountCriteria(sortedRecentMetricTimestampsAndValuesInWindow.size(), minimumSampleCount);
            if (!doesMeetMinimumSampleCountCriteria) {
                return null;
            }

            BigDecimal doesMeetThresholdCriteria = null;

            // threshold check
            if (Objects.equals(combination, Alert.COMBINATION_ALL)) {
                doesMeetThresholdCriteria = doesMeetThresholdCriteria_All(sortedRecentMetricTimestampsAndValuesInWindow, threshold, operator);
            }
            else if (Objects.equals(combination, Alert.COMBINATION_ANY)) {
                doesMeetThresholdCriteria = doesMeetThresholdCriteria_Any(sortedRecentMetricTimestampsAndValuesInWindow, threshold, operator);
            }
            else if (Objects.equals(combination, Alert.COMBINATION_AVERAGE)) {
                doesMeetThresholdCriteria = doesMeetThresholdCriteria_Average(sortedRecentMetricTimestampsAndValuesInWindow, threshold, operator);
            }
            else if (Objects.equals(combination, Alert.COMBINATION_AT_LEAST_COUNT)) {
                doesMeetThresholdCriteria = doesMeetThresholdCriteria_AtLeastCount(sortedRecentMetricTimestampsAndValuesInWindow, threshold, operator, combinationCount);
            }
            else if (Objects.equals(combination, Alert.COMBINATION_AT_MOST_COUNT)) {
                doesMeetThresholdCriteria = doesMeetThresholdCriteria_AtMostCount(sortedRecentMetricTimestampsAndValuesInWindow, threshold, operator, combinationCount);
            }
            
            return doesMeetThresholdCriteria;
        }
        
        return null;
    }

    private static int[] getStartAndEndIndexesOfTimestamps(long startTimestamp, long endTimestamp, List<MetricTimestampAndValue> metricTimestampsAndValues) {

        if ((metricTimestampsAndValues == null) || metricTimestampsAndValues.isEmpty()) {
            return null;
        }
        
        if (startTimestamp > endTimestamp) {
            logger.error("Start-Timestamp cannot be before End-Timestamp");
            return null;
        }

        int[] startAndEndIndexes = new int[] {-1,-1};
        boolean foundStart = false, foundEnd = false;
        
        for (int i = 0; (i < metricTimestampsAndValues.size()); i++) {
            Long currentTimestamp = metricTimestampsAndValues.get(i).getTimestamp();
            
            if (!foundStart && (startTimestamp <= currentTimestamp)) {
                startAndEndIndexes[0] = i;
                foundStart = true;
            }
        
            if (startTimestamp == endTimestamp) {
                if (endTimestamp == currentTimestamp) {
                    startAndEndIndexes[1] = i;
                    foundEnd = true;
                    break;
                }
            }
            else {
                if ((endTimestamp < currentTimestamp)) {
                    startAndEndIndexes[1] = i - 1;
                    foundEnd = true;
                    break;
                }
                else if (endTimestamp == currentTimestamp) {
                    startAndEndIndexes[1] = i;
                    foundEnd = true;
                    break;
                }
                else if (((i + 1) == metricTimestampsAndValues.size())) {
                    startAndEndIndexes[1] = i;
                    foundEnd = true;
                    break;
                }
            }
        }
        
        if (foundStart && foundEnd) {
            if (startAndEndIndexes[1] >= startAndEndIndexes[0]) {
                return startAndEndIndexes;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
        
    }

    private static boolean doesMeetMinimumSampleCountCriteria(Integer sampleCount, Integer allowedMinimumCount) {
        
        if ((sampleCount == null) || (allowedMinimumCount == null)) {
            return false;
        }
        
        if (sampleCount < allowedMinimumCount) {
            return false;
        }
        
        return true;
    }
    
    /*
    If the criteria is not met, then return null.
    If the criteria is met, then return "the last metric value in 'recentMetricTimestampsAndValues'"
    */
    private static BigDecimal doesMeetThresholdCriteria_All(List<MetricTimestampAndValue> recentMetricTimestampsAndValues, 
            BigDecimal threshold, Integer operator) {
        
        if ((recentMetricTimestampsAndValues == null) || recentMetricTimestampsAndValues.isEmpty() || (threshold == null) || (operator == null)) {
            return null;
        }

        int greaterThanCount = 0, lessThanCount = 0, equalsCount = 0;
        
        for (MetricTimestampAndValue metricTimestampAndValue : recentMetricTimestampsAndValues) {
            int compareResult = metricTimestampAndValue.getMetricValue().compareTo(threshold);
            
            if (compareResult == -1) {
                lessThanCount++;
            }
            else if (compareResult == 0) {
                equalsCount++;
            }
            else if (compareResult == 1) {
                greaterThanCount++;
            }
        }
        
        if (Objects.equals(operator, Alert.OPERATOR_GREATER)) {
            if (greaterThanCount == recentMetricTimestampsAndValues.size()) {
                return recentMetricTimestampsAndValues.get(recentMetricTimestampsAndValues.size() - 1).getMetricValue();
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_GREATER_EQUALS)) {
            if ((greaterThanCount + equalsCount) == recentMetricTimestampsAndValues.size()) {
                return recentMetricTimestampsAndValues.get(recentMetricTimestampsAndValues.size() - 1).getMetricValue();
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS)) {
            if (lessThanCount == recentMetricTimestampsAndValues.size()) {
                return recentMetricTimestampsAndValues.get(recentMetricTimestampsAndValues.size() - 1).getMetricValue();
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS_EQUALS)) {
            if ((lessThanCount + equalsCount) == recentMetricTimestampsAndValues.size()) {
                return recentMetricTimestampsAndValues.get(recentMetricTimestampsAndValues.size() - 1).getMetricValue();
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_EQUALS)) {
            if (equalsCount == recentMetricTimestampsAndValues.size()) {
                return recentMetricTimestampsAndValues.get(recentMetricTimestampsAndValues.size() - 1).getMetricValue();
            }
        }
        
        return null;
    }
    
    /*
    If the criteria is not met, then return null.
    If the criteria is met, then return "the first metric value in 'recentMetricTimestampsAndValues' that matched the criteria"
    */
    private static BigDecimal doesMeetThresholdCriteria_Any(List<MetricTimestampAndValue> recentMetricTimestampsAndValues, 
            BigDecimal threshold, Integer operator) {
        
        if ((recentMetricTimestampsAndValues == null) || recentMetricTimestampsAndValues.isEmpty() || (threshold == null) || (operator == null)) {
            return null;
        }

        int recentMetricTimestampsAndValuesIndexSize = recentMetricTimestampsAndValues.size() - 1;
        
        for (int i = recentMetricTimestampsAndValuesIndexSize; i >= 0; i--) {
            MetricTimestampAndValue metricTimestampAndValue = recentMetricTimestampsAndValues.get(i);
            
            int compareResult = metricTimestampAndValue.getMetricValue().compareTo(threshold);
            
            if (Objects.equals(operator, Alert.OPERATOR_GREATER)) {
                if (compareResult == 1) {
                    return metricTimestampAndValue.getMetricValue();
                }
            }
            else if (Objects.equals(operator, Alert.OPERATOR_GREATER_EQUALS)) {
                if ((compareResult == 0) || (compareResult == 1)) {
                    return metricTimestampAndValue.getMetricValue();
                }
            }
            else if (Objects.equals(operator, Alert.OPERATOR_LESS)) {
                if (compareResult == -1) {
                    return metricTimestampAndValue.getMetricValue();
                }
            }
            else if (Objects.equals(operator, Alert.OPERATOR_LESS_EQUALS)) {
                if ((compareResult == 0) || (compareResult == -1)) {
                    return metricTimestampAndValue.getMetricValue();
                }
            }
            else if (Objects.equals(operator, Alert.OPERATOR_EQUALS)) {
                if (compareResult == 0) {
                    return metricTimestampAndValue.getMetricValue();
                }
            }
        }
        
        return null;
    }
   
    /*
    If the criteria is not met, then return null.
    If the criteria is met, then return "the average value of the input metric values"
    */
    private static BigDecimal doesMeetThresholdCriteria_Average(List<MetricTimestampAndValue> recentMetricTimestampsAndValues, 
            BigDecimal threshold, Integer operator) {
        
        if ((recentMetricTimestampsAndValues == null) || recentMetricTimestampsAndValues.isEmpty() || (threshold == null) || (operator == null)) {
            return null;
        }

        BigDecimal recentMetricValuesSum = new BigDecimal(0);
        
        for (MetricTimestampAndValue metricTimestampAndValue : recentMetricTimestampsAndValues) {
            recentMetricValuesSum = recentMetricValuesSum.add(metricTimestampAndValue.getMetricValue());
        }
        
        BigDecimal recentMetricValuesCount = new BigDecimal(recentMetricTimestampsAndValues.size());
        BigDecimal recentMetricValuesAverage = MathUtilities.smartBigDecimalScaleChange(recentMetricValuesSum.
                divide(recentMetricValuesCount, ALERT_MATH_CONTEXT), ALERT_SCALE, ALERT_ROUNDING_MODE);
        
        int compareResult = recentMetricValuesAverage.compareTo(threshold);
        
        if (Objects.equals(operator, Alert.OPERATOR_GREATER)) {
            if (compareResult == 1) {
                return recentMetricValuesAverage;
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_GREATER_EQUALS)) {
            if ((compareResult == 0) || (compareResult == 1)) {
                return recentMetricValuesAverage;
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS)) {
            if (compareResult == -1) {
                return recentMetricValuesAverage;
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS_EQUALS)) {
            if ((compareResult == 0) || (compareResult == -1)) {
                return recentMetricValuesAverage;
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_EQUALS)) {
            if (compareResult == 0) {
                return recentMetricValuesAverage;
            }
        }
        
        return null;
    }

    /*
    If the criteria is not met, then return null.
    If the criteria is met, then return "the # of metrics that met the 'at least' criteria"
    */
    private static BigDecimal doesMeetThresholdCriteria_AtLeastCount(List<MetricTimestampAndValue> recentMetricTimestampsAndValues, 
            BigDecimal threshold, Integer operator, Integer count) {
        
        if ((recentMetricTimestampsAndValues == null) || recentMetricTimestampsAndValues.isEmpty() || (threshold == null) || (operator == null) || (count == null)) {
            return null;
        }

        int greaterThanCount = 0, lessThanCount = 0, equalsCount = 0;
        
        for (MetricTimestampAndValue metricTimestampAndValue : recentMetricTimestampsAndValues) {
            int compareResult = metricTimestampAndValue.getMetricValue().compareTo(threshold);
            
            if (compareResult == -1) {
                lessThanCount++;
            }
            else if (compareResult == 0) {
                equalsCount++;
            }
            else if (compareResult == 1) {
                greaterThanCount++;
            }
        }
        
        if (Objects.equals(operator, Alert.OPERATOR_GREATER)) {
            if (greaterThanCount >= count) {
                return new BigDecimal(greaterThanCount);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_GREATER_EQUALS)) {
            int countSum = greaterThanCount + equalsCount;
            
            if (countSum >= count) {
                return new BigDecimal(countSum);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS)) {
            if (lessThanCount >= count) {
                return new BigDecimal(lessThanCount);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS_EQUALS)) {
            int countSum = lessThanCount + equalsCount;
                        
            if (countSum >= count) {
                return new BigDecimal(countSum);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_EQUALS)) {
            if (equalsCount >= count) {
                return new BigDecimal(equalsCount);
            }
        }
        
        return null;
    }
    
    /*
    If the criteria is not met, then return null.
    If the criteria is met, then return "the # of metrics that met the 'at most' criteria"
    */
    private static BigDecimal doesMeetThresholdCriteria_AtMostCount(List<MetricTimestampAndValue> recentMetricTimestampsAndValues, 
            BigDecimal threshold, Integer operator, Integer count) {
        
        if ((recentMetricTimestampsAndValues == null) || recentMetricTimestampsAndValues.isEmpty() || (threshold == null) || (operator == null) || (count == null)) {
            return null;
        }

        int greaterThanCount = 0, lessThanCount = 0, equalsCount = 0;
        
        for (MetricTimestampAndValue metricTimestampAndValue : recentMetricTimestampsAndValues) {
            int compareResult = metricTimestampAndValue.getMetricValue().compareTo(threshold);
            
            if (compareResult == -1) {
                lessThanCount++;
            }
            else if (compareResult == 0) {
                equalsCount++;
            }
            else if (compareResult == 1) {
                greaterThanCount++;
            }
        }
        
        if (Objects.equals(operator, Alert.OPERATOR_GREATER)) {
            if (greaterThanCount <= count) {
                return new BigDecimal(greaterThanCount);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_GREATER_EQUALS)) {
            int countSum = greaterThanCount + equalsCount;

            if (countSum <= count) {
                return new BigDecimal(countSum);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS)) {
            if (lessThanCount <= count) {
                return new BigDecimal(lessThanCount);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_LESS_EQUALS)) {
            int countSum = lessThanCount + equalsCount;

            if (countSum <= count) {
                return new BigDecimal(countSum);
            }
        }
        else if (Objects.equals(operator, Alert.OPERATOR_EQUALS)) {
            if (equalsCount <= count) {
                return new BigDecimal(equalsCount);
            }
        }
        
        return null;
    }

    public List<GraphiteMetricAggregated> generateAlertStatusMetricsForGraphite(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new ArrayList<>();
        }
        
        long timestamp = System.currentTimeMillis();

        List<GraphiteMetricAggregated> alertStatusGraphiteMetrics = new ArrayList<>();
        Set<String> alertStatusGraphiteMetricNames = new HashSet<>();

        for (Alert alert : alerts) {
            if ((alert.getName() == null) || (alert.getId() == null)) continue;
            
            BigDecimal alertGraphiteMetricValue;

            StringBuilder graphiteFormattedAlertName = new StringBuilder("");
            if (ApplicationConfiguration.isGlobalMetricNamePrefixEnabled()) graphiteFormattedAlertName.append(ApplicationConfiguration.getGlobalMetricNamePrefixValue()).append(".");
            if (ApplicationConfiguration.isAlertOutputAlertStatusToGraphite()) graphiteFormattedAlertName.append(ApplicationConfiguration.getAlertOutputAlertStatusToGraphiteMetricPrefix()).append(".");
            graphiteFormattedAlertName.append(com.pearson.statsagg.metric_aggregation.graphite.Common.getGraphiteFormattedMetricPath(alert.getName()));
            graphiteFormattedAlertName.append("~~").append(alert.getId());
            
            if (activeCautionAlertMetricKeysByAlertId_.containsKey(alert.getId()) && activeDangerAlertMetricKeysByAlertId_.containsKey(alert.getId())) {
                alertGraphiteMetricValue = new BigDecimal(3);
            }
            else if (activeDangerAlertMetricKeysByAlertId_.containsKey(alert.getId())) {
                alertGraphiteMetricValue = new BigDecimal(2);
            }
            else if (activeCautionAlertMetricKeysByAlertId_.containsKey(alert.getId())) {
                alertGraphiteMetricValue = new BigDecimal(1);
            }
            else {
                alertGraphiteMetricValue = BigDecimal.ZERO;
            }
            
            // correct for duplicate names by adding a '+' sign to the end of the alert name
            while(alertStatusGraphiteMetricNames.contains(graphiteFormattedAlertName.toString())) {
                graphiteFormattedAlertName.append("+");
            }
            
            String graphiteFormattedAlertName_Final = graphiteFormattedAlertName.toString();
            alertStatusGraphiteMetricNames.add(graphiteFormattedAlertName_Final);
            GraphiteMetricAggregated graphiteMetricAggregated = new GraphiteMetricAggregated(graphiteFormattedAlertName_Final, alertGraphiteMetricValue, timestamp, timestamp);
            alertStatusGraphiteMetrics.add(graphiteMetricAggregated);
        }
        
        return alertStatusGraphiteMetrics;
    }

    
}
