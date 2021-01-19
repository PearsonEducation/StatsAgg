package com.pearson.statsagg.database_objects.metric_groups;

import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupsDao {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsDao.class.getName());
    
    protected static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupsSql.Insert_MetricGroup, 
                    metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription(), 
                    metricGroup.getMetricGroupTemplateId(), metricGroup.getVariableSetId());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    protected static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupsSql.Update_MetricGroup_ByPrimaryKey, 
                    metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription(), 
                    metricGroup.getMetricGroupTemplateId(), metricGroup.getVariableSetId(),
                    metricGroup.getId());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean update_Name(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, 
            int metricGroupId, String newMetricGroupName) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupsSql.Update_MetricGroup_Name, 
                    newMetricGroupName, newMetricGroupName.toUpperCase(), 
                    metricGroupId);
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    protected static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricGroup metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, metricGroup.getId());

            boolean upsertSuccess = true;
            if (metricGroupFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, metricGroup);
            else if (!metricGroupFromDb.isEqual(metricGroup)) upsertSuccess = update(connection, false, commitOnCompletion, metricGroup);

            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
            
            return upsertSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }

    }
    
    protected static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup, String oldMetricGroupName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricGroup metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, false, oldMetricGroupName);

            boolean upsertSuccess = true;
            if (metricGroupFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, metricGroup);
            }
            else {
                metricGroup.setId(metricGroupFromDb.getId());
                if (!metricGroupFromDb.isEqual(metricGroup)) upsertSuccess = update(connection, false, commitOnCompletion, metricGroup);
            }

            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
            
            return upsertSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }

    }
    
    protected static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroup metricGroup) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupsSql.Delete_MetricGroup_ByPrimaryKey, 
                    metricGroup.getId());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static MetricGroup getMetricGroup(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, false, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_MetricGroup_ByPrimaryKey, id);
            
            MetricGroup metricGroup = DatabaseUtils.getSingleResultFromList(metricGroups);
            setMetricGroupRegexesAndTagsForMetricGroup(connection, closeConnectionOnCompletion, metricGroup);
            
            return metricGroup;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static MetricGroup getMetricGroup(Connection connection, boolean closeConnectionOnCompletion, String metricGroupName) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, false, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_MetricGroup_ByName, metricGroupName);
            
            MetricGroup metricGroup = DatabaseUtils.getSingleResultFromList(metricGroups);
            setMetricGroupRegexesAndTagsForMetricGroup(connection, closeConnectionOnCompletion, metricGroup);
            
            return metricGroup;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static MetricGroup getMetricGroup_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, String metricGroupName) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, false, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_MetricGroup_ByUppercaseName, metricGroupName.toUpperCase());
            
            MetricGroup metricGroup = DatabaseUtils.getSingleResultFromList(metricGroups);
            setMetricGroupRegexesAndTagsForMetricGroup(connection, closeConnectionOnCompletion, metricGroup);
            
            return metricGroup;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<MetricGroup> getMetricGroups_FilterByMetricGroupTemplateId(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupTemplateId) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, false, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_MetricGroup_ByMetricGroupTemplateId, 
                    metricGroupTemplateId);

            setMetricGroupRegexesAndTagsForMetricGroups(connection, closeConnectionOnCompletion, metricGroups);
            
            return metricGroups;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<MetricGroup> getMetricGroups(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, false, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_AllMetricGroups);
            
            setMetricGroupRegexesAndTagsForMetricGroups(connection, closeConnectionOnCompletion, metricGroups);
            
            return metricGroups;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Set<String> getMetricGroupNames(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                        new MetricGroupsResultSetHandler(), 
                        MetricGroupsSql.Select_MetricGroupNames);
            
            Set<String> metricGroupNames = new HashSet<>();
            if (metricGroups == null) return metricGroupNames;
            
            for (MetricGroup metricGroup : metricGroups) {
                if ((metricGroup != null) && (metricGroup.getName() != null)) metricGroupNames.add(metricGroup.getName());
            }
            
            return metricGroupNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getMetricGroupNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> metricGroupNames = new ArrayList<>();
            
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupsResultSetHandler(), 
                    (resultSetLimit + 5), 
                    MetricGroupsSql.Select_MetricGroup_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((metricGroups == null) || metricGroups.isEmpty()) return metricGroupNames;
            
            int rowCounter = 0;
            for (MetricGroup metricGroup : metricGroups) {
                if ((metricGroup != null) && (metricGroup.getName() != null)) {
                    metricGroupNames.add(metricGroup.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return metricGroupNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Integer> getAllMetricGroupIds(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<Integer> metricGroupIds = new ArrayList<>();
            
            List<MetricGroup> metricGroups = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupsResultSetHandler(), 
                    MetricGroupsSql.Select_DistinctMetricGroupIds);
            
            if ((metricGroups == null) || metricGroups.isEmpty()) return metricGroupIds;
            
            for (MetricGroup metricGroup : metricGroups) {
                if ((metricGroup != null) && (metricGroup.getId() != null)) {
                    metricGroupIds.add(metricGroup.getId());
                }
            }
            
            return metricGroupIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,MetricGroup> getMetricGroups_ByName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<String,MetricGroup> metricGroups_ByName = new HashMap<>();
            
            List<MetricGroup> metricGroups = getMetricGroups(connection, false);
            setMetricGroupRegexesAndTagsForMetricGroups(connection, closeConnectionOnCompletion, metricGroups);

            if (metricGroups != null) {
                for (MetricGroup metricGroup : metricGroups) {
                    if ((metricGroup == null) || (metricGroup.getName() == null)) continue;
                    metricGroups_ByName.put(metricGroup.getName(), metricGroup);
                }
            }

            return metricGroups_ByName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,MetricGroup> getMetricGroups_ByUppercaseName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroup> metricGroups = getMetricGroups(connection, false);
            setMetricGroupRegexesAndTagsForMetricGroups(connection, closeConnectionOnCompletion, metricGroups);
            if (metricGroups == null) return null;
            
            Map<String,MetricGroup> metricGroups_ByUppercaseName = new HashMap<>();

            for (MetricGroup metricGroup : metricGroups) {
                if ((metricGroup == null) || (metricGroup.getName() == null)) continue;
                metricGroups_ByUppercaseName.put(metricGroup.getName().toUpperCase(), metricGroup);
            }

            return metricGroups_ByUppercaseName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean isMetricGroupCreatedByMetricGroupTemplate(Connection connection, boolean closeConnectionOnCompletion, MetricGroup metricGroup, String oldMetricGroupName) {
        
        try {
            MetricGroup metricGroupFromDb;
            
            if (oldMetricGroupName != null) {
                metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, closeConnectionOnCompletion, oldMetricGroupName);
            }
            else {
                if ((metricGroup == null) || (metricGroup.getName() == null)) return false;
                metricGroupFromDb = MetricGroupsDao.getMetricGroup(connection, closeConnectionOnCompletion, metricGroup.getName());
            }

            if ((metricGroupFromDb != null) && (metricGroupFromDb.getMetricGroupTemplateId() != null)) return true;
        }
        catch (Exception e){
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }    
        
        return false;
    }
    
    private static void setMetricGroupRegexesAndTagsForMetricGroup(Connection connection, boolean closeConnectionOnCompletion, MetricGroup metricGroup) {
        
        if ((metricGroup == null) || (metricGroup.getId() == null)) return;
        
        try {
            List<MetricGroupRegex> metricGroupRegexes = MetricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(connection, false, metricGroup.getId());
            TreeSet<String> metricGroupTags = MetricGroupTagsDao.getMetricGroupTagsSortedSetByMetricGroupId(connection, closeConnectionOnCompletion, metricGroup.getId());

            TreeSet<String> matchMetricGroupRegexesSortedSet = MetricGroupRegex.getMatchPatternStringsSortedFromMetricGroupRegexes(metricGroupRegexes);
            TreeSet<String> blacklistMetricGroupRegexesSortedSet = MetricGroupRegex.getBlacklistPatternStringsSortedFromMetricGroupRegexes(metricGroupRegexes);
            metricGroup.setMatchRegexes(matchMetricGroupRegexesSortedSet);
            metricGroup.setBlacklistRegexes(blacklistMetricGroupRegexesSortedSet);
            metricGroup.setTags(metricGroupTags);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    private static void setMetricGroupRegexesAndTagsForMetricGroups(Connection connection, boolean closeConnectionOnCompletion, List<MetricGroup> metricGroups) {
        
        if (metricGroups == null) return;
        
        try {
            Map<Integer,List<MetricGroupRegex>> metricGroupRegexesByMetricGroupId = MetricGroupRegexesDao.getAllMetricGroupRegexesByMetricGroupId(connection, false);
            Map<Integer,List<MetricGroupTag>> metricGroupTagsByMetricGroupId = MetricGroupTagsDao.getAllMetricGroupTagsByMetricGroupId(connection, closeConnectionOnCompletion);
            
            for (MetricGroup metricGroup : metricGroups) {
                if ((metricGroup == null) || (metricGroup.getId() == null))  continue;
                
                if (metricGroupRegexesByMetricGroupId != null) {
                    List<MetricGroupRegex> metricGroupRegexesSortedSet = metricGroupRegexesByMetricGroupId.get(metricGroup.getId());
                    TreeSet<String> matchMetricGroupRegexesSortedSet = MetricGroupRegex.getMatchPatternStringsSortedFromMetricGroupRegexes(metricGroupRegexesSortedSet);
                    TreeSet<String> blacklistMetricGroupRegexesSortedSet = MetricGroupRegex.getBlacklistPatternStringsSortedFromMetricGroupRegexes(metricGroupRegexesSortedSet);
                    metricGroup.setMatchRegexes(matchMetricGroupRegexesSortedSet);
                    metricGroup.setBlacklistRegexes(blacklistMetricGroupRegexesSortedSet);
                }

                if (metricGroupTagsByMetricGroupId != null) {
                    List<MetricGroupTag> metricGroupTags = metricGroupTagsByMetricGroupId.get(metricGroup.getId());
                    TreeSet<String> metricGroupTagsSortedSet = MetricGroupTag.getMetricGroupTagStringsSortedSet(metricGroupTags);
                    metricGroup.setTags(metricGroupTagsSortedSet);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
}
