package com.pearson.statsagg.threads.alert_related;

import com.pearson.statsagg.threads.internal_housekeeping.CleanupThread;
import com.google.common.collect.Lists;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklistDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import com.pearson.statsagg.utilities.core_utils.Threads;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAssociation {

    private static final Logger logger = LoggerFactory.getLogger(MetricAssociation.class.getName());

    private static final byte REGEX_TYPE_BLACKLIST = 1;
    private static final byte REGEX_TYPE_MATCH = 2;
    
    // k=Regex_String, v="Regex compiled pattern. This is a cache for compiled regex patterns."
    private final static ConcurrentHashMap<String,MetricAssociationPattern> regexPatterns_ = new ConcurrentHashMap<>(); 
    
    // k=Regex_String, v="Regex_String. If a regex pattern is bad (doesn't compile), then it is stored here so we don't try to recompile it."
    private final static ConcurrentHashMap<String,String> regexBlacklist_ = new ConcurrentHashMap<>(); 
    
    public static final AtomicBoolean IsMetricAssociationRoutineCurrentlyRunning = new AtomicBoolean(false);
    public static final AtomicBoolean IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating = new AtomicBoolean(false);
    public static final AtomicBoolean IsMetricAssociationRoutineForOutputBlacklistCurrentlyRunning = new AtomicBoolean(false);
    public static final AtomicBoolean IsMetricAssociationRoutineForOutputBlacklistCurrentlyRunning_CurrentlyAssociating = new AtomicBoolean(false);
    public static final AtomicBoolean IsMetricGroupChangeOutputBlacklist = new AtomicBoolean(false); 
    
    // the core routine that associates metrics with metric-groups & suspensions. this is called periodically by the alert routine
    protected static void associateMetricKeysWithMetricGroups(String threadId, ThreadPoolExecutor threadPoolExecutor, int numThreads) {
        
        try {
            // stops multiple metric association routines from running simultaneously 
            if (!IsMetricAssociationRoutineCurrentlyRunning.compareAndSet(false, true)) {
                if ((threadPoolExecutor != null) && (threadPoolExecutor.getActiveCount() <= 1)) {
                    logger.warn("Invalid state detected (detected that statsagg thinks another thread is running the metric association routine, but it is not.");
                    IsMetricAssociationRoutineCurrentlyRunning.set(false);
                }
                else {
                    logger.warn("ThreadId=" + threadId + ", Routine=MetricAssociation, Message=\"Only 1 metric association routine can run at a time\"");
                    return;
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        //  wait until the the cleanup thread is done running
        if (CleanupThread.isCleanupThreadCurrentlyRunning.get()) Threads.sleepMilliseconds(50, false);
        
        // set a flag to indicate that the metric association routine is running -- prevents other threads from running at the same time
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(true);
        try {
            // get metric group & suspension ids & populate some data structures for those ids
            List<Integer> allMetricGroupIds = getMetricGroupIds_And_AssociateMetricKeysWithNewOrAlteredMetricGroups();
            List<Integer> allMetricSuspensionIds = getMetricSuspensionIds_And_AssociateMetricKeysWithNewOrAlteredSuspensions();
            createMatchingMetricKeysDataStructuresForNewMetricGroupIds(allMetricGroupIds);
            createMatchingMetricKeysDataStructuresForNewSuspensionIds(allMetricSuspensionIds);

            int threads = numThreads;
            if (threads <= 0) threads = 1;
            int numMetricsPerPartition = GlobalVariables.metricKeysLastSeenTimestamp.keySet().size() / threads;
            if (numMetricsPerPartition <= 0) numMetricsPerPartition = 1;
            ArrayList<String> metricsList = new ArrayList<>(GlobalVariables.metricKeysLastSeenTimestamp.keySet());
            List<List<String>> metricKeys_Partitions = Lists.partition(metricsList, numMetricsPerPartition);
            List<Thread> metricKeyAssociation_Threads = new ArrayList<>();
            for (List<String> metricKeys_Partition : metricKeys_Partitions) {
                Thread metricKeyAssociation_Thread = new Thread(new metricKeyAssociation_Thread(metricKeys_Partition, allMetricGroupIds, allMetricSuspensionIds));
                metricKeyAssociation_Threads.add(metricKeyAssociation_Thread);
            }

            Threads.threadExecutorFixedPool(metricKeyAssociation_Threads, threads, 365, TimeUnit.DAYS);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(false);

        IsMetricAssociationRoutineCurrentlyRunning.set(false);
    }
    
    protected static void createMatchingMetricKeysDataStructuresForNewMetricGroupIds(List<Integer> metricGroupIds) {
        
        if (metricGroupIds == null) {
            return;
        }
        
        for (Integer metricGroupId : metricGroupIds) {
            if (metricGroupId == null) continue;
            Set<String> matchingMetricKeyAssociations = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);

            if (matchingMetricKeyAssociations == null) {
                matchingMetricKeyAssociations = new HashSet<>();
                GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.put(metricGroupId, Collections.synchronizedSet(matchingMetricKeyAssociations));
            }
        }
    }
    
    protected static void createMatchingMetricKeysDataStructuresForNewSuspensionIds(List<Integer> suspensionIds) {
        
        if (suspensionIds == null) {
            return;
        }
        
        for (Integer suspensionId : suspensionIds) {
            if (suspensionId == null) continue;
            Set<String> matchingMetricKeyAssociations = GlobalVariables.matchingMetricKeysAssociatedWithSuspension.get(suspensionId);

            if (matchingMetricKeyAssociations == null) {
                matchingMetricKeyAssociations = new HashSet<>();
                GlobalVariables.matchingMetricKeysAssociatedWithSuspension.put(suspensionId, Collections.synchronizedSet(matchingMetricKeyAssociations));
            }
        }
    }
    
    // associates a set of metric-keys with the metric output blacklist. this is intended to be called periodically, and only 1 call to this method can run at a time
    // returns null on error, otherwise returns the count of newly processed metrics (which had regex matching ran against them)
    public static Long associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(String threadId, ThreadPoolExecutor threadPoolExecutor) {

        try {
            // stops multiple output blacklist metric association routines from running simultaneously 
            if (!IsMetricAssociationRoutineForOutputBlacklistCurrentlyRunning.compareAndSet(false, true)) {
                if ((threadPoolExecutor != null) && (threadPoolExecutor.getActiveCount() <= 1)) {
                    logger.warn("Invalid state detected (detected that statsagg thinks another thread is running the output blacklist metric association routine, but it is not.");
                    IsMetricAssociationRoutineForOutputBlacklistCurrentlyRunning.set(false);
                }
                else {
                    logger.warn("ThreadId=" + threadId + ", Routine=MetricAssociation_OutputBlacklist, Message=\"Only 1 output blacklist metric association routine can run at a time\"");
                    return null;
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        long numNewKeysProcessed = 0;
        ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithOutputBlacklistMetricGroup_Local = new ConcurrentHashMap<>(); 
        ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup_Local = new ConcurrentHashMap<>();  
        
        //  wait until the the cleanup thread is done running
        if (CleanupThread.isCleanupThreadCurrentlyRunning.get()) Threads.sleepMilliseconds(50, false);
        
        // set a flag to indicate that the output blacklist metric association routine is running -- prevents other threads from running at the same time
        IsMetricAssociationRoutineForOutputBlacklistCurrentlyRunning_CurrentlyAssociating.set(true);

        boolean isMetricGroupChangeDetected = IsMetricGroupChangeOutputBlacklist.getAndSet(false);
        if (!isMetricGroupChangeDetected) {
            metricKeysAssociatedWithOutputBlacklistMetricGroup_Local = GlobalVariables.metricKeysAssociatedWithOutputBlacklistMetricGroup;
            matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup_Local = GlobalVariables.matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup;
        }
            
        // identify & store metrics that are on the output blacklist metric group
        OutputBlacklist outputBlacklist = OutputBlacklistDao.getOutputBlacklist_SingleRow(DatabaseConnections.getConnection(), true);
        
        // if a output blacklist metric group exists, associate metrics-keys with it
        if ((outputBlacklist != null) && (outputBlacklist.getMetricGroupId() != null)) {
            
            // update the output blacklist regex
            List<Integer> outputBlacklistMetricGroupId_List = new ArrayList<>();
            outputBlacklistMetricGroupId_List.add(outputBlacklist.getMetricGroupId());
            updateMergedRegexesForMetricGroups(outputBlacklistMetricGroupId_List);
                
            // associate metrics-keys with the output blacklist
            for (String metricKey : GlobalVariables.metricKeysLastSeenTimestamp.keySet()) {
                ConcurrentHashMap<String,String> immediateCleanupMetrics = GlobalVariables.immediateCleanupMetrics;
                if ((immediateCleanupMetrics != null) && !immediateCleanupMetrics.isEmpty() && immediateCleanupMetrics.containsKey(metricKey)) continue;

                if (!metricKeysAssociatedWithOutputBlacklistMetricGroup_Local.containsKey(metricKey)) {
                    Boolean didMatch = associateMetricKeyWithId(metricKey, outputBlacklist.getMetricGroupId(), 
                            matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup_Local, 
                            GlobalVariables.mergedMatchRegexesByMetricGroupId, 
                            GlobalVariables.mergedBlacklistRegexesByMetricGroupId);

                    if (didMatch != null) {
                        GlobalVariables.metricKeysAssociatedWithOutputBlacklistMetricGroup.put(metricKey, didMatch);
                        numNewKeysProcessed++;
                    }
                }
            }
        }
        
        GlobalVariables.metricKeysAssociatedWithOutputBlacklistMetricGroup = metricKeysAssociatedWithOutputBlacklistMetricGroup_Local;
        GlobalVariables.matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup = matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup_Local;
        
        IsMetricAssociationRoutineForOutputBlacklistCurrentlyRunning_CurrentlyAssociating.set(false);

        IsMetricAssociationRoutineForOutputBlacklistCurrentlyRunning.set(false);
        
        return numNewKeysProcessed;
    }
    
    // associates a list of metric-keys with the metric output blacklist. this is intended to be called by the various metric aggregation routines
    // returns the number of metric keys that were newly processed
    public static Long associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(String threadId, List<String> metricKeys) {
        
        if (metricKeys == null) {
            return new Long(0);
        }

        boolean restartRoutineRequred = true;
        long numNewKeysProcessed = 0;
        
        while (restartRoutineRequred) {
            // don't run this loop a second time unless this variable is changed below
            restartRoutineRequred = false;
            
            String matchRegex_BeforeMatching = null, matchRegex_AfterMatching = null, blacklistRegex_BeforeMatching = null, blacklistRegex_AfterMatching = null;
            MetricAssociationPattern metricAssociationPattern_Match_BeforeMatching = null, metricAssociationPattern_Blacklist_BeforeMatching = null;
            MetricAssociationPattern metricAssociationPattern_Match_AfterMatching = null, metricAssociationPattern_Blacklist_AfterMatching = null;
            
            //  get the metric-group id of the output blacklist (if one exists)
            OutputBlacklist outputBlacklist_BeforeMatching = OutputBlacklistDao.getOutputBlacklist_SingleRow(DatabaseConnections.getConnection(), true);
            Integer outputBlacklist_BeforeMatching_MetricGroupId = null;
            if ((outputBlacklist_BeforeMatching != null) && (outputBlacklist_BeforeMatching.getMetricGroupId() != null)) outputBlacklist_BeforeMatching_MetricGroupId = outputBlacklist_BeforeMatching.getMetricGroupId();
            
            // if a output blacklist metric group exists, associate metrics-keys with it
            if (outputBlacklist_BeforeMatching_MetricGroupId != null) {
                // update the output blacklist regex
                List<Integer> outputBlacklistMetricGroupId_List = new ArrayList<>();
                outputBlacklistMetricGroupId_List.add(outputBlacklist_BeforeMatching_MetricGroupId);
                updateMergedRegexesForMetricGroups(outputBlacklistMetricGroupId_List);

                // used in checking if the output-blacklist was altered while this routine was running
                matchRegex_BeforeMatching = GlobalVariables.mergedMatchRegexesByMetricGroupId.get(outputBlacklist_BeforeMatching_MetricGroupId);
                if (matchRegex_BeforeMatching != null) metricAssociationPattern_Match_BeforeMatching = regexPatterns_.get(matchRegex_BeforeMatching);
                blacklistRegex_BeforeMatching = GlobalVariables.mergedBlacklistRegexesByMetricGroupId.get(outputBlacklist_BeforeMatching_MetricGroupId);
                if (blacklistRegex_BeforeMatching != null) metricAssociationPattern_Blacklist_BeforeMatching = regexPatterns_.get(blacklistRegex_BeforeMatching);

                // associate metrics-keys with the output blacklist
                for (String metricKey : metricKeys) {
                    ConcurrentHashMap<String,String> immediateCleanupMetrics = GlobalVariables.immediateCleanupMetrics;
                    if ((immediateCleanupMetrics != null) && !immediateCleanupMetrics.isEmpty() && immediateCleanupMetrics.containsKey(metricKey)) continue;

                    if (!GlobalVariables.metricKeysAssociatedWithOutputBlacklistMetricGroup.containsKey(metricKey)) {
                        Boolean didMatch = associateMetricKeyWithId(metricKey, outputBlacklist_BeforeMatching_MetricGroupId, 
                                GlobalVariables.matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup, 
                                GlobalVariables.mergedMatchRegexesByMetricGroupId, 
                                GlobalVariables.mergedBlacklistRegexesByMetricGroupId);

                        if (didMatch != null) {
                            GlobalVariables.metricKeysAssociatedWithOutputBlacklistMetricGroup.put(metricKey, didMatch);
                            numNewKeysProcessed++;
                        }
                    }
                }
                
                // used in checking if the output-blacklist was altered while this routine was running
                OutputBlacklist outputBlacklist_AfterMatching = OutputBlacklistDao.getOutputBlacklist_SingleRow(DatabaseConnections.getConnection(), true);
                Integer outputBlacklist_AfterMatching_MetricGroupId = null;
                if ((outputBlacklist_AfterMatching != null) && outputBlacklist_AfterMatching.getMetricGroupId() != null) outputBlacklist_AfterMatching_MetricGroupId = outputBlacklist_BeforeMatching_MetricGroupId;
                if (outputBlacklist_AfterMatching_MetricGroupId != null) matchRegex_AfterMatching = GlobalVariables.mergedMatchRegexesByMetricGroupId.get(outputBlacklist_AfterMatching_MetricGroupId);
                if (matchRegex_AfterMatching != null) metricAssociationPattern_Match_AfterMatching = regexPatterns_.get(matchRegex_AfterMatching);
                if (outputBlacklist_AfterMatching_MetricGroupId != null) blacklistRegex_AfterMatching = GlobalVariables.mergedBlacklistRegexesByMetricGroupId.get(outputBlacklist_AfterMatching_MetricGroupId);
                if (blacklistRegex_AfterMatching != null) metricAssociationPattern_Blacklist_AfterMatching = regexPatterns_.get(blacklistRegex_AfterMatching);
                
                // check if the output-blacklist was altered while this routine was running. if it was, then re-run this routine
                if (((metricAssociationPattern_Match_BeforeMatching != null) && (metricAssociationPattern_Blacklist_BeforeMatching != null) &&
                        (metricAssociationPattern_Match_AfterMatching != null) && (metricAssociationPattern_Blacklist_AfterMatching != null)) && 
                        ((outputBlacklist_BeforeMatching_MetricGroupId != null) && (outputBlacklist_AfterMatching_MetricGroupId != null) && 
                        (outputBlacklist_BeforeMatching_MetricGroupId.intValue() == outputBlacklist_AfterMatching_MetricGroupId.intValue())) &&
                        ((metricAssociationPattern_Match_BeforeMatching.getTimestamp() != metricAssociationPattern_Match_AfterMatching.getTimestamp()) || 
                        (metricAssociationPattern_Blacklist_BeforeMatching.getTimestamp() != metricAssociationPattern_Blacklist_AfterMatching.getTimestamp()))) {
                    logger.info("ThreadId=" + threadId + ", Routine=MetricAssociation_OutputBlacklist_MetricSet, Message=\"Detected output-blacklist metric-group change. Restarting association routine.\"");
                    restartRoutineRequred = true;
                    numNewKeysProcessed = 0;
                }
            }
        }

        return numNewKeysProcessed;
    }
    
    private static class metricKeyAssociation_Thread implements Runnable {
		
        private final List<String> metricKeys__;
        private final List<Integer> allMetricGroupIds__;
        private final List<Integer> allMetricSuspensionIds__;
        
        public metricKeyAssociation_Thread(List<String> metricKeys, List<Integer> allMetricGroupIds, List<Integer> allMetricSuspensionIds) {
            this.metricKeys__ = metricKeys;
            this.allMetricGroupIds__ = allMetricGroupIds;
            this.allMetricSuspensionIds__ = allMetricSuspensionIds;
        }
        
        @Override
        public void run() {
            // run the association routine against all metric-groups/metric-suspensions/metric-keys. should only run the pattern matcher against previously unknown metric-keys.
            for (String metricKey : metricKeys__) {
                ConcurrentHashMap<String,String> immediateCleanupMetrics = GlobalVariables.immediateCleanupMetrics;
                if ((immediateCleanupMetrics != null) && !immediateCleanupMetrics.isEmpty() && immediateCleanupMetrics.containsKey(metricKey)) continue;

                associateMetricKeyWithIds(metricKey, allMetricGroupIds__, 
                        GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup, GlobalVariables.metricKeysAssociatedWithAnyMetricGroup, 
                        GlobalVariables.mergedMatchRegexesByMetricGroupId, GlobalVariables.mergedBlacklistRegexesByMetricGroupId);

                associateMetricKeyWithIds(metricKey, allMetricSuspensionIds__, 
                        GlobalVariables.matchingMetricKeysAssociatedWithSuspension, GlobalVariables.metricKeysAssociatedWithAnySuspension, 
                        GlobalVariables.mergedMatchRegexesBySuspensionId, GlobalVariables.mergedBlacklistRegexesBySuspensionId);
            }
        }
    }
    
    private static List<Integer> getMetricGroupIds_And_AssociateMetricKeysWithNewOrAlteredMetricGroups() {
        
        List<String> metricsToReassociateWithAlteredMetricGroups = new ArrayList<>();
        List<Integer> newAndAlteredMetricGroupIds = new ArrayList<>();
        List<Integer> allMetricGroupIds;

        synchronized(GlobalVariables.metricGroupChanges) {
            OutputBlacklist outputBlacklist = OutputBlacklistDao.getOutputBlacklist_SingleRow(DatabaseConnections.getConnection(), true);
            if ((outputBlacklist != null) && (outputBlacklist.getMetricGroupId() != null) && GlobalVariables.metricGroupChanges.containsKey(outputBlacklist.getMetricGroupId())) {
                IsMetricGroupChangeOutputBlacklist.set(true);
            }
            
            associateMetricKeysWithNewOrAlteredIds_DetectChanges(GlobalVariables.metricGroupChanges, 
                    GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup, GlobalVariables.metricKeysAssociatedWithAnyMetricGroup, 
                    GlobalVariables.mergedMatchRegexesByMetricGroupId, GlobalVariables.mergedBlacklistRegexesByMetricGroupId,
                    newAndAlteredMetricGroupIds, metricsToReassociateWithAlteredMetricGroups);
            
            updateMergedRegexesForMetricGroups(newAndAlteredMetricGroupIds);
            
            allMetricGroupIds = MetricGroupsDao.getAllMetricGroupIds(DatabaseConnections.getConnection(), true);
            if (allMetricGroupIds == null) {
                logger.error("Failure reading metric group ids from the database.");
                allMetricGroupIds = new ArrayList<>();
            }
        }
        
        associateMetricKeysWithNewOrAlteredIds_AssociateMetrics(GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup, GlobalVariables.metricKeysAssociatedWithAnyMetricGroup,
                GlobalVariables.mergedMatchRegexesByMetricGroupId, GlobalVariables.mergedBlacklistRegexesByMetricGroupId,
                newAndAlteredMetricGroupIds, metricsToReassociateWithAlteredMetricGroups);
            
        return allMetricGroupIds;
    }

    private static List<Integer> getMetricSuspensionIds_And_AssociateMetricKeysWithNewOrAlteredSuspensions() {
        
        List<String> metricsToReassociateWithAlteredSuspensions = new ArrayList<>();
        List<Integer> newAndAlteredSuspensionIds = new ArrayList<>();
        List<Integer> allMetricSuspensionIds;

        synchronized(GlobalVariables.suspensionChanges) {
            associateMetricKeysWithNewOrAlteredIds_DetectChanges(GlobalVariables.suspensionChanges, 
                    GlobalVariables.matchingMetricKeysAssociatedWithSuspension, GlobalVariables.metricKeysAssociatedWithAnySuspension, 
                    GlobalVariables.mergedMatchRegexesBySuspensionId, GlobalVariables.mergedBlacklistRegexesBySuspensionId,
                    newAndAlteredSuspensionIds, metricsToReassociateWithAlteredSuspensions);

            updateMergedRegexesForSuspensions(newAndAlteredSuspensionIds);

            allMetricSuspensionIds = SuspensionsDao.getSuspensionIds_BySuspendBy(DatabaseConnections.getConnection(), true, Suspension.SUSPEND_BY_METRICS);
            if (allMetricSuspensionIds == null) {
                logger.error("Failure reading metric suspension ids from the database.");
                allMetricSuspensionIds = new ArrayList<>();
            }
        }
        
        associateMetricKeysWithNewOrAlteredIds_AssociateMetrics(GlobalVariables.matchingMetricKeysAssociatedWithSuspension, GlobalVariables.metricKeysAssociatedWithAnySuspension,
                GlobalVariables.mergedMatchRegexesBySuspensionId, GlobalVariables.mergedBlacklistRegexesBySuspensionId,
                newAndAlteredSuspensionIds, metricsToReassociateWithAlteredSuspensions);
            
        return allMetricSuspensionIds;
    }
    
    // update global variables for the case of a suspension or metric-group being newly added, altered, or removed
    // 'id' refers to either metric-group id or suspension id
    // a list of ids that are new or have been altered is written to 'newAndAlteredIds'
    // a set of metrics to reassociate with ids that are new or altered is written to 'metricsToReassociateWithAlteredIds'
    private static void associateMetricKeysWithNewOrAlteredIds_DetectChanges(
            ConcurrentHashMap<Integer,Byte> changesById,
            ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithId, ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithAnyId,
            ConcurrentHashMap<Integer,String> mergedMatchRegexesById, ConcurrentHashMap<Integer,String> mergedBlacklistRegexesById,
            List<Integer> newAndAlteredIds, List<String> metricsToReassociateWithAlteredIds) {
        
        Set<Integer> changeIds_Local = new HashSet<>(changesById.keySet());

        for (Integer id : changeIds_Local) {
            Byte changeCode = changesById.get(id);

            if ((changeCode != null) && changeCode.equals(GlobalVariables.NEW)) {
                newAndAlteredIds.add(id);
            }
            else if ((changeCode != null) && changeCode.equals(GlobalVariables.ALTER)) {
                Set<String> metricKeysWhereThisIdIsTheOnlyIdAssociated = getMetricKeysWhereThisIdIsTheOnlyIdAssociated(id, matchingMetricKeysAssociatedWithId);

                for (String metricKey : metricKeysWhereThisIdIsTheOnlyIdAssociated) {
                    metricKeysAssociatedWithAnyId.remove(metricKey);
                    metricsToReassociateWithAlteredIds.add(metricKey);
                }

                matchingMetricKeysAssociatedWithId.remove(id);
                mergedMatchRegexesById.remove(id);
                mergedBlacklistRegexesById.remove(id);
                newAndAlteredIds.add(id);
            }
            else if ((changeCode != null) && changeCode.equals(GlobalVariables.REMOVE)) {
                Set<String> metricKeysWhereThisIdIsTheOnlyIdAssociated = getMetricKeysWhereThisIdIsTheOnlyIdAssociated(id, matchingMetricKeysAssociatedWithId);
                for (String metricKey : metricKeysWhereThisIdIsTheOnlyIdAssociated) metricKeysAssociatedWithAnyId.put(metricKey, false);

                matchingMetricKeysAssociatedWithId.remove(id);
                mergedMatchRegexesById.remove(id);
                mergedBlacklistRegexesById.remove(id);
            }

            changesById.remove(id);
        }
        
    }
    
    private static void associateMetricKeysWithNewOrAlteredIds_AssociateMetrics(
            ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithId, ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithAnyId,
            ConcurrentHashMap<Integer,String> mergedMatchRegexesById, ConcurrentHashMap<Integer,String> mergedBlacklistRegexesById,
            List<Integer> newAndAlteredIds, List<String> metricsToReassociateWithAlteredIds) {
        
        // only run the association routine against metrics that have already been through the association routine before
        for (Integer id : newAndAlteredIds) {
            for (String metricKey : metricKeysAssociatedWithAnyId.keySet()) {
                associateMetricKeyWithId(metricKey, id, matchingMetricKeysAssociatedWithId, mergedMatchRegexesById, mergedBlacklistRegexesById);
            }
            
            for (String metricKey : metricsToReassociateWithAlteredIds) {
                associateMetricKeyWithId(metricKey, id, matchingMetricKeysAssociatedWithId, mergedMatchRegexesById, mergedBlacklistRegexesById);
            }

            Set<String> matchingMetricKeyAssociatedWithId = matchingMetricKeysAssociatedWithId.get(id);
            if (matchingMetricKeyAssociatedWithId == null) continue;

            synchronized (matchingMetricKeyAssociatedWithId) {
                for (String metricKey : matchingMetricKeyAssociatedWithId) {
                    metricKeysAssociatedWithAnyId.put(metricKey, true);
                }
            }
        }
        
    }
    
    /*
     For a specific id (suspension id or metric-group id), return the set of metric-keys where the metrics-keys are ONLY associated with this particular id.
    */
    private static Set<String> getMetricKeysWhereThisIdIsTheOnlyIdAssociated(Integer id, ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithId) {
                
        try {
            Set<String> metricKeysWhereThisIdIsTheOnlyIdAssociated;
            Set<String> matchingMetricKeys = matchingMetricKeysAssociatedWithId.get(id);

            if (matchingMetricKeys != null) {
                synchronized(matchingMetricKeys) {
                    metricKeysWhereThisIdIsTheOnlyIdAssociated = new HashSet<>(matchingMetricKeys);
                }
            }
            else metricKeysWhereThisIdIsTheOnlyIdAssociated = new HashSet<>();

            if (!metricKeysWhereThisIdIsTheOnlyIdAssociated.isEmpty()) {
                for (Integer currentId : matchingMetricKeysAssociatedWithId.keySet()) {
                    if (id.equals(currentId)) continue;

                    Set<String> currentMatchingMetricKeysAssociatedWithId = matchingMetricKeysAssociatedWithId.get(currentId);
                    if (currentMatchingMetricKeysAssociatedWithId == null) continue;

                    synchronized(currentMatchingMetricKeysAssociatedWithId) {
                        for (String metricKey : currentMatchingMetricKeysAssociatedWithId) {
                            metricKeysWhereThisIdIsTheOnlyIdAssociated.remove(metricKey);
                        }
                    }
                }
            }

            return metricKeysWhereThisIdIsTheOnlyIdAssociated;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new HashSet<>();
        }
    }

    /*
     For a specific id (suspension id or metric-group id), determine if this metric key is associated with it. 
     If the association is true, then the association is cached in "matchingMetricKeysAssociatedWithId".
     Returns null on error, false if the metric-key did not match the regex, true if the metric-key did match the regex
    */
    private static Boolean associateMetricKeyWithId(String metricKey, Integer id, 
            ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithId,
            ConcurrentHashMap<Integer,String> mergedMatchRegexesById,
            ConcurrentHashMap<Integer,String> mergedBlacklistRegexesById) {

        ConcurrentHashMap<String,String> immediateCleanupMetrics = GlobalVariables.immediateCleanupMetrics;
        if ((immediateCleanupMetrics != null) && !immediateCleanupMetrics.isEmpty() && immediateCleanupMetrics.containsKey(metricKey)) return null;
        
        boolean didMatch = false;
        
        try {
            String matchRegex = mergedMatchRegexesById.get(id);
            if (matchRegex == null) return null;

            Pattern matchPattern = getPatternFromRegexString(matchRegex);

            if (matchPattern != null) {
                Matcher matchMatcher = matchPattern.matcher(metricKey);
                boolean isMatchRegexMatch = matchMatcher.matches();
                boolean isMetricKeyAssociatedWithId = false;

                if (isMatchRegexMatch) {
                    String blacklistRegex = mergedBlacklistRegexesById.get(id);
                    Pattern blacklistPattern = getPatternFromRegexString(blacklistRegex);

                    if (blacklistPattern != null) {
                        Matcher blacklistMatcher = blacklistPattern.matcher(metricKey);
                        boolean isBlacklistRegexMatch = blacklistMatcher.matches();
                        if (!isBlacklistRegexMatch) isMetricKeyAssociatedWithId = true;
                    }
                    else isMetricKeyAssociatedWithId = true;
                }

                if (isMetricKeyAssociatedWithId) {
                    Set<String> matchingMetricKeyAssociations = matchingMetricKeysAssociatedWithId.get(id);

                    if (matchingMetricKeyAssociations == null) {
                        matchingMetricKeyAssociations = new HashSet<>();
                        matchingMetricKeysAssociatedWithId.put(id, Collections.synchronizedSet(matchingMetricKeyAssociations));
                    }

                    matchingMetricKeyAssociations.add(metricKey);
                }
                
                didMatch = isMetricKeyAssociatedWithId;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return didMatch;
    }
    
    /*
     This method performs two tasks. 
     Task 1: Determines if a metric key is associated with ANY id (where id is either a suspension id or a metric-group id). 
             The boolean value of this determination is returned & is stored in 'metricKeysAssociatedWithAnyId'.
     Task 2: For every id (where id is either a suspension id or a metric-group id), determine if this metric key is associated with it. 
             If the association is true, then the association is cached in 'matchingMetricKeysAssociatedWithId'.
     */
    private static void associateMetricKeyWithIds(String metricKey, List<Integer> ids,
            ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithId,
            ConcurrentHashMap<String,Boolean> metricKeysAssociatedWithAnyId,
            ConcurrentHashMap<Integer,String> mergedMatchRegexesById,
            ConcurrentHashMap<Integer,String> mergedBlacklistRegexesById) {
        
        Boolean isMetricKeyAssociatedWithAnyId = metricKeysAssociatedWithAnyId.get(metricKey);
        if (isMetricKeyAssociatedWithAnyId != null) return;
        isMetricKeyAssociatedWithAnyId = false;

        for (Integer id : ids) {
            try {
                String matchRegex = mergedMatchRegexesById.get(id);
                if (matchRegex == null) continue;

                Pattern matchPattern = getPatternFromRegexString(matchRegex);

                if (matchPattern != null) {
                    Matcher matchMatcher = matchPattern.matcher(metricKey);
                    boolean isMatchRegexMatch = matchMatcher.matches();
                    boolean isMetricKeyAssociatedWithId = false;

                    if (isMatchRegexMatch) {
                        String blacklistRegex = mergedBlacklistRegexesById.get(id);
                        Pattern blacklistPattern = getPatternFromRegexString(blacklistRegex);
                        
                        if (blacklistPattern != null) {
                            Matcher blacklistMatcher = blacklistPattern.matcher(metricKey);
                            boolean isBlacklistRegexMatch = blacklistMatcher.matches();
                            if (!isBlacklistRegexMatch) isMetricKeyAssociatedWithId = true;
                        }
                        else isMetricKeyAssociatedWithId = true;
                    }
                    
                    if (isMetricKeyAssociatedWithId) {
                        Set<String> matchingMetricKeyAssociations = matchingMetricKeysAssociatedWithId.get(id);
                        
                        if (matchingMetricKeyAssociations != null) {
                            matchingMetricKeyAssociations.add(metricKey);
                            isMetricKeyAssociatedWithAnyId = true;
                        }
                        else {
                            logger.error("This shouldn't be possible. MG or Suspension does not have a matchingMetricKeyAssociations datastructure. ID=" + id);
                        }
                    }
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        metricKeysAssociatedWithAnyId.put(metricKey, isMetricKeyAssociatedWithAnyId);
    }
    
    public static Pattern getPatternFromRegexString(String regex) {

        if (regex == null) {
            return null;
        }

        Pattern pattern = null; 
        MetricAssociationPattern metricAssociationPattern = regexPatterns_.get(regex);
        if (metricAssociationPattern != null) pattern = metricAssociationPattern.getPattern();
        
        if (pattern == null) {
            boolean isRegexBad = regexBlacklist_.containsKey(regex);
            if (isRegexBad) return null;
            
            try {
                pattern = Pattern.compile(regex);
                MetricAssociationPattern newMetricAssociationPattern = new MetricAssociationPattern(pattern, System.currentTimeMillis());
                regexPatterns_.put(regex, newMetricAssociationPattern);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                regexBlacklist_.put(regex, regex);
            }
        }

        return pattern;
    }
    
    // update GlobalVariables.mergedMatchRegexesByMetricGroupId & GlobalVariables.mergedBlacklistRegexesByMetricGroupId with the latest merged regexes
    // also update regexPatterns_ & regexBlacklist_ with the latest patterns
    public static void updateMergedRegexesForMetricGroups(List<Integer> metricGroupIds) {

        if (metricGroupIds == null) {
            return;
        }
        
        for (Integer metricGroupId : metricGroupIds) {
            String mergedMatchRegex = GlobalVariables.mergedMatchRegexesByMetricGroupId.get(metricGroupId);
            String mergedBlacklistRegex = GlobalVariables.mergedBlacklistRegexesByMetricGroupId.get(metricGroupId);
            List<MetricGroupRegex> metricGroupRegexes = null;
            
            if ((mergedMatchRegex == null) || (mergedBlacklistRegex == null)) {
                metricGroupRegexes = MetricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(DatabaseConnections.getConnection(), true, metricGroupId);
            }
                
            if (mergedMatchRegex == null) {
                mergedMatchRegex = createMergedMetricGroupRegex(metricGroupRegexes, REGEX_TYPE_MATCH);
                if (mergedMatchRegex != null) {
                    GlobalVariables.mergedMatchRegexesByMetricGroupId.put(metricGroupId, mergedMatchRegex);
                    getPatternFromRegexString(mergedMatchRegex);
                }
            }
            
            if (mergedBlacklistRegex == null) {
                mergedBlacklistRegex = createMergedMetricGroupRegex(metricGroupRegexes, REGEX_TYPE_BLACKLIST);
                if (mergedBlacklistRegex != null) {
                    GlobalVariables.mergedBlacklistRegexesByMetricGroupId.put(metricGroupId, mergedBlacklistRegex);
                    getPatternFromRegexString(mergedMatchRegex);
                }
            }
        }
        
    }
    
    /* 
     This method merges every regex associated with a single metric group into a single regex (using '|' as the glue between regexes).
     */
    private static String createMergedMetricGroupRegex(List<MetricGroupRegex> metricGroupRegexes, byte regexType) {

        if (metricGroupRegexes == null) {
            return null;
        }

        List<String> regexPatterns = new ArrayList<>();

        for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
            if ((metricGroupRegex.isBlacklistRegex() != null) && (metricGroupRegex.getPattern() != null)) {
                if (metricGroupRegex.isBlacklistRegex() && (regexType == REGEX_TYPE_BLACKLIST)) regexPatterns.add(metricGroupRegex.getPattern());
                else if (!metricGroupRegex.isBlacklistRegex() && (regexType == REGEX_TYPE_MATCH)) regexPatterns.add(metricGroupRegex.getPattern());
            }
        }

        String mergedRegex = StringUtilities.createMergedRegex(regexPatterns);
        return mergedRegex;
    }
    
    public static void updateMergedRegexesForSuspensions(List<Integer> suspensionIds) {

        if (suspensionIds == null) {
            return;
        }
        
        for (Integer suspensionId : suspensionIds) {
            if (suspensionId == null) continue;
            
            try {
                String mergedMatchRegex = GlobalVariables.mergedMatchRegexesBySuspensionId.get(suspensionId);

                if (mergedMatchRegex == null) {
                    Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, suspensionId);
                    if (suspension == null) continue;
                    
                    List<String> regexPatterns = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricSuspensionRegexes(), '\n');
                    mergedMatchRegex = StringUtilities.createMergedRegex(regexPatterns);

                    if (mergedMatchRegex != null) GlobalVariables.mergedMatchRegexesBySuspensionId.put(suspensionId, mergedMatchRegex);
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
    }
    
    // todo: improve performance of this method
    public static List<String> getMetricKeysAssociatedWithAlert(Alert alert, Set<String> suspendedMetricKeys) {

        if (alert == null) {
            return new ArrayList<>();
        }

        Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(alert.getMetricGroupId());
        Set<String> metricKeysAssociatedWithAlert;
        
        if (matchingMetricKeysAssociatedWithMetricGroup != null) {
            
            synchronized(matchingMetricKeysAssociatedWithMetricGroup) {
                metricKeysAssociatedWithAlert = new HashSet<>(matchingMetricKeysAssociatedWithMetricGroup);
            }
            
            if (metricKeysAssociatedWithAlert.size() >= suspendedMetricKeys.size()) {
                metricKeysAssociatedWithAlert.removeAll(suspendedMetricKeys);
            }
            else {
                Set<String> metricKeysAssociatedWithAlert_Temp = new HashSet<>();
                
                for (String metricKeyAssociatedWithAlert : metricKeysAssociatedWithAlert) {
                    if (!suspendedMetricKeys.contains(metricKeyAssociatedWithAlert)) metricKeysAssociatedWithAlert_Temp.add(metricKeyAssociatedWithAlert);
                }
                
                metricKeysAssociatedWithAlert = metricKeysAssociatedWithAlert_Temp;
            }
        }
        else {
            metricKeysAssociatedWithAlert = new HashSet<>();
        }

        return new ArrayList<>(metricKeysAssociatedWithAlert);
    }

    // if metricMatchLimit < 0, then it is treated as infinite
    public static Set<String> getRegexMatches(Set<String> metricKeys, String matchRegex, String blacklistRegex, int metricMatchLimit) {
        
        if ((metricKeys == null) || (matchRegex == null)) {
            return null;
        }
        
        Pattern matchPattern = null, blacklistPattern = null;
        
        try {
            matchPattern = Pattern.compile(matchRegex.trim());
            if ((blacklistRegex != null) && !blacklistRegex.isEmpty()) blacklistPattern = Pattern.compile(blacklistRegex.trim());
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        Set<String> matchingMetricKeys = new HashSet<>();
        
        try {
            if (matchPattern != null) {
                int matchCounter = 0;
                boolean isAnyMatchLimt = (metricMatchLimit >= 0);

                for (String metricKey : metricKeys) {
                    Matcher matcher = matchPattern.matcher(metricKey);

                    if (matcher.matches()) {
                        if (blacklistPattern != null) {
                            Matcher blacklistMatcher = blacklistPattern.matcher(metricKey);

                            if (!blacklistMatcher.matches()) {
                                matchingMetricKeys.add(metricKey);
                                matchCounter++;
                            }
                        }
                        else {
                            matchingMetricKeys.add(metricKey);
                            matchCounter++;
                        }
                    }

                    if (isAnyMatchLimt && (matchCounter == metricMatchLimit)) {
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return matchingMetricKeys;
    }
    
}
