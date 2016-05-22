package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 */
@WebServlet(name="API_MetricGroupDetails", urlPatterns={"/api/metric-group-details"})
public class MetricGroupDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "API_MetricGroupDetails";
    
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
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            String json = getMetricGroupDetails(request);
            response.setContentType("application/json");
            PrintWriter out;
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    /**
     * Returns a json string containing the details of the requested metric group.
     * 
     * @param request servlet request
     * @return details of the requested metric group
     */
    protected String getMetricGroupDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_JSON;
        }
        
        try {
            Integer metricGroupId = null;
            String metricGroupName = null;

            if (request.getParameter("id") != null) metricGroupId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) metricGroupName = request.getParameter("name");

            if ((metricGroupId == null) && (metricGroupName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequetBody(request);
                metricGroupId = Helper.getIntegerFieldFromJsonObject(jsonObject, "id");
                metricGroupName = Helper.getStringFieldFromJsonObject(jsonObject, "name");
            }

            MetricGroup metricGroup = null;
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
            if (metricGroupId != null) metricGroup = metricGroupsDao.getMetricGroup(metricGroupId);
            else if (metricGroupName != null) metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
            else metricGroupsDao.close();
            
            if (metricGroup != null) return getApiFriendlyJsonObject(metricGroup);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_JSON;
    }
    
    private String getApiFriendlyJsonObject(MetricGroup metricGroup) {
        
        if (metricGroup == null) {
            return null;
        }
        
        Gson metricGroup_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
        JsonElement metricGroup_JsonElement = metricGroup_Gson.toJsonTree(metricGroup);
        JsonObject jsonObject = new Gson().toJsonTree(metricGroup_JsonElement).getAsJsonObject();
        
        return metricGroup_Gson.toJson(jsonObject);
    }
    
}
