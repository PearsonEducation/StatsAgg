package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
@WebServlet(name="API_MetricGroups_List", urlPatterns={"/api/metric-groups-list"})
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
        
        try {
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao(false);
            List<MetricGroup> metricGroups = metricGroupsDao.getAllDatabaseObjectsInTable();
            if (metricGroups == null) metricGroups = new ArrayList<>();
            
            List<JsonObject> metricGroupsJsonObjects = new ArrayList<>();
            MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao(metricGroupsDao.getDatabaseInterface());
            MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao(metricGroupsDao.getDatabaseInterface());
            
            for (MetricGroup metricGroup : metricGroups) {
                List<MetricGroupRegex> metricGroupRegexes = new ArrayList<>();
                if (metricGroup != null) metricGroupRegexes = metricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(metricGroup.getId());
                
                List<MetricGroupTag> metricGroupTags = new ArrayList<>();
                if (metricGroup != null) metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroup.getId());
                
                JsonObject metricGroupJsonObject = MetricGroup.getJsonObject_ApiFriendly(metricGroup, metricGroupRegexes, metricGroupTags);
                if (metricGroupJsonObject != null) metricGroupsJsonObjects.add(metricGroupJsonObject);
            }
            
            metricGroupsDao.close();
            
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
        
    }
     
}
