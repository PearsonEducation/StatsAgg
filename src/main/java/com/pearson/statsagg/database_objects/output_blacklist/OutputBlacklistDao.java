package com.pearson.statsagg.database_objects.output_blacklist;

import com.pearson.statsagg.database_engine.DatabaseInterface;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.database_engine.DatabaseObjectDao;
import com.pearson.statsagg.globals.DatabaseConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OutputBlacklistDao extends DatabaseObjectDao<OutputBlacklist> {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputBlacklistDao.class.getName());
    
    private final String tableName_ = "OUTPUT_BLACKLIST";
    
    public OutputBlacklistDao(){}
            
    public OutputBlacklistDao(boolean closeConnectionAfterOperation) {
        databaseInterface_.setCloseConnectionAfterOperation(closeConnectionAfterOperation);
    }
    
    public OutputBlacklistDao(DatabaseInterface databaseInterface) {
        super(databaseInterface);
    }
    
    public boolean dropTable() {
        return dropTable(OutputBlacklistSql.DropTable_OutputBlacklist);
    }
    
    public boolean createTable() {
        List<String> databaseCreationSqlStatements = new ArrayList<>();
        
        if (DatabaseConfiguration.getType() == DatabaseConfiguration.MYSQL) {
            databaseCreationSqlStatements.add(OutputBlacklistSql.CreateTable_OutputBlacklist_MySQL);
        }
        else {
            databaseCreationSqlStatements.add(OutputBlacklistSql.CreateTable_OutputBlacklist_Derby);
            databaseCreationSqlStatements.add(OutputBlacklistSql.CreateIndex_OutputBlacklist_PrimaryKey);
        }
        
        databaseCreationSqlStatements.add(OutputBlacklistSql.CreateIndex_OutputBlacklist_ForeignKey_MetricGroupId);

        return createTable(databaseCreationSqlStatements);
    }
    
    @Override
    public OutputBlacklist getDatabaseObject(OutputBlacklist outputBlacklist) {
        if (outputBlacklist == null) return null;
        return getDatabaseObject(OutputBlacklistSql.Select_OutputBlacklist_ByPrimaryKey, outputBlacklist.getId()); 
    }
    
    @Override
    public boolean insert(OutputBlacklist outputBlacklist) {
        if (outputBlacklist == null) return false;
        return insert(OutputBlacklistSql.Insert_OutputBlacklist, outputBlacklist.getMetricGroupId());
    }
    
    @Override
    public boolean update(OutputBlacklist outputBlacklist) {
        if (outputBlacklist == null) return false;
        return update(OutputBlacklistSql.Update_OutputBlacklist_ByPrimaryKey, outputBlacklist.getMetricGroupId(), outputBlacklist.getId());
    }

    @Override
    public boolean delete(OutputBlacklist outputBlacklist) {
        if (outputBlacklist == null) return false;
        return delete(OutputBlacklistSql.Delete_OutputBlacklist_ByPrimaryKey, outputBlacklist.getId()); 
    }
    
    @Override
    public OutputBlacklist processSingleResultAllColumns(ResultSet resultSet) {
        
        try {     
            if ((resultSet == null) || resultSet.isClosed()) {
                return null;
            }

            Integer id = resultSet.getInt("ID");
            if (resultSet.wasNull()) id = null;
            
            Integer mgId = resultSet.getInt("METRIC_GROUP_ID");
            if (resultSet.wasNull()) mgId = null;

            OutputBlacklist outputBlacklist = new OutputBlacklist(id, mgId);
            
            return outputBlacklist;
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
    
    public static OutputBlacklist getSingleOutputBlacklistRow() {
        
        OutputBlacklist outputBlacklist = null;
        
        OutputBlacklistDao outputBlacklistDao = new OutputBlacklistDao();
        List<OutputBlacklist> outputBlacklists = outputBlacklistDao.getAllDatabaseObjectsInTable();
        
        if ((outputBlacklists != null) && outputBlacklists.size() > 1) {
            logger.warn("There should not be more than one output blacklist row in the database.");
        }
        
        if ((outputBlacklists != null) && !outputBlacklists.isEmpty()) {
            for (OutputBlacklist outputBlacklistFromDb : outputBlacklists) {
                outputBlacklist = outputBlacklistFromDb;
                break;
            }
        }
        
        return outputBlacklist;
    }

}
