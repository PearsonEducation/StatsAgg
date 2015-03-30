package com.pearson.statsagg.database.metric_last_seen;

import com.google.common.collect.Lists;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.database.DatabaseObjectDao;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricLastSeenDao extends DatabaseObjectDao<MetricLastSeen> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricLastSeenDao.class.getName());
    
    private final String tableName_ = "METRIC_LAST_SEEN";
    
    public MetricLastSeenDao(){}
            
    public MetricLastSeenDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public boolean dropTable() {
        return dropTable(MetricLastSeenSql.DropTable_MetricLastSeen);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(MetricLastSeenSql.CreateTable_MetricLastSeen_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(MetricLastSeenSql.CreateTable_MetricLastSeen_Derby);
        }
        
        databaseCreationSqlStatements.add(MetricLastSeenSql.CreateIndex_MetricLastSeen_PrimaryKey);

        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public MetricLastSeen getDatabaseObject(MetricLastSeen metricLastSeen) {
        if (metricLastSeen == null) return null;
        
        return getDatabaseObject(MetricLastSeenSql.Select_MetricLastSeen_ByPrimaryKey, 
                metricLastSeen.getMetricKeySha1()); 
    }
    
    @Override
    public boolean insert(MetricLastSeen metricLastSeen) {
        if (metricLastSeen == null) return false;

        return insert(MetricLastSeenSql.Insert_MetricLastSeen, 
                metricLastSeen.getMetricKeySha1(), metricLastSeen.getMetricKey(), metricLastSeen.getLastModified());
    }
    
    @Override
    public boolean update(MetricLastSeen metricLastSeen) {
        if (metricLastSeen == null) return false;
        
        return update(MetricLastSeenSql.Update_MetricLastSeen_ByPrimaryKey, 
                metricLastSeen.getMetricKey(), metricLastSeen.getLastModified(), metricLastSeen.getMetricKeySha1());
    }

    @Override
    public boolean delete(MetricLastSeen metricLastSeen) {
        if (metricLastSeen == null) return false;
        
        return delete(MetricLastSeenSql.Delete_MetricLastSeen_ByPrimaryKey, 
                metricLastSeen.getMetricKeySha1()); 
    }
    
    @Override
    public MetricLastSeen processSingleResultAllColumns(ResultSet resultSet) {
        
        try {     
            if ((resultSet == null) || resultSet.isClosed()) {
                return null;
            }

            String metricKeySha1 = resultSet.getString("METRIC_KEY_SHA1");
            if (resultSet.wasNull()) metricKeySha1 = null;
            
            String metricKey = resultSet.getString("METRIC_KEY");
            if (resultSet.wasNull()) metricKey = null;
            
            Timestamp lastModified = resultSet.getTimestamp("LAST_MODIFIED");
            if (resultSet.wasNull()) lastModified = null;

            MetricLastSeen metricLastSeen = new MetricLastSeen(metricKeySha1, metricKey, lastModified);
            
            return metricLastSeen;
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
    
    public MetricLastSeen getMetricLastSeen(String metricKeySha1) {
        return getDatabaseObject(MetricLastSeenSql.Select_MetricLastSeen_ByPrimaryKey, 
                metricKeySha1); 
    }

    public boolean delete(String metricKeySha1) {
        return delete(MetricLastSeenSql.Delete_MetricLastSeen_ByPrimaryKey, 
                metricKeySha1); 
    }
    
    public boolean batchUpsert(List<MetricLastSeen> metricLastSeens) {
        
        if ((metricLastSeens == null) || metricLastSeens.isEmpty()) {
            return false;
        }

        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            boolean wasAllUpsertSuccess = true;
            List<List<MetricLastSeen>> metricLastSeenPartitions = Lists.partition(metricLastSeens, 1000);
            
            for (List<MetricLastSeen> metricLastSeenPartition : metricLastSeenPartitions) {
                List<Object> parameters = new ArrayList<>();

                for (MetricLastSeen metricLastSeen : metricLastSeenPartition) {
                    parameters.add(metricLastSeen.getMetricKeySha1());
                    parameters.add(metricLastSeen.getMetricKey());
                    parameters.add(metricLastSeen.getLastModified());
                }

                boolean wasUpsertSuccess = genericDmlStatement(MetricLastSeenSql.generateBatchUpsert(metricLastSeenPartition.size()), parameters);
                if (!wasUpsertSuccess) wasAllUpsertSuccess = false;
            }
            
            return wasAllUpsertSuccess;
        }
        else if (!databaseInterface_.isManualTransactionControl()) {
            return upsert(metricLastSeens, true);
        }
        else {
            boolean wasAllUpsertSuccess = true;
            
            for (MetricLastSeen metricLastSeen : metricLastSeens) {
                boolean wasUpsertSuccess = upsert(metricLastSeen);
                if (!wasUpsertSuccess) wasAllUpsertSuccess = false;
            }
            
            return wasAllUpsertSuccess;
        }
        
    }
    
}
