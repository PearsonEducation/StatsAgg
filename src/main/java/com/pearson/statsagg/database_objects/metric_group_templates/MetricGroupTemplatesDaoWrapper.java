package com.pearson.statsagg.database_objects.metric_group_templates;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.database_objects.AbstractDaoWrapper;
import com.pearson.statsagg.globals.GlobalVariables;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTemplatesDaoWrapper extends AbstractDaoWrapper {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplatesDaoWrapper.class.getName());
    
    private static final String HUMAN_FRIENDLY_NAME = "metric group template";
    private static final String LOG_FRIENDLY_NAME = "MetricGroupTemplate";
    
    private final MetricGroupTemplate metricGroupTemplate_;

    private MetricGroupTemplatesDaoWrapper(MetricGroupTemplate metricGroupTemplate) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, true);
        this.metricGroupTemplate_ = metricGroupTemplate;
    }
    
    private MetricGroupTemplatesDaoWrapper(MetricGroupTemplate metricGroupTemplate, String oldName) {
        super(HUMAN_FRIENDLY_NAME, LOG_FRIENDLY_NAME, oldName);
        this.metricGroupTemplate_ = metricGroupTemplate;
    }

    private MetricGroupTemplatesDaoWrapper alterRecordInDatabase() {
        
        if ((metricGroupTemplate_ == null) || (metricGroupTemplate_.getName() == null)) {
            getReturnString_AlterFail_InitialChecks();
            return this;
        }

        Connection connection = DatabaseConnections.getConnection(false);

        try {
            getReturnString_AlterInitialValue(metricGroupTemplate_.getName());

            MetricGroupTemplate metricGroupTemplateFromDb = MetricGroupTemplatesDao.getMetricGroupTemplate_FilterByUppercaseName(connection, false, metricGroupTemplate_.getName());

            if (isNewDatabaseObject_ && (metricGroupTemplateFromDb != null)) { 
                getReturnString_CreateFail_SameNameAlreadyExists(metricGroupTemplate_.getName());
            }
            else {
                boolean isUpsertSuccess;
                if (oldDatabaseObjectName_ == null) isUpsertSuccess = MetricGroupTemplatesDao.upsert(connection, false, true, metricGroupTemplate_);
                else isUpsertSuccess = MetricGroupTemplatesDao.upsert(connection, false, true, metricGroupTemplate_, oldDatabaseObjectName_);

                if (isUpsertSuccess) getReturnString_AlterSuccess(metricGroupTemplate_.getName());
                else getReturnString_AlterFail(metricGroupTemplate_.getName());
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }

        return this;
    }

    private MetricGroupTemplatesDaoWrapper deleteRecordInDatabase() {
        
        if ((metricGroupTemplate_ == null)) {
            getReturnString_DeleteFail_RecordNotFound();
            return this;
        }
       
        if ((metricGroupTemplate_ == null) || (metricGroupTemplate_.getId() == null)) {
            getReturnString_DeleteFail_InitialChecks();
            return this;
        }
        
        Connection connection = null;

        try {
            String metricGroupTemplateName = (metricGroupTemplate_ != null) ? metricGroupTemplate_.getName() : null;
            getReturnString_DeleteInitialValue(metricGroupTemplateName);

            connection = DatabaseConnections.getConnection(false);

            boolean didDeleteMetricGroupTemplateSucceed = MetricGroupTemplatesDao.delete(connection, false, false, metricGroupTemplate_);

            if (didDeleteMetricGroupTemplateSucceed) {
                boolean didCommitSucceed = DatabaseUtils.commit(connection, false);

                if (didCommitSucceed) {
                    getReturnString_DeleteSuccess(metricGroupTemplateName);
                }
                else {
                    DatabaseUtils.rollback(connection);
                    getReturnString_DeleteFail(metricGroupTemplateName);
                }
            }
            else {
                DatabaseUtils.rollback(connection);
                getReturnString_DeleteFail(metricGroupTemplateName);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return this;
    }

    public static MetricGroupTemplatesDaoWrapper createRecordInDatabase(MetricGroupTemplate metricGroupTemplate) {
        MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = new MetricGroupTemplatesDaoWrapper(metricGroupTemplate);
        return metricGroupTemplatesDaoWrapper.alterRecordInDatabase();
    }
    
    public static MetricGroupTemplatesDaoWrapper alterRecordInDatabase(MetricGroupTemplate metricGroupTemplate) {
        String metricGroupTemplateName = (metricGroupTemplate != null) ? metricGroupTemplate.getName() : null;
        MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = new MetricGroupTemplatesDaoWrapper(metricGroupTemplate, metricGroupTemplateName);
        return metricGroupTemplatesDaoWrapper.alterRecordInDatabase();
    }
    
    public static MetricGroupTemplatesDaoWrapper alterRecordInDatabase(MetricGroupTemplate metricGroupTemplate, String oldName) {
        MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = new MetricGroupTemplatesDaoWrapper(metricGroupTemplate, oldName);
        return metricGroupTemplatesDaoWrapper.alterRecordInDatabase();
    }

    public static MetricGroupTemplatesDaoWrapper deleteRecordInDatabase(String metricGroupTemplateName) {
        MetricGroupTemplate metricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, metricGroupTemplateName);
        MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = new MetricGroupTemplatesDaoWrapper(metricGroupTemplate);
        return metricGroupTemplatesDaoWrapper.deleteRecordInDatabase();
    }
    
    public static MetricGroupTemplatesDaoWrapper deleteRecordInDatabase(MetricGroupTemplate metricGroupTemplate) {
        MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = new MetricGroupTemplatesDaoWrapper(metricGroupTemplate);
        return metricGroupTemplatesDaoWrapper.deleteRecordInDatabase();
    }
    
}
