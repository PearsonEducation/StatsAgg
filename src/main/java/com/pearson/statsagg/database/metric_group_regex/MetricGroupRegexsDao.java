package com.pearson.statsagg.database.metric_group_regex;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.database.DatabaseObjectDao;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupRegexsDao extends DatabaseObjectDao<MetricGroupRegex> {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupRegexsDao.class.getName());
   
    private final String tableName_ = "METRIC_GROUP_REGEXS";
    
    public MetricGroupRegexsDao(){}
            
    public MetricGroupRegexsDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public boolean dropTable() {
        return dropTable(MetricGroupRegexsSql.DropTable_MetricGroupRegexs);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        databaseCreationSqlStatements.add(MetricGroupRegexsSql.CreateTable_MetricGroupRegexs);
        databaseCreationSqlStatements.add(MetricGroupRegexsSql.CreateIndex_MetricGroupRegexs_PrimaryKey);
        databaseCreationSqlStatements.add(MetricGroupRegexsSql.CreateIndex_MetricGroupRegexs_ForeignKey_MetricGroupId);
        
        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public MetricGroupRegex getDatabaseObject(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return null;
        
        return getDatabaseObject(MetricGroupRegexsSql.Select_MetricGroupRegex_ByPrimaryKey, 
                metricGroupRegex.getId()); 
    }
        
    @Override
    public boolean insert(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return false;
        
        return insert(MetricGroupRegexsSql.Insert_MetricGroupRegex, 
                metricGroupRegex.getMgId(), metricGroupRegex.getPattern());
    }
    
    @Override
    public boolean update(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return false;

        return update(MetricGroupRegexsSql.Update_MetricGroupRegex_ByPrimaryKey, 
                metricGroupRegex.getMgId(), metricGroupRegex.getPattern(), metricGroupRegex.getId());
    }

    @Override
    public boolean delete(MetricGroupRegex metricGroupRegex) {
        if (metricGroupRegex == null) return false;
        
        return delete(MetricGroupRegexsSql.Delete_MetricGroupRegex_ByPrimaryKey, 
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
            
            String pattern = resultSet.getString("PATTERN");

            MetricGroupRegex metricGroupRegex = new MetricGroupRegex(id, mgId, pattern);
            
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
        return getDatabaseObject(MetricGroupRegexsSql.Select_MetricGroupRegex_ByPrimaryKey, 
                id); 
    }  
    
    public Map<Integer,List<MetricGroupRegex>> getAllMetricGroupRegexsByMetricGroupId() {
        List<MetricGroupRegex> metricGroupRegexs = getAllDatabaseObjectsInTable();
        
        if ((metricGroupRegexs == null) || metricGroupRegexs.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<Integer,List<MetricGroupRegex>> databaseObjectsInTableByMetricGroupId = new HashMap<>();

        for (MetricGroupRegex metricGroupRegex : metricGroupRegexs) {
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
    
    public List<MetricGroupRegex> getMetricGroupRegexsByMetricGroupId(int metricGroupId) {
        
        try {

            if (!isConnectionValid()) {
                return new ArrayList<>();
            }

            databaseInterface_.createPreparedStatement(MetricGroupRegexsSql.Select_MetricGroupRegexs_ByMetricGroupId, 100);
            databaseInterface_.addPreparedStatementParameters(metricGroupId);
            databaseInterface_.executePreparedStatement();
            
            if (!databaseInterface_.isResultSetValid()) {
                return new ArrayList<>();
            }

            List<MetricGroupRegex> metricGroupRegexs = new ArrayList<>();
            ResultSet resultSet = databaseInterface_.getResults();

            while (resultSet.next()) {
                MetricGroupRegex databaseObject = processSingleResultAllColumns(resultSet);
                metricGroupRegexs.add(databaseObject);
            }

            return metricGroupRegexs;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return new ArrayList<>();
        }
        finally {
            databaseInterface_.cleanupAutomatic();
        } 
        
    }
    
    public boolean deleteByMetricGroupId(int id) {
        return delete(MetricGroupRegexsSql.Delete_MetricGroupRegex_ByMetricGroupId, 
                id); 
    }
    
    public List<String> getPatterns(Integer metricGroupId) {

        if (metricGroupId == null) {
            return new ArrayList<>();
        }

        List<String> patterns = new ArrayList<>();
                
        List<MetricGroupRegex> metricGroupRegexs = getMetricGroupRegexsByMetricGroupId(metricGroupId);

        if (metricGroupRegexs != null) {
            for (MetricGroupRegex metricGroupRegex : metricGroupRegexs) {
                if (metricGroupRegex.getPattern() != null) {
                    patterns.add(metricGroupRegex.getPattern());
                }
            }
        }
        
        return patterns;
    }
    
}
