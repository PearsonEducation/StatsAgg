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
        
        applyMetricGroupGlobalVariableChanges();
  
        MetricGroupsDao metricGrouspDao = new MetricGroupsDao();
        List<Integer> metricGroupIds = metricGrouspDao.getAllMetricGroupIds();
        if (metricGroupIds == null) {
            logger.error("Failure reading metric group ids from the database. Aborting metric association routine.");
            return;
        }
        
        updateMergedRegexsForMetricGroups(metricGroupIds);
        
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(true);
        Set<String> metricKeys = GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.keySet();
        for (String metricKey : metricKeys) associateMetricKeyWithMetricGroups(metricKey, metricGroupIds);
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(false);

        IsMetricAssociationRoutineCurrentlyRunning.set(false);
    }

    private static void applyMetricGroupGlobalVariableChanges() {
        
        synchronized(GlobalVariables.metricGroupChanges) {
            Set<Integer> metricGroupIdsLocal = new HashSet<>(GlobalVariables.metricGroupChanges.keySet());

            for (Integer metricGroupId : metricGroupIdsLocal) {
                String alterOrRemove = GlobalVariables.metricGroupChanges.get(metricGroupId);

                // update global variables for the case of a metric group being newly added, changed, or removed
                if ((alterOrRemove != null) && (alterOrRemove.equalsIgnoreCase("Alter") || alterOrRemove.equalsIgnoreCase("Remove"))) {

                    for (String metricKey : GlobalVariables.metricGroupsAssociatedWithMetricKeys.keySet()) {
                        ArrayList[] associationLists = GlobalVariables.metricGroupsAssociatedWithMetricKeys.get(metricKey);
                        if ((associationLists != null) && associationLists.length == 2) {
                            associationLists[0].remove(metricGroupId);
                            associationLists[0].trimToSize();
                            associationLists[1].remove(metricGroupId);
                            associationLists[1].trimToSize();
                        }
                    }

                    GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.remove(metricGroupId);
                    GlobalVariables.mergedRegexsForMetricGroups.remove(metricGroupId);
                    GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.clear();
                    GlobalVariables.metricGroupChanges.remove(metricGroupId);

                    while (GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                    while (GlobalVariables.mergedRegexsForMetricGroups.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                    while (GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.size() > 0) Threads.sleepMilliseconds(10);
                    while (GlobalVariables.metricGroupChanges.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                }
            }
        }
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
    
    /*
     This method performs two tasks. 
     Task 1: Determines if a metric key is associated with ANY metric group. 
     The boolean value of this determination is returned & is stored in "GlobalVariables.metricKeysAssociatedWithAnyMetricGroup".
     Task 2: For every metric group, determine if this metric key is associated with it. 
     The boolean value of this determination is stored in the lists associated with "GlobalVariables.metricGroupsAssociatedWithMetricKeys".
     Also, if the association is true, then determination is stored in "GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup".
     */
    private static boolean associateMetricKeyWithMetricGroups(String metricKey, List<Integer> metricGroupIds) {

        if ((metricKey == null) || (metricGroupIds == null)) {
            return false;
        }

        synchronized(GlobalVariables.cleanupOldMetricsLock) {
            Boolean isMetricKeyAssociatedWithAnyMetricGroup = GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.get(metricKey);
            if (isMetricKeyAssociatedWithAnyMetricGroup != null) return isMetricKeyAssociatedWithAnyMetricGroup;
            isMetricKeyAssociatedWithAnyMetricGroup = false;

            // The 'array of ArrayLists' approach is used to save memory. HashMaps are too memory intensive for this purpose.
            // A little bit of CPU is being sacraficed to save a lot of memory.
            ArrayList[] metricGroupAssociatedWithMetricKeys = GlobalVariables.metricGroupsAssociatedWithMetricKeys.get(metricKey);
            if (metricGroupAssociatedWithMetricKeys == null) {
                ArrayList[] associationLists = new ArrayList[2];
                ArrayList negativeMatchList = new ArrayList<>();
                ArrayList positiveMatchList = new ArrayList<>();
                negativeMatchList.trimToSize();
                positiveMatchList.trimToSize();
                associationLists[0] = negativeMatchList;
                associationLists[1] = positiveMatchList;
                GlobalVariables.metricGroupsAssociatedWithMetricKeys.put(metricKey, associationLists);
                metricGroupAssociatedWithMetricKeys = GlobalVariables.metricGroupsAssociatedWithMetricKeys.get(metricKey);
            }

            if ((metricGroupAssociatedWithMetricKeys == null) || (metricGroupAssociatedWithMetricKeys.length != 2) || 
                    (metricGroupAssociatedWithMetricKeys[0] == null) || (metricGroupAssociatedWithMetricKeys[1] == null)) {
                logger.error("Error creating/initializing associationLists");
                return false;
            }

            for (Integer metricGroupId : metricGroupIds) {
                try {
                    String regex = GlobalVariables.mergedRegexsForMetricGroups.get(metricGroupId);
                    if (regex == null) continue;

                    ArrayList negativeMatchList = metricGroupAssociatedWithMetricKeys[0];
                    ArrayList positiveMatchList = metricGroupAssociatedWithMetricKeys[1];
                    boolean isMetricGroupIdInNegativeList = negativeMatchList.contains(metricGroupId);
                    boolean isMetricGroupIdInPositiveList = positiveMatchList.contains(metricGroupId);

                    if (!isMetricGroupIdInNegativeList && !isMetricGroupIdInPositiveList) {
                        Pattern pattern = getPatternFromRegexString(regex);

                        if (pattern != null) {
                            Matcher matcher = pattern.matcher(metricKey);
                            boolean isMetricKeyAssociatedWithMetricGroup = matcher.matches();

                            if (isMetricKeyAssociatedWithMetricGroup) {
                                positiveMatchList.add(metricGroupId);
                                positiveMatchList.trimToSize();
                            }
                            else {
                                negativeMatchList.add(metricGroupId);
                                negativeMatchList.trimToSize();
                            }

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
                        else {
                            isMetricKeyAssociatedWithAnyMetricGroup = false;
                        }
                    }
                    else if (isMetricGroupIdInPositiveList) {
                        isMetricKeyAssociatedWithAnyMetricGroup = true;
                    }
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }

            GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.put(metricKey, isMetricKeyAssociatedWithAnyMetricGroup);

            return isMetricKeyAssociatedWithAnyMetricGroup;
        }
    }

    protected static List<String> getMetricKeysAssociatedWithAlert(Alert alert) {

        if (alert == null) {
            return new ArrayList<>();
        }

        List<String> metricKeysAssociatedWithAlert = new ArrayList<>();

        Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(alert.getMetricGroupId());

        try {
            if (matchingMetricKeysAssociatedWithMetricGroup != null) {
                for (String metricKey : matchingMetricKeysAssociatedWithMetricGroup) {
                    metricKeysAssociatedWithAlert.add(metricKey);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        return metricKeysAssociatedWithAlert;
    }

}
