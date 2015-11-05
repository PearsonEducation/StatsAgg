package com.pearson.statsagg.database_objects.alert_suspensions;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertSuspensionsDao extends DatabaseObjectDao<AlertSuspension> {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionsDao.class.getName());
   
    private final String tableName_ = "ALERT_SUSPENSIONS";
    
    public AlertSuspensionsDao(){}
            
    public AlertSuspensionsDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public boolean dropTable() {
        return dropTable(AlertSuspensionsSql.DropTable_AlertSuspensions);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateTable_AlertSuspensions_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateTable_AlertSuspensions_Derby);
            databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateIndex_AlertSuspensions_PrimaryKey);
        }
        
        databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateIndex_AlertSuspensions_Unique_Name);
        databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateIndex_AlertSuspensions_Unique_UppercaseName);
        databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateIndex_AlertSuspensions_SuspendBy);
        databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateIndex_AlertSuspensions_DeleteAtTimestamp);
        databaseCreationSqlStatements.add(AlertSuspensionsSql.CreateIndex_AlertSuspensions_ForeignKey_AlertId);
                        
        return createTable(databaseCreationSqlStatements);
    }

    @Override
    public AlertSuspension getDatabaseObject(AlertSuspension suspension) {
        if (suspension == null) return null;

        return getDatabaseObject(AlertSuspensionsSql.Select_AlertSuspension_ByPrimaryKey, 
                suspension.getId()); 
    }
    
    @Override
    public boolean insert(AlertSuspension suspension) {
        if (suspension == null) return false;
        
        return insert(AlertSuspensionsSql.Insert_AlertSuspension, 
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
    public boolean update(AlertSuspension suspension) {
        if (suspension == null) return false;

        return update(AlertSuspensionsSql.Update_AlertSuspension_ByPrimaryKey,
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
    public boolean delete(AlertSuspension suspension) {
        if (suspension == null) return false;
        
        return delete(AlertSuspensionsSql.Delete_AlertSuspension_ByPrimaryKey, 
                suspension.getId()); 
    }
    
    @Override
    public AlertSuspension processSingleResultAllColumns(ResultSet resultSet) {
        
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
            
            AlertSuspension suspension = new AlertSuspension(
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

    public AlertSuspension getSuspension(int id) {
        return getDatabaseObject(AlertSuspensionsSql.Select_AlertSuspension_ByPrimaryKey, 
                id); 
    }  
    
    public AlertSuspension getSuspensionByName(String name) {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(AlertSuspensionsSql.Select_AlertSuspension_ByName, 1);
            databaseInterface_.addPreparedStatementParameters(name);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();
            
            if (resultSet.next()) {
                AlertSuspension suspension = processSingleResultAllColumns(resultSet);
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

            databaseInterface_.createPreparedStatement(AlertSuspensionsSql.Select_AlertSuspensionId_BySuspendBy, 100);
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
    
    public List<AlertSuspension> getSuspensions_BySuspendBy(Integer suspendByCode) {
        
        if (suspendByCode == null) {
            return new ArrayList<>();
        }
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            databaseInterface_.createPreparedStatement(AlertSuspensionsSql.Select_AlertSuspension_BySuspendBy, 100);
            databaseInterface_.addPreparedStatementParameters(suspendByCode);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            List<AlertSuspension> suspensions = processResultSet(databaseInterface_.getResults());
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
        
        return genericDmlStatement(AlertSuspensionsSql.Delete_AlertSuspension_DeleteAtTimestamp, parametersList);
    }
    
    public Map<Integer,List<AlertSuspension>> getSuspensions_SuspendByAlertId_ByAlertId() {

        List<AlertSuspension> suspensions_SuspendByAlertId = getSuspensions_BySuspendBy(AlertSuspension.SUSPEND_BY_ALERT_ID);
        
        if (suspensions_SuspendByAlertId == null) return new HashMap<>();
        
        Map<Integer,List<AlertSuspension>> suspensions_SuspendByAlertId_ByAlertId = new HashMap<>();
        
        for (AlertSuspension suspension : suspensions_SuspendByAlertId) {
            if (suspension.getAlertId() == null) continue;
            
            Integer alertId = suspension.getAlertId();
            List<AlertSuspension> suspensions = suspensions_SuspendByAlertId_ByAlertId.get(alertId);
            
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
    
    public Map<String,List<AlertSuspension>> getSuspensions_ForSuspendByAlertId_ByMetricGroupTag() {

        List<AlertSuspension> suspensions_SuspendByMetricGroupTags = getSuspensions_BySuspendBy(AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS);
        
        if (suspensions_SuspendByMetricGroupTags == null) return new HashMap<>();
        
        Map<String,List<AlertSuspension>> suspensions_SuspendByMetricGroupTag_ByMetricGroupTag = new HashMap<>();
        
        for (AlertSuspension suspension : suspensions_SuspendByMetricGroupTags) {
            if (suspension.getAlertId() == null) continue;
            
            String metricGroupTags_NewlineDelimitedString = suspension.getMetricGroupTagsInclusive();
            List<String> metricGroupTags = StringUtilities.getListOfStringsFromDelimitedString(metricGroupTags_NewlineDelimitedString, '\n');
            
            for (String metricGroupTag : metricGroupTags) {
                List<AlertSuspension> suspensions = suspensions_SuspendByMetricGroupTag_ByMetricGroupTag.get(metricGroupTag);

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
    
    public JSONObject getSuspension(int offset, int pageSize) {

        List<Object> parametersList = new ArrayList<>(2);

        JSONArray suspensionList = new JSONArray();
        JSONObject suspensionJson = new JSONObject();
        int suspensionCount = 0;

        try {
            if (!isConnectionValid()) {
                return null;
            }

            if ((offset == 0) && (pageSize == 0)) {
                suspensionJson.put("suspensions", suspensionList);
                suspensionJson.put("count", suspensionCount);
                return suspensionJson;
            }

            parametersList.add(offset);
            parametersList.add(pageSize);

            if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
                databaseInterface_.createPreparedStatement(AlertSuspensionsSql.Select_AlertSuspension_ByPageNumberAndPageSize_MySQL, pageSize);
            }
            else {
                databaseInterface_.createPreparedStatement(AlertSuspensionsSql.Select_AlertSuspension_ByPageNumberAndPageSize_Derby, pageSize);
            }
            
            databaseInterface_.addPreparedStatementParameters(parametersList);

            databaseInterface_.executePreparedStatement();

            if (!databaseInterface_.isResultSetValid()) {
                logger.debug("Invalid resultset");
                return null;
            }

            ResultSet resultSet = databaseInterface_.getResults();

            while (resultSet.next()) {
                JSONObject suspension = new JSONObject();
                suspension.put("name", resultSet.getString("NAME"));
                suspension.put("id", resultSet.getString("ID"));
                suspensionList.add(suspension);
                suspensionCount++;
            }

            suspensionJson.put("alerts", suspensionList);
            suspensionJson.put("count", suspensionCount);

            return suspensionJson;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        }
    }

}
