package com.pearson.statsagg.web_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
public class MetricGroupsList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsList.class.getName());
    
    public static final String PAGE_NAME = "API_MetricGroups_List";
 
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        
        PrintWriter out = null;
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        try {    
            String json = getMetricGroupsList(request);       
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }  
        finally {            
            if (out != null) {
                out.close();
            }
        } 
        
    }

    /**
     * Returns a json object containing a list of metric groups.
     * 
     * @param request servlet request
     * @return list of metric groups
     */ 
    protected String getMetricGroupsList(HttpServletRequest request) {

        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }

        Connection connection = DatabaseConnections.getConnection();

        try {
            List<MetricGroup> metricGroups = MetricGroupsDao.getMetricGroups(connection, false);
            if (metricGroups == null) metricGroups = new ArrayList<>();
            
            List<JsonObject> metricGroupsJsonObjects = new ArrayList<>();
            
            for (MetricGroup metricGroup : metricGroups) {
                JsonObject metricGroupJsonObject = MetricGroup.getJsonObject_ApiFriendly(metricGroup, false, -1);
                if (metricGroupJsonObject != null) metricGroupsJsonObjects.add(metricGroupJsonObject);
            }
            
            DatabaseUtils.cleanup(connection);
            
            Gson metricGroupsGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonElement metricGroups_JsonElement = metricGroupsGson.toJsonTree(metricGroupsJsonObjects);
            JsonArray jsonArray = new Gson().toJsonTree(metricGroups_JsonElement).getAsJsonArray();
            String metricGroupsJson = metricGroupsGson.toJson(jsonArray);
            
            return metricGroupsJson;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return Helper.ERROR_UNKNOWN_JSON;
        }
        finally {
            try {
                DatabaseUtils.cleanup(connection);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
    }
     
}
