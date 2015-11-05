package com.pearson.statsagg.alerts;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import com.pearson.statsagg.utilities.Threads;
import java.util.concurrent.ConcurrentHashMap;
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
    private final static ConcurrentHashMap<String,Pattern> regexPatterns = new ConcurrentHashMap<>(); 
    
    // k=Regex_String, v="Regex_String. If a regex pattern is bad (doesn't compile), then it is stored here so we don't try to recompile it."
    private final static ConcurrentHashMap<String,String> regexBlacklist = new ConcurrentHashMap<>(); 
    
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
        
        // set a flag to indicate that the metric association routine is running -- prevents other threads from running at the same time
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(true);
        
        List<Integer> allMetricGroupIds = getMetricGroupIds_And_AssociateMetricKeysWithNewOrAlteredMetricGroups();
        List<Integer> allMetricSuspensionIds = getMetricSuspensionIds_And_AssociateMetricKeysWithNewOrAlteredSuspensions();

        // run the association routine against all metric-groups/metric-suspensions/metric-keys. should only run the pattern matcher against previously unknown metric-keys.
        for (String metricKey : GlobalVariables.metricKeysLastSeenTimestamp.keySet()) {
            ConcurrentHashMap<String,String> immediateCleanupMetrics = GlobalVariables.immediateCleanupMetrics;
            if ((immediateCleanupMetrics != null) && !immediateCleanupMetrics.isEmpty() && immediateCleanupMetrics.containsKey(metricKey)) continue;
                    
            associateMetricKeyWithIds(metricKey, allMetricGroupIds, 
                    GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup, GlobalVariables.metricKeysAssociatedWithAnyMetricGroup, 
                    GlobalVariables.mergedMatchRegexesByMetricGroupId, GlobalVariables.mergedBlacklistRegexesByMetricGroupId);

            associateMetricKeyWithIds(metricKey, allMetricSuspensionIds, 
                    GlobalVariables.matchingMetricKeysAssociatedWithSuspension, GlobalVariables.metricKeysAssociatedWithAnySuspension, 
                    GlobalVariables.mergedMatchRegexesBySuspensionId, GlobalVariables.mergedBlacklistRegexesBySuspensionId);
        }
        
        IsMetricAssociationRoutineCurrentlyRunning_CurrentlyAssociating.set(false);

        IsMetricAssociationRoutineCurrentlyRunning.set(false);
    }
    
    private static List<Integer> getMetricGroupIds_And_AssociateMetricKeysWithNewOrAlteredMetricGroups() {
        
        List<String> metricsToReassociateWithAlteredMetricGroups = new ArrayList<>();
        List<Integer> newAndAlteredMetricGroupIds = new ArrayList<>();
        List<Integer> allMetricGroupIds;

        synchronized(GlobalVariables.metricGroupChanges) {
            associateMetricKeysWithNewOrAlteredIds_DetectChanges(GlobalVariables.metricGroupChanges, 
                    GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup, GlobalVariables.metricKeysAssociatedWithAnyMetricGroup, 
                    GlobalVariables.mergedMatchRegexesByMetricGroupId, GlobalVariables.mergedBlacklistRegexesByMetricGroupId,
                    newAndAlteredMetricGroupIds, metricsToReassociateWithAlteredMetricGroups);
            
            updateMergedRegexesForMetricGroups(newAndAlteredMetricGroupIds);
            
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
            allMetricGroupIds = metricGroupsDao.getAllMetricGroupIds();
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

            AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
            allMetricSuspensionIds = alertSuspensionsDao.getSuspensionIds_BySuspendBy(AlertSuspension.SUSPEND_BY_METRICS);
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
    */
    private static void associateMetricKeyWithId(String metricKey, Integer id, 
            ConcurrentHashMap<Integer,Set<String>> matchingMetricKeysAssociatedWithId,
            ConcurrentHashMap<Integer,String> mergedMatchRegexesById,
            ConcurrentHashMap<Integer,String> mergedBlacklistRegexesById) {

        ConcurrentHashMap<String,String> immediateCleanupMetrics = GlobalVariables.immediateCleanupMetrics;
        if ((immediateCleanupMetrics != null) && !immediateCleanupMetrics.isEmpty() && immediateCleanupMetrics.containsKey(metricKey)) return;
        
        try {
            String matchRegex = mergedMatchRegexesById.get(id);
            if (matchRegex == null) return;

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
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
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

                        if (matchingMetricKeyAssociations == null) {
                            matchingMetricKeyAssociations = new HashSet<>();
                            matchingMetricKeysAssociatedWithId.put(id, Collections.synchronizedSet(matchingMetricKeyAssociations));
                        }

                        matchingMetricKeyAssociations.add(metricKey);
                        isMetricKeyAssociatedWithAnyId = true;
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

        Pattern pattern = regexPatterns.get(regex);

        if (pattern == null) {
            boolean isRegexBad = regexBlacklist.containsKey(regex);
            if (isRegexBad) return null;
            
            try {
                pattern = Pattern.compile(regex);
                regexPatterns.put(regex, pattern);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                regexBlacklist.put(regex, regex);
            }
        }

        return pattern;
    }
    
    // update GlobalVariables.mergedMatchRegexesByMetricGroupId & GlobalVariables.mergedBlacklistRegexesByMetricGroupId with the latest merged regexes
    public static void updateMergedRegexesForMetricGroups(List<Integer> metricGroupIds) {

        if (metricGroupIds == null) {
            return;
        }
        
        for (Integer metricGroupId : metricGroupIds) {
            String mergedMatchRegex = GlobalVariables.mergedMatchRegexesByMetricGroupId.get(metricGroupId);
            String mergedBlacklistRegex = GlobalVariables.mergedBlacklistRegexesByMetricGroupId.get(metricGroupId);
            List<MetricGroupRegex> metricGroupRegexes = null;
            
            if ((mergedMatchRegex == null) || (mergedBlacklistRegex == null)) {
                MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao();
                metricGroupRegexes = metricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(metricGroupId);
            }
                
            if (mergedMatchRegex == null) {
                mergedMatchRegex = createMergedMetricGroupRegex(metricGroupRegexes, REGEX_TYPE_MATCH);
                if (mergedMatchRegex != null) GlobalVariables.mergedMatchRegexesByMetricGroupId.put(metricGroupId, mergedMatchRegex);
            }
            
            if (mergedBlacklistRegex == null) {
                mergedBlacklistRegex = createMergedMetricGroupRegex(metricGroupRegexes, REGEX_TYPE_BLACKLIST);
                if (mergedBlacklistRegex != null) GlobalVariables.mergedBlacklistRegexesByMetricGroupId.put(metricGroupId, mergedBlacklistRegex);
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
            String mergedMatchRegex = GlobalVariables.mergedMatchRegexesBySuspensionId.get(suspensionId);

            if (mergedMatchRegex == null) {
                AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
                AlertSuspension suspension = alertSuspensionsDao.getSuspension(suspensionId);
                
                List<String> regexPatterns = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricSuspensionRegexes(), '\n');
                mergedMatchRegex = StringUtilities.createMergedRegex(regexPatterns);

                if (mergedMatchRegex != null) GlobalVariables.mergedMatchRegexesBySuspensionId.put(suspensionId, mergedMatchRegex);
            }
        }
        
    }
    
    // todo: improve performance of this method
    public static List<String> getMetricKeysAssociatedWithAlert(Alert alert) {

        if (alert == null) {
            return new ArrayList<>();
        }

        List<String> suspendedMetricKeys_Local;
        Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(alert.getMetricGroupId());
        Set<String> metricKeysAssociatedWithAlert;
        
        if (matchingMetricKeysAssociatedWithMetricGroup != null) {
            
            synchronized(GlobalVariables.suspendedMetricKeys) {
                suspendedMetricKeys_Local = new ArrayList<>(GlobalVariables.suspendedMetricKeys.keySet());
            }
            
            synchronized(matchingMetricKeysAssociatedWithMetricGroup) {
                metricKeysAssociatedWithAlert = new HashSet<>(matchingMetricKeysAssociatedWithMetricGroup);
            }
            
            metricKeysAssociatedWithAlert.removeAll(suspendedMetricKeys_Local);
        }
        else {
            metricKeysAssociatedWithAlert = new HashSet<>();
        }

        return new ArrayList<>(metricKeysAssociatedWithAlert);
    }

}
