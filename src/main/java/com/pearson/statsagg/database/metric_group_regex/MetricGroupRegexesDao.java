package com.pearson.statsagg.database.metric_group_regex;

import com.pearson.statsagg.database.DatabaseInterface;
import java.sql.ResultSet;
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
public class MetricGroupRegexesDao extends DatabaseObjectDao<MetricGroupRegex> {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupRegexesDao.class.getName());
   
    private final String tableName_ = "METRIC_GROUP_REGEXES";
    
    public MetricGroupRegexesDao(){}
            
    public MetricGroupRegexesDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public MetricGroupRegexesDao(DatabaseInterface databaseInterface) {
        super(databaseInterface);
    }
    
    public boolean dropTable() {
        return dropTable(MetricGroupRegexesSql.DropTable_MetricGroupRegexes);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(MetricGroupRegexesSql.CreateTable_MetricGroupRegexes_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(MetricGroupRegexesSql.CreateTable_MetricGroupRegexes_Derby);
            databaseCreationSqlStatements.add(MetricGroupRegexesSql.CreateIndex_MetricGroupRegexes_PrimaryKey);
        }
        
        databaseCreationSqlStatements.add(MetricGroupRegexesSql.CreateIndex_MetricGroupRegexes_ForeignKey_MetricGroupId);
        
        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public MetricGroupRegex getDatabaseObject(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return null;
        
        return getDatabaseObject(MetricGroupRegexesSql.Select_MetricGroupRegex_ByPrimaryKey, 
                metricGroupRegex.getId()); 
    }
        
    @Override
    public boolean insert(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return false;
        
        return insert(MetricGroupRegexesSql.Insert_MetricGroupRegex, 
                metricGroupRegex.getMgId(), metricGroupRegex.isBlacklistRegex(), metricGroupRegex.getPattern());
    }
    
    @Override
    public boolean update(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return false;

        return update(MetricGroupRegexesSql.Update_MetricGroupRegex_ByPrimaryKey, 
                metricGroupRegex.getMgId(), metricGroupRegex.isBlacklistRegex(), metricGroupRegex.getPattern(), metricGroupRegex.getId());
    }

    @Override
    public boolean delete(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return false;
        
        return delete(MetricGroupRegexesSql.Delete_MetricGroupRegex_ByPrimaryKey, 
                metricGroupRegex.getId()); 
    }
    
    @Override
    public MetricGroupRegex processSingleResultAllColumns(ResultSet resultSet) {
        
        try {     
            if ((resultSet == null) || resultSet.isClosed()) {
                return null;
            }

            Integer id = resultSet.getInt("ID");
            if (resultSet.wasNull()) id = null;
            
            Integer mgId = resultSet.getInt("METRIC_GROUP_ID");
            if (resultSet.wasNull()) mgId = null;
            
            Boolean isBlacklistRegex = resultSet.getBoolean("IS_BLACKLIST_REGEX");
            if (resultSet.wasNull()) isBlacklistRegex = null;

            String pattern = resultSet.getString("PATTERN");
            if (resultSet.wasNull()) pattern = null;

            MetricGroupRegex metricGroupRegex = new MetricGroupRegex(id, mgId, isBlacklistRegex, pattern);
            
            return metricGroupRegex;
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
    
    public MetricGroupRegex getMetricGroupRegex(int id) {
        return getDatabaseObject(MetricGroupRegexesSql.Select_MetricGroupRegex_ByPrimaryKey, 
                id); 
    }  
    
    public Map<Integer,List<MetricGroupRegex>> getAllMetricGroupRegexesByMetricGroupId() {
        List<MetricGroupRegex> metricGroupRegexes = getAllDatabaseObjectsInTable();
        
        if ((metricGroupRegexes == null) || metricGroupRegexes.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Integer,List<MetricGroupRegex>> databaseObjectsInTableByMetricGroupId = new HashMap<>();

        for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
            Integer metricGroupId = metricGroupRegex.getMgId();
            
            if (databaseObjectsInTableByMetricGroupId.containsKey(metricGroupId)) {
                List<MetricGroupRegex> databaseObjects = databaseObjectsInTableByMetricGroupId.get(metricGroupId);
                databaseObjects.add(metricGroupRegex);
            }
            else {
                List<MetricGroupRegex> databaseObjects = new ArrayList<>();
                databaseObjects.add(metricGroupRegex);
                databaseObjectsInTableByMetricGroupId.put(metricGroupId, databaseObjects);
            }
        }
        
        return databaseObjectsInTableByMetricGroupId;
    }
    
    public List<MetricGroupRegex> getMetricGroupRegexesByMetricGroupId(int metricGroupId) {
        
        try {

            if (!isConnectionValid()) {
                return null;
            }

            databaseInterface_.createPreparedStatement(MetricGroupRegexesSql.Select_MetricGroupRegexes_ByMetricGroupId, 100);
            databaseInterface_.addPreparedStatementParameters(metricGroupId);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return null;
            }

            List<MetricGroupRegex> metricGroupRegexes = new ArrayList<>();
            ResultSet resultSet = databaseInterface_.getResults();

            while (resultSet.next()) {
                MetricGroupRegex databaseObject = processSingleResultAllColumns(resultSet);
                metricGroupRegexes.add(databaseObject);
            }

            return metricGroupRegexes;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public boolean deleteByMetricGroupId(int id) {
        return delete(MetricGroupRegexesSql.Delete_MetricGroupRegex_ByMetricGroupId, 
                id); 
    }
    
    public List<String> getPatterns(Integer metricGroupId) {

        if (metricGroupId == null) {
            return null;
        }

        List<String> patterns = null;
                
        List<MetricGroupRegex> metricGroupRegexes = getMetricGroupRegexesByMetricGroupId(metricGroupId);

        if (metricGroupRegexes != null) {
            patterns = new ArrayList<>();
            
            for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
                if (metricGroupRegex.getPattern() != null) {
                    patterns.add(metricGroupRegex.getPattern());
                }
            }
        }
        
        return patterns;
    }
    
}
