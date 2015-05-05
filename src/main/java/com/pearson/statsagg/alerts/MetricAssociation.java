package com.pearson.statsagg.alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import com.pearson.statsagg.utilities.Threads;
import com.pearson.statsagg.webui.MetricGroupsLogic;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAssociation {

    private static final Logger logger = LoggerFactory.getLogger(MetricAssociation.class.getName());

    public static final AtomicBoolean IsMetricAssociationRoutineCurrentlyRunning = new AtomicBoolean(false);
    public static final AtomicBoolean IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating = new AtomicBoolean(false);
    
    protected static void associateMetricKeysWithMetricGroups(String threadId) {
        
        // stops multiple metric association methods from running simultaneously 
        if (!IsMetricAssociationRoutineCurrentlyRunning.compareAndSet(false, true)) {
            logger.warn("ThreadId=" + threadId + ", Routine=MetricAssociation, Message=\"Only 1 metric association routine can run at a time\"");
            return;
        }
        
        //  wait until the the cleanup thread is done running
        if (CleanupThread.isCleanupThreadCurrentlyRunning.get()) Threads.sleepMilliseconds(50, false);
        
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(true);
        List<Integer> metricGroupIds = applyMetricGroupGlobalVariableChanges();
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(false);
        
        updateMergedRegexsForMetricGroups(metricGroupIds);
        
        // run the association routine against all metric-groups/metric-keys. should only run the pattern matcher against previously unknown metric-keys.
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(true);
        Set<String> metricKeys = GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.keySet();
        for (String metricKey : metricKeys) associateMetricKeyWithMetricGroups(metricKey, metricGroupIds);
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(false);

        IsMetricAssociationRoutineCurrentlyRunning.set(false);
    }

    // update global variables for the case of a metric group being newly added, altered, or removed
    // returns the current/active set of metric-group ids
    private static List<Integer> applyMetricGroupGlobalVariableChanges() {
        
        List<Integer> allMetricGroupIds;
        List<Integer> newAndAlteredMetricGroupIds = new ArrayList<>();
        List<String> metricsToReassociateWithAlteredMetricGroups = new ArrayList<>();
        
        synchronized(GlobalVariables.metricGroupChanges) {
            Set<Integer> metricGroupIdsLocal_MetricGroupsChanging = new HashSet<>(GlobalVariables.metricGroupChanges.keySet());

            for (Integer metricGroupId : metricGroupIdsLocal_MetricGroupsChanging) {
                Byte metricGroupChangeCode = GlobalVariables.metricGroupChanges.get(metricGroupId);

                if ((metricGroupChangeCode != null) && metricGroupChangeCode.equals(MetricGroupsLogic.NEW)) {
                    newAndAlteredMetricGroupIds.add(metricGroupId);
                }
                else if ((metricGroupChangeCode != null) && metricGroupChangeCode.equals(MetricGroupsLogic.ALTER)) {
                    Set<String> metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated = getMetricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated(metricGroupId);

                    for (String metricKey : metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated) {
                        GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.remove(metricKey);
                        metricsToReassociateWithAlteredMetricGroups.add(metricKey);
                    }

                    GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.remove(metricGroupId);
                    GlobalVariables.mergedRegexsForMetricGroups.remove(metricGroupId);
                    newAndAlteredMetricGroupIds.add(metricGroupId);
                }
                else if ((metricGroupChangeCode != null) && metricGroupChangeCode.equals(MetricGroupsLogic.REMOVE)) {
                    Set<String> metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated = getMetricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated(metricGroupId);
                    for (String metricKey : metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated) GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.put(metricKey, false);
                    
                    GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.remove(metricGroupId);
                    GlobalVariables.mergedRegexsForMetricGroups.remove(metricGroupId);
                }
                
                GlobalVariables.metricGroupChanges.remove(metricGroupId);
            }
            
            updateMergedRegexsForMetricGroups(newAndAlteredMetricGroupIds);
            
            MetricGroupsDao metricGrouspDao = new MetricGroupsDao();
            allMetricGroupIds = metricGrouspDao.getAllMetricGroupIds();
            if (allMetricGroupIds == null) {
                logger.error("Failure reading metric group ids from the database. Aborting metric association routine.");
                allMetricGroupIds = new ArrayList<>();
            }
        }
        
        // doing metric-group associations for new metric groups outside of the synchronized block to avoid holding the lock on GlobalVariables.metricGroupChanges for too long
        // only run the association routine against metrics that have already been through the association routine before
        for (Integer metricGroupId : newAndAlteredMetricGroupIds) {
            for (String metricKey : GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.keySet()) associateMetricKeyWithMetricGroups(metricKey, metricGroupId);
            for (String metricKey : metricsToReassociateWithAlteredMetricGroups) associateMetricKeyWithMetricGroups(metricKey, metricGroupId);
            
            Set<String> matchingMetricKeyAssociationWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);
            if (matchingMetricKeyAssociationWithMetricGroup == null) continue;
            
            synchronized(matchingMetricKeyAssociationWithMetricGroup) {
                for (String metricKey : matchingMetricKeyAssociationWithMetricGroup) {
                    GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.put(metricKey, true);
                }
            }
        }
        
        return allMetricGroupIds;
    }

    // update GlobalVariables.mergedRegexsForMetricGroups with the latest merged regexs
    private static void updateMergedRegexsForMetricGroups(List<Integer> metricGroupIds) {

        if (metricGroupIds == null) {
            return;
        }
        
        for (Integer metricGroupId : metricGroupIds) {
            String mergedMetricGroupRegex = GlobalVariables.mergedRegexsForMetricGroups.get(metricGroupId);

            if (mergedMetricGroupRegex == null) {
                mergedMetricGroupRegex = createMergedMetricGroupRegex(metricGroupId);
                GlobalVariables.mergedRegexsForMetricGroups.put(metricGroupId, mergedMetricGroupRegex);
                while (!GlobalVariables.mergedRegexsForMetricGroups.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
            }
        }
        
    }
    
    /* 
     This method merges every regex associated with a single metric group into a single regex (using '|' as the glue between regexs).
     */
    private static String createMergedMetricGroupRegex(Integer metricGroupId) {

        if (metricGroupId == null) {
            return null;
        }
                    
        MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
        List<String> regexs = metricGroupRegexsDao.getPatterns(metricGroupId);
        
        if (regexs != null) {
            String mergedRegex = StringUtilities.createMergedRegex(regexs);
            return mergedRegex;
        }
        else {
            return null;
        }
    }

    private static Pattern getPatternFromRegexString(String regex) {

        if (regex == null) {
            return null;
        }

        Pattern pattern = GlobalVariables.metricGroupRegexPatterns.get(regex);

        if (pattern == null) {
            boolean isRegexBad = GlobalVariables.metricGroupRegexBlacklist.containsKey(regex);
            if (isRegexBad) return null;
            
            try {
                pattern = Pattern.compile(regex);
                GlobalVariables.metricGroupRegexPatterns.put(regex, pattern);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                GlobalVariables.metricGroupRegexBlacklist.put(regex, regex);
            }
        }

        return pattern;
    }
    
    private static Set<String> getMetricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated(Integer metricGroupId) {
             
        if (metricGroupId == null) {
            return new HashSet<>();
        }
        
        Set<String> metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated;
                
        try {
            Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);

            if (matchingMetricKeysAssociatedWithMetricGroup != null) {
                synchronized(matchingMetricKeysAssociatedWithMetricGroup) {
                    metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated = new HashSet<>(matchingMetricKeysAssociatedWithMetricGroup);
                }
            }
            else metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated = new HashSet<>();

            if (!metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated.isEmpty()) {
                for (Integer currentMetricGroupId : GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.keySet()) {
                    if (metricGroupId.equals(currentMetricGroupId)) continue;

                    Set<String> currentMatchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(currentMetricGroupId);
                    if (currentMatchingMetricKeysAssociatedWithMetricGroup == null) continue;

                    synchronized(currentMatchingMetricKeysAssociatedWithMetricGroup) {
                        for (String metricKey : currentMatchingMetricKeysAssociatedWithMetricGroup) {
                            metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated.remove(metricKey);
                        }
                    }
                }
            }

            return metricKeysWhereThisMetricGroupIsTheOnlyMetricGroupAssociated;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new HashSet<>();
        }
    }
    
    /*
     For a specific metric group, determine if this metric key is associated with it. 
     If the association is true, then the association is cached in "GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup".
    */
    private static void associateMetricKeyWithMetricGroups(String metricKey, Integer metricGroupId) {

        if ((metricKey == null) || (metricGroupId == null)) {
            return;
        }
        
        if ((GlobalVariables.immediateCleanupMetrics != null) && !GlobalVariables.immediateCleanupMetrics.isEmpty() && GlobalVariables.immediateCleanupMetrics.containsKey(metricKey)) {
            return;
        }
        
        try {
            String regex = GlobalVariables.mergedRegexsForMetricGroups.get(metricGroupId);
            if (regex == null) return;

            Pattern pattern = getPatternFromRegexString(regex);

            if (pattern != null) {
                Matcher matcher = pattern.matcher(metricKey);
                boolean isMetricKeyAssociatedWithMetricGroup = matcher.matches();

                if (isMetricKeyAssociatedWithMetricGroup) {
                    Set<String> matchingMetricKeyAssociationWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);

                    if (matchingMetricKeyAssociationWithMetricGroup == null) {
                        matchingMetricKeyAssociationWithMetricGroup = new HashSet<>();
                        GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.put(metricGroupId, Collections.synchronizedSet(matchingMetricKeyAssociationWithMetricGroup));
                    }

                    matchingMetricKeyAssociationWithMetricGroup.add(metricKey);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    /*
     This method performs two tasks. 
     Task 1: Determines if a metric key is associated with ANY metric group. 
             The boolean value of this determination is returned & is stored in "GlobalVariables.metricKeysAssociatedWithAnyMetricGroup".
     Task 2: For every metric group, determine if this metric key is associated with it. 
             If the association is true, then the association is cached in "GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup".
     */
    private static void associateMetricKeyWithMetricGroups(String metricKey, List<Integer> metricGroupIds) {

        if ((metricKey == null) || (metricGroupIds == null)) {
            return;
        }

        if ((GlobalVariables.immediateCleanupMetrics != null) && !GlobalVariables.immediateCleanupMetrics.isEmpty() && GlobalVariables.immediateCleanupMetrics.containsKey(metricKey)) {
            return;
        }
        
        Boolean isMetricKeyAssociatedWithAnyMetricGroup = GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.get(metricKey);
        if (isMetricKeyAssociatedWithAnyMetricGroup != null) return;
        isMetricKeyAssociatedWithAnyMetricGroup = false;

        for (Integer metricGroupId : metricGroupIds) {
            try {
                String regex = GlobalVariables.mergedRegexsForMetricGroups.get(metricGroupId);
                if (regex == null) continue;

                Pattern pattern = getPatternFromRegexString(regex);

                if (pattern != null) {
                    Matcher matcher = pattern.matcher(metricKey);
                    boolean isMetricKeyAssociatedWithMetricGroup = matcher.matches();

                    if (isMetricKeyAssociatedWithMetricGroup) {
                        Set<String> matchingMetricKeyAssociationWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);

                        if (matchingMetricKeyAssociationWithMetricGroup == null) {
                            matchingMetricKeyAssociationWithMetricGroup = new HashSet<>();
                            GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.put(metricGroupId, Collections.synchronizedSet(matchingMetricKeyAssociationWithMetricGroup));
                        }

                        matchingMetricKeyAssociationWithMetricGroup.add(metricKey);
                        isMetricKeyAssociatedWithAnyMetricGroup = true;
                    }
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.put(metricKey, isMetricKeyAssociatedWithAnyMetricGroup);
    }

    protected static List<String> getMetricKeysAssociatedWithAlert(Alert alert) {

        if (alert == null) {
            return new ArrayList<>();
        }

        List<String> metricKeysAssociatedWithAlert;
        
        Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(alert.getMetricGroupId());

        if (matchingMetricKeysAssociatedWithMetricGroup != null) {
            synchronized(matchingMetricKeysAssociatedWithMetricGroup) {
                metricKeysAssociatedWithAlert = new ArrayList<>(matchingMetricKeysAssociatedWithMetricGroup);
            }
        }
        else {
            metricKeysAssociatedWithAlert = new ArrayList<>();
        }

        return metricKeysAssociatedWithAlert;
    }

}
