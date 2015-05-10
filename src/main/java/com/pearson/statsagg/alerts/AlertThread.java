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
import com.pearson.statsagg.database.metric_last_seen.MetricLastSeen;
import com.pearson.statsagg.database.metric_last_seen.MetricLastSeenDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.metric_aggregation.graphite.GraphiteMetric;
import com.pearson.statsagg.metric_aggregation.threads.SendMetricsToGraphiteThread;
import com.pearson.statsagg.utilities.MathUtilities;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.Threads;
import com.pearson.statsagg.webui.StatsAggHtmlFramework;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
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
    private static final ConcurrentHashMap<Integer,Map<String,String>> positiveAlertReasons_Caution_ByAlertId_ = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer,Map<String,String>> positiveAlertReasons_Danger_ByAlertId_ = new ConcurrentHashMap<>();
    
    protected final Long threadStartTimestampInMilliseconds_;
    protected final boolean runMetricAssociationRoutine_;
    protected final boolean runAlertRoutine_;
    protected final String threadId_;
    protected final String statsAggLocation_;
    
    private final List<Alert> enabledAlerts_ = new ArrayList<>();
    private Map<Integer,Alert> alertsByAlertId_ = null;    
    
    private final Map<Integer, List<String>> activeCautionAlertMetricKeysByAlertId_ = new ConcurrentHashMap<>();
    private final Map<Integer, List<String>> activeDangerAlertMetricKeysByAlertId_ = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> activeCautionAlertMetricValues_ = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> activeDangerAlertMetricValues_ = new ConcurrentHashMap<>();
    private final Map<Integer, Set<String>> activeCautionAvailabilityAlerts_ = new ConcurrentHashMap<>();
    private final Map<Integer, Set<String>> activeDangerAvailabilityAlerts_ = new ConcurrentHashMap<>();
    
    private final AtomicLong activeCautionAlertMetricKeysByAlertId_Counter_ = new AtomicLong(0);
    private final AtomicLong activeDangerAlertMetricKeysByAlertId_Counter_ = new AtomicLong(0);
    private final AtomicLong activeCautionAlertMetricValues_Counter_ = new AtomicLong(0);
    private final AtomicLong activeDangerAlertMetricValues_Counter_ = new AtomicLong(0);
    
    private AlertSuspensions alertSuspensions_ = null;
    
    public AlertThread(Long threadStartTimestampInMilliseconds, boolean runMetricAssociationRoutine, boolean runAlertRoutine) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.runMetricAssociationRoutine_ = runMetricAssociationRoutine;
        this.runAlertRoutine_ = runAlertRoutine;

        this.threadId_ = "A-" + threadStartTimestampInMilliseconds_.toString();
        this.statsAggLocation_ = ApplicationConfiguration.getAlertStatsAggLocation();
    }
    
    @Override
    public void run() {
  
        // stops multiple alert threads from running simultaneously 
        if (!isThreadCurrentlyRunning_.compareAndSet(false, true)) {
            logger.warn("ThreadId=" + threadId_ + ", Routine=Alert, Message=\"Only 1 alert thread can run at a time\"");
            return;
        }
        
        // run the metric association routine
        long metricAssociationTimeElasped = 0;
        if (ApplicationConfiguration.isAlertRoutineEnabled() && runMetricAssociationRoutine_) {
            long metricAssociationStartTime = System.currentTimeMillis();
            MetricAssociation.associateMetricKeysWithMetricGroups(threadId_);
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
            
            // run the alerting routine
            if (ApplicationConfiguration.isAlertRoutineEnabled() && runAlertRoutine_) {
                long alertRoutineStartTime = System.currentTimeMillis();
                runAlertRoutine(alerts);
                alertRoutineTimeElasped = System.currentTimeMillis() - alertRoutineStartTime; 
                
                if (SendMetricsToGraphiteThread.isAnyGraphiteOutputModuleEnabled()) {
                    // generate messages for graphite
                    List<GraphiteMetric> alertStatusMetricsForGraphite = generateAlertStatusMetricsForGraphite(alerts);
                    
                    // send to graphite
                    SendMetricsToGraphiteThread.sendMetricsToGraphiteEndpoints(alertStatusMetricsForGraphite, threadId_, ApplicationConfiguration.getFlushTimeAgg());
                }
            }
        }

        String outputMessage = "ThreadId=" + threadId_
                + ", Routine=Alert"
                + ", MetricAssociationTime=" + metricAssociationTimeElasped
                + ", AlertRoutineTime=" + alertRoutineTimeElasped
                + ", AlertSuspensionRoutineTime=" + alertSuspensionRoutineTimeElapsed
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
        
        // syncs the local 'active danger availability alerts' objects with the global objects
        activeCautionAvailabilityAlerts_.putAll(GlobalVariables.activeCautionAvailabilityAlerts);
        activeDangerAvailabilityAlerts_.putAll(GlobalVariables.activeDangerAvailabilityAlerts);
        
        // clears all data about 'availability alert statuses' if the alert is disabled
        removeDisabledActiveAvailabilityAlerts(alerts);
        
        // removes all data about 'availability alert statuses' for alerts that were deleted
        removeDeletedActiveAvailabilityAlerts();
        
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
        
        // updates the 'metric last seen' table in the database with the current set of values. this also removes values from the database that aren't needed any longer.
        updateMetricLastSeen();
        
        // updates all global variables related to the alert-routine
        updateAlertGlobalVariables();
    }
    
    private void removeDisabledActiveAvailabilityAlerts(List<Alert> alerts) {
        
        if (alerts == null) {
            return;
        }
        
        for (Alert alert : alerts) {
            if ((alert.isEnabled() != null) && !alert.isEnabled() && (alert.getId() != null)) {
                if (activeCautionAvailabilityAlerts_ != null) activeCautionAvailabilityAlerts_.remove(alert.getId());
                if (activeDangerAvailabilityAlerts_ != null) activeDangerAvailabilityAlerts_.remove(alert.getId());
            }
        }
        
    }
    
    private void removeDeletedActiveAvailabilityAlerts() {
        
        // caution
        List<Integer> alertIdsToDelete_Caution = new ArrayList<>();
        
        for (Integer alertId : activeCautionAvailabilityAlerts_.keySet()) {
            if (!alertsByAlertId_.containsKey(alertId)) {
                alertIdsToDelete_Caution.add(alertId);
            }
        }
        
        for (Integer alertId : alertIdsToDelete_Caution) {
            activeCautionAvailabilityAlerts_.remove(alertId);
        }

        // danger
        List<Integer> alertIdsToDelete_Danger = new ArrayList<>();
        
        for (Integer alertId : activeDangerAvailabilityAlerts_.keySet()) {
            if (!alertsByAlertId_.containsKey(alertId)) {
                alertIdsToDelete_Danger.add(alertId);
            }
        }
        
        for (Integer alertId : alertIdsToDelete_Danger) {
            activeDangerAvailabilityAlerts_.remove(alertId);
        }
    }
    
    /*
    Reset all static variables associated with the alert routine
    */
    public static void reset() {
        isThreadCurrentlyRunning_.set(false);
        alertRoutineExecutionCounter_.set(0);
        pendingCautionAlertsByAlertId_.clear();
        pendingDangerAlertsByAlertId_.clear();
        positiveAlertReasons_Caution_ByAlertId_.clear();
        positiveAlertReasons_Danger_ByAlertId_.clear();
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
                Alert alert = pendingCautionAlertsByAlertId_.get(alertId);
                
                if ((alert.getAlertType() != null) && 
                        (alert.getAlertType() == Alert.TYPE_THRESHOLD) && 
                        activeCautionAlertMetricKeysByAlertId_.containsKey(alertId) && 
                        (activeCautionAlertMetricKeysByAlertId_.get(alertId) != null) && 
                        !activeCautionAlertMetricKeysByAlertId_.get(alertId).isEmpty()) {
                    hasAlertReachedPreviousState = true;
                }

                if (hasAlertReachedPreviousState) {
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

        synchronized(GlobalVariables.activeCautionAvailabilityAlerts) {
            GlobalVariables.activeCautionAvailabilityAlerts.clear();
            while (GlobalVariables.activeCautionAvailabilityAlerts.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.activeCautionAvailabilityAlerts.putAll(activeCautionAvailabilityAlerts_);
            while (GlobalVariables.activeCautionAvailabilityAlerts.size() != activeCautionAvailabilityAlerts_.size()) {
                logger.warn("Message=\"activeCautionAvailabilityAlerts size mismatch\", Size=" + GlobalVariables.activeCautionAvailabilityAlerts.size() +
                    ", Expected=" + activeCautionAvailabilityAlerts_.size());
                Threads.sleepMilliseconds(10);
            }
        }
            
        synchronized(GlobalVariables.activeDangerAvailabilityAlerts) {
            GlobalVariables.activeDangerAvailabilityAlerts.clear();
            while (GlobalVariables.activeDangerAvailabilityAlerts.size() > 0) Threads.sleepMilliseconds(1);
            GlobalVariables.activeDangerAvailabilityAlerts.putAll(activeDangerAvailabilityAlerts_);
            while (GlobalVariables.activeDangerAvailabilityAlerts.size() != activeDangerAvailabilityAlerts_.size()) {
                logger.warn("Message=\"activeDangerAvailabilityAlerts size mismatch\", Size=" + GlobalVariables.activeDangerAvailabilityAlerts.size() +
                    ", Expected=" + activeDangerAvailabilityAlerts_.size());
                Threads.sleepMilliseconds(10);
            }
        }
        
        synchronized(GlobalVariables.activeAvailabilityAlerts) {
            GlobalVariables.activeAvailabilityAlerts.clear();
            while (GlobalVariables.activeAvailabilityAlerts.size() > 0) Threads.sleepMilliseconds(1);
            
            for (Integer alertId : activeCautionAvailabilityAlerts_.keySet()) {
                Set<String> activeCautionAvailabilityMetricKeys = activeCautionAvailabilityAlerts_.get(alertId);
                if (activeCautionAvailabilityMetricKeys != null) {
                    for (String metricKey : activeCautionAvailabilityMetricKeys) GlobalVariables.activeAvailabilityAlerts.put(metricKey, metricKey);
                }
            }
            
            for (Integer alertId : activeDangerAvailabilityAlerts_.keySet()) {
                Set<String> activeDangerAvailabilityMetricKeys = activeDangerAvailabilityAlerts_.get(alertId);
                if (activeDangerAvailabilityMetricKeys != null) {
                    for (String metricKey : activeDangerAvailabilityMetricKeys) GlobalVariables.activeAvailabilityAlerts.put(metricKey, metricKey);
                }
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
            determineAlertStatus_Thread.setPriority(3);
            determineAlertStatusThreads.add(determineAlertStatus_Thread);
        }

        Threads.threadExecutorFixedPool(determineAlertStatusThreads, alertsByCpuCore.size(), threadTimeoutInMilliseconds, TimeUnit.MILLISECONDS);

        waitForConcurrentHashMapsToSettle();
    }

    public Set<String> getMetricKeysAssociatedWithActiveAvailabilityAlerts() {
        
        Set<String> metricKeys = new HashSet<>();
        
        if (activeCautionAvailabilityAlerts_ != null) {
            for (Integer alertId : activeCautionAvailabilityAlerts_.keySet()) {
                try {
                    metricKeys.addAll(activeCautionAvailabilityAlerts_.get(alertId));
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        
        if (activeDangerAvailabilityAlerts_ != null) {
            for (Integer alertId : activeDangerAvailabilityAlerts_.keySet()) {
                try {
                    metricKeys.addAll(activeDangerAvailabilityAlerts_.get(alertId));
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }

        return metricKeys;
    }
    
    private void updateMetricLastSeen() {
        
        MetricLastSeenDao metricLastSeenDao = new MetricLastSeenDao(false);
        metricLastSeenDao.getDatabaseInterface().setIsManualTransactionControl(true);
        metricLastSeenDao.getDatabaseInterface().beginTransaction();
        List<MetricLastSeen> metricLastSeens = metricLastSeenDao.getAllDatabaseObjectsInTable();
        
        Map<String,MetricLastSeen> metricKeySha1sFromDb_ByMetricKey = new HashMap<>();

        if (metricLastSeens != null) {
            for (MetricLastSeen metricLastSeen : metricLastSeens) {
                if ((metricLastSeen.getMetricKey() == null) || (metricLastSeen.getMetricKeySha1() == null)) continue;
                metricKeySha1sFromDb_ByMetricKey.put(metricLastSeen.getMetricKey(), metricLastSeen);
            }
        }
        
        // gets the current set of metric-keys that we need to track & persist in the database
        Set<String> metricKeysAssociatedWithActiveAvailabilityAlerts = getMetricKeysAssociatedWithActiveAvailabilityAlerts();
        
        if (metricKeysAssociatedWithActiveAvailabilityAlerts != null) {            
            // removes metric-keys that don't need to be in the db any longer
            for (String metricKey : metricKeySha1sFromDb_ByMetricKey.keySet()) {
                if (!metricKeysAssociatedWithActiveAvailabilityAlerts.contains(metricKey)) { 
                    metricLastSeenDao.delete(metricKeySha1sFromDb_ByMetricKey.get(metricKey).getMetricKeySha1());
                }
            }
            
            // updates the database with the current set of tracked 'metric last seen' values
            List<MetricLastSeen> metricLastSeens_PutInDatabase = new ArrayList<>();
            
            for (String metricKey : metricKeysAssociatedWithActiveAvailabilityAlerts) {
                String metricKeySha1;
                MetricLastSeen metricLastSeenFromDb = metricKeySha1sFromDb_ByMetricKey.get(metricKey);
                
                if ((metricLastSeenFromDb != null) && (metricLastSeenFromDb.getMetricKeySha1() != null)) metricKeySha1 = metricLastSeenFromDb.getMetricKeySha1();
                else metricKeySha1 = DigestUtils.sha1Hex(metricKey);

                Long timestamp = GlobalVariables.metricKeysLastSeenTimestamp.get(metricKey);

                if ((metricKeySha1 != null) && (timestamp != null)) {
                    Timestamp lastSeenTimestamp = new Timestamp(timestamp);
                    MetricLastSeen metricLastSeen = new MetricLastSeen(metricKeySha1, metricKey, lastSeenTimestamp);
                    
                    if ((metricLastSeenFromDb == null) || !metricLastSeenFromDb.isEqual(metricLastSeen)) {
                        metricLastSeens_PutInDatabase.add(metricLastSeen);
                    }
                }
            }
            
            metricLastSeenDao.batchUpsert(metricLastSeens_PutInDatabase);
        }

        metricLastSeenDao.getDatabaseInterface().endTransaction(true);
        metricLastSeenDao.close();
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
                    Set<MetricTimestampAndValue> recentMetricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);
                    List<MetricTimestampAndValue> recentMetricTimestampsAndValuesLocal = null;

                    if (recentMetricTimestampsAndValues != null) {
                        synchronized(recentMetricTimestampsAndValues) {
                            recentMetricTimestampsAndValuesLocal = new ArrayList<>(recentMetricTimestampsAndValues);
                        }
                        
                        Collections.sort(recentMetricTimestampsAndValuesLocal, MetricTimestampAndValue.COMPARE_BY_TIMESTAMP);
                    }

                    if (isCautionAlertCriteriaValid && (alert.isCautionEnabled() != null) && alert.isCautionEnabled()) {
                        determineAlertStatus_Caution(alert, alertThread__, recentMetricTimestampsAndValuesLocal, metricKey);
                    }

                    if (isDangerAlertCriteriaValid && (alert.isDangerEnabled() != null) && alert.isDangerEnabled()) {
                        determineAlertStatus_Danger(alert, alertThread__, recentMetricTimestampsAndValuesLocal, metricKey);
                    }
                }
            }
        }
    }

    private static void determineAlertStatus_Caution(Alert alert, AlertThread alertThread, List<MetricTimestampAndValue> recentMetricTimestampsAndValues, String metricKey) {
        
        if ((alert == null) || (metricKey == null) || (alert.getId() == null)) {
            return;
        }
        
        BigDecimal activeAlertValue = null;
        
        if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
            Long metricKeyLastSeenTimestamp = GlobalVariables.metricKeysLastSeenTimestamp.get(metricKey);
            activeAlertValue = isAlertActive_Availability(alertThread.threadStartTimestampInMilliseconds_, metricKeyLastSeenTimestamp, alert.getAlertType(), alert.getCautionWindowDuration());
            Set<String> activeCautionAvailabilityMetricKeys = alertThread.activeCautionAvailabilityAlerts_.get(alert.getId());
            
            if ((metricKeyLastSeenTimestamp == null) && (activeAlertValue == null)) {
                if (activeCautionAvailabilityMetricKeys != null) activeCautionAvailabilityMetricKeys.remove(metricKey);
                
                positiveAlertReasons_Caution_ByAlertId_.putIfAbsent(alert.getId(), new ConcurrentHashMap<String,String>());
                Map<String,String> positiveAlertReasons = positiveAlertReasons_Caution_ByAlertId_.get(alert.getId());
                positiveAlertReasons.put(metricKey, "Inconclusive");
            }
            else if (activeAlertValue == null) { // a recent metric value has been detected -- so the availability alert is not active
                if (activeCautionAvailabilityMetricKeys != null) activeCautionAvailabilityMetricKeys.remove(metricKey);
                
                positiveAlertReasons_Caution_ByAlertId_.putIfAbsent(alert.getId(), new ConcurrentHashMap<String,String>());
                Map<String,String> positiveAlertReasons = positiveAlertReasons_Caution_ByAlertId_.get(alert.getId());
                positiveAlertReasons.put(metricKey, "New data point(s) received");
            }
            else { // a recent metric value has not been detected -- so the availability alert is active
                if ((alert.getCautionStopTrackingAfter() != null) && (alert.getCautionStopTrackingAfter() < activeAlertValue.longValue())) { // enough time has passed that we 'stop tracking' the metric key. the availability alert to inactive for this metric key 
                    if (activeCautionAvailabilityMetricKeys != null) activeCautionAvailabilityMetricKeys.remove(metricKey);
                    activeAlertValue = null;
                    
                    positiveAlertReasons_Caution_ByAlertId_.putIfAbsent(alert.getId(), new ConcurrentHashMap<String,String>());
                    Map<String,String> positiveAlertReasons = positiveAlertReasons_Caution_ByAlertId_.get(alert.getId());
                    positiveAlertReasons.put(metricKey, "Reached 'Stop Tracking' time limit");
                }
                else if (activeCautionAvailabilityMetricKeys == null) { // the availability alert is active
                    Set<String> activeCautionAvailabilityMetricKeys_New = Collections.synchronizedSet(new HashSet<String>());
                    activeCautionAvailabilityMetricKeys_New.add(metricKey);
                    alertThread.activeCautionAvailabilityAlerts_.put(alert.getId(), activeCautionAvailabilityMetricKeys_New);                    
                }
                else {
                    activeCautionAvailabilityMetricKeys.add(metricKey);
                } 
            }
        }
        else if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) {
            activeAlertValue = isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, recentMetricTimestampsAndValues, alert.getAlertType(),
                    alert.getCautionWindowDuration(), alert.getCautionOperator(), alert.getCautionCombination(), alert.getCautionCombinationCount(), 
                    alert.getCautionThreshold(), alert.getCautionMinimumSampleCount());
        }
        
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
        
        if ((alert == null) || (metricKey == null) || (alert.getId() == null)) {
            return;
        }
        
        BigDecimal activeAlertValue = null;
        
        if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
            Long metricKeyLastSeenTimestamp = GlobalVariables.metricKeysLastSeenTimestamp.get(metricKey);
            activeAlertValue = isAlertActive_Availability(alertThread.threadStartTimestampInMilliseconds_, metricKeyLastSeenTimestamp, alert.getAlertType(), alert.getDangerWindowDuration());
            Set<String> activeDangerAvailabilityMetricKeys = alertThread.activeDangerAvailabilityAlerts_.get(alert.getId());
            
            if ((metricKeyLastSeenTimestamp == null) && (activeAlertValue == null)) {
                if (activeDangerAvailabilityMetricKeys != null) activeDangerAvailabilityMetricKeys.remove(metricKey);
                
                positiveAlertReasons_Danger_ByAlertId_.putIfAbsent(alert.getId(), new ConcurrentHashMap<String,String>());
                Map<String,String> positiveAlertReasons = positiveAlertReasons_Danger_ByAlertId_.get(alert.getId());
                positiveAlertReasons.put(metricKey, "Inconclusive");
            }
            else if (activeAlertValue == null) { // a recent metric value has been detected -- so the availability alert is not active
                if (activeDangerAvailabilityMetricKeys != null) activeDangerAvailabilityMetricKeys.remove(metricKey);
                
                positiveAlertReasons_Danger_ByAlertId_.putIfAbsent(alert.getId(), new ConcurrentHashMap<String,String>());
                Map<String,String> positiveAlertReasons = positiveAlertReasons_Danger_ByAlertId_.get(alert.getId());
                positiveAlertReasons.put(metricKey, "New data point(s) received");
            }
            else { // a recent metric value has not been detected -- so the availability alert is active
                if ((alert.getDangerStopTrackingAfter() != null) && (alert.getDangerStopTrackingAfter() < activeAlertValue.longValue())) { // enough time has passed that we 'stop tracking' the metric key. the availability alert to inactive for this metric key 
                    if (activeDangerAvailabilityMetricKeys != null) activeDangerAvailabilityMetricKeys.remove(metricKey);
                    activeAlertValue = null;
                    
                    positiveAlertReasons_Danger_ByAlertId_.putIfAbsent(alert.getId(), new ConcurrentHashMap<String,String>());
                    Map<String,String> positiveAlertReasons = positiveAlertReasons_Danger_ByAlertId_.get(alert.getId());
                    positiveAlertReasons.put(metricKey, "Reached 'Stop Tracking' time limit");
                }
                else if (activeDangerAvailabilityMetricKeys == null) { // the availability alert is active
                    Set<String> activeDangerAvailabilityMetricKeys_New = Collections.synchronizedSet(new HashSet<String>());
                    activeDangerAvailabilityMetricKeys_New.add(metricKey);
                    alertThread.activeDangerAvailabilityAlerts_.put(alert.getId(), activeDangerAvailabilityMetricKeys_New);                    
                }
                else {
                    activeDangerAvailabilityMetricKeys.add(metricKey);
                } 
            }
        }
        else if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) {
            activeAlertValue = isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, recentMetricTimestampsAndValues, alert.getAlertType(),
                    alert.getDangerWindowDuration(), alert.getDangerOperator(), alert.getDangerCombination(), alert.getDangerCombinationCount(), 
                    alert.getDangerThreshold(), alert.getDangerMinimumSampleCount());
        }
        
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
        
        Map<String,String> positiveAlertReasons_Caution = positiveAlertReasons_Caution_ByAlertId_.get(alert.getId());
        
        // the alert is suspended (the entire alert, not just notifications)
        if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) != null) && alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) &&
                (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) != null) && (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) == AlertSuspensions.SUSPEND_ENTIRE_ALERT)) {
            alert.setIsCautionAlertActive(false);
            alert.setCautionFirstActiveAt(null);
            alert.setCautionAlertLastSentTimestamp(null);
            alert.setCautionActiveAlertsSet(null);
            doUpdateAlertInDb = true;
        }
        // not active -> active
        else if ((((alert.isCautionAlertActive() != null) && !alert.isCautionAlertActive()) || (alert.isCautionAlertActive() == null)) && isCautionAlertActive) {
            alert.setIsCautionAlertActive(true);
            alert.setCautionFirstActiveAt(new Timestamp(currentTimeInMs));
            alert.setIsCautionAcknowledged(false);
            alert.setCautionAlertLastSentTimestamp(new Timestamp(currentTimeInMs));
            
            if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                Alert alertCopy = Alert.copy(alert);
                EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, activeCautionAlertMetricValues_, positiveAlertReasons_Caution, false, false, statsAggLocation_);
                SendEmailThreadPoolManager.executeThread(emailThread);
            }

            doUpdateAlertInDb = true;
        } 
        // active -> not active
        else if (alert.isCautionAlertActive() && !isCautionAlertActive) {
            alert.setIsCautionAlertActive(false);
            
            if ((alert.isAlertOnPositive() != null) && alert.isAlertOnPositive()) {
                if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                    Alert alertCopy = Alert.copy(alert);
                    EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, activeCautionAlertMetricValues_, positiveAlertReasons_Caution, true, false, statsAggLocation_);
                    SendEmailThreadPoolManager.executeThread(emailThread);
                }
            }
            
            positiveAlertReasons_Caution_ByAlertId_.remove(alert.getId());
            alert.setCautionFirstActiveAt(null);
            alert.setIsCautionAcknowledged(null);
            alert.setCautionActiveAlertsSet(null);
            alert.setCautionAlertLastSentTimestamp(null);
            doUpdateAlertInDb = true;
        } 
        // active -> active
        else if (alert.isCautionAlertActive() && isCautionAlertActive) {
            if (alert.isAllowResendAlert() && (alert.getCautionAlertLastSentTimestamp() != null) && ((alert.isCautionAcknowledged() == null) || !alert.isCautionAcknowledged())) { 
                long timeSinceLastNotificationInMs = currentTimeInMs - alert.getCautionAlertLastSentTimestamp().getTime();
                
                if (timeSinceLastNotificationInMs >= alert.getSendAlertEveryNumMilliseconds()) {
                    if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                        Alert alertCopy = Alert.copy(alert);
                        EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, activeCautionAlertMetricValues_, positiveAlertReasons_Caution, false, true, statsAggLocation_);
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
        
        Map<String,String> positiveAlertReasons_Danger = positiveAlertReasons_Danger_ByAlertId_.get(alert.getId());
        
        // the alert is suspended (the entire alert, not just notifications)
        if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) != null) && alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) &&
                (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) != null) && (alertSuspensions_.getAlertSuspensionLevelsByAlertId().get(alert.getId()) == AlertSuspensions.SUSPEND_ENTIRE_ALERT)) {
            alert.setIsDangerAlertActive(false);
            alert.setDangerFirstActiveAt(null);
            alert.setDangerAlertLastSentTimestamp(null);
            alert.setDangerActiveAlertsSet(null);
            doUpdateAlertInDb = true;
        }
        // not active -> active
        else if ((((alert.isDangerAlertActive() != null) && !alert.isDangerAlertActive()) || (alert.isDangerAlertActive() == null)) && isDangerAlertActive) {
            alert.setIsDangerAlertActive(true);
            alert.setDangerFirstActiveAt(new Timestamp(currentTimeInMs));
            alert.setIsDangerAcknowledged(false);
            alert.setDangerAlertLastSentTimestamp(new Timestamp(currentTimeInMs));
            
            if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                Alert alertCopy = Alert.copy(alert);
                EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_DANGER, metricKeys, activeDangerAlertMetricValues_, positiveAlertReasons_Danger, false, false, statsAggLocation_);
                SendEmailThreadPoolManager.executeThread(emailThread);
            }
            
            doUpdateAlertInDb = true;
        } 
        // active -> not active
        else if (alert.isDangerAlertActive() && !isDangerAlertActive) {
            alert.setIsDangerAlertActive(false);
            
            if ((alert.isAlertOnPositive() != null) && alert.isAlertOnPositive()) {
                if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                    Alert alertCopy = Alert.copy(alert);
                    EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_DANGER, metricKeys, activeDangerAlertMetricValues_, positiveAlertReasons_Danger, true, false, statsAggLocation_);
                    SendEmailThreadPoolManager.executeThread(emailThread);
                }
            }
            
            positiveAlertReasons_Danger_ByAlertId_.remove(alert.getId());
            alert.setDangerFirstActiveAt(null);
            alert.setIsDangerAcknowledged(null);
            alert.setDangerActiveAlertsSet(null);
            alert.setDangerAlertLastSentTimestamp(null);
            doUpdateAlertInDb = true;
        } 
        // active -> active
        else if (alert.isDangerAlertActive() && isDangerAlertActive) {
            if (alert.isAllowResendAlert() && (alert.getDangerAlertLastSentTimestamp() != null) && ((alert.isDangerAcknowledged() == null) || !alert.isDangerAcknowledged())) { 
                long timeSinceLastNotificationInMs = currentTimeInMs - alert.getDangerAlertLastSentTimestamp().getTime();
                
                if (timeSinceLastNotificationInMs >= alert.getSendAlertEveryNumMilliseconds()) {
                    if ((alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId()) == null) || !alertSuspensions_.getAlertSuspensionStatusByAlertId().get(alert.getId())) {
                        Alert alertCopy = Alert.copy(alert);
                        EmailThread emailThread = new EmailThread(alertCopy, EmailThread.WARNING_LEVEL_DANGER, metricKeys, activeDangerAlertMetricValues_, positiveAlertReasons_Danger, false, true, statsAggLocation_);
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
        
        StringBuilder activeMetricKeys = new StringBuilder();
        
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
    If the alert is active, then this method returns the time elapsed (in milliseconds) since the metric-key was last seen 
    */
    public static BigDecimal isAlertActive_Availability(long threadStartTimestampInMilliseconds, Long metricKeyLastSeenTimestamp, Integer alertType, Long windowDuration) {
        
        if ((metricKeyLastSeenTimestamp == null) || (alertType == null) || (alertType != Alert.TYPE_AVAILABILITY) || (windowDuration == null)) {
            return null;
        }
        
        long oldestTimestampAllowed = threadStartTimestampInMilliseconds - windowDuration;

        if (metricKeyLastSeenTimestamp < oldestTimestampAllowed) {
            long timeElapsedSinceMetricKeyLastSeenTimestamp = threadStartTimestampInMilliseconds - metricKeyLastSeenTimestamp;
            return new BigDecimal(timeElapsedSinceMetricKeyLastSeenTimestamp);
        }
        else {
            return null;
        }
    }
    
    /*
    If the alert is not active, then this method returns null.
    If the alert is active, then this method returns a value that fits the context of the alert critera.
    */
    public static BigDecimal isAlertActive_Threshold(long threadStartTimestampInMilliseconds, List<MetricTimestampAndValue> sortedRecentMetricTimestampsAndValues, 
            Integer alertType, Long windowDuration, Integer operator, Integer combination, Integer combinationCount, BigDecimal threshold, Integer minimumSampleCount) {

        if ((sortedRecentMetricTimestampsAndValues == null) || sortedRecentMetricTimestampsAndValues.isEmpty() || 
                (alertType == null) || (alertType != Alert.TYPE_THRESHOLD) || (windowDuration == null)) {
            return null;
        }

        // get a list of timestamps that are within the alert window
        long startTimestamp = threadStartTimestampInMilliseconds - windowDuration;
        int[] timestampStartAndEndIndexes = getStartAndEndIndexesOfTimestamps(startTimestamp, threadStartTimestampInMilliseconds, sortedRecentMetricTimestampsAndValues);
        if (timestampStartAndEndIndexes == null) return null;
        List<MetricTimestampAndValue> sortedRecentMetricTimestampsAndValuesInWindow = new ArrayList<>(sortedRecentMetricTimestampsAndValues.subList(timestampStartAndEndIndexes[0], timestampStartAndEndIndexes[1]));
        sortedRecentMetricTimestampsAndValuesInWindow.add(sortedRecentMetricTimestampsAndValues.get(timestampStartAndEndIndexes[1])); // needed b/c subList excludes the ending index element

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
        
        return (sampleCount >= allowedMinimumCount);
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

    public List<GraphiteMetric> generateAlertStatusMetricsForGraphite(List<Alert> alerts) {
        
        if ((alerts == null) || alerts.isEmpty()) {
            return new ArrayList<>();
        }
        
        long timestamp = System.currentTimeMillis();

        List<GraphiteMetric> alertStatusGraphiteMetrics = new ArrayList<>();
        Set<String> alertStatusGraphiteMetricNames = new HashSet<>();

        for (Alert alert : alerts) {
            if ((alert.getName() == null) || (alert.getId() == null)) continue;
            
            BigDecimal alertGraphiteMetricValue;

            StringBuilder graphiteFormattedAlertName = new StringBuilder();
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
            GraphiteMetric graphiteMetric = new GraphiteMetric(graphiteFormattedAlertName_Final, alertGraphiteMetricValue, timestamp, timestamp);
            alertStatusGraphiteMetrics.add(graphiteMetric);
        }
        
        return alertStatusGraphiteMetrics;
    }
    
}
