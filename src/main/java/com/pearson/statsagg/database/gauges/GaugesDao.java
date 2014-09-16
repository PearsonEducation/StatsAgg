package com.pearson.statsagg.database.gauges;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.database.DatabaseObjectDao;
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
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        databaseCreationSqlStatements.add(GaugesSql.CreateTable_Gauges);
        databaseCreationSqlStatements.add(GaugesSql.CreateIndex_Gauges_PrimaryKey);
        
        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public Gauge getDatabaseObject(Gauge gauge) {
        if (gauge == null) return null;
        
        return getDatabaseObject(GaugesSql.Select_Gauge_ByPrimaryKey, 
                gauge.getBucket()); 
    }
    
    @Override
    public boolean insert(Gauge gauge) {
        if (gauge == null) return false;

        return insert(GaugesSql.Insert_Gauge, 
                gauge.getBucket(), gauge.getMetricValue(), gauge.getLastModified());
    }
    
    @Override
    public boolean update(Gauge gauge) {
        if (gauge == null) return false;
        
        return update(GaugesSql.Update_Gauge_ByPrimaryKey, 
                gauge.getBucket(), gauge.getMetricValue(), gauge.getLastModified(), gauge.getBucket());
    }

    @Override
    public boolean delete(Gauge gauge) {
        if (gauge == null) return false;
        
        return delete(GaugesSql.Delete_Gauge_ByPrimaryKey, 
                gauge.getBucket()); 
    }
    
    @Override
    public Gauge processSingleResultAllColumns(ResultSet result) {
        
        try {     
            if ((result == null) || result.isClosed()) {
                return null;
            }

            String bucket = result.getString("BUCKET");
            BigDecimal metricValue = result.getBigDecimal("METRIC_VALUE");
            Timestamp lastModified = result.getTimestamp("LAST_MODIFIED");
            
            Gauge gauge = new Gauge(bucket, metricValue, lastModified);
            
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
    
    public Gauge getGauge(String bucket) {
        return getDatabaseObject(GaugesSql.Select_Gauge_ByPrimaryKey, 
                bucket); 
    }

    public boolean delete(String bucket) {
        return delete(GaugesSql.Delete_Gauge_ByPrimaryKey, 
                bucket); 
    }
    
}
