package com.pearson.statsagg.database.alert_suspensions;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.database.DatabaseObjectDao;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
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
    public AlertSuspension getDatabaseObject(AlertSuspension alertSuspension) {
        if (alertSuspension == null) return null;

        return getDatabaseObject(AlertSuspensionsSql.Select_AlertSuspension_ByPrimaryKey, 
                alertSuspension.getId()); 
    }
    
    @Override
    public boolean insert(AlertSuspension alertSuspension) {
        if (alertSuspension == null) return false;
        
        return insert(AlertSuspensionsSql.Insert_AlertSuspension, 
                alertSuspension.getName(), alertSuspension.getUppercaseName(), alertSuspension.isEnabled(), 
                alertSuspension.getSuspendBy(), alertSuspension.getAlertId(), 
                alertSuspension.getMetricGroupTagsInclusive(), alertSuspension.getMetricGroupTagsExclusive(),
                alertSuspension.isOneTime(), alertSuspension.isSuspendNotificationOnly(), 
                alertSuspension.isRecurSunday(), alertSuspension.isRecurMonday(), 
                alertSuspension.isRecurTuesday(), alertSuspension.isRecurWednesday(),
                alertSuspension.isRecurThursday(), alertSuspension.isRecurFriday(),
                alertSuspension.isRecurSaturday(), alertSuspension.getStartDate(), 
                alertSuspension.getStartTime(), alertSuspension.getDuration(),
                alertSuspension.getDeleteAtTimestamp()
        );
    }
    
    @Override
    public boolean update(AlertSuspension alertSuspension) {
        if (alertSuspension == null) return false;

        return update(AlertSuspensionsSql.Update_AlertSuspension_ByPrimaryKey,
                alertSuspension.getName(), alertSuspension.getUppercaseName(), alertSuspension.isEnabled(), 
                alertSuspension.getSuspendBy(), alertSuspension.getAlertId(), 
                alertSuspension.getMetricGroupTagsInclusive(), alertSuspension.getMetricGroupTagsExclusive(),
                alertSuspension.isOneTime(), alertSuspension.isSuspendNotificationOnly(), 
                alertSuspension.isRecurSunday(), alertSuspension.isRecurMonday(), 
                alertSuspension.isRecurTuesday(), alertSuspension.isRecurWednesday(),
                alertSuspension.isRecurThursday(), alertSuspension.isRecurFriday(),
                alertSuspension.isRecurSaturday(), alertSuspension.getStartDate(), 
                alertSuspension.getStartTime(), alertSuspension.getDuration(),
                alertSuspension.getDeleteAtTimestamp(),
                alertSuspension.getId());
    }

    @Override
    public boolean delete(AlertSuspension alertSuspension) {
        if (alertSuspension == null) return false;
        
        return delete(AlertSuspensionsSql.Delete_AlertSuspension_ByPrimaryKey, 
                alertSuspension.getId()); 
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
            
            Integer duration = resultSet.getInt("DURATION");
            if (resultSet.wasNull()) duration = null;
            
            Timestamp deleteAtTimestamp = resultSet.getTimestamp("DELETE_AT_TIMESTAMP");
            if (resultSet.wasNull()) deleteAtTimestamp = null;            
            
            AlertSuspension alertSuspension = new AlertSuspension(
                    id, name, uppercaseName, isEnabled, suspendBy, alertId, metricGroupTagsInclusive, metricGroupTagsExclusive, isOneTime, isSuspendNotificationOnly, 
                    isRecurSunday, isRecurMonday, isRecurTuesday, isRecurWednesday, isRecurThursday, isRecurFriday, isRecurSaturday, 
                    startDate, startTime, duration, deleteAtTimestamp);
            
            return alertSuspension;
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

    public AlertSuspension getAlertSuspension(int id) {
        return getDatabaseObject(AlertSuspensionsSql.Select_AlertSuspension_ByPrimaryKey, 
                id); 
    }  
    
    public AlertSuspension getAlertSuspensionByName(String name) {
        
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
                AlertSuspension alertSuspension = processSingleResultAllColumns(resultSet);
                return alertSuspension;
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
    
    public List<AlertSuspension> getAlertSuspensions_BySuspendBy(Integer suspendByCode) {
        
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

            List<AlertSuspension> alertSuspensions = processResultSet(databaseInterface_.getResults());
            return alertSuspensions; 
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
    
    public Map<Integer,List<AlertSuspension>> getAlertSuspensions_SuspendByAlertId_ByAlertId() {

        List<AlertSuspension> alertSuspensions_SuspendByAlertId = getAlertSuspensions_BySuspendBy(AlertSuspension.SUSPEND_BY_ALERT_ID);
        
        if (alertSuspensions_SuspendByAlertId == null) return new HashMap<>();
        
        Map<Integer,List<AlertSuspension>> alertSuspensions_SuspendByAlertId_ByAlertId = new HashMap<>();
        
        for (AlertSuspension alertSuspension : alertSuspensions_SuspendByAlertId) {
            if (alertSuspension.getAlertId() == null) continue;
            
            Integer alertId = alertSuspension.getAlertId();
            List<AlertSuspension> alertSuspensions = alertSuspensions_SuspendByAlertId_ByAlertId.get(alertId);
            
            if (alertSuspensions != null) {
                alertSuspensions.add(alertSuspension);
            }
            else {
                alertSuspensions = new ArrayList<>();
                alertSuspensions_SuspendByAlertId_ByAlertId.put(alertId, alertSuspensions);
            }
            
        }
        
        return alertSuspensions_SuspendByAlertId_ByAlertId;
    }
    
    public Map<String,List<AlertSuspension>> getAlertSuspensions_ForSuspendByAlertId_ByMetricGroupTag() {

        List<AlertSuspension> alertSuspensions_SuspendByMetricGroupTags = getAlertSuspensions_BySuspendBy(AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS);
        
        if (alertSuspensions_SuspendByMetricGroupTags == null) return new HashMap<>();
        
        Map<String,List<AlertSuspension>> alertSuspensions_SuspendByMetricGroupTag_ByMetricGroupTag = new HashMap<>();
        
        for (AlertSuspension alertSuspension : alertSuspensions_SuspendByMetricGroupTags) {
            if (alertSuspension.getAlertId() == null) continue;
            
            String metricGroupTags_NewlineDelimitedString = alertSuspension.getMetricGroupTagsInclusive();
            List<String> metricGroupTags = AlertSuspension.getMetricGroupTagStringsFromNewlineDelimitedString(metricGroupTags_NewlineDelimitedString);
            
            for (String metricGroupTag : metricGroupTags) {
                List<AlertSuspension> alertSuspensions = alertSuspensions_SuspendByMetricGroupTag_ByMetricGroupTag.get(metricGroupTag);

                if (alertSuspensions != null) {
                    alertSuspensions.add(alertSuspension);
                }
                else {
                    alertSuspensions = new ArrayList<>();
                    alertSuspensions_SuspendByMetricGroupTag_ByMetricGroupTag.put(metricGroupTag, alertSuspensions);
                }
            }
            
        }
        
        return alertSuspensions_SuspendByMetricGroupTag_ByMetricGroupTag;
    }
    
}
