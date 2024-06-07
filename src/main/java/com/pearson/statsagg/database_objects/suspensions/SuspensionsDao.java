package com.pearson.statsagg.database_objects.suspensions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SuspensionsDao {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionsDao.class.getName());
   
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Suspension suspension) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    SuspensionsSql.Insert_Suspension, 
                    suspension.getName(), suspension.getUppercaseName(), suspension.getDescription(), suspension.isEnabled(), 
                    suspension.getSuspendBy(), suspension.getAlertId(), 
                    suspension.getMetricGroupTagsInclusive(), suspension.getMetricGroupTagsExclusive(),
                    suspension.getMetricSuspensionRegexes(),
                    suspension.isOneTime(), suspension.isSuspendNotificationOnly(), 
                    suspension.isRecurSunday(), suspension.isRecurMonday(), 
                    suspension.isRecurTuesday(), suspension.isRecurWednesday(),
                    suspension.isRecurThursday(), suspension.isRecurFriday(),
                    suspension.isRecurSaturday(), suspension.getStartDate(), 
                    suspension.getStartTime(), suspension.getDuration(), suspension.getDurationTimeUnit(),
                    suspension.getDeleteAtTimestamp());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Suspension suspension) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    SuspensionsSql.Update_Suspension_ByPrimaryKey, 
                    suspension.getName(), suspension.getUppercaseName(), suspension.getDescription(), suspension.isEnabled(), 
                    suspension.getSuspendBy(), suspension.getAlertId(), 
                    suspension.getMetricGroupTagsInclusive(), suspension.getMetricGroupTagsExclusive(),
                    suspension.getMetricSuspensionRegexes(),
                    suspension.isOneTime(), suspension.isSuspendNotificationOnly(), 
                    suspension.isRecurSunday(), suspension.isRecurMonday(), 
                    suspension.isRecurTuesday(), suspension.isRecurWednesday(),
                    suspension.isRecurThursday(), suspension.isRecurFriday(),
                    suspension.isRecurSaturday(), suspension.getStartDate(), 
                    suspension.getStartTime(), suspension.getDuration(), suspension.getDurationTimeUnit(),
                    suspension.getDeleteAtTimestamp(),
                    suspension.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Suspension suspension) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            Suspension suspensionFromDb = SuspensionsDao.getSuspension(connection, false, suspension.getId());

            boolean upsertSuccess = true;
            if (suspensionFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, suspension);
            else if (!suspensionFromDb.isEqual(suspension)) upsertSuccess = update(connection, false, commitOnCompletion, suspension);

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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Suspension suspension, String oldSuspensionName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            Suspension suspensionFromDb = SuspensionsDao.getSuspension(connection, false, oldSuspensionName);

            boolean upsertSuccess = true;
            if (suspensionFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, suspension);
            }
            else {
                suspension.setId(suspensionFromDb.getId());
                if (!suspensionFromDb.isEqual(suspension)) upsertSuccess = update(connection, false, commitOnCompletion, suspension);
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
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Suspension suspension) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    SuspensionsSql.Delete_Suspension_ByPrimaryKey, 
                    suspension.getId());
            
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
    
    public static boolean deleteExpired(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, Timestamp specifiedDateAndTime) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    SuspensionsSql.Delete_Suspension_DeleteAtTimestamp, 
                    specifiedDateAndTime);
            
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

    public static Suspension getSuspension(Connection connection, boolean closeConnectionOnCompletion, Integer suspensionId) {
        
        try {
            List<Suspension> suspensions = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new SuspensionsResultSetHandler(), 
                    SuspensionsSql.Select_Suspension_ByPrimaryKey, suspensionId);
            
            return DatabaseUtils.getSingleResultFromList(suspensions);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static Suspension getSuspension(Connection connection, boolean closeConnectionOnCompletion, String suspensionName) {
        
        try {
            List<Suspension> suspensions = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new SuspensionsResultSetHandler(), 
                    SuspensionsSql.Select_Suspension_ByName, suspensionName);
            
            return DatabaseUtils.getSingleResultFromList(suspensions);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Suspension getSuspension_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, String suspensionName) {
        
        try {
            List<Suspension> suspensions = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new SuspensionsResultSetHandler(), 
                    SuspensionsSql.Select_Suspension_ByUppercaseName, suspensionName.toUpperCase());
            
            return DatabaseUtils.getSingleResultFromList(suspensions);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Suspension> getSuspensions(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<Suspension> suspensions = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new SuspensionsResultSetHandler(), 
                    SuspensionsSql.Select_AllSuspensions);
            
            return suspensions;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Suspension> getSuspensions_FilterByAlertId(Connection connection, boolean closeConnectionOnCompletion, int alertId) {
        
        try {
            List<Suspension> suspensions = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new SuspensionsResultSetHandler(), 
                    SuspensionsSql.Select_Suspension_ByAlertId,
                    alertId);
            
            return suspensions;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Integer> getSuspensionIds_BySuspendBy(Connection connection, boolean closeConnectionOnCompletion, Integer suspendByCode) {
        
        try {
            List<Integer> suspensionIds = new ArrayList<>();
            
            List<Suspension> suspensions = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new SuspensionsResultSetHandler(), 
                    SuspensionsSql.Select_SuspensionId_BySuspendBy,
                    suspendByCode);
            
            if ((suspensions == null) || suspensions.isEmpty()) return suspensionIds;
            
            for (Suspension suspension : suspensions) {
                if ((suspension != null) && (suspension.getId() != null)) {
                    suspensionIds.add(suspension.getId());
                }
            }
            
            return suspensionIds;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<Suspension> getSuspensions_BySuspendBy(Connection connection, boolean closeConnectionOnCompletion, Integer suspendByCode) {
        
        try {
            List<Suspension> suspensions = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new SuspensionsResultSetHandler(), 
                    SuspensionsSql.Select_Suspension_BySuspendBy,
                    suspendByCode);

            return suspensions;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<Integer,List<Suspension>> getSuspensions_SuspendByAlertId_ByAlertId(Connection connection, boolean closeConnectionOnCompletion) {

        Map<Integer,List<Suspension>> suspensions_SuspendByAlertId_ByAlertId = new HashMap<>();
        
        try {
            List<Suspension> suspensions_SuspendByAlertId = getSuspensions_BySuspendBy(connection, closeConnectionOnCompletion, Suspension.SUSPEND_BY_ALERT_ID);
            if (suspensions_SuspendByAlertId == null) return suspensions_SuspendByAlertId_ByAlertId;
            
            for (Suspension suspension : suspensions_SuspendByAlertId) {
                if ((suspension == null) || (suspension.getAlertId() == null)) continue;
                Integer alertId = suspension.getAlertId();
                
                List<Suspension> suspensions = suspensions_SuspendByAlertId_ByAlertId.get(alertId);

                if (suspensions != null) {
                    suspensions.add(suspension);
                }
                else {
                    suspensions = new ArrayList<>();
                    suspensions_SuspendByAlertId_ByAlertId.put(alertId, suspensions);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return suspensions_SuspendByAlertId_ByAlertId;
    }
    
    public Map<String,List<Suspension>> getSuspensions_ForSuspendByAlertId_ByMetricGroupTag(Connection connection, boolean closeConnectionOnCompletion) {

        Map<String,List<Suspension>> suspensions_SuspendByMetricGroupTag_ByMetricGroupTag = new HashMap<>();

        try {
            List<Suspension> suspensions_SuspendByMetricGroupTags = getSuspensions_BySuspendBy(connection, closeConnectionOnCompletion, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS);
            if (suspensions_SuspendByMetricGroupTags == null) return new HashMap<>();
            
            for (Suspension suspension : suspensions_SuspendByMetricGroupTags) {
                if ((suspension == null) || (suspension.getAlertId() == null)) continue;

                String metricGroupTags_NewlineDelimitedString = suspension.getMetricGroupTagsInclusive();
                List<String> metricGroupTags = StringUtilities.getListOfStringsFromDelimitedString(metricGroupTags_NewlineDelimitedString, '\n');

                for (String metricGroupTag : metricGroupTags) {
                    List<Suspension> suspensions = suspensions_SuspendByMetricGroupTag_ByMetricGroupTag.get(metricGroupTag);

                    if (suspensions != null) {
                        suspensions.add(suspension);
                    }
                    else {
                        suspensions = new ArrayList<>();
                        suspensions_SuspendByMetricGroupTag_ByMetricGroupTag.put(metricGroupTag, suspensions);
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return suspensions_SuspendByMetricGroupTag_ByMetricGroupTag;
    }

}
