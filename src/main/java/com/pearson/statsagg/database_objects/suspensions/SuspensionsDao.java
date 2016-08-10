package com.pearson.statsagg.database_objects.suspensions;

import com.pearson.statsagg.database_engine.DatabaseInterface;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.database_engine.DatabaseObjectDao;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SuspensionsDao extends DatabaseObjectDao<Suspension> {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionsDao.class.getName());
   
    private final String tableName_ = "SUSPENSIONS";
    
    public SuspensionsDao(){}
            
    public SuspensionsDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public SuspensionsDao(DatabaseInterface databaseInterface) {
        super(databaseInterface);
    }
    
    public boolean dropTable() {
        return dropTable(SuspensionsSql.DropTable_Suspensions);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(SuspensionsSql.CreateTable_Suspensions_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(SuspensionsSql.CreateTable_Suspensions_Derby);
            databaseCreationSqlStatements.add(SuspensionsSql.CreateIndex_Suspensions_PrimaryKey);
        }
        
        databaseCreationSqlStatements.add(SuspensionsSql.CreateIndex_Suspensions_Unique_Name);
        databaseCreationSqlStatements.add(SuspensionsSql.CreateIndex_Suspensions_Unique_UppercaseName);
        databaseCreationSqlStatements.add(SuspensionsSql.CreateIndex_Suspensions_SuspendBy);
        databaseCreationSqlStatements.add(SuspensionsSql.CreateIndex_Suspensions_DeleteAtTimestamp);
        databaseCreationSqlStatements.add(SuspensionsSql.CreateIndex_Suspensions_ForeignKey_AlertId);
                        
        return createTable(databaseCreationSqlStatements);
    }

    @Override
    public Suspension getDatabaseObject(Suspension suspension) {
        if (suspension == null) return null;

        return getDatabaseObject(SuspensionsSql.Select_Suspension_ByPrimaryKey, 
                suspension.getId()); 
    }
    
    @Override
    public boolean insert(Suspension suspension) {
        if (suspension == null) return false;
        
        return insert(SuspensionsSql.Insert_Suspension, 
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
                suspension.getDeleteAtTimestamp()
        );
    }
    
    @Override
    public boolean update(Suspension suspension) {
        if (suspension == null) return false;

        return update(SuspensionsSql.Update_Suspension_ByPrimaryKey,
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
    }

    @Override
    public boolean delete(Suspension suspension) {
        if (suspension == null) return false;
        
        return delete(SuspensionsSql.Delete_Suspension_ByPrimaryKey, 
                suspension.getId()); 
    }
    
    @Override
    public Suspension processSingleResultAllColumns(ResultSet resultSet) {
        
        try {
            if ((resultSet == null) || resultSet.isClosed()) {
                return null;
            }

            Integer id = resultSet.getInt("ID");
            if (resultSet.wasNull()) id = null;
            
            String name = resultSet.getString("NAME");
            if (resultSet.wasNull()) name = null;
            
            String uppercaseName = resultSet.getString("UPPERCASE_NAME");
            if (resultSet.wasNull()) uppercaseName = null;
            
            String description = resultSet.getString("DESCRIPTION");
            if (resultSet.wasNull()) description = null;
            
            Boolean isEnabled = resultSet.getBoolean("IS_ENABLED");
            if (resultSet.wasNull()) isEnabled = null;
            
            Integer suspendBy = resultSet.getInt("SUSPEND_BY");
            if (resultSet.wasNull()) suspendBy = null;
            
            Integer alertId = resultSet.getInt("ALERT_ID");
            if (resultSet.wasNull()) alertId = null;

            String metricGroupTagsInclusive = resultSet.getString("METRIC_GROUP_TAGS_INCLUSIVE");
            if (resultSet.wasNull()) metricGroupTagsInclusive = null;
            
            String metricGroupTagsExclusive = resultSet.getString("METRIC_GROUP_TAGS_EXCLUSIVE");
            if (resultSet.wasNull()) metricGroupTagsExclusive = null;
            
            String metricSuspensionRegexes = resultSet.getString("METRIC_SUSPENSION_REGEXES");
            if (resultSet.wasNull()) metricSuspensionRegexes = null;
            
            Boolean isOneTime = resultSet.getBoolean("IS_ONE_TIME");
            if (resultSet.wasNull()) isOneTime = null;
            
            Boolean isSuspendNotificationOnly = resultSet.getBoolean("IS_SUSPEND_NOTIFICATION_ONLY");
            if (resultSet.wasNull()) isSuspendNotificationOnly = null;
            
            Boolean isRecurSunday = resultSet.getBoolean("IS_RECUR_SUNDAY");
            if (resultSet.wasNull()) isRecurSunday = null;
            
            Boolean isRecurMonday = resultSet.getBoolean("IS_RECUR_MONDAY");
            if (resultSet.wasNull()) isRecurMonday = null;
            
            Boolean isRecurTuesday = resultSet.getBoolean("IS_RECUR_TUESDAY");
            if (resultSet.wasNull()) isRecurTuesday = null;
            
            Boolean isRecurWednesday = resultSet.getBoolean("IS_RECUR_WEDNESDAY");
            if (resultSet.wasNull()) isRecurWednesday = null;
            
            Boolean isRecurThursday = resultSet.getBoolean("IS_RECUR_THURSDAY");
            if (resultSet.wasNull()) isRecurThursday = null;
            
            Boolean isRecurFriday = resultSet.getBoolean("IS_RECUR_FRIDAY");
            if (resultSet.wasNull()) isRecurFriday = null;
            
            Boolean isRecurSaturday = resultSet.getBoolean("IS_RECUR_SATURDAY");
            if (resultSet.wasNull()) isRecurSaturday = null;

            Timestamp startDate = resultSet.getTimestamp("START_DATE");
            if (resultSet.wasNull()) startDate = null;
            
            Timestamp startTime = resultSet.getTimestamp("START_TIME");
            if (resultSet.wasNull()) startTime = null;
            
            Long duration = resultSet.getLong("DURATION");
            if (resultSet.wasNull()) duration = null;
            
            Integer durationTimeUnit = resultSet.getInt("DURATION_TIME_UNIT");
            if (resultSet.wasNull()) durationTimeUnit = null;
            
            Timestamp deleteAtTimestamp = resultSet.getTimestamp("DELETE_AT_TIMESTAMP");
            if (resultSet.wasNull()) deleteAtTimestamp = null;            
            
            Suspension suspension = new Suspension(
                    id, name, uppercaseName, description, isEnabled, suspendBy, alertId, metricGroupTagsInclusive, metricGroupTagsExclusive, metricSuspensionRegexes,
                    isOneTime, isSuspendNotificationOnly, isRecurSunday, isRecurMonday, isRecurTuesday, isRecurWednesday, isRecurThursday, isRecurFriday, isRecurSaturday, 
                    startDate, startTime, duration, durationTimeUnit, deleteAtTimestamp);
            
            return suspension;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
    }

    @Override
    public String getTableName() {
        return tableName_;
    }

    public Suspension getSuspension(int id) {
        return getDatabaseObject(SuspensionsSql.Select_Suspension_ByPrimaryKey, 
                id); 
    }  
    
    public Suspension getSuspensionByName(String name) {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(SuspensionsSql.Select_Suspension_ByName, 1);
            databaseInterface_.addPreparedStatementParameters(name);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            if (resultSet.next()) {
                Suspension suspension = processSingleResultAllColumns(resultSet);
                return suspension;
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public List<Integer> getSuspensionIds_BySuspendBy(Integer suspendByCode) {
        
        if (suspendByCode == null) {
            return new ArrayList<>();
        }
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            databaseInterface_.createPreparedStatement(SuspensionsSql.Select_SuspensionId_BySuspendBy, 100);
            databaseInterface_.addPreparedStatementParameters(suspendByCode);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }
            
            List<Integer> suspensionIds = new ArrayList<>();
            
            ResultSet resultSet = databaseInterface_.getResults();

            while (resultSet.next()) {
                Integer id = resultSet.getInt("ID");
                suspensionIds.add(id);
            }
            
            return suspensionIds; 
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public List<Suspension> getSuspensions_BySuspendBy(Integer suspendByCode) {
        
        if (suspendByCode == null) {
            return new ArrayList<>();
        }
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            databaseInterface_.createPreparedStatement(SuspensionsSql.Select_Suspension_BySuspendBy, 100);
            databaseInterface_.addPreparedStatementParameters(suspendByCode);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            List<Suspension> suspensions = processResultSet(databaseInterface_.getResults());
            return suspensions; 
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public boolean deleteExpired(Timestamp specifiedDateAndTime) {
        List<Object> parametersList = new ArrayList<>();
        parametersList.add(specifiedDateAndTime);
        
        return genericDmlStatement(SuspensionsSql.Delete_Suspension_DeleteAtTimestamp, parametersList);
    }
    
    public Map<Integer,List<Suspension>> getSuspensions_SuspendByAlertId_ByAlertId() {

        List<Suspension> suspensions_SuspendByAlertId = getSuspensions_BySuspendBy(Suspension.SUSPEND_BY_ALERT_ID);
        
        if (suspensions_SuspendByAlertId == null) return new HashMap<>();
        
        Map<Integer,List<Suspension>> suspensions_SuspendByAlertId_ByAlertId = new HashMap<>();
        
        for (Suspension suspension : suspensions_SuspendByAlertId) {
            if (suspension.getAlertId() == null) continue;
            
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
        
        return suspensions_SuspendByAlertId_ByAlertId;
    }
    
    public Map<String,List<Suspension>> getSuspensions_ForSuspendByAlertId_ByMetricGroupTag() {

        List<Suspension> suspensions_SuspendByMetricGroupTags = getSuspensions_BySuspendBy(Suspension.SUSPEND_BY_METRIC_GROUP_TAGS);
        
        if (suspensions_SuspendByMetricGroupTags == null) return new HashMap<>();
        
        Map<String,List<Suspension>> suspensions_SuspendByMetricGroupTag_ByMetricGroupTag = new HashMap<>();
        
        for (Suspension suspension : suspensions_SuspendByMetricGroupTags) {
            if (suspension.getAlertId() == null) continue;
            
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
        
        return suspensions_SuspendByMetricGroupTag_ByMetricGroupTag;
    }

}
