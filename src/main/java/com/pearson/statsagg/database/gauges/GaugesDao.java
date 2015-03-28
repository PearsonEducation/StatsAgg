package com.pearson.statsagg.database.gauges;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
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
public class GaugesDao extends DatabaseObjectDao<Gauge> {
    
    private static final Logger logger = LoggerFactory.getLogger(GaugesDao.class.getName());
    
    private final String tableName_ = "GAUGES";
    
    public GaugesDao(){}
            
    public GaugesDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public boolean dropTable() {
        return dropTable(GaugesSql.DropTable_Gauges);
    }
    
    public boolean truncateTable() {
        return truncateTable(GaugesSql.TruncateTable_Gauges);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(GaugesSql.CreateTable_Gauges_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(GaugesSql.CreateTable_Gauges_Derby);
        }
        
        databaseCreationSqlStatements.add(GaugesSql.CreateIndex_Gauges_PrimaryKey);

        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public Gauge getDatabaseObject(Gauge gauge) {
        if (gauge == null) return null;
        
        return getDatabaseObject(GaugesSql.Select_Gauge_ByPrimaryKey, 
                gauge.getBucketSha1()); 
    }
    
    @Override
    public boolean insert(Gauge gauge) {
        if (gauge == null) return false;

        return insert(GaugesSql.Insert_Gauge, 
                gauge.getBucketSha1(), gauge.getBucket(), gauge.getMetricValue(), gauge.getLastModified());
    }
    
    @Override
    public boolean update(Gauge gauge) {
        if (gauge == null) return false;
        
        return update(GaugesSql.Update_Gauge_ByPrimaryKey, 
                gauge.getBucket(), gauge.getMetricValue(), gauge.getLastModified(), gauge.getBucketSha1());
    }

    @Override
    public boolean delete(Gauge gauge) {
        if (gauge == null) return false;
        
        return delete(GaugesSql.Delete_Gauge_ByPrimaryKey, 
                gauge.getBucketSha1()); 
    }
    
    @Override
    public Gauge processSingleResultAllColumns(ResultSet resultSet) {
        
        try {     
            if ((resultSet == null) || resultSet.isClosed()) {
                return null;
            }

            String bucketSha1 = resultSet.getString("BUCKET_SHA1");
            if (resultSet.wasNull()) bucketSha1 = null;
            
            String bucket = resultSet.getString("BUCKET");
            if (resultSet.wasNull()) bucket = null;
            
            BigDecimal metricValue = resultSet.getBigDecimal("METRIC_VALUE");
            if (resultSet.wasNull()) metricValue = null;
            
            Timestamp lastModified = resultSet.getTimestamp("LAST_MODIFIED");
            if (resultSet.wasNull()) lastModified = null;

            Gauge gauge = new Gauge(bucketSha1, bucket, metricValue, lastModified);
            
            return gauge;
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
    
    public Gauge getGauge(String bucketSha1) {
        return getDatabaseObject(GaugesSql.Select_Gauge_ByPrimaryKey, 
                bucketSha1); 
    }

    public boolean delete(String bucketSha1) {
        return delete(GaugesSql.Delete_Gauge_ByPrimaryKey, 
                bucketSha1); 
    }
    
    public boolean batchUpsert(List<Gauge> gauges) {
        
        if ((gauges == null) || gauges.isEmpty()) {
            return false;
        }

        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            boolean wasAllUpsertSuccess = true;
            List<List<Gauge>> gaugesPartitions = Lists.partition(gauges, 1000);
            
            for (List<Gauge> gaugesPartition : gaugesPartitions) {
                List<Object> parameters = new ArrayList<>();

                for (Gauge gauge : gaugesPartition) {
                    parameters.add(gauge.getBucketSha1());
                    parameters.add(gauge.getBucket());
                    parameters.add(gauge.getMetricValue());
                    parameters.add(gauge.getLastModified());
                }

                boolean wasUpsertSuccess = genericDmlStatement(GaugesSql.generateBatchUpsert(gaugesPartition.size()), parameters);
                if (!wasUpsertSuccess) wasAllUpsertSuccess = false;
            }
            
            return wasAllUpsertSuccess;
        }
        else {
            return upsert(gauges, true);
        }
        
    }
    
}
