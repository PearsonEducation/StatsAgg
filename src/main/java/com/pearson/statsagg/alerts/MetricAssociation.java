package com.pearson.statsagg.alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import com.pearson.statsagg.utilities.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAssociation {

    private static final Logger logger = LoggerFactory.getLogger(MetricAssociation.class.getName());

    public static void associateMetricKeysWithMetricGroups() {
        
        Set<String> metricKeys = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.keySet();
        
        synchronized(GlobalVariables.metricGroupChanges) {
            applyMetricGroupGlobalVariableChanges();
        }
  
        MetricGroupsDao metricGrouspDao = new MetricGroupsDao();
        List<Integer> metricGroupIds = metricGrouspDao.getAllMetricGroupIds();
        
        updateMergedRegexsForMetricGroups(metricGroupIds);
                
        for (String metricKey : metricKeys) {
            associateMetricKeyWithMetricGroups(metricKey, metricGroupIds);
        }
        
    }

    private static void applyMetricGroupGlobalVariableChanges() {
        Set<Integer> metricGroupIdsLocal = new HashSet<>(GlobalVariables.metricGroupChanges.keySet());
        
        for (Integer metricGroupId : metricGroupIdsLocal) {
            String alterOrRemove = GlobalVariables.metricGroupChanges.get(metricGroupId);

            // update global variables for the case of a metric group either being newly added or changed
            if ((alterOrRemove != null) && alterOrRemove.equalsIgnoreCase("Alter")) {
                GlobalVariables.metricKeysAssociatedWithMetricGroup.remove(metricGroupId);
                GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.remove(metricGroupId);
                GlobalVariables.mergedRegexsForMetricGroups.remove(metricGroupId);
                GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.clear();
                GlobalVariables.metricGroupChanges.remove(metricGroupId);
                
                while (GlobalVariables.metricKeysAssociatedWithMetricGroup.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                while (GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                while (GlobalVariables.mergedRegexsForMetricGroups.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                while (GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.size() > 0) Threads.sleepMilliseconds(10);
                while (GlobalVariables.metricGroupChanges.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
            }
            // update global variables for the case of a metric group either being removed
            else if ((alterOrRemove != null) && alterOrRemove.equalsIgnoreCase("Remove")) {
                GlobalVariables.metricKeysAssociatedWithMetricGroup.remove(metricGroupId);
                GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.remove(metricGroupId);
                GlobalVariables.mergedRegexsForMetricGroups.remove(metricGroupId);
                GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.clear();
                GlobalVariables.metricGroupChanges.remove(metricGroupId);

                while (GlobalVariables.metricKeysAssociatedWithMetricGroup.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                while (GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                while (GlobalVariables.mergedRegexsForMetricGroups.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
                while (GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.size() > 0) Threads.sleepMilliseconds(10);
                while (GlobalVariables.metricGroupChanges.containsKey(metricGroupId)) Threads.sleepMilliseconds(10);
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
     This method performs two tasks. 
     Task 1: Determines if a metric key is associated with ANY metric group. 
     The boolean value of this determination is returned & is stored in "GlobalVariables.metricKeysAssociatedWithAnyMetricGroup".
     Task 2: For every metric group, determine if this metric key is associated with it. 
     The boolean value of this determination is stored in "GlobalVariables.metricKeysAssociatedWithMetricGroup" (stores true & false determinations).
     Also, if the association is true, then determination is stored in "GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup".
     */
    private static boolean associateMetricKeyWithMetricGroups(String metricKey, List<Integer> metricGroupIds) {

        if ((metricKey == null) || (metricGroupIds == null)) {
            return false;
        }

        Boolean isMetricKeyAssociatedWithAnyMetricGroup = GlobalVariables.metricKeysAssociatedWithAnyMetricGroup.get(metricKey);
        if (isMetricKeyAssociatedWithAnyMetricGroup != null) return isMetricKeyAssociatedWithAnyMetricGroup;
        isMetricKeyAssociatedWithAnyMetricGroup = false;

        for (Integer metricGroupId : metricGroupIds) {
            try {
                String regex = GlobalVariables.mergedRegexsForMetricGroups.get(metricGroupId);
                if (regex == null) continue;
                
                Map<String, Boolean> metricKeyAssociationWithMetricGroup = GlobalVariables.metricKeysAssociatedWithMetricGroup.get(metricGroupId);

                if (metricKeyAssociationWithMetricGroup == null) {
                    metricKeyAssociationWithMetricGroup = new HashMap<>();
                    GlobalVariables.metricKeysAssociatedWithMetricGroup.put(metricGroupId, Collections.synchronizedMap(metricKeyAssociationWithMetricGroup));
                }

                Boolean metricKeyAssociation = metricKeyAssociationWithMetricGroup.get(metricKey);

                if (metricKeyAssociation == null) {
                    Pattern pattern = getPatternFromRegexString(regex);
                    Matcher matcher = pattern.matcher(metricKey);
                    boolean isMetricKeyAssociatedWithMetricGroup = matcher.matches();
                    metricKeyAssociationWithMetricGroup.put(metricKey, isMetricKeyAssociatedWithMetricGroup);

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
                else if (metricKeyAssociation) {
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

    /* 
     This method merges every regex associated with a single metric group into a single regex (using '|' as the glue between regexs).
     */
    public static String createMergedMetricGroupRegex(Integer metricGroupId) {

        if (metricGroupId == null) {
            return null;
        }
                    
        MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
        List<String> regexs = metricGroupRegexsDao.getPatterns(metricGroupId);
        String mergedRegex = StringUtilities.createMergedRegex(regexs);
        
        return mergedRegex;
    }

    public static Pattern getPatternFromRegexString(String regex) {

        if (regex == null) {
            return null;
        }

        Pattern pattern = GlobalVariables.metricGroupRegexPatterns.get(regex);

        if (pattern == null) {
            try {
                pattern = Pattern.compile(regex);
                GlobalVariables.metricGroupRegexPatterns.put(regex, pattern);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        return pattern;
    }

    public static List<String> getMetricKeysAssociatedWithAlert(Alert alert) {

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
