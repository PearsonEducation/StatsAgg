package com.pearson.statsagg.database_objects.metric_group_templates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTemplatesDao {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplatesDao.class.getName());
  
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTemplate metricGroupTemplate) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupTemplatesSql.Insert_MetricGroupTemplate, 
                    metricGroupTemplate.getName(), metricGroupTemplate.getUppercaseName(), metricGroupTemplate.getVariableSetListId(), 
                    metricGroupTemplate.getMetricGroupNameVariable(), metricGroupTemplate.getDescriptionVariable(), 
                    metricGroupTemplate.getMatchRegexesVariable(), metricGroupTemplate.getBlacklistRegexesVariable(), metricGroupTemplate.getTagsVariable(), 
                    metricGroupTemplate.isMarkedForDelete());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTemplate metricGroupTemplate) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupTemplatesSql.Update_MetricGroupTemplate_ByPrimaryKey, 
                    metricGroupTemplate.getName(), metricGroupTemplate.getUppercaseName(), metricGroupTemplate.getVariableSetListId(), 
                    metricGroupTemplate.getMetricGroupNameVariable(), metricGroupTemplate.getDescriptionVariable(), 
                    metricGroupTemplate.getMatchRegexesVariable(), metricGroupTemplate.getBlacklistRegexesVariable(), metricGroupTemplate.getTagsVariable(),
                    metricGroupTemplate.isMarkedForDelete(),
                    metricGroupTemplate.getId());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTemplate metricGroupTemplate) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricGroupTemplate metricGroupTemplateFromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, metricGroupTemplate.getId());

            boolean upsertSuccess = true;
            if (metricGroupTemplateFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, metricGroupTemplate);
            else if (!metricGroupTemplateFromDb.isEqual(metricGroupTemplate)) upsertSuccess = update(connection, false, commitOnCompletion, metricGroupTemplate);

            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
            
            return upsertSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }

    }
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTemplate metricGroupTemplate, String oldMetricGroupTemplateName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            MetricGroupTemplate metricGroupTemplateFromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, oldMetricGroupTemplateName);

            boolean upsertSuccess = true;
            if (metricGroupTemplateFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, metricGroupTemplate);
            }
            else {
                metricGroupTemplate.setId(metricGroupTemplateFromDb.getId());
                if (!metricGroupTemplateFromDb.isEqual(metricGroupTemplate)) upsertSuccess = update(connection, false, commitOnCompletion, metricGroupTemplate);
            }

            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, true);
            
            return upsertSuccess;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }

    }
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, MetricGroupTemplate metricGroupTemplate) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    MetricGroupTemplatesSql.Delete_MetricGroupTemplate_ByPrimaryKey, 
                    metricGroupTemplate.getId());
            
            return (result >= 0);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static MetricGroupTemplate getMetricGroupTemplate(Connection connection, boolean closeConnectionOnCompletion, Integer metricGroupTemplateId) {
        
        try {
            List<MetricGroupTemplate> metricGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTemplatesResultSetHandler(), 
                    MetricGroupTemplatesSql.Select_MetricGroupTemplate_ByPrimaryKey, metricGroupTemplateId);
            
            return DatabaseUtils.getSingleResultFromList(metricGroupTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static List<MetricGroupTemplate> getMetricGroupTemplates(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroupTemplate> metricGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                        new MetricGroupTemplatesResultSetHandler(), 
                        MetricGroupTemplatesSql.Select_AllMetricGroupTemplates);
            
            return metricGroupTemplates;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
        
    public static MetricGroupTemplate getMetricGroupTemplate(Connection connection, boolean closeConnectionOnCompletion, String metricGroupTemplateName) {
        
        try {
            List<MetricGroupTemplate> metricGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTemplatesResultSetHandler(), 
                    MetricGroupTemplatesSql.Select_MetricGroupTemplate_ByName, metricGroupTemplateName);
            
            return DatabaseUtils.getSingleResultFromList(metricGroupTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static MetricGroupTemplate getMetricGroupTemplate_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, String metricGroupTemplateName) {
        
        try {
            List<MetricGroupTemplate> metricGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTemplatesResultSetHandler(), 
                    MetricGroupTemplatesSql.Select_MetricGroupTemplate_ByUppercaseName, metricGroupTemplateName.toUpperCase());
            
            return DatabaseUtils.getSingleResultFromList(metricGroupTemplates);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Set<String> getMetricGroupTemplateNames(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<MetricGroupTemplate> metricGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                        new MetricGroupTemplatesResultSetHandler(), 
                        MetricGroupTemplatesSql.Select_MetricGroupTemplateNames);
            
            Set<String> metricGroupTemplateNames = new HashSet<>();
            if (metricGroupTemplates == null) return metricGroupTemplateNames;
            
            for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
                if ((metricGroupTemplate != null) && (metricGroupTemplate.getName() != null)) metricGroupTemplateNames.add(metricGroupTemplate.getName());
            }
            
            return metricGroupTemplateNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<String,MetricGroupTemplate> getMetricGroupTemplates_ByName(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<String,MetricGroupTemplate> metricGroupTemplates_ByName = new HashMap<>();
            
            List<MetricGroupTemplate> metricGroupTemplates = getMetricGroupTemplates(connection, false);
            
            if (metricGroupTemplates != null) {
                for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
                    if ((metricGroupTemplate == null) || (metricGroupTemplate.getName() == null)) continue;
                    metricGroupTemplates_ByName.put(metricGroupTemplate.getName(), metricGroupTemplate);
                }
            }

            return metricGroupTemplates_ByName;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static Map<Integer,MetricGroupTemplate> getMetricGroupTemplates_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<Integer,MetricGroupTemplate> metricGroupTemplates_ById = new HashMap<>();
            
            List<MetricGroupTemplate> metricGroupTemplates = getMetricGroupTemplates(connection, false);
            if (metricGroupTemplates == null) return null;
            
            for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
                if ((metricGroupTemplate == null) || (metricGroupTemplate.getId() == null)) continue;
                metricGroupTemplates_ById.put(metricGroupTemplate.getId(), metricGroupTemplate);
            }

            return metricGroupTemplates_ById;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<String> getMetricGroupTemplateNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> metricGroupTemplateNames = new ArrayList<>();
            
            List<MetricGroupTemplate> metricGroupTemplates = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new MetricGroupTemplatesResultSetHandler(), 
                    (resultSetLimit + 5), 
                    MetricGroupTemplatesSql.Select_MetricGroupTemplate_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((metricGroupTemplates == null) || metricGroupTemplates.isEmpty()) return metricGroupTemplateNames;
            
            int rowCounter = 0;
            for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
                if ((metricGroupTemplate != null) && (metricGroupTemplate.getName() != null)) {
                    metricGroupTemplateNames.add(metricGroupTemplate.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return metricGroupTemplateNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

}
