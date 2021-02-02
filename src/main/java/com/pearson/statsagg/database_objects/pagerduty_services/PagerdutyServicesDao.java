package com.pearson.statsagg.database_objects.pagerduty_services;

import java.util.List;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class PagerdutyServicesDao {
    
    private static final Logger logger = LoggerFactory.getLogger(PagerdutyServicesDao.class.getName());
    
    public static boolean insert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, PagerdutyService pagerdutyService) {
        
        try {                   
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    PagerdutyServicesSql.Insert_PagerdutyService, 
                    pagerdutyService.getName(), pagerdutyService.getUppercaseName(), pagerdutyService.getDescription(), pagerdutyService.getRoutingKey());
            
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
    
    public static boolean update(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, PagerdutyService pagerdutyService) {
        
        try {                    
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    PagerdutyServicesSql.Update_PagerdutyService_ByPrimaryKey, 
                    pagerdutyService.getName(), pagerdutyService.getUppercaseName(), pagerdutyService.getDescription(), pagerdutyService.getRoutingKey(), pagerdutyService.getId());
            
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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, PagerdutyService pagerdutyService) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            PagerdutyService pagerdutyServiceFromDb = PagerdutyServicesDao.getPagerdutyService(connection, false, pagerdutyService.getId());

            boolean upsertSuccess = true;
            if (pagerdutyServiceFromDb == null) upsertSuccess = insert(connection, false, commitOnCompletion, pagerdutyService);
            else if (!pagerdutyServiceFromDb.isEqual(pagerdutyService)) upsertSuccess = update(connection, false, commitOnCompletion, pagerdutyService);

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
    
    public static boolean upsert(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, PagerdutyService pagerdutyService, String oldPagerdutyServiceName) {
        
        try {                   
            boolean isConnectionInitiallyAutoCommit = connection.getAutoCommit();
            if (isConnectionInitiallyAutoCommit) DatabaseUtils.setAutoCommit(connection, false);
            
            PagerdutyService pagerdutyServiceFromDb = PagerdutyServicesDao.getPagerdutyService(connection, false, oldPagerdutyServiceName);

            boolean upsertSuccess = true;
            if (pagerdutyServiceFromDb == null) {
                upsertSuccess = insert(connection, false, commitOnCompletion, pagerdutyService);
            }
            else {
                pagerdutyService.setId(pagerdutyServiceFromDb.getId());
                if (!pagerdutyServiceFromDb.isEqual(pagerdutyService)) upsertSuccess = update(connection, false, commitOnCompletion, pagerdutyService);
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
    
    public static boolean delete(Connection connection, boolean closeConnectionOnCompletion, boolean commitOnCompletion, PagerdutyService pagerdutyService) {
        
        try {                 
            long result = DatabaseUtils.dml_PreparedStatement(connection, closeConnectionOnCompletion, commitOnCompletion, 
                    PagerdutyServicesSql.Delete_PagerdutyService_ByPrimaryKey, 
                    pagerdutyService.getId());
            
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

    public static PagerdutyService getPagerdutyService(Connection connection, boolean closeConnectionOnCompletion, Integer id) {
        
        try {
            List<PagerdutyService> pagerdutyServices = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new PagerdutyServicesResultSetHandler(), 
                    PagerdutyServicesSql.Select_PagerdutyService_ByPrimaryKey, id);
            
            return DatabaseUtils.getSingleResultFromList(pagerdutyServices);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static PagerdutyService getPagerdutyService(Connection connection, boolean closeConnectionOnCompletion, String pagerdutyServiceName) {
        
        try {
            List<PagerdutyService> pagerdutyServices = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new PagerdutyServicesResultSetHandler(), 
                    PagerdutyServicesSql.Select_PagerdutyService_ByName, pagerdutyServiceName);
            
            return DatabaseUtils.getSingleResultFromList(pagerdutyServices);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static PagerdutyService getPagerdutyService_FilterByUppercaseName(Connection connection, boolean closeConnectionOnCompletion, String pagerdutyServiceName) {
        
        try {
            List<PagerdutyService> pagerdutyServices = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new PagerdutyServicesResultSetHandler(), 
                    PagerdutyServicesSql.Select_PagerdutyService_ByUppercaseName, pagerdutyServiceName.toUpperCase());
            
            return DatabaseUtils.getSingleResultFromList(pagerdutyServices);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }

    }
    
    public static List<String> getPagerdutyServiceNames(Connection connection, boolean closeConnectionOnCompletion, String filter, Integer resultSetLimit) {
        
        try {
            List<String> pagerdutyServiceNames = new ArrayList<>();
            
            List<PagerdutyService> pagerdutyServices = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new PagerdutyServicesResultSetHandler(), 
                    (resultSetLimit + 5), 
                    PagerdutyServicesSql.Select_PagerdutyService_Names_OrderByName,
                    ("%" + filter + "%"));
            
            if ((pagerdutyServices == null) || pagerdutyServices.isEmpty()) return pagerdutyServiceNames;
            
            int rowCounter = 0;
            for (PagerdutyService pagerdutyService : pagerdutyServices) {
                if ((pagerdutyService != null) && (pagerdutyService.getName() != null)) {
                    pagerdutyServiceNames.add(pagerdutyService.getName());
                    rowCounter++;
                }
                
                if (rowCounter >= resultSetLimit) break;
            }
            
            return pagerdutyServiceNames;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static List<PagerdutyService> getPagerdutyServices(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            List<PagerdutyService> pagerdutyServices = DatabaseUtils.query_PreparedStatement(connection, closeConnectionOnCompletion, 
                    new PagerdutyServicesResultSetHandler(), 
                    PagerdutyServicesSql.Select_AllPagerdutyServices);
            
            return pagerdutyServices;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {
            if (closeConnectionOnCompletion) DatabaseUtils.cleanup(connection);
        }
        
    }

    public static Map<Integer, PagerdutyService> getPagerdutyServices_ById(Connection connection, boolean closeConnectionOnCompletion) {
        
        try {
            Map<Integer, PagerdutyService> pagerdutyServicesById = new HashMap<>();

            List<PagerdutyService> pagerdutyServices = getPagerdutyServices(connection, closeConnectionOnCompletion);
            if (pagerdutyServices == null) return null;

            for (PagerdutyService pagerdutyService : pagerdutyServices) {
                if (pagerdutyService.getId() != null) {
                    pagerdutyServicesById.put(pagerdutyService.getId(), pagerdutyService);
                }
            }
            
            return pagerdutyServicesById;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }

    }
    
}
